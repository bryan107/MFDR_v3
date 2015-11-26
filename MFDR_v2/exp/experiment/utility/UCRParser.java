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
	 * Read the file names from a list file.
	 * @param fagent
	 * @param filelistaddress
	 * @return LinkedList<String>
	 */
	public LinkedList<String> getFileNameList(FileAccessAgent fagent,String filelistaddress);
	
	/**
	 * Return a LinkedList <TimeSeries>
	 * When null, return null
	 * @param fagent
	 * @return LinkedList<TimeSeries>
	 */
	public LinkedList<TimeSeries> getTimeSeriesList();

	/**
	 * Return a LinkedList <TimeSeries>
	 * When null, return null
	 * @param fagent
	 * @return LinkedList<TimeSeries>
	 */
	public LinkedList<TimeSeries> getTimeSeriesList(String address,String filename, String arg);
	
	/**
	 * Return Return a LinkedList <TimeSeries> from UCR Test files. The file name dose not have to include "TEST"
	 * @param fagent
	 * @param address
	 * @param filename
	 * @return LinkedList<TimeSeries>
	 */
	public LinkedList<TimeSeries> getTimeSeriesListTest(FileAccessAgent fagent ,String address, String filename);
	
	/**
	 * Return Return a LinkedList <TimeSeries> from UCR Train files. The file name dose not have to include "TRAIN"
	 * @param fagent
	 * @param address
	 * @param filename
	 * @return LinkedList<TimeSeries>
	 */
	public LinkedList<TimeSeries> getTimeSeriesListTrain(FileAccessAgent fagent ,String address,String filename);
	
	/**
	 * Return Return a LinkedList <TimeSeries> from both UCR Test and Train files. 
	 * The file name dose not have to include "TRAIN" or "TEST"
	 * @param fagent
	 * @param address
	 * @param filename
	 * @return LinkedList<TimeSeries>
	 */
	public LinkedList<TimeSeries> getTimeSeriesListALL(FileAccessAgent fagent ,String address,String filename);
	
	
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
