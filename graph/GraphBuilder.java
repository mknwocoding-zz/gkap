package graph;

import algorithm.BreadthFirstSearch;
import io.GraphSaver;
import org.graphstream.algorithm.generator.Generator;
import org.graphstream.algorithm.generator.GridGenerator;
import org.graphstream.algorithm.generator.RandomGenerator;
import org.graphstream.graph.Edge;
import org.graphstream.graph.EdgeRejectedException;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.MultiGraph;
import org.graphstream.graph.implementations.SingleGraph;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;

import static java.lang.String.format;
import static java.util.Objects.isNull;
import static java.util.regex.Pattern.compile;

/**
 * <h1>GraphBuilder.java</h1> Diese Klasse generiert einen Graphen
 *
 * @author Mesut koc
 * @version 1.4
 * @since 2016-10-30
 */

// TODO: mittels primitiven Datentypen implementieren. FORD mittels
public class GraphBuilder {
    private static final String ATTR_ARG_NAME = "capacity";
    private static Random random = new Random();

    private GraphBuilder() {
    }

    /**
     * Erstellt einen Graphen
     *
     * @param lines die zulesenden Daten aus dem Graph
     * @return ein Graph
     */
    public static Graph createGraph(List<String> lines) throws FileNotFoundException {
        if (lines.isEmpty()) return null;

        Graph graph = new MultiGraph("Graph");
        final String uml = "[_öÖäÄüÜßa-zA-Z0-9]";
        final String ws = "\\p{Blank}*";
        final String edgePattern = format("(%s+)(%s(-[->])%s(%s+))?(%s\\((%s*)\\))?(%s:%s(\\d+))?%s;", uml, ws, ws, uml, ws, uml, ws, ws, ws);

        for (String line : lines) {
            Matcher lineMatcher = compile(edgePattern).matcher(line);
            if (lineMatcher.matches()) {
                Boolean isDirected = false;
                String edgeID = "", edgeWeight = "", direction, node1;
                String node0 = lineMatcher.group(1);
                createNode(graph, node0);

                if (!isNull(lineMatcher.group(3)) && !isNull(lineMatcher.group(4))) {
                    direction = lineMatcher.group(3);
                    node1 = lineMatcher.group(4);
                    createNode(graph, node1);
                    if (!isNull(lineMatcher.group(6))) {
                        edgeID = lineMatcher.group(6);
                        if (!isNull(graph.getEdge(edgeID)))
                            edgeID = format("%s_to_%s", node0, node1);
                    } else
                        edgeID = format("%s_to_%s", node0, node1);

                    if (!isNull(lineMatcher.group(8))) edgeWeight = lineMatcher.group(8);
                    if (direction.equals("->")) isDirected = true;
                } else {
                    if (!isNull(lineMatcher.group(6))) node1 = node0;
                    else break;
                }
                addEdge(graph, edgeID, node0, node1, isDirected, edgeWeight);
            }
        }
        for (Node node : graph) node.addAttribute("ui.label", node.getId());
        return graph;
    }

    private static void addEdge(Graph graph, String edge, String node0,
                                String node1, Boolean isDirected, String weight) throws EdgeRejectedException {
        if (weight.equals("")) // without edgeWeight
            graph.addEdge(edge, node0, node1, isDirected);
        else
            graph.addEdge(edge, node0, node1, isDirected).setAttribute(ATTR_ARG_NAME, Integer.valueOf(weight));
    }

    private static void createNode(Graph graph, String node0) {
        if (graph.getNode(node0) == null) graph.addNode(node0);
    }

    /**
     * Erstellt einen Graphen mit anz. der Knoten und Kanten
     *
     * @return der erstelle Graph
     * @throws IOException falls der Graph nicht gespeichert werden kann
     */
    public static Graph createDirectedRandomGraph(int nodes, int edges) throws IOException {
        Graph result = new MultiGraph("big");
        if ((nodes * (nodes - 1) / 2)) throw new Exception("Zu viele Kanten! INFO: n(n-1)/2");

        for (int i = 1; i <= nodes; i++) {
            if (i == 1) resultGraph.addNode("q");
            if (i != nodes) resultGraph.addNode("v" + i);
            else resultGraph.addNode("s");
        }

        while (edges > 0) {
            int r1 = random.nextInt(nodes);
            int r2 = random.nextInt(nodes);
            int r3 = random.nextInt((nodes - 1) + 1);
            if (!hasEdge(resultGraph, r1, r2)) {
                createEdge(resultGraph, r1, r2);
                resultGraph.getEdge(format("%s_%s", resultGraph.getNode(r1).getId(), resultGraph.getNode(r2).getId())).addAttribute("weight", r3);
                edgesAnz--;
            } else {
                throw new EdgeRejectedException("Kante schon vorhanden");
            }
        }
        GraphSaver.saveGraph(result, new File("graph/subwerkzeuge/bspGraphen/saved/biG.gka"));
        return result;
    }

    private static boolean hasEdge(MultiGraph g, int r1, int r2) {
        return g.getEdge(g.getNode(r1).getId() + "_" + g.getNode(r2).getId()) != null;
    }

    private static Edge createEdge(MultiGraph g, int r1, int r2) {
        return g.addEdge(format("%s_%s", g.getNode(r1).getId(), g.getNode(r2).getId()), g.getNode(r1).getId(), g.getNode(r2).getId(), true);
    }

    /**
     * Quelle: graphstream / how to dynamically create & set attributes
     * https://github.com/graphstream/gs-algo/commit/de99cb6df56142d183d5a9cfd9078d865e2e3fab
     * Erstellt einen grit Network (random)
     *
     * @param nodes Anzahl der Knoten
     * @return ein Graph
     * @throws IOException falls nicht gespeichert werden kann
     */
    public static Graph createGritNetworkGraph(int nodes) throws IOException {
        Graph graph = new SingleGraph("Random");
        Generator gen = new GridGenerator(false, false, true, true);
        int moeglicheEdges = nodes * (nodes - 1) / 2;

        gen.addSink(graph);
        gen.begin();
        for (int i = 0; i < moeglicheEdges; i++) gen.nextEvents();
        gen.end();

        BreadthFirstSearch bfs = new BreadthFirstSearch();
        bfs.init(graph);
        bfs.compute();

        for (Edge edge : graph.getEachEdge())
            edge.setAttribute("capacity", Double.valueOf(edge.getTargetNode().getAttribute("steps").toString()));

        GraphSaver.saveGraph(graph, new File("graph/subwerkzeuge/bspGraphen/saved/BigNet_50_800_new.gka"));
        return graph;
    }

    public static Graph createRandomNetwork(int nodes, int maxEdges) throws IOException {
        Graph graph = new SingleGraph("Random");
        int averageDegree = (maxEdges / nodes) * 2;

        RandomGenerator gen = new RandomGenerator(averageDegree);
        gen.addSink(graph);
        gen.addEdgeAttribute("capacity");
        gen.setDirectedEdges(true, false);

        gen.begin();
        for (int i = 0; i < nodes; i++) gen.nextEvents();
        gen.end();

        GraphSaver.saveGraph(graph, new File("graph/subwerkzeuge/bspGraphen/saved/BigNet_50_800_NN.gka"));
        return graph;
    }

    /**
     * Visualisiert den Graphen als App
     *
     * @param graph     der zu visualierende Graph
     * @param showGraph soll der Graph angezeigt werden?
     */
    public static void setGraphSettings(Graph graph, boolean showGraph) {
        if (!showGraph) return;
        graph.setStrict(false); // Überprüft zB doppelte Knotennamen,benutzung
        // von nicht existierenden Elementen usw.
        graph.setAutoCreate(true); // nodes are automatically created when
        // referenced when creating a edge, even if
        // not yet inserted in the graph.
        graph.addAttribute("ui.stylesheet",
                "url('file:graph/subwerkzeuge/stylesheet')");
        System.setProperty("org.graphstream.ui.renderer",
                "org.graphstream.ui.j2dviewer.J2DGraphRenderer");
        graph.display();
    }
}