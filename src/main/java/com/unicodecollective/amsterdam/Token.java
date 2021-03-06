/*
 * Copyright (c) 2012 Unicode Collective London > Amsterdam contributors
 * This program is made available under the terms of the MIT License:
 * http://www.opensource.org/licenses/MIT
 */
package com.unicodecollective.amsterdam;

public class Token {

	private final boolean valid;

	public Token(boolean valid) {
		this.valid = valid;
	}

	public boolean isValid() {
		return valid;
	}

	public static Token validToken() {
		return new Token(true);
	}

	public static Token invalidToken() {
		return new Token(false);
	}

}
