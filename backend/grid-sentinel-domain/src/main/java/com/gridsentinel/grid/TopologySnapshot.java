package com.gridsentinel.grid;

import com.gridsentinel.common.CommonKernel;
import com.gridsentinel.asset.Asset;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;

import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/**
 * Immutable snapshot of grid topology at a point in time.
 * Used for deterministic simulation, risk assessment, and incident response.
 * <p>
 * Provides graph-based connectivity analysis using JGraphT.
 * </p>
 */
public final class TopologySnapshot {

    private final String snapshotId;
    private final Instant capturedAt;
    private final Map<CommonKernel.GridNodeId, GridNode> nodes;
    private final Map<CommonKernel.GridEdgeId, GridEdge> edges;
    private final Map<CommonKernel.GridNodeId, Set<CommonKernel.GridEdgeId>> adjacency;
    private final transient Graph<CommonKernel.GridNodeId, DefaultEdge> graph;

    private TopologySnapshot(Builder builder) {
        this.snapshotId = UUID.randomUUID().toString();
        this.capturedAt = Instant.now();
        this.nodes = Map.copyOf(Objects.requireNonNull(builder.nodes, "Nodes cannot be null"));
        this.edges = Map.copyOf(Objects.requireNonNull(builder.edges, "Edges cannot be null"));
        this.adjacency = buildAdjacency();
        this.graph = buildGraph();
    }

    private Map<CommonKernel.GridNodeId, Set<CommonKernel.GridEdgeId>> buildAdjacency() {
        Map<CommonKernel.GridNodeId, Set<CommonKernel.GridEdgeId>> adj = new HashMap<>();
        for (GridEdge edge : edges.values()) {
            adj.computeIfAbsent(edge.getFromNodeId(), k -> new HashSet<>()).add(edge.getId());
            adj.computeIfAbsent(edge.getToNodeId(), k -> new HashSet<>()).add(edge.getId());
        }
        return Map.copyOf(adj);
    }

    private Graph<CommonKernel.GridNodeId, DefaultEdge> buildGraph() {
        Graph<CommonKernel.GridNodeId, DefaultEdge> g = new SimpleGraph<>(DefaultEdge.class);
        for (CommonKernel.GridNodeId nodeId : nodes.keySet()) {
            g.addVertex(nodeId);
        }
        for (GridEdge edge : edges.values()) {
            g.addEdge(edge.getFromNodeId(), edge.getToNodeId());
        }
        return g;
    }

    // =========================================================================
    // Query Methods
    // =========================================================================

    public GridNode getNode(CommonKernel.GridNodeId id) {
        return nodes.get(id);
    }

    public GridEdge getEdge(CommonKernel.GridEdgeId id) {
        return edges.get(id);
    }

    public Set<GridNode> getAllNodes() {
        return Set.copyOf(nodes.values());
    }

    public Set<GridEdge> getAllEdges() {
        return Set.copyOf(edges.values());
    }

    public Set<CommonKernel.GridEdgeId> getConnectedEdges(CommonKernel.GridNodeId nodeId) {
        return adjacency.getOrDefault(nodeId, Collections.emptySet());
    }

    public Set<GridNode> getConnectedNodes(CommonKernel.GridNodeId nodeId) {
        Set<GridNode> connected = new HashSet<>();
        for (CommonKernel.GridEdgeId edgeId : getConnectedEdges(nodeId)) {
            GridEdge edge = edges.get(edgeId);
            if (edge != null) {
                connected.add(nodes.get(edge.getOtherNode(nodeId)));
            }
        }
        return connected;
    }

    /**
     * Find all nodes reachable from a starting node (connected component).
     */
    public Set<GridNode> getReachableNodes(CommonKernel.GridNodeId startNodeId) {
        Set<GridNode> reachable = new HashSet<>();
        Set<CommonKernel.GridNodeId> visited = new HashSet<>();
        dfs(startNodeId, visited);
        for (CommonKernel.GridNodeId id : visited) {
            GridNode node = nodes.get(id);
            if (node != null) reachable.add(node);
        }
        return reachable;
    }

    private void dfs(CommonKernel.GridNodeId current, Set<CommonKernel.GridNodeId> visited) {
        visited.add(current);
        for (CommonKernel.GridEdgeId edgeId : getConnectedEdges(current)) {
            GridEdge edge = edges.get(edgeId);
            if (edge != null) {
                CommonKernel.GridNodeId neighbor = edge.getOtherNode(current);
                if (!visited.contains(neighbor)) {
                    dfs(neighbor, visited);
                }
            }
        }
    }

    /**
     * Find shortest path (by edge count) between two nodes.
     * Returns empty if no path exists.
     */
    public java.util.List<CommonKernel.GridNodeId> findShortestPath(
            CommonKernel.GridNodeId from, CommonKernel.GridNodeId to) {

        if (!nodes.containsKey(from) || !nodes.containsKey(to)) {
            return Collections.emptyList();
        }

        // BFS for unweighted shortest path
        Map<CommonKernel.GridNodeId, CommonKernel.GridNodeId> parent = new HashMap<>();
        Set<CommonKernel.GridNodeId> visited = new HashSet<>();
        java.util.Queue<CommonKernel.GridNodeId> queue = new java.util.LinkedList<>();

        visited.add(from);
        queue.add(from);

        while (!queue.isEmpty()) {
            CommonKernel.GridNodeId current = queue.poll();
            if (current.equals(to)) break;

            for (CommonKernel.GridEdgeId edgeId : getConnectedEdges(current)) {
                GridEdge edge = edges.get(edgeId);
                if (edge != null) {
                    CommonKernel.GridNodeId neighbor = edge.getOtherNode(current);
                    if (!visited.contains(neighbor)) {
                        visited.add(neighbor);
                        parent.put(neighbor, current);
                        queue.add(neighbor);
                    }
                }
            }
        }

        if (!parent.containsKey(to) && !from.equals(to)) {
            return Collections.emptyList();
        }

        // Reconstruct path
        java.util.List<CommonKernel.GridNodeId> path = new java.util.ArrayList<>();
        CommonKernel.GridNodeId current = to;
        while (current != null) {
            path.add(current);
            current = parent.get(current);
        }
        Collections.reverse(path);
        return path;
    }

    /**
     * Find alternative paths between two nodes (for redundancy analysis).
     * Returns up to maxPaths paths, excluding the primary path.
     */
    public java.util.List<java.util.List<CommonKernel.GridNodeId>> findAlternativePaths(
            CommonKernel.GridNodeId from, CommonKernel.GridNodeId to, int maxPaths) {

        java.util.List<CommonKernel.GridNodeId> primary = findShortestPath(from, to);
        if (primary.isEmpty()) return Collections.emptyList();

        java.util.List<java.util.List<CommonKernel.GridNodeId>> alternatives = new java.util.ArrayList<>();
        Set<CommonKernel.GridEdgeId> primaryEdges = new HashSet<>();

        // Collect edges in primary path
        for (int i = 0; i < primary.size() - 1; i++) {
            GridNode u = nodes.get(primary.get(i));
            GridNode v = nodes.get(primary.get(i + 1));
            if (u != null && v != null) {
                for (CommonKernel.GridEdgeId edgeId : getConnectedEdges(u.getId())) {
                    GridEdge edge = edges.get(edgeId);
                    if (edge != null && edge.getOtherNode(u.getId()).equals(v.getId())) {
                        primaryEdges.add(edgeId);
                        break;
                    }
                }
            }
        }

        // Try removing each primary edge and finding new paths
        for (CommonKernel.GridEdgeId blockedEdge : primaryEdges) {
            if (alternatives.size() >= maxPaths) break;

            // Temporarily remove edge
            GridEdge edge = edges.get(blockedEdge);
            if (edge == null) continue;

            // Find path without this edge
            var path = findPathAvoidingEdge(from, to, blockedEdge);
            if (!path.isEmpty() && !path.equals(primary)) {
                alternatives.add(path);
            }
        }

        return alternatives;
    }

    private java.util.List<CommonKernel.GridNodeId> findPathAvoidingEdge(
            CommonKernel.GridNodeId from, CommonKernel.GridNodeId to, CommonKernel.GridEdgeId avoidEdge) {

        Map<CommonKernel.GridNodeId, CommonKernel.GridNodeId> parent = new HashMap<>();
        Set<CommonKernel.GridNodeId> visited = new HashSet<>();
        java.util.Queue<CommonKernel.GridNodeId> queue = new java.util.LinkedList<>();

        visited.add(from);
        queue.add(from);

        while (!queue.isEmpty()) {
            CommonKernel.GridNodeId current = queue.poll();
            if (current.equals(to)) break;

            for (CommonKernel.GridEdgeId edgeId : getConnectedEdges(current)) {
                if (edgeId.equals(avoidEdge)) continue;
                GridEdge edge = edges.get(edgeId);
                if (edge != null) {
                    CommonKernel.GridNodeId neighbor = edge.getOtherNode(current);
                    if (!visited.contains(neighbor)) {
                        visited.add(neighbor);
                        parent.put(neighbor, current);
                        queue.add(neighbor);
                    }
                }
            }
        }

        if (!parent.containsKey(to) && !from.equals(to)) {
            return Collections.emptyList();
        }

        java.util.List<CommonKernel.GridNodeId> path = new java.util.ArrayList<>();
        CommonKernel.GridNodeId current = to;
        while (current != null) {
            path.add(current);
            current = parent.get(current);
        }
        Collections.reverse(path);
        return path;
    }

    /**
     * Get the underlying JGraphT graph for advanced algorithms.
     */
    public Graph<CommonKernel.GridNodeId, DefaultEdge> getGraph() {
        return graph;
    }

    public int nodeCount() {
        return nodes.size();
    }

    public int edgeCount() {
        return edges.size();
    }

    public Instant getCapturedAt() {
        return capturedAt;
    }

    public String getSnapshotId() {
        return snapshotId;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private Map<CommonKernel.GridNodeId, GridNode> nodes = new HashMap<>();
        private Map<CommonKernel.GridEdgeId, GridEdge> edges = new HashMap<>();

        private Builder() {}

        public Builder nodes(Iterable<GridNode> nodes) {
            for (GridNode node : nodes) {
                this.nodes.put(node.getId(), node);
            }
            return this;
        }

        public Builder node(GridNode node) {
            this.nodes.put(node.getId(), node);
            return this;
        }

        public Builder edges(Iterable<GridEdge> edges) {
            for (GridEdge edge : edges) {
                this.edges.put(edge.getId(), edge);
            }
            return this;
        }

        public Builder edge(GridEdge edge) {
            this.edges.put(edge.getId(), edge);
            return this;
        }

        public TopologySnapshot build() {
            return new TopologySnapshot(this);
        }
    }
}