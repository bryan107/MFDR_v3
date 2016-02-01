package experiment.thread;

import experiment.core.RepresentationErrorParameterCore;
import junit.framework.TestCase;

public class ExperimentContainer6 extends TestCase {

	private final int listnumber = 1;
	private final String readaddress = "C:\\TEST\\MFDR\\UCR_TS_Archive_2015\\";
	private String writeaddress = "C:\\TEST\\MFDR\\Experiment\\MFDRParameter\\list_" + listnumber + "_";
	private final String listaddress = "C:\\TEST\\MFDR\\UCR_TS_Archive_2015\\list_" + listnumber + ".txt";
	
	public void test(){
		RepresentationErrorParameterCore core = new RepresentationErrorParameterCore();
		core.run(readaddress, writeaddress, listaddress, 2, 2, 10, 0);
	}
}
