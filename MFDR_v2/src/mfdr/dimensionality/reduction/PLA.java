package mfdr.dimensionality.reduction;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import mfdr.datastructure.Data;
import mfdr.datastructure.TimeSeries;
import mfdr.dimensionality.datastructure.DFTData;
import mfdr.dimensionality.datastructure.PLAData;
import mfdr.distance.Distance;

public class PLA extends DimensionalityReduction {
	private static Log logger = LogFactory.getLog(PLA.class);

	public PLA(int NoC) {
		setNoC(NoC);
	}
	
	@Override
	public String name() {
		return "PLA";
	}

	@Override
	public TimeSeries getFullResolutionDR(TimeSeries ts) {
		TimeSeries plafull = new TimeSeries();
		LinkedList<PLAData> pla = getDR(ts);
		calFullResolutionDR(ts, plafull, pla);
		return plafull;
	}

	public TimeSeries getFullResolutionDR(LinkedList<PLAData> pla,
			TimeSeries ref) {
		TimeSeries plafull = new TimeSeries();
		calFullResolutionDR(ref, plafull, pla);
		return plafull;
	}

	private void calFullResolutionDR(TimeSeries ts, TimeSeries plafull,
			LinkedList<PLAData> pla) {
		if(pla.size()==0){
			for(Data data : ts){
				plafull.add(new Data(data.time(), 0));
			}
			return;
		}
		int windowsize = ts.size()/pla.size();
		if((double)ts.size() % pla.size() != 0)
			windowsize ++;
		int sizecount = 0;
		for(PLAData data : pla){
			for(int i = 0 ; i < windowsize ; i++){
				if(sizecount >= ts.size())
					break;
				double value = data.getValue(i+1);
				plafull.add(new Data(ts.get(sizecount).time(), value));
				sizecount++;
			}
		}
		
		
//		int windowsize = ts.size()/pla.size();
//		int winnum = 0;
//		int plaindex = 0;
//		Iterator<Data> it = ts.iterator();
//		while(it.hasNext()){
//			Data data = it.next();
//			double value = pla.get(plaindex).getValue(data.time()-(plaindex*windowsize)+1);
//			plafull.add(new Data(data.time(), value));
//			winnum++;
//			if(winnum % windowsize == 0){
//				plaindex++;
//				if(plaindex >= pla.size()){
//					break;
//				}
//			}
//		}
//		for(;winnum < ts.size();winnum++){
//			double value = pla.getLast().getValue(ts.get(winnum).time()-((pla.size()-1)*windowsize));
//			plafull.add(new Data(ts.get(winnum).time(), value));
//		}
	}

	@Override
	public LinkedList<PLAData> getDR(TimeSeries ts) {
		LinkedList<PLAData> pla = new LinkedList<PLAData>();
		if (NoC == 0) {
			pla.add(new PLAData(0, 0, 0));
			return pla;
		}
		// n = window size
		double n = ts.size() / NoC;
		double x = 1;
		int j = 1;
		double asum = 0, bsum = 0;
		while (x <= ts.size()) {
			double t = x - (j - 1)*n;
			asum += (t - (n + 1)/2) * ts.get((int) x - 1).value();
			bsum += (t - (2*n+1)/3) * ts.get((int) x - 1).value();
			if (x % n == 0) {
				double a = 12 * asum / (n * (n + 1) * (n - 1));
				double b = 6 * bsum / (n * (1 - n));
				pla.add(new PLAData(ts.get((int) (x - n)).time(), b, a));
				asum = 0;
				bsum = 0;
				j++;
			}
			x++;
		}
		return pla;
	}

	@Override
	public double getDistance(TimeSeries ts1, TimeSeries ts2, Distance distance) {
		LinkedList<PLAData> dr1 = getDR(ts1);
		LinkedList<PLAData> dr2 = getDR(ts2);
		return getDistance(dr1, dr2, ts1.size(), distance);
	}
	
	public double getBruteForceDistance(TimeSeries ts1, TimeSeries ts2, Distance distance) {
		TimeSeries dr1full = getFullResolutionDR(ts1);
		TimeSeries dr2full = getFullResolutionDR(ts2);
		return distance.calDistance(dr1full, dr2full, dr1full);
	}

	public double getDistanceOld(LinkedList<PLAData> dr1,
			LinkedList<PLAData> dr2, TimeSeries ref, Distance distance) {
		TimeSeries dr1full = getFullResolutionDR(dr1, ref);
		TimeSeries dr2full = getFullResolutionDR(dr2, ref);
		return distance.calDistance(dr1full, dr2full, dr1full);
	}

	/**
	 * Compute Distance
	 * @param dr1
	 * @param dr2
	 * @param size
	 * @param distance
	 * @return
	 */
	public double getDistance(LinkedList<PLAData> dr1, LinkedList<PLAData> dr2,
			int size, Distance distance) {
		if (dr1.size() != dr2.size()) {
//			logger.info("PLA inputs are at different lengths");
			int lcm = lcm(dr1.size(), dr2.size());
			LinkedList<LinkedList<PLAData>> lcmtrends = getLCMPLADataList(dr1,dr2, lcm, size);
			return getDistance(lcmtrends.get(0), lcmtrends.get(1),size, distance);
		} else{
			double dist_total = 0;
			double l = size / dr1.size();
			for (int i = 0; i < dr1.size(); i++) {
				PLAData pla_1 = dr1.get(i);
				PLAData pla_2 = dr2.get(i);
				double a3 = pla_1.a1() - pla_2.a1();
				double b3 = pla_1.a0() - pla_2.a0();
				double part1 = ((l * (l + 1) * (2*l + 1))/6) * Math.pow(a3, 2); 
				double part2 = l * (l + 1) * a3 * b3;
				double part3 = l * Math.pow(b3, 2);
				dist_total +=  part1 + part2 + part3;
			}
			return Math.sqrt(dist_total);
		}
		
	}
	
	public double[] computeIndexingQuery(TimeSeries query, LinkedList<LinkedList<PLAData>> plalist, Distance distance,  int q_NoC_t){
		PLA pla = new PLA(q_NoC_t);
		return computeIndexingQuerySingle(pla.getDR(query), plalist, query.size(), distance);
	}
	
	public double[] computeIndexingQuery(LinkedList<LinkedList<PLAData>> q_plalist, LinkedList<LinkedList<PLAData>> plalist, int size, Distance distance){
		double[] dist_query = new double[plalist.size()];
		for(int i = 0 ; i < plalist.size() ; i++){
			dist_query[i] = getDistance(q_plalist.get(i), plalist.get(i), size, distance);
		}
		return dist_query;
	}
	
	public double[] computeIndexingQuerySingle(LinkedList<PLAData> q_pla, LinkedList<LinkedList<PLAData>> plalist, int size, Distance distance){
		double[] dist_query = new double[plalist.size()];
		for(int i = 0 ; i < plalist.size() ; i++){
			dist_query[i] = getDistance(q_pla, plalist.get(i), size, distance);
		}
		return dist_query;
	}

	public LinkedList<LinkedList<PLAData>> extractIndexingQuery(LinkedList<LinkedList<PLAData>> q_trend, LinkedList<LinkedList<PLAData>> plalist, int q_NoC_t){
		LinkedList<LinkedList<PLAData>> q_plalist = new LinkedList<LinkedList<PLAData>>();
		// Iterate through plalist
		for(int i = 0 ; i < plalist.size() ; i++){
			// Null PLA
			if(plalist.get(i).size() == 1 && plalist.get(i).get(0).a0() == 0 && plalist.get(i).get(0).a1() == 0)
				q_plalist.add(q_trend.get(0));
			// If the index entity has less or equal NoC_t than query NoC_t.
			else if(plalist.get(i).size() <= q_NoC_t)
				q_plalist.add(q_trend.get(q_NoC_t));
			// If the index entity has more NoC_t than query NoC_t.
			else
				q_plalist.add(q_trend.get(plalist.get(i).size()));
		}
		return q_plalist;
	}
	
	public LinkedList<LinkedList<PLAData>> getLCMPLADataList(
			LinkedList<PLAData> trend1, LinkedList<PLAData> trend2, int lcm,
			double tslength) {
		LinkedList<LinkedList<PLAData>> list = new LinkedList<LinkedList<PLAData>>();
		int division1 = lcm / trend1.size();
		int division2 = lcm / trend2.size();
		double windowsize = tslength / lcm;
		LinkedList<PLAData> trend_lcm_1 = new LinkedList<PLAData>();
		LinkedList<PLAData> trend_lcm_2 = new LinkedList<PLAData>();
		for (int i = 0; i < lcm; i++) {
			double time = trend1.get(0).time() + windowsize * i;
			trend_lcm_1.add(new PLAData(time, trend1.get(i / division1).a0()
					+ trend1.get(i / division1).a1() * (i % division1)
					* windowsize, trend1.get(i / division1).a1()));
			trend_lcm_2.add(new PLAData(time, trend2.get(i / division2).a0()
					+ trend2.get(i / division2).a1() * (i % division2)
					* windowsize, trend2.get(i / division2).a1()));
		}
		list.add(trend_lcm_1);
		list.add(trend_lcm_2);
		return list;
	}

	public int gcd(int a, int b) {
		while (b > 0) {
			int temp = b;
			b = a % b; // % is remainder
			a = temp;
		}
		return a;
	}

	public int lcm(int a, int b) {
		return a * (b / gcd(a, b));
	}
	
//	public LinkedList<LinkedList<PLAData>> extractIndexingQuery(TimeSeries query, LinkedList<LinkedList<PLAData>> plalist, int q_NoC_t){
//		LinkedList<LinkedList<PLAData>> q_plalist = new LinkedList<LinkedList<PLAData>>();
//		PLA q_pla = new PLA(0);
//		// Iterate through plalist
//		for(int i = 0 ; i < plalist.size() ; i++){
//			// Null PLA
//			if(plalist.get(i).size() == 1 && plalist.get(i).get(0).a0() == 0 && plalist.get(i).get(0).a1() == 0)
//				q_pla.setNoC(0);
//			// If the index entity has more or equal NoC_t than query NoC_t.
//			else if(plalist.get(i).size() <= q_NoC_t)
//				q_pla.setNoC(q_NoC_t);
//			// If the index entity has less NoC_t than query NoC_t.
//			else
//				q_pla.setNoC(plalist.get(i).size());
//				q_plalist.add(q_trend.get(plalist.get(i).size()));
//		}
//		return q_plalist;
//	}
}
