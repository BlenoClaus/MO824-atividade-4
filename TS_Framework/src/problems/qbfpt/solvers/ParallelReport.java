package problems.qbfpt.solvers;

import problems.log.Log;
import solutions.Solution;

public class ParallelReport {
	
	static String[] INSTANCES = {
			"instances/qbf020",
			"instances/qbf040",
			"instances/qbf060",
			"instances/qbf080",
			"instances/qbf100",
			"instances/qbf200",
			"instances/qbf400",
		}; 
	
	static Integer[] INTERATIONS = {
			10000000,
			100000000,
			1000000000,
			1000000000,
			1000000000,
			1000000000,
			1000000000,
		}; 
	
	static Integer[] TENURE = {
			20,
			100
	};
	
	static float[] PROBABILITY = { 
	    (float) 0.25, 
	    (float) 0.50, 
	    (float) 0.75 
	 };
	
	private static String getFileName(String prefix, int i, int j) {
		String instance = INSTANCES[i].replace("instances/", "");
		String tenure = TENURE[j].toString();
		return "report_"+prefix+"-"+instance+"-"+tenure+"-"+i+"-"+j+".log";
	}
	private static String getFileName(String prefix, int i, int j, int m) {
    String instance = INSTANCES[i].replace("instances/", "");
    String tenure = TENURE[j].toString();
    return "report_"+prefix+"-"+instance+"-"+tenure+"-"+i+"-"+j+"-"+m+".log";
  }
	
	public static void main(String[] args) {
	  
	  boolean runDiversificationBest = false;
	  boolean runDiversificationFirst = false;
	  boolean runProbabilisticFirst = true;
	  boolean runProbabilisticBest = true;
	  
	  if (runDiversificationBest) {
  		for (int i = 0; i < INSTANCES.length; i++) {
  			for (int j = 0; j < TENURE.length; j++) {
  				final int z = i;
  				final int h = j;
  				new Thread() {
  					public void run() {
  						diversificationJob(z, h, Boolean.TRUE);
  					};
  				}.start();
  			}
  		}
		}
		if (runDiversificationFirst) {
  		for (int i = 0; i < INSTANCES.length; i++) {
  			for (int j = 0; j < TENURE.length; j++) {
  				final int z = i;
  				final int h = j;
  				new Thread() {
  					public void run() {
  						diversificationJob(z, h, Boolean.FALSE);
  					};
  				}.start();
  			}
  		}
		}
		
		if (runProbabilisticFirst) {
		  for (int i = 4; i < INSTANCES.length; i++) {
        for (int j = 0; j < TENURE.length; j++) {
            for (int g = 0; g < PROBABILITY.length; g++) {
              final int z = i;
              final int h = j;
              final int m = g;
              new Thread() {
                public void run() {
                  probabilisticJob(z, h, m, Boolean.FALSE);
                };
              }.start();
            }
         }
      }
		}
		
		if (runProbabilisticBest) {
      for (int i = 4; i < INSTANCES.length; i++) {
        for (int j = 0; j < TENURE.length; j++) {
            for (int g = 0; g < PROBABILITY.length; g++) {
              final int z = i;
              final int h = j;
              final int m = g;
              new Thread() {
                public void run() {
                  probabilisticJob(z, h, m, Boolean.TRUE);
                };
              }.start();
            }
         }
      }
    }
		
	}
	
	private static void diversificationJob(int z, int h, boolean best) {
		String fileName = getFileName("diversification-"+(best? "best": "first"),z,h);
		Thread.currentThread().setName(fileName);
		System.out.println("Thread: "+Thread.currentThread().getName()+ " start!");
		try {
			Log.getLogger(fileName).info("{\nInstancia: "+INSTANCES[z]);
			Log.getLogger(fileName).info("Interações: "+INTERATIONS[z]);
			Log.getLogger(fileName).info("\tTamanho Tabu: "+TENURE[h]);
			Log.getLogger(fileName).info("\tAlgoritmo: Diversification > Limite = "+1000);
			Log.getLogger(fileName).info("\t\tBest Improving: "+(best? "SIM": "NAO"));
			Solution<Integer> solution = 
					new TS_QBFPT_Diversification(TENURE[h], INTERATIONS[z], INSTANCES[z],  best, 1000).solve();
			Log.getLogger(fileName).info("\t\t\t"+solution.getReport());
			Log.getLogger(fileName).info("\n}");
			System.out.println("Thread: "+Thread.currentThread().getName()+ "running ...");
		} catch (Throwable e) {
			Log.getLogger(fileName).info("Erro "+e.getStackTrace().toString());
			e.printStackTrace();
		}
		System.out.println("Thread: "+Thread.currentThread().getName()+ "end!");
		
	}
	
	private static void probabilisticJob(int z, int h, int m, boolean best) {
    String fileName = getFileName("probabilistic-"+(best? "best": "first"),z,h,m);
    Thread.currentThread().setName(fileName);
    System.out.println("Thread: "+Thread.currentThread().getName()+ " start!");
    try {
      Log.getLogger(fileName).info("{\nInstancia: "+INSTANCES[z]);
      Log.getLogger(fileName).info("Interações: "+INTERATIONS[z]);
      Log.getLogger(fileName).info("\tTamanho Tabu: "+TENURE[h]);
      Log.getLogger(fileName).info("\tAlgoritmo: Probabilistico > Prob = "+PROBABILITY[m]);
      Log.getLogger(fileName).info("\t\tBest Improving: "+(best? "SIM": "NAO"));
      Solution<Integer> solution = 
          new TS_QBFPT_Probabilistic(TENURE[h], INTERATIONS[z], INSTANCES[z],  best, PROBABILITY[m]).solve();
      Log.getLogger(fileName).info("\t\t\t"+solution.getReport());
      Log.getLogger(fileName).info("\n}");
      System.out.println("Thread: "+Thread.currentThread().getName()+ "running ...");
    } catch (Throwable e) {
      Log.getLogger(fileName).info("Erro "+e.getStackTrace().toString());
      e.printStackTrace();
    }
    System.out.println("Thread: "+Thread.currentThread().getName()+ "end!");
    
  }

}
