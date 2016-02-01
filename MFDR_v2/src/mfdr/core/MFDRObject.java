package mfdr.core;

import mfdr.dimensionality.datastructure.MFDRWaveData;

public class MFDRObject {

	private final int NoC_t;
	private final int NoC_s;
	private final double lowestperiod;
	private final MFDRWaveData data;
	private final double err;
	private final long nanotime;
	
	public MFDRObject(int NoC_t, int NoC_s, double lowestperiod, MFDRWaveData data, double err, long nanotime){
		this.NoC_t = NoC_t;
		this.NoC_s = NoC_s;
		this.lowestperiod = lowestperiod;
		this.data = data;
		this.err = err;
		this.nanotime = nanotime;
	}
	
	public int NoC_t(){
		return this.NoC_t;
	}
	
	public int NoC_s(){
		return this.NoC_s;
	}
	
	public double lowestPeriod(){
		return this.lowestperiod;
	}
	
	public MFDRWaveData data(){
		return this.data;
	}
	
	public double error(){
		return this.err;
	}
	
	public long nanoTime(){
		return this.nanotime;
	}
}
