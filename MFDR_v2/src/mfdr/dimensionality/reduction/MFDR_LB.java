package mfdr.dimensionality.reduction;

import java.util.LinkedList;

import org.jtransforms.fft.DoubleFFT_1D;

import flanagan.analysis.Stat;
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

	public MFDR_LB(int NoC_t, int NoC_s, double LB_num_dev) {
		super(NoC_t, NoC_s);
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
	 residualfull = DataListCalculator.getInstance().getDifference(residualfull,seasonalfull);
	 // Prepare LB_Data
	 double residual_mean =	 Stat.mean(DataListOperator.getInstance().linkedListToArray(residualfull,(short)1));
	 double residual_variance =	 Stat.variance(DataListOperator.getInstance().linkedListToArray(residualfull,(short)1));
	 return new MFDR_LB_Data(data, residual_mean, residual_variance);
	 }

	 @Override
	 public MFDR_LB_Data getDR(TimeSeries ts, double lowestperiod) {
		 MFDRWaveData data = super.getDR(ts, lowestperiod);
		 // Get full resolution residual
		 // Aware that trend and white noise have been removed from residual.
		 TimeSeries residualfull = data.getMeta().getResidualFull();
		 TimeSeries seasonalfull = dft.getFullResolutionDR(data.seasonal(), ts);
		 // Compute the residual
		 residualfull = DataListCalculator.getInstance().getDifference(residualfull,seasonalfull);
		 // Prepare LB_Data
		 double residual_mean =	 Stat.mean(DataListOperator.getInstance().linkedListToArray(residualfull,(short)1));
		 double residual_variance =	 Stat.variance(DataListOperator.getInstance().linkedListToArray(residualfull,(short)1));
		 return new MFDR_LB_Data(data, residual_mean, residual_variance);
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
		return MFDR_dist + computeCompensation(mfdrdata1, mfdrdata2, size);
	}
	
	// TODO To complete this function.
	private double computeCompensation(MFDR_LB_Data mfdrdata1, MFDR_LB_Data mfdrdata2,
			int size){
		return 0;
	}

}
