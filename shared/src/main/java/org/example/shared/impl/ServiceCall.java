package org.example.shared.impl;

import org.example.shared.LockSemantics;
import org.example.shared.OnDispatcher;
import org.example.shared.ProjectRef;
import org.jetbrains.annotations.Nullable;

import java.io.Serial;

public final class ServiceCall extends RemoteCall {
    @Serial
    private static final long serialVersionUID = 1L;

    private final ProjectRef projectRef;

    public ServiceCall(int sessionId,
                       String pluginId,
                       OnDispatcher dispatcher,
                       LockSemantics lockSemantics,
                       String className,
                       String methodName,
                       Object[] args,
                       @Nullable ProjectRef projectRef) {
        super(sessionId, pluginId, dispatcher, lockSemantics, className, methodName, args);
        this.projectRef = projectRef;
    }

    public ProjectRef getProjectRef() {
        return projectRef;
    }

    @Override
    public String toString() {
        return "ServiceCall{" +
                "className=" + getClassName() +
                "methodName=" + getMethodName() +
                "projectRef=" + projectRef +
                '}';
    }
}
