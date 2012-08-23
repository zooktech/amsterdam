Amsterdam
=========

Amsterdam is a java library for throughput management.

It currently contains two key classes:
- *TokenBucket*: An implementation of the [token bucket](http://en.wikipedia.org/wiki/Token_bucket) algorithm 
for defining throughput rates. 
- *RateLimiterFactory*: This is a higher level service for decorating an object with a proxy which will limit 
the rate at which a method can be called. 


Usage
-----

Download the jar from ???.

Wrap an object in a proxy to limit the rate at which calls are made to its methods: 

```java
MyServiceInterface myService = new MyServiceImpl();
int bucketCapacity = 10;
FillRate bucketFillRate = FillRate.perSecond(10);
RateLimit rateLimit = new RateLimit(bucketCapacity, bucketFillRate);
MyServiceInterface rateLimitedService = new RateLimiterFactory().limitRate(myService, 
	methodMatching("rateLimitedMethodRegex.*"), TokenCostFunctions.fixedCost(1), rateLimit);
// ...
try {
	rateLimitedService.doSomething();
} catch (CapacityExceededException e) {
	// Maximim rate exceeded...
}

```

If the rate is exceeded a `CapacityExceededException` (a `RuntimeException`) is thrown.

To use the lower level Token Bucket directly:

```java
TokenBuket tokenBucket = new TokenBucket(0, perMilli(1));
if (tokenBucket.getTokens(2)) {
  // Do something useful...
}
```

License
------

Amsterdam is released under the MIT license:
- http://www.opensource.org/licenses/MIT
