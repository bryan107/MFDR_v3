package experiment.utility;

import java.util.LinkedList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import mfdr.datastructure.Data;
import mfdr.datastructure.TimeSeries;
import mfdr.dimensionality.reduction.PLA;
import mfdr.file.FileAccessAgent;

public class UCRParserOld implements UCRParser {
	private static Log logger = LogFactory.getLog(UCRParserOld.class);
	FileAccessAgent fagent;
	
	public UCRParserOld(FileAccessAgent fagent){
		updateFileAccessAgent(fagent);
	}
	
	@Override
	public void updateFileAccessAgent(FileAccessAgent fagent){
		this.fagent = fagent;
	}
	
	@Override
	public UCRData getUCRData() {
		int clusternumber = -1;
		TimeSeries ts = new TimeSeries();
		String line = fagent.readLineFromFile();
		if(line == null){
			logger.info("Extraction from " + fagent.readingPath() + " is completed");
			return new UCRData(-1, null);
		}
		int count = -1;
		String[] split = line.split(" ");
		for(int i = 0 ; i < split.length ; i++){
			// Remove unused spaces
			split[i].replaceAll("\\s", "");
			if(!split[i].equals("")){
				count++;
				if(count == 0){
					clusternumber = (int)extractValue(split, i);
					continue;
				}
				try {
					double test = extractValue(split, i);
					ts.add(new Data(count,test));
				} catch (Exception e) {
					logger.info("split:" + split[i] + e);
				}
			}
		}
		return new UCRData(clusternumber, ts);
	}

	private double extractValue(String[] split, int i) {
		double test;
		if(split[i].contains("e")){
			String[] split2 = split[i].split("e");
			test = Double.valueOf(split2[0])*Math.pow(10, Double.valueOf(split2[1]));
		} else{
			test = Double.valueOf(split[i]);
		}
		return test;
	}

	@Override
	public LinkedList<TimeSeries> getTimeSeriesList(){
//		// Check the correctness of file address.
//		if(!checkFileNameCorrectness("TEST"))
//			return null;
		// Extracting time series
		LinkedList<TimeSeries> ts= new LinkedList<TimeSeries>();
		// Iterate through test data
		while(true){
			TimeSeries temp = getUCRData().timeSeries();
			if(temp == null)
				break;
			ts.add(temp);
		}
		return ts;
	}
	
	@Override
	public LinkedList<TimeSeries> getTimeSeriesList(String address,String filename, String arg){
		fagent.updatereadingpath(address+ filename +"\\"+ filename +"_" + arg);
		// Check the correctness of file address.
		if(!checkFileNameCorrectness(arg))
			return null;
		return getTimeSeriesList();
	}
	
	@Override
	public LinkedList<UCRData> getUCRDataList(){
		// Extracting time series
		LinkedList<UCRData> list= new LinkedList<UCRData>();
		// Iterate through test data
		while(true){
			UCRData temp = getUCRData();
			if(temp == null)
				break;
			list.add(temp);
		}
		return list;
	}

	@Override
	public LinkedList<UCRData> getUCRDataList(String address,String filename, String arg){
		fagent.updatereadingpath(address+ filename +"\\"+ filename +"_" + arg);
		// Check the correctness of file address.
		if(!checkFileNameCorrectness(arg))
			return null;
		return getUCRDataList();
	}
	
	
	@Override
	public boolean checkFileNameCorrectness(String squence) {
		if(!fagent.readingPath().contains(squence)){
			logger.error("TEST file not specified");
			return false;
		}
		return true;
	}



}
