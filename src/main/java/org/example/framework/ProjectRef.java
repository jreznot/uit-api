package org.example.framework;

import java.io.Serializable;
import java.util.UUID;

public record ProjectRef(UUID id) implements Serializable {
    static final long serialVersionUID = 42L;
}