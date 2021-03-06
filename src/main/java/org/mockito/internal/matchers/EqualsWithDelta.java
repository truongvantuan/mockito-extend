/*
 * Copyright (c) 2007 Mockito contributors
 * This program is made available under the terms of the MIT License.
 */
package org.mockito.internal.matchers;

import java.io.Serializable;

import java.util.Objects;
import org.mockito.ArgumentMatcher;

public class EqualsWithDelta implements ArgumentMatcher<Number>, Serializable {

    private final Number wanted;
    private final Number delta;

    public EqualsWithDelta(Number value, Number delta) {
        this.wanted = value;
        this.delta = delta;
    }

    @Override
    public boolean matches(Number actual) {
        if (wanted == null ^ actual == null) {
            return false;
        }

        if (Objects.equals(wanted, actual)) {
            return true;
        }

        return wanted.doubleValue() - delta.doubleValue() <= actual.doubleValue()
                && actual.doubleValue() <= wanted.doubleValue() + delta.doubleValue();
    }

    @Override
    public String toString() {
        return "eq(" + wanted + ", " + delta + ")";
    }
}
