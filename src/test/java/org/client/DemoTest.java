package org.client;

import org.example.framework.RemoteRef;
import org.junit.Test;

import static org.client.JmxCallHandler.jmx;

public class DemoTest {
    @Test
    public void test() {
        BuildClient client = jmx(BuildClient.class);
        RemoteRef done = client.build();
        System.out.println(done);
    }
}
