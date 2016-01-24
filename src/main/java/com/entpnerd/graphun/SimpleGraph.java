package graphun;

/**
 * Interface for standard operations of an undirected, simple graph. By <em>simple</em> it is meant that there are no
 * <em>loops</em> (i.e. an edge from a node itself), or <em>multi-edges</em> (i.e. multiple edges between the same pair
 * of nodes.
 */
public interface SimpleGraph {

    /**
     * Depth-first search traversal of this graph.
     * 
     * @param root the root node from which to start the traversal
     * @param eventProcessor processor for events when triggered during traversal
     */
    void dfs(Node root, EventProcessor eventProcessor);

    /**
     * Returns the number of vertices in the graph.
     * 
     * @return the number of vertices in the graph.
     */
    int getNumVertices();

    /**
     * Returns the number of edges in the graph.
     * 
     * @return the number of edges in the graph.
     */
    int getNumEdges();

    /**
     * Determines whether or not all nodes in this graph are connected to one another.
     * 
     * @return <code>true</code> if all nodes in the graph are connected to each other, <code>false</code> otherwise.
     */
    boolean isConnected();

    /** Builder for conveniently creating the undirected graph. */
    interface Builder {
        /**
         * Adds an unconnected node to graph being built.
         * 
         * @param node the {@link Node} to add
         * @return <code>this</code> builder
         * @throws IllegalArgumentException if:
         * <ol>
         * <li>The given <code>node</code> is null</li>
         * <li>The given <code>node</code> is already in the graph</li>
         * </ol>
         */
        Builder addNode(Node node);

        /**
         * Connects the <code>left</code> node with the <code>right</code> node. If either node has not already been
         * added to the graph, this method will add the appropriate node(s) as a convenience.
         * 
         * @param left the first node in the edge
         * @param right the second node in the edge
         * @return <code>this</code> builder
         * @throws IllegalArgumentException if:<br/>
         * <ol>
         * <li>The <code>left</code> node is null</li>
         * <li>The <code>right</code> node is null</li>
         * <li>The same node is being connected to itself.</li>
         * <li>The <code>left</code> and <code>right</code> nodes have already been connected.</li>
         * </ol>
         */
        Builder connect(Node left, Node right);

        /**
         * Deletes the node from the graph along with all edges that immediately connect to it. If the <code>node</code>
         * isn't part of of the graph, no action will be taken.
         * 
         * @param node the node to delete
         * @return <code>this</code> builder
         * @throws IllegalArgumentException if:<br/>
         * <ol>
         * <li>The given <code>node</code> is null</li>
         * <li>The given <code>node</code> is not already present in the graph being built</li>
         * </ol>
         */
        Builder delete(Node node);

        /**
         * Instantiates a new {@link SimpleGraph} based on the thus-far constructed graph. This builder may be reused
         * later to instantiate new graphs mutating the existing state of the builder.
         * 
         * @return a new {@link SimpleGraph} instance
         */
        SimpleGraph build();
    }

    /**
     * Processes events when they occur during graph algorithms.
     */
    interface EventProcessor {
        /**
         * Perform some custom action when a new {@link Edge} is reached during a graph traversal.
         * 
         * @param edge the edge to process
         */
        void processEdge(Edge edge);

        /**
         * Perform some custom action when a new {@link Node} is reached during a graph traversal.
         * 
         * @param node the {@link Node} to process
         */
        void processNode(Node node);
    }
}
