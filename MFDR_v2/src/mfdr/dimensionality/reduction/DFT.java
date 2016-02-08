package mfdr.dimensionality.reduction;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jtransforms.fft.DoubleFFT_1D;

import mfdr.datastructure.Data;
import mfdr.datastructure.TimeSeries;
import mfdr.dimensionality.datastructure.DFTDataOld;
import mfdr.dimensionality.datastructure.DFTData;
import mfdr.dimensionality.datastructure.DFTWaveData;
import mfdr.distance.Distance;
import mfdr.utility.DataListOperator;
import mfdr.utility.ValueComparator;

public class DFT extends DimensionalityReduction {
	private static Log logger = LogFactory.getLog(DFT.class);
	public DFT(int NoC) {
		setNoC(NoC);
	}

	@Override
	public String name() {
		return "DFT";
	}

	@Override
	public TimeSeries getFullResolutionDR(TimeSeries ts) {
		DFTData dft = getDR(ts);
		return getFullResolutionDR(dft, ts);
	}

	public TimeSeries getFullResolutionDR(DFTData dft, TimeSeries ref) {
		TimeSeries drfull = new TimeSeries();
		double[] value = recoverFullResolutionFrequency(dft, ref.size());
		DoubleFFT_1D fft = new DoubleFFT_1D(ref.size());
		fft.realInverse(value, true);
		for(int i = 0 ; i < ref.size() ; i++){
			drfull.add(new Data(ref.get(i).time(), value[i]));
		}
		return drfull;
	}
	
	public TimeSeries getFullResolutionDR(LinkedList<DFTWaveData> wavelist, TimeSeries ref) {
		TimeSeries tsreduced = new TimeSeries();
		double[] real = new double[ref.size()];

		for(int i = 0 ; i < wavelist.size() ; i++){
			// Extract constant
			if(wavelist.get(i).freq() == 0){
				for (int j = 0; j < ref.size(); j++) {
					real[j] += wavelist.get(i).amplitude();
				}
			}
			// Extract Highest frequency
			else if(wavelist.get(i).freq() == ref.size()/2){
				for (int j = 0; j < ref.size(); j++) {
					if(j%1 == 0)
						real[j] += wavelist.get(i).amplitude();
					else
						real[j] -= wavelist.get(i).amplitude();
				}

			}
			// Extract remainders
			else{
				for (int j = 0; j < ref.size(); j++) {
					real[j] += wavelist.get(i).getWaveValue(j, ref.size());
				}	
			}
		}
		for (int i = 0; i < ref.size(); i++) {
			tsreduced.add(new Data(ref.get(i).time(), real[i]));
		}
		return tsreduced;
	}
	
	private double[] recoverFullResolutionFrequency(DFTData dft, int length){
		double[] output = new double[length];
		for(int i = 0 ; i < length ; i++){
			if(dft.getMap().containsKey(i)){
				output[i] = dft.getMap().get(i);
			}else{
				output[i] = 0;
			}
		}
		return output;
	}


	/**
	 * Here we use double array as the ouput data structure to store the DFT results in the Hilbert Space.
	 * @param ts
	 * @return a double array containing the DWT result in Hilbert Space.
	 */
	@Override
	public DFTData getDR(TimeSeries ts) {
		return getDR(converTSToFrequency(ts));
	}
	
	/**
	 * This function get DFTData from hilb array
	 * @param hilb
	 * @return
	 */
	public DFTData getDR(double[] hilb){
		// Get Sorted Hilb
		Map<Integer, Double> map = new HashMap<Integer, Double>();
		ValueComparator bvc =  new ValueComparator(map);
		TreeMap<Integer, Double> sorted_map = new TreeMap<Integer, Double>(bvc);
		for(int i = 0 ; i < hilb.length ; i++){
			map.put(i, Math.abs(hilb[i]));
		}
		sorted_map.putAll(map);
		// Store results to DFTData Map
		Map<Integer, Double> hilbmap = new HashMap<Integer, Double>();
		Iterator<Integer> it = sorted_map.keySet().iterator();
		for(int j = 0 ; j < NoC ; j++){
			int index = it.next();
			hilbmap.put(index, hilb[index]);
		}
		/* *****************************************/ 
		return new DFTData(hilbmap, hilb.length);
	}
	
	public double[] converTSToFrequency(TimeSeries ts){
		LinkedList<Double> tsvalues = DataListOperator.getInstance().getValueList(ts);
		double[] valuearray = DataListOperator.getInstance().linkedDoubleListToArray(tsvalues);
		
		// Conver value array into frequency domain
		DoubleFFT_1D fft = new DoubleFFT_1D(valuearray.length);
		fft.realForward(valuearray);
		return valuearray;
	}

	public double[] extractHighFrequency(double[] input, double lowestperiod, double timeinterval){
		int normalisedperiod = (int)(lowestperiod/timeinterval);
		int keeplength = 1 + (input.length-1)*2/normalisedperiod;
		double[] noisearray = new double[input.length-keeplength];
		for(int i = keeplength ; i < input.length ; i++){
			noisearray[i-keeplength] = input[i];
			input[i] = 0;
		}
		return noisearray;
	}
	
	@Override
	public double getDistance(TimeSeries ts1, TimeSeries ts2, Distance distance) {
		DFTData dr1 = getDR(ts1);
		DFTData dr2 = getDR(ts2);
		return getDistance(dr1, dr2, distance,ts1.size());
	}
	
	public double getDistance(DFTData dr1, DFTData dr2, Distance distance, int signallength){
		// Clone Map
		Map<Integer, Double> dr1map = new HashMap<Integer, Double>(dr1.getMap());
		Map<Integer, Double> dr2map = new HashMap<Integer, Double>(dr2.getMap());
		
		LinkedList<Double> list1 = new LinkedList<Double>();
		LinkedList<Double> list2 = new LinkedList<Double>();
		
		// Iterate through dr1map
		Iterator<Integer> it = dr1map.keySet().iterator();
		while (it.hasNext()) {
			int index = it.next();
			double normalisefactor;
			if(index < 2)
				normalisefactor = Math.pow(signallength, 0.5);
			else
				normalisefactor = Math.pow(signallength/2, 0.5);
			list1.add(dr1map.get(index)/normalisefactor);
			if(dr2map.containsKey(index)){
				list2.add(dr2map.remove(index)/normalisefactor);
			}else{
				list2.add(0.0);
			}
		}
		
		// Iterate through dr2map
		it = dr2map.keySet().iterator();
		while (it.hasNext()) {
			int index = (int) it.next();
			double normalisefactor;
			if(index < 2)
				normalisefactor = Math.pow(signallength, 0.5);
			else
				normalisefactor = Math.pow(signallength/2, 0.5);
			list2.add(dr2map.get(index)/normalisefactor);
			list1.add(0.0);
		}
		
		double[][] drarray = new double[2][list1.size()];
		for (int i = 0; i < list1.size(); i++) {
		    drarray[0][i] = list1.get(i);
		    drarray[1][i] = list2.get(i);
		}
		return distance.calDistance(drarray[0], drarray[1]);
	}
	
	/**
	 * This function accept LinkedList<DFTWaveData> stored in DFTData objects.
	 * @param wavelist1
	 * @param wavelist2
	 * @param distance
	 * @param signallength
	 * @return
	 */
	public double getDistance(LinkedList<DFTWaveData> wavelist1 , LinkedList<DFTWaveData> wavelist2 , Distance distance, int signallength){
		Map<Double, DFTWaveData> dr1map = new HashMap<Double, DFTWaveData>();
		Map<Double, DFTWaveData> dr2map = new HashMap<Double, DFTWaveData>();
		
		
		// Save to maps
		Iterator<DFTWaveData> it = wavelist1.iterator();
		while (it.hasNext()) {
			DFTWaveData wave = it.next();
			dr1map.put(wave.freq(), wave);
		}
		it = wavelist2.iterator();
		while (it.hasNext()) {
			DFTWaveData wave = it.next();
			dr2map.put(wave.freq(), wave);
		}
		
		LinkedList<Double> list1 = new LinkedList<Double>();
		LinkedList<Double> list2 = new LinkedList<Double>();
		// Iterate through dr1map
				Iterator<Double> it2 = dr1map.keySet().iterator();
				while (it2.hasNext()) {
					double index = it2.next();
					if(dr1map.get(index).freq() == 0 || dr1map.get(index).freq() == signallength/2){
						list1.add( dr1map.get(index).amplitude()*Math.sqrt(signallength));
						if(dr2map.containsKey(index)){
							list2.add(dr2map.get(index).amplitude()*Math.sqrt(signallength));
							dr2map.remove(index);
						} else{
							list2.add(0.0);
						}
					} else{
						list1.add(dr1map.get(index).getCosAmplitude()*Math.sqrt(signallength/2));
						list1.add(dr1map.get(index).getSinAmplitude()*Math.sqrt(signallength/2));
						if(dr2map.containsKey(index)){
							list2.add(dr2map.get(index).getCosAmplitude()*Math.sqrt(signallength/2));
							list2.add(dr2map.get(index).getSinAmplitude()*Math.sqrt(signallength/2));
							dr2map.remove(index);
						}else{
							list2.add(0.0);
							list2.add(0.0);
						}
					}
		
				}
				
				// Iterate through dr2map
				it2 = dr2map.keySet().iterator();
				while (it2.hasNext()) {
					double index = it2.next();
					if(dr2map.get(index).freq() == 0 || dr2map.get(index).freq() == signallength/2){
						list2.add(dr2map.get(index).amplitude()*Math.sqrt(signallength));
						list1.add(0.0);
					} else{
						list2.add(dr2map.get(index).getCosAmplitude()*Math.sqrt(signallength/2));
						list2.add(dr2map.get(index).getSinAmplitude()*Math.sqrt(signallength/2));
						list1.add(0.0);
						list1.add(0.0);
					}
				}
		
				double[][] drarray = new double[2][list1.size()];
				for (int i = 0; i < list1.size(); i++) {
				    drarray[0][i] = list1.get(i);
				    drarray[1][i] = list2.get(i);
				}
		return distance.calDistance(drarray[0], drarray[1]);
	}
	
	/**
	 * This function computes the LB distances, that required by Indexing, between a full dimensional query and a DFT-compressed index.
	 * @param query
	 * @param dftlist
	 * @param distance
	 * @return
	 */
	public double[] computeIndexingQuery(TimeSeries query, LinkedList<DFTData> dftlist, Distance distance){
		double[] dist_query = new double[dftlist.size()];
		// Extract data value and convert to frequency.
		double[] freq_query_all = converTSToFrequency(query);
		// Iterate through dftlist
		for(int i = 0 ; i < dftlist.size() ; i++){
			Map<Integer, Double> freq_query = new HashMap<Integer, Double>();
			// Extract corresponding frequencies
			Iterator<Integer> it =  dftlist.get(i).getMap().keySet().iterator();
			while(it.hasNext()){
				int freq = it.next();
				freq_query.put(freq, freq_query_all[freq]);
			}
			// Compute distance
			dist_query[i] = getDistance(freq_query, dftlist.get(i).getMap(), query.size(), distance);
		}
		return dist_query;
	}
	
	/**
	 * The query must be in the order of number NoC_s
	 * @param query
	 * @param dftlist
	 * @param distance
	 * @return
	 */
//	public double[] computeIndexingQuery(LinkedList<TimeSeries> query_list, LinkedList<DFTData> dftlist, Distance distance){
//		LinkedList<DFTData> q_dftlist = extractIndexingQuery(query_list, dftlist);
//		try {
//			return computeIndexingQuery(q_dftlist, q_dftlist, query_list.get(0).size(), distance);
//		} catch (Exception e) {
//			logger.info("Error in DFT computing indexing query");
//			return null;
//		}
//		
//	}
	
	/**
	 * The query must be in the order of number NoC_s. q_dftlist has to be pre-processed with extractIndexQuery to match dftlist.
	 * @param query
	 * @param dftlist
	 * @param distance
	 * @return
	 */
	public double[] computeIndexingQuery(LinkedList<DFTData> q_dftlist, LinkedList<DFTData> dftlist, int size, Distance distance){
		double[] dist_query = new double[dftlist.size()];
		for(int i = 0 ; i < dftlist.size() ; i++){
			// Compute distance
			dist_query[i] = getDistance(q_dftlist.get(i).getMap(), dftlist.get(i).getMap(), size, distance);
		}
		return dist_query;
	}
	
	
	/**
	 * The query must be in the order of number NoC_s
	 * @param query
	 * @param dftlist
	 * @param distance
	 * @return
	 */
	public LinkedList<DFTData> extractIndexingQuery(LinkedList<TimeSeries> query, LinkedList<DFTData> dftlist){
		LinkedList<DFTData> q_dftlist = new LinkedList<DFTData>();
		Map<Integer, double[]> querymap = new HashMap<Integer, double[]>();
		// Iterate through dftlist
		for(int i = 0 ; i < dftlist.size() ; i++){
			Map<Integer, Double> hilb_query = new HashMap<Integer, Double>();
			int NoC_s = dftlist.get(i).getMap().size();
			// Convert query into freq if null in querymap.
			if(!querymap.containsKey(NoC_s)){
				double[] freq_query_all = converTSToFrequency(query.get(NoC_s));
				querymap.put(NoC_s, freq_query_all);
			}
			// Extract corresponding frequencies
			Iterator<Integer> it =  dftlist.get(i).getMap().keySet().iterator();
			while(it.hasNext()){
				int freq = it.next();
				hilb_query.put(freq, querymap.get(NoC_s)[freq]);
			}
			q_dftlist.add(new DFTData(hilb_query, query.get(0).size()));
		}
		return q_dftlist;
	}
	
	/* ************** Code below is not in use.***************** */
	/**
	 * Compute the distance between two frequency maps
	 * @param freq1
	 * @param freq2
	 * @param size
	 * @param distance
	 * @return
	 */
	public double getDistance(Map<Integer, Double> freq1, Map<Integer, Double> freq2, int size, Distance distance){
		double[] freq1_array = new double[freq1.size()];
		double[] freq2_array = new double[freq2.size()];
		int i = 0;
		
		// Normalise values
		Iterator<Integer> it = freq1.keySet().iterator();
		while(it.hasNext()){
			int index = it.next();
			if(index < 2){
				freq1_array[i] = freq1.get(index) / Math.pow(size, 0.5);
				freq2_array[i] = freq2.get(index) / Math.pow(size, 0.5);
			}
			else{
				freq1_array[i] = freq1.get(index) / Math.pow(size/2, 0.5);
				freq2_array[i] = freq2.get(index) / Math.pow(size/2, 0.5);
			}
			i++;
		}
		// Compute and return distance
		return distance.calDistance(freq1_array, freq2_array);
	}
	
	/**
	 * This function calculate the distance between two dwt lists.
	 * 
	 * @param dft_list1
	 * @param dft_list2
	 * @param distance
	 * @return
	 */
	public double getDistance(LinkedList<DFTDataOld> dft_list1 , LinkedList<DFTDataOld> dft_list2 , Distance distance){
		double[] hilb1 = new double[dft_list1.peek().hilb().length * dft_list1.size()];
		double[] hilb2 = new double[dft_list2.peek().hilb().length * dft_list2.size()];
		if(hilb1.length != hilb2.length){
			logger.info("The length of input dwt LinkedList is not equal.");
			return 0;
		}
		int datalength = dft_list1.size();
		int datasize = dft_list1.peek().hilb().length;
		for(int i = 0 ; i < datalength ; i++){
			for(int j = 0 ; j < datasize ; j++){
				hilb1[i*datasize+j] = dft_list1.get(i).hilb()[j]; 
				hilb2[i*datasize+j] = dft_list2.get(i).hilb()[j]; 
			}
		}
		return distance.calDistance(hilb1, hilb2);
	}

	/**
	 * This is a redundant function that calculate distances after restore DFT into full resolution
	 * @param ts1
	 * @param ts2
	 * @param distance
	 * @return
	 */
	public double getDistanceTest(TimeSeries ts1, TimeSeries ts2, Distance distance) {
		TimeSeries dr1full = getFullResolutionDR(ts1);
		TimeSeries dr2full = getFullResolutionDR(ts2);
		double[][] dr1fullarray = DataListOperator.getInstance().linkedDataListToArray(dr1full);
		double[][] dr2fullarray = DataListOperator.getInstance().linkedDataListToArray(dr2full);
		return distance.calDistance(dr1fullarray[1], dr2fullarray[1]);
	}

}
