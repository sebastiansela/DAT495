import java.util.Comparator;

public class RangeBinarySearch {

    // Returns the index of the *first* element in `a` that equals the search key,
    // according to the given comparator, or -1 if there is no matching element.
    // Precondition: `a` is sorted according to the given comparator.
    // Complexity: O(log N) comparisons where N is the length of `a`
    public static <T> int firstIndexOf(T[] a, T key, Comparator<T> comparator) {
        int lo = 0;
        int hi = a.length - 1;
        int mid;
        int finalIndex = -1;

        while (lo <= hi) {
            mid = (lo + hi) / 2;
            if (comparator.compare(a[mid], key) < 0) {
                lo = mid + 1;
            } else if (comparator.compare(a[mid], key) == 0) {
                finalIndex = mid;
                hi = mid - 1;
            } else if (comparator.compare(a[mid], key) > 0) {
                hi = mid - 1;
            }
        }
        return finalIndex;
    }

    // Returns the index of the *last* element in `a` that equals the search key,
    // according to the given comparator, or -1 if there are is matching element.
    // Precondition: `a` is sorted according to the given comparator.
    // Complexity: O(log N) comparisons where N is the length of `a`
    public static <T> int lastIndexOf(T[] a, T key, Comparator<T> comparator) {
        int lo = 0;
        int hi = a.length - 1;
        int mid;
        int finalIndex = -1;

        while (lo <= hi) {
            mid = (lo + hi) / 2;

            if (comparator.compare(a[mid], key) < 0) {
                lo = mid + 1;
            } else if (comparator.compare(a[mid], key) == 0) {
                finalIndex = mid;
                lo = mid + 1;
            } else if (comparator.compare(a[mid], key) > 0) {
                hi = mid - 1;
            }
        }
        return finalIndex;
    }


    // For testing purposes.
    public static void main(String[] args) {
        // Here you can write some tests if you want.
        //System.out.println(firstIndexOf(a, "e", ));
        Term[] term = {new Term("a", 0), new Term("a", 0), new Term("b", 1), new Term("c", 2), new Term("c", 4), new Term("c", 5), new Term("f", 5), };
        System.out.println(firstIndexOf(term, new Term("c", 4), Term.byLexicographicOrder));
        System.out.println(lastIndexOf(term, new Term("c", 4), Term.byLexicographicOrder));

    }
}
