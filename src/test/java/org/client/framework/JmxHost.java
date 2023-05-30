package org.client.framework;

public class JmxHost {
    private String address;
    private String user;
    private String password;

    public JmxHost(String user, String password, String address) {
        this.user = user;
        this.password = password;
        this.address = address;
    }

    public String getAddress() {
        return address;
    }

    public String getUser() {
        return user;
    }

    public String getPassword() {
        return password;
    }
}