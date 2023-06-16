package org.example.shared;

import java.io.Serializable;

public record RemoteCallResult(
        Serializable value
) implements Serializable {
    private static final long serialVersionUID = 1L;
}
