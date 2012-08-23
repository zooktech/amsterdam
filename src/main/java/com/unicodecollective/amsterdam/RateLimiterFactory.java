/*
 * Copyright (c) 2012 Unicode Collective London > Amsterdam contributors
 * This program is made available under the terms of the MIT License:
 * http://www.opensource.org/licenses/MIT
 */
package com.unicodecollective.amsterdam;

import static java.lang.reflect.Proxy.newProxyInstance;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

public class RateLimiterFactory {
	
	public <T, I> T limitRate(T decorated, CallSpec callSpec, 
			TokenCostFunction tokenCostFunction, RateLimit rateLimit) {
		@SuppressWarnings("unchecked")
		T proxiedInstance = (T) newProxyInstance(decorated.getClass().getClassLoader(), 
				decorated.getClass().getInterfaces(), new RateLimitingInvocationHandler<T>(decorated, callSpec, tokenCostFunction, rateLimit));
		return proxiedInstance;
	}
	
	private static final class RateLimitingInvocationHandler<T> implements InvocationHandler {

		private final T underlying;
		private final CallSpec callSpec;
		private final TokenCostFunction tokenCostFunction;
		private final TokenBucket tokenBucket;

		public RateLimitingInvocationHandler(T underlying, CallSpec callSpec, TokenCostFunction tokenCostFunction, RateLimit rateLimit) {
			this.underlying = underlying;
			this.callSpec = callSpec;
			this.tokenCostFunction = tokenCostFunction;
			this.tokenBucket = new TokenBucket(rateLimit);
		}

		@Override
		public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
			int tokenCost = tokenCostFunction.calculateCost(method, args);
			if (callSpec.matches(method, args) && !tokenBucket.getTokens(tokenCost)) {
				throw new CapacityExceededException();
			} else {
				return method.invoke(underlying, args);
			}
		}
		
	}

}
