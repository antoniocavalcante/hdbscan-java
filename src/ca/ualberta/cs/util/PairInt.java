package ca.ualberta.cs.util;

public class PairInt {
	public int a;
	public int b;
	
	public PairInt(int a, int b) {
		super();
		this.a = a;
		this.b = b;
	}
	
	public int getA() {
		return a;
	}
	
	public void setA(int a) {
		this.a = a;
	}
	
	public int getB() {
		return b;
	}
	
	public void setB(int b) {
		this.b = b;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + a;
		result = prime * result + b;
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
		PairInt other = (PairInt) obj;
		if (a == other.a && b == other.b)
			return true;
		if (a == other.b && b == other.a)
			return true;
		return false;
	}
	
	@Override
	public String toString() {
		return "Pair [a=" + a + ", b=" + b + "]";
	}	
}
