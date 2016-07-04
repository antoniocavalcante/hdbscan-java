package ca.ualberta.cs.util;

public class Edge {

	public int u;
	public int v;
	public double w;
	
	public Edge(int u, int v, double w) {
		super();
		this.u = u;
		this.v = v;
		this.w = w;
	}

	public Edge() {

	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + u;
		result = prime * result + v;
		long temp;
		temp = Double.doubleToLongBits(w);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Edge other = (Edge) obj;
		if (u != other.u)
			return false;
		if (v != other.v)
			return false;
		if (Double.doubleToLongBits(w) != Double.doubleToLongBits(other.w))
			return false;
		return true;
	}
	
}
