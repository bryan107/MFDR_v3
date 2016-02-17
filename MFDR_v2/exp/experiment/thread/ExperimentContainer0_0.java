package experiment.thread;

import experiment.core.ClosenessOfDistanceCore;
import experiment.core.ExperimentCore;
import experiment.core.RepresentationErrorParameterCore;
import junit.framework.TestCase;

public class ExperimentContainer0_0 extends TestCase {

	private final int listnumber = 0;
	private final String readaddress = "C:\\TEST\\MFDR\\UCR_TS_Archive_2015\\";
	private String writeaddress = "C:\\TEST\\MFDR\\Experiment\\Closeness\\list_" + listnumber + "_";
//	private final String listaddress = "C:\\TEST\\MFDR\\UCR_TS_Archive_2015\\list_" + listnumber + ".txt";
	private final String listaddress = "C:\\TEST\\MFDR\\UCR_TS_Archive_2015\\test_list.txt";
	public void test(){
		ExperimentCore core = new RepresentationErrorParameterCore();
		core.run(readaddress, writeaddress, listaddress, 4, 1, 5, 0);
	}
}
