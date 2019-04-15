package problems.qbfpt.solvers;

import problems.log.Log;

public class Report {
	
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
			1000000,
			1000000,
			10000000,
			10000000,
			100000000,
			100000000,
			1000000000,
		}; 
	
	static Integer[] TENURE = {
			20,
			100
	};
	
	
	public static void main(String[] args) {
		
		for (int i = 0; i < INSTANCES.length; i++) {
			Log.geLogger().info("Instancia: "+INSTANCES[i]);
			Log.geLogger().info("Interações: "+INTERATIONS[i]);
			for (int j = 0; j < TENURE.length; j++) {
				Log.geLogger().info("Tamanho Tabu: "+TENURE[j]);
				try {
					Log.geLogger().info("Algoritmo: Padrão");
					Log.geLogger().info("Best Improving: SIM");
					new TS_QBFPT(TENURE[j], INTERATIONS[i], INSTANCES[i], Boolean.TRUE).solve();
					Log.geLogger().info("Best Improving: NÂO");
					new TS_QBFPT(TENURE[j], INTERATIONS[i], INSTANCES[i], Boolean.FALSE).solve();
					Log.geLogger().info("Algoritmo: Diversification > Limite = "+1000);
					Log.geLogger().info("Best Improving: SIM");
					new TS_QBFPT_Diversification(TENURE[j], INTERATIONS[i], INSTANCES[i],  Boolean.TRUE, 1000).solve();
					Log.geLogger().info("Best Improving: NÂO");
					new TS_QBFPT_Diversification(TENURE[j], INTERATIONS[i], INSTANCES[i],  Boolean.TRUE, 1000).solve();
				} catch (Throwable e) {
					Log.geLogger().info("Erro");
				}
				
			}
			
		}
		
	}
}
