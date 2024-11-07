package dev.hexnowloading.dungeonnowloading.components;

public class VertexNodeConnectionContext {
    private final VertexNode vertexNode;
    private final boolean isBeamParent;

    public VertexNodeConnectionContext(VertexNode vertexNode, boolean isBeamParent) {
        this.vertexNode = vertexNode;
        this.isBeamParent = isBeamParent;
    }

    public VertexNode getVertexNode() {
        return vertexNode;
    }

    public boolean isBeamParent() {
        return isBeamParent;
    }}
