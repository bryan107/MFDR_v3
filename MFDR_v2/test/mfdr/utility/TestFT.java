package mfdr.utility;

import java.util.LinkedList;

import mfdr.datastructure.Data;

import org.jtransforms.fft.DoubleFFT_1D;

import junit.framework.TestCase;

public class TestFT extends TestCase {

	public void test(){
		LinkedList<Data> residual = new LinkedList<Data>();
		generateResidual(residual, 10, 0.5, 128);
		double[] valuearray = DataListOperator.getInstance().linkedListToArray(residual, (short)1);
		DoubleFFT_1D fft = new DoubleFFT_1D(valuearray.length);
		
		System.out.println("VALUE");
		Print.getInstance().printArray(valuearray, valuearray.length);
		
		fft.realForward(valuearray);
		System.out.println("FREQ");
		Print.getInstance().printArray(valuearray, valuearray.length);

		fft.realInverse(valuearray, true);
		System.out.println("VALUE");
		Print.getInstance().printArray(valuearray, valuearray.length);

		
	}
	
	private void generateResidual(LinkedList<Data> residual,
			double trendvariation, double noisevariation, long size) {
		for (double i = 0; i < size; i += 1) {
			java.util.Random r = new java.util.Random();
			double noise = r.nextGaussian() * Math.sqrt(noisevariation);
			double trend = trendvariation * Math.pow(i, 0.5);
//			if (i > size / 2) {
//				trend = -trend;
//			}
			// double value = trendvariation*noise;
			double value = trend * Math.sin(i * Math.PI / 12) ;
			// double value = trend * Math.sin(Math.pow(i, 3))+ noise;
			// double value = 9.5 * Math.sin(i*Math.PI / 3) + 4.5 *
			// Math.cos(i*Math.PI / 6) + noise + trend;
			residual.add(new Data(i, value));
		}
	}
}
