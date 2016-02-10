package experiment.thread;

import experiment.core.RepresentationErrorParameterCore;
import junit.framework.TestCase;

public class ExperimentContainer6 extends TestCase {

	private int listnumber = 2;
	private final String readaddress = "C:\\TEST\\MFDR\\UCR_TS_Archive_2015\\";
	private String writeaddress = "C:\\TEST\\MFDR\\Experiment\\MFDRParameter\\list_" + listnumber + "_";
	private String listaddress = "C:\\TEST\\MFDR\\UCR_TS_Archive_2015\\list_" + listnumber + ".txt";
	
	public void test(){
		RepresentationErrorParameterCore core = new RepresentationErrorParameterCore();
		core.run(readaddress, writeaddress, listaddress, 2, 1, 11, 0);
		
		listnumber = 4;
		writeaddress = "C:\\TEST\\MFDR\\Experiment\\MFDRParameter\\list_" + listnumber + "_";
		listaddress = "C:\\TEST\\MFDR\\UCR_TS_Archive_2015\\list_" + listnumber + ".txt";
		core.run(readaddress, writeaddress, listaddress, 2, 1, 11, 0);
	}
}
