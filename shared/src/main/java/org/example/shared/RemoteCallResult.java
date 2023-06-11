package org.example.shared;

import java.io.Serializable;

public record RemoteCallResult(
        Serializable value
) implements Serializable {
    static final long serialVersionUID = 42L;
}
