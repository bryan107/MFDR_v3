package experiment.test;

import java.util.LinkedList;

import org.jtransforms.fft.DoubleFFT_1D;

import mfdr.datastructure.Data;
import mfdr.datastructure.TimeSeries;
import mfdr.dimensionality.reduction.DFT;
import mfdr.file.FileAccessAgent;
import mfdr.utility.DataListOperator;
import junit.framework.TestCase;

public class TestNewLowerBound extends TestCase {

	public void test(){
		DFT dft = new DFT(10);
		TimeSeries ts = generateTimeSeries4(1024,4);
		double[] freq = dft.converTSToFrequency(ts);
		
		FileAccessAgent fagent = new FileAccessAgent("C:\\TEST\\MFDR\\Test\\freq.csv", "C:\\TEST\\MFDR\\NULL.txt");
		String outputstring = "";
		LinkedList<Double> data = DataListOperator.getInstance().getValueList(ts);
		for(double item : data){
			outputstring += item + ",";
		}
		fagent.writeLineToFile(outputstring);
		
		outputstring = "";
		for(double item : freq){
			outputstring += item + ",";
		}
		fagent.writeLineToFile(outputstring);


		//----------------------------------
		double candidate = 0;
		int candidateid = 0;
		for (int i = 0 ; i < freq.length ; i++){
			if(freq[i] > candidate){
				candidate = freq[i];
				candidateid = i;
			}
		}
//		freq[candidateid] = 0;
		DoubleFFT_1D fft = new DoubleFFT_1D(freq.length);
		fft.realInverseFull(freq, true);
		
		outputstring = "";
		for(double item : freq){
			outputstring += item + ",";
		}
		fagent.writeLineToFile(outputstring);
	}
	
	private TimeSeries generateTimeSeries(int length){
		TimeSeries ts = new TimeSeries();
		for(int i = 0 ; i < length ; i++){
			double value = (double)i * 0.1;
			ts.add(new Data(i, value));
		}
		return ts;
	}
	private TimeSeries generateTimeSeries2(int length){
		TimeSeries ts = new TimeSeries();
		double value = 0 ;
		for(int i = 0 ; i < length/2 ; i++){
			value = (double)i * 0.1;
			ts.add(new Data(i, value));
		}
		for(int i = length/2 + 1 ; i < length ; i++){
			double value2 = value  - (double)i * 0.15;
			ts.add(new Data(i, value2));
		}
		return ts;
	}
	private TimeSeries generateTimeSeries4(int length, int sec){
		TimeSeries ts = new TimeSeries();
		double value = 0 ;
		double value2 = 0 ;
		double value3 = 0 ;
		double value4 = 0;
		int seclength = length / sec;
		for(int i = 0 ; i < seclength ; i++){
			value = (double)i * 0.1;
			ts.add(new Data(i, value));
		}
		for(int i = seclength + 1 ; i < 2*seclength ; i++){
			value2 = value  - (double)i * 0.15;
			ts.add(new Data(i, value2));
		}
		for(int i = 2*seclength + 1 ; i < 3*seclength ; i++){
			value3 = value2 + (double)i * 0.10;
			ts.add(new Data(i, value3));
		}
		for(int i = 3*seclength + 1 ; i < 4*seclength ; i++){
			value4 = value3 + (double)i * 0.05;
			ts.add(new Data(i, value4));
		}
		return ts;
	}
	
}
