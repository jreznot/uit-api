package org.example.app.remote;

import org.example.shared.RemoteCall;
import org.example.shared.RemoteCallResult;

@SuppressWarnings("unused")
public interface InvokerMBean {
    RemoteCallResult invoke(RemoteCall call);

    int newSession();
    void cleanup(int sessionId);
}
