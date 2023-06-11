package org.example;

import org.example.remoting.Remote;
import org.example.remoting.Service;
import org.example.remoting.ServiceLevel;
import org.example.shared.ProjectRef;

@Remote("org.example.app.BuildService")
@Service(ServiceLevel.PROJECT)
public interface BuildClient {
    BuildResult build(ProjectRef projectRef, String args);
}

@Remote("com.Result")
interface BuildResult {
    String getArgs();
}