/*
 * Copyright (c) 2012 Unicode Collective London > Amsterdam contributors
 * This program is made available under the terms of the MIT License.
 */
package com.unicodecollective.amsterdam;

import static com.unicodecollective.amsterdam.CallSpecs.methodMatching;
import static com.unicodecollective.amsterdam.TokenBucket.FillRate.perSecond;
import static com.unicodecollective.amsterdam.TokenCostFunctions.fixedCost;
import static junit.framework.Assert.assertNotSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.lang.reflect.Method;

import org.junit.Before;
import org.junit.Test;


public class RateLimiterFactoryTests {
	
	private RateLimiterFactory rateLimiterFactory;
	private Tester mockTester;
	
	@Before
	public void setUp() {
		rateLimiterFactory = new RateLimiterFactory();
		mockTester = mock(Tester.class);
	}
	
	@Test
	public void proxyPassesCallOnToDecoratedObject() {
		Tester rateLimitedTester = rateLimiterFactory.limitRate(mockTester, methodMatching(".*"), fixedCost(1), new RateLimit(10, 10, perSecond(0)));
		rateLimitedTester.rateLimitMe(11);
		
		verify(mockTester).rateLimitMe(11);
		assertNotSame(rateLimitedTester, mockTester);
	}
	
	@Test
	public void methodCallIsRateLimited() {
		RateLimit rateLimit = new RateLimit(10, 10, perSecond(0));
		Tester rateLimitedTester = rateLimiterFactory.limitRate(mockTester, methodMatching("rateLimit.*"), fixedCost(1), rateLimit);
		for (int i = 0; i < 20; i++) {
			try {
				rateLimitedTester.rateLimitMe(10);
			} catch (CapacityExceededException e) {}
		}
		
		verify(mockTester, times(10)).rateLimitMe(10);
	}
	
	@Test
	public void methodCallIsNotRateLimited() {
		RateLimit rateLimit = new RateLimit(10, 10, perSecond(0));
		Tester rateLimitedTester = rateLimiterFactory.limitRate(mockTester, methodMatching("rateLimit.*"), fixedCost(1), rateLimit);
		for (int i = 0; i < 20; i++) {
			try {
				rateLimitedTester.dontRateLimitMe(100);
			} catch (CapacityExceededException e) {}
		}
		
		verify(mockTester, times(20)).dontRateLimitMe(100);
	}
	
	@Test
	public void methodCallIsLimitedByTokenCostFunction() {
		RateLimit rateLimit = new RateLimit(100, 100, perSecond(0));
		TokenCostFunction tokenCostFuction = new TokenCostFunction() {
			@Override
			public int calculateCost(Method method, Object[] args) {
				return (Integer) args[0] * 2;
			}
		};
		Tester rateLimitedTester = rateLimiterFactory.limitRate(mockTester, methodMatching("rateLimit.*"), tokenCostFuction, rateLimit);
		rateLimitedTester.rateLimitMe(48); // Use 96 of the 100 tokens available.
		try {
			rateLimitedTester.rateLimitMe(10); // Fail: wants 20, but only 4 remaining.
		} catch (CapacityExceededException e) {}
		rateLimitedTester.rateLimitMe(2); // Use remaining 4 tokens.
		try {
			rateLimitedTester.rateLimitMe(1); // FailsL wants 2, but none available.
		} catch (CapacityExceededException e) {}
		
		verify(mockTester, times(1)).rateLimitMe(48);
		verify(mockTester, times(1)).rateLimitMe(2);
		
	}
	
	
	private static interface Tester {
		
		public void rateLimitMe(int i);
		
		public void dontRateLimitMe(int i);
		
	}

}
