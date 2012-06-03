package com.unicodecollective.amsterdam;

import static com.unicodecollective.amsterdam.TokenBucket.FillRate.perMilli;
import static com.unicodecollective.amsterdam.TokenBucket.FillRate.perMinute;
import static java.lang.Thread.sleep;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.unicodecollective.amsterdam.TokenBucket;

public class TokenBucketTests {
	
	private TokenBucket tokenBucket;
	
	@Before
	public void setUp() {
	}
	
	@After
	public void tearDown() {
		if (tokenBucket.isFilling()) {
			tokenBucket.stopFilling();
		}
	}
	
	@Test
	public void firstTokenFromBucketSizeOneIsValid() {
		tokenBucket = new TokenBucket(1);
		assertTrue(tokenBucket.getToken());
	}

	@Test
	public void secondTokenFromBucketSizeOneIsInvalid() {
		tokenBucket = new TokenBucket(1);
		tokenBucket.getToken();
		assertFalse(tokenBucket.getToken());
	}
	
	@Test
	public void requestingThreeTokensFromABucketOfTwoIsInvalid() {
		tokenBucket = new TokenBucket(2);
		assertFalse(tokenBucket.getTokens(3));
	}
	
	/**
	 * Make sure that subsequent requests for an available number of 
	 * tokens are not blocked by previous request for too many tokens.
	 */
	@Test
	public void requestingOneTokenAfterARequestForThreeFromABucketOfTwoIsValid() {
		tokenBucket = new TokenBucket(2);
		tokenBucket.getTokens(3);
		assertTrue(tokenBucket.getTokens(2));
	}
	
	@Test
	public void requestingOneTokenFromEmptyBucketWhichFillsAtOnePerSecondIsInitiallyInvalid() {
		tokenBucket = new TokenBucket(0);
		tokenBucket.startFilling(perMinute(1));
		assertFalse(tokenBucket.getTokens(1));
	}
	
	@Test
	public void requestingOneTokenFromEmptyBucketWhichFillsAtOnePerSecondIsValidAfterWaitingMoreThanASecond() throws InterruptedException {
		tokenBucket = new TokenBucket(0);
		tokenBucket.startFilling(perMilli(1));
		sleep(4);
		assertTrue(tokenBucket.getTokens(1));
	}
	
	/**
	 * TODO: More precise sleep timing...?
	 */
	@Test
	public void refill1000TimesGivesCapacityOfAbout1000() throws InterruptedException {
		tokenBucket = new TokenBucket(0);
		tokenBucket.startFilling(perMilli(1));
		sleep(1000);
		int capacity = tokenBucket.getCapacity();
		System.out.println("Capacity after 1000ms: " + capacity);
		assertTrue("Expected capacity of >= 999, but was: " + capacity, capacity >= 999);
		assertTrue("Expected capacity of <= 1001, but was: " + capacity, capacity <= 1001);
	}
	
	@Test
	public void ensureMaximumCapacityIsNotExceeded() throws InterruptedException {
		tokenBucket = new TokenBucket(5, 10);
		tokenBucket.startFilling(perMilli(100));
		sleep(4);
		assertEquals(10, tokenBucket.getCapacity());
	}
	
	@Test(expected = NullPointerException.class)
	public void startFillingCannotBeInitiatedWithNullFillRate() {
		tokenBucket = new TokenBucket(0);
		tokenBucket.startFilling(null);
	}
	
	@Test(expected = IllegalStateException.class)
	public void startFillingCannotBeCalledInSuccessionWithoutStopFilling() {
		tokenBucket = new TokenBucket(0);
		tokenBucket.startFilling(perMilli(100));
		tokenBucket.startFilling(perMilli(100));
	}
	
	@Test(expected = NullPointerException.class)
	public void stopFillingCannotBeCalledWithoutStartFilling() {
		tokenBucket = new TokenBucket(0);
		tokenBucket.stopFilling();
	}
	
}
