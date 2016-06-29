package ca.ualberta.cs.util;

public class SeparatedPair {

	public FairSplitTree T1;
	public FairSplitTree T2;
	
	public SeparatedPair(FairSplitTree t1, FairSplitTree t2) {
		super();
		T1 = t1;
		T2 = t2;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((T1 == null) ? 0 : T1.hashCode());
		result = prime * result + ((T2 == null) ? 0 : T2.hashCode());
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
		if (T1 == null) {
			if (other.T1 != null)
				return false;
		} else if (!T1.equals(other.T1))
			return false;
		if (T2 == null) {
			if (other.T2 != null)
				return false;
		} else if (!T2.equals(other.T2))
			return false;
		return true;
	}
}
