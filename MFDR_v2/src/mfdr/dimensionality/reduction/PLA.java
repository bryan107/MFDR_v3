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
		int winnum = 0;
		int plaindex = 0;
		Iterator<Data> it = ts.iterator();
		while(it.hasNext()){
			Data data = it.next();
			double value = pla.get(plaindex).getValue(data.time()-(plaindex*windowsize)+1);
			plafull.add(new Data(data.time(), value));
			winnum++;
			if(winnum % windowsize == 0){
				plaindex++;
				if(plaindex >= pla.size()){
					break;
				}
			}
		}
		for(;winnum < ts.size();winnum++){
			double value = pla.getLast().getValue(ts.get(winnum).time()-((pla.size()-1)*windowsize));
			plafull.add(new Data(ts.get(winnum).time(), value));
		}
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
			logger.info("PLA inputs are at different lengths");
			return 0;
		}
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
	
	public double[] computeIndexingQuery(TimeSeries query, LinkedList<LinkedList<PLAData>> plalist, Distance distance){
		LinkedList<LinkedList<PLAData>> q_plalist = extractIndexingQuery(query, plalist);
		return computeIndexingQuery(q_plalist, plalist, query.size(), distance);
	}
	
	public double[] computeIndexingQuery(LinkedList<LinkedList<PLAData>> q_plalist, LinkedList<LinkedList<PLAData>> plalist, int size, Distance distance){
		double[] dist_query = new double[plalist.size()];
		for(int i = 0 ; i < plalist.size() ; i++){
			dist_query[i] = getDistance(q_plalist.get(i), plalist.get(i), size, distance);
		}
		return dist_query;
	}

	public LinkedList<LinkedList<PLAData>> extractIndexingQuery(TimeSeries query, LinkedList<LinkedList<PLAData>> plalist){
		LinkedList<LinkedList<PLAData>> q_plalist = new LinkedList<LinkedList<PLAData>>();
		PLA q_pla = new PLA(0);
		// Iterate through plalist
		for(int i = 0 ; i < plalist.size() ; i++){
			if(plalist.get(i).size() == 1 && plalist.get(i).get(0).a0() == 0 && plalist.get(i).get(0).a1() == 0)
				q_pla.setNoC(0);
			else
				q_pla.setNoC(plalist.get(i).size());
			q_plalist.add(q_pla.getDR(query));
		}
		return q_plalist;
	}
}
