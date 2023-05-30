package org.example.app;

import org.example.framework.RemoteRef;
import org.jetbrains.annotations.TestOnly;

public interface BuildServiceMBean {
    @SuppressWarnings("unused")
    @TestOnly
    RemoteRef build();
}