package ca.ualberta.cs.util;

public class PairDoubleInteger {
	private double key;
	private int value;
	
	public PairDoubleInteger(double key, int value) {
		super();
		this.key = key;
		this.value = value;
	}
	
	public double getKey() {
		return key;
	}
	
	public void setKey(double key) {
		this.key = key;
	}
	
	public int getValue() {
		return value;
	}
	
	public void setValue(int value) {
		this.value = value;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		long temp;
		temp = Double.doubleToLongBits(key);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + value;
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
		PairDoubleInteger other = (PairDoubleInteger) obj;
		if (Double.doubleToLongBits(key) != Double.doubleToLongBits(other.key))
			return false;
		if (value != other.value)
			return false;
		return true;
	}
}
