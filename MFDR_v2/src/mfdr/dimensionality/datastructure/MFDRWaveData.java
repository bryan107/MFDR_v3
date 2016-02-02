package mfdr.dimensionality.datastructure;

import java.util.LinkedList;

import mfdr.datastructure.TimeSeries;
import mfdr.dimensionality.reduction.DFTForMFDR;
import mfdr.dimensionality.reduction.DFTWave;

public class MFDRWaveData {
	private final LinkedList<PLAData> trends;
	private final LinkedList<DFTWaveData> seasonal;
	private final double noise_energy_density;
	private final MFDRWaveDataMeta meta;
	
	public MFDRWaveData(LinkedList<PLAData> trends, LinkedList<DFTWaveData> seasonal, double noise_energy_density){
		this.trends = trends;
		this.seasonal = seasonal;
		this.noise_energy_density = noise_energy_density;
		meta = new MFDRWaveDataMeta();
	}
	
	public MFDRWaveData(MFDRWaveData self){
		this(self.trends, self.seasonal, self.noise_energy_density);
	}
	
	public LinkedList<PLAData> trends(){
		return this.trends;
	}
	
	public LinkedList<DFTWaveData> seasonal(){
		return this.seasonal;
	}
	
	public double noiseEnergyDensity(){
		return this.noise_energy_density;
	}
	
	public MFDRWaveDataMeta getMeta(){
		return meta;
	}
	
	
	//Nested 
	public class MFDRWaveDataMeta{
		private TimeSeries residualfull;
		private double[] residualfreq;
		
		public void setResidualFull(TimeSeries residualfull){
			this.residualfull = residualfull;
		}
		
		public TimeSeries getResidualFull(){
			return this.residualfull;
		}

		public void setResFreq(double[] residualfreq){
			this.residualfreq = residualfreq;
		}
		
		public TimeSeries getResidualWithNoise(){
			DFTForMFDR dft = new DFTForMFDR(1);
			return dft.convertFreqtoTS(residualfreq);
		}
	}
	
}
