package experiment.core;

public class RepresentationErrorResult {

	private final double err_mean, err_variance, time_mean, time_variance;

	
	public RepresentationErrorResult(double err_mean, double err_variance, double time_mean, double time_variance){
		this.err_mean = err_mean;
		this.err_variance = err_variance;
		this.time_mean = time_mean;
		this.time_variance = time_variance;
	}
	
	public double errMean(){
		return this.err_mean;
	}
	
	public double errVariance(){
		return this.err_variance;
	}
	
	public double timeMean(){
		return this.time_mean;
	}
	
	public double timeVariance(){
		return this.time_variance;
	}
	
}
