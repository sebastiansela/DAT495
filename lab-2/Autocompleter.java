import java.util.Arrays;

public class Autocompleter {
    private final Term[] dictionary;

    // Initializes the dictionary from the given array of terms.
    public Autocompleter(Term[] dictionary) {
        this.dictionary = dictionary;
        sortDictionary();
    }

    // Sorts the dictionary in *case-insensitive* lexicographic order.
    // Complexity: O(N log N) where N is the number of dictionary terms
    private void sortDictionary() {
        //måste sorta i lexicographic
        Arrays.sort(dictionary, Term.byLexicographicOrder);
    }

    // Returns the number of terms that start with the given prefix.
    // Precondition: the internal dictionary is in lexicographic order.
    // Complexity: O(log N) where N is the number of dictionary terms
    public int numberOfMatches(String prefix) {
        Term term = new Term(prefix, 1);
        //the array is in lexicographic order, so we can use first and last index of,
        //minus every other element in the array, and we get the number of matches.
        //our problem is that the methods need the types to be the same, we used
        //Term[], String (prefix), comparator(Term)
        int lo;
        if (RangeBinarySearch.firstIndexOf(dictionary, term, Term.byPrefixOrder(prefix.length())) < 0){
            return 0;
        } else {
            lo = RangeBinarySearch.firstIndexOf(dictionary, term, Term.byPrefixOrder(prefix.length()));
        }

        int hi;
        if (RangeBinarySearch.lastIndexOf(dictionary, term, Term.byPrefixOrder(prefix.length())) < 0){
            return 0;
        } else {
            hi = RangeBinarySearch.lastIndexOf(dictionary, term, Term.byPrefixOrder(prefix.length()));
        }

        return (hi - lo) + 1;

    }

    // Returns all terms that start with the given prefix, in descending order of weight.
    // Precondition: the internal dictionary is in lexicographic order.
    // Complexity: O(log N + M log M) where M is the number of matching terms
    public Term[] allMatches(String prefix) {
        Term term = new Term(prefix, 1);

        int loBinarySearch = RangeBinarySearch.firstIndexOf(dictionary, term, Term.byPrefixOrder(prefix.length()));
        int hiBinarySearch = RangeBinarySearch.lastIndexOf(dictionary, term, Term.byPrefixOrder(prefix.length()));

        int loIndex;
        int hiIndex;
        if (loBinarySearch < 0) {
            return new Term[0];
        } else {
            loIndex = loBinarySearch;
        }

        if (hiBinarySearch < 0) {
            return new Term[0];
        } else {
            hiIndex = hiBinarySearch;
        }

        Term[] termArr = Arrays.copyOfRange(dictionary, loIndex, hiIndex + 1);
        Arrays.sort(termArr, Term.byReverseWeightOrder);
        return termArr;
    }
}
