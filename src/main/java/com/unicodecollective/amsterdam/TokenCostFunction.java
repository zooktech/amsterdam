/*
 * Copyright (c) 2012 Unicode Collective London > Amsterdam contributors
 * This program is made available under the terms of the MIT License:
 * http://www.opensource.org/licenses/MIT
 */
package com.unicodecollective.amsterdam;

import java.lang.reflect.Method;

public interface TokenCostFunction {

	int calculateCost(Method method, Object[] args);

}
