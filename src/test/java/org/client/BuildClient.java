package org.client;

import org.example.framework.RemoteRef;

@JmxName("com.intellij:type=BuildService")
public interface BuildClient {
    RemoteRef build();
}
