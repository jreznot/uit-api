package org.example.app;

public class FileEditor {
    private final String name;

    public FileEditor(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "FileEditor{" +
                "name='" + name + '\'' +
                '}';
    }
}
