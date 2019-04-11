package problems.qbfpt.triples;

import java.util.ArrayList;
import java.util.List;

public class ForbiddenTriplesBuilder {
	
	private final static Integer pi1G = 131;
	private final static Integer pi2G = 1031;
	private final static Integer pi1H = 193;
	private final static Integer pi2H = 1093;
	
	private Integer n = new Integer(0);
	private List<Triple> forbiddenTriple = new ArrayList<>();
	
	public ForbiddenTriplesBuilder(Integer n) {
		this.n = n;
		this.build();
	}

	private Integer l_g(Integer u) {
		return 1 + ((pi1G * u + pi2G) % n);
	}
	
	private Integer l_h(Integer u) {
		return 1 + ((pi1H * u + pi2H) % n);
	}
	
	private Integer g(Integer u) {
		Integer lg = l_g(u);		
		return lg.equals(u)? 1 + (lg % n) : lg;
	}
	
	private Integer h(Integer u) {
		Integer lh = l_h(u);
		if (!lh.equals(u) && !lh.equals(g(u))) {
			return lh;
		}
		Integer lhMod = (1 + (lh % n));
		if (!lhMod.equals(u) && !lhMod.equals(g(u))) {
			return lhMod;
		}
		return 1 + (lh + 1 % n);
	}
	
	public List<Triple> build() {
		if (this.forbiddenTriple.isEmpty()) {
			for (Integer u = 0 ; u < n ; u++) {
				this.forbiddenTriple.add(new Triple(u+1, g(u), h(u)));
			}
		}
		return this.forbiddenTriple;
	}
	
	public static void main(String[] args) {
		int n = 5;
		List<Triple> build = new ForbiddenTriplesBuilder(n).build();
		for (Triple triple : build) {
			System.out.println(triple);
		}
		
	}

	public List<Integer> getForbiddenValues(Integer x, Integer y) {
		List<Integer> values = new ArrayList<>();
		for (Triple triple : forbiddenTriple) {
			if (triple.contains(x, y)) {
				Integer complement = triple.getComplement(x, y);
				if (complement != null) values.add(complement);
			}
		}
		return values;
	}
	
}
