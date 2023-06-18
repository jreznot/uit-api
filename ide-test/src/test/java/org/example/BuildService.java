package org.example;

import org.example.remoting.Remote;
import org.example.shared.Ref;

@Remote("org.example.app.BuildService")
interface BuildService {
    BuildResult build(Ref projectRef, String args);
}

@Remote("org.example.app.BuildResult")
interface BuildResult {
    String getArgs();
}