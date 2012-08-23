package com.unicodecollective.amsterdam;

import static com.unicodecollective.amsterdam.TokenBucket.FillRate.perMilli;
import static com.unicodecollective.amsterdam.TokenBucket.FillRate.perMinute;
import static com.unicodecollective.amsterdam.TokenBucket.FillRate.transactionsPerSecond;
import static java.lang.Thread.sleep;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.unicodecollective.amsterdam.TokenBucket.FillRate;

public class TokenBucketTests {
	
	private TokenBucket tokenBucket;
	
	@Before
	public void setUp() {
	}
	
	@After
	public void tearDown() {
		if (tokenBucket != null && tokenBucket.isFilling()) {
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
	public void requestingOneTokenFromEmptyBucketWhichFillsAtOnePerMinuteIsInitiallyInvalid() {
		tokenBucket = new TokenBucket(0);
		tokenBucket.startFilling(perMinute(1));
		assertFalse(tokenBucket.getTokens(1));
	}
	
	@Test
	public void requestingOneTokenFromEmptyBucketWhichFillsAtOnePerMillisecondIsValidAfterWaitingMoreThanAMillisecond() throws InterruptedException {
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
		tokenBucket = new TokenBucket(0, perMilli(1));
		sleep(1000);
		int capacity = tokenBucket.getCapacity();
		System.out.println("Capacity after 1000ms: " + capacity);
		assertTrue("Expected capacity of >= 999, but was: " + capacity, capacity >= 999);
		assertTrue("Expected capacity of <= 1001, but was: " + capacity, capacity <= 1001);
	}
	
	@Test
	public void ensureMaximumCapacityIsNotExceeded() throws InterruptedException {
		tokenBucket = new TokenBucket(5, 10, perMilli(100));
		sleep(4);
		assertEquals(10, tokenBucket.getCapacity());
	}
	
	@Test(expected = NullPointerException.class)
	public void startFillingCannotBeInitiatedWithNullFillRate() {
		tokenBucket = new TokenBucket(0, null);
	}
	
	@Test(expected = NullPointerException.class)
	public void stopFillingCannotBeCalledWithoutStartFilling() {
		tokenBucket = new TokenBucket(0);
		tokenBucket.stopFilling();
	}
	
	@Test
	public void tpsFillRate() {
		assertFillRate(1, 1, transactionsPerSecond(1000));
		assertFillRate(3, 1, transactionsPerSecond(3000));
		assertFillRate(1, 2, transactionsPerSecond(500));
		assertFillRate(1, 1000, transactionsPerSecond(1));
		assertFillRate(1, 100, transactionsPerSecond(10));
		assertFillRate(3, 1000, transactionsPerSecond(3));
		assertFillRate(1321, 1000, transactionsPerSecond(1321));
	}

	private void assertFillRate(int transactionsPerTick, int tickMills, FillRate fillRate) {
		assertEquals(transactionsPerTick, fillRate.getAmount());
		assertEquals(tickMills, fillRate.getDuration().getMillis());
	}
	
}
