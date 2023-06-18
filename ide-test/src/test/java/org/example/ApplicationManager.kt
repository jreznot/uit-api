package org.example

import org.example.remoting.Remote

@Remote("org.example.app.ApplicationManager")
interface ApplicationManager {
    fun getApplication(): Application
}

@Remote("org.example.app.Application")
interface Application