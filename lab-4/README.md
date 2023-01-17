# Lab 4: Path finder

In this lab your task is to write a program that calculates the shortest path between two nodes in a graph.
You will try this on several kinds of graphs.


## About the labs

- The lab is part of the examination of the course. Therefore, you must not copy code from or show code to other groups. You are welcome to discuss ideas with one another, but anything you do must be **the work of you and your lab partners**.
- Please read the pages "Doing the lab assignments" and "Running the labs" on Canvas.


## Getting started

The lab files consist of a number of Java files (explained below), a directory of **graphs**, and a file **answers.txt** where you will answer questions for the lab.


## Background

Your task is to write a program solving one of the most important graph problem: calculating the shortest path between two nodes.
This is similar to what map applications do (e.g. Waze, OpenStreetMap, Google, Apple, Garmin, etc.), although your solution will not quite be able to handle as large road maps as them.
But this is also useful in several other circumstances, and you will see some in this laboration.

There is a main program `RunPathFinder` which you can compile and run right away.
It takes three required arguments:

- the algorithm ("random", "ucs", or "astar"),
- the type of graph to read ("AdjacencyGraph", "NPuzzle", "GridGraph", or "WordLadder"),
- the graph itself (usually a file name into the `graphs` subfolder, but for the NPuzzle it's a number denoting the size of the puzzle),

The program first prints about some information about the specified graph:

```
$ javac RunPathFinder.java
$ java RunPathFinder random AdjacencyGraph graphs/AdjacencyGraph/citygraph-VGregion.txt
Adjacency graph with 130 nodes and 838 edges

Random example nodes with outgoing edges:
Trollhättan ---> Grästorp [27], Lilla Edet [23], Sjuntorp [16], Sollebrunn [33], Uddevalla [30], Vargön [12], Vänersborg [14]
Bollebygd ---> Alingsås [39], Fritsla [23], Hindås [11], Ingared [36], Jonstorp [51], Kinna [27], Landvetter [24], Rävlanda [6], Sandared [14]
Ed ---> Billingsfors [26], Dals Långed [29], Färgelanda [48], Mellerud [42], Munkedal [59], Strömstad [65], Tanumshede [52], Vänersborg [79]
Björlanda ---> Göteborg [13], Torslanda [7]
Mölndal ---> Eriksbo [16], Göteborg [8], Kållered [6], Mölnlycke [9], Partille [15], Västra Frölunda [8], Öjersjö [12]
Uddevalla ---> Brålanda [49], Färgelanda [28], Henån [30], Ljungskile [19], Lysekil [38], Munkedal [25], Sollebrunn [58], Trollhättan [30], Vänersborg [30]
Viskafors ---> Borås [12], Fritsla [13], Limmared [41], Länghem [32], Sjömarken [15], Sätila [31]
Bengtsfors ---> Billingsfors [7], Gullspång [187], Åmål [36]
```

It then allows you to (repeatedly) perform path searches by specifying start and goal:

```
start: Mölndal
goal: Göteborg

Loop iterations: 5
Elapsed time: 0.0s
Path cost from Mölndal to Göteborg: 44
Number of edges: 4
Mölndal --[12]-> Öjersjö --[9]-> Mölnlycke --[15]-> Partille --[8]-> Göteborg
```

Note that a random walk will find a different path every time.
It may even run forever (or it would, if we didn't terminate the random walk after visiting 1 million nodes).

Alternatively, you may directly specify start and goal nodes as additional program arguments.
In that case, the program directly prints the path found:

```
$ javac RunPathFinder.java
$ java RunPathFinder random AdjacencyGraph graphs/AdjacencyGraph/citygraph-VGregion.txt Skara Vara
Loop iterations: 16
Elapsed time: 0.0s
Cost of path from Skara to Vara: 417
Number of edges: 15
Skara --[21]-> Skara --[15]-> Skara --[25]-> Skara --[36]-> Skara --[21]-> ..... --[31]-> Åkarp --[21]-> Götene --[36]-> Floby --[19]-> Källby --[35]-> Vara
```

Alternatively, you can comment out lines 24–35 in **RunPathFinder.java** and set the arguments directly in the source code.


## Descriptions of the Java classes

The lab contains the following Java classes:

- **RunPathFinder**
- **PathFinder**
- **DirectedEdge**
- **DirectedGraph** is an interface with implementing classes:
  - **AdjacencyGraph**
  - **NPuzzle**
  - **GridGraph**
  - **WordLadder**
- **Point** is a helper class used by **GridGraph** and **WordLadder**

### RunPathFinder

This is the main class, which was described previously in the background.
It is the entry point for graph searches:

    RunPathFinder:
        void main(String[] args)

### PathFinder

This is the class that does the heavy work of path finding.
It consists of some searching methods, inner classes for priority queue entries and search results, and an auxiliary method for extracting the solution path:

    PathFinder<Node>:
        Result search(String algorithm, Node start, Node end)
        Result searchRandom(Node start, Node end)
        Result searchUCS(Node start, Node end)    // this is Task 1a+c
        Result searchAstar(Node start, Node end)  // this is Task 3

        List<DirectedEdge<node>> extractPath(PQEntry entry) // this is Task 1b

        class PQEntry  // you will extend this in Task 3
        class Result

### DirectedEdge

A weighted directed edge, consisting of two nodes and a non-negative weight..
If the weight isn't provided (such as for the graph types **WordLadder** and **NPuzzle**), it defaults to 1.

    DirectedEdge<Node>:
        Node from()
        Node to()
        double weight()

### DirectedGraph

This is an interface with three important methods:

    interface DirectedGraph<Node>:
        Set<Node> nodes()
        List<DirectedEdge<Node>> outgoingEdges(Node from)
        double guessCost(Node from, Node goal)

The methods are described later.
**PathFinder** works for graphs implements this interface.
All the below graphs do.

Note that this interface differs from the graph interface in the course API (it lacks several of the API methods).
But it is enough for the purposes in this lab.

### AdjacencyGraph

The **AdjacencyGraph** reads a generic finite graph, one edge per line, and stores it as an adjacency list as described in the course book and the lectures.
Th graph can represent anything.
In the graph repository, there are distance graphs for cities (city graphs) in several regions, including EU and Västra Götaland (VGregion).
There is also a link graph between more than 4500 Wikipedia pages, "wikipedia-graph.txt":

```
$ java RunPathFinder random AdjacencyGraph graphs/AdjacencyGraph/wikipedia-graph.txt Sweden Norway
Loop iterations: 175
Elapsed time: 0.001s
Cost of path from Skara to Vara: 174
Number of edges: 174
Path: Sweden -> Potato -> Stefan_Edberg -> German_language -> Parliamentary_system -> ... -> Parliamentary_system -> Eastern_Orthodox_Church -> Chile -> Lithuania -> Norway
```

### NPuzzle

**NPuzzle** is an encoding of the [n-puzzle](https://en.wikipedia.org/wiki/15_puzzle).
The nodes for this graph are the possible states of the puzzle.
An edge represents a move in the game, swapping the empty tile with an adjacent tile.

We represent each state as a string encoding of an *N* x *N* matrix.
The tiles are represented by characters starting from the letter A (`A`…`H` for *N*=3, and `A`…`O` for *N*=4).
The empty tile is represented by `_`.
To make it more readable for humans, every row is separated by `/`:

```
$ java RunPathFinder ucs NPuzzle 2 /_C/BA/ /AB/C_/
Loop iterations: 22
Elapsed time: 0.018s
Cost of path from /_C/BA/ to /AB/C_/: 6
Number of edges: 6
/_C/BA/ -> /BC/_A/ -> /BC/A_/ -> /B_/AC/ -> /_B/AC/ -> /AB/_C/ -> /AB/C_/

$ java RunPathFinder ucs NPuzzle 3 /ABC/DEF/HG_/ /ABC/DEF/GH_/
Loop iterations: 483841
Elapsed time: 0.562s
No path found from /ABC/DEF/HG_/ to /ABC/DEF/GH_/
```

**Note:**
The above output shows a working implementation.
The current implementation of UCS is just a stub that never finds anything.
Your Task 1 is to fix that.

It's no use trying the "random" algorithm on the **NPuzzle**: it will almost certainly never find a solution.
In fact, already for *N* = 4, the number of states is 16! ≈ 2 · 10<sup>13</sup>.
Thus, we cannot even store the set of nodes in memory.

### GridGraph

**GridGraph** is a 2D-map encoded as a bitmap, or an *N* x *M* matrix of characters.
Some characters are passable, others denote obstacles.
A node is a point in the bitmap, consisting of an x- and a y-coordinate.
This is defined by the helper class `Point`.
You can move from each point to the eight point around it.
The edge costs are 1 (for up/down/left/right) and sqrt(2) (for diagonal movement).
A point is written as `x:y`, like this:

```
$ java RunPathFinder ucs GridGraph graphs/GridGraph/maze-10x5.txt
Bitmap graph of dimensions 41 x 11 pixels
+---+---+---+---+---+---+---+---+---+---+
        |   |                       |   |
+---+   +   +   +---+   +---+---+   +   +
|   |   |           |   |   |           |
+   +   +---+---+---+   +   +   +---+---+
|               |       |   |           |
+---+   +---+---+   +---+   +---+   +   +
|       |                   |       |   |
+   +---+   +---+---+---+---+   +---+   +
|           |                   |
+---+---+---+---+---+---+---+---+---+---+

Random example points with outgoing edges:
7:5 ---> 6:4 [1.41], 6:5, 6:6 [1.41], 7:4, 7:6, 8:5
33:5 ---> 32:5, 33:6, 34:5, 34:6 [1.41]
15:3 ---> 14:2 [1.41], 14:3, 15:2, 16:3
2:8 ---> 1:7 [1.41], 1:8, 1:9 [1.41], 2:7, 2:9, 3:7 [1.41], 3:8, 3:9 [1.41]
7:2 ---> 6:1 [1.41], 6:2, 6:3 [1.41], 7:1, 7:3
3:7 ---> 2:7, 2:8 [1.41], 3:8, 4:7
15:3 ---> 14:2 [1.41], 14:3, 15:2, 16:3
25:4 ---> 25:3, 25:5, 26:3 [1.41], 26:4, 26:5 [1.41]

start: 1:1
goal: 39:9

Loop iterations: 971
Elapsed time: 0.009s
Cost of path from 1:1 to 39:9: 58.87
Number of edges: 51
1:1 -> 2:1 -> 3:1 -> 4:1 -> 5:2 -> ..... -> 36:5 -> 37:6 -> 38:7 -> 39:8 -> 39:9

+---+---+---+---+---+---+---+---+---+---+
 ****   |   |           *********   |   |
+---+*  +   +   +---+  *+---+---+*  +   +
|   |*  |           | * |   |   *       |
+   +*  +---+---+---+*  +   +  *+---+---+
|    *          |   *   |   |   *****   |
+---+*  +---+---+  *+---+   +---+   +*  +
|   *   |  ********         |       | * |
+  *+---+ * +---+---+---+---+   +---+  *+
|   ******  |                   |      *
+---+---+---+---+---+---+---+---+---+---+
```

### WordLadder

This class is unfinished.
You will complete it in Task 2.

> Word ladder is a word game invented by Lewis Carroll.
> A word ladder puzzle begins with two words, and to solve the puzzle one must find a chain of other words to link the two, in which two adjacent words (that is, words in successive steps) differ by one letter.
>
> ([Wikipedia](https://en.wikipedia.org/wiki/Word_ladder))

We model this problem as a graph.
The nodes denote words in a dictionary and the edges denote one step in this word ladder.
Note that edges only connect words of the same length.

The class does not store the full graph in memory, just the dictionary of words.
The edges are then computed on demand.
The class already contains code that reads the dictionary, but you must complete the rest of the class.


## About the graphs in the collection

**Note:**
All graph files are encoded in UTF-8 (Unicode).
If you experience problems searching for words with special characters (`å`, `ä`, `ö`), your setup may have a character encoding problem.
Try switching to an English or Swedish system locale.

#### AdjacencyGraph:

- All graphs **citygraph-XX.txt** are extracted from freely available [mileage charts](https://www.mileage-charts.com/).
  The smallest graph has 130 cities and 838 edges (citygraph-VGregion.txt).
  The largest one 996 cities and 28054 edges (citygraph-EU.txt).
  All edge costs are in kilometers.
  - Suggested searches: `Göteborg` to `Götene` (**citygraph-VGregion.txt**); `Lund` to `Kiruna` (**citygraph-SE.txt**); `Porto, Portugal` to `Vorkuta, Russia` (**citygraph-EU.txt**)

- **wikipedia-graph.txt** is converted from [the Wikispeedia dataset](http://snap.stanford.edu/data/wikispeedia.html) in SNAP (Stanford Large Network Dataset Collection).
  It contains 4587 Wikipedia pages and 119882 page links.
  All edges have cost 1.
  - Suggested search: `Superconductivity` to `Anna_Karenina`

#### NPuzzle:

- **NPuzzle** does not need a file for initializing the graph, just a number giving the size of the puzzle.
  Larger sizes than 3 are usually too difficult, even for the algorithms in this lab.
  - Suggested search for size 2: `/_C/BA/` to goal `/AB/C_/`
  - Suggested searches for size 3: any of `/_AB/CDE/FGH/`, `/CBA/DEF/_HG/`, `/FDG/HE_/CBA/` or `/HFG/BED/C_A/`, to the goal `/ABC/DEF/GH_/`
  - Try also the following size-3 puzzle, which doesn't have a solution (why?): `/ABC/DEF/HG_/` to `/ABC/DEF/GH_/`

#### GridGraph:

- **AR0011SR.map** and **AR0012SR.map** are taken from the [2D Pathfinding Benchmarks](https://www.movingai.com/benchmarks/grids.html) in Nathan Sturtevant's Moving AI Lab.
  The maps are from the collection "Baldurs Gate II Original maps", and are grids of sizes 216 x 224 and 148 x 139, respectively.
  There are also associated PNG files, so that you can see how they look like.
  - Suggested searches: `23:161` to `130:211` (**AR0011SR.map**); `11:73` to `85:127` (**AR0012SR.map**)

- **maze-10x5.txt**, **maze-20x10.txt**, and **maze-100x50.txt** are generated by a [random maze generator](http://www.delorie.com/game-room/mazes/genmaze.cgi).
  They are grids of sizes 41 x 11, 81 x 21, and 201 x 101, respectively.
  - Suggested searches: `1:1` to `39:9` (**maze-10x5.txt**); `1:1` to `79:19` (maze-20x10.txt); `1:1` to `199:99` (**maze-100x50.txt**)

#### WordLadder:

- **english-crossword.txt** comes from the official crossword lists in the [Moby project](https://en.wikipedia.org/wiki/Moby_Project).
  It consists of 117,969 words.
  - Suggested searches: any start and goal of the same length (between 4 and 8 characters)

- **swedish-romaner.txt** and **swedish-saldo.txt** are two Swedish word lists compiled from [Språkbanken Text](https://spraakbanken.gu.se/resurser).
  They contain 75,740 words (**swedish-romaner.txt**) and 888,275 words (**swedish-saldo.txt**), respectively.
  - Suggested searches (after you have completed Task 2 below): `eller` to `glada` (**swedish-romaner.txt**); `njuta` to `övrig` (**swedish-saldo.txt**)
  - Another interesting combination is to try any combination of the following words: `ämnet`, `åmade`, `örter`, `öring` (**swedish-romaner.txt**)

## Task 1: Uniform-cost search

There is a skeleton method `searchUCS` in **PathFinder**.
Your goal in Task 1 is to implement uniform-cost search (UCS).
This is a variant of Dijkstra's algorithm which can handle infinite and very large graphs.
It is also arguably easier to understand than the usual formulation of Dijkstra's.


### Task 1a: The simple UCS algorithm

The main data structure used in UCS is a priority queue.
It contains graph nodes paired with the cost to reach them.
We store this information as the inner class `PQEntry` (which is already implemented):

    class PQEntry:
        Node node
        double costToHere
        DirectedEdge lastEdge
        PQEntry backPointer

The `backPointer` is necessary for recreating the final path from the start node to the goal.
More about this in Task 1b below.
The very first entry will not have any previous entry, so we set `lastEdge` and `baskPointer` to `null`.

Here is pseudocode of the simplest version of UCS:

    Result searchUCS(Node start, Node goal):
        pqueue = new priority queue of PQEntry comparing costToHere
        add pqueue entry for start
        while there is an entry to remove from pqueue:
            if entry.node is goal:
                SUCCESS:) extract the path and return it
            for every edge starting at entry.node:
                add pqueue entry for target of edge with cost that of entry plus edge weight
        FAILURE:( there is no path

It is important that we return as soon as we reach the goal.
Otherwise, we will continue adding new entries to the queue indefinitely.

**Hint**: `removeMin` is called `remove` in the Java API for priority queues.

Implement this algorithm in the `searchUCS` method.
When you return a result, use `null` for the `path` argument for now.
You should increase the counter `iterations` every time you remove an entry from `pqueue`.
When you have done this, you should be able to run queries for nodes not too far apart:

```
$ java RunPathFinder ucs AdjacencyGraph graphs/AdjacencyGraph/citygraph-VGregion.txt Skara Lerum
Loop iterations: 66240
Elapsed time: 0.126s
Cost of path from Skara to Lerum: 115
WARNING: you have not implemented extractPath!
```

But there are two problems with this implementation:

1. the path found is not printed, and
2. it becomes extremely slow on more difficult problems (e.g., try to find the way from Skara to Vara).

We will address these problems in Tasks 1b and 1c.

### Task 1b: Extracting the solution

Now you should write code to extract the solution, the list of edges forming the shortest path.
For this, implement and make use of the skeleton method `extractPath`:

    List<DirectedEdge<Node>> extractPath(PQEntry entry)

Make sure you get the order of edges right!

After this is completed, your output will change:

```
$ java RunPathFinder ucs AdjacencyGraph graphs/AdjacencyGraph/citygraph-VGregion.txt Skara Lerum
Loop iterations: 66240
Elapsed time: 0.123s
Cost of path from Skara to Lerum: 115
Number of edges: 6
Skara --[35]-> Vara --[28]-> Vårgårda --[9]-> Jonstorp --[15]-> Alingsås --[23]-> Stenkullen --[5]-> Lerum
```

As you can see, the result is the same as before, but now the path is printed too.
Check that you get the same path as shown here.


### Task 1c: Remembering visited nodes

The reason why the algorithm is slow is that it will revisit the same node every time it is reached.
There are hundreds of ways to get from Göteborg to Stenkullen, and the algorithm visits most of them before it finds its way to Alingsås.
But all the subsequent visits to Stenkullen are unnecessary because the first visit is already via the shortest path (why?).

Therefore, a simple solution is to record the visited nodes in a set.
Immediately after you retrieve a node from the priority queue, check if it has already been visited.
Only proceed if the node is "fresh".
Don't forget to then add it to the visited nodes: otherwise there won't be much of an optimisation.

**Note**:
You may freely use the data structures of the Java collections library in this lab.
Consult the Java documentation for classes implementing `Set` in `java.util`.

When this is done, you should see a drastic improvement:

```
$ java RunPathFinder ucs AdjacencyGraph graphs/AdjacencyGraph/citygraph-VGregion.txt Skara Lerum
Loop iterations: 291
Elapsed time: 0.005s
Cost of path from Skara to Lerum: 115
Number of edges: 6
Skara --[35]-> Vara --[28]-> Vårgårda --[9]-> Jonstorp --[15]-> Alingsås --[23]-> Stenkullen --[5]-> Lerum
```

The number of loop iterations went down by a factor of 300!
Now you should be able to solve all kinds of problems in adjacency graphs, n-puzzles, and grid graphs:

```
$ java RunPathFinder ucs AdjacencyGraph graphs/AdjacencyGraph/citygraph-EU.txt "Volos, Greece" "Oulu, Finland"
Loop iterations: 23515
Elapsed time: 0.038s
Cost of path from Volos, Greece to Oulu, Finland: 3488
Number of edges: 12
Volos, Greece --[923]-> Timişoara, Romania --[55]-> Arad, Romania --[114]-> Oradea, Romania --[83]-> Debrecen, Hungary --[50]-> ..... --[169]-> Lublin, Poland --[253]-> Białystok, Poland --[825]-> Tallinn, Estonia --[88]-> Helsinki, Finland --[607]-> Oulu, Finland

$ java RunPathFinder ucs NPuzzle 3 /_AB/CDE/FGH/ /ABC/DEF/GH_/
Loop iterations: 152439
Elapsed time: 0.245s
Cost of path from /_AB/CDE/FGH/ to /ABC/DEF/GH_/: 22
Number of edges: 22
/_AB/CDE/FGH/ -> /A_B/CDE/FGH/ -> /ADB/C_E/FGH/ -> /ADB/_CE/FGH/ -> /ADB/FCE/_GH/ -> ..... -> /ABC/GDE/_HF/ -> /ABC/_DE/GHF/ -> /ABC/D_E/GHF/ -> /ABC/DE_/GHF/ -> /ABC/DEF/GH_/

$ java RunPathFinder ucs GridGraph graphs/GridGraph/maze-100x50.txt 1:1 199:99
Loop iterations: 26478
Elapsed time: 0.109s
Cost of path from 1:1 to 199:99: 1216.48
Number of edges: 1016
1:1 -> 1:2 -> 1:3 -> 1:4 -> 2:5 -> ..... -> 196:97 -> 197:97 -> 198:97 -> 199:98 -> 199:99
[...]
```

Go on!
Try the suggestions for the different graphs in the section "About the graphs in the collection" above!

***Important***:
Make sure you get the cost (shorted path length) shown in these examples.
If you got a higher cost, then UCS didn't find the optimal path.
If you got a lower cost, there's an error in how you calculate the path costs (or you take some illegal shortcuts).
Furthermore, it is a good sign (but not required) if your implementation has the same number of loop iterations as shown above.


## Task 2: Word ladders

The class **WordLadder** is not fully implemented.
This task is to make it work correctly.
What is implemented is the reading of the dictionary, adding of words, and some auxiliary methods.
What is missing is the implementation of `outgoingEdges`:

    public List<DirectedEdge<String>> outgoingEdges(String word)

An edge is one step in the word ladder.
The target word must:

- be in the dictionary,
- be of the same length,
- differ by exacty one letter.

At your disposal are the following two instance variables:

    private Set<String> dictionary
    private Set<Character> alphabet

Here, `alphabet` is the set of letters appearing in dictionary words.
Use this instead of going over a fixed collection of characters.

**Note**: You should not go over all words in the dictionary (that's too expensive).

After you completed your implementation, you should be able to solve the following word ladders:

```
$ java RunPathFinder ucs WordLadder graphs/WordLadder/swedish-romaner.txt
Word ladder graph with 75740 words
Alphabet: àaábâcdäeåfægçhèiéjêkëlmínîoïpqñrsótuõvöwxøyzúü

Random example words with ladder steps:
vandrat ---> vandrar
eldskenet with no outgoing edges
bitterheter ---> bitterheten
nyutnämnd with no outgoing edges
bamse with no outgoing edges
handvändning with no outgoing edges
paddlar with no outgoing edges
kvällstrafiken with no outgoing edges

start: mamma
goal: pappa

Loop iterations: 888
Elapsed time: 0.03s
Cost of path from mamma to pappa: 6
Number of edges: 6
mamma -> mumma -> summa -> sumpa -> pumpa -> puppa -> pappa

start: katter
goal: hundar

Loop iterations: 9036
Elapsed time: 0.084s
Cost of path from katter to hundar: 14
Number of edges: 14
katter -> kanter -> tanter -> tanten -> tanden -> ..... -> randas -> randad -> rundad -> rundar -> hundar

start: örter
goal: öring

Loop iterations: 20127
Elapsed time: 0.095s
Cost of path from örter to öring: 30
Number of edges: 30
örter -> arter -> arten -> armen -> almen -> ..... -> slang -> klang -> kling -> kring -> öring
```


## Task 3: The A* algorithm

The UCS algorithm finds an optimal path, but there is an optimisation which can help discover it much faster!
This algorithm is called A*.
Your task is to implement it in the method `searchAstar` in **PathFinder**:

    public Result searchAstar(Node start, Node goal)

The basic structure of A* is that of UCS, so you can start by copying your UCS code to this method.
For each entry in the priority queue, A* doesn't just keep track of the cost so far, as in UCS, but also of the *estimated total cost* from the start, via this node, to the goal.
The latter score is used as the priority.
To be able to do this efficiently, you may need to add instance variables to the internal `PQEntry` class:

    private class PQEntry

To work, A* needs a *distance heuristic*, an educated guess of the distance between two nodes.
This requires some additional insight into the problem, so the heuristics are different for different types of graphs and problems.
Our graph API (the interface **DirectedGraph**) provides this heuristic in the form of the method `guessCost`, which takes two nodes as argument and returns a cost estimate:

    public double guessCost(Node n, Node m)

The estimated total cost for an entry is then defined as the cost so far *plus* the estimated cost from the current node to the goal.

**Important**:
Make sure your implementation doesn't call `guessCost` too many times.
This could slow down your search.
The priority queue comparator should not call `guessCost` directly, but instead use a value stored with the priority queue entry.
Also avoid operations on the priority queue that take linear time such as iterating over it or calling `contains`.

**Important**:
There are other versions of A* that modify the priority of an existing entry in the priority queue (this is called a `decreaseKey` operation).
The priority queue class of Java (and also the priority queue ADT in this course) does not support this operation.

When you have implemented A*, try it out for **NPuzzle** problems.
This is the only graph type with a ready-baked heuristic (see the next task for how it works):

```
$ java RunPathFinder ucs NPuzzle 3 /CBA/DEF/_HG/ /ABC/DEF/GH_/
Loop iterations: 292528
Elapsed time: 0.392s
Cost of path from /CBA/DEF/_HG/ to /ABC/DEF/GH_/: 24
Number of edges: 24
/CBA/DEF/_HG/ -> /CBA/_EF/DHG/ -> /_BA/CEF/DHG/ -> /B_A/CEF/DHG/ -> /BA_/CEF/DHG/ -> ..... -> /AC_/DBF/GEH/ -> /A_C/DBF/GEH/ -> /ABC/D_F/GEH/ -> /ABC/DEF/G_H/ -> /ABC/DEF/GH_/

$ java RunPathFinder astar NPuzzle 3 /CBA/DEF/_HG/ /ABC/DEF/GH_/
Loop iterations: 3871
Elapsed time: 0.054s
Cost of path from /CBA/DEF/_HG/ to /ABC/DEF/GH_/: 24
Number of edges: 24
/CBA/DEF/_HG/ -> /CBA/_EF/DHG/ -> /_BA/CEF/DHG/ -> /B_A/CEF/DHG/ -> /BA_/CEF/DHG/ -> ..... -> /AC_/DBF/GEH/ -> /A_C/DBF/GEH/ -> /ABC/D_F/GEH/ -> /ABC/DEF/G_H/ -> /ABC/DEF/GH_/
```

Note that A* visits much fewer nodes, but finds a path of the same length as UCS.
If your implementation doesn't, then there's probably a bug somewhere.

If we don't have a way of guessing the cost, we should use 0.
That's the current implementation of `guessCost` in the other graph types.
In that case, the A* algorithm behaves exactly like UCS (why?).
Try that!
If you get different numbers of nodes visited, you might have a bug.


## Task 4: Guessing the cost

The graph API method `guessCost` should return an optimistic guess for the cost (distance) between two nodes.
This is already implemented for **NPuzzle**, but is missing for the other graph types.
The implementation for **NPuzzle** estimates the cost as the sum over each tile of the [Manhattan distance](https://en.wikipedia.org/wiki/Taxicab_geometry) between its positions in the source and target state.
This is the implementation:

    double guessCost(String s, String t):
        cost = 0
        for tile in tiles (excluding the empty tile):
            (sx, sy) = coordinates of tile in s
            (tx, ty) = coordinates of tile in t
            cost += |sx - sy| + |tx - ty|
        return cost

Your task is to implement the following `guessCost` heuristic for **GridGraph** and **WordLadder**:

- **GridGraph**:
  The [straight-line distance](https://en.wikipedia.org/wiki/Euclidean_distance) between the two points.
  You may find some methods of the **Point** class useful here.
  After implementing it, you should see an improvement in the number of loop iterations.

    ```
    $ java RunPathFinder ucs GridGraph-NoGrid graphs/GridGraph/AR0012SR.map 11:73 85:127
    Loop iterations: 40266
    Elapsed time: 0.083s
    Cost of path from 11:73 to 85:127: 147.68
    Number of edges: 122
    11:73 -> 12:73 -> 13:74 -> 14:75 -> 15:75 -> ..... -> 86:123 -> 85:124 -> 85:125 -> 85:126 -> 85:127

    $ java RunPathFinder astar GridGraph-NoGrid graphs/GridGraph/AR0012SR.map 11:73 85:127
    Loop iterations: 16700
    Elapsed time: 0.064s
    Cost of path from 11:73 to 85:127: 147.68
    Number of edges: 122
    11:73 -> 12:73 -> 13:73 -> 14:73 -> 15:73 -> ..... -> 87:123 -> 86:124 -> 86:125 -> 85:126 -> 85:127
    ```

- **WordLadder**:
  The number of character positions where the letters differ from each other.
  For example, `guessCost("örter", "arten")` should return 2: the first and last characters differ (`ö`/`a` and `r`/`n`), but the middle ones (`rte`) are the same.
  Your method should not fail if it happens to be called on words of different length, but the return value then doesn't matter much (why?).

    ```
    $ java RunPathFinder ucs WordLadder graphs/WordLadder/swedish-saldo.txt eller glada
    Loop iterations: 25481
    Elapsed time: 0.252s
    Cost of path from eller to glada: 7
    Number of edges: 7
    eller -> elles -> ellas -> elias -> glias -> glids -> glads -> glada

    $ java RunPathFinder astar WordLadder graphs/WordLadder/swedish-saldo.txt eller glada
    Loop iterations: 192
    Elapsed time: 0.024s
    Cost of path from eller to glada: 7
    Number of edges: 7
    eller -> elles -> ellas -> elias -> glias -> glids -> glads -> glada
    ```

- You don't have to implement `guessCost` for **AdjacencyGraph**.
  That would need domain-specific information about the graph, which the class does not have.
  But see the optional tasks below.

### Important note

The A* algorithm works correctly only if the heuristic is *admissible*, which means that the heuristic must never over-estimate the cost.
It's fine to under-estimate: it will still find an optimal path, but if the heuristic is too under-estimating, it will take longer.


## Your submission

Push your changes to your repository on Chalmers GitLab.
This should be the following files:

- **PathFinder.java**: Task 1 and 3
- **WordLadder.java**: Task 2 and 4
- **GridGraph.java**: Task 4
- **answers.txt**, with all (non-optional) questions answered

Search for all the places with a comment called `TODO: Task n`.

When you are finished, create a tag `submission0` (for the commit you wish to submit).
For re-submissions, use `submission1`, `submission2`, etc.
The tag serves as your proof of submission.
You cannot change or delete it afterwards.
We will then grade your submission and post our feedback as issues in your project.
For more information on how to submit, see "Doing the lab assignments" on Canvas.

Please do not change the signatures or behaviour of pre-existing methods unless instruncted, as it will make your submission harder for us to test and grade.
Of course, you are allowed to add things (such as new instance variables or private methods) that do not affect the existing functionality.

### Robograder

Our friendly *Robograder* returns to help you test your submission before you submit.
Create a tag starting with `test` (for example `testPlz`) on Chalmers GitLab, and Robograder will report back to you in a GitLab Issue.
Alas, Robograder only has time to test your submission twice per deadline.


## Optional tasks

This lab can be expanded in several ways, here are only some suggestions:

- Try the implementations on more graphs: There are several to download from [Moving AI Lab](https://www.movingai.com/benchmarks/grids.html), or [the SNAP project](http://snap.stanford.edu/data/index.html). You can also create more random mazes from several places on the web (search for "random maze generator").

- Show the results nicer, e.g., as an animation (for **NPuzzle**).

- **WordLadder** only connects words of the start and goal have the same length.
  Invent and implement word ladder rules for changing the number of letters in a word.
  Remember that all graph nodes must be words in the dictionary.

- You can assign different costs for different kinds of "terrain" (i.e., different characters) in **GridGraph**.

- Try to come up with an even better admissible heuristic than straight-line distance for **GridGraph**.
  Hint: Modify the [Manhattan distance](https://en.wikipedia.org/wiki/Taxicab_geometry) so that it allows for diagonal moves.

- Experiment with different representation of the state in **NPuzzle**.
  See the comments of the internal class **State**.

- Implement code for reading other graph formats.
  For example, you could read an image as a graph where where dark pixels are considered obstacles.
  See the PNG files in `graphs/GridGraph` for examples.

- Implement a heuristic for roadmaps (**citygraph-XX.txt**).
  For this you need locations of all cities, and they can be found in the [DSpace-CRIS database](https://dspace-cris.4science.it/handle/123456789/31).
  You have to scrape the information you need from the database, i.e., the latitude-longitude of each city.
  Then you have to filter the database to only include the cities you want.
  You also need to read in the position database in the graph class, and finally you need to [translate latitude-longitude into kilometers](https://stackoverflow.com/questions/27928/calculate-distance-between-two-latitude-longitude-points-haversine-formula).

- What kind of heuristic could be useful for the link distance between two Wikipedia pages (remember **wikipedia-graph.txt**)?
  Assume e.g. that you know the text content of both pages.
  Or that you know the [categories](https://en.wikipedia.org/wiki/Help:Category) that each Wikipedia page belongs to.
