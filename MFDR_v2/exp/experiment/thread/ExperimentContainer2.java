package experiment.thread;

import experiment.core.ClosenessOfDistanceCore;
import junit.framework.TestCase;

public class ExperimentContainer2 extends TestCase {

	private int listnumber = 1;
	private final String readaddress = "C:\\TEST\\MFDR\\UCR_TS_Archive_2015\\";
	private String writeaddress = "C:\\TEST\\MFDR\\Experiment\\Closeness\\list_" + listnumber + "_";
	private String listaddress = "C:\\TEST\\MFDR\\UCR_TS_Archive_2015\\list_" + listnumber + ".txt";
	
	public void test(){
		ClosenessOfDistanceCore core = new ClosenessOfDistanceCore();
		core.run(readaddress, writeaddress, listaddress, 2, 1, 11, 0);
		
		listnumber = 2;
		writeaddress = "C:\\TEST\\MFDR\\Experiment\\Closeness\\list_" + listnumber + "_";
		listaddress = "C:\\TEST\\MFDR\\UCR_TS_Archive_2015\\list_" + listnumber + ".txt";
		core.run(readaddress, writeaddress, listaddress, 2, 1, 11, 0);
		
		listnumber = 3;
		writeaddress = "C:\\TEST\\MFDR\\Experiment\\Closeness\\list_" + listnumber + "_";
		listaddress = "C:\\TEST\\MFDR\\UCR_TS_Archive_2015\\list_" + listnumber + ".txt";
		core.run(readaddress, writeaddress, listaddress, 2, 1, 11, 0);
		
		listnumber = 4;
		writeaddress = "C:\\TEST\\MFDR\\Experiment\\Closeness\\list_" + listnumber + "_";
		listaddress = "C:\\TEST\\MFDR\\UCR_TS_Archive_2015\\list_" + listnumber + ".txt";
		core.run(readaddress, writeaddress, listaddress, 2, 1, 11, 0);
	}
}
