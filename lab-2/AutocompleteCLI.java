/******************************************************************************
 *  Compilation:  javac AutocompleteCLI.java
 *  Execution:    java AutocompleteCLI dictionary.txt max-matches
 *  Dependencies: Autocomplete.java ParsedInput.java RangeBinarySearch.java Term.java
 *
 *  @author Peter Ljunglöf
 *  @author Christian Sattler
 *
 *  Interactive program to demonstrate the Autocomplete class.
 *
 *     * Reads a list of terms and weights from a file, specified as a
 *       program argument.
 *
 *     * When the user enters a string, it displays the top max-matches
 *       terms that start with the text that the user typed.
 *
 ******************************************************************************/

import java.io.IOException;
import java.util.Arrays;
import java.util.Scanner;

class AutocompleteCLI {
    public static void main(String[] args) throws IOException {
        /* // If you don't want to specify arguments on the command-line, just uncomment this block.
        if (args.length == 0) {
            args = new String[] { "dictionaries/romaner.txt",  // Path to the dictionary file.
                                  "5" };                       // Maximum number of matches to display.
        }
        */

        ParsedInput parsedInput = new ParsedInput(args);
        Autocompleter autocompleter = new Autocompleter(parsedInput.dictionary);

        // The main REPL (read-eval-print loop)
        Scanner input = new Scanner(System.in);
        while (true) {
            // Read prefix from input line, exit if there is no more input.
            System.out.println("Enter search prefix (CTRL-C/D/Z to quit)");
            if (!input.hasNextLine())
                break;
            String prefix = input.nextLine();

            // Print the number of matches.
            int nrMatches = autocompleter.numberOfMatches(prefix);
            System.out.println("Number of matches for prefix " + prefix + ": " + nrMatches);

            // Find all matches and print the top-most ones.
            Term[] results = autocompleter.allMatches(prefix);
            Arrays.stream(results).limit(parsedInput.maxMatches)
                .forEach(term -> System.out.format("%12d    %s\n", term.getWeight(), term.getWord()));
            System.out.println();
        }
    }
}
