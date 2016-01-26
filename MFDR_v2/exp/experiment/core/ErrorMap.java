package experiment.core;

import java.util.HashMap;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ErrorMap {
	private static Log logger = LogFactory.getLog(ErrorMap.class);
	private Map<String, Double> errmap = new HashMap<String, Double>();
	private int maxNoCsize;

	public ErrorMap(int maxNoCsize) {
		updateNoCSize(maxNoCsize);
	}

	public void updateNoCSize(int maxNoCsize) {
		this.maxNoCsize = maxNoCsize;
	}

	public int maxNoCSize() {
		return maxNoCsize;
	}

	/**
	 * Store err of NoC_t and NoC_s combination.
	 * @param NoC_t
	 * @param NoC_s
	 * @param err
	 */
	public void storeError(int NoC_t, int NoC_s, double err) {
		errmap.put(NoC_t+","+NoC_s, err);
	}
	
	public double getError(int NoC_t, int NoC_s){
		try {
			return errmap.get(NoC_t+","+NoC_s);
		} catch (Exception e) {
			logger.debug("NoC too big or empty entry");
			return 0;
		}
	}

}
