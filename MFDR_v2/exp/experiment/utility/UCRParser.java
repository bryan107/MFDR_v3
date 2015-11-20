package experiment.utility;

import java.util.LinkedList;

import mfdr.datastructure.TimeSeries;
import mfdr.file.FileAccessAgent;

public interface UCRParser {

	/**
	 * Update File Access Agent
	 * @param fagent
	 * @return 
	 */
	public void updateFileAccessAgent(FileAccessAgent fagent);
	
	/**
	 * Return a UCRData Object
	 * When null, clusterNumber() = -1 and timeSeries() = null
	 * @param fagent
	 * @return UCRData
	 */
//	public UCRData perTimeSeriesExtraction(FileAccessAgent fagent);
	
	/**
	 * Return a UCRData Object
	 * When null, clusterNumber() = -1 and timeSeries() = null
	 * @param fagent
	 * @return UCRData
	 */
	public UCRData getUCRData();
	
//	/**
//	 * Return a UCRData <List Object>
//	 * When null, return null
//	 * @param fagent
//	 * @return LinkedList<UCRData>
//	 */
//	public LinkedList<TimeSeries> getTimeSeriesList();
	
	/**
	 * Return a LinkedList <TimeSeries>
	 * When null, return null
	 * @param fagent
	 * @return LinkedList<UCRData>
	 */
	public LinkedList<TimeSeries> getTimeSeriesList();

	/**
	 * Return a LinkedList <TimeSeries>
	 * When null, return null
	 * @param fagent
	 * @return LinkedList<UCRData>
	 */
	public LinkedList<TimeSeries> getTimeSeriesList(String address,String filename, String arg);
	
	/**
	 * Return a LinkedList <UCRData>
	 * When null, return null
	 * @param fagent
	 * @return LinkedList<UCRData>
	 */
	public LinkedList<UCRData> getUCRDataList();
	
	/**
	 * Return a LinkedList <UCRData>
	 * When null, return null
	 * @param fagent
	 * @return LinkedList<UCRData>
	 */
	public LinkedList<UCRData> getUCRDataList(String address,String filename, String arg);
	
	/**
	 * Check if the reading path of current file agent contains the specified sequence.
	 * @param readingpath, sequence
	 * @return boolean
	 */
	public boolean checkFileNameCorrectness(String squence);
}
