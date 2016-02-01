package experiment.utility;

import java.util.LinkedList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.Logger;

import mfdr.datastructure.TimeSeries;
import mfdr.file.FileAccessAgent;

public class DataParser_BAK {
	private static Log logger = LogFactory.getLog(DataParser_BAK.class);
	private UCRParser core;
	private FileAccessAgent fagent;
	
	public DataParser_BAK(UCRParser fs, FileAccessAgent fagent){
		updateFileStructure(fs);
		updateFileAccessAgent(fagent);
	}
	
	public void updateFileStructure(UCRParser core){
		this.core = core;
	}
	
	public void updateFileAccessAgent(FileAccessAgent fagent){
		this.fagent = fagent;
	}
	
	/**
	 * Store time series data from file to a TimeSeries object
	 * return null when no time series can be extracted from file	
	 * @return TimeSeries
	 */
	public UCRData getTimeSeriesDetails(){
		return core.getUCRData();
	}
	
	
	/**
	 * Store time series data from file to a TimeSeries object with updated reading path
	 * @param readingpath
	 * @return TimeSeries
	 */
	public UCRData getTimeSeriesDetails(String readingpath){
		fagent.updatereadingpath(readingpath);
		return getTimeSeriesDetails();
	}
	
	/**
	 * Extract all Time Series from TEST class from current specified File.
	 * This function is less encouraged to use as 
	 * @param 
	 * @return LinkedList<TimeSeries>
	 */
	public LinkedList<TimeSeries> getTimeSeriesList(){
		// Extracting time series
		LinkedList<TimeSeries> ts= new LinkedList<TimeSeries>();
		// Iterate through test data
		while(true){
			TimeSeries temp = getTimeSeriesDetails().timeSeries();
			if(temp == null)
				break;
			ts.add(temp);
		}
		return ts;
	}
	
	/**
	 * Extract all Time Series from TEST class from specified File.
	 * This is 
	 * @param address, filename, arg (optional type indicator)
	 * @return LinkedList<TimeSeries>
	 */
	public LinkedList<TimeSeries> getTimeSeriesList(String address, String filename, String arg){
		fagent.updatereadingpath(address + filename +"\\"+ filename +"_" + arg);
		return getTimeSeriesList();
	}
	

	
	
	
	/**
	 * Store time series data from between specific segment	
	 * @return TimeSeries
	 */
//	public TimeSeries getTimeSeries(double start, double end){
//		double time = 0;
//		TimeSeries ts = new TimeSeries();
//		// store  
//		String line = fagent.readLineFromFile();
//		while(line != null){
//			if(time < start){
//				time++;
//				continue;
//			} else if(time > end){
//				break;
//			}
//			fs.perTimeSeriesExtraction(ts, line, time);
//			time++;
//			line = fagent.readLineFromFile();
//		}
//		return ts;
//	}
	
	/**
	 * Store time series data from between specific segment with updated reading path
	 * @param readingpath
	 * @return TimeSeries
	 */
//	public TimeSeries getTimeSeries(double start, double end, String readingpath){
//		TimeSeries ts = new TimeSeries();
//		fagent.updatereadingpath(readingpath);
//		getTimeSeries(start, end);
//		return ts;
//	}
}
