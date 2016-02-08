package mfdr.dimensionality.reduction;

import java.util.LinkedList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jtransforms.fft.DoubleFFT_1D;

import flanagan.analysis.Stat;
import mfdr.datastructure.Data;
import mfdr.datastructure.MFDRDistanceDetails;
import mfdr.datastructure.TimeSeries;
import mfdr.dimensionality.datastructure.DFTWaveData;
import mfdr.dimensionality.datastructure.MFDRWaveData;
import mfdr.dimensionality.datastructure.MFDR_LB_Data;
import mfdr.dimensionality.datastructure.PLAData;
import mfdr.distance.Distance;
import mfdr.math.emd.utility.DataListCalculator;
import mfdr.utility.DataListOperator;

public class MFDR_LB extends MFDR {

	protected double LB_num_dev;
	private static Log logger = LogFactory.getLog(MFDR_LB.class);
	public MFDR_LB(int NoC_t, int NoC_s, double LB_num_dev) {
		super(NoC_t, NoC_s);
		updateNoDeviation(LB_num_dev);
	}

	@Override
	public String name() {
		return "MFDR_LB";
	}

	/* Parameter settings */
	public void updateNoDeviation(double LB_num_dev) {
		this.LB_num_dev = LB_num_dev;
	}

	/**
	 * This function returns the number of deviation set for computing lower
	 * bound.
	 * 
	 * @return
	 */
	public double LBNumDev() {
		return this.LB_num_dev;
	}

	/* ******************************************** */
	/* Getting DR */
	/* ******************************************** */

	 @Override
	 public MFDR_LB_Data getDR(TimeSeries ts) {
	 MFDRWaveData data = super.getDR(ts);
	 // Get full resolution residual
	 // Aware that trend has been removed from residual.
	 TimeSeries residualfull = data.getMeta().getResidualFull();
	 TimeSeries seasonalfull = dft.getFullResolutionDR(data.seasonal(), ts);
	 // Compute the residual
	 TimeSeries q_full = DataListCalculator.getInstance().getDifference(residualfull,seasonalfull);
	 // Prepare LB_Data
	 double q_mean =	 Stat.mean(DataListOperator.getInstance().linkedListToArray(q_full,(short)1));
	 double q_variance =	 Stat.variance(DataListOperator.getInstance().linkedListToArray(q_full,(short)1));
	 return new MFDR_LB_Data(data, q_mean, q_variance, residualfull);
	 }

	 @Override
	 public MFDR_LB_Data getDR(TimeSeries ts, double lowestperiod) {
		 MFDRWaveData data = super.getDR(ts, lowestperiod);
		 // Get full resolution residual
		 // Aware that trend and white noise have been removed from residual.
		 TimeSeries residualfull = data.getMeta().getResidualFull();
		 TimeSeries seasonalfull = dft.getFullResolutionDR(data.seasonal(), ts);
		 // Compute the residual
		 TimeSeries q_full = DataListCalculator.getInstance().getDifference(residualfull,seasonalfull);
		 // Prepare LB_Data
		 double q_mean =	 Stat.mean(DataListOperator.getInstance().linkedListToArray(q_full,(short)1));
		 double q_variance =	 Stat.variance(DataListOperator.getInstance().linkedListToArray(q_full,(short)1));
		 return new MFDR_LB_Data(data, q_mean, q_variance);
	}
	
	@Override
	public double getDistance(TimeSeries ts1, TimeSeries ts2, Distance distance) {
			MFDR_LB_Data mfdrdata1 = getDR(ts1);
			MFDR_LB_Data mfdrdata2 = getDR(ts2);
			// ts1 is used as the sampling reference
			return getDistance(mfdrdata1, mfdrdata2, ts1.size(), distance);
	}
	
	public double getDistance(MFDR_LB_Data mfdrdata1, MFDR_LB_Data mfdrdata2,
			int size, Distance distance) {
		double MFDR_dist = super.getDistance(mfdrdata1, mfdrdata2, size, distance);
		double comp = computeBruteCompensation(mfdrdata1, mfdrdata2, size);
		logger.info("MFDR_Dist=" + MFDR_dist + " COMP=" + Math.sqrt(comp));
		Math.sqrt(Math.pow(MFDR_dist, 2) + comp);
		return Math.sqrt(Math.pow(MFDR_dist, 2) + comp);
	}
	
	// TODO To test this function.
	private double computeCompensation(MFDR_LB_Data mfdrdata1, MFDR_LB_Data mfdrdata2,
			int size){
		int lcm = lcm(mfdrdata1.trends().size(), mfdrdata2.trends().size());
		double windowlength = size / lcm; 
		LinkedList<LinkedList<PLAData>> lcmtrend = getLCMPLADataList(mfdrdata1.trends(), mfdrdata2.trends(), lcm, size);
		
		/* Get T (average of trend1 - trend2) */
		double t = 0;
		for(int index = 0 ; index < lcm ; index++){
			// Compute constant
			double t1_a0 = lcmtrend.get(0).get(index).a0();
			double t2_a0 = lcmtrend.get(1).get(index).a0();
			// Compute scale
			double t1_a1 = lcmtrend.get(0).get(index).a1();
			double t2_a1 = lcmtrend.get(0).get(index).a1();
			t += (t1_a0 - t2_a0) * windowlength + (t1_a1 - t2_a1) * 0.5 * windowlength; 
		}
		// Compute average
		t = t / size;
		
		/* Get Q, q = m +- LB*v */
		double m = mfdrdata1.residualMean() - mfdrdata2.residualMean();
		double v = mfdrdata1.residualVariance() + mfdrdata2.residualVariance();
		
		/* Compute final compensation where K should be as small as possible */
		// If t < 0, which means v should be as big as possible.
		if (t < 0){
			return 2 * size * t * (m + this.LB_num_dev * v);
		} 
		// If t > 0, which means v should be as small as possible.
		else if (t > 0){
			return 2 * size * t * (m - this.LB_num_dev * v);
		} 
		// If t = 0, no compensation is needed.
		else{
			return 0;			
		}
	}
	
	// TODO To test this function.
	private double computeBruteCompensation(MFDR_LB_Data mfdrdata1, MFDR_LB_Data mfdrdata2, int size){
		
		TimeSeries ts = new TimeSeries();
		for(int i = 1 ; i < size+1 ; i++){
			ts.add(new Data(i, 0));
		}
		TimeSeries t1full = pla.getFullResolutionDR(mfdrdata1.trends(), ts);
		TimeSeries t2full = pla.getFullResolutionDR(mfdrdata2.trends(), ts);
		
		TimeSeries f1full = dft.getFullResolutionDR(mfdrdata1.seasonal(), ts);
		TimeSeries f2full = dft.getFullResolutionDR(mfdrdata2.seasonal(), ts);
		
		TimeSeries r1full = mfdrdata1.residualFull();
		TimeSeries r2full = mfdrdata2.residualFull();
		
		TimeSeries q1full = DataListOperator.getInstance().linkedtListSubtraction(r1full, f1full);
		TimeSeries q2full = DataListOperator.getInstance().linkedtListSubtraction(r2full, f2full);
		
		double k = 0;
		for(int i = 0 ; i < ts.size() ; i++){
			k += (t1full.get(i).value() - t2full.get(i).value()) * (q1full.get(i).value() - q2full.get(i).value());
		}
		return 2*k;
	}

}
