package experiment.utility;

import java.util.LinkedList;

import mfdr.datastructure.TimeSeries;
import mfdr.file.FileAccessAgent;
import junit.framework.TestCase;

public class UCRParser2015SaveFile extends TestCase {

	public void testParse(){
		String listaddress = "C:\\TEST\\MFDR\\UCR_TS_Archive_2015\\dataset_listALL.txt";
		String readaddress = "C:\\TEST\\MFDR\\UCR_TS_Archive_2015\\";
		String writingaddress = "C:\\TEST\\MFDR\\UCR_TS_Archive_2015\\__ParsedData\\";
		String filetype = "";
		
		FileAccessAgent fagent = new FileAccessAgent(
				"C:\\TEST\\MFDR\\NULL.txt", "C:\\TEST\\MFDR\\NULL.txt");
		UCRParser2015 parser = new UCRParser2015(fagent);
		LinkedList<String> filenamelist = parser.getFileNameList(fagent,
				listaddress);
		for (int i = 0; i < filenamelist.size(); i++) {
			filetype = "TRAIN";
			fileConverting(readaddress, writingaddress, fagent, parser,	filenamelist, i, filetype);	
			filetype = "TEST";
			fileConverting(readaddress, writingaddress, fagent, parser,	filenamelist, i, filetype);	
		}
	}

	private void fileConverting(String readaddress, String writingaddress,
			FileAccessAgent fagent, UCRParser2015 parser,
			LinkedList<String> filenamelist, int i, String filetype) {
		LinkedList<UCRData> ucrdatalist = parser.getUCRDataList(readaddress, filenamelist.get(i), filetype);
		fagent.updatewritingpath(writingaddress + filenamelist.get(i) + "_" + filetype + ".csv");
		for(int j = 0 ; j < ucrdatalist.size() ; j++){
			// Prepare writing string
			String writestring = "Series " + j + ",";
			UCRData ucrdata = ucrdatalist.get(j);
			writestring += "[" + ucrdata.ClusterNumber() + "],";
			// Dump time series data into time series
			for(int k = 0 ; k < ucrdata.timeSeries().size() ; k++){
				writestring += ucrdata.timeSeries().get(k).value() + ",";
			}
			fagent.writeLineToFile(writestring);
		}
		System.out.println("Write to " + writingaddress + filenamelist.get(i) + "_" + filetype + ".csv is complete" );
	}
}
