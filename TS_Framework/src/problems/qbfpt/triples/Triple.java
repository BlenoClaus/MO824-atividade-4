package problems.qbfpt.triples;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class Triple {
	
	private int x;
	private int y;
	private int z;

	public Triple(int x, int y, int z) {
		List<Integer> triple = Arrays.asList(x,y,z);
		Collections.sort(triple);
		this.x = triple.get(0);
		this.y = triple.get(1);
		this.z = triple.get(2);
	}
	
	public int getX() {
		return x;
	}

	public void setX(int x) {
		this.x = x;
	}

	public int getY() {
		return y;
	}

	public void setY(int y) {
		this.y = y;
	}

	public int getZ() {
		return z;
	}

	public void setZ(int z) {
		this.z = z;
	}
	
	public boolean contains(int u) {
		return x == u || y == u || z == u;
	}
	
	public boolean contains(int u, int t) {
		return contains(u) || contains(t);
	}
	
	public Integer getComplement(int u, int t) {
		if (x == u && y == t) return z;
		if (x == u && z == t) return y;
		if (y == u && z == t) return x;
		if (x == t && y == u) return z;
		if (x == t && z == u) return y;
		if (y == t && z == u) return x;
		return null;
	}
	
	@Override
	public String toString() {
		return "["+x+", "+y+", "+z+"]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + x;
		result = prime * result + y;
		result = prime * result + z;
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
		Triple other = (Triple) obj;
		if (x != other.x)
			return false;
		if (y != other.y)
			return false;
		if (z != other.z)
			return false;
		return true;
	}
	
	public boolean equals(int i, int j, int z) {
		List<Integer> l1 = Arrays.asList(i,j,z);
		List<Integer> l2 = Arrays.asList(x,y,z);
		Collections.sort(l1);
		Collections.sort(l2);
		return l1.equals(l2);
	}

	public Collection<? extends Integer> asList() {
		return Arrays.asList(x,y,z);
	}
	
	

}
