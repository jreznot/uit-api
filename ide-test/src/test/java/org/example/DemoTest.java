package org.example;

import org.example.remoting.Connection;
import org.junit.Test;

public class DemoTest {
    @Test
    public void test() {
        BuildClient client = new Connection().getService(BuildClient.class);

        BuildResult done = client.build(null, "some");
        done.getArgs();

        System.out.println(done);
    }
}
