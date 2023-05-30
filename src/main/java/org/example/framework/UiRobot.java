package org.example.framework;

public class UiRobot implements UiRobotMBean {
    @Override
    public RemoteRef findByXPath(String xpath) {
        throw new UnsupportedOperationException();
    }

    @Override
    public RemoteRef findByText(String text) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void click(RemoteRef ref) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void sendKeys(RemoteRef ref) {
        throw new UnsupportedOperationException();
    }
}
