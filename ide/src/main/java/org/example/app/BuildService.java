package org.example.app;

public class BuildService {
    public BuildResult build(Project project, String args) {
        return new BuildResult();
    }

    public static BuildService getInstance() {
        return new BuildService();
    }
}

