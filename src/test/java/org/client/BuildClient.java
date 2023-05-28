package org.client;

import org.example.RemoteRef;

@JmxName("com.intellij:type=BuildService")
public interface BuildClient {
    RemoteRef build();
}
