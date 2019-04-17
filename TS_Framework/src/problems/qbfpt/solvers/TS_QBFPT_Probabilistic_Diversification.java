package problems.qbfpt.solvers;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import problems.log.Log;
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
public class TS_QBFPT_Probabilistic_Diversification extends TS_QBF{

	private ForbiddenTriplesBuilder ftBuilder;
	private Map<Integer, Integer> frequency = new HashMap<>();
	private Stack<Solution<Integer>> stackSolution = new Stack<Solution<Integer>>();
	private Stack<ArrayList<Integer>> stackCL = new Stack<ArrayList<Integer>>();
	private Stack<ArrayDeque<Integer>> stackTL = new Stack<ArrayDeque<Integer>>();
	private int interactionsWithoutImprovingSolution = 0;
	private int limitDiversification;
	private boolean isBestImproving;
	private double probabilityParameter;

	public TS_QBFPT_Probabilistic_Diversification(Integer tenure, Integer iterations, String filename, 
			boolean isBestImproving, double probability) throws IOException {
		super(tenure, iterations, filename);
		this.ftBuilder = new ForbiddenTriplesBuilder(ObjFunction.getDomainSize());
		this.limitDiversification = (int) (iterations*0.33);
		this.probabilityParameter = probability;
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
		//updateFrequency();
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
	
//	private void updateFrequency() {
//		incumbentSol.stream().forEach(elem -> putIncrease(elem));
//	}
	
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

		ArrayList<Double> pIn = new ArrayList<Double>();
		ArrayList<Double> pOut = new ArrayList<Double>();
		minDeltaCost = Double.POSITIVE_INFINITY;
		double x;
		updateCL();
		
		// Evaluate insertions
		for (Integer candIn : CL) {
			x = rng.nextFloat();
			pIn.add(x);
			if (x < probabilityParameter) {
				putIncrease(candIn);
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
		}
		// Evaluate removals
		for (Integer candOut : incumbentSol) {
			x = rng.nextFloat();
			//System.out.println("out x " + x);
			pOut.add(x);
			if (x < probabilityParameter) {
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
		}
		// Evaluate exchanges
		EXIT:
		for (int i = 0; i < CL.size(); i++) {
			if (pIn.get(i) < probabilityParameter) {
				Integer candIn = CL.get(i);
				for (int j = 0; j < incumbentSol.size(); j++) {
					Integer candOut = incumbentSol.get(j);
					if (pOut.get(j) < probabilityParameter) {
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
		
		//updateFrequency();
		return null;
	}
	
	private void restart(int current_i) {
		incumbentSol = createEmptySol();
		CL = makeCL();
		TL = makeTL();
		List<Integer> forbiddenValues = new ArrayList<>();
		Iterator<Integer> iter = CL.iterator();
		

		double min = Collections.min(frequency.values());
		
		min = min + (Collections.max(frequency.values())-min)*0.33;
		// add to the incumbentSol the elements with freq < current_i*probability
		// respecting forbiddenValues
		while (iter.hasNext()) { 
			Integer candIn = iter.next();
			//System.out.println(" freq " + getFrequency(candIn) + " forbidden " + forbiddenValues.contains(candIn+1));
			if (getFrequency(candIn) <= min &&
					!forbiddenValues.contains(candIn+1)) {
				// try to add cand to incumbentSol
				incumbentSol.add(candIn);
				iter.remove();
				TL.add(candIn);
				putIncrease(candIn);
				
				for (int i = 0; i < this.incumbentSol.size()-1; i++) {
					forbiddenValues.addAll(ftBuilder.getForbiddenValues(this.incumbentSol.get(i)+1, candIn+1));
				}
			}
		}
		ObjFunction.evaluate(incumbentSol);
		
		//System.out.println(" iter " + current_i + " cur_i*p" + current_i*probabilityParameter + " new sol " + incumbentSol);
		System.out.println(incumbentSol);
	}
	
	/**
	 * The TS mainframe. It consists of a constructive heuristic followed by
	 * a loop, in which each iteration a neighborhood move is performed on
	 * the current solution. The best solution is returned as result.
	 * 
	 * @return The best feasible solution obtained throughout all iterations.
	 */
	public Solution<Integer> solve() {
		long startTime = System.currentTimeMillis();
		double thirtyMinutes = 30 * 60;
		double totalTempo = 0.0;

		bestSol = createEmptySol();
		constructiveHeuristic();
		TL = makeTL();
		int i, i_best = 0, i_restart = 0; 
		for (i = 0; i < iterations && totalTempo < thirtyMinutes; i++) {
			if (i - i_best >= iterations*0.05 && i - i_restart >= limitDiversification) {
				i_restart = i;
				restart(i);
			}
			neighborhoodMove();
			if (bestSol.cost > incumbentSol.cost) {
				i_best = i;
				bestSol = new Solution<Integer>(incumbentSol);
				if (verbose)
					Log.geLogger().info("(Iter. " + i + ") BestSol = " + bestSol);
			}
			long endTime   = System.currentTimeMillis();
			long totalTime = endTime - startTime;
			totalTempo = (double)totalTime/(double)1000;
		}
		Log.info("Tempo: "+totalTempo+" s");
		Log.info("Interações: "+i);

		return bestSol;
	}
	
	public static void main(String[] args) throws IOException {
		long startTime = System.currentTimeMillis();
		TS_QBF tabusearch = new TS_QBFPT_Probabilistic_Diversification(200, 10000000, "instances/qbf020",  Boolean.TRUE, 0.33);
		Solution<Integer> bestSol = tabusearch.solve();
		System.out.println("maxVal = " + bestSol);
		long endTime   = System.currentTimeMillis();
		long totalTime = endTime - startTime;
		System.out.println("Time = "+(double)totalTime/(double)1000+" seg");
		
	}
	
	
}
