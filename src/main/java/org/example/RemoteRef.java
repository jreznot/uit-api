package org.example;

import java.io.Serializable;

public class RemoteRef implements Serializable {
    static final long serialVersionUID = 42L;

    private String name;

    public RemoteRef(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "RemoteRef{" +
                "name='" + name + '\'' +
                '}';
    }
}