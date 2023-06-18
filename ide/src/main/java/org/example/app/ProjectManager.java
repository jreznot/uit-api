package org.example.app;

import java.util.Collections;
import java.util.List;

public class ProjectManager {
    List<Project> getOpenProjects() {
        return Collections.emptyList();
    }

    public static ProjectManager getInstance() {
        return new ProjectManager();
    }
}
