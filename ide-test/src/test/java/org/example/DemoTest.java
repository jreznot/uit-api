package org.example;

import org.example.remoting.Connection;
import org.junit.Test;

public class DemoTest {
    @Test
    public void test() {
        Connection connection = new Connection();
        BuildClient client = connection.getInstance(BuildClient.class);

        connection.withSession(() -> {
            BuildResult done = client.build(null, "some");
            done.getArgs();

            System.out.println(done);
        });
    }
}
