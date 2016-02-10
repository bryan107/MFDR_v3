package experiment.thread;

import experiment.core.ClosenessOfDistanceCore;
import experiment.core.ExperimentCore;
import junit.framework.TestCase;

public class ExperimentContainer0_0 extends TestCase {

	private final int listnumber = 4;
	private final String readaddress = "C:\\TEST\\MFDR\\UCR_TS_Archive_2015\\";
	private String writeaddress = "C:\\TEST\\MFDR\\Experiment\\Closeness\\list_" + listnumber + "_";
	private final String listaddress = "C:\\TEST\\MFDR\\UCR_TS_Archive_2015\\list_" + listnumber + ".txt";
	
	public void test(){
		ClosenessOfDistanceCore core = new ClosenessOfDistanceCore();
		core.run(readaddress, writeaddress, listaddress, 2, 1, 11, 0);
	}
}
