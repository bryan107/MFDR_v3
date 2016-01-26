package experiment.utility;

public class NoCObject {

	private final int NoC_t;
	private final double errorvalue;
	
	public NoCObject(int NoC_t, double errorvalue){
		this.NoC_t = NoC_t;
		this.errorvalue = errorvalue;
	}
	
	public int NoC_t(){
		return NoC_t;
	}
	
	public double errvalue(){
		return errorvalue;
	}
	
	
}
