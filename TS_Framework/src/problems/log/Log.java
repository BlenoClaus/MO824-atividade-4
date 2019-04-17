package problems.log;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

public class Log {
	
	private static final Logger logger = Logger.getLogger("Reporter"); 
	private static Log log;
	
	private Log() {
		FileHandler fh;
		try {
			fh = new FileHandler("instances/Report.log");
			fh.setFormatter(new CustomFormatter());
			getLogger().addHandler(fh);
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} 
    }
	
	public static Logger geLogger() {
		if (log == null) {
			log = new Log();
		}
		return Log.getLogger();
	}

	public static Logger getLogger() {
		return logger;
	}
	
	public static void info(String msg) {
		logger.info(msg);		
		//System.out.println(msg);
	}
	
	
	private class CustomFormatter extends Formatter {
		 
        @Override
        public String format(LogRecord record) {
            StringBuffer sb = new StringBuffer();
            sb.append(record.getMessage());
            sb.append("\n");
            return sb.toString();
        }
         
    }

}

