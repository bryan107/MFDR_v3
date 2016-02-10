package experiment.core;

import java.util.LinkedList;

import org.jtransforms.fft.DoubleFFT_1D;

import mfdr.datastructure.Data;
import mfdr.datastructure.TimeSeries;
import mfdr.dimensionality.reduction.DFT;
import mfdr.dimensionality.reduction.PLA;
import mfdr.file.FileAccessAgent;
import mfdr.utility.DataListOperator;
import mfdr.utility.File;
import mfdr.utility.Print;
import junit.framework.TestCase;

public class DRExample extends TestCase {

	String writeaddress = "C:\\TEST\\MFDR\\Experiment\\Examples\\";
	
	public void test(){
		TimeSeries residual = new TimeSeries();
		PLA pla = new PLA(1);
		DFT dft = new DFT(1);
		generateResidual(residual, 0.5, 128);
		
		System.out.println("PLA_1:" + (residual.energyDensity()-pla.getFullResolutionDR(residual).energyDensity()) / residual.energyDensity());
		System.out.println("DFT_1:" + (residual.energyDensity()-dft.getFullResolutionDR(residual).energyDensity()) / residual.energyDensity());
		double[] valuearray = DataListOperator.getInstance().linkedDataListToArray(residual)[1];
		
		String outputstring = "Time domain";
		File.getInstance().saveArrayToFile(outputstring, valuearray, writeaddress + "Trend.csv");
		
		DoubleFFT_1D fft = new DoubleFFT_1D(valuearray.length);
		fft.realForward(valuearray);
		
		outputstring = "Frequency Domain";
		File.getInstance().saveArrayToFile(outputstring, valuearray, writeaddress + "Trend.csv");
		TimeSeries residual2 = new TimeSeries();

		generateResidualFreq(residual2, 0.5, 128);
		
		System.out.println("PLA_2:" + (residual2.energyDensity()-pla.getFullResolutionDR(residual2).energyDensity()) / residual2.energyDensity());
		System.out.println("DFT_2:" + (residual2.energyDensity()-dft.getFullResolutionDR(residual2).energyDensity()) / residual2.energyDensity());
		
		valuearray = DataListOperator.getInstance().linkedDataListToArray(residual2)[1];
		outputstring = "Time domain";
		File.getInstance().saveArrayToFile(outputstring, valuearray, writeaddress + "Trend.csv");
		
		fft = new DoubleFFT_1D(valuearray.length);
		fft.realForward(valuearray);
		
		outputstring = "Frequency Domain";
		File.getInstance().saveArrayToFile(outputstring, valuearray, writeaddress + "Trend.csv");
	}
	
	private void generateResidual(LinkedList<Data> residual, double trendvariation, long size) {
		double value = 0;
		for (double i = 0; i < size; i += 1) {
			value = trendvariation * Math.exp(2+i*0.02) ;
			residual.add(new Data(i, value));
		}
	}
	
	private void generateResidualFreq(LinkedList<Data> residual,double variation, long size) {
		for (double i = 0; i < size; i += 1) {
			double value = variation * Math.sin(i * Math.PI / 12) ;
			residual.add(new Data(i, value));
		}
	}
}
