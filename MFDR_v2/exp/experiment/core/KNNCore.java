package experiment.core;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import mfdr.core.BruteForceNoCCalculator;
import mfdr.core.HeuristicNoCCalculator;
import mfdr.core.MFDRWithParameter;
import mfdr.dimensionality.datastructure.DFTData;
import mfdr.dimensionality.datastructure.MFDRWaveData;
import mfdr.dimensionality.datastructure.PAAData;
import mfdr.dimensionality.datastructure.PLAAData;
import mfdr.dimensionality.datastructure.PLAData;
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
import experiment.utility.UCRData;
import experiment.utility.UCRParser2015;

public class KNNCore implements ExperimentCore {

	private static Log logger = LogFactory.getLog(KNNCore.class);
	private UCRParser2015 parser;
	private int count = 0;
	@Override
	public void run(String readaddress, String writeaddress,
			String listaddress, int NoC_Start, int NoC_Interval, int NoC_End,
			int K) {
		FileAccessAgent fagent = new FileAccessAgent(
				"C:\\TEST\\MFDR\\NULL.txt", "C:\\TEST\\MFDR\\NULL.txt");
		parser = new UCRParser2015(fagent);
		LinkedList<String> filenamelist = parser.getFileNameList(fagent,
				listaddress);
		MFDRWithParameter mfdr_p = new MFDRWithParameter();
		
//		mfdr_p.updateNoCCalculator(new HeuristicNoCCalculator());
		runMFDRMethod(readaddress, writeaddress, NoC_Start, NoC_Interval, NoC_End, mfdr_p, fagent, filenamelist, K);
//		
//		mfdr_p.updateNoCCalculator(new HeuristicNoCCalculator());
		runMFDRMethod(readaddress, writeaddress, NoC_Start, NoC_Interval, NoC_End, mfdr_p, fagent, filenamelist, K);
//		
//		mfdr_p.updateNoCCalculator(new BruteForceNoCCalculator());
		runMFDRMethod(readaddress, writeaddress, NoC_Start, NoC_Interval, NoC_End, mfdr_p, fagent, filenamelist, K);
		
		runDFTMethod(readaddress, writeaddress, NoC_Start, NoC_Interval, NoC_End, new DFT(2), fagent, filenamelist, K);
		
		runDFTMethod(readaddress, writeaddress, NoC_Start, NoC_Interval, NoC_End, new DFT_LB(2), fagent, filenamelist, K);
		
		runPLAMethod(readaddress, writeaddress, NoC_Start, NoC_Interval, NoC_End, new PLA(2), fagent, filenamelist, K);
		
		runPLAAMethod(readaddress, writeaddress, NoC_Start, NoC_Interval, NoC_End, new PLAA(2), fagent, filenamelist, K);
		
		/* Run PAA which can host two times more NoC then other solution for fair comparison. */
		runPAAMethod(readaddress, writeaddress, NoC_Start*2, NoC_Interval*2, NoC_End*2, new PAA(2), fagent, filenamelist, K);
	}

	public void runMFDRMethod(String readaddress, String writeaddress,
			int NoC_Start, int NoC_Interval, int NoC_End,
			MFDRWithParameter mfdr_p, FileAccessAgent fagent,
			LinkedList<String> filenamelist, int K) {
		fagent.updatewritingpath(writeaddress + "KNN_" + mfdr_p.name() + ".csv");
		MFDR mfdr = new MFDR(0, 0);
		Distance distance = new EuclideanDistance();
		/* Iterate through all data set in the given list. */
		for (int i = 0; i < filenamelist.size(); i++) {
			LinkedList<UCRData> trainlist = parser.getUCRDataListTrain(
					readaddress, filenamelist.get(i));
			LinkedList<UCRData> testlist = parser.getUCRDataListTest(
					readaddress, filenamelist.get(i));

			logger.info("Training File:" + filenamelist.get(i) + ", TS Size: "
					+ trainlist.size() + ", Extracting...");
			logger.info("Testing File:" + filenamelist.get(i) + ", TS Size: "
					+ testlist.size() + ", Extracting...");
			int size = trainlist.get(0).timeSeries().size();
			/* Iterate through all NoC settings */
			for (int NoC = NoC_Start; NoC <= NoC_End; NoC += NoC_Interval) {
				/*-------------- Print Start ------------*/
				System.out.print("[NoC=" + NoC + "]: ");
				/*-------------- Print End --------------*/
				count = 0;
				/* Prepare Training list */
				LinkedList<MFDRWaveData> train_dr_list = new LinkedList<MFDRWaveData>();
				for (UCRData data : trainlist) {
					train_dr_list.add(mfdr_p.getResult(data.timeSeries(), NoC)
							.data());
				}
				/* Prepare Testing list */
				LinkedList<MFDRWaveData> test_dr_list = new LinkedList<MFDRWaveData>();
				for (UCRData data : testlist) {
					test_dr_list.add(mfdr_p.getResult(data.timeSeries(), NoC)
							.data());
				}
				/* Test each time sereis in the test list */
				int[] result = new int[testlist.size()];
				for (int j = 0; j < testlist.size(); j++) {
					Map<Double, Integer> trainlistmap = new HashMap<Double, Integer>();
					double[] dist = new double[trainlist.size()];
					for (int k = 0; k < trainlist.size(); k++) {
						dist[k] = mfdr.getDistance(test_dr_list.get(j),
								train_dr_list.get(k), size, distance);
						trainlistmap.put(dist[k], trainlist.get(k)
								.ClusterNumber());
					}
					computeKNN(K, testlist, result, j, trainlistmap,	dist);
				}
				saveResult(fagent, filenamelist, i, testlist, NoC, result);
			}
		}
	}

	public void runDFTMethod(String readaddress, String writeaddress,
			int NoC_Start, int NoC_Interval, int NoC_End,
			DFT dr, FileAccessAgent fagent,
			LinkedList<String> filenamelist, int K) {
		fagent.updatewritingpath(writeaddress + "KNN_" + dr.name() + ".csv");
		Distance distance = new EuclideanDistance();
		/* Iterate through all data set in the given list. */
		for (int i = 0; i < filenamelist.size(); i++) {
			LinkedList<UCRData> trainlist = parser.getUCRDataListTrain(
					readaddress, filenamelist.get(i));
			LinkedList<UCRData> testlist = parser.getUCRDataListTest(
					readaddress, filenamelist.get(i));

			logger.info("Training File:" + filenamelist.get(i) + ", TS Size: "
					+ trainlist.size() + ", Extracting...");
			logger.info("Testing File:" + filenamelist.get(i) + ", TS Size: "
					+ testlist.size() + ", Extracting...");
			int size = trainlist.get(0).timeSeries().size();
			/* Iterate through all NoC settings */
			for (int NoC = NoC_Start; NoC <= NoC_End; NoC += NoC_Interval) {
				dr.setNoC(NoC);
				/*-------------- Print Start ------------*/
				System.out.print("[NoC=" + NoC + "]: ");
				/*-------------- Print End --------------*/
				count = 0;
				/* Prepare Training list */
				LinkedList<DFTData> train_dr_list = new LinkedList<DFTData>();
				for (UCRData data : trainlist) {
					train_dr_list.add(dr.getDR(data.timeSeries()));
				}
				/* Prepare Testing list */
				LinkedList<DFTData> test_dr_list = new LinkedList<DFTData>();
				for (UCRData data : testlist) {
					test_dr_list.add(dr.getDR(data.timeSeries()));
				}
				/* Test each time sereis in the test list */
				int[] result = new int[testlist.size()];
				for (int j = 0; j < testlist.size(); j++) {
					Map<Double, Integer> trainlistmap = new HashMap<Double, Integer>();
					double[] dist = new double[trainlist.size()];
					for (int k = 0; k < trainlist.size(); k++) {
						dist[k] = dr.getDistance(test_dr_list.get(j),train_dr_list.get(k), distance, size);
						trainlistmap.put(dist[k], trainlist.get(k)
								.ClusterNumber());
					}
					computeKNN(K, testlist, result, j, trainlistmap,	dist);
				}
				saveResult(fagent, filenamelist, i, testlist, NoC, result);
			}
		}
	}
	
	public void runPLAMethod(String readaddress, String writeaddress,
			int NoC_Start, int NoC_Interval, int NoC_End,
			PLA dr, FileAccessAgent fagent,
			LinkedList<String> filenamelist, int K) {
		fagent.updatewritingpath(writeaddress + "KNN_" + dr.name() + ".csv");
		Distance distance = new EuclideanDistance();
		/* Iterate through all data set in the given list. */
		for (int i = 0; i < filenamelist.size(); i++) {
			LinkedList<UCRData> trainlist = parser.getUCRDataListTrain(
					readaddress, filenamelist.get(i));
			LinkedList<UCRData> testlist = parser.getUCRDataListTest(
					readaddress, filenamelist.get(i));

			logger.info("Training File:" + filenamelist.get(i) + ", TS Size: "
					+ trainlist.size() + ", Extracting...");
			logger.info("Testing File:" + filenamelist.get(i) + ", TS Size: "
					+ testlist.size() + ", Extracting...");
			int size = trainlist.get(0).timeSeries().size();
			/* Iterate through all NoC settings */
			for (int NoC = NoC_Start; NoC <= NoC_End; NoC += NoC_Interval) {
				dr.setNoC(NoC);
				/*-------------- Print Start ------------*/
				System.out.print("[NoC=" + NoC + "]: ");
				/*-------------- Print End --------------*/
				count = 0;
				/* Prepare Training list */
				LinkedList<LinkedList<PLAData>> train_dr_list = new LinkedList<LinkedList<PLAData>>();
				for (UCRData data : trainlist) {
					train_dr_list.add(dr.getDR(data.timeSeries()));
				}
				/* Prepare Testing list */
				LinkedList<LinkedList<PLAData>> test_dr_list = new LinkedList<LinkedList<PLAData>>();
				for (UCRData data : testlist) {
					test_dr_list.add(dr.getDR(data.timeSeries()));
				}
				/* Test each time sereis in the test list */
				int[] result = new int[testlist.size()];
				for (int j = 0; j < testlist.size(); j++) {
					Map<Double, Integer> trainlistmap = new HashMap<Double, Integer>();
					double[] dist = new double[trainlist.size()];
					for (int k = 0; k < trainlist.size(); k++) {
						dist[k] = dr.getDistance(test_dr_list.get(j),train_dr_list.get(k), size, distance);
						trainlistmap.put(dist[k], trainlist.get(k)
								.ClusterNumber());
					}
					computeKNN(K, testlist, result, j, trainlistmap,	dist);
				}
				saveResult(fagent, filenamelist, i, testlist, NoC, result);
			}
		}
	}

	public void runPLAAMethod(String readaddress, String writeaddress,
			int NoC_Start, int NoC_Interval, int NoC_End,
			PLAA dr, FileAccessAgent fagent,
			LinkedList<String> filenamelist, int K) {
		fagent.updatewritingpath(writeaddress + "KNN_" + dr.name() + ".csv");
		Distance distance = new EuclideanDistance();
		/* Iterate through all data set in the given list. */
		for (int i = 0; i < filenamelist.size(); i++) {
			LinkedList<UCRData> trainlist = parser.getUCRDataListTrain(
					readaddress, filenamelist.get(i));
			LinkedList<UCRData> testlist = parser.getUCRDataListTest(
					readaddress, filenamelist.get(i));

			logger.info("Training File:" + filenamelist.get(i) + ", TS Size: "
					+ trainlist.size() + ", Extracting...");
			logger.info("Testing File:" + filenamelist.get(i) + ", TS Size: "
					+ testlist.size() + ", Extracting...");
			int size = trainlist.get(0).timeSeries().size();
			/* Iterate through all NoC settings */
			for (int NoC = NoC_Start; NoC <= NoC_End; NoC += NoC_Interval) {
				dr.setNoC(NoC);
				/*-------------- Print Start ------------*/
				System.out.print("[NoC=" + NoC + "]: ");
				/*-------------- Print End --------------*/
				count = 0;
				/* Prepare Training list */
				LinkedList<LinkedList<PLAAData>> train_dr_list = new LinkedList<LinkedList<PLAAData>>();
				for (UCRData data : trainlist) {
					train_dr_list.add(dr.getDR(data.timeSeries()));
				}
				/* Prepare Testing list */
				LinkedList<LinkedList<PLAAData>> test_dr_list = new LinkedList<LinkedList<PLAAData>>();
				for (UCRData data : testlist) {
					test_dr_list.add(dr.getDR(data.timeSeries()));
				}
				/* Test each time sereis in the test list */
				int[] result = new int[testlist.size()];
				for (int j = 0; j < testlist.size(); j++) {
					Map<Double, Integer> trainlistmap = new HashMap<Double, Integer>();
					double[] dist = new double[trainlist.size()];
					for (int k = 0; k < trainlist.size(); k++) {
						dist[k] = dr.getDistance(test_dr_list.get(j),train_dr_list.get(k), size, distance);
						trainlistmap.put(dist[k], trainlist.get(k)
								.ClusterNumber());
					}
					computeKNN(K, testlist, result, j, trainlistmap,	dist);
				}
				saveResult(fagent, filenamelist, i, testlist, NoC, result);
			}
		}
	}
	
	public void runPAAMethod(String readaddress, String writeaddress,
			int NoC_Start, int NoC_Interval, int NoC_End,
			PAA dr, FileAccessAgent fagent,
			LinkedList<String> filenamelist, int K) {
		fagent.updatewritingpath(writeaddress + "KNN_" + dr.name() + ".csv");
		Distance distance = new EuclideanDistance();
		/* Iterate through all data set in the given list. */
		for (int i = 0; i < filenamelist.size(); i++) {
			LinkedList<UCRData> trainlist = parser.getUCRDataListTrain(
					readaddress, filenamelist.get(i));
			LinkedList<UCRData> testlist = parser.getUCRDataListTest(
					readaddress, filenamelist.get(i));

			logger.info("Training File:" + filenamelist.get(i) + ", TS Size: "
					+ trainlist.size() + ", Extracting...");
			logger.info("Testing File:" + filenamelist.get(i) + ", TS Size: "
					+ testlist.size() + ", Extracting...");
			int size = trainlist.get(0).timeSeries().size();
			/* Iterate through all NoC settings */
			for (int NoC = NoC_Start; NoC <= NoC_End; NoC += NoC_Interval) {
				dr.setNoC(NoC);
				/*-------------- Print Start ------------*/
				System.out.print("[NoC=" + NoC + "]: ");
				/*-------------- Print End --------------*/
				count = 0;
				/* Prepare Training list */
				LinkedList<LinkedList<PAAData>> train_dr_list = new LinkedList<LinkedList<PAAData>>();
				for (UCRData data : trainlist) {
					train_dr_list.add(dr.getDR(data.timeSeries()));
				}
				/* Prepare Testing list */
				LinkedList<LinkedList<PAAData>> test_dr_list = new LinkedList<LinkedList<PAAData>>();
				for (UCRData data : testlist) {
					test_dr_list.add(dr.getDR(data.timeSeries()));
				}
				/* Test each time sereis in the test list */
				int[] result = new int[testlist.size()];
				for (int j = 0; j < testlist.size(); j++) {
					Map<Double, Integer> trainlistmap = new HashMap<Double, Integer>();
					double[] dist = new double[trainlist.size()];
					for (int k = 0; k < trainlist.size(); k++) {
						dist[k] = dr.getDistance(test_dr_list.get(j),train_dr_list.get(k), size, distance);
						trainlistmap.put(dist[k], trainlist.get(k)
								.ClusterNumber());
					}
					computeKNN(K, testlist, result, j, trainlistmap,	dist);
				}
				saveResult(fagent, filenamelist, i, testlist, NoC, result);
			}
		}
	}
	
	public void saveResult(FileAccessAgent fagent,
			LinkedList<String> filenamelist, int i,
			LinkedList<UCRData> testlist, int NoC, int[] result) {
		/* Finalise results */
		double success_count = 0;
		for (int j = 0; j < testlist.size(); j++) {
			if (testlist.get(i).ClusterNumber() == result[j])
				success_count++;
		}
		System.out.println(100 + "% ");

		/* Store result */
		String outputstring = filenamelist.get(i) + ",[" + NoC + "],SUC,"
				+ success_count + ",TOTAL," + testlist.size()
				+ ",SUC_Ratio," + success_count/testlist.size();
		fagent.writeLineToFile(outputstring);
		System.out.println(outputstring);
	}

	public void computeKNN(int K, LinkedList<UCRData> testlist, 
			int[] result, int j, Map<Double, Integer> trainlistmap,
			double[] dist) {
		// Sort and get candidate counts
		Arrays.sort(dist);
		Map<Integer, KNNCandidate> candidate = new HashMap<Integer, KNNCandidate>();
		for (int k = 0; k < K; k++) {
			int cluster_id = trainlistmap.get(dist[k]);
			if (candidate.containsKey(cluster_id)) {
				if (dist[k] < candidate.get(cluster_id).dist()) {
					int current_count = candidate.get(cluster_id)
							.count();
					candidate
							.put(cluster_id, new KNNCandidate(
									cluster_id, dist[k],
									current_count + 1));
				}
			} else {
				candidate.put(cluster_id, new KNNCandidate(
						cluster_id, dist[k], 1));
			}
		}
		// Resort results
		Iterator<Integer> it = candidate.keySet().iterator();
		KNNCandidate candidate_cluster = candidate.get(it.next());
		while (it.hasNext()) {
			int id = it.next();
			if (candidate.get(id).count() > candidate_cluster
					.count()) {
				candidate_cluster = candidate.get(id);
			} else if (candidate.get(id).count() == candidate_cluster
					.count()) {
				if (candidate.get(id).dist() < candidate_cluster
						.dist())
					candidate_cluster = candidate.get(id);
			}
		}
		result[j] = candidate_cluster.id();

		/* ----- Print Counter Start ------- */
		int currentcount = j * 20 / testlist.size();
		if (currentcount > count) {
			System.out.print(currentcount * 5 + "% ");
			count++;
		}
		/* ------- Print Counter End -------- */
	}
}
