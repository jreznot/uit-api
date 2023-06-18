package org.example.app.remote;

import org.example.shared.impl.RemoteCall;
import org.example.shared.impl.RemoteCallResult;

@SuppressWarnings("unused")
public interface InvokerMBean {
    void ping();

    RemoteCallResult invoke(RemoteCall call);

    int newSession();
    void cleanup(int sessionId);
}
