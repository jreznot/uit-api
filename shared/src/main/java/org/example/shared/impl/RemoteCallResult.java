package org.example.shared.impl;

import java.io.Serial;
import java.io.Serializable;

public record RemoteCallResult(
        Serializable value
) implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
}
