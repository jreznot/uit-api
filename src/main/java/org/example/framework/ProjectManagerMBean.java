package org.example.framework;

import java.util.List;

public interface ProjectManagerMBean {
    List<ProjectRef> getProjects();

    ProjectRef getSingle();
}
