package experiment.thread;

import experiment.core.ExperimentCore;
import experiment.core.KNNCore;
import experiment.core.RepresentationErrorParameterCore;
import junit.framework.TestCase;

public class ExperimentContainer5 extends TestCase {
	private final String readaddress = "C:\\TEST\\MFDR\\UCR_TS_Archive_2015\\";
	private String writeaddress;
	private String listaddress;
	
	public void test(){
		ExperimentCore core = new KNNCore();
		for(int listnumber = 4 ; listnumber <5 ; listnumber++){
			for(int k = 5; k < 6 ; k++){
				writeaddress = "C:\\TEST\\MFDR\\Experiment\\KNN\\";
				listaddress = "C:\\TEST\\MFDR\\UCR_TS_Archive_2015\\Full_No_Ele.txt";
				core.run(readaddress, writeaddress, listaddress, 2, 1, 6, k);
			}
		}
	}
}
