package com.signalflow.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

public class BlockGraph {

    private final List<Block> blocks;
    private final List<Connection> connections;

    public BlockGraph() {
        this.blocks = new ArrayList<>();
        this.connections = new ArrayList<>();
    }

    public List<Block> getBlocks() {
        return Collections.unmodifiableList(blocks);
    }

    public List<Connection> getConnections() {
        return Collections.unmodifiableList(connections);
    }

    public void addBlock(Block block) {
        if (block == null) {
            throw new IllegalArgumentException("Block must not be null");
        }
        if (!blocks.contains(block)) {
            blocks.add(block);
        }
    }

    /**
     * Removes a block and all connections that reference any of its ports.
     */
    public void removeBlock(Block block) {
        if (block == null) {
            return;
        }
        connections.removeIf(conn ->
                conn.getSourcePort().getParent() == block
                        || conn.getDestPort().getParent() == block);
        blocks.remove(block);
    }

    /**
     * Creates a new connection between an output port and an input port.
     *
     * @return the newly created Connection
     */
    public Connection addConnection(Port source, Port dest) {
        Connection connection = new Connection(source, dest);
        connections.add(connection);
        return connection;
    }

    public void removeConnection(Connection connection) {
        connections.remove(connection);
    }

    /**
     * Finds the connection that feeds into the given destination port.
     *
     * @return the Connection whose destPort matches, or null if none
     */
    public Connection findConnection(Port dest) {
        for (Connection conn : connections) {
            if (conn.getDestPort() == dest) {
                return conn;
            }
        }
        return null;
    }

    /**
     * Returns blocks in topological order using Kahn's algorithm.
     * Edges are derived from connections (source block → dest block).
     *
     * @return an ordered list safe for sequential evaluation
     * @throws IllegalStateException if the graph contains a cycle
     */
    public List<Block> topologicalSort() {
        // Build adjacency and in-degree maps
        Map<Block, Integer> inDegree = new HashMap<>();
        Map<Block, List<Block>> adjacency = new HashMap<>();

        for (Block block : blocks) {
            inDegree.put(block, 0);
            adjacency.put(block, new ArrayList<>());
        }

        for (Connection conn : connections) {
            Block sourceBlock = conn.getSourcePort().getParent();
            Block destBlock = conn.getDestPort().getParent();

            adjacency.get(sourceBlock).add(destBlock);
            inDegree.merge(destBlock, 1, Integer::sum);
        }

        // Seed queue with zero-in-degree blocks
        Queue<Block> queue = new LinkedList<>();
        for (Map.Entry<Block, Integer> entry : inDegree.entrySet()) {
            if (entry.getValue() == 0) {
                queue.add(entry.getKey());
            }
        }

        List<Block> sorted = new ArrayList<>();
        while (!queue.isEmpty()) {
            Block current = queue.poll();
            sorted.add(current);

            for (Block neighbor : adjacency.get(current)) {
                int updated = inDegree.get(neighbor) - 1;
                inDegree.put(neighbor, updated);
                if (updated == 0) {
                    queue.add(neighbor);
                }
            }
        }

        if (sorted.size() != blocks.size()) {
            throw new IllegalStateException(
                    "Graph contains a cycle — topological sort is not possible");
        }

        return sorted;
    }

    /**
     * Advances the entire graph by one simulation step:
     * 1. Propagate all connection values (source → dest).
     * 2. Tick each block in topological order.
     */
    public void tick(double dt) {
        for (Connection conn : connections) {
            conn.propagate();
        }

        List<Block> order = topologicalSort();
        for (Block block : order) {
            block.tick(dt);
        }
    }
}
