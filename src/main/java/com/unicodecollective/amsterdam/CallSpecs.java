package com.unicodecollective.amsterdam;

import java.lang.reflect.Method;
import java.util.regex.Pattern;

public class CallSpecs {
	
	public static CallSpec methodMatching(String regex) {
		final Pattern methodPattern = Pattern.compile(regex);
		return new CallSpec() {
			@Override
			public boolean matches(Method method, Object[] args) {
				return methodPattern.matcher(method.getName()).find();
			}
		};
	}
	
}
