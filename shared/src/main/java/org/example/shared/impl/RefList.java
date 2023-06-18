package org.example.shared.impl;

import org.example.shared.Ref;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

public record RefList(
        int id,
        String className,
        List<Ref> items
) implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
}
