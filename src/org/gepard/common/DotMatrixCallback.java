package org.gepard.common;

// interface used by DotMatrix calcuation controllers to communicate 
// with the DotMatrix class about the calcuation progress

public interface DotMatrixCallback {
	public void dotmatrixCalcStatus(final float percent);
	public int tellCallbackStep(int wordlen, int windowsize);
	public void tellAborted();
}