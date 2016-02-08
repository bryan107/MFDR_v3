package mfdr.dimensionality.datastructure;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.TreeMap;

import mfdr.math.trigonometric.Triangle;
import mfdr.utility.ValueComparator;

import org.apache.commons.collections.map.HashedMap;

public class DFTData {
	private final Map<Integer, Double> hilb;
	private LinkedList<DFTWaveData> wavelist;
	
	public DFTData(Map<Integer, Double> hilb, int signallength){
		this.hilb = hilb;
		this.wavelist = computeWave(signallength);
	}
	
//	public void setMap(Map<Integer, Double> hilb){
//		this.hilb = hilb;
//	}
	
	public Map<Integer, Double> getMap(){
		return this.hilb;
	}
	
	public LinkedList<DFTWaveData> getWaveList(){
		return this.wavelist;
	}

	private LinkedList<DFTWaveData> computeWave(int signallength){
		LinkedList<DFTWaveData> wavelist = new LinkedList<DFTWaveData>();
		// Get Sorted Hilb
		
		Map<Integer, Double> map = new HashMap<Integer, Double>();
        Iterator<Integer> it = hilb.keySet().iterator();
        LinkedList<Integer> removedlist = new LinkedList<Integer>();
		for(int j = 0 ; j < hilb.size() ; j++){
			int index = it.next();
			if(removedlist.contains(index))
				continue;
			// If index < 2
//			if(index < 2){
//				double phasedelay = 0;
//				double amplitude = hilb[index] / hilb.length;
//				double freq;
//				if(index == 0)
//					freq = 0;
//				else
//					freq = hilb.length/2;
//				wavelist.add(new DFTWaveData(amplitude, phasedelay, freq));
//				map.put(index, amplitude);
//			}
			// If index is a real rumber
			if(index % 2 == 0){
				double cos, sin;
				// If map contains its complex number
				if(index+1>=signallength){
					cos =hilb.get(index);
					sin =0;
				}
				else if(hilb.containsKey(index+1)){
					cos =hilb.get(index);
					sin =hilb.get(index+1);
					removedlist.add(index+1);
				} else {
					cos =hilb.get(index);
					sin =0;
				}
				double phasedelay = Triangle.getInstance().getPhaseDelay(cos, sin);
				double amplitude = Triangle.getInstance().getAmplitude(cos, sin)
						/ (signallength / 2);
				double freq = index / 2;
				wavelist.add(new DFTWaveData(amplitude, phasedelay, freq));
			}
			// If index is a complex rumber
			if(index % 2 == 1){
				double cos, sin;
				// If map contains its complex number
				if(hilb.containsKey(index-1)){
					cos =hilb.get(index-1);
					sin =hilb.get(index);
					removedlist.add(index-1);
				} else {
					cos =0;
					sin =hilb.get(index);
				}
				double phasedelay = Triangle.getInstance().getPhaseDelay(cos, sin);
				double amplitude = Triangle.getInstance().getAmplitude(cos, sin)
						/ (signallength / 2);
				double freq = index / 2;
				wavelist.add(new DFTWaveData(amplitude, phasedelay, freq));
			}
		}
		return wavelist;
	}
	
	
}
