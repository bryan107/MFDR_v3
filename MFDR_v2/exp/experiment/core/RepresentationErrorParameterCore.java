package experiment.core;

import java.util.LinkedList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import experiment.utility.UCRParser;
import experiment.utility.UCRParser2015;
import flanagan.analysis.Stat;
import mfdr.core.BruteForceNoCCalculator;
import mfdr.core.HeuristicNoCCalculator;
import mfdr.core.MFDRObject;
import mfdr.core.MFDRWithParameter;
import mfdr.datastructure.TimeSeries;
import mfdr.dimensionality.reduction.DFT;
import mfdr.dimensionality.reduction.DimensionalityReduction;
import mfdr.dimensionality.reduction.PAA;
import mfdr.dimensionality.reduction.PLA;
import mfdr.file.FileAccessAgent;
import mfdr.file.PropertyAgent;
import mfdr.math.emd.utility.DataListCalculator;

public class RepresentationErrorParameterCore implements ExperimentCore {
	private UCRParser parser;
	private static Log logger = LogFactory
			.getLog(RepresentationErrorParameterCore.class);

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
		runMFDRMethod(readaddress, writeaddress + "WarmUpDump\\", 2, 2, 3,
				mfdr_p, fagent, filenamelist);

		/* Run MFDR with Heuristic Solution */
		mfdr_p.updateNoCCalculator(new HeuristicNoCCalculator());
		runMFDRMethod(readaddress, writeaddress, NoC_Start, NoC_Interval,
				NoC_End, mfdr_p, fagent, filenamelist);

		/* Run MFDR with BruteForce Solution */
		mfdr_p.updateNoCCalculator(new BruteForceNoCCalculator());
		runMFDRMethod(readaddress, writeaddress, NoC_Start, NoC_Interval,
				NoC_End, mfdr_p, fagent, filenamelist);

		/* NoC setting i.e. new PAA(2) is just temporary and is replaced in the run method. */
		/* Run PAA which can host two times more NoC then other solution for fair comparison. */
		runComparedMethod(readaddress, writeaddress, NoC_Start *2 , NoC_Interval *2, NoC_End*2, new PAA(2), fagent, filenamelist);
		
		/* Run PLA */
		runComparedMethod(readaddress, writeaddress, NoC_Start, NoC_Interval, NoC_End, new PLA(2), fagent, filenamelist);
		
		/* Run DFT */
		runComparedMethod(readaddress, writeaddress, NoC_Start, NoC_Interval, NoC_End, new DFT(2), fagent, filenamelist);
		
	}

	public void runMFDRMethod(String readaddress, String writeaddress,
			int NoC_Start, int NoC_Interval, int NoC_End,
			MFDRWithParameter mfdr_p, FileAccessAgent fagent,
			LinkedList<String> filenamelist) {
		fagent.updatewritingpath(writeaddress + "RepresentationError_"
				+ mfdr_p.name() + ".csv");
		/* Iterate through all data set in the given list. */
		for (int i = 0; i < filenamelist.size(); i++) {
			LinkedList<TimeSeries> tsset = parser.getTimeSeriesListALL(fagent,
					readaddress, filenamelist.get(i));
			logger.info("File:" + filenamelist.get(i) + ", TS Size: "
					+ tsset.size() + ", Extracting...");
			/* Iterate through all NoC settings */
			for (int NoC = NoC_Start; NoC <= NoC_End; NoC += NoC_Interval) {
				/*-------------- Print Start ------------*/
				System.out.print("[NoC=" + NoC + "]: ");
				/*-------------- Print End --------------*/
				double[] err = new double[tsset.size()];
				long[] time = new long[tsset.size()];
				RepresentationErrorResult err_all;

				/* Iterate through all time series in the file */
				// long startTime = System.currentTimeMillis();
				int count = 0;
				for (int j = 0; j < tsset.size(); j++) {

					MFDRObject result = mfdr_p.getResult(tsset.get(j), NoC);
					err[j] = result.error();
					time[j] = result.nanoTime();
					/* ----- Print Counter Start ------- */
					int currentcount = j * 20 / tsset.size();
					if (currentcount > count) {
						System.out.print(currentcount * 5 + "% ");
						count++;
					}
					/* ------- Print Counter End -------- */
				}
				System.out.println(100 + "% ");
				// long endTime = System.currentTimeMillis();

				/* Accumulating results */
				err_all = new RepresentationErrorResult(Stat.mean(err),
						Stat.variance(err), Stat.mean(time),
						Stat.variance(time));

				/* Store result */
				String outputstring = filenamelist.get(i) + ",[" + NoC + "],M,"
						+ err_all.errMean() + ",V," + err_all.errVariance()
						+ ",T," + err_all.timeMean();
				fagent.writeLineToFile(outputstring);
				System.out.println(outputstring);
			}
		}
	}

	public void runComparedMethod(String readaddress, String writeaddress,
			int NoC_Start, int NoC_Interval, int NoC_End,
			DimensionalityReduction DR, FileAccessAgent fagent,
			LinkedList<String> filenamelist) {
		fagent.updatewritingpath(writeaddress + "RepresentationError_"
				+ DR.name() + ".csv");
		/* Iterate through all data set in the given list. */
		for (int i = 0; i < filenamelist.size(); i++) {
			LinkedList<TimeSeries> tsset = parser.getTimeSeriesListALL(fagent,
					readaddress, filenamelist.get(i));
			logger.info("File:" + filenamelist.get(i) + ", TS Size: "
					+ tsset.size() + ", Extracting...");
			/* Iterate through all NoC settings */
			for (int NoC = NoC_Start; NoC <= NoC_End; NoC += NoC_Interval) {
				/*-------------- Print Start ------------*/
				System.out.print("[NoC=" + NoC + "]: ");
				/*-------------- Print End --------------*/
				double[] err = new double[tsset.size()];
				long[] time = new long[tsset.size()];

				/* Iterate through all time series in the file */
				// long startTime = System.currentTimeMillis();
				int count = 0;
				for (int j = 0; j < tsset.size(); j++) {
					long startTime = System.nanoTime();
					DR.getDR(tsset.get(j), NoC);
					long endTime = System.nanoTime();
					err[j] = DataListCalculator.getInstance().getDifference(
							DR.getFullResolutionDR(tsset.get(j), NoC),
							tsset.get(j)).energyDensity();
					time[j] = endTime - startTime;

					/* ----- Print Counter Start ------- */
					int currentcount = j * 20 / tsset.size();
					if (currentcount > count) {
						System.out.print(currentcount * 5 + "% ");
						count++;
					}
					/* ------- Print Counter End -------- */
				}
				System.out.println(100 + "% ");
				// long endTime = System.currentTimeMillis();

				/* Accumulating results */
				RepresentationErrorResult err_all = new RepresentationErrorResult(
						Stat.mean(err), Stat.variance(err), Stat.mean(time),
						Stat.variance(time));

				/* Store result */
				String outputstring = filenamelist.get(i) + ",[" + NoC + "],M,"
						+ err_all.errMean() + ",V," + err_all.errVariance()
						+ ",T," + err_all.timeMean();
				fagent.writeLineToFile(outputstring);
				System.out.println(outputstring);
			}
		}
	}

}
