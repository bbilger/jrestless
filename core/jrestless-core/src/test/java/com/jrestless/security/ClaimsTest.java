package com.jrestless.security;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

public abstract class ClaimsTest<T extends Claims> {
	private final Map<String, Object> claimsMap = new HashMap<>();

	private final Method getter;
    private final String mapKey;
    private final Object mapValue;
    private final Object getterValue;
    private final boolean testNull;
    private final boolean testClassCastException;

    public ClaimsTest(String getterName, String mapKey, Object mapValue)
			throws NoSuchMethodException, SecurityException {
    	this(getterName, mapKey, mapValue, mapValue, true);
    }

    public ClaimsTest(String getterName, String mapKey, Object mapValue, Object getterValue, boolean testNull)
			throws NoSuchMethodException, SecurityException {
    	this(getterName, mapKey, mapValue, getterValue, testNull, testNull);
    }

	public ClaimsTest(String getterName, String mapKey, Object mapValue, Object getterValue, boolean testNull, boolean testClassCastException)
			throws NoSuchMethodException, SecurityException {
		this.mapKey = mapKey;
		this.mapValue = mapValue;
		this.getterValue = getterValue;
		this.getter = getGetterByName(getterName);
		this.testNull = testNull;
		this.testClassCastException = testClassCastException;
	}

	Map<String, Object> getClaimsMap() {
		return claimsMap;
	}

	abstract T getClaims();

	abstract Method getGetterByName(String getterName) throws NoSuchMethodException, SecurityException;

	@Test
	public void get_MapValueGiven_ShouldReturnMapValue()
			throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		claimsMap.clear();
		claimsMap.put(mapKey, mapValue);
		assertEquals(getterValue, getter.invoke(getClaims()));
	}

	@Test
	public void get_NoMapValueGiven_ShouldReturnNull()
			throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		if (testNull) {
			claimsMap.clear();
			assertNull(getter.invoke(getClaims()));
		}
	}

	@Test
	public void get_InvalidTypeValueGiven_ShouldThrowClassCastException()
			throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		if (testClassCastException) {
			claimsMap.clear();
			claimsMap.put(mapKey, new Object());
			try {
				getter.invoke(getClaims());
			} catch (InvocationTargetException ite) {
				assertTrue(ite.getTargetException() instanceof ClassCastException);
			}
		}
	}
}
