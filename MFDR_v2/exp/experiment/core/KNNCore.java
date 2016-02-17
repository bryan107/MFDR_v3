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
import mfdr.datastructure.TimeSeries;
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
		
		mfdr_p.updateNoCCalculator(new HeuristicNoCCalculator());
		runMFDRNewMethod(readaddress, writeaddress, NoC_Start, NoC_Interval, NoC_End, mfdr_p, fagent, filenamelist, K);
		
//		mfdr_p.updateNoCCalculator(new BruteForceNoCCalculator());
//		runMFDRMethod(readaddress, writeaddress, NoC_Start, NoC_Interval, NoC_End, mfdr_p, fagent, filenamelist, K);
//		
//		runDFTMethod(readaddress, writeaddress, NoC_Start, NoC_Interval, NoC_End, new DFT(2), fagent, filenamelist, K);
//		
//		runDFTMethod(readaddress, writeaddress, NoC_Start, NoC_Interval, NoC_End, new DFT_LB(2), fagent, filenamelist, K);
//		
//		runPLAMethod(readaddress, writeaddress, NoC_Start, NoC_Interval, NoC_End, new PLA(2), fagent, filenamelist, K);
//		
//		runPLAAMethod(readaddress, writeaddress, NoC_Start, NoC_Interval, NoC_End, new PLAA(2), fagent, filenamelist, K);
//		
//		/* Run PAA which can host two times more NoC then other solution for fair comparison. */
//		runPAAMethod(readaddress, writeaddress, NoC_Start*2, NoC_Interval*2, NoC_End*2, new PAA(2), fagent, filenamelist, K);
//		
//		runFullMethod(readaddress, writeaddress, fagent, filenamelist, K);
	}

	public void runFullMethod(String readaddress, String writeaddress, FileAccessAgent fagent,
			LinkedList<String> filenamelist, int K) {
		fagent.updatewritingpath(writeaddress + "KNN_" + K + "_FULL.csv");
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
			
			/* Prepare Testing list */
			LinkedList<TimeSeries> test_s_list = new LinkedList<TimeSeries>();
			LinkedList<Integer> test_id_list = new LinkedList<Integer>();
			for (int r = 0 ; r < 100 ; r++) {
				if(test_s_list.size() > 100)
					break;
				int id = (int)(Math.random()*testlist.size());
				test_s_list.add(testlist.get(id).timeSeries());
				test_id_list.add(id);
			}
			/* Test each time sereis in the test list */
			int[] result = new int[test_s_list.size()];
			for (int j = 0; j < test_s_list.size(); j++) {
				Map<Double, Integer> trainlistmap = new HashMap<Double, Integer>();
				double[] dist = new double[trainlist.size()];
				for (int k = 0; k <trainlist.size(); k++) {
					try {
						dist[k] = distance.calDistance(test_s_list.get(j), trainlist.get(k).timeSeries(), test_s_list.get(j));
						trainlistmap.put(dist[k], trainlist.get(k).ClusterNumber());
					} catch (Exception e) {
						System.out.println("GG");
					}
				
				}
				computeKNN(K, testlist, result, j, trainlistmap,	dist);
			}
			saveResult(fagent, filenamelist, i, testlist, test_id_list, 1, result);
		}
	}
	
	public void runMFDRNewMethod(String readaddress, String writeaddress,
			int NoC_Start, int NoC_Interval, int NoC_End,
			MFDRWithParameter mfdr_p, FileAccessAgent fagent,
			LinkedList<String> filenamelist, int K) {
		fagent.updatewritingpath(writeaddress + "KNN_" + K + "_" + mfdr_p.name() + ".csv");
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
					train_dr_list.add(mfdr_p.getResult(data.timeSeries(), NoC).data());
				}
				/* Prepare Testing list */
				LinkedList<MFDRWaveData> test_dr_list = new LinkedList<MFDRWaveData>();
				LinkedList<Integer> test_id_list = new LinkedList<Integer>();
				for (int r = 0 ; r < 100 ; r++) {
					if(test_dr_list.size() > 100)
						break;
					int id = (int)(Math.random()*testlist.size());
					test_dr_list.add(mfdr_p.getResult(testlist.get(id).timeSeries(), NoC).data());
					test_id_list.add(id);
				}
				/* Test each time sereis in the test list */
				int[] result = new int[test_dr_list.size()];
				for (int j = 0; j < test_dr_list.size(); j++) {
					Map<Double, Integer> trainlistmap = new HashMap<Double, Integer>();
					double[] dist = new double[train_dr_list.size()];
					for(int x = 0 ; x < dist.length ; x++){
						dist[x] = 100;
					}
					for (int k = 0; k < train_dr_list.size(); k++) {
						/* only consider candidates has same decomposition */
						if(train_dr_list.get(k).trends().size() == test_dr_list.get(j).trends().size()){
							dist[k] = mfdr.getDistance(test_dr_list.get(j),	train_dr_list.get(k), size, distance);
							trainlistmap.put(dist[k], trainlist.get(k).ClusterNumber());
						}
							
					}
					computeKNN(K, testlist, result, j, trainlistmap,	dist);
				}
				saveResult(fagent, filenamelist, i, testlist, test_id_list, NoC, result);
			}
		}
	}
	
	public void runMFDRMethod(String readaddress, String writeaddress,
			int NoC_Start, int NoC_Interval, int NoC_End,
			MFDRWithParameter mfdr_p, FileAccessAgent fagent,
			LinkedList<String> filenamelist, int K) {
		fagent.updatewritingpath(writeaddress + "KNN_" + K + "_" + mfdr_p.name() + ".csv");
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
					train_dr_list.add(mfdr_p.getResult(data.timeSeries(), NoC).data());
				}
				/* Prepare Testing list */
				LinkedList<MFDRWaveData> test_dr_list = new LinkedList<MFDRWaveData>();
				LinkedList<Integer> test_id_list = new LinkedList<Integer>();
				for (int r = 0 ; r < 100 ; r++) {
					if(test_dr_list.size() > 100)
						break;
					int id = (int)(Math.random()*testlist.size());
					test_dr_list.add(mfdr_p.getResult(testlist.get(id).timeSeries(), NoC).data());
					test_id_list.add(id);
				}
				/* Test each time sereis in the test list */
				int[] result = new int[test_dr_list.size()];
				for (int j = 0; j < test_dr_list.size(); j++) {
					Map<Double, Integer> trainlistmap = new HashMap<Double, Integer>();
					double[] dist = new double[train_dr_list.size()];
					for (int k = 0; k < train_dr_list.size(); k++) {
						dist[k] = mfdr.getDistance(test_dr_list.get(j),
								train_dr_list.get(k), size, distance);
						trainlistmap.put(dist[k], trainlist.get(k).ClusterNumber());
					}
					computeKNN(K, testlist, result, j, trainlistmap,	dist);
				}
				saveResult(fagent, filenamelist, i, testlist, test_id_list, NoC, result);
			}
		}
	}

	public void runDFTMethod(String readaddress, String writeaddress,
			int NoC_Start, int NoC_Interval, int NoC_End,
			DFT dr, FileAccessAgent fagent,
			LinkedList<String> filenamelist, int K) {
		fagent.updatewritingpath(writeaddress + "KNN_" + K + "_" + dr.name() + ".csv");
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
				LinkedList<Integer> test_id_list = new LinkedList<Integer>();
				/* Prepare Testing list */
				LinkedList<DFTData> test_dr_list = new LinkedList<DFTData>();
				for (int r = 0 ; r < 100 ; r++) {
					if(test_dr_list.size() > 100)
						break;
					int id = (int)(Math.random()*testlist.size());
					test_dr_list.add(dr.getDR(testlist.get(id).timeSeries()));
					test_id_list.add(id);
				}
				/* Test each time sereis in the test list */
				int[] result = new int[test_dr_list.size()];
				for (int j = 0; j < test_dr_list.size(); j++) {
					Map<Double, Integer> trainlistmap = new HashMap<Double, Integer>();
					double[] dist = new double[train_dr_list.size()];
					for (int k = 0; k < train_dr_list.size(); k++) {
						dist[k] = dr.getDistance(test_dr_list.get(j),train_dr_list.get(k), distance, size);
						trainlistmap.put(dist[k], trainlist.get(k)
								.ClusterNumber());
					}
					computeKNN(K, testlist, result, j, trainlistmap,	dist);
				}
				saveResult(fagent, filenamelist, i, testlist, test_id_list, NoC, result);
			}
		}
	}
	
	public void runPLAMethod(String readaddress, String writeaddress,
			int NoC_Start, int NoC_Interval, int NoC_End,
			PLA dr, FileAccessAgent fagent,
			LinkedList<String> filenamelist, int K) {
		fagent.updatewritingpath(writeaddress + "KNN_" + K + "_" + dr.name() + ".csv");
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
				LinkedList<Integer> test_id_list = new LinkedList<Integer>();
				for (int r = 0 ; r < 100 ; r++) {
					if(test_dr_list.size() > 100)
						break;
					int id = (int)(Math.random()*testlist.size());
					test_dr_list.add(dr.getDR(testlist.get(id).timeSeries()));
					test_id_list.add(id);
				}
				/* Test each time sereis in the test list */
				int[] result = new int[test_dr_list.size()];
				for (int j = 0; j < test_dr_list.size(); j++) {
					Map<Double, Integer> trainlistmap = new HashMap<Double, Integer>();
					double[] dist = new double[train_dr_list.size()];
					for (int k = 0; k < train_dr_list.size(); k++) {
						dist[k] = dr.getDistance(test_dr_list.get(j),train_dr_list.get(k), size, distance);
						trainlistmap.put(dist[k], trainlist.get(k)
								.ClusterNumber());
					}
					computeKNN(K, testlist, result, j, trainlistmap,	dist);
				}
				saveResult(fagent, filenamelist, i, testlist, test_id_list, NoC, result);
			}
		}
	}

	public void runPLAAMethod(String readaddress, String writeaddress,
			int NoC_Start, int NoC_Interval, int NoC_End,
			PLAA dr, FileAccessAgent fagent,
			LinkedList<String> filenamelist, int K) {
		fagent.updatewritingpath(writeaddress + "KNN_" + K + "_" + dr.name() + ".csv");
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
				LinkedList<Integer> test_id_list = new LinkedList<Integer>();
				for (int r = 0 ; r < 100 ; r++) {
					if(test_dr_list.size() > 100)
						break;
					int id = (int)(Math.random()*testlist.size());
					test_dr_list.add(dr.getDR(testlist.get(id).timeSeries()));
					test_id_list.add(id);
				}
				/* Test each time sereis in the test list */
				int[] result = new int[test_dr_list.size()];
				for (int j = 0; j < test_dr_list.size(); j++) {
					Map<Double, Integer> trainlistmap = new HashMap<Double, Integer>();
					double[] dist = new double[train_dr_list.size()];
					for (int k = 0; k < train_dr_list.size(); k++) {
						dist[k] = dr.getDistance(test_dr_list.get(j),train_dr_list.get(k), size, distance);
						trainlistmap.put(dist[k], trainlist.get(k)
								.ClusterNumber());
					}
					computeKNN(K, testlist, result, j, trainlistmap,	dist);
				}
				saveResult(fagent, filenamelist, i, testlist, test_id_list, NoC, result);
			}
		}
	}
	
	public void runPAAMethod(String readaddress, String writeaddress,
			int NoC_Start, int NoC_Interval, int NoC_End,
			PAA dr, FileAccessAgent fagent,
			LinkedList<String> filenamelist, int K) {
		fagent.updatewritingpath(writeaddress + "KNN_" + K + "_" + dr.name() + ".csv");
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
				LinkedList<Integer> test_id_list = new LinkedList<Integer>();
				for (int r = 0 ; r < 100 ; r++) {
					if(test_dr_list.size() > 100)
						break;
					int id = (int)(Math.random()*testlist.size());
					test_dr_list.add(dr.getDR(testlist.get(id).timeSeries()));
					test_id_list.add(id);
				}
				/* Test each time sereis in the test list */
				int[] result = new int[test_dr_list.size()];
				for (int j = 0; j < test_dr_list.size(); j++) {
					Map<Double, Integer> trainlistmap = new HashMap<Double, Integer>();
					double[] dist = new double[train_dr_list.size()];
					for (int k = 0; k < train_dr_list.size(); k++) {
						dist[k] = dr.getDistance(test_dr_list.get(j),train_dr_list.get(k), size, distance);
						trainlistmap.put(dist[k], trainlist.get(k)
								.ClusterNumber());
					}
					computeKNN(K, testlist, result, j, trainlistmap,	dist);
				}
				saveResult(fagent, filenamelist, i, testlist, test_id_list, NoC, result);
			}
		}
	}
	
	public void saveResult(FileAccessAgent fagent,
			LinkedList<String> filenamelist, int i,
			LinkedList<UCRData> testlist, LinkedList<Integer> test_id_list, int NoC, int[] result) {
		/* Finalise results */
		double success_count = 0;
		int max;
//		if(testlist.size()>100){
//			max = 100 ;
//		}else{
//			max = testlist.size();
//		}
		for (int j = 0; j < 100; j++) {
			if (testlist.get(test_id_list.get(j)).ClusterNumber() == result[j])
				success_count++;
		}
		System.out.println(100 + "% ");

		/* Store result */
		String outputstring = filenamelist.get(i) + ",[" + NoC + "],SUC_Ratio," + success_count/100 +",SUC,"
				+ success_count + ",TOTAL," + 100;
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
			int cluster_id;
			try {
				cluster_id = trainlistmap.get(dist[k]);
			} catch (Exception e) {
				cluster_id = -1;
			}
			if (candidate.containsKey(cluster_id)) {
				int current_count = candidate.get(cluster_id).count();
				candidate.put(cluster_id, new KNNCandidate(cluster_id, dist[k],	current_count + 1));
			} else if(cluster_id == -1){
				candidate.put(cluster_id, new KNNCandidate(cluster_id, 100, 1));
			} 
			else {
				candidate.put(cluster_id, new KNNCandidate(cluster_id, dist[k], 1));
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
