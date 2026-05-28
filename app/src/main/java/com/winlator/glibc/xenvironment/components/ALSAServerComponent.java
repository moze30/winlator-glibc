package com.winlator.glibc.xenvironment.components;

import com.winlator.glibc.alsaserver.ALSAClientConnectionHandler;
import com.winlator.glibc.alsaserver.ALSARequestHandler;
import com.winlator.glibc.xconnector.UnixSocketConfig;
import com.winlator.glibc.xconnector.XConnectorEpoll;
import com.winlator.glibc.xenvironment.EnvironmentComponent;

public class ALSAServerComponent extends EnvironmentComponent {
    private XConnectorEpoll connector;
    private final UnixSocketConfig socketConfig;

    public ALSAServerComponent(UnixSocketConfig socketConfig) {
        this.socketConfig = socketConfig;
    }

    @Override
    public void start() {
        if (connector != null) return;
        connector = new XConnectorEpoll(socketConfig, new ALSAClientConnectionHandler(), new ALSARequestHandler());
        connector.setMultithreadedClients(true);
        connector.start();
    }

    @Override
    public void stop() {
        if (connector != null) {
            connector.stop();
            connector = null;
        }
    }
}
