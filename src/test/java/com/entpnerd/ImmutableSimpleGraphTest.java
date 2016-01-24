package graphun;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import graphun.ImmutableSimpleGraph.SimpleGraphBuilder;
import graphun.SimpleGraph.EventProcessor;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.google.common.collect.Sets;

/** JUnit tests for {@link ImmutableGraph}. */
public class ImmutableSimpleGraphTest {

    @Mock
    Node mockNodeCenter;
    @Mock
    Node mockNodeLeft;
    @Mock
    Node mockNodeRight;
    @Mock
    Node mockRim1;
    @Mock
    Node mockRim2;
    @Mock
    Node mockRim3;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void addNodeHappy() {
        SimpleGraphBuilder builder = new SimpleGraphBuilder();
        SimpleGraphBuilder actBuilder;
        actBuilder = builder.addNode(this.mockNodeCenter);
        assertEquals(builder, actBuilder);
        Map<Node, Set<Node>> expGraph = new HashMap<>();
        expGraph.put(this.mockNodeCenter, new HashSet<Node>());

        Map<Node, Set<Node>> actGraph = builder.getNodeLinks();
        assertEquals(expGraph, actGraph);
    }

    @Test
    public void addNodeNull() {
        SimpleGraphBuilder builder = new SimpleGraphBuilder();
        try {
            builder.addNode(null);
            fail();
        }
        catch (IllegalArgumentException e) {
            // expect this when adding null node
        }
    }

    /**
     * If a node is already present the graph, the expectation is that it is simply re-added.
     */
    @Test
    public void addNodeDuplicate() {
        SimpleGraphBuilder builder = new SimpleGraphBuilder();
        SimpleGraphBuilder actBuilder;
        actBuilder = builder.addNode(this.mockNodeRight);
        assertEquals(builder, actBuilder);
        try {
            builder.addNode(this.mockNodeRight);
            fail();
        }
        catch (IllegalArgumentException e) {
            // expected to fail fast here
        }
    }

    @Test
    public void connectNodesHappy() {
        SimpleGraphBuilder builder = new SimpleGraphBuilder();
        builder.addNode(this.mockNodeLeft);
        builder.addNode(this.mockNodeRight);
        SimpleGraphBuilder actBuilder = builder.connect(this.mockNodeLeft, this.mockNodeRight);
        assertEquals(builder, actBuilder);

        Map<Node, Set<Node>> expGraph = new HashMap<>();
        expGraph.put(this.mockNodeLeft, Sets.newHashSet(this.mockNodeRight));
        expGraph.put(this.mockNodeRight, Sets.newHashSet(this.mockNodeLeft));

        Map<Node, Set<Node>> actGraph = builder.getNodeLinks();
        assertEquals(expGraph, actGraph);
    }

    @Test
    public void connectNodesNotYetAddedHappy() {
        SimpleGraphBuilder builder = new SimpleGraphBuilder();
        SimpleGraphBuilder actBuilder;
        actBuilder = builder.connect(this.mockNodeLeft, this.mockNodeRight);
        assertEquals(builder, actBuilder);

        Map<Node, Set<Node>> expGraph = new HashMap<>();
        expGraph.put(this.mockNodeLeft, Sets.newHashSet(this.mockNodeRight));
        expGraph.put(this.mockNodeRight, Sets.newHashSet(this.mockNodeLeft));

        Map<Node, Set<Node>> actGraph = builder.getNodeLinks();
        assertEquals(expGraph, actGraph);
    }

    /**
     * Test that when connecting a node to itself, an edge is created between the two nodes.
     */
    @Test
    public void connectDuplicateNodes() {
        SimpleGraphBuilder builder = new SimpleGraphBuilder();
        builder.addNode(this.mockNodeLeft);
        try {
            builder.connect(this.mockNodeLeft, this.mockNodeLeft);
            fail();
        }
        catch (IllegalArgumentException e) {
            // expected to fail fast like this
        }
    }

    /**
     * Test that when connecting a node to itself, an edge is created between the two nodes.
     */
    @Test
    public void connectNodesAlreadyConnected() {
        SimpleGraphBuilder builder = new SimpleGraphBuilder();
        builder.connect(this.mockNodeLeft, this.mockNodeRight);
        try {
            builder.connect(this.mockNodeLeft, this.mockNodeRight);
            fail();
        }
        catch (IllegalArgumentException e) {
            // expected to fail fast like this
        }
    }

    @Test
    public void connectLeftNullNode() {
        SimpleGraphBuilder builder = new SimpleGraphBuilder();
        try {
            builder.connect(null, this.mockNodeRight);
            fail();
        }
        catch (IllegalArgumentException e) {
            // expected to the throw this.
        }
    }

    @Test
    public void connectRightNullNode() {
        SimpleGraphBuilder builder = new SimpleGraphBuilder();
        try {
            builder.connect(this.mockNodeLeft, null);
            fail();
        }
        catch (IllegalArgumentException e) {
            // expected to the throw this.
        }
    }

    @Test
    public void deleteHappy() {
        SimpleGraphBuilder builderContainingThreeProngWheel = setupThreeProngWheel();

        SimpleGraphBuilder actBuilder = builderContainingThreeProngWheel.delete(this.mockNodeCenter);
        assertEquals(builderContainingThreeProngWheel, actBuilder);

        Map<Node, Set<Node>> expGraph = new HashMap<>();
        expGraph.put(this.mockRim1, Sets.newHashSet(this.mockRim2, this.mockRim3));
        expGraph.put(this.mockRim2, Sets.newHashSet(this.mockRim1, this.mockRim3));
        expGraph.put(this.mockRim3, Sets.newHashSet(this.mockRim1, this.mockRim2));

        Map<Node, Set<Node>> actGraph = builderContainingThreeProngWheel.getNodeLinks();
        assertEquals(expGraph, actGraph);
    }

    @Test
    public void deleteNodeNull() {
        SimpleGraphBuilder builder = new SimpleGraphBuilder();
        builder.addNode(this.mockNodeCenter);
        try {
            builder.delete(null);
            fail();
        }
        catch (IllegalArgumentException e) {
            // expected to fail fast in this manner
        }
    }

    @Test
    public void deleteNodeNotYetAdded() {
        SimpleGraphBuilder builder = new SimpleGraphBuilder();
        try {
            builder.delete(this.mockNodeCenter);
            fail();
        }
        catch (IllegalArgumentException e) {
            // expected to fail fast in this manner
        }
    }

    @Test
    public void testBuildHappy() {
        SimpleGraphBuilder threeProngWheelBuilder = setupThreeProngWheel();
        ImmutableSimpleGraph actGraph = threeProngWheelBuilder.build();
        Map<Node, Set<Node>> actNodeLinks = actGraph.getNodeLinks();

        Map<Node, Set<Node>> expNodeLinks = new HashMap<>();
        expNodeLinks.put(this.mockNodeCenter, Sets.newHashSet(this.mockRim1, this.mockRim2, this.mockRim3));
        expNodeLinks.put(this.mockRim1, Sets.newHashSet(this.mockRim2, this.mockRim3, this.mockNodeCenter));
        expNodeLinks.put(this.mockRim2, Sets.newHashSet(this.mockRim1, this.mockRim3, this.mockNodeCenter));
        expNodeLinks.put(this.mockRim3, Sets.newHashSet(this.mockRim1, this.mockRim2, this.mockNodeCenter));

        assertEquals(expNodeLinks, actNodeLinks);
    }

    @Test
    public void basicFunctionalityForEmptyGraph() {
        ImmutableSimpleGraph emptyGraph = buildEmptyGraph();
        // can't do DFS because there is no root in the empty graph
        verifyIsConnected(emptyGraph, false);
        verifyGetNumEdges(emptyGraph, 0);
        verifyGetNumVertices(emptyGraph, 0);
    }

    @Test
    public void basicFunctionalityForOneDotGraph() {
        ImmutableSimpleGraph oneDot = buildOneDotGraph();
        verifyDfs(oneDot, 1);
        verifyIsConnected(oneDot, false);
        verifyGetNumEdges(oneDot, 0);
        verifyGetNumVertices(oneDot, 1);
    }

    @Test
    public void basicFunctionalityForThreeProngWheelGraph() {
        ImmutableSimpleGraph threeProngWheel = buildThreeProngWheelGraph();
        verifyDfs(threeProngWheel, 4);
        verifyIsConnected(threeProngWheel, true);
        verifyGetNumEdges(threeProngWheel, 6);
        verifyGetNumVertices(threeProngWheel, 4);
    }

    @Test
    public void basicFunctionalityForTwoBarsGraph() {
        ImmutableSimpleGraph twoBars = buildTwoBarsGraph();
        // node count is 2 because no matter where the root, DFS will only be able to traverse 2 out of 4 nodes.
        verifyDfs(twoBars, 2);
        verifyIsConnected(twoBars, false);
        verifyGetNumEdges(twoBars, 2);
        verifyGetNumVertices(twoBars, 4);
    }

    @Test
    public void basicFunctionalityForTwoDotsGraph() {
        ImmutableSimpleGraph twoDots = buildTwoDotsGraph();
        verifyDfs(twoDots, 1);
        verifyIsConnected(twoDots, false);
        verifyGetNumEdges(twoDots, 0);
        verifyGetNumVertices(twoDots, 2);
    }

    private void verifyDfs(ImmutableSimpleGraph graph, int expNumNodes) {
        EventProcessor mockEventProcessor = mock(EventProcessor.class);
        // do nothing by default for processEdge()
        // but for processNode(), effectively count the nodes, DFS should not double count nodes, only processing new
        // nodes.

        // use an AtomicInteger here as a hack so that I can increment nodeCount from processNode answer. Anonymous
        // class methods can't refer to a non-final variable, so I exploit the incrementation functionality of
        // AtomicInteger.
        final AtomicInteger nodeCount = new AtomicInteger(0);
        doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {
                nodeCount.incrementAndGet();
                return null;
            }
        }).when(mockEventProcessor).processNode(any(Node.class));

        Node root = graph.getNodeLinks().keySet().iterator().next();
        graph.dfs(root, mockEventProcessor);
        assertEquals(expNumNodes, nodeCount.get());
    }

    private void verifyGetNumEdges(ImmutableSimpleGraph graph, int expNumEdges) {
        int actNumEdges = graph.getNumEdges();
        assertEquals(expNumEdges, actNumEdges);
    }

    private void verifyGetNumVertices(ImmutableSimpleGraph graph, int expNumVertices) {
        int actNumVertices = graph.getNumVertices();
        assertEquals(expNumVertices, actNumVertices);
    }

    private void verifyIsConnected(ImmutableSimpleGraph graph, boolean expIsConnected) {
        boolean actIsConnected = graph.isConnected();
        assertEquals(expIsConnected, actIsConnected);
    }

    private ImmutableSimpleGraph buildThreeProngWheelGraph() {
        SimpleGraphBuilder builder = setupThreeProngWheel();
        ImmutableSimpleGraph threeProngWheelGraph = builder.build();
        return threeProngWheelGraph;
    }

    private ImmutableSimpleGraph buildEmptyGraph() {
        SimpleGraphBuilder builder = new SimpleGraphBuilder();
        ImmutableSimpleGraph emptyGraph = builder.build();
        return emptyGraph;
    }

    private ImmutableSimpleGraph buildOneDotGraph() {
        SimpleGraphBuilder builder = new SimpleGraphBuilder();
        builder.addNode(this.mockNodeCenter);
        ImmutableSimpleGraph oneDotGraph = builder.build();
        return oneDotGraph;
    }

    private ImmutableSimpleGraph buildTwoDotsGraph() {
        SimpleGraphBuilder builder = new SimpleGraphBuilder();
        builder.addNode(this.mockNodeLeft);
        builder.addNode(this.mockNodeRight);
        ImmutableSimpleGraph twoDotsGraph = builder.build();
        return twoDotsGraph;
    }

    private ImmutableSimpleGraph buildTwoBarsGraph() {
        SimpleGraphBuilder builder = new SimpleGraphBuilder();
        builder.connect(this.mockNodeLeft, this.mockNodeRight);
        builder.connect(this.mockRim1, this.mockRim2);
        ImmutableSimpleGraph twoBarsGraph = builder.build();
        return twoBarsGraph;
    }

    private SimpleGraphBuilder setupThreeProngWheel() {
        // setup and create a star patterned graph where outer nodes are connected in a rim
        SimpleGraphBuilder builder = new SimpleGraphBuilder();
        builder.addNode(this.mockNodeCenter);
        builder.connect(this.mockNodeCenter, this.mockRim1);
        builder.connect(this.mockNodeCenter, this.mockRim2);
        builder.connect(this.mockNodeCenter, this.mockRim3);
        builder.connect(this.mockRim1, this.mockRim2);
        builder.connect(this.mockRim2, this.mockRim3);
        builder.connect(this.mockRim3, this.mockRim1);

        return builder;
    }
}
