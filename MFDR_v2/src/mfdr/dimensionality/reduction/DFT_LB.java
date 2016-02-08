package mfdr.dimensionality.reduction;

import java.util.HashMap;
import java.util.Map;
import mfdr.dimensionality.datastructure.DFTData;

public class DFT_LB extends DFT {
	public DFT_LB(int NoC) {
		super(NoC);
	}

	/**
	 * This object provides the DFT function that guarantees lower bounding when compute the distance from two time series.
	 * It preserves the lowest frequencies as it compresses time series.
	 * @param NoC
	 */

	@Override
	public String name() {
		return "DFT_LB";
	}
	
	/**
	 * This function get the lowest frequency components from hilb[] (i.e. frequency array).
	 * @param hilb
	 * @return
	 */
	public DFTData getDR(double[] hilb){
		Map<Integer, Double> hilbmap = new HashMap<Integer, Double>();
		for(int index = 0 ; index < NoC ; index++){
			hilbmap.put(index, hilb[index]);
		}
		return new DFTData(hilbmap, hilb.length);
	}
}
