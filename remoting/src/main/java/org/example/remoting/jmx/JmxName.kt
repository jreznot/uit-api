package org.example.remoting.jmx

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
annotation class JmxName(val value: String)