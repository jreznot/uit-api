package org.example;

import org.example.remoting.*;
import org.example.shared.ProjectRef;

@Remote("org.example.app.BuildService")
interface BuildService {
    BuildResult build(ProjectRef projectRef, String args);
}

@Remote("org.example.app.BuildResult")
interface BuildResult {
    String getArgs();
}