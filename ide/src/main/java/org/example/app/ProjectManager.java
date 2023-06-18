package org.example.app;

import java.util.List;

public class ProjectManager {
    public List<Project> getOpenProjects() {
        return List.of(new Project("intellij"));
    }

    public static ProjectManager getInstance() {
        return new ProjectManager();
    }
}
