package org.example;

import org.example.remoting.Remote;
import org.example.remoting.sdk.Project;

@Remote("org.example.app.BuildService")
interface BuildService {
    BuildResult build(Project projectRef, String args);
}

@Remote("org.example.app.BuildResult")
interface BuildResult {
    String getArgs();
}