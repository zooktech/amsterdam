package com.unicodecollective.amsterdam;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static java.lang.Math.min;
import static java.lang.System.currentTimeMillis;
import static java.lang.Thread.sleep;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.apache.commons.lang.math.Fraction.getReducedFraction;
import static org.apache.log4j.Logger.getLogger;
import static org.joda.time.Duration.millis;
import static org.joda.time.Duration.standardMinutes;
import static org.joda.time.Duration.standardSeconds;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang.math.Fraction;
import org.apache.log4j.Logger;
import org.joda.time.Duration;

public class TokenBucket {
	
	private static final Logger log = getLogger(TokenBucket.class);
	
	private final AtomicInteger capacity;
	private final Integer maximumCapacity;
	
	private ExecutorService bucketFillerExecutor;
	private boolean filling;
	
	public TokenBucket(int initialCapacity) {
		this.capacity = new AtomicInteger(initialCapacity);
		this.maximumCapacity = null;
	}
	
	public TokenBucket(int initialCapacity, FillRate fillRate) {
		this(initialCapacity);
		startFilling(fillRate);
	}
	
	public TokenBucket(int initialCapacity, int maximumCapacity) {
		this.capacity = new AtomicInteger(initialCapacity);
		this.maximumCapacity = maximumCapacity;
	}
	
	public TokenBucket(int initialCapacity, int maximumCapacity, FillRate fillRate) {
		this(initialCapacity, maximumCapacity);
		startFilling(fillRate);
	}

	public TokenBucket(RateLimit rateLimit) {
		this(rateLimit.getInitialCapacity(), rateLimit.getMaxCapacity(), rateLimit.getFillRate());
	}

	public void startFilling(FillRate fillRate) {
		checkNotNull(fillRate);
		checkState(bucketFillerExecutor == null, "Bucket filler has already been started for this token bucket.");
		bucketFillerExecutor = Executors.newSingleThreadExecutor();
		bucketFillerExecutor.execute(new BucketFiller(fillRate));
		filling = true;
	}
	
	public void stopFilling() {
		checkNotNull(bucketFillerExecutor, "Bucket filler is not running.");
		bucketFillerExecutor.shutdownNow();
		try {
			bucketFillerExecutor.awaitTermination(10, SECONDS);
		} catch (InterruptedException e) {
			log.error("Timed out waiting for bucket filler thread to shut down: ", e);
		} finally {
			bucketFillerExecutor = null;
			filling = false;
		}
	}
	
	public int getCapacity() {
		return capacity.get();
	}

	public boolean getToken() {
		return getTokens(1);
	}

	public boolean getTokens(int numTokens) {
		int capacityAfter = capacity.addAndGet(-numTokens);
		if (capacityAfter < 0) {
			// Optimistically claimed more tokens than were actually available - give them back, so the next thread can try:
			capacity.addAndGet(numTokens);
			return false;
		}
		return true;
	}

	public boolean isFilling() {
		return filling;
	}

	public static class FillRate {
		
		private final int amount;
		private final Duration duration;

		public FillRate(int amount, Duration duration) {
			this.amount = amount;
			this.duration = duration;
		}
		
		public int getAmount() {
			return amount;
		}
		
		public Duration getDuration() {
			return duration;
		}

		public static FillRate perSecond(int amount) {
			return new FillRate(amount, standardSeconds(1));
		}

		public static FillRate perMinute(int amount) {
			return new FillRate(amount, standardMinutes(1));
		}

		public static FillRate perMilli(int amount) {
			return new FillRate(amount, millis(1));
		}

		public static FillRate transactionsPerSecond(int tps) {
			checkArgument(tps > 0, "TPS value must be greater than 0.");
			Fraction reducedFraction = getReducedFraction(tps, 1000);
			return new FillRate(reducedFraction.getNumerator(), Duration.millis(reducedFraction.getDenominator()));
		}
		
	}
	
	private class BucketFiller implements Runnable {
		
		private final FillRate fillRate;
		
		private long nextFillTime;

		public BucketFiller(FillRate fillRate) {
			this.fillRate = fillRate;
			nextFillTime = currentTimeMillis() + fillRate.getDuration().getMillis();
		}

		@Override
		public void run() {
			while (true) {
				try {
					nextFillTime += fillRate.getDuration().getMillis();
					long millisUntilNextFill = nextFillTime - currentTimeMillis();
					if (millisUntilNextFill > 0) {
						sleep(millisUntilNextFill);
					}
					
					int expectedAmount = fillRate.getAmount();
					if (maximumCapacity != null) {
						boolean finished = false;
						while (!finished) {
							int currentCapacity = capacity.get();
							int maxAmountToAdd = maximumCapacity - currentCapacity;
							int amountToAdd = min(expectedAmount, maxAmountToAdd);
							finished = capacity.compareAndSet(currentCapacity, currentCapacity + amountToAdd);
						}
					} else {					
						capacity.addAndGet(expectedAmount);
					}
				} catch (InterruptedException e) {
					log.info("Stopping bucket filler thread.");
					return;
				}
			}
		}
		
	}


}
