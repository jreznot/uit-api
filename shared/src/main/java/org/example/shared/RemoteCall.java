package org.example.shared;

import java.io.Serializable;
import java.util.UUID;

public record RemoteCall(
        int sessionId,
        OnDispatcher dispatcher,
        LockSemantics lockSemantics,
        UUID projectId,
        String className,
        String methodName,
        Object[] args,
        boolean isService,
        boolean isStatic
) implements Serializable {
    static final long serialVersionUID = 1L;
}