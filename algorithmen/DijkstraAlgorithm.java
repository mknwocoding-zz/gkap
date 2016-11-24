package algorithmen;

import org.graphstream.algorithm.Algorithm;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static java.lang.Double.POSITIVE_INFINITY;
import static java.lang.Integer.parseInt;
import static java.util.Collections.reverse;
import static java.util.Objects.*;
/**
 * <h1>DijkstraAlgorithm.java</h1> Diese Klasse führt den Dijkstra Algorithmus aus
 *
 * @author Mesut koc
 * @version 1.0
 * @since 2016-11-22
 */
public class DijkstraAlgorithm implements Algorithm {
    private Graph graph;
    private Set<Node> trueNodes, falseNodes;
    private Map<Node, Node> predecessors;
    private Map<Node, Double> distance;
    private Node source, target;
    private int graphAccCounter;

    /* (non-Javadoc)
     * @see org.graphstream.algorithm.Algorithm#compute()
     */
    @Override
    public void compute() {
        trueNodes = new HashSet<>();
        falseNodes = new HashSet<>();
        distance = new HashMap<>();
        predecessors = new HashMap<>();

        //Der Startwert ist 0 für i = 1 und ∞ sonst
        distance.put(source, 0.0);
        // Source als vorerst einzigen Wert in _falseList schreiben
        falseNodes.add(source);

        do {
            // 1.) Suche unter den Knoten v(i) mit OK(i) = false einen Knoten v(h) mit dem kleinsten Wert von Entf(i)
            Node currentNode = getNodeWithShortDistance(falseNodes);

            // 2.) Setze OK(h) = true
            //ok.replace(currentNode, false, true);
            trueNodes.add(currentNode);
            falseNodes.remove(currentNode);

            // 3.) Für alle Knoten v(j) mit OK(j) = false, für die die Kante v(h),v(j)
            // exisitiert die Entfernung und gegebenenfalls den Vorgänger neuberechnen
            calcNewDistances(currentNode);
        } while (!falseNodes.isEmpty());
    }

    /* (non-Javadoc)
     * @see org.graphstream.algorithm.Algorithm#init(org.graphstream.graph.Graph)
     */
    @Override
    public void init(@NotNull Graph g) {
        this.graph = requireNonNull(g);
    }

    /**
     * Führt den eigentlichen Algorithmus aus. Dabei prüft die Methode, ob es sich um gültige Knoten handelt
     * und führt danach den Algorithmus aus.
     *
     * @param source der Startknoten
     * @param target der Zielknotem
     * @return der Kürzeste Weg von {@code source} zu {@code target}
     */
    public List<Node> getShortestPath(Node source, Node target) throws Exception {
        this.source = requireNonNull(source);
        this.target = requireNonNull(target);
        compute();
        return getShortestPath(target);
    }

    /**
     * Liefert den kürzesten Pfad vom Startknoten zu dem Zielknoten oder liefert null, wenn der Pfad nicht existiert.
     *
     * @param target der Zielknoten
     * @return der kürzeste Weg zum Zielknoten vom Startknoten
     */
    @Nullable
    private List<Node> getShortestPath(Node target) {
        LinkedList<Node> path = new LinkedList<>();
        Node step = target;

        if (isNull(predecessors.get(step))) return null;
        path.add(step);

        while (nonNull(predecessors.get(step))) {
            step = predecessors.get(step);
            path.add(step);
        }

        reverse(path);
        return path;
    }

    /**
     * Diese Methode überprüft in der FALSE-Liste,
     * welcher Knoten die kürzeste Distance hat.
     *
     * @param nodes die Liste mit dem Knoten
     * @return minimum der kürzeste Distance Weg
     */
    private Node getNodeWithShortDistance(Set<Node> nodes) {
        Node minimum = null;
        for (Node nodeItems : nodes) {
            if (isNull(minimum) || (getShortestDistance(nodeItems) < getShortestDistance(minimum)))
                minimum = nodeItems;
        }
        return minimum;
    }

    private void calcNewDistances(Node source) {
        List<Node> adjacentNodes = getNeighbors(source);
        adjacentNodes.stream()
                .filter(child -> getShortestDistance(child) > getShortestDistance(source) + getDistance(source, child))
                .forEach(target -> {
                    distance.put(target, getShortestDistance(source) + getDistance(source, target));
                    predecessors.put(target, source);
                    falseNodes.add(target);
                });
    }

    /**
     * Liefert alle Nachbarn des angegebenen Knotens
     * Dabei prüft es, ob der Knoten noch in der OK(FALSE) Liste
     *
     * @param node ein Knoten
     * @return List mit Nachbarn des Knotens
     */
    private List<Node> getNeighbors(Node node) {
        List<Node> adjacentNodes = new ArrayList<>();
        for (Edge edge : graph.getEachEdge()) {
            graphAccCounter++;
            if (edge.getSourceNode().equals(node) && isNotTrueNode(edge.getTargetNode())) {
                adjacentNodes.add(edge.getTargetNode());
            } else if (edge.getTargetNode().equals(node) && isNotTrueNode(edge.getSourceNode())) {
                adjacentNodes.add(edge.getSourceNode());
            }
        }
        return adjacentNodes;
    }

    /**
     * Returns the Weight from the reading Graph
     *
     * @param source the startNode
     * @param target the targetNode
     *
     * @return Weight from the Graph if found else 0
     */
    private int getDistance(Node source, Node target) {
        for (Edge edge : graph.getEachEdge()) {
            graphAccCounter++;

            boolean sourceNodeEquals = Objects.equals(edge.getSourceNode(), source),
                    targetNodeEquals = Objects.equals(edge.getTargetNode(), target),
                    targetNodeEqualsSource = Objects.equals(edge.getTargetNode(), source),
                    sourceNodeEqualsTarget = Objects.equals(edge.getSourceNode(), target);

            if ((sourceNodeEquals && targetNodeEquals) || (targetNodeEqualsSource && sourceNodeEqualsTarget))
                return parseInt(edge.getAttribute("weight").toString());
        }
        throw new RuntimeException("Distance not found");
    }

    @Override
    public String toString() {
        return "DijkstraAlgorithm{" +
                "  \ngraph=" + graph +
                ", \ntrueNodes=" + trueNodes +
                ", \nfalseNodes=" + falseNodes +
                ", \npredecessors=" + predecessors +
                ", \ndistance=" + distance +
                ", \nsource=" + source +
                ", \ntarget=" + target +
                ", \ngraphAccCounter=" + graphAccCounter +
                '}';
    }

    /**
     * Liefert von der Distance Map die Distance vom aktuellen Knoten
     *
     * @param destination ein Knoten
     * @return INF if the Distance not settled or if true the Distance value
     */
    private double getShortestDistance(Node destination) {
        return !isNull(distance.get(destination)) ? distance.get(destination) : POSITIVE_INFINITY;
    }

    /**
     * Returns the total Distance from the target node
     *
     * @return distance as double if the distance has a value, else INF
     */
    public Double getDistanceLength() {
        return distance.get(target);
    }

    /**
     * Liefert die gesamt Zugriffe auf den Grphen
     *
     * @return int mit dem Zugriffen
     */
    public int getGraphAccCounter() {
        return graphAccCounter;
    }

    /**
     * Proofs if the node false
     *
     * @param node a node
     * @return true if the node settled or false if not
     */
    private boolean isNotTrueNode(Node node) {
        return !trueNodes.contains(node);
    }

    /**
     * Proofs if the node settled
     *
     * @param g  a graph
     * @param v1 a Node
     * @param v2 a Node
     * @return the runtime value
     */
    public long dijkstraRtm(Graph g, Node v1, Node v2) throws Exception {
        init(g);
        long resultTime;
        long startTime = System.nanoTime();
        getShortestPath(v1, v2);
        long endTime = System.nanoTime();
        resultTime = endTime - startTime;
        return resultTime;
    }
}