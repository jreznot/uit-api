package org.example.app;

import javax.management.*;
import javax.swing.*;
import java.awt.*;
import java.lang.management.ManagementFactory;

public class Main {
    // -Dcom.sun.management.jmxremote=true
    // -Dcom.sun.management.jmxremote.port=7777
    // -Dcom.sun.management.jmxremote.authenticate=false
    // -Dcom.sun.management.jmxremote.ssl=false
    // -Djava.rmi.server.hostname=localhost

    public static void main(String[] args) {
        registerJMX();

        JFrame frame = new JFrame("IntelliJ IDEA");
        JLabel label = new JLabel("JMX is running on "
                + System.getProperty("com.sun.management.jmxremote.port"));
        label.setBorder(BorderFactory.createEmptyBorder(50, 50, 50, 50));

        frame.getContentPane().setLayout(new BorderLayout());
        frame.getContentPane().add(label, BorderLayout.CENTER);
        frame.setSize(300, 200);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        frame.setVisible(true);
    }

    private static void registerJMX() {
        try {
            ObjectName objectName = new ObjectName("com.intellij:type=BuildService");
            MBeanServer server = ManagementFactory.getPlatformMBeanServer();
            server.registerMBean(new BuildService(), objectName);
        } catch (MalformedObjectNameException | InstanceAlreadyExistsException |
                 MBeanRegistrationException | NotCompliantMBeanException e) {
            //noinspection ThrowablePrintedToSystemOut
            System.err.println(e);
            e.printStackTrace();
            System.exit(-1);
        }
    }
}