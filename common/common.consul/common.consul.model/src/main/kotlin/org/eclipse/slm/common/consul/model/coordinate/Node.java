package org.eclipse.slm.common.consul.model.coordinate;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Node {

	@JsonProperty("Node")
	private String node;

	@JsonProperty("Coord")
	private Coord coord;

	public String getNode() {
		return node;
	}

	public void setNode(String node) {
		this.node = node;
	}

	public Coord getCoord() {
		return coord;
	}

	public void setCoord(Coord coord) {
		this.coord = coord;
	}

	@Override
	public String toString() {
		return "Node{" +
				"node='" + node + '\'' +
				", coord=" + coord +
				'}';
	}
}
