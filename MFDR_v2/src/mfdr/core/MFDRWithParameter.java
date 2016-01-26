package mfdr.core;

import java.util.LinkedList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import mfdr.datastructure.TimeSeries;
import mfdr.dimensionality.datastructure.MFDRObject;
import mfdr.dimensionality.datastructure.MFDRWaveData;
import mfdr.dimensionality.reduction.MFDR;
import mfdr.dimensionality.reduction.MFDRWave;
import mfdr.distance.Distance;
import mfdr.learning.LinearLearning;
import mfdr.learning.LR4DLearning;
import mfdr.learning.LinearLearningResults;
import mfdr.learning.VarienceLearning;
import mfdr.learning.datastructure.TrainingSet;
import mfdr.math.emd.EMD;
import mfdr.math.emd.datastructure.IMFS;
import mfdr.math.emd.utility.DataListCalculator;

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
	private WhiteNoiseFilter wfilter;

	// Trend Filter
	private MFDRNoCCalculator NoCcalculator;

	/**
	 * This constructor provides the old function with k 1-motif solution
	 * @param white_noise_level
	 * @param white_noise_threshold
	 * @param min_NSratio
	 * @param FTratio
	 * @param motif_k
	 * @param motif_threshold
	 */
	public MFDRWithParameter(double white_noise_level,
			double white_noise_threshold, double min_NSratio) {
		// this.mfdr = new MFDR();
		updateWhiteNoiseFilter(white_noise_level, white_noise_threshold,
				min_NSratio);
		this.NoCcalculator = new MFDRNoCCalculator();
	}
	
	/**
	 * This is the new solution with only dist input
	 * @param white_noise_level
	 * @param white_noise_threshold
	 * @param min_NSratio
	 * @param dist
	 */
	public MFDRWithParameter(double white_noise_level,
			double white_noise_threshold, double min_NSratio, Distance dist) {
		// this.mfdr = new MFDR();
		updateWhiteNoiseFilter(white_noise_level, white_noise_threshold,
				min_NSratio);
	}

	
	public void updateWhiteNoiseFilter(double white_noise_level,
			double white_noise_threshold, double min_NSratio) {
		wfilter = new WhiteNoiseFilter(white_noise_level,
				white_noise_threshold, min_NSratio);
	}

	/**
	 * This function learns the window sizes from a time series
	 * the condition use_white_noise_filter defines whether to filter out white noise components.
	 * @param ts, NoC, use_white_noise_filter
	 * @return
	 */
	public MFDRObject getBruteForceResult(TimeSeries ts, int NoC, boolean use_white_noise_filter) {
		MFDRObject result;
		// STEP 2: AYALYZE IMFs
		double lowestperiod = 0;
		if(use_white_noise_filter){
			// STEP 1 : EMD
			// EMD service object
			EMD emd = new EMD(ts, zerocrossingaccuracy, IFparamaters[0],
					IFparamaters[1], IFparamaters[2]);
			// Calculate IMF with EMD
			IMFS imfs = emd.getIMFs(MAXLEVEL);
			lowestperiod = wfilter.getWhiteNoisePeriod(imfs, ts);
			result = NoCcalculator.getBruteForceMFDRNoCs(ts, NoC, lowestperiod);
		} else{
			result = NoCcalculator.getBruteForceMFDRNoCs(ts, NoC, lowestperiod);
		}

		return result;
	}
	
	
	public MFDRObject getResult(TimeSeries ts, int NoC, boolean use_white_noise_filter, boolean use_booster) {
		MFDRObject result;
		// STEP 2: AYALYZE IMFs
		double lowestperiod = 0;
		if(use_white_noise_filter){
			// STEP 1 : EMD
			// EMD service object
			EMD emd = new EMD(ts, zerocrossingaccuracy, IFparamaters[0],
					IFparamaters[1], IFparamaters[2]);
			// Calculate IMF with EMD
			IMFS imfs = emd.getIMFs(MAXLEVEL);
			lowestperiod = wfilter.getWhiteNoisePeriod(imfs, ts);
			if(use_booster){
				MFDRObject[] edge = new MFDRObject[2];
				extractEdgeComposition(ts, NoC, lowestperiod, edge);
				result = NoCcalculator.getOptimalMFDRNoCs(edge[0], edge[1], ts, NoC, lowestperiod);
			} else{
				result = NoCcalculator.getBruteForceMFDRNoCs(ts, NoC, lowestperiod);
			}
			
		} else{ // No white noise filter
			if(use_booster){
				MFDRObject[] edge = new MFDRObject[2];
				extractEdgeComposition(ts, NoC, lowestperiod, edge);
				result = NoCcalculator.getOptimalMFDRNoCs(edge[0], edge[1], ts, NoC, lowestperiod);
			} else{
				result = NoCcalculator.getBruteForceMFDRNoCs(ts, NoC, lowestperiod);
			}
		}
		return result;
	}
	

	private void extractEdgeComposition(TimeSeries ts, int NoC, double lowestperiod, MFDRObject[] edge) {
		MFDR mfdr;
		// Compute left
		mfdr = new MFDR(0, NoC);
		MFDRWaveData data = mfdr.getDR(ts);
		double err = DataListCalculator.getInstance()
				.getDifference(ts, mfdr.getFullResolutionDR(ts)).energyDensity();
		edge[0] = new MFDRObject(0, NoC, lowestperiod, data, err);
		
		//Compute right
		mfdr = new MFDR(NoC, 0);
		data = mfdr.getDR(ts);
		err = DataListCalculator.getInstance()
				.getDifference(ts, mfdr.getFullResolutionDR(ts)).energyDensity();
		edge[1] = new MFDRObject(NoC, 0, lowestperiod, data, err);
	}
	
	/*
	 * This function trains with reduced distance (should be more accurate) It
	 * provides higher lower bound results and make for sense in terms of
	 * learning. While inputs are at the same form as when use.
	 * 
	 * **** IT DOES NOT WORK......IT VIOLATES the TRIANGLE INEQUALITY
	 */

	public LearningResults learnParameters(LinkedList<TimeSeries> ts,
			MFDRWave mfdr, double tolerancevarience, Distance d) {

		LinearLearning alearn = new LR4DLearning(); // STEP 1: Setup training
													// set
		VarienceLearning vlearn = new VarienceLearning();
		
		LinkedList<TrainingSet> trainingset = alearn.getTrainingSet(ts, mfdr, d);

		// STEP 2: Train Alearn before use.
		LinearLearningResults weights = alearn.trainingParameters(trainingset);
		// STEP 3: Vlearn is trained as soon as it initiated.
		double guaranteed_cmopensation = vlearn.getGuaranteedCompensation(
				trainingset, weights, tolerancevarience);
		// STEP 4: Set training resutls
		return new LearningResults(weights, guaranteed_cmopensation);
	}
}
