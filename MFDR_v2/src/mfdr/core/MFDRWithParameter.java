package mfdr.core;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import mfdr.datastructure.TimeSeries;
import mfdr.math.emd.EMD;
import mfdr.math.emd.datastructure.IMFS;

public class MFDRWithParameter {
	private static Log logger = LogFactory.getLog(MFDRWithParameter.class);
	// ****** Post processing flags ******

	// ***********************************
	// ********** EMD Parameters *********

	// IMF Decomposition
	private double[] IFparamaters = { 4, 2, 1 };
	private double zerocrossingaccuracy = 0.0001;
	private final int MAXLEVEL = 10;

	// White Noise Filter
	private WhiteNoiseCalculator wfilter;

	// Trend Filter
	private NoCCalculator NoCcalculator;

	/**
	 * The default constructor for MFDR + default Heuristic NoC Calculator.
	 */
	public MFDRWithParameter() {
		updateNullWhiteNoiseFilter();
		updateNoCCalculator(new HeuristicNoCCalculator());
	}
	
	/**
	 * The constructor for MFDR + user defined NoC Calculator.
	 * @param cal
	 */
	public MFDRWithParameter(NoCCalculator cal) {
		updateNullWhiteNoiseFilter();
		updateNoCCalculator(cal);
	}
	
	/**
	 * The constructor for MFDR-N + default Heuristic NoC Calculator.
	 * @param wn_level
	 * @param wn_threshold
	 * @param wn_NSratio
	 */
	public MFDRWithParameter(double wn_level, double wn_threshold, double wn_NSratio) {
		updateWhiteNoiseFilter(wn_level, wn_threshold,	wn_NSratio);
		updateNoCCalculator(new HeuristicNoCCalculator());
	}
	
	/**
	 * The constructor for MFDR-N + user defined NoC Calculator.
	 * @param wn_level
	 * @param wn_threshold
	 * @param wn_NSratio
	 * @param cal
	 */
	public MFDRWithParameter(double wn_level, double wn_threshold, double wn_NSratio, NoCCalculator cal) {
		updateWhiteNoiseFilter(wn_level, wn_threshold,	wn_NSratio);
		updateNoCCalculator(cal);
	}
	

	/**
	 * This function updates the parameter settings of white noise filter.
	 * 
	 * @param white_noise_level
	 * @param white_noise_threshold
	 * @param min_NSratio
	 */
	public void updateWhiteNoiseFilter(double white_noise_level,
			double white_noise_threshold, double min_NSratio) {
		wfilter = new WhiteNoiseCalculator(white_noise_level,
				white_noise_threshold, min_NSratio);
		logger.info("white-Noise Filter set to:  Level=" + white_noise_level
				+ " Threshold=" + white_noise_threshold + " Min NSratio="
				+ min_NSratio);
	}

	/**
	 * This function set white noise filter as null.
	 */
	public void updateNullWhiteNoiseFilter() {
		wfilter = null;
		logger.info("white-Noise Filter set to null");
	}
	
	public void updateNoCCalculator(NoCCalculator cal){
		this.NoCcalculator = cal;
	}

	/**
	 * This function provide a brute force solution with O(N) complexity to
	 * identify optimal NoC combination.
	 * 
	 * @param ts , NoC
	 * @return result
	 */
	public MFDRObject getResult(TimeSeries ts, int NoC) {
		MFDRObject result;
		double lowestperiod = 0;
		// Compute lowest period for MFDR-N
		if (wfilter != null) {
			EMD emd = new EMD(ts, zerocrossingaccuracy, IFparamaters[0],
					IFparamaters[1], IFparamaters[2]);
			IMFS imfs = emd.getIMFs(MAXLEVEL);
			lowestperiod = wfilter.getWhiteNoisePeriod(imfs, ts);
		}
		// Compute NoCs
		result = NoCcalculator.getMFDRNoCs(ts, NoC, lowestperiod);
		return result;
	}

	public String name(){
		if(wfilter == null){
			return "MFDR_" + NoCcalculator.name();	
		} else{
			return "MFDR-N_" + NoCcalculator.name();	
		}
		
	}

}
