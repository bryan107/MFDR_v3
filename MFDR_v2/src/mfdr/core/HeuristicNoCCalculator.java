package mfdr.core;

import mfdr.datastructure.TimeSeries;
import mfdr.dimensionality.datastructure.MFDRWaveData;
import mfdr.dimensionality.reduction.MFDR;
import mfdr.math.emd.utility.DataListCalculator;

public class HeuristicNoCCalculator implements NoCCalculator {

	private MFDR mfdr;
	private long startTime;
	private long endTime;

	public HeuristicNoCCalculator() {
		this.mfdr = new MFDR(0, 0);
		startTime = 0;
		endTime = 0;
	}

	@Override
	public MFDRObject getMFDRNoCs(TimeSeries ts, int NoC, double lowestperiod) {
		MFDRObject[] edge = new MFDRObject[2];
		this.startTime = System.nanoTime();
		extractEdgeComposition(ts, NoC, lowestperiod, edge);
		MFDRObject temp = getOptimalMFDRNoCs(edge[0], edge[1], ts, NoC,
				lowestperiod);
		this.endTime = System.nanoTime();
		MFDRObject result = new MFDRObject(temp.NoC_t(), temp.NoC_s(),
				temp.lowestPeriod(), temp.data(), temp.error(), endTime
						- startTime);
		return result;
	}

	private MFDRObject getOptimalMFDRNoCs(MFDRObject left, MFDRObject right,
			TimeSeries ts, int NoC, double lowestperiod) {
		int mid_NoC_t = (left.NoC_t() + right.NoC_t()) / 2;
		// If reach the leaf of search (No mid can be retrieved)
		if (mid_NoC_t == left.NoC_t() || mid_NoC_t == right.NoC_t()) {
			if (left.error() < right.error()) {
				return left;
			} else {
				return right;
			}
		} else {
			// Compute mid values
			mfdr.updateParameters(mid_NoC_t, NoC - mid_NoC_t);
			// MFDR mfdr = new MFDR(mid_NoC_t, NoC - mid_NoC_t);
			MFDRWaveData mid_data;
			double mid_err;
			if (lowestperiod == 0) {
				mid_data = mfdr.getDR(ts);
				// mid_err = DataListCalculator.getInstance().getDifference(ts,
				// mfdr.getFullResolutionDR(ts)).energyDensity();
			} else {
				mid_data = mfdr.getDR(ts, lowestperiod);
				// mid_err = DataListCalculator.getInstance().getDifference(ts,
				// mfdr.getFullResolutionDR(ts,lowestperiod)).energyDensity();
			}

			mid_err = DataListCalculator.getInstance()
					.getDifference(ts, mfdr.getFullResolutionDR(mid_data, ts))
					.energyDensity();

			MFDRObject mid = new MFDRObject(mid_NoC_t, NoC - mid_NoC_t,
					lowestperiod, mid_data, mid_err, 0);
			// Recursive operation
			if (left.error() < right.error()) {
				return getOptimalMFDRNoCs(left, mid, ts, NoC, lowestperiod);
			} else {
				return getOptimalMFDRNoCs(mid, right, ts, NoC, lowestperiod);
			}
		}
	}

	private void extractEdgeComposition(TimeSeries ts, int NoC,
			double lowestperiod, MFDRObject[] edge) {
		MFDR mfdr;

		// Compute left (with full NoC_t)
		mfdr = new MFDR(NoC, 0);
		MFDRWaveData data0;
		if (lowestperiod == 0)
			data0 = mfdr.getDR(ts);
		else
			data0 = mfdr.getDR(ts, lowestperiod);
		double err = DataListCalculator.getInstance()
				.getDifference(ts, mfdr.getFullResolutionDR(data0, ts))
				.energyDensity();

		// Compute right (with full NoC_s)
		mfdr = new MFDR(0, NoC);
		MFDRWaveData data1;
		if (lowestperiod == 0)
			data1 = mfdr.getDR(ts);
		else
			data1 = mfdr.getDR(ts, lowestperiod);
		err = DataListCalculator.getInstance()
				.getDifference(ts, mfdr.getFullResolutionDR(data1, ts))
				.energyDensity();

		// Store results
		edge[0] = new MFDRObject(NoC, 0, lowestperiod, data0, err, 0);
		edge[1] = new MFDRObject(0, NoC, lowestperiod, data1, err, 0);
	}

	private MFDRObject getOptimalMFDRNoCsBAK(MFDRObject left, MFDRObject right,
			TimeSeries ts, int NoC, double lowestperiod) {
		int mid_NoC_t = (left.NoC_t() + right.NoC_t()) / 2;
		// If reach the leaf of search (No mid can be retrieved)
		if (mid_NoC_t == left.NoC_t() || mid_NoC_t == right.NoC_t()) {
			if (left.error() < right.error()) {
				return left;
			} else {
				return right;
			}
		} else {
			// Compute mid values
			mfdr.updateParameters(mid_NoC_t, NoC - mid_NoC_t);
			// MFDR mfdr = new MFDR(mid_NoC_t, NoC - mid_NoC_t);
			MFDRWaveData mid_data;
			double mid_err;
			if (lowestperiod == 0) {
				mid_data = mfdr.getDR(ts);
				mid_err = DataListCalculator.getInstance()
						.getDifference(ts, mfdr.getFullResolutionDR(ts))
						.energyDensity();
			} else {
				mid_data = mfdr.getDR(ts, lowestperiod);
				mid_err = DataListCalculator
						.getInstance()
						.getDifference(ts,
								mfdr.getFullResolutionDR(ts, lowestperiod))
						.energyDensity();
			}

			MFDRObject mid = new MFDRObject(mid_NoC_t, NoC - mid_NoC_t,
					lowestperiod, mid_data, mid_err,0);
			// Recursive operation
			if (left.error() < right.error()) {
				return getOptimalMFDRNoCs(left, mid, ts, NoC, lowestperiod);
			} else {
				return getOptimalMFDRNoCs(mid, right, ts, NoC, lowestperiod);
			}
		}
	}

	private void extractEdgeCompositionBAK(TimeSeries ts, int NoC,
			double lowestperiod, MFDRObject[] edge) {
		MFDR mfdr;
		// Compute left
		mfdr = new MFDR(0, NoC);
		MFDRWaveData data;
		if (lowestperiod == 0)
			data = mfdr.getDR(ts);
		else
			data = mfdr.getDR(ts, lowestperiod);
		double err = DataListCalculator.getInstance()
				.getDifference(ts, mfdr.getFullResolutionDR(ts))
				.energyDensity();
		edge[0] = new MFDRObject(0, NoC, lowestperiod, data, err,0);

		// Compute right
		mfdr = new MFDR(NoC, 0);
		data = mfdr.getDR(ts);
		err = DataListCalculator.getInstance()
				.getDifference(ts, mfdr.getFullResolutionDR(ts))
				.energyDensity();
		edge[1] = new MFDRObject(NoC, 0, lowestperiod, data, err,0);
	}

	@Override
	public String name() {
		return "Heuristic";
	}
}
