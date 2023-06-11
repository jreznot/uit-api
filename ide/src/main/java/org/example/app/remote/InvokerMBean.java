package org.example.app.remote;

import org.example.shared.RemoteCall;
import org.example.shared.RemoteCallResult;

public interface InvokerMBean {
    RemoteCallResult invoke(RemoteCall call);
}
