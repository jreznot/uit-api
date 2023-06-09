package org.example.shared;

import java.io.Serial;
import java.io.Serializable;

public record Ref(
        int id,
        String className,
        int identityHashCode,
        String asString
) implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
}