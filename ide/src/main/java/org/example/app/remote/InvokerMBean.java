package org.example.app.remote;

import org.example.shared.impl.RemoteCall;
import org.example.shared.impl.RemoteCallResult;

@SuppressWarnings("unused")
public interface InvokerMBean {
    RemoteCallResult invoke(RemoteCall call);

    int newSession();
    void cleanup(int sessionId);
}
