package problems.qbfpt.solvers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import problems.qbf.solvers.TS_QBF;
import problems.qbfpt.triples.ForbiddenTriplesBuilder;
import solutions.Solution;

public class TS_QBFPT_Probabilistic extends TS_QBF {
	
	private ForbiddenTriplesBuilder ftBuilder;

	public TS_QBFPT_Probabilistic(Integer tenure, Integer iterations, String filename) throws IOException {
		super(tenure, iterations, filename);
		this.ftBuilder = new ForbiddenTriplesBuilder(ObjFunction.getDomainSize());
	}
	
	@Override
	public void updateCL() {
		if (!this.incumbentSol.isEmpty()) {
			List<Integer> forbiddenValues = new ArrayList<>();
			Integer lastElem = this.incumbentSol.get(this.incumbentSol.size()-1);
			for (int i = 0; i < this.incumbentSol.size()-1; i++) {
				forbiddenValues.addAll(ftBuilder.getForbiddenValues(this.incumbentSol.get(i)+1, lastElem+1));
			}
			for (Integer fv : forbiddenValues) {
				int index = CL.indexOf(fv-1);
				if (index >= 0) CL.remove(index);
			}
		}
	}

	@Override
	public Solution<Integer> neighborhoodMove() {

		Double minDeltaCost;
		Integer bestCandIn = null, bestCandOut = null;

		minDeltaCost = Double.POSITIVE_INFINITY;
		updateCL();
		// Evaluate insertions
		for (Integer candIn : CL) {
			Double deltaCost = ObjFunction.evaluateInsertionCost(candIn, incumbentSol);
			if (!TL.contains(candIn) || incumbentSol.cost+deltaCost < bestSol.cost) {
				if (deltaCost < minDeltaCost) {
					minDeltaCost = deltaCost;
					bestCandIn = candIn;
					bestCandOut = null;
				}
			}
		}
		// Evaluate removals
		for (Integer candOut : incumbentSol) {
			Double deltaCost = ObjFunction.evaluateRemovalCost(candOut, incumbentSol);
			if (!TL.contains(candOut) || incumbentSol.cost+deltaCost < bestSol.cost) {
				if (deltaCost < minDeltaCost) {
					minDeltaCost = deltaCost;
					bestCandIn = null;
					bestCandOut = candOut;
				}
			}
		}
		// Evaluate exchanges
		for (Integer candIn : CL) {
			for (Integer candOut : incumbentSol) {
				Double deltaCost = ObjFunction.evaluateExchangeCost(candIn, candOut, incumbentSol);
				if ((!TL.contains(candIn) && !TL.contains(candOut)) || incumbentSol.cost+deltaCost < bestSol.cost) {
					if (deltaCost < minDeltaCost) {
						minDeltaCost = deltaCost;
						bestCandIn = candIn;
						bestCandOut = candOut;
					}
				}
			}
		}
		// Implement the best non-tabu move
		TL.poll();
		if (bestCandOut != null) {
			incumbentSol.remove(bestCandOut);
			CL.add(bestCandOut);
			TL.add(bestCandOut);
		} else {
			TL.add(fake);
		}
		TL.poll();
		if (bestCandIn != null) {
			incumbentSol.add(bestCandIn);
			CL.remove(bestCandIn);
			TL.add(bestCandIn);
		} else {
			TL.add(fake);
		}
		ObjFunction.evaluate(incumbentSol);
		
		return null;
	}
	
	public static void main(String[] args) throws IOException {
		long startTime = System.currentTimeMillis();
		TS_QBF tabusearch = new TS_QBFPT_Probabilistic(20, 10000, "instances/qbf100");
		Solution<Integer> bestSol = tabusearch.solve();
		System.out.println("maxVal = " + bestSol);
		long endTime   = System.currentTimeMillis();
		long totalTime = endTime - startTime;
		System.out.println("Time = "+(double)totalTime/(double)1000+" seg");
		
	}
	
	

}
