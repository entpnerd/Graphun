package graphun;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Stack;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableMap;

/** An immutable graph implementation if a {@link SimpleGraph}. */
public final class ImmutableSimpleGraph implements SimpleGraph {

    // don't mutate this graph and don't expose it. Otherwise, this won't be an immutable class.
    private final Map<Node, Set<Node>> nodeLinks;

    private ImmutableSimpleGraph(SimpleGraphBuilder builder) {
        this.nodeLinks = ImmutableMap.copyOf(builder.nodeLinks);
    }

    /** {@inheritDoc} */
    @Override
    public int getNumVertices() {
        int numVertices = this.nodeLinks.size();
        return numVertices;
    }

    /** {@inheritDoc} */
    @Override
    public int getNumEdges() {
        int numEdges = 0;
        for (Set<Node> adjacentNodes : this.nodeLinks.values()) {
            int numAdjacentNodes = adjacentNodes.size();
            numEdges += numAdjacentNodes;
        }
        // numEdges now has double the number of edges
        assert numEdges % 2 == 0;
        numEdges /= 2;
        return numEdges;
    }

    /** {@inheritDoc} */
    @Override
    public boolean isConnected() {
        // to determine if the entire graph is connected, do a DFS and ascertain whether the number of nodes found
        // equals the total number of nodes in the graph.
        Collection<Node> nodesInGraph = this.nodeLinks.keySet();
        
        // an empty graph is not connected and neither is a single unconnected node
        if (nodesInGraph.size() <= 1) {
            return false;
        }
        Node root = nodesInGraph.iterator().next();
        NodeCounter counter = new NodeCounter();
        dfs(root, counter);

        int numNodesTraversed = counter.numNodesFound;
        int numNodesInGraph = this.nodeLinks.size();
        boolean connected = (numNodesInGraph == numNodesTraversed);
        return connected;
    }

    /** {@inheritDoc} */
    @Override
    public void dfs(Node root, EventProcessor eventProcessor) {
        int numNodes = this.nodeLinks.size();
        Set<Node> visited = new HashSet<>(numNodes);
        // the stack is of type Iterator<Node> because we need to loop over several ancestors and their siblings. Type
        // Node alone isn't sufficient because we can't take into account siblings, Type List<Node> isn't sufficient
        // because we don't know about the current sibling, so it's Iterator<Node>.
        Stack<Iterator<Node>> ancestryStack = new Stack<>();

        visited.add(root);
        eventProcessor.processNode(root);
        Set<Node> rootChildren = this.nodeLinks.get(root);
        Iterator<Node> rootChildrenIterator = rootChildren.iterator();
        ancestryStack.push(rootChildrenIterator);
        while (!ancestryStack.isEmpty()) {
            // put peek here instead of using a for loop so that peek() doesn't throw an EmptyStackException when the
            // stack is finally empty at the end of the traversal.
            Iterator<Node> children = ancestryStack.peek();
            if (!children.hasNext()) {
                ancestryStack.pop();
                continue;
            }
            Node nextChild = children.next();
            if (visited.contains(nextChild)) {
                continue;
            }
            visited.add(nextChild);
            eventProcessor.processNode(nextChild);
            Set<Node> grandchildren = this.nodeLinks.get(nextChild);
            Iterator<Node> grandchildrenIterator = grandchildren.iterator();
            ancestryStack.push(grandchildrenIterator);
        }
    }

    /* (Non-Javadoc)
     * Only exists for unit testing, don't access this from non-unit-test code.
     * 
     * @return the internal graph data structure
     */
    @VisibleForTesting
    Map<Node, Set<Node>> getNodeLinks() {
        return this.nodeLinks;
    }

    public static final class SimpleGraphBuilder implements Builder {

        // basically the same as an adjacently list implementation, except that we use a map, instead.
        private final Map<Node, Set<Node>> nodeLinks;

        public SimpleGraphBuilder() {
            this.nodeLinks = new LinkedHashMap<>();
        }

        /** {@inheritDoc} */
        @Override
        public SimpleGraphBuilder addNode(Node node) {
            if (node == null) {
                throw new IllegalArgumentException("Can't add null node.");
            }
            if (this.nodeLinks.containsKey(node)) {
                throw new IllegalArgumentException("Won't add duplicate node. node='" + node + "'.");
            }
            Set<Node> newLinkedList = new LinkedHashSet<>();
            this.nodeLinks.put(node, newLinkedList);
            return this;
        }

        /** {@inheritDoc} */
        @Override
        public SimpleGraphBuilder connect(Node left, Node right) {
            if (left == null) {
                throw new IllegalArgumentException("Left node is null!");
            }
            if (right == null) {
                throw new IllegalArgumentException("Right node is null!");
            }
            if (left.equals(right)) {
                throw new IllegalArgumentException("Simple graphs can't connect nodes with themselves. left='" +
                        left + "', right='" + right + "'.");
            }
            if (nodesShareEdge(left, right)) {
                throw new IllegalArgumentException("Node left is already connected to right. left='" +
                        left + "', right='" + right + "'.");
            }

            // add nodes only after check so that the method doesn't haven't any side-effects under edge cases
            if(!this.nodeLinks.containsKey(left)) {
                addNode(left);
            }
            if(!this.nodeLinks.containsKey(right)) {
                addNode(right);
            }
            Set<Node> nodesAdjacentToLeftNode = this.nodeLinks.get(left);
            if(!nodesAdjacentToLeftNode.contains(right)) {
                nodesAdjacentToLeftNode.add(right);
            }
            Set<Node> nodesAdjacentToRightNode = this.nodeLinks.get(right);
            if (!nodesAdjacentToRightNode.contains(left)) {
                nodesAdjacentToRightNode.add(left);
            }
            return this;
        }

        private boolean nodesShareEdge(Node left, Node right) {
            if (!this.nodeLinks.containsKey(left)) {
                // node not yet in graph can't yet share edge with another node
                return false;
            }

            Set<Node> edgesOffLeft = this.nodeLinks.get(left);
            boolean leftAndRightHaveEdge = edgesOffLeft.contains(right);
            return leftAndRightHaveEdge;
        }

        /** {@inheritDoc} */
        @Override
        public SimpleGraphBuilder delete(Node node) {
            if(node == null) {
                throw new IllegalArgumentException("Can't delete null node.");
            }
            if(!this.nodeLinks.containsKey(node)) {
                throw new IllegalArgumentException("Can't delete node that is not already in graph. node='" + node +
                        "'.");
            }
            this.nodeLinks.remove(node);
            for (Entry<Node, Set<Node>> nodeEntry : this.nodeLinks.entrySet()) {
                Set<Node> edgesOffNode = nodeEntry.getValue();
                edgesOffNode.remove(node);
            }
            return this;
        }

        /** {@inheritDoc} */
        @Override
        public ImmutableSimpleGraph build() {
            ImmutableSimpleGraph graph = new ImmutableSimpleGraph(this);
            return graph;
        }

        /**
         * Only exists for unit testing, don't access this from non-unit-test code.
         * 
         * @return the internal graph data structure
         */
        @VisibleForTesting
        Map<Node, Set<Node>> getNodeLinks() {
            return this.nodeLinks;
        }
    }

    // Private instance to count all nodes encountered.
    private static final class NodeCounter implements EventProcessor {

        private int numNodesFound = 0;

        /** {@inheritDoc} */
        @Override
        public void processEdge(Edge edge) {
            throw new UnsupportedOperationException();
        }

        /** {@inheritDoc} */
        @Override
        public void processNode(Node node) {
            ++this.numNodesFound;
        }
    }
}
f
