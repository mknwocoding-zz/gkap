package algorithmen;

import java.util.*;
import java.util.stream.Collectors;
import org.graphstream.algorithm.Algorithm;
import org.graphstream.graph.*;

public class DijkstraAlgorithm implements Algorithm {
    private Graph graph;
    private Set<Node> settledNodes, unSettledNodes;
    private Map<Node, Node> predecessors;

    private Map<Node, Double> distance;
    private Node source;

    public static <K, V extends Comparable<? super V>> Map<K, V> sortDistanceValue(Map<K, V> map) {
        return map.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByValue(/*Collections.reverseOrder()*/))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
                        (e1, e2) -> e1,
                        LinkedHashMap::new
                ));
    }

    /* (non-Javadoc)
     * @see org.graphstream.algorithm.Algorithm#compute()
     */
    @Override
    public void compute() {
        settledNodes = new HashSet<>();
        unSettledNodes = new HashSet<>();
        distance = new HashMap<>();
        predecessors = new HashMap<>();

        distance.put(source, 0.0);
        unSettledNodes.add(source);
        initDijkstraToViz();

        while (!unSettledNodes.isEmpty()) {
            Node node = getMinimum(unSettledNodes); // Select the Node with min. Distance
            settledNodes.add(node); // Add to settledNodes
            unSettledNodes.remove(node); // Remove, cuz we visited the Node
            findMinimalDistances(node); // Find for the new Node die new Distance
        }
    }

    /* (non-Javadoc)
     * @see org.graphstream.algorithm.Algorithm#init(org.graphstream.graph.Graph)
     */
    @Override
    public void init(Graph g) {
        this.graph = Objects.requireNonNull(g);
    }

    /**
     * Computes and returns the shortest Path from the source node to the target node
     *
     * @param source a start node
     * @param target a target node
     * @return the shortest path
     */
    public List<Node> getPath(Node source, Node target) {
        this.source = Objects.requireNonNull(source);
        Node target1 = Objects.requireNonNull(target);
        compute();
        return getPath(target1);
    }

    /**
     * Returns the path from the source to the selected target or null if no path exists
     *
     * @param target a selected target
     * @return the path from source to the
     */
    private List<Node> getPath(Node target) {
        LinkedList<Node> path = new LinkedList<>();
        Node step = target;

        if (Objects.isNull(predecessors.get(step))) return null;
        path.add(step);

        while (Objects.nonNull(predecessors.get(step))) {
            step = predecessors.get(step);
            path.add(step);
        }

        Collections.reverse(path);
        path.forEach(this::updateLabel);
        return path;
    }

	private void findMinimalDistances(Node node) {
		List<Node> adjacentNodes = getNeighbors(node);
		adjacentNodes.stream()
				.filter(target -> getShortestDistance(
						target) > getShortestDistance(node)
								+ getDistance(node, target))
				.forEach(target -> {
					distance.put(target, getShortestDistance(node) + getDistance(node, target));
					predecessors.put(target, node);
					unSettledNodes.add(target);
				});
	}

    private List<Node> getNeighbors(Node node) {
        List<Node> neighbors = new ArrayList<>();
        for (Edge edge : graph.getEachEdge()) {
            if (edge.getSourceNode().equals(node) && !isSettled(edge.getTargetNode())) {
                neighbors.add(edge.getTargetNode());
            } else if (edge.getTargetNode().equals(node) && !isSettled(edge.getSourceNode())) {
                neighbors.add(edge.getSourceNode());
            }
        }
        return neighbors;
    }

    /**
     * Returns the node with min. Distance
     *
     * @param nodes the set with the nodes
     * @return the node with the min distance
     */
    private Node getMinimum(Set<Node> nodes) {
        Node minimum = null;
        for (Node node : nodes) {
            if (Objects.isNull(minimum)) {
                minimum = node;
            } else if (getShortestDistance(node) < getShortestDistance(minimum)) {
                minimum = node;
            }
        }
        return minimum;
    }

    /**
     * Returns the node with the Distance
     *
     * @param destination the set with the nodes
     * @return INF if the Distance not settled or the Distance value
     */
    private double getShortestDistance(Node destination) {
        Double d = distance.get(destination);
        return Objects.isNull(d) ? Double.POSITIVE_INFINITY : d;
    }

    /**
     * Returns the Weight from the reading Graph
     *
     * @param node the startNode
     * @param target the targetNode
     *
     * @return Weight from the Graph if found else 0
     */
    private int getDistance(Node node, Node target) {
        for (Edge edge : graph.getEachEdge()) {
            if (edge.getSourceNode().equals(node) && edge.getTargetNode().equals(target) ||
                    edge.getTargetNode().equals(node) && edge.getSourceNode().equals(target)) {
                return Integer.parseInt(edge.getAttribute("weight").toString());
            }
        }
        throw new RuntimeException("Runetime Error");
    }

    private void initDijkstraToViz() {
        for (Node node : graph) {
            if (!node.equals(source)) {
                node.addAttribute("Distance", Double.POSITIVE_INFINITY);
                node.addAttribute("OK", false);
                node.addAttribute("Predecessor", 0);
                updateLabel(node);
            } else {
                source.addAttribute("Distance", 0.0);
                source.addAttribute("OK", true);
                source.addAttribute("Predecessor", source);
            }
        }

        for (Edge edge : graph.getEachEdge())
            edge.addAttribute("ui.label", edge.getAttribute("weight").toString());
    }

    private void updateLabel(Node node) {
        node.setAttribute("ui.label",
                node.getId() + " | Dist: "
                        + distance.get(node) + " | OK: "
                        + node.getAttribute("OK") + " | Pred: "
                        + node.getAttribute("Predecessor"));
    }

    private boolean isSettled(Node node) {
        return settledNodes.contains(node);
    }

    public long dijkstraRtm(Graph g, Node v1, Node v2) {
        init(g);
        long resultTime;
        long startTime = System.nanoTime();
        getPath(v1, v2);
        long endTime = System.nanoTime();
        resultTime = endTime - startTime;
        return resultTime;
    }
}