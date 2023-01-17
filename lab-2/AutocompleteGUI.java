/******************************************************************************
 *  Compilation:  javac AutocompleteGUI.java
 *  Execution:    java AutocompleteGUI dictionary.txt max-matches
 *  Dependencies: Autocompleter.java ParsedInput.java RangeBinarySearch.java Term.java
 *
 *  @author Matthew Drabick
 *  @author Ming-Yee Tsang
 *  @author Andrew Ward
 *  @author Peter Ljunglöf (minor changes to file reading etc.)
 *  @author Christian Sattler
 *
 *  Interactive GUI used to demonstrate the Autocompleter data type.
 *
 *     * Reads a list of terms and weights from a file, specified as a
 *       program argument.
 *
 *     * As the user types in a text box, display the top max-matches terms
 *       that start with the text that the user types.
 *
 *     * Displays the result in a browser if the user selects a term
 *       (by pressing enter, clicking a selection, or pressing the
 *       "Search Google" button).
 *
 *  BUG: Search bar and suggestion drop-down don't resize properly with window;
 *       they stay the same size when the window gets wider, and the weights
 *       get hidden when the window gets smaller.
 *
 ******************************************************************************/

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.LayoutStyle;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.MouseInputAdapter;
import java.awt.Color;
import java.awt.Container;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

@SuppressWarnings("serial")
public class AutocompleteGUI extends JFrame {

    private static final int DEF_WIDTH  = 850; // width of the GUI window
    private static final int DEF_HEIGHT = 400; // height of the GUI window

    // URL prefix for searches
    private static final String SEARCH_URL = "https://www.google.com/search?q=";

    // Display top k results
    private final int maxMatches;

    // Indicates whether to display weights next to query matches
    private boolean displayWeights = true;

    /**
     * Initializes the GUI, and the associated Autocompleter object
     * @param parsedInput the parsed input data (specifying the query terms etc.)
     */
    public AutocompleteGUI(ParsedInput parsedInput) {
        this.maxMatches = parsedInput.maxMatches;
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setTitle("Autocomplete Me");
        setPreferredSize(new Dimension(DEF_WIDTH, DEF_HEIGHT));
        pack();
        setLocationRelativeTo(null);
        Container content = getContentPane();
        GroupLayout layout = new GroupLayout(content);
        content.setLayout(layout);
        layout.setAutoCreateGaps(true);
        layout.setAutoCreateContainerGaps(true);

        final AutocompletePanel ap = new AutocompletePanel(parsedInput.dictionary);

        JLabel textLabel = new JLabel("Search query:");

        // Create and add a listener to the search button.
        JButton searchButton = new JButton("Search Google");
        searchButton.addActionListener(e -> searchOnline(ap.getSelectedText()));

        // Create and add a listener to a "show weights" checkbox.
        JCheckBox checkbox = new JCheckBox("Show weights", null, displayWeights);
        checkbox.addActionListener(e -> {
            displayWeights = !displayWeights;
            ap.getSuggestions(ap.searchText.getText());
        });

        // Define the layout of the window.
        layout.setHorizontalGroup(layout.createSequentialGroup()
            .addGroup(layout.createParallelGroup(GroupLayout.Alignment.TRAILING)
                .addComponent(textLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addComponent(checkbox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
            .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE)
            .addComponent(ap, 0, GroupLayout.DEFAULT_SIZE, DEF_WIDTH)
            .addComponent(searchButton, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE)
        );

        layout.setVerticalGroup(layout.createSequentialGroup()
            .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(textLabel)
                .addComponent(checkbox))
            .addComponent(ap)
            .addComponent(searchButton))
        );
    }

    /**
     * The panel that interfaces with the Autocompleter object.  It consists
     * of a search bar that text can be entered into, and a drop-down list
     * of suggestions auto-completing the user's query.
     *
     * The event handling is disgusting spaghetti code.
     */
    private class AutocompletePanel extends JPanel {
        private final JTextField searchText;      // the search bar
        private final Autocompleter auto;         // the Autocompleter object
        private final String[] results =
                new String[maxMatches];           // an array of matches
        private final JList<String> suggestions;  // a list of autocomplete matches
        private final JScrollPane scrollPane;     // the scroll bar on the side of the
        private final JPanel suggestionsPanel;    // the dropdown menu of suggestions
        private final int extraMargin = 5;        // extra room to leave at the bottom of
                                                  // the suggestion drop-down below the
                                                  // last suggestion

        // Keep these next two values in sync! - used to keep the search box
        // the same width as the drop-down
        // DEF_COLUMNS should be the number of characters in suggListLen

        // number of columns in the search text that is kept
        private final int DEF_COLUMNS = 45;

        // an example of one of the longest strings in the database
        private final String suggListLen = "<b>Harry Potter and the Deathly Hallows: Part 1 (2010)</b>";

        /**
         * Creates the Autocompleter object and the search bar and suggestion
         * drop-down portions of the GUI
         * @param dictionary the Autocompleter object is constructed from
         */
        public AutocompletePanel(Term[] dictionary) {
            super();

            // Create the autocomplete object
            auto = new Autocompleter(dictionary);

            GroupLayout layout = new GroupLayout(this);
            this.setLayout(layout);

            // create the search text, and allow the user to interact with it
            searchText = new JTextField(DEF_COLUMNS);
            searchText.setMaximumSize(new Dimension(
                searchText.getMaximumSize().width,
                searchText.getPreferredSize().height));
            searchText.getInputMap().put(KeyStroke.getKeyStroke("UP"), "none");
            searchText.getInputMap().put(KeyStroke.getKeyStroke("DOWN"), "none");
            searchText.addFocusListener(new FocusAdapter() {
                @Override
                public void focusGained(FocusEvent e) {
                    int pos = searchText.getText().length();
                    searchText.setCaretPosition(pos);
                }
            });

            // create the search text box
            JPanel searchTextPanel = new JPanel();
            searchTextPanel.add(searchText);
            searchTextPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
            searchTextPanel.setLayout(new GridLayout(1, 1));

            // create the drop-down menu items
            int fontSize = 13;
            int cellHeight = 20;

            suggestions = new JList<>(results);
            suggestions.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
            suggestions.setVisible(false);
            suggestions.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            suggestions.setMaximumSize(new Dimension(
                searchText.getMaximumSize().width,
                suggestions.getPreferredSize().height
            ));

            // Set to make equal to the width of the text field
            suggestions.setPrototypeCellValue(suggListLen);
            suggestions.setFont(suggestions.getFont().deriveFont(Font.PLAIN, fontSize));
            suggestions.setFixedCellHeight(cellHeight);

            // add arrow-key interactivity to the drop-down menu items
            Action makeSelection = new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (!suggestions.isSelectionEmpty()) {
                        String selection = suggestions.getSelectedValue();
                        if (displayWeights)
                            selection = selection.substring(0, selection.indexOf("<td width="));
                        selection = selection.replaceAll("<.*?>", "");
                        searchText.setText(selection);
                        getSuggestions(searchText.getText());
                    }
                    searchOnline(searchText.getText());
                }
            };
            Action moveSelectionUp =  new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (suggestions.getSelectedIndex() >= 0) {
                        suggestions.requestFocusInWindow();
                        suggestions.setSelectedIndex(suggestions.getSelectedIndex() - 1);
                    }
                }
            };
            Action moveSelectionDown = new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (suggestions.getSelectedIndex() != results.length) {
                        suggestions.requestFocusInWindow();
                        suggestions.setSelectedIndex(suggestions.getSelectedIndex() + 1);
                    }
                }
            };
            Action moveSelectionUpFocused = new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (suggestions.getSelectedIndex() == 0) {
                        suggestions.clearSelection();
                        searchText.requestFocusInWindow();
                        searchText.setSelectionEnd(0);
                    } else if (suggestions.getSelectedIndex() >= 0)
                        suggestions.setSelectedIndex(suggestions.getSelectedIndex() - 1);
                }
            };
            suggestions.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
                KeyStroke.getKeyStroke("UP"), "moveSelectionUp");
            suggestions.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
                KeyStroke.getKeyStroke("DOWN"), "moveSelectionDown");
            suggestions.getActionMap().put("moveSelectionUp", moveSelectionUp);
            suggestions.getActionMap().put("moveSelectionDown", moveSelectionDown);
            suggestions.getInputMap(JComponent.WHEN_FOCUSED).put(
                KeyStroke.getKeyStroke("ENTER"), "makeSelection");
            suggestions.getInputMap().put(KeyStroke.getKeyStroke("UP"), "moveSelectionUpFocused");
            suggestions.getActionMap().put("moveSelectionUpFocused", moveSelectionUpFocused);
            suggestions.getActionMap().put("makeSelection", makeSelection);

            // Create the suggestion drop-down panel and scroll bar
            suggestionsPanel = new JPanel();

            scrollPane = new JScrollPane(suggestions);
            scrollPane.setVisible(false);
            int prefBarWidth = scrollPane.getVerticalScrollBar().getPreferredSize().width;
            suggestions.setPreferredSize(new Dimension(searchText.getPreferredSize().width, 0));
            scrollPane.setAutoscrolls(true);
            scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
            scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

            // resize widths and heights of all components to fit nicely
            int preferredWidth      = searchText.getPreferredSize().width + 2*prefBarWidth;
            int maxWidth            = searchText.getMaximumSize().width + 2*prefBarWidth;
            int searchBarHeight     = searchText.getPreferredSize().height;
            int suggestionHeight    = suggestions.getFixedCellHeight();
            int maxSuggestionHeight = DEF_HEIGHT*2;

            suggestionsPanel.setPreferredSize(new Dimension(preferredWidth, suggestionHeight));
            suggestionsPanel.setMaximumSize(new Dimension(maxWidth, maxSuggestionHeight));
            suggestionsPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
            suggestionsPanel.add(scrollPane);
            suggestionsPanel.setLayout(new GridLayout(1, 1));

            this.setPreferredSize(new Dimension(preferredWidth, this.getPreferredSize().height));
            this.setMaximumSize(new Dimension(preferredWidth, searchBarHeight + maxSuggestionHeight));

            searchTextPanel.setPreferredSize(new Dimension(preferredWidth, searchBarHeight));
            searchTextPanel.setMaximumSize(new Dimension(maxWidth, searchBarHeight));
            searchText.setMaximumSize(new Dimension(maxWidth, searchBarHeight));

            // add mouse interactivity with the drop-down menu
            suggestions.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent mouseEvent) {
                    @SuppressWarnings("unchecked") JList<String> theList = (JList<String>) mouseEvent.getSource();
                    if (mouseEvent.getClickCount() >= 1) {
                        int index = theList.locationToIndex(mouseEvent.getPoint());
                        if (index >= 0) {
                            String selection = getSelectedText();
                            searchText.setText(selection);
                            getSuggestions(searchText.getText());
                            searchOnline(selection);
                        }
                    }
                }

                @Override
                public void mouseEntered(MouseEvent mouseEvent) {
                    @SuppressWarnings("unchecked") JList<String> theList = (JList<String>) mouseEvent.getSource();
                    int index = theList.locationToIndex(mouseEvent.getPoint());
                    theList.requestFocusInWindow();
                    theList.setSelectedIndex(index);
                }

                @Override
                public void mouseExited(MouseEvent mouseEvent) {
                    suggestions.clearSelection();
                    searchText.requestFocusInWindow();
                }
            });

            suggestions.addMouseMotionListener(new MouseInputAdapter() {
                @Override
                public void mouseMoved(MouseEvent mouseEvent) {
                    @SuppressWarnings("unchecked") JList<String> theList = (JList<String>) mouseEvent.getSource();
                    int index = theList.locationToIndex(mouseEvent.getPoint());
                    theList.requestFocusInWindow();
                    theList.setSelectedIndex(index);
                }
            });

            // add a listener that allows updates each time the user types
            searchText.getDocument().addDocumentListener(new DocumentListener() {
                @Override
                public void insertUpdate(DocumentEvent e) {
                    changedUpdate(e);
                }

                @Override
                public void removeUpdate(DocumentEvent e) {
                    changedUpdate(e);
                }

                @Override
                public void changedUpdate(DocumentEvent e) {
                    // updates the drop-down menu
                    getSuggestions(searchText.getText());
                    updateListSize();
                }
            });

            // When a user clicks on a suggestion, Google it
            searchText.addActionListener(e -> {
                String selection = getSelectedText();
                searchText.setText(selection);
                getSuggestions(selection);
                searchOnline(selection);
            });

            // Define the layout of the text box and suggestion dropdown
            layout.setHorizontalGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addComponent(searchTextPanel, 0, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(suggestionsPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
            );
            layout.setVerticalGroup(layout.createSequentialGroup()
                .addComponent(searchTextPanel)
                .addComponent(suggestionsPanel)
            );
        }

        /**
         * Re-populates the drop-down menu with the new suggestions, and
         * resizes the containing panel vertically
         */
        private void updateListSize() {
            int rows = Math.min(maxMatches, suggestions.getModel().getSize());
            int suggWidth = searchText.getPreferredSize().width;
            int suggPanelWidth = suggestionsPanel.getPreferredSize().width;
            int suggHeight = rows*suggestions.getFixedCellHeight();

            suggestions.setPreferredSize(new Dimension(suggWidth, suggHeight));
            suggestionsPanel.setPreferredSize(new Dimension(suggPanelWidth, suggHeight + extraMargin));
            suggestionsPanel.setMaximumSize(new Dimension(suggPanelWidth, suggHeight + extraMargin));

            // redraw the suggestion panel
            suggestionsPanel.setVisible(false);
            suggestionsPanel.setVisible(true);
        }

        /**
         * Makes a call to the implementation of Autocompleter to get
         * suggestions for the currently entered text.
         * @param text string to search for
         */
        public void getSuggestions(String text) {
            // Don't search for suggestions if there is empty input.
            Term[] matches;
            if (text.isEmpty())
                matches = new Term[] {};
            else {
                matches = auto.allMatches(text);
                if (matches == null)
                    throw new NullPointerException("allMatches(text) is null");
            }

            suggestions.setListData(Arrays.stream(matches).limit(maxMatches).map(match -> {
                if (match == null)
                    throw new NullPointerException("allMatches(text) returned an array with a null entry");

                // Truncate length if needed
                String query = match.getWord();
                if (query.length() > suggListLen.length())
                    query = query.substring(0, suggListLen.length());

                // Create the table HTML.
                // Disgusting hack (no escaping).
                String html = "<html><table width=\""
                    + searchText.getPreferredSize().width + "\">"
                    + "<tr><td align=left>"
                    + query.substring(0, text.length())
                    + "<b>" + query.substring(text.length()) + "</b>";
                if (displayWeights)
                    html += "<td width=\"10%\" align=right>"
                        + "<font size=-1><span id=\"weight\" "
                        + "style=\"float:right;color:gray\">"
                        + match.getWeight() + "</font>";
                return html;
            }).toArray(String[]::new));

            if (matches.length == 0)
                suggestions.clearSelection();
            suggestions.setVisible(matches.length != 0);
            scrollPane.setVisible(matches.length != 0);
        }

        // bring the clicked suggestion up to the Search bar and search it
        public String getSelectedText() {
            if (suggestions.isSelectionEmpty())
                return searchText.getText();

            // Disgusting hack (search query should not be recomputed from formatted output).
            String selection = suggestions.getSelectedValue();
            if (displayWeights)
                selection = selection.substring(0, selection.indexOf("<td width="));
            selection = selection.replaceAll("<.*?>", "");
            selection = selection.replaceAll("^[ \t]+|[ \t]+$", "");
            return selection;
        }
    }

    /**
     * Creates a URI from the user-defined string and searches the web with the
     * selected search engine
     * Opens the default web browser (or a new tab if it is already open)
     * @param s string to search online for
     */
    private void searchOnline(String s) {
        // Create the URL.
        URI searchAddress;
        try {
            URI tempAddress = new URI(SEARCH_URL + URLEncoder.encode(s.trim(), StandardCharsets.UTF_8));
            searchAddress = new URI(tempAddress.toASCIIString()); // Hack to handle Unicode
        }
        catch (URISyntaxException e2) {
            e2.printStackTrace();
            return;
        }

        // open the URL in the browser
        try {
            Desktop.getDesktop().browse(searchAddress);
        }
        catch (IOException e1) {
            e1.printStackTrace();
        }
    }

    /**
     * Creates an AutocompleteGUI object and starts it continuously running
     * @param args the filename from which the Autocompleter object is populated
     * and the integer that defines the maximum number of objects in the
     * dropdown menu
     */
    public static void main(String[] args) throws IOException {
        ParsedInput parsedInput = new ParsedInput(args);
        SwingUtilities.invokeLater(() -> new AutocompleteGUI(parsedInput).setVisible(true));
    }

}
