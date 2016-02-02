package mfdr.core;

import mfdr.datastructure.TimeSeries;

public interface NoCCalculator {
	public MFDRObject getMFDRNoCs(TimeSeries ts, int NoC, double lowestperiod);
	public String name();
}
