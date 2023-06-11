package org.example.app.remote;

import org.example.shared.RemoteCall;
import org.example.shared.RemoteCallResult;

public class Invoker implements InvokerMBean {
    @Override
    public RemoteCallResult invoke(RemoteCall call) {
        return new RemoteCallResult(null);
    }
}
