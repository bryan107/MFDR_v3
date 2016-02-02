package experiment.test;

import mfdr.core.BruteForceNoCCalculator;
import mfdr.core.MFDRWithParameter;
import junit.framework.TestCase;

public class TestMFERWithParameter extends TestCase {

	public void test(){
		MFDRWithParameter mfdr_p = new MFDRWithParameter();
		System.out.println("name= " + mfdr_p.name());
		mfdr_p.updateNoCCalculator(new BruteForceNoCCalculator());
		System.out.println("name= " + mfdr_p.name());
		
	}
	
}
