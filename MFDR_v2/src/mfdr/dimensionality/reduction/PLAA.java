package mfdr.dimensionality.reduction;

import java.util.LinkedList;

import mfdr.datastructure.Data;
import mfdr.datastructure.TimeSeries;
import mfdr.dimensionality.datastructure.PAAData;
import mfdr.dimensionality.datastructure.PLAAData;
import mfdr.dimensionality.datastructure.PLAData;
import mfdr.distance.Distance;
import mfdr.math.emd.utility.DataListCalculator;
import mfdr.utility.DataListOperator;

public class PLAA extends DimensionalityReduction {

	public PLAA(int NoC){
		setNoC(NoC);
	}
	
	@Override
	public String name() {
		return "PLAA";
	}

	@Override
	public TimeSeries getFullResolutionDR(TimeSeries ts) {
		LinkedList<PLAAData> dr = getDR(ts);
		return getFullResolutionDR(dr, ts);
	}

	// TODO not implemented
	public TimeSeries getFullResolutionDR(LinkedList<PLAAData> dr, TimeSeries ref) {
		TimeSeries ts = new TimeSeries();
		int windowsize = ref.size()/dr.size();
		if((double)ref.size() % dr.size() != 0)
			windowsize ++;
		int refindex = 0;
		for(PLAAData data : dr){
			if(data.isPLA()){
				for(int i = 0; i < windowsize ; i++){
					if(refindex >= ref.size())
						break;
					ts.add(new Data(ref.get(refindex).time(), data.getValue(i+1)));
					refindex++;
				}
			}else{
				// Compute windowsize
				int w_1= windowsize / 2;
				int w_2;
				if(windowsize % 2 == 0){
					w_2 = w_1;
				}else{
					w_2 = w_1+1;
				}
				// iterate through first part
				for(int i = 0 ; i < w_1 ; i++){
					if(refindex >= ref.size())
						break;
					ts.add(new Data(ref.get(refindex).time(), data.average(0)));
					refindex++;
				}
				// iterate through second part
				for(int i = 0 ; i < w_2 ; i++){
					if(refindex >= ref.size())
						break;
					ts.add(new Data(ref.get(refindex).time(), data.average(1)));
					refindex++;
				}
			}
			
		}
		return ts;
	}

	@Override
	public LinkedList<PLAAData> getDR(TimeSeries ts) {
		PLA pla = new PLA(1);
		PAA paa = new PAA(2);
		LinkedList<PLAAData> plaa = new LinkedList<PLAAData>();
		if (NoC == 0) {
			plaa.add(new PLAAData(0, 0, 0, true));
			return plaa;
		}
		// n = window size
		double n = ts.size() / NoC;
		LinkedList<TimeSeries> tslist = DataListOperator.getInstance().linkedListDivision(ts, n);
		for (TimeSeries subts : tslist) {
//			if (i + n < ts.size()) {
//				subts = (TimeSeries) ts.subList(i, (int) (i + n));
//				DataListOperator.getInstance().linkedListDivision(linkedlist, windowsize)
//			} else {
//				subts = (TimeSeries) ts.subList(i, ts.size());
//			}
			LinkedList<PLAData> dr_pla = pla.getDR(subts);
			LinkedList<PAAData> dr_paa = paa.getDR(subts);
			// Compute segment error
			double err_pla = DataListCalculator
					.getInstance()
					.getDifference(pla.getFullResolutionDR(dr_pla, subts),
							subts).energyDensity();
			double err_paa = DataListCalculator
					.getInstance()
					.getDifference(paa.getFullResolutionDR(dr_paa, subts),
							subts).energyDensity();

			if (err_pla < err_paa) { // PLA fits better
				plaa.add(new PLAAData(subts.get(0).time(), dr_pla.get(0).a0(),
						dr_pla.get(0).a1(), true));
			} else { // PAA fits better
				try {
					plaa.add(new PLAAData(subts.get(0).time(),
							dr_paa.get(0).average(), dr_paa.get(1).average(), false)); 
				} catch (Exception e) {
//					System.out.println("GG");
				}
				
				// plaa.add(new PLAAData(ts.get(i).time(),
				// dr_paa.get(0).average(), 0, true)); // Not work
				// plaa.add(new PLAAData(ts.get(i).time(),
				// dr_paa.get(1).average(), 0, true));
			}
		}
		return plaa;
	}

	@Override
	public double getDistance(TimeSeries ts1, TimeSeries ts2, Distance distance) {
		LinkedList<PLAAData> dr1 = getDR(ts1);
		LinkedList<PLAAData> dr2 = getDR(ts2);
		return getDistance(dr1, dr2, ts1.size(), distance);
	}

	// TODO fix this does not work.
	public double getDistance(LinkedList<PLAAData> dr1,
			LinkedList<PLAAData> dr2, int size, Distance distance) {
		PLA pla = new PLA(1);
		double dist_total = 0;
		double l = size / dr1.size();
		for (int i = 0; i < dr1.size(); i++) {
			// If both PLA
			LinkedList<PLAData> pla_1 = new LinkedList<PLAData>(), pla_2 = new LinkedList<PLAData>();
			if (dr1.get(i).isPLA() && dr2.get(i).isPLA()) {
				pla_1.add(dr1.get(i));
				pla_2.add(dr2.get(i));
				dist_total += Math.pow(pla.getDistance(pla_1, pla_2, (int) l, distance), 2);
			}
			// If both PAA
			else if (!dr1.get(i).isPLA() && !dr2.get(i).isPLA()) {
				for (int j = 0; j < 2; j++) {
					dist_total += Math.pow(dr1.get(i).average(j)- dr2.get(i).average(j), 2)* l / 2	;
				}
			}
			// If one is PLA one is PAA
			else {
				// Prepare dr1
				if (dr1.get(i).isPLA()) {
					pla_1.add(dr1.get(i));
					pla_1.add(new PLAData(dr1.get(i).time(), dr1.get(i).a0()
							+ dr1.get(i).a1() * l / 2, dr1.get(i).a1()));
				} else {
					pla_1.add(new PLAData(dr1.get(i).time(), dr1.get(i).a0(), 0));
					pla_1.add(new PLAData(dr1.get(i).time(), dr1.get(i).a1(), 0));
				}
				// Prepare dr2
				if (dr2.get(i).isPLA()) {
					pla_2.add(dr2.get(i));
					pla_2.add(new PLAData(dr2.get(i).time(), dr2.get(i).a0()
							+ dr2.get(i).a1() * l / 2, dr2.get(i).a1()));
				} else {
					pla_2.add(new PLAData(dr2.get(i).time(), dr2.get(i).a0(), 0));
					pla_2.add(new PLAData(dr2.get(i).time(), dr2.get(i).a1(), 0));
				}
				// Compute distance
				dist_total += Math.pow(pla.getDistance(pla_1, pla_2, (int) l, distance), 2);
			}
		}
		return Math.sqrt(dist_total);
	}

}
