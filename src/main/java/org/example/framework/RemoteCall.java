package org.example.framework;

import java.io.Serializable;

public record RemoteCall(String clazz, String method, Object[] args) implements Serializable {
}
