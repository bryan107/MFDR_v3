package experiment.core;

public interface ExperimentCore {

	/**
	 * run the experiment with the following inputs.
	 * arg is a integer value which may not be used. 
	 * @param readaddress
	 * @param writeaddress
	 * @param listaddress
	 * @param NoC_Start
	 * @param NoC_Interval
	 * @param NoC_End
	 * @param arg
	 */
	public void run(String readaddress, String writeaddress,
			String listaddress, int NoC_Start, int NoC_Interval, int NoC_End,
			int arg);

}
