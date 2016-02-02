package mfdr.core;

import mfdr.datastructure.TimeSeries;
import mfdr.dimensionality.datastructure.MFDRWaveData;
import mfdr.dimensionality.reduction.MFDR;
import mfdr.math.emd.utility.DataListCalculator;

public class BruteForceNoCCalculator implements NoCCalculator {

	public BruteForceNoCCalculator() {
		// TODO Auto-generated constructor stub
	}
	
	
	@Override
	public MFDRObject getMFDRNoCs(TimeSeries ts, int NoC, double lowestperiod) {
		int[] candidateNoCs = {0 , NoC};
		MFDR mfdr = new MFDR(candidateNoCs[0], candidateNoCs[1]);
		TimeSeries residual;
		long startTime = System.nanoTime();
		if(lowestperiod == 0){
			residual = DataListCalculator.getInstance().getDifference(ts, mfdr.getFullResolutionDR(ts));
		} else{
			residual = DataListCalculator.getInstance().getDifference(ts, mfdr.getFullResolutionDR(ts, lowestperiod));
		}
		double candidateError = residual.energyDensity();
		// Iterate through difference combination
		MFDRWaveData data, candidatedata = null;
		for(int NoC_t = 1 ; NoC_t <=NoC ; NoC_t++){
			int NoC_s = NoC - NoC_t;
			mfdr.updateParameters(NoC_t, NoC_s);
//			mfdr = new MFDR(NoC_t, NoC_s);
			if(lowestperiod == 0){
				data = mfdr.getDR(ts);
//				residual = DataListCalculator.getInstance().getDifference(ts, mfdr.getFullResolutionDR(ts));
			} else{
				data = mfdr.getDR(ts,lowestperiod);
//				residual = DataListCalculator.getInstance().getDifference(ts, mfdr.getFullResolutionDR(ts, lowestperiod));
			}
			residual = DataListCalculator.getInstance().getDifference(ts, mfdr.getFullResolutionDR(data, ts));
			double error = residual.energyDensity();
			if(error < candidateError ){
				candidateError = error;
				candidateNoCs[0] = NoC_t;
				candidateNoCs[1] = NoC_s;
				candidatedata = data;
			} 
		}
		long endTime = System.nanoTime();
		
//		mfdr.updateParameters(candidateNoCs[0], candidateNoCs[1]);
//		MFDRWaveData data = mfdr.getDR(ts);
		return new MFDRObject(candidateNoCs[0], candidateNoCs[1], lowestperiod, candidatedata, candidateError, endTime - startTime);
	}
	
	
	public MFDRObject getMFDRNoCsBAK(TimeSeries ts, int NoC, double lowestperiod) {
		int[] candidateNoCs = {0 , NoC};
		MFDR mfdr = new MFDR(candidateNoCs[0], candidateNoCs[1]);
		TimeSeries residual;
		if(lowestperiod == 0){
			residual = DataListCalculator.getInstance().getDifference(ts, mfdr.getFullResolutionDR(ts));
		} else{
			residual = DataListCalculator.getInstance().getDifference(ts, mfdr.getFullResolutionDR(ts, lowestperiod));
		}
		double candidateError = residual.energyDensity();
		long startTime = System.nanoTime();
		// Iterate through difference combination
		for(int NoC_t = 1 ; NoC_t <=NoC ; NoC_t++){
			int NoC_s = NoC - NoC_t;
			mfdr.updateParameters(NoC_t, NoC_s);
//			mfdr = new MFDR(NoC_t, NoC_s);
			if(lowestperiod == 0){
				residual = DataListCalculator.getInstance().getDifference(ts, mfdr.getFullResolutionDR(ts));
			} else{
				residual = DataListCalculator.getInstance().getDifference(ts, mfdr.getFullResolutionDR(ts, lowestperiod));
			}
			double error = residual.energyDensity();
			if(error < candidateError ){
				candidateError = error;
				candidateNoCs[0] = NoC_t;
				candidateNoCs[1] = NoC_s;
			} 
		}
		long endTime = System.nanoTime();
		mfdr.updateParameters(candidateNoCs[0], candidateNoCs[1]);
		MFDRWaveData data = mfdr.getDR(ts);
		return new MFDRObject(candidateNoCs[0], candidateNoCs[1], lowestperiod, data, candidateError, endTime - startTime);
	}

	@Override
	public String name() {
		return "BruteForce";
	}

}
