package problems.qbfpt.solvers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import problems.qbf.solvers.TS_QBF;
import problems.qbfpt.triples.ForbiddenTriplesBuilder;
import solutions.Solution;

/**
 * Diversification
 * ideia: Passar como parametro um número de limite para interações na qual a solução não muda
 * para que o mecanismo de diversificação entre em ação.
 * O mecanismo de diversificação foi implementado assim:
 * É armazenado as frequencias do elementos que participam da solução e as soluções encontradas,
 * quando acontece X interações na qual a solução não é melhorada, 
 * é pegado, dentre os cadidados possiveis (que respeita a as restrições de triplas proibidas)
 * e adicionado o elemento na qual possui a menor frequencia
 *
 */
public class TS_QBFPT_Diversification extends TS_QBF{

	private ForbiddenTriplesBuilder ftBuilder;
	private Map<Integer, Integer> frequency = new HashMap<>();
	private Stack<Solution<Integer>> stackSolution = new Stack<Solution<Integer>>();
	private int interactionsWithoutImprovingSolution = 0;
	private int limitDiversification;

	public TS_QBFPT_Diversification(Integer tenure, Integer iterations, String filename, int limitDiversification) throws IOException {
		super(tenure, iterations, filename);
		this.ftBuilder = new ForbiddenTriplesBuilder(ObjFunction.getDomainSize());
		this.limitDiversification = limitDiversification;
		initFrequency();
	}

	private void initFrequency() {
		for (Integer i = 0; i < ObjFunction.getDomainSize(); i++) {
			frequency.put(i, 0);
		}
	}
	
	@Override
	public Solution<Integer> constructiveHeuristic() {
		Solution<Integer> solution = super.constructiveHeuristic();
		updateFrequency();
		return solution;
	}
	
	private Integer getFrequency(Integer elem) {
		Integer freq = frequency.get(elem);
		if (freq == null) return 0;
		return freq;
	}
	
	private Integer putIncrease(Integer elem) {
		Integer value = frequency.get(elem);
		if (value == null) {
			frequency.put(elem, 1);
			return 1;
		}
		value = value + 1;
		frequency.put(elem, value);
		return value;
	}
	
	private void updateFrequency() {
		incumbentSol.stream().forEach(elem -> putIncrease(elem));
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
		if (stackSolution.isEmpty()) {
			stackSolution.push(incumbentSol);
		} else {
			if (stackSolution.lastElement().equals(incumbentSol)) {
				this.interactionsWithoutImprovingSolution ++;	
				System.out.println("interactionsWithoutImprovingSolution = "+interactionsWithoutImprovingSolution);
				if (interactionsWithoutImprovingSolution >= limitDiversification) {
					Integer min = Integer.MAX_VALUE;
					Integer candidate = null;
					
					for(Integer clElem : CL) {
						Integer freq = getFrequency(clElem);
						if (min > freq) {
							min = freq;
							candidate = clElem;
						}
					}
					if (candidate != null) {
						incumbentSol.add(candidate);
						CL.remove(candidate);
						TL.add(candidate);
						stackSolution.push(incumbentSol);
						interactionsWithoutImprovingSolution = 0;
						System.out.println("candidate = "+candidate);
						System.out.println("new Solution = "+incumbentSol+" Old Soltion: "+stackSolution.lastElement());
					}
				}
				
			} else {
				stackSolution.push(incumbentSol);
				this.interactionsWithoutImprovingSolution = 0;
			}
			
		}
		updateFrequency();
		return null;
	}
	
	public static void main(String[] args) throws IOException {
		long startTime = System.currentTimeMillis();
		TS_QBF tabusearch = new TS_QBFPT_Diversification(20, 10000, "instances/qbf100", 20);
		Solution<Integer> bestSol = tabusearch.solve();
		System.out.println("maxVal = " + bestSol);
		long endTime   = System.currentTimeMillis();
		long totalTime = endTime - startTime;
		System.out.println("Time = "+(double)totalTime/(double)1000+" seg");
		
	}
	
	
}
