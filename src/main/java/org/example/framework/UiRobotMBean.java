package org.example.framework;

public interface UiRobotMBean {
    RemoteRef findByXPath(String xpath);
    RemoteRef findByText(String text);
}
