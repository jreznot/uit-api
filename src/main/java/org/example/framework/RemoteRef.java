package org.example.framework;

import java.io.Serializable;

public record RemoteRef(String name) implements Serializable {
    static final long serialVersionUID = 42L;
}