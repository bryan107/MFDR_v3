package mfdr.dimensionality.reduction;

import java.util.LinkedList;

import mfdr.core.MFDRWithParameter;
import mfdr.core.back.MFDRParameters;
import mfdr.datastructure.TimeSeries;
import mfdr.distance.Distance;
import mfdr.distance.EuclideanDistance;
import mfdr.file.FileAccessAgent;
import mfdr.utility.DataListOperator;
import mfdr.utility.File;
import experiment.utility.UCRParser;
import experiment.utility.UCRParser2015;
import junit.framework.TestCase;

public class TestPLAA extends TestCase {
	private final String readaddress = "C:\\TEST\\MFDR\\UCR_TS_Archive_2015\\";
	private final String listaddress = "C:\\TEST\\MFDR\\UCR_TS_Archive_2015\\test_list.txt";
	private final String writeaddress = "C:\\TEST\\MFDR\\test\\PLAA.csv";
	
	PLAA plaa = new PLAA(6);
	
	public void testDR(){
		
	}
	
	public void testFullDR(){
		TimeSeries ts1 = getTimeSeries(0);
		TimeSeries ts2 = getTimeSeries(1);
		TimeSeries drfull = plaa.getFullResolutionDR(ts1);
		File.getInstance().saveLinkedListToFile("ts_0", ts1, writeaddress);
		File.getInstance().saveLinkedListToFile("plaafull_0", drfull, writeaddress);
		
		drfull = plaa.getFullResolutionDR(ts2);
		File.getInstance().saveLinkedListToFile("ts_1", ts2, writeaddress);
		File.getInstance().saveLinkedListToFile("plaafull_1", drfull, writeaddress);
	}
	
	public void testDistance(){
		MFDRWithParameter mfdr_p = new MFDRWithParameter();
		MFDR mfdr = new MFDR(3,3);
		PLA pla = new PLA(6);
		
		TimeSeries ts1 = getTimeSeries(0);
		TimeSeries ts2 = getTimeSeries(1);
		
		TimeSeries drfull_1 = plaa.getFullResolutionDR(ts1);
		TimeSeries drfull_2 = plaa.getFullResolutionDR(ts2);
		Distance distance = new EuclideanDistance();
		
		System.out.println("Please: " + distance.calDistance(mfdr.getFullResolutionDR(mfdr_p.getResult(ts1, 6).data(), ts1), ts1, ts1));
		System.out.println("NO: " + distance.calDistance(drfull_1, ts1, ts1));
		
		System.out.println("MFDR: " + mfdr.getDistance(mfdr_p.getResult(ts1, 6).data(), mfdr_p.getResult(ts2, 6).data(), ts1.size(), distance));
		System.out.println("TS Dist:" + distance.calDistance(ts1, ts2, ts1));
		
		System.out.println("Full Dist: " + distance.calDistance(drfull_1, drfull_2, drfull_1));
		System.out.println("DR Dist: " + plaa.getDistance(ts1, ts2, distance));
		
		TimeSeries plafull_1 = pla.getFullResolutionDR(ts1);
		TimeSeries plafull_2 = pla.getFullResolutionDR(ts2);
		System.out.println("PLA Full Dist: " + distance.calDistance(plafull_1, plafull_2, plafull_1));
		System.out.println("PLA Dist: " + pla.getDistance(ts1, ts2, distance));
	}
	
	private TimeSeries getTimeSeries(int TSID){
		FileAccessAgent fagent = new FileAccessAgent(
				"C:\\TEST\\MFDR\\NULL.txt", "C:\\TEST\\MFDR\\NULL.txt");
		UCRParser parser = new UCRParser2015(fagent);
		LinkedList<String> filenamelist = parser.getFileNameList(fagent,
				listaddress);
		LinkedList<TimeSeries> tssetall = parser.getTimeSeriesListTrain(fagent,
				readaddress, filenamelist.get(0));
		return tssetall.get(TSID);
	}
}
