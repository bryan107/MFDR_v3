package experiment.utility;

import java.util.LinkedList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import mfdr.datastructure.Data;
import mfdr.datastructure.TimeSeries;
import mfdr.file.FileAccessAgent;

public class UCRParser2015 implements UCRParser {
	private static Log logger = LogFactory.getLog(UCRParser2015.class);
	FileAccessAgent fagent;

	public UCRParser2015(FileAccessAgent fagent) {
		updateFileAccessAgent(fagent);
	}

	@Override
	public void updateFileAccessAgent(FileAccessAgent fagent) {
		this.fagent = fagent;
	}

	@Override
	public UCRData getUCRData() {
		int clusternumber = -1;
		TimeSeries ts = new TimeSeries();
		String line = fagent.readLineFromFile();
		if (line == null) {
			logger.info("Extraction from " + fagent.readingPath()
					+ " is completed");
			return new UCRData(-1, null);
		}
		int count = -1;
		String[] split = line.split(",");
		for (int i = 0; i < split.length; i++) {
			count++;
			if (count == 0) {
				clusternumber = (int) extractValue(split, i);
				continue;
			}
			try {
				double test = extractValue(split, i);
				ts.add(new Data(count, test));
			} catch (Exception e) {
				logger.info("split:" + split[i] + e);
			}
		}
		return new UCRData(clusternumber, ts);
	}

	private double extractValue(String[] split, int i) {
		return Double.valueOf(split[i]);
	}

	@Override
	public LinkedList<TimeSeries> getTimeSeriesList() {
		// // Check the correctness of file address.
		// if(!checkFileNameCorrectness("TEST"))
		// return null;
		// Extracting time series
		LinkedList<TimeSeries> ts = new LinkedList<TimeSeries>();
		// Iterate through test data
		while (true) {
			TimeSeries temp = getUCRData().timeSeries();
			if (temp == null)
				break;
			ts.add(temp);
		}
		return ts;
	}

	@Override
	public LinkedList<TimeSeries> getTimeSeriesList(String address,
			String filename, String arg) {
		fagent.updatereadingpath(address + filename + "\\" + filename + "_"
				+ arg);
		// Check the correctness of file address.
		if (!checkFileNameCorrectness(arg))
			return null;
		return getTimeSeriesList();
	}

	@Override
	public LinkedList<UCRData> getUCRDataList() {
		// Extracting time series
		LinkedList<UCRData> list = new LinkedList<UCRData>();
		// Iterate through test data
		while (true) {
			UCRData temp = getUCRData();
			if (temp.timeSeries() == null)
				break;
			list.add(temp);
		}
		return list;
	}

	@Override
	public LinkedList<UCRData> getUCRDataList(String address, String filename,
			String arg) {
		fagent.updatereadingpath(address + filename + "\\" + filename + "_"
				+ arg);
		// Check the correctness of file address.
		if (!checkFileNameCorrectness(arg))
			return null;
		return getUCRDataList();
	}
	
	/**
	 * Return both TRAIN and TEST data with UCRData structure
	 * @param address
	 * @param filename
	 * @return LinkedList<UCRData>
	 */
	public LinkedList<UCRData> getUCRDataListALL(String address, String filename) {
		LinkedList<UCRData> list = new LinkedList<UCRData>();
		fagent.updatereadingpath(address + filename + "\\" + filename + "_TRAIN");
		list = getUCRDataList();
		fagent.updatereadingpath(address + filename + "\\" + filename + "_TEST");
		list.addAll(getUCRDataList());
		return list;
	}
	
	/**
	 * Return both TRAIN data with UCRData structure
	 * @param address
	 * @param filename
	 * @return LinkedList<UCRData>
	 */
	public LinkedList<UCRData> getUCRDataListTrain(String address, String filename) {
		LinkedList<UCRData> list = new LinkedList<UCRData>();
		fagent.updatereadingpath(address + filename + "\\" + filename + "_TRAIN");
		list = getUCRDataList();
		return list;
	}
	
	/**
	 * Return both TRAIN and TEST data with UCRData structure
	 * @param address
	 * @param filename
	 * @return LinkedList<UCRData>
	 */
	public LinkedList<UCRData> getUCRDataListTest(String address, String filename) {
		LinkedList<UCRData> list = new LinkedList<UCRData>();
		fagent.updatereadingpath(address + filename + "\\" + filename + "_TEST");
		list = getUCRDataList();
		return list;
	}

	@Override
	public boolean checkFileNameCorrectness(String squence) {
		if (!fagent.readingPath().contains(squence)) {
			logger.error("TEST file not specified");
			return false;
		}
		return true;
	}

	@Override
	public LinkedList<String> getFileNameList(FileAccessAgent fagent,
			String filelistaddress) {
		LinkedList<String> filenamelist = new LinkedList<String>();
		fagent.updatereadingpath(filelistaddress);
		while (true) {
			String filename = fagent.readLineFromFile();
			if (filename == null) {
				break;
			}
			filenamelist.add(filename);
		}
		return filenamelist;
	}

	@Override
	public LinkedList<TimeSeries> getTimeSeriesListTest(FileAccessAgent fagent,
			String address, String filename) {
		UCRParser parser = new UCRParser2015(fagent);
		// Update
		fagent.updatereadingpath(address + filename + "\\" + filename + "_TEST");
		LinkedList<TimeSeries> ts = parser.getTimeSeriesList();
		return ts;
	}

	@Override
	public LinkedList<TimeSeries> getTimeSeriesListTrain(
			FileAccessAgent fagent, String address, String filename) {
		UCRParser parser = new UCRParser2015(fagent);
		fagent.updatereadingpath(address + filename + "\\" + filename
				+ "_TRAIN");
		LinkedList<TimeSeries> ts = parser.getTimeSeriesList();
		return ts;
	}

	@Override
	public LinkedList<TimeSeries> getTimeSeriesListALL(FileAccessAgent fagent,
			String address, String filename) {
		UCRParser parser = new UCRParser2015(fagent);
		// Get time series from TRAIN file
		fagent.updatereadingpath(address + filename + "\\" + filename + "_TRAIN");
		LinkedList<TimeSeries> ts = parser.getTimeSeriesList();
		// Get and attach time series from TEST file
		fagent.updatereadingpath(address + filename + "\\" + filename + "_TEST");
		ts.addAll(parser.getTimeSeriesList());
		return ts;
	}

}
