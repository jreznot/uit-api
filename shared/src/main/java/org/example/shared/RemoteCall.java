package org.example.shared;

import java.io.Serializable;
import java.util.List;

public record RemoteCall(
        String className,
        String methodName,
        List<Object> args,
        boolean isService,
        boolean isStatic
) implements Serializable {
    static final long serialVersionUID = 42L;
}