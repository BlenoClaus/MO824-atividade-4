package problems.qbfpt.solvers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import problems.qbf.solvers.TS_QBF;
import problems.qbfpt.triples.ForbiddenTriplesBuilder;
import solutions.Solution;

public class TS_QBFPT_Probabilistic extends TS_QBF {
	
	private ForbiddenTriplesBuilder ftBuilder;

	private float probabilityParameter;
	public TS_QBFPT_Probabilistic(Integer tenure, Integer iterations, String filename, float probability) throws IOException {
		super(tenure, iterations, filename);
		this.ftBuilder = new ForbiddenTriplesBuilder(ObjFunction.getDomainSize());
		this.probabilityParameter = probability;
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
		Double pBestCandIn = null, pBestCandOut = null;
		ArrayList<Double> pIn = new ArrayList<Double>(); 
		ArrayList<Double> pOut = new ArrayList<Double>(); 
		minDeltaCost = Double.POSITIVE_INFINITY;
		updateCL();
		
		
		// Evaluate insertions
		
		for (Integer candIn : CL) {
			double x = rng.nextFloat();
			pIn.add(x);
			if (x < probabilityParameter) {
				Double deltaCost = ObjFunction.evaluateInsertionCost(candIn, incumbentSol);
				if (!TL.contains(candIn) || incumbentSol.cost+deltaCost < bestSol.cost) {
					if (deltaCost < minDeltaCost) {
						minDeltaCost = deltaCost;
						bestCandIn = candIn;
						pBestCandIn = x;
						bestCandOut = null;
					}
				}
			}
		}
		// Evaluate removals
		for (Integer candOut : incumbentSol) {
			double x = rng.nextFloat();
			pOut.add(x);
			if (x < probabilityParameter) {
				Double deltaCost = ObjFunction.evaluateRemovalCost(candOut, incumbentSol);
				if (!TL.contains(candOut) || incumbentSol.cost+deltaCost < bestSol.cost) {
					if (deltaCost < minDeltaCost) {
						minDeltaCost = deltaCost;
						bestCandIn = null;
						bestCandOut = candOut;
						pBestCandOut = x;
					}
				}
			}
		}
		// Evaluate exchanges
		for (int i = 0; i < CL.size();i++){
			Integer candIn = CL.get(i);
			if(pIn.get(i)< probabilityParameter){
				for (int j = 0; j < incumbentSol.size();j++){
					Integer candOut = incumbentSol.get(j);
					if(pOut.get(i)< probabilityParameter){
					Double deltaCost = ObjFunction.evaluateExchangeCost(candIn, candOut, incumbentSol);
					if ((!TL.contains(candIn) && !TL.contains(candOut)) || incumbentSol.cost+deltaCost < bestSol.cost) {
						if (deltaCost < minDeltaCost) {
							minDeltaCost = deltaCost;
							bestCandIn = candIn;
							bestCandOut = candOut;
							pBestCandIn = pIn.get(i);
							pBestCandOut = pOut.get(i);
						}
					}
				}
			}
		}
		}
		// Implement the best non-tabu move
		TL.poll();
		if (bestCandOut != null) {
			incumbentSol.remove(bestCandOut);
			pOut.remove(pBestCandOut);
			CL.add(bestCandOut);
			TL.add(bestCandOut);
		} else {
			TL.add(fake);
		}
		TL.poll();
		if (bestCandIn != null) {
			incumbentSol.add(bestCandIn);
			CL.remove(bestCandIn);
			pIn.remove(pBestCandIn);
			TL.add(bestCandIn);
		} else {
			TL.add(fake);
		}
		ObjFunction.evaluate(incumbentSol);
		
		return null;
	}
	
	public static void main(String[] args) throws IOException {
		long startTime = System.currentTimeMillis();
		TS_QBF tabusearch = new TS_QBFPT_Probabilistic(10, 1000000, "TS_Framework/instances/qbf020",(float) 0.5);
		Solution<Integer> bestSol = tabusearch.solve();
		System.out.println("maxVal = " + bestSol);
		long endTime   = System.currentTimeMillis();
		long totalTime = endTime - startTime;
		System.out.println("Time = "+(double)totalTime/(double)1000+" seg");
		
	}
	
	

}
