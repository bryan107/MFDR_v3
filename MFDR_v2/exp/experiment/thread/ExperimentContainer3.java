package experiment.thread;

import experiment.core.ExperimentCore;
import experiment.core.KNNCore;
import junit.framework.TestCase;

public class ExperimentContainer3 extends TestCase {

	private final int listnumber = 1;
	private final String readaddress = "C:\\TEST\\MFDR\\UCR_TS_Archive_2015\\";
	private String writeaddress = "C:\\TEST\\MFDR\\Experiment\\KNN\\list_" + listnumber + "_";
	private final String listaddress = "C:\\TEST\\MFDR\\UCR_TS_Archive_2015\\list_" + listnumber + ".txt";
	
	public void test(){
		ExperimentCore core = new KNNCore();
		core.run(readaddress, writeaddress, listaddress, 2, 1, 6, 1);
		core.run(readaddress, writeaddress, listaddress, 2, 1, 6, 3);
	}
}
