package mfdr.dimensionality.datastructure;

import java.util.LinkedList;

import mfdr.datastructure.TimeSeries;

public class MFDR_LB_Data extends MFDRWaveData  {

	private TimeSeries residualfull = null;
	
	public MFDR_LB_Data(LinkedList<PLAData> trends,
			DFTData seasonal, double noise_energy_density,
			double residual_mean, double residual_variance) {
		super(trends, seasonal, noise_energy_density);
		this.residual_mean = residual_mean;
		this.residual_variance = residual_variance;
	}
	
	public MFDR_LB_Data(MFDRWaveData wavedata,
			double residual_mean, double residual_variance) {
		super(wavedata);
		this.residual_mean = residual_mean;
		this.residual_variance = residual_variance;
	}
	
	public MFDR_LB_Data(MFDRWaveData wavedata,
			double residual_mean, double residual_variance, TimeSeries residualfull) {
		super(wavedata);
		this.residual_mean = residual_mean;
		this.residual_variance = residual_variance;
		this.residualfull = residualfull;
	}

	private final double residual_mean;
	private final double residual_variance;

	public double residualMean() {
		return this.residual_mean;
	}

	public double residualVariance() {
		return this.residual_variance;
	}

	public TimeSeries residualFull(){
		return this.residualfull;
	}
}
