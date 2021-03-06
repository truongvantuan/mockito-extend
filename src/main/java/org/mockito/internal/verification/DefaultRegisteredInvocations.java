/*
 * Copyright (c) 2007 Mockito contributors
 * This program is made available under the terms of the MIT License.
 */
package org.mockito.internal.verification;

import static org.mockito.internal.util.ObjectMethodsGuru.isToStringMethod;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import org.mockito.invocation.Invocation;

public class DefaultRegisteredInvocations implements RegisteredInvocations, Serializable {

    private static final long serialVersionUID = -2674402327380736290L;
    private final LinkedList<Invocation> invocations = new LinkedList<>();

    @Override
    public void add(Invocation invocation) {
        synchronized (invocations) {
            invocations.add(invocation);
        }
    }

    @Override
    public void removeLast() {
        // TODO: add specific test for synchronization of this block (it is tested by
        // InvocationContainerImplTest at the moment)
        synchronized (invocations) {
            if (!invocations.isEmpty()) {
                invocations.removeLast();
            }
        }
    }

    @Override
    public List<Invocation> getAll() {
        List<Invocation> copiedList;
        synchronized (invocations) {
            copiedList = new LinkedList<>(invocations);
        }

        return copiedList.stream()
                .filter(invocation -> !isToStringMethod(invocation.getMethod()))
                .collect(Collectors.toList());
    }

    @Override
    public void clear() {
        synchronized (invocations) {
            invocations.clear();
        }
    }

    @Override
    public boolean isEmpty() {
        synchronized (invocations) {
            return invocations.isEmpty();
        }
    }
}
