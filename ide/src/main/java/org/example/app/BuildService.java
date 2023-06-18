package org.example.app;

class BuildService {
    public BuildResult build(Project project, String args) {
        return new BuildResult();
    }

    public static BuildService getInstance() {
        return new BuildService();
    }
}

class BuildResult {
    String getArgs() {
        return "no-args";
    }
}