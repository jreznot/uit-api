package org.example.app;

public class FileEditorManager {
    public static FileEditorManager getInstance() {
        return new FileEditorManager();
    }

    public FileEditor[] getEditors() {
        return new FileEditor[]{
                new FileEditor("Main.java"),
                new FileEditor("application.properties")
        };
    }
}
