package mfdr.dimensionality.datastructure;

import java.util.LinkedList;

public class MFDR_LB_Data extends MFDRWaveData  {

	public MFDR_LB_Data(LinkedList<PLAData> trends,
			LinkedList<DFTWaveData> seasonal, double noise_energy_density,
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

	private final double residual_mean;
	private final double residual_variance;

	public double residualMean() {
		return this.residual_mean;
	}

	public double residualVariance() {
		return this.residual_variance;
	}

}
