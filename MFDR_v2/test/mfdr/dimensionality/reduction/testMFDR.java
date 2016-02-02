package mfdr.dimensionality.reduction;

import java.util.LinkedList;

import mfdr.datastructure.Data;
import mfdr.datastructure.TimeSeries;
import mfdr.distance.Distance;
import mfdr.distance.EuclideanDistance;
import mfdr.math.emd.utility.DataListCalculator;
import junit.framework.TestCase;

public class testMFDR extends TestCase {

	
	public void test1(){
		MFDR mfdr = new MFDR(1, 1);
		Distance distance = new EuclideanDistance();
		TimeSeries ts1 = new TimeSeries();
		TimeSeries ts2 = new TimeSeries();
		generateResidual(ts1, 3, 0.30, 128);
		generateResidual(ts2, -2, 0.30, 128);
		System.out.println("Origin: " + distance.calDistance(ts1, ts2, ts1));
		System.out.println("MFDR: " + mfdr.getDistance(ts1, ts2, distance));
		System.out.println("MFDR-LB: " + mfdr.getLowerBoundDistance(ts1, ts2, distance));
		System.out.println("MFDR-FULL: " + mfdr.getDistanceBruteForce(ts1, ts2, distance));
	}
	
	private TimeSeries getTimeSeries(double trend, double freq, int length){
		TimeSeries ts = new TimeSeries();
		for (int i = 0 ; i < length ; i++){
			double data = trend* 0.1 + Math.cos(freq) + 0.2 * Math.cos(freq*2);
			ts.add(new Data(i, data));
		}
		return ts;
	}
	
	private double generateResidual(LinkedList<Data> residual, double trendvariation, double noisevariation , long size) {
		for (double i = 0; i < size; i+=1) {
			java.util.Random r = new java.util.Random();
			double noise = r.nextGaussian() * Math.sqrt(noisevariation);
			double trend = trendvariation * Math.pow(i, 0.5);
			if(i > size/2){
				trend = -trend;
			}
			double value = 9.5 * Math.sin(i*Math.PI / 3) + 4.5 * Math.cos(i*Math.PI / 6)  + noise + trend;
//			double value = noise + trend;
			residual.add(new Data(i, value));
		}
		return (double)1/6;
//		return 1/(2*Math.PI*3);
	}
}
