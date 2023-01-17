import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;

public class ParsedInput {

    public final Path dictFile;
    public final Term[] dictionary;
    public final int maxMatches;

    // Parse the input given to the program.
    public ParsedInput(String[] args) throws IOException {
        // If no arguments are given, ask for them.
        if (args.length == 0) {
            args = new String[2];
            Scanner input = new Scanner(System.in);
            System.out.println("You have not specified any program arguments.");
            System.out.print("Give the path to a dictionary file: ");
            args[0] = input.nextLine();
            System.out.print("Give the maximum number of matches to display: ");
            args[1] = input.nextLine();
        }

        // If the wrong number of program arguments are given, display the usage.
        if (args.length != 2) {
            System.err.println("Usage: you have to provide exactly two program arguments:");
            System.err.println("  (1) the path to a dictionary file,");
            System.err.println("  (2) the maximum number of matches to display.");
            System.exit(1);
        }

        // Load dictionary file specified in first program argument.
        try {
            dictFile = Paths.get(args[0]);
            dictionary = Files.lines(dictFile).map(line -> {
                String[] parts = line.trim().split("\\s+", 2);
                return new Term(parts[1], Long.valueOf(parts[0]));
            }).toArray(Term[]::new);
        } catch (Exception e) {
            System.err.println("I failed to read the dictionary file.");
            throw e;
        }

        // Parse maximum number of matches specified in second program argument.
        try {
            maxMatches = Integer.parseInt(args[1]);
        } catch (Exception e) {
            System.err.println("I failed to parse the maximum number of matches to display.");
            throw e;
        }

        // Print some help.
        System.out.println("Loaded dictionary " + dictFile + " containing " + dictionary.length + " words");
        System.out.println("Maximum number of matches to display: " + maxMatches);
        System.out.println();
    }

}
