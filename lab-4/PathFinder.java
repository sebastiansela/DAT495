import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * This is a class that can find paths in a given graph.
 * <p>
 * There are several methods for finding paths,
 * and they all return a PathFinder.Result object.
 */
public class PathFinder<Node> {

    private final DirectedGraph<Node> graph;
    private long startTimeMillis;

    /**
     * Creates a new pathfinder for the given graph.
     *
     * @param graph the graph to search
     */
    public PathFinder(DirectedGraph<Node> graph) {
        this.graph = graph;
    }

    /**
     * The main search method, taking the search algorithm as input.
     *
     * @param algorithm "random", "ucs" or "astar"
     * @param start     the start node
     * @param goal      the goal node
     */
    public Result search(String algorithm, Node start, Node goal) {
        TreeMap<String, Supplier<Result>> byAlgorithm = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        byAlgorithm.put("random", () -> searchRandom(start, goal));
        byAlgorithm.put("ucs", () -> searchUCS(start, goal));
        byAlgorithm.put("astar", () -> searchAstar(start, goal));

        Supplier<Result> action = byAlgorithm.get(algorithm);
        if (action == null)
            throw new IllegalArgumentException("unknown search algorithm " + algorithm);

        startTimeMillis = System.currentTimeMillis();
        return action.get();
    }

    /**
     * Perform a random walk in the graph, hoping to reach the goal.
     * Warning: this method will give up of the random walk
     * reaches a dead end or after one million iterations.
     * So a negative result does not mean there is no path.
     *
     * @param start the start node
     * @param goal  the goal node
     */
    public Result searchRandom(Node start, Node goal) {
        int iterations = 0;
        LinkedList<DirectedEdge<Node>> path = new LinkedList<>();
        double cost = 0;
        Random random = new Random();

        Node current = start;
        while (iterations < 1e6) {
            iterations++;
            if (current.equals(goal))
                return new Result(true, start, current, cost, path, iterations);

            List<DirectedEdge<Node>> neighbours = graph.outgoingEdges(start);
            if (neighbours.size() == 0)
                break;

            DirectedEdge<Node> edge = neighbours.get(random.nextInt(neighbours.size()));
            path.add(edge);
            cost += edge.weight();
            current = edge.to();
        }
        return new Result(false, start, goal, -1, null, iterations);
    }

    /**
     * Run uniform-cost search for finding the shortest path.
     *
     * @param start the start node
     * @param goal  the goal node
     */
    public Result searchUCS(Node start, Node goal) {
        int iterations = 0;
        Queue<PQEntry> pqueue = new PriorityQueue<>(Comparator.comparingDouble(e -> e.costToHere));
        /*************************************************************************************************
         * TODO: Task 1a+c                                                                               *
         * Change here.                                                                                  *
         * Note: Every time you remove a node from the priority queue, you should increment `iterations` *
         *************************************************************************************************/
        pqueue.add(new PQEntry(start, 0, null, null));
        Set<Node> visited = new HashSet<>();

        while (!pqueue.isEmpty()) {
            PQEntry entry = pqueue.remove();
            iterations++;
            if (!visited.contains(entry.node)) {
                visited.add(entry.node);
                if (entry.node.equals(goal)) {
                    return new Result(true, start, goal, entry.costToHere, extractPath(entry), iterations);
                }
                for (DirectedEdge<Node> edge : graph.outgoingEdges(entry.node)) {
                    pqueue.add(new PQEntry(edge.to(), (entry.costToHere + edge.weight()), edge, entry));
                }
            }
        }
        return new Result(false, start, goal, -1, null, iterations);
    }

    /**
     * Run the A* algorithm for finding the shortest path.
     *
     * @param start the start node
     * @param goal  the goal node
     */
    public Result searchAstar(Node start, Node goal) {
        int iterations = 0;
        /*************************************************************************************************
         * TODO: Task 1a+c                                                                               *
         * Change here.                                                                                  *
         * Note: Every time you remove a node from the priority queue, you should increment `iterations` *
         *************************************************************************************************/
        Queue<PQEntry> pqueue = new PriorityQueue<>(Comparator.comparingDouble(e -> e.costToHere));
        pqueue.add(new PQEntry(start, 0, null, null));
        Set<Node> visited = new HashSet<>();

        while (!pqueue.isEmpty()) {
            PQEntry entry = pqueue.remove();
            iterations++;
            if (!visited.contains(entry.node)) {
                visited.add(entry.node);
                if (entry.node.equals(goal)) {
                    return new Result(true, start, goal, entry.costToHere, extractPath(entry), iterations);
                }
                for (DirectedEdge<Node> edge : graph.outgoingEdges(entry.node)) {
                    pqueue.add(new PQEntry(edge.to(), (entry.costToHere + edge.weight()), edge, entry));
                }
            }
        }
        return new Result(false, start, goal, -1, null, iterations);
    }

    /**
     * Extract the path from the start to the current priority queue entry.
     *
     * @param entry the priority queue entry
     * @return the path from start to goal as a list of edges
     */
    private List<DirectedEdge<Node>> extractPath(PQEntry entry) {
        /*****************
         * TODO: Task 1b *
         * Change here.  *
         *****************/
        LinkedList<DirectedEdge<Node>> edges = new LinkedList<>();

        while (entry.backPointer != null) {
            edges.addFirst(entry.lastEdge);
            entry = entry.backPointer;
        }

        return edges;
    }

    /**
     * Entries to put in the priority queues in {@code searchUCS} and {@code searchAstar}.
     */
    private class PQEntry {
        public final Node node;
        public final double costToHere;
        public final DirectedEdge<Node> lastEdge;  // null for starting entry
        public final PQEntry backPointer;          // null for starting entry

        /***************************************************
         * TODO: Task 3                                    *
         * Change here,                                    *
         * For example, to add new fields or constructors. *
         **************************************************/

        PQEntry(Node node, double costToHere, DirectedEdge<Node> lastEdge, PQEntry backPointer) {
            this.node = node;
            this.costToHere = costToHere;
            this.lastEdge = lastEdge;
            this.backPointer = backPointer;
        }
    }

    /**
     * The internal class for search results.
     */
    public class Result {
        public final boolean success;
        public final Node start;
        public final Node goal;
        public final double cost;
        public final List<DirectedEdge<Node>> path;
        public final int iterations;
        public final double elapsedTime;

        public Result(boolean success, Node start, Node goal, double cost, List<DirectedEdge<Node>> path, int iterations) {
            this.success = success;
            this.start = start;
            this.goal = goal;
            this.cost = cost;
            this.path = path;
            this.iterations = iterations;
            this.elapsedTime = (System.currentTimeMillis() - startTimeMillis) / 1000.0;
        }

        private String formatPathPart(boolean withWeight, boolean suffix, int i, int j) {
            return path.subList(i, j).stream()
                    .map(e -> e.toString(withWeight, !suffix, suffix))
                    .collect(Collectors.joining());
        }

        public String toString(boolean withWeight) {
            StringWriter buffer = new StringWriter();
            PrintWriter w = new PrintWriter(buffer);
            if (iterations <= 0)
                w.println("ERROR: You have to iterate over at least the starting node!");
            w.println("Loop iterations: " + iterations);
            w.println("Elapsed time: " + elapsedTime + "s");
            if (success) {
                w.println("Cost of path from " + start + " to " + goal + ": " + DirectedEdge.DECIMAL_FORMAT.format(cost));
                if (path == null)
                    w.println("WARNING: you have not implemented extractPath!");
                else {
                    // Print path.
                    w.println("Number of edges: " + path.size());
                    w.println(path.size() <= 10 ?
                            start + formatPathPart(withWeight, true, 0, path.size()) :
                            formatPathPart(withWeight, false, 0, 5) + "....." + formatPathPart(withWeight, true, path.size() - 5, path.size())
                    );
                    // We sum using left association order to mimic the algorithm.
                    // Then we can use exact comparison of doubles.
                    double actualCost = path.stream().mapToDouble(DirectedEdge::weight).reduce(0, Double::sum);
                    if (cost != actualCost)
                        w.println("WARNING: the actual path cost " + actualCost + " differs from the reported cost " + cost);
                }
            } else
                w.println("No path found from " + start + " to " + goal);
            return buffer.toString();
        }

        @Override
        public String toString() {
            return toString(false);
        }

    }

}
