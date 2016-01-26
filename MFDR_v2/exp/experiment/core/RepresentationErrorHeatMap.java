package experiment.core;

import java.util.LinkedList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.Logger;

import experiment.utility.NoCObject;
import experiment.utility.UCRParser;
import experiment.utility.UCRParser2015;
import mfdr.core.MFDRParameterFacade;
import mfdr.core.MFDRParameters;
import mfdr.datastructure.TimeSeries;
import mfdr.dimensionality.datastructure.MFDRData;
import mfdr.dimensionality.datastructure.MFDRObject;
import mfdr.dimensionality.datastructure.MFDRWaveData;
import mfdr.dimensionality.reduction.MFDR;
import mfdr.file.FileAccessAgent;
import mfdr.math.emd.utility.DataListCalculator;
import mfdr.utility.File;
import mfdr.utility.Print;

public class RepresentationErrorHeatMap implements ExperimentCore {
	private UCRParser parser;
	private static Log logger = LogFactory
			.getLog(RepresentationErrorHeatMap.class);

	@Override
	public void run(String readaddress, String writeaddress,
			String listaddress, int NoC_Start, int NoC_Interval, int NoC_End,
			int arg) {
		// Create file access agent object
		FileAccessAgent fagent = new FileAccessAgent(
				"C:\\TEST\\MFDR\\NULL.txt", "C:\\TEST\\MFDR\\NULL.txt");
		// Create UCR parser object
		parser = new UCRParser2015(fagent);

		MFDRParameterFacade facade = new MFDRParameterFacade(3, 0.5, 6.5);

		// Retrieve name list
		LinkedList<String> filenamelist = parser.getFileNameList(fagent,
				listaddress);

		// Iterate through all data set in the given list.
		for (int i = 0; i < filenamelist.size(); i++) {
			LinkedList<TimeSeries> tsset = parser.getTimeSeriesListALL(fagent,
					readaddress, filenamelist.get(i));
			logger.info("File:" + filenamelist.get(i));
			// Iterate through all time series in the given data set.
			// for(int j = 0 ; j < tsset.size() ; j++){
			for (int j = 0; j < 1; j++) {
				ErrorMap errmap = new ErrorMap(NoC_End);
				TimeSeries ts = tsset.get(j);
				logger.info("TS:");
				Print.getInstance().printDataLinkedList(ts, ts.size());
				// Iterate through all NoC set
				for (int NoC = NoC_Start; NoC <= NoC_End; NoC += NoC_Interval) {
					MFDR mfdr;
					TimeSeries residual;
					MFDRObject[] edge = new MFDRObject[2];
					extractEdgeComposition(ts, NoC, 0, edge);
					MFDRObject result = findOptimalNoC_T(edge[0], edge[1], ts, NoC);
					
					logger.info("NoC[" + NoC + "] NoC_t:" + result.NoC_t() + " Err:" + result.error());
					
					// Iterate through all NoC combination with a given time
					// series ts
					for (int NoC_t = 0; NoC_t <= NoC; NoC_t++) {
						int NoC_s = NoC - NoC_t;
						mfdr = new MFDR(NoC_t, NoC_s);
						residual = DataListCalculator
								.getInstance()
								.getDifference(ts, mfdr.getFullResolutionDR(ts));
						// Store error into map
						errmap.storeError(NoC_t, NoC_s,	residual.energyDensity());
					}
				}

				// ************ Store Results ************
				// Upsate writing path
				fagent.updatewritingpath(writeaddress + filenamelist.get(i)
						+ "_" + j + ".csv");
				// Prepare output labels
				String outputstring = ",";
				for (int k = 0; k <= NoC_End; k++) {
					outputstring += k + ",";
				}
				// Store output labels
				fagent.writeLineToFile(outputstring);
				// Iteration
				for (int NoC_t = 0; NoC_t <= NoC_End; NoC_t++) {
					// Prepare output values
					outputstring = NoC_t + ",";
					for (int NoC_s = 0; NoC_s <= NoC_End - NoC_t; NoC_s++) {
						outputstring += errmap.getError(NoC_t, NoC_s) + ",";
					}
					// Store output values
					fagent.writeLineToFile(outputstring);
				}
			}
		}
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

//	private int findOptimalNoC_T(int NoC, TimeSeries ts) {
//		double err;
//		MFDR mfdr;
//		NoCObject left = new NoCObject(NoC, 1) , mid= new NoCObject(NoC/2, 1), right= new NoCObject(0, 1);
//		
//		
//		
//		
//		// Compute left
//		mfdr = new MFDR(left.NoC_t(), NoC - left.NoC_t());
//		err = DataListCalculator.getInstance()
//				.getDifference(ts, mfdr.getFullResolutionDR(ts)).energyDensity();
//		left = new NoCObject(NoC, err);
//		
//		// Compute mid
//		int midNoC_t = (left.NoC_t()+right.NoC_t())/2;
//		mfdr = new MFDR(midNoC_t, NoC - midNoC_t);
//		err = DataListCalculator.getInstance()
//				.getDifference(ts, mfdr.getFullResolutionDR(ts)).energyDensity();
//		mid = new NoCObject(NoC, err);
//		
//		// Compute right
//		mfdr = new MFDR(right.NoC_t(), NoC - right.NoC_t());
//		err = DataListCalculator.getInstance()
//				.getDifference(ts, mfdr.getFullResolutionDR(ts)).energyDensity();
//		left = new NoCObject(NoC, err);
//		
//		if(left.errvalue()< right.errvalue()){
//			right = mid;
//		} else {
//			left = mid;
//		}
//
//
//		return 0;
//	}
	
	/**
	 * Recursive greedy function that finds local optimal solution
	 * @param left
	 * @param right
	 * @return local optimal solution stored in a MFDRObject
	 */
	private MFDRObject findOptimalNoC_T(MFDRObject left, MFDRObject right, TimeSeries ts, int NoC) {
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
			MFDRWaveData mid_data = mfdr.getDR(ts);
			double mid_err = DataListCalculator.getInstance()
					.getDifference(ts, mfdr.getFullResolutionDR(ts)).energyDensity();
			MFDRObject mid = new MFDRObject(mid_NoC_t, NoC - mid_NoC_t, 0, mid_data, mid_err);
			// Recursive operation
			if(left.error() < right.error()){
				return findOptimalNoC_T(left, mid,ts,NoC);
			} else{
				return findOptimalNoC_T(mid, right,ts,NoC);
			}
		}
	}
	
}
