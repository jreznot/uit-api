package org.example.shared;

import java.io.Serializable;
import java.util.UUID;

public record ProjectRef(UUID id, String name) implements Serializable {
    static final long serialVersionUID = 42L;
}