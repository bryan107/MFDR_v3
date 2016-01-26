package mfdr.core;

import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import mfdr.datastructure.TimeSeries;
import mfdr.dimensionality.datastructure.MFDRObject;
import mfdr.dimensionality.datastructure.MFDRWaveData;
import mfdr.dimensionality.reduction.DFT;
import mfdr.dimensionality.reduction.DFTForMFDR;
import mfdr.dimensionality.reduction.MFDR;
import mfdr.dimensionality.reduction.PLA;
import mfdr.math.emd.utility.DataListCalculator;

/**
 * FREQUENCY/TREND DISCRIMINATION FUNCTIONS
 * 
 * The functions are implemented with the consideration of Energy containing in
 * the K-Motifs If the energy of an IMF concentrates in certain number of motifs
 * (which repeat recursively), The given IMF is considered as a frequency
 * intense signal; otherwise trend intense.
 **/

public class MFDRNoCCalculator {
	/**
	 * Use this constructor for old K 1-motif solution
	 */
	private static Log logger = LogFactory.getLog(MFDRNoCCalculator.class);

	public MFDRNoCCalculator() {

	}
	
	/**
	 * This function uses a brute force algorithm to calculate Candidate trend NoC and Seasonal NoC.
	 * @param ts
	 * @param NoC
	 * @param lowestperiod
	 * @return
	 */
	public MFDRObject getBruteForceMFDRNoCs(TimeSeries ts, int NoC, double lowestperiod){
		int[] candidateNoCs = {0 , NoC};
		MFDR mfdr = new MFDR(candidateNoCs[0], candidateNoCs[1]);
		TimeSeries residual = DataListCalculator.getInstance().getDifference(ts, mfdr.getFullResolutionDR(ts,lowestperiod));
		double candidateError = residual.energyDensity();
		// Iterate through difference combination
		for(int NoC_t = 1 ; NoC_t <=NoC ; NoC_t++){
			int NoC_s = NoC - NoC_t;
			mfdr = new MFDR(NoC_t, NoC_s);
			residual = DataListCalculator.getInstance().getDifference(ts, mfdr.getFullResolutionDR(ts,lowestperiod));
			double error = residual.energyDensity();
			if(error < candidateError ){
				candidateError = error;
				candidateNoCs[0] = NoC_t;
				candidateNoCs[1] = NoC_s;
			} 
		}
		mfdr.updateParameters(candidateNoCs[0], candidateNoCs[1]);
		MFDRWaveData data = mfdr.getDR(ts,lowestperiod);
		return new MFDRObject(candidateNoCs[0], candidateNoCs[1], lowestperiod, data, candidateError);
	}
	
	
	/**
	 * Recursive greedy function that finds local optimal solution
	 * @param left
	 * @param right
	 * @return local optimal solution stored in a MFDRObject
	 */
	public MFDRObject getOptimalMFDRNoCs(MFDRObject left, MFDRObject right, TimeSeries ts, int NoC, double lowestperiod) {
		int mid_NoC_t = (left.NoC_t() + right.NoC_t())/2;
		// If reach the leaf of search (No mid can be retrieved)
		if(mid_NoC_t == left.NoC_t() || mid_NoC_t == right.NoC_t()){
			if(left.error() < right.error()){
				return left;
			} else{
				return right;
			}
		} else {
			// Compute mid values
			MFDR mfdr = new MFDR(mid_NoC_t, NoC - mid_NoC_t);
			MFDRWaveData mid_data = mfdr.getDR(ts,lowestperiod);
			double mid_err = DataListCalculator.getInstance()
					.getDifference(ts, mfdr.getFullResolutionDR(ts,lowestperiod)).energyDensity();
			MFDRObject mid = new MFDRObject(mid_NoC_t, NoC - mid_NoC_t, lowestperiod, mid_data, mid_err);
			// Recursive operation
			if(left.error() < right.error()){
				return getOptimalMFDRNoCs(left, mid,ts,NoC,lowestperiod);
			} else{
				return getOptimalMFDRNoCs(mid, right,ts,NoC,lowestperiod);
			}
		}
	}
}
