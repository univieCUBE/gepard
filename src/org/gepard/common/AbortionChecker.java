package org.gepard.common;

// simple interface for classes which control a dotplot
// and may send an abortion-flag back to the SuffixArray or DotMatix calcuation

public interface AbortionChecker {
	public boolean dotplotAborted();
}
