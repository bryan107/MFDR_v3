package mfdr.dimensionality.reduction;

import java.util.LinkedList;


import mfdr.core.MFDRObject;
import mfdr.core.MFDRWithParameter;
import mfdr.datastructure.TimeSeries;
import mfdr.dimensionality.datastructure.DFTData;
import mfdr.dimensionality.datastructure.MFDRWaveData;
import mfdr.dimensionality.datastructure.PLAData;
import mfdr.distance.Distance;
import mfdr.distance.EuclideanDistance;
import mfdr.file.FileAccessAgent;
import mfdr.utility.Print;
import experiment.utility.UCRParser;
import experiment.utility.UCRParser2015;
import junit.framework.TestCase;

public class TestMFDR_LB extends TestCase {

	private final String readaddress = "C:\\TEST\\MFDR\\UCR_TS_Archive_2015\\";
	private final String listaddress = "C:\\TEST\\MFDR\\UCR_TS_Archive_2015\\test_list.txt";
	private int NoC_t = 6, NoC_s = 1;
	private double LB_num_dev = 3;
	private int run = 30;
	private int TSID = 0; 
	
	public void testIndexQuery() {
		Distance distance = new EuclideanDistance();
		DFT dft = new DFT(NoC_t + NoC_s);
		PLA pla = new PLA(NoC_t + NoC_s);
		MFDR mfdr = new MFDR(NoC_t, NoC_s);
		MFDRWithParameter mfdr_p = new MFDRWithParameter();
		DFT_LB dftlb = new DFT_LB(NoC_t + NoC_s);
		FileAccessAgent fagent = new FileAccessAgent(
				"C:\\TEST\\MFDR\\NULL.txt", "C:\\TEST\\MFDR\\NULL.txt");
		UCRParser parser = new UCRParser2015(fagent);
		LinkedList<String> filenamelist = parser.getFileNameList(fagent,
				listaddress);
		LinkedList<TimeSeries> tssetall = parser.getTimeSeriesListTrain(fagent,
				readaddress, filenamelist.get(0));

		LinkedList<TimeSeries> tsset = new LinkedList<TimeSeries>();
		for(int i = 0 ; i < 50 ; i++){
			tsset.add(tssetall.get(i));
		}
		LinkedList<TimeSeries> tsquery = parser.getTimeSeriesListTest(fagent, readaddress, filenamelist.get(0));
		
		// Prepare Index
		LinkedList<DFTData> dftlist = new LinkedList<DFTData>();
		for (TimeSeries ts : tsset) {
			dftlist.add(dft.getDR(ts));
		}
		LinkedList<DFTData> dftlblist = new LinkedList<DFTData>();
		for (TimeSeries ts : tsset) {
			dftlblist.add(dftlb.getDR(ts));
		}
		LinkedList<LinkedList<PLAData>> plalist = new LinkedList<LinkedList<PLAData>>();
		for (TimeSeries ts : tsset) {
			plalist.add(pla.getDR(ts));
		}
		LinkedList<MFDRWaveData> mfdrlist = new LinkedList<MFDRWaveData>();
		for (TimeSeries ts : tsset) {
			MFDRObject ob = mfdr_p.getResult(ts, NoC_t+NoC_s);
			mfdrlist.add(ob.data());
		}
		
		
		
		// Compute Distance
		double[] dft_dist = dft.computeIndexingQuery(tsquery.get(TSID), dftlist,distance);
		double[] pla_dist = pla.computeIndexingQuery(tsquery.get(TSID), plalist, distance,NoC_t+NoC_s);
		double[] mfdr_dist = mfdr.computeIndexingQuery(tsquery.get(TSID), mfdrlist, distance, NoC_t+NoC_s, 0);
		double[] dft_NLB_dist = new double[dftlist.size()];
		double[] mfdr_NLB_dist = new double[mfdrlist.size()];
		MFDRObject mfdrdata_NLB = mfdr_p.getResult(tsquery.get(TSID), NoC_t+NoC_s);
		for(int i = 0 ; i < mfdrlist.size() ; i++){
			mfdr_NLB_dist[i] = mfdr.getDistance(mfdrdata_NLB.data(), mfdrlist.get(i), tsquery.get(TSID).size(), distance);
		}
		
		
		DFTData querydata = dft.getDR(tsquery.get(TSID));
		DFTData querylbdata = dftlb.getDR(tsquery.get(TSID));
		
		double dft_violation = 0, dft_NLB_violation = 0, dft_LB_violation = 0, pla_violation = 0, mfdr_violation = 0, mfdr_NLB_violation = 0;
		double err_dft = 0, err_NLB_dft = 0, err_LB_dft = 0 , err_pla = 0, err_mfdr = 0, err_mfdr_NLB = 0;
		
		double[] act_dist = new double[dft_dist.length];
		for (int i = 0; i < dft_dist.length; i++) {
			// Compute
			dft_NLB_dist[i] = dft.getDistance(querydata, dftlist.get(i),
					distance, tsquery.get(TSID).size());
			double dft_LB_dist = dft.getDistance(querylbdata, dftlblist.get(i),
					distance, tsquery.get(TSID).size());
			act_dist[i] = distance.calDistance(tsquery.get(TSID),
					tsset.get(i), tsset.get(i));
			// Store reuslts
			err_dft += Math.abs(dft_dist[i]-act_dist[i]) / act_dist[i];
			if (dft_dist[i] > act_dist[i])
				dft_violation++;

			err_pla += Math.abs(pla_dist[i]-act_dist[i]) / act_dist[i];
			if (pla_dist[i] > act_dist[i])
				pla_violation++;
			
			err_mfdr += Math.abs(mfdr_dist[i]-act_dist[i]) / act_dist[i];
			if (mfdr_dist[i] > act_dist[i])
				mfdr_violation++;
			
			err_mfdr_NLB += Math.abs(mfdr_NLB_dist[i]-act_dist[i]) / act_dist[i];
			if (mfdr_NLB_dist[i] > act_dist[i])
				mfdr_NLB_violation++;
			
			err_NLB_dft += Math.abs(dft_NLB_dist[i]-act_dist[i]) / act_dist[i];
			if (dft_NLB_dist[i] > act_dist[i])
				dft_NLB_violation++;

			err_LB_dft += Math.abs(dft_LB_dist-act_dist[i]) / act_dist[i];
			if (dft_LB_dist > act_dist[i])
				dft_LB_violation++;
		}
		System.out.println("[DFT]     V:" + dft_violation / dftlist.size()	+ " ERR:" + err_dft/ dftlist.size());
		System.out.println("[PLA]     V:" + pla_violation / dftlist.size()	+ " ERR:" + err_pla/ dftlist.size());
		System.out.println("[MFDR]     V:" + mfdr_violation / dftlist.size()	+ " ERR:" + err_mfdr/ dftlist.size());
		System.out.println("[MFDR_NLB] V:" + mfdr_NLB_violation / dftlist.size()	+ " ERR:" + err_mfdr_NLB/ dftlist.size());
		System.out.println("[DFT_NLB] V:" + dft_NLB_violation / dftlist.size() + " ERR:" + err_NLB_dft/ dftlist.size());
		System.out.println("[DFT_LB]  V:" + dft_LB_violation / dftlist.size() + " ERR:" + err_LB_dft/ dftlist.size());
		System.out.print("Actual Dist    : ");
		Print.getInstance().printArray(act_dist, act_dist.length);
		System.out.print("DFT Dist       : ");
		Print.getInstance().printArray(dft_dist, dft_dist.length);
		System.out.print("PLA Dist       : ");
		Print.getInstance().printArray(pla_dist, pla_dist.length);
		System.out.print("MFDR Dist      : ");
		Print.getInstance().printArray(mfdr_dist, mfdr_dist.length);
		System.out.print("MFDR_NLB Dist  : ");
		Print.getInstance().printArray(mfdr_NLB_dist, mfdr_NLB_dist.length);
		System.out.print("DFT_NLB Dist   : ");
		Print.getInstance().printArray(dft_NLB_dist, dft_NLB_dist.length);
	}

	// public void testFFT(){
	// MFDR mfdr = new MFDR(NoC_t, NoC_s);
	// DFT_LB dft = new DFT_LB(NoC_t+NoC_s);
	// MFDR_LB mfdrlb = new MFDR_LB(NoC_t, NoC_s, LB_num_dev);
	// /* Retrieve time series files */
	// FileAccessAgent fagent = new FileAccessAgent(
	// "C:\\TEST\\MFDR\\NULL.txt", "C:\\TEST\\MFDR\\NULL.txt");
	// UCRParser parser = new UCRParser2015(fagent);
	// LinkedList<String> filenamelist = parser.getFileNameList(fagent,
	// listaddress);
	// LinkedList<TimeSeries> tsset =
	// parser.getTimeSeriesListTest(fagent,readaddress, filenamelist.get(0));
	// Distance distance = new EuclideanDistance();
	// int mfdr_violation = 0, mfdr_lb_violation = 0, dft_violation = 0,
	// dd_violation = 0;
	// double err_mfdr = 0 , err_mfdr_lb = 0, err_dft = 0, err_dd = 0 ;
	// System.out.println("Ori:" + distance.calDistance(tsset.get(20),
	// tsset.get(40), tsset.get(20)));
	//
	// dft.setNoC(10);
	// double[] ts1 =
	// DataListOperator.getInstance().linkedListToArray(tsset.get(20), (short)
	// 1);
	// double[] ts2 =
	// DataListOperator.getInstance().linkedListToArray(tsset.get(40), (short)
	// 1);
	// // double[] ts1 =
	// DataListOperator.getInstance().linkedListToArray(getTS(6,32), (short) 1);
	// // double[] ts2 =
	// DataListOperator.getInstance().linkedListToArray(getTS(10,32), (short)
	// 1);
	// System.out.println("Ori:" + distance.calDistance(ts1, ts1));
	// System.out.println("ARR:" + distance.calDistance(ts1, ts2));
	// TimeSeries tts1 = DataListOperator.getInstance().arrayToLinkedList(ts1);
	// TimeSeries tts2 = DataListOperator.getInstance().arrayToLinkedList(ts2);
	// System.out.println("DFT:" + dft.getDistance(tts1, tts2, distance));
	// DoubleFFT_1D fft = new DoubleFFT_1D(ts1.length);
	// fft.realForward(ts1);
	// fft.realForward(ts2);
	// for(int index = 0 ; index < ts1.length ; index++){
	// if(index < 2){
	// ts1[index] = ts1[index] / Math.pow(32, 0.5);
	// ts2[index] = ts2[index] / Math.pow(32, 0.5);
	// }
	// else{
	// ts1[index] = ts1[index] / Math.pow(32/2, 0.5);
	// ts2[index] = ts2[index] / Math.pow(32/2, 0.5);
	// }
	// }
	// System.out.println("FFT:" + distance.calDistance(ts1, ts2));
	//
	// }
	//
	// private TimeSeries getTS(double speed, int length){
	// TimeSeries ts = new TimeSeries();
	// for(int i = 0 ; i < length ; i++){
	// ts.add(new Data(i, Math.cos(i*Math.PI / speed)));
	// }
	// return ts;
	// }
	//
	// public void testGetDistance(){
	// MFDR mfdr = new MFDR(NoC_t, NoC_s);
	// DFT_LB dft = new DFT_LB(NoC_t+NoC_s);
	// PLA pla = new PLA(NoC_t+NoC_s);
	// MFDR_LB mfdrlb = new MFDR_LB(NoC_t, NoC_s, LB_num_dev);
	// /* Retrieve time series files */
	// FileAccessAgent fagent = new FileAccessAgent(
	// "C:\\TEST\\MFDR\\NULL.txt", "C:\\TEST\\MFDR\\NULL.txt");
	// UCRParser parser = new UCRParser2015(fagent);
	// LinkedList<String> filenamelist = parser.getFileNameList(fagent,
	// listaddress);
	// LinkedList<TimeSeries> tsset =
	// parser.getTimeSeriesListTrain(fagent,readaddress, filenamelist.get(0));
	// Distance distance = new EuclideanDistance();
	//
	// int mfdr_violation = 0, mfdr_lb_violation = 0, dft_violation = 0,
	// dd_violation = 0, pla_violation = 0, pp_count = 0;
	// double err_mfdr = 0 , err_mfdr_lb = 0, err_dft = 0, err_dd = 0, err_pla =
	// 0 ;
	// for(int i = 0 ; i < run ; i++){
	// double act_dist = distance.calDistance(tsset.get(i), tsset.get(i +
	// 20),tsset.get(i));
	// double mfdr_dist = mfdr.getDistance(tsset.get(i), tsset.get(i + 20),
	// distance);
	// double mfdr_lb_dist = mfdrlb.getDistance(tsset.get(i), tsset.get(i + 20),
	// distance);
	// double dft_dist = dft.getDistance(tsset.get(i), tsset.get(i + 20),
	// distance);
	// double dd_dist =
	// distance.calDistance(dft.getFullResolutionDR(tsset.get(i)),
	// dft.getFullResolutionDR(tsset.get(i + 20)), tsset.get(i));
	// double pla_dist = pla.getDistance(tsset.get(i), tsset.get(i + 20),
	// distance);
	//
	// if(mfdr_dist > act_dist)
	// mfdr_violation++;
	// if(mfdr_lb_dist > act_dist){
	// mfdr_lb_violation++;
	// System.out.print("[" + i + "]:   ");
	// System.out.print("Actual_Dist: " + act_dist);
	// System.out.print(" MFDR_Dist :" + mfdr_dist);
	// System.out.println(" MFDR_LB_Dist :" + mfdr_lb_dist);
	// }
	// if(dft_dist > act_dist)
	// dft_violation++;
	// if(dd_dist > act_dist){
	// dd_violation++;
	// double[][] arr = new double [2][];
	// arr[0] = dft.converTSToFrequency(tsset.get(i));
	// arr[1] = dft.converTSToFrequency(tsset.get(i + 20));
	// System.out.print("[" + i + "]:   ");
	// System.out.println("Actual_Dist: " + act_dist + " DDF:" +
	// Math.sqrt(distance.calDistance(arr[0], arr[1])) + " DD:" + dd_dist);
	// }
	// if(pla_dist > act_dist)
	// pla_violation++;
	// err_mfdr += Math.abs(mfdr_dist)/act_dist;
	// err_mfdr_lb += Math.abs(mfdr_lb_dist)/act_dist;
	// err_dft += Math.abs(dft_dist)/act_dist;
	// err_dd += Math.abs(dd_dist)/act_dist;
	// err_pla += Math.abs(pla_dist)/act_dist;
	//
	// if(err_pla > err_dft)
	// pp_count++;
	//
	// }
	// System.out.println("Violation Count: MFDR=" + mfdr_violation +
	// " MFDR_LB=" + mfdr_lb_violation + " DFT=" + dft_violation +" DD=" +
	// dft_violation +" DD=" + pla_violation +" out of " + run);
	// System.out.println("Average Error: MFDR=" + err_mfdr/run + " MFDR_LB=" +
	// err_mfdr_lb/run + " DFT=" + err_dft/run + " DD=" + err_dd/run + " PLA=" +
	// err_pla/run);
	// System.out.println("PP:" + pp_count);
	// }

}
