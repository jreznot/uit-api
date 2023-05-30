package org.example.app;

import org.example.framework.RemoteRef;

class BuildService implements BuildServiceMBean {
    @Override
    public RemoteRef build() {
        return new RemoteRef("done");
    }
}
