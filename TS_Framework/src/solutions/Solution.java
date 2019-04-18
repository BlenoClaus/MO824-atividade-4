package solutions;

import java.util.ArrayList;

@SuppressWarnings("serial")
public class Solution<E> extends ArrayList<E> {
	
	public Double cost = Double.POSITIVE_INFINITY;
	
	private StringBuilder report = new StringBuilder();
	
	public Solution() {
		super();
	}
	
	public Solution(Solution<E> sol) {
		super(sol);
		cost = sol.cost;
		report.append(sol.getReport());
	}

	@Override
	public String toString() {
		return "Solution: cost=[" + cost + "], size=[" + this.size() + "], elements=" + super.toString();
	}
	
	public void appendReport(String info) {
		report.append(info+"\n");
	}
	
	public String getReport() {
		return this.report.toString();
	}
	
	@Override
	public boolean equals(Object arg0) {
		if (arg0 == null) {
			return false;
		}
		
		if (arg0 instanceof Solution) {
			Solution<?> arrayList = (Solution<?>) arg0;
			if (this.size() != arrayList.size()) {
				return false;
			}
			
			for (int i = 0; i < this.size(); i++) {
				if (!this.get(i).equals(arrayList.get(i))) {
					return false;
				}
			}
			return true;
		}
		return false;
	}

}

