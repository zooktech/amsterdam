package com.unicodecollective.amsterdam;

import java.lang.reflect.Method;

public interface CallSpec {

	boolean matches(Method method, Object[] args);

}
