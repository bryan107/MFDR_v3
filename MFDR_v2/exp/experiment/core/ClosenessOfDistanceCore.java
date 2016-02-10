package experiment.core;

import java.util.LinkedList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import mfdr.core.BruteForceNoCCalculator;
import mfdr.core.HeuristicNoCCalculator;
import mfdr.core.MFDRWithParameter;
import mfdr.datastructure.TimeSeries;
import mfdr.dimensionality.datastructure.MFDRWaveData;
import mfdr.dimensionality.reduction.DFT;
import mfdr.dimensionality.reduction.DFT_LB;
import mfdr.dimensionality.reduction.DimensionalityReduction;
import mfdr.dimensionality.reduction.MFDR;
import mfdr.dimensionality.reduction.PAA;
import mfdr.dimensionality.reduction.PLA;
import mfdr.dimensionality.reduction.PLAA;
import mfdr.distance.Distance;
import mfdr.distance.EuclideanDistance;
import mfdr.file.FileAccessAgent;
import mfdr.file.PropertyAgent;
import experiment.utility.UCRParser;
import experiment.utility.UCRParser2015;
import flanagan.analysis.Stat;

public class ClosenessOfDistanceCore implements ExperimentCore {
	private UCRParser parser;
	private static Log logger = LogFactory
			.getLog(ClosenessOfDistanceCore.class);
	private int round = 500;
	private Distance distance = new EuclideanDistance();
	@Override
	public void run(String readaddress, String writeaddress,
			String listaddress, int NoC_Start, int NoC_Interval, int NoC_End,
			int whitenoise) {
		double wn_level = 0, wn_threshold = 0, wn_nsratio = 0;
		MFDRWithParameter mfdr_p = new MFDRWithParameter();
		/* Retrieve white noise settings. */
		if (whitenoise == 1) {
			wn_level = Double.parseDouble(PropertyAgent.getInstance()
					.getProperties("MFDR", "WhiteNoise_Level"));
			wn_threshold = Double.parseDouble(PropertyAgent.getInstance()
					.getProperties("MFDR", "WhiteNoise_Threshold"));
			wn_nsratio = Double.parseDouble(PropertyAgent.getInstance()
					.getProperties("MFDR", "WhiteNoise_Min_NSRatio"));
		}
		/* Retrieve time series files */
		FileAccessAgent fagent = new FileAccessAgent(
				"C:\\TEST\\MFDR\\NULL.txt", "C:\\TEST\\MFDR\\NULL.txt");
		parser = new UCRParser2015(fagent);
		LinkedList<String> filenamelist = parser.getFileNameList(fagent,
				listaddress);

		/* Warm up MFDR with Heuristic Solution */
		mfdr_p.updateNoCCalculator(new HeuristicNoCCalculator());
		String testlistaddress = "C:\\TEST\\MFDR\\UCR_TS_Archive_2015\\test_list.txt";
		runMFDRMethod(readaddress, writeaddress + "WarmUpDump\\", 2, 2, 3,
				mfdr_p, fagent, parser.getFileNameList(fagent,
						testlistaddress));

		/* Run MFDR with Heuristic Solution */
		mfdr_p.updateNoCCalculator(new HeuristicNoCCalculator());
		runMFDRMethod(readaddress, writeaddress, NoC_Start, NoC_Interval,
				NoC_End, mfdr_p, fagent, filenamelist);

		/* Run MFDR with BruteForce Solution */
		mfdr_p.updateNoCCalculator(new BruteForceNoCCalculator());
		runMFDRMethod(readaddress, writeaddress, NoC_Start, NoC_Interval,
				NoC_End, mfdr_p, fagent, filenamelist);

//		/* NoC setting i.e. new PAA(2) is just temporary and is replaced in the run method. */
//		/* Run PAA which can host two times more NoC then other solution for fair comparison. */
//		runComparedMethod(readaddress, writeaddress, NoC_Start *2 , NoC_Interval *2, NoC_End*2, new PAA(2), fagent, filenamelist);
//		
//		/* Run PLA */
//		runComparedMethod(readaddress, writeaddress, NoC_Start, NoC_Interval, NoC_End, new PLA(2), fagent, filenamelist);
//		
//		/* Run DFT */
//		runComparedMethod(readaddress, writeaddress, NoC_Start, NoC_Interval, NoC_End, new DFT(2), fagent, filenamelist);
//		
//		/* Run DFT_LB */
//		runComparedMethod(readaddress, writeaddress, NoC_Start, NoC_Interval, NoC_End, new DFT_LB(2), fagent, filenamelist);
	
		/* Run PLAA */
		runComparedMethod(readaddress, writeaddress, NoC_Start, NoC_Interval, NoC_End, new PLAA(2), fagent, filenamelist);
	}
	
	public void runMFDRMethod(String readaddress, String writeaddress,
			int NoC_Start, int NoC_Interval, int NoC_End,
			MFDRWithParameter mfdr_p, FileAccessAgent fagent,
			LinkedList<String> filenamelist) {
		fagent.updatewritingpath(writeaddress + "ClosenessOfDistance_"
				+ mfdr_p.name() + ".csv");
		MFDR mfdr = new MFDR(0, 0);
		
		/* Iterate through all data set in the given list. */
		for (int i = 0; i < filenamelist.size(); i++) {
			LinkedList<TimeSeries> tsset = parser.getTimeSeriesListALL(fagent,
					readaddress, filenamelist.get(i));
			logger.info("File:" + filenamelist.get(i) + ", TS Size: "
					+ tsset.size() + ", Extracting...");
			int size = tsset.get(0).size();
			/* Iterate through all NoC settings */
			for (int NoC = NoC_Start; NoC <= NoC_End; NoC += NoC_Interval) {
				/*-------------- Print Start ------------*/
				System.out.print("[NoC=" + NoC + "]: ");
				/*-------------- Print End --------------*/
				double[] err = new double[round];
				long[] time = new long[round];
				double NLB_count = 0;

				/* Iterate through all time series in the file */
				// long startTime = System.currentTimeMillis();
				int count = 0;
				
				LinkedList<MFDRWaveData> mfdrlist = new LinkedList<MFDRWaveData>();
				for (TimeSeries ts : tsset) {
					mfdrlist.add(mfdr_p.getResult(ts, NoC).data());
				}
				for (int j = 0; j < round; j++) {
					int index_1 = (int)(mfdrlist.size()*Math.random());
					int index_2 = (int)(mfdrlist.size()*Math.random());
					while(index_2 == index_1){
						index_2 = (int)(mfdrlist.size()*Math.random());
					}
					long starttime = System.nanoTime();
					MFDRWaveData m1 = mfdrlist.get(index_1);
					MFDRWaveData m2 = mfdrlist.get(index_2);
					double dist_mfdr = mfdr.getDistance(mfdrlist.get(index_1), mfdrlist.get(index_2), size, distance);
					long endtime = System.nanoTime();
					double dist_act = distance.calDistance(tsset.get(index_1), tsset.get(index_2), tsset.get(index_1));
					if(dist_act == 0){
						err[j] = 0;
					} else{
						err[j] = Math.abs(dist_act-dist_mfdr)/dist_act;
					}
					if(dist_act-dist_mfdr < 0)
						NLB_count++;
					time[j] = endtime - starttime;
					
					/* ----- Print Counter Start ------- */
					int currentcount = j * 20 / round;
					if (currentcount > count) {
						System.out.print(currentcount * 5 + "% ");
						count++;
					}
					/* ------- Print Counter End -------- */
				}
				System.out.println(100 + "% ");

				/* Accumulating results */
				RepresentationErrorResult err_all = new RepresentationErrorResult(Stat.mean(err),
						Stat.variance(err), Stat.mean(time),
						Stat.variance(time));

				/* Store result */
				String outputstring = filenamelist.get(i) + ",[" + NoC + "],M,"
						+ err_all.errMean() + ",V," + err_all.errVariance()
						+ ",T," + err_all.timeMean() + ",NLB," + NLB_count/round;
				fagent.writeLineToFile(outputstring);
				System.out.println(outputstring);
			}
		}
	}
	
	public void runComparedMethod(String readaddress, String writeaddress,
			int NoC_Start, int NoC_Interval, int NoC_End,
			DimensionalityReduction dr, FileAccessAgent fagent,
			LinkedList<String> filenamelist) {
		fagent.updatewritingpath(writeaddress + "ClosenessOfDistance_"
				+ dr.name() + ".csv");
		
		/* Iterate through all data set in the given list. */
		for (int i = 0; i < filenamelist.size(); i++) {
			LinkedList<TimeSeries> tsset = parser.getTimeSeriesListALL(fagent,
					readaddress, filenamelist.get(i));
			logger.info("File:" + filenamelist.get(i) + ", TS Size: "
					+ tsset.size() + ", Extracting...");
			/* Iterate through all NoC settings */
			for (int NoC = NoC_Start; NoC <= NoC_End; NoC += NoC_Interval) {
				dr.setNoC(NoC);
				/*-------------- Print Start ------------*/
				System.out.print("[NoC=" + NoC + "]: ");
				/*-------------- Print End --------------*/
				double[] err = new double[round];
				long[] time = new long[round];
				double NLB_count = 0;

				/* Iterate through all time series in the file */
				// long startTime = System.currentTimeMillis();
				int count = 0;
				for (int j = 0; j < round; j++) {
					int index_1 = (int)(tsset.size()*Math.random());
					int index_2 = (int)(tsset.size()*Math.random());
					while(index_2 == index_1){
						index_2 = (int)(tsset.size()*Math.random());
					}
					long starttime = System.nanoTime();
					double dist_dr = dr.getDistance(tsset.get(index_1), tsset.get(index_2), distance);
					long endtime = System.nanoTime();
					double dist_act = distance.calDistance(tsset.get(index_1), tsset.get(index_2), tsset.get(index_1));
					if(dist_act == 0){
						err[j] = 0;
					} else{
						err[j] = Math.abs(dist_act-dist_dr)/dist_act;
					}
					if(dist_act-dist_dr < 0)
						NLB_count++;
					time[j] = endtime - starttime;
					
					/* ----- Print Counter Start ------- */
					int currentcount = j * 20 / tsset.size();
					if (currentcount > count) {
						System.out.print(currentcount * 5 + "% ");
						count++;
					}
					/* ------- Print Counter End -------- */
				}
				System.out.println(100 + "% ");

				/* Accumulating results */
				RepresentationErrorResult err_all = new RepresentationErrorResult(Stat.mean(err),
						Stat.variance(err), Stat.mean(time),
						Stat.variance(time));

				/* Store result */
				String outputstring = filenamelist.get(i) + ",[" + NoC + "],M,"
						+ err_all.errMean() + ",V," + err_all.errVariance()
						+ ",T," + err_all.timeMean() + ",NLB," + NLB_count/round;
				fagent.writeLineToFile(outputstring);
				System.out.println(outputstring);
			}
		}
	}
}
