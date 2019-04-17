package problems.qbfpt.solvers;

import java.io.IOException;
import java.util.ArrayDeque;
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
 * É armazenado as frequencias do elementos que participam da solução, as soluções encontradas,
 * as CL e TL das respectivas soluções,
 * quando acontece X interações na qual a solução não é melhorada, 
 * é pegado, dentre os cadidados possiveis (que respeita a as restrições de triplas proibidas)
 * e e que não está na solução inicial e é adicionado o elemento na qual possui a menor frequencia
 * (lógica de recomeço está no método restart)
 */
public class TS_QBFPT_Diversification extends TS_QBF{

	private ForbiddenTriplesBuilder ftBuilder;
	private Map<Integer, Integer> frequency = new HashMap<>();
	private Stack<Solution<Integer>> stackSolution = new Stack<Solution<Integer>>();
	private Stack<ArrayList<Integer>> stackCL = new Stack<ArrayList<Integer>>();
	private Stack<ArrayDeque<Integer>> stackTL = new Stack<ArrayDeque<Integer>>();
	private int interactionsWithoutImprovingSolution = 0;
	private int limitDiversification;
	private boolean isBestImproving;

	public TS_QBFPT_Diversification(Integer tenure, Integer iterations, String filename, boolean isBestImproving, int limitDiversification) throws IOException {
		super(tenure, iterations, filename);
		this.ftBuilder = new ForbiddenTriplesBuilder(ObjFunction.getDomainSize());
		this.limitDiversification = limitDiversification;
		this.initFrequency();
		this.isBestImproving = isBestImproving;
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
					if (!isBestImproving) {
						break;
					}
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
					if (!isBestImproving) {
						break;
					}
				}
			}
		}
		// Evaluate exchanges
		EXIT:
		for (Integer candIn : CL) {
			for (Integer candOut : incumbentSol) {
				Double deltaCost = ObjFunction.evaluateExchangeCost(candIn, candOut, incumbentSol);
				if ((!TL.contains(candIn) && !TL.contains(candOut)) || incumbentSol.cost+deltaCost < bestSol.cost) {
					if (deltaCost < minDeltaCost) {
						minDeltaCost = deltaCost;
						bestCandIn = candIn;
						bestCandOut = candOut;
						if (!isBestImproving) {
							break EXIT;
						}
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
			stackCL.push(CL);
			stackTL.push(TL);
		} else {
			if (stackSolution.lastElement().equals(incumbentSol)) {
				this.interactionsWithoutImprovingSolution ++;	
				//System.out.println("interactionsWithoutImprovingSolution = "+interactionsWithoutImprovingSolution);
				if (interactionsWithoutImprovingSolution >= limitDiversification) {
					restart();
				}
			} else {
				stackSolution.push(incumbentSol);
				stackCL.push(CL);
				stackTL.push(TL);
				this.interactionsWithoutImprovingSolution = 0;
			}
		}
		updateFrequency();
		return null;
	}
	
	private void restart() {
		if (stackSolution.isEmpty() || stackCL.isEmpty()) { 
			return;
		}
		Solution<Integer> solutionToRestart = stackSolution.get(0);
		ArrayList<Integer> CLtoRestart = stackCL.get(0);
		ArrayDeque<Integer> TLtoRestart = stackTL.get(0);
		Integer min = Integer.MAX_VALUE;
		Integer candidate = null;
		for (Integer elem : frequency.keySet()) {
			Integer freq = getFrequency(elem);
			if (min > freq && !solutionToRestart.contains(elem) && CLtoRestart.contains(elem)) {
				min = freq;
				candidate = elem;
			}
		}
		
		if (candidate != null ) {
			CL = CLtoRestart;
			TL = TLtoRestart;
			incumbentSol = solutionToRestart;
			incumbentSol.add(candidate);
			CL.remove(candidate);
			TL.add(candidate);
			stackSolution.clear();
			frequency.clear();
			stackCL.clear();
			initFrequency();
			stackSolution.push(incumbentSol);
			stackCL.push(CL);
			interactionsWithoutImprovingSolution = 0;
			//System.out.println("candidate = "+candidate);
			//System.out.println("new Solution = "+incumbentSol+" Old Soltion: "+stackSolution.lastElement());
		}
	}
	
	public static void main(String[] args) throws IOException {
		long startTime = System.currentTimeMillis();
		TS_QBF tabusearch = new TS_QBFPT_Diversification(200, 100000, "instances/qbf020",  Boolean.TRUE, 20);
		Solution<Integer> bestSol = tabusearch.solve();
		System.out.println("maxVal = " + bestSol);
		long endTime   = System.currentTimeMillis();
		long totalTime = endTime - startTime;
		System.out.println("Time = "+(double)totalTime/(double)1000+" seg");
		
	}
	
	
}
