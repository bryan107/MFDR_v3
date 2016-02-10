package mfdr.dimensionality.datastructure;

public class PLAAData extends PLAData{
	private final boolean isPLA;
	
	/**
	 * PLAA Data contains three parameters
	 * - type: type of data structure, true = PLA, false = PAA
	 * - time: the starting time of a given window
	 * - a0: the constant of the linear representation
	 * - a1: the parameter of  x.
	 * @param type
	 * @param time
	 * @param a0
	 * @param a1
	 */
	public PLAAData(double time, double a0, double a1 , boolean isPLA){
		super(time, a0, a1);
		this.isPLA = isPLA;
	}
	
	public PLAData getPLAData(){
		return self();
	}
	
	public boolean isPLA(){
		return isPLA;
	}
	
	public double average(int part){
		if(part == 0)
			return a0;
		else
			return a1;
	}

	
}
