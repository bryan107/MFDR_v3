package mfdr.dimensionality.datastructure;

public class MFDRObject {

	private final int NoC_t;
	private final int NoC_s;
	private final double lowestperiod;
	private final MFDRWaveData data;
	private final double err;
	
	public MFDRObject(int NoC_t, int NoC_s, double lowestperiod, MFDRWaveData data, double err){
		this.NoC_t = NoC_t;
		this.NoC_s = NoC_s;
		this.lowestperiod = lowestperiod;
		this.data = data;
		this.err = err;
	}
	
	public int NoC_t(){
		return this.NoC_t;
	}
	
	public int NoC_s(){
		return this.NoC_s;
	}
	
	public double lowestperiod(){
		return this.lowestperiod;
	}
	
	public MFDRWaveData data(){
		return this.data;
	}
	
	public double error(){
		return this.err;
	}
}
