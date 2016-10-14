package ca.ualberta.cs.util;

import java.io.Serializable;

public class SeparatedPair implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public int T1;
	public int T2;
	
	public SeparatedPair(int t1, int t2) {
		super();
		T1 = t1;
		T2 = t2;
	}
	
	public FairSplitTree getT1() {
		return FairSplitTree.root.get(T1);
	}

	public FairSplitTree getT2() {
		return FairSplitTree.root.get(T2);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + T1;
		result = prime * result + T2;
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
		SeparatedPair other = (SeparatedPair) obj;
		if (T1 != other.T1)
			return false;
		if (T2 != other.T2)
			return false;
		return true;
	}
}
