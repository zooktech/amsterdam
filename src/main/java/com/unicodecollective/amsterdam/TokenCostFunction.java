package com.unicodecollective.amsterdam;

import java.lang.reflect.Method;

public interface TokenCostFunction {

	int calculateCost(Method method, Object[] args);

}
