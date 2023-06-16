package org.example.shared;

import java.io.Serial;
import java.io.Serializable;

public record Ref(
        String id,
        String asString,
        String className,
        String identityHashCode
) implements Serializable {
    @Serial
    private static final long serialVersionUID = 42L;
}