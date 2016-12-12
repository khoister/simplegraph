package com.panduit.graph;

import org.apache.commons.lang3.StringUtils;


public class Edge {
    private String label;
    private Double weight;

    public Edge() {
        this.label = StringUtils.EMPTY;
        this.weight = Double.POSITIVE_INFINITY;
    }

    public Edge(double weight) {
        this.label = StringUtils.EMPTY;
        this.weight = weight;
    }

    public Edge(final String label, double weight) {
        this.label = label;
        this.weight = weight;
    }

    public String getLabel() {
        return label;
    }

    public double getWeight() {
        return weight;
    }
}
