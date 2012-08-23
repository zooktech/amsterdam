Amsterdam
=========

Amsterdam is a java library for throughput management.


Components
----------

- *TokenBucket*: An implementation of the [token bucket](http://en.wikipedia.org/wiki/Token_bucket) algorithm 
for defining throughput rates. 
- *RateLimiterFactory*: This is a higher level service for decorating an object with a proxy which will limit 
the rate at which a method can be called. 


Usage
-----

```java
TokenBuket tokenBucket = new TokenBucket(0, perMilli(1));
if (tokenBucket.getTokens(2)) {
  // Do something useful...
}
```

```java
MyServiceInterface myService = new MyServiceImpl();
int initialCapacity = 10;
int maxCapacity = 10; 
RateLimit rateLimit = new RateLimit(initialCapacity, maxCapacity, perSecond(0));
Mynew RateLimiterFactory().limitRate(myService, 
	methodMatching("rateLimitedMethodRegex.*"), TokenCostFunctions.fixedCost(1), rateLimit);
// ...
try {
	myService.doSomething();
} catch (CapacityExceededException e) {
	// Maximim rate exceeded...
}

```