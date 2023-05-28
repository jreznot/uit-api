package org.example;

import javax.management.*;
import java.lang.management.ManagementFactory;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        try {
            ObjectName objectName = new ObjectName("com.intellij:type=BuildService");
            MBeanServer server = ManagementFactory.getPlatformMBeanServer();
            server.registerMBean(new BuildService(), objectName);
        } catch (MalformedObjectNameException | InstanceAlreadyExistsException |
                 MBeanRegistrationException | NotCompliantMBeanException e) {
            // handle exceptions
            System.err.println(e);
            e.printStackTrace();
            System.exit(-1);
        }

        while (true) {
            Thread.sleep(5000);
        }
    }
}

class BuildService implements BuildServiceMBean {
    @Override
    public RemoteRef build() {
        return new RemoteRef("done");
    }
}
