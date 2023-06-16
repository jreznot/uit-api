package org.example.shared;

import java.io.Serial;
import java.io.Serializable;
import java.util.UUID;

public record ProjectRef(UUID id, String name) implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
}