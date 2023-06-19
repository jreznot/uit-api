package org.example

import org.example.remoting.Remote

@Remote("org.example.app.FileEditorManager")
interface FileEditorManager {
    fun getEditors(): List<FileEditor>
}

@Remote("org.example.app.FileEditor")
interface FileEditor