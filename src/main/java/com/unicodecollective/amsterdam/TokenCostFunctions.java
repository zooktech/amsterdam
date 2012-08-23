package com.unicodecollective.amsterdam;

import java.lang.reflect.Method;

public class TokenCostFunctions {
	
	public static TokenCostFunction fixedCost(final int cost) {
		return new TokenCostFunction() {
			@Override
			public int calculateCost(Method method, Object[] args) {
				return cost;
			}
		};
	}

}
