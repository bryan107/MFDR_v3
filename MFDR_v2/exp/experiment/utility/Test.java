package experiment.utility;

import java.util.LinkedList;

import mfdr.datastructure.Data;
import mfdr.datastructure.TimeSeries;
import mfdr.file.FileAccessAgent;
import mfdr.utility.File;
import junit.framework.TestCase;

public class Test extends TestCase {

	public void testWholeFile(){
		LinkedList<String> filenamelist = new LinkedList<String>();
		FileAccessAgent fagent = new FileAccessAgent("C:\\TEST\\MFDR\\NULL.txt", "C:\\TEST\\MFDR\\NULL.txt");
		fagent.updatereadingpath("C:\\TEST\\MFDR\\UCR_TS_Archive_2015\\test_list.txt");
		while(true){
			String filename = fagent.readLineFromFile();
			if(filename == null){
				break;
			}
			filenamelist.add(filename);
		}
		UCRParser parser = new UCRParser2015(fagent);
		for(int i = 0 ; i < filenamelist.size() ; i++){
			String filename = filenamelist.get(i);
			fagent.updatereadingpath("C:\\TEST\\MFDR\\UCR_TS_Archive_2015\\"+ filename +"\\"+ filename +"_TEST");
//			LinkedList<TimeSeries> ts= new LinkedList<TimeSeries>();
			LinkedList<TimeSeries> ts= parser.getTimeSeriesList();
			int count = 0;
			while(true){

//				TimeSeries temp = parser.getUCRData().timeSeries();
//				if(temp == null)
//					break;
//				ts.add(temp);
				File.getInstance().saveLinkedListToFile("Series " + count, ts.get(count), "C:\\TEST\\MFDR\\Data\\DATA_NEW_2015\\"+ filename +"_list.csv");
				count ++;
				if(count >= ts.size())
					break;
			}
		}	
	}
	
//	public void testSingle(){
//		FileAccessAgent fagent = new FileAccessAgent("C:\\TEST\\MDFR\\Null.txt", "C:\\TEST\\MDFR\\Null.txt");
//		fagent.updatereadingpath("C:\\TEST\\MDFR\\Data\\TRY.csv");
//		DataParser_BAK parser = new DataParser_BAK(new OneLineOneDataParserCore(), fagent);
//		TimeSeries temp = parser.getTimeSeriesDetails().timeSeries();
//		File.getInstance().saveLinkedListToFile("USD/EUR", temp, "C:\\TEST\\MDFR\\Data\\DATA5\\TRY.csv");
//	}
}
