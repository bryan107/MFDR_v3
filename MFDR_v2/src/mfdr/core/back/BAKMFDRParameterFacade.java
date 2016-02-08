//package mfdr.core.back;
//
//import java.nio.channels.NoConnectionPendingException;
//import java.util.Iterator;
//import java.util.LinkedList;
//
//import org.apache.commons.logging.Log;
//import org.apache.commons.logging.LogFactory;
//
//import flanagan.analysis.Stat;
//import flanagan.control.LowPassPassive;
//import mfdr.core.MFDRObject;
//import mfdr.core.WhiteNoiseCalculator;
//import mfdr.datastructure.MFDRDistanceDetails;
//import mfdr.datastructure.TimeSeries;
//import mfdr.dimensionality.datastructure.MFDRWaveData;
//import mfdr.dimensionality.reduction.MFDR;
//import mfdr.dimensionality.reduction.MFDRWave;
//import mfdr.distance.Distance;
//import mfdr.learning.LinearLearning;
//import mfdr.learning.LR4DLearning;
//import mfdr.learning.LinearLearningResults;
//import mfdr.learning.VarienceLearning;
//import mfdr.learning.datastructure.TrainingSet;
//import mfdr.math.emd.EMD;
//import mfdr.math.emd.datastructure.IMFS;
//import mfdr.math.emd.utility.DataListCalculator;
//
//public class BAKMFDRParameterFacade {
//	private static Log logger = LogFactory.getLog(BAKMFDRParameterFacade.class);
//	// private MFDR mfdr;
//	// ****** Post processing flags ******
//	// private boolean windowsizetrain = false;
//	// private boolean parametertrain = false;
//
//	// ***********************************
//	// ********** EMD Parameters *********
//
//	// IMF Decomposition
//	private double[] IFparamaters = { 4, 2, 1 };
//	private double zerocrossingaccuracy = 0.0001;
//	private final int MAXLEVEL = 10;
//
//	// White Noise Filter
//	private WhiteNoiseCalculator wfilter;
//
//	// Trend Filter
//	private BAKNoCCalculator NoCcalculator;
//
//	// ************************************
//	// ********* Learning Objects *********
//	// private WindowSize windowsize;
//	// private AngleLearning alearn;
//	// private VarienceLearning vlearn;
//
//	// ************************************
//
//	/**
//	 * This constructor provides the old function with k 1-motif solution
//	 * @param white_noise_level
//	 * @param white_noise_threshold
//	 * @param min_NSratio
//	 * @param FTratio
//	 * @param motif_k
//	 * @param motif_threshold
//	 */
//	public BAKMFDRParameterFacade(double white_noise_level,
//			double white_noise_threshold, double min_NSratio) {
//		// this.mfdr = new MFDR();
//		updateWhiteNoiseFilter(white_noise_level, white_noise_threshold,
//				min_NSratio);
//		this.NoCcalculator = new BAKNoCCalculator();
//	}
//	
//	/**
//	 * This is the new solution with only dist input
//	 * @param white_noise_level
//	 * @param white_noise_threshold
//	 * @param min_NSratio
//	 * @param dist
//	 */
//	public BAKMFDRParameterFacade(double white_noise_level,
//			double white_noise_threshold, double min_NSratio, Distance dist) {
//		// this.mfdr = new MFDR();
//		updateWhiteNoiseFilter(white_noise_level, white_noise_threshold,
//				min_NSratio);
//	}
//
//	
//	public void updateWhiteNoiseFilter(double white_noise_level,
//			double white_noise_threshold, double min_NSratio) {
//		wfilter = new WhiteNoiseCalculator(white_noise_level,
//				white_noise_threshold, min_NSratio);
//	}
//
//	/**
//	 * Learn NoC Parameters from input TimeSeries<LinkedList>
//	 * 
//	 * @param ts
//	 * @return WindowSize
//	 */
//	public MFDRParameters learnMFDRParameters(LinkedList<TimeSeries> ts, int NoC, boolean use_IMF_tfilter) {
//		int[] NoC_t_array = new int[ts.size()];
//		int[] NoC_s_array = new int[ts.size()];
//		double[] lowestperiod_array = new double[ts.size()];
//		MFDRParameters parameters;
//		// Learn Window sizes of with the training data set
//		for (int i = 0; i < ts.size(); i++) {
//			parameters = learnMFDRParameters(ts.get(i), NoC, use_IMF_tfilter);
//			NoC_t_array[i] = parameters.trendNoC();
//			NoC_s_array[i] = parameters.seasonalNoC();
//			lowestperiod_array[i] = parameters.lowestPeriod();
//		}
//		// Take the medians as final results
//		int NoC_t = (int) Stat.median(NoC_t_array);
//		int NoC_s = (int) Stat.median(NoC_s_array);
//		double lowestperiod = Stat.median(lowestperiod_array);
//		// Adjust window sizes to fit the sampling rate of input time series.
//		lowestperiod = (int) (lowestperiod / ts.peek().timeInterval())
//				* ts.peek().timeInterval();
//		return new MFDRParameters(NoC_t, NoC_s, lowestperiod);
//	}
//
//	/**
//	 * Learn NoC Parameters from input TimeSeries <Array>
//	 * 
//	 * @param ts
//	 * @return
//	 */
//	public MFDRParameters learnMFDRParameters(TimeSeries[] ts, int NoC, boolean use_IMF_tfilter) {
//		int[] NoC_t_array = new int[ts.length];
//		int[] NoC_s_array = new int[ts.length];
//		double[] lowestperiod_array = new double[ts.length];
//		MFDRParameters parameters;
//		// Learn Window sizes of with the training data set
//		for (int i = 0; i < ts.length; i++) {
//			parameters = learnMFDRParameters(ts[i], NoC, use_IMF_tfilter);
//			NoC_t_array[i] = parameters.trendNoC();
//			NoC_s_array[i] = parameters.seasonalNoC();
//			lowestperiod_array[i] = parameters.lowestPeriod();
//		}
//		// Take the medians as final results
//		int NoC_t = (int) Stat.median(NoC_t_array);
//		int NoC_s = (int) Stat.median(NoC_s_array);
//		double lowestperiod = Stat.median(lowestperiod_array);
//		// Adjust window sizes to fit the sampling rate of input time series.
//		lowestperiod = (int) (lowestperiod / ts[0].timeInterval())
//				* ts[0].timeInterval();
//		return new MFDRParameters(NoC_t, NoC_s, lowestperiod);
//	}
//
//	/**
//	 * This function learns the window sizes from a time series
//	 * the condition use_white_noise_filter defines whether to filter out white noise components.
//	 * @param ts, NoC, use_white_noise_filter
//	 * @return
//	 */
//	public MFDRParameters learnMFDRParameters(TimeSeries ts, int NoC, boolean use_white_noise_filter) {
//		MFDRObject result;
//		// STEP 2: AYALYZE IMFs
//		double lowestperiod = 0;
//		if(use_white_noise_filter){
//			// STEP 1 : EMD
//			// EMD service object
//			EMD emd = new EMD(ts, zerocrossingaccuracy, IFparamaters[0],
//					IFparamaters[1], IFparamaters[2]);
//			// Calculate IMF with EMD
//			IMFS imfs = emd.getIMFs(MAXLEVEL);
//			lowestperiod = wfilter.getWhiteNoisePeriod(imfs, ts);
//			result = NoCcalculator.getBruteForceMFDRNoCs(ts, NoC, lowestperiod);
//		} else{
//			result = NoCcalculator.getBruteForceMFDRNoCs(ts, NoC, lowestperiod);
//		}
//		int NoC_t = result.NoC_t();
//		int NoC_s = NoC - NoC_t;
//		// STEP 3: Set training result;
//		return new MFDRParameters(NoC_t, NoC_s, lowestperiod);
//	}
//	
//	
//	public MFDRParameters learnMFDRParametersWithBooster(TimeSeries ts, int NoC, boolean use_white_noise_filter, boolean use_booster) {
//		MFDRObject result;
//		// STEP 2: AYALYZE IMFs
//		double lowestperiod = 0;
//		if(use_white_noise_filter){
//			// STEP 1 : EMD
//			// EMD service object
//			EMD emd = new EMD(ts, zerocrossingaccuracy, IFparamaters[0],
//					IFparamaters[1], IFparamaters[2]);
//			// Calculate IMF with EMD
//			IMFS imfs = emd.getIMFs(MAXLEVEL);
//			lowestperiod = wfilter.getWhiteNoisePeriod(imfs, ts);
//			if(use_booster){
//				MFDRObject[] edge = new MFDRObject[2];
//				extractEdgeComposition(ts, NoC, lowestperiod, edge);
//				result = NoCcalculator.getOptimalMFDRNoCs(edge[0], edge[1], ts, NoC, lowestperiod);
//			} else{
//				result = NoCcalculator.getBruteForceMFDRNoCs(ts, NoC, lowestperiod);
//			}
//			
//		} else{ // No white noise filter
//			if(use_booster){
//				MFDRObject[] edge = new MFDRObject[2];
//				extractEdgeComposition(ts, NoC, lowestperiod, edge);
//				result = NoCcalculator.getOptimalMFDRNoCs(edge[0], edge[1], ts, NoC, lowestperiod);
//			} else{
//				result = NoCcalculator.getBruteForceMFDRNoCs(ts, NoC, lowestperiod);
//			}
//		}
//		int NoC_t = result.NoC_t();
//		int NoC_s = NoC - NoC_t;
//		// STEP 3: Set training result;
//		return new MFDRParameters(NoC_t, NoC_s, lowestperiod);
//	}
//	
//
//	private void extractEdgeComposition(TimeSeries ts, int NoC, double lowestperiod, MFDRObject[] edge) {
//		MFDR mfdr;
//		// Compute left
//		mfdr = new MFDR(0, NoC);
//		MFDRWaveData data = mfdr.getDR(ts);
//		double err = DataListCalculator.getInstance()
//				.getDifference(ts, mfdr.getFullResolutionDR(ts)).energyDensity();
//		edge[0] = new MFDRObject(0, NoC, lowestperiod, data, err,0);
//		
//		//Compute right
//		mfdr = new MFDR(NoC, 0);
//		data = mfdr.getDR(ts);
//		err = DataListCalculator.getInstance()
//				.getDifference(ts, mfdr.getFullResolutionDR(ts)).energyDensity();
//		edge[1] = new MFDRObject(NoC, 0, lowestperiod, data, err,0);
//	}
//	
//	/*
//	 * This function trains with reduced distance (should be more accurate) It
//	 * provides higher lower bound results and make for sense in terms of
//	 * learning. While inputs are at the same form as when use.
//	 * 
//	 * **** IT DOES NOT WORK......IT VIOLATES the TRIANGLE INEQUALITY
//	 */
//
//	public LearningResults learnParameters(LinkedList<TimeSeries> ts,
//			MFDRWave mfdr, double tolerancevarience, Distance d) {
//
//		LinearLearning alearn = new LR4DLearning(); // STEP 1: Setup training
//													// set
//		VarienceLearning vlearn = new VarienceLearning();
//		
//		LinkedList<TrainingSet> trainingset = alearn.getTrainingSet(ts, mfdr, d);
//
//		// STEP 2: Train Alearn before use.
//		LinearLearningResults weights = alearn.trainingParameters(trainingset);
//		// STEP 3: Vlearn is trained as soon as it initiated.
//		double guaranteed_cmopensation = vlearn.getGuaranteedCompensation(
//				trainingset, weights, tolerancevarience);
//		// STEP 4: Set training resutls
//		return new LearningResults(weights, guaranteed_cmopensation);
//	}
//}
