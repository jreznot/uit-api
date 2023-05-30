package org.example.framework;

public interface UiRobotMBean {
    RemoteRef findByXPath(String xpath);
    RemoteRef findByText(String text);

    void click(RemoteRef ref);
    void sendKeys(RemoteRef ref);
}
