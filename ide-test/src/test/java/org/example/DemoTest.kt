package org.example;

import org.example.remoting.Connection;
import org.junit.Test;

public class DemoTest {
    final Connection connection = new Connection();

    @Test
    public void test() {
        BuildService buildService = connection.getInstance(BuildService.class);

        connection.withWriteAction(() -> {
            BuildResult done = buildService.build(null, "some");
            String args = done.getArgs();

            System.out.println(args);
        });
    }
}
