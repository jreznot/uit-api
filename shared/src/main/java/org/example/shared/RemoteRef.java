package org.example.shared;

import java.io.Serializable;

public record RemoteRef(
        String id,
        String asString,
        String className,
        String identityHashCode
) implements Serializable {
    private static final long serialVersionUID = 42L;
}