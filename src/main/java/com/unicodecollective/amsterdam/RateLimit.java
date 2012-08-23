/*
 * Copyright (c) 2012 Unicode Collective London > Amsterdam contributors
 * This program is made available under the terms of the MIT License:
 * http://www.opensource.org/licenses/MIT
 */
package com.unicodecollective.amsterdam;

import com.unicodecollective.amsterdam.TokenBucket.FillRate;

public class RateLimit {

	private final int initialCapacity;
	private final int maxCapacity;
	private final FillRate fillRate;
	
	public RateLimit(int capacity, FillRate fillRate) {
		this(capacity, capacity, fillRate);
	}

	public RateLimit(int initialCapacity, int maxCapacity, FillRate fillRate) {
		this.initialCapacity = initialCapacity;
		this.maxCapacity = maxCapacity;
		this.fillRate = fillRate;
	}
	
	public int getInitialCapacity() {
		return initialCapacity;
	}
	
	public int getMaxCapacity() {
		return maxCapacity;
	}
	
	public FillRate getFillRate() {
		return fillRate;
	}

}
