package org.example;

import org.example.remoting.*;
import org.example.shared.ProjectRef;

@Remote("org.example.app.BuildService")
@Service(ServiceLevel.APP)
interface BuildClient {
    BuildResult build(ProjectRef projectRef, String args);
}

@Remote("com.example.BuildResult")
interface BuildResult {
    String getArgs();
}