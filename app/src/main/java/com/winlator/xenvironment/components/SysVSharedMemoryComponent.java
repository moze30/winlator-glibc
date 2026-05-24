package com.winlator.glibc.xenvironment.components;

import com.winlator.glibc.sysvshm.SysVSHMConnectionHandler;
import com.winlator.glibc.sysvshm.SysVSHMRequestHandler;
import com.winlator.glibc.sysvshm.SysVSharedMemory;
import com.winlator.glibc.xconnector.UnixSocketConfig;
import com.winlator.glibc.xconnector.XConnectorEpoll;
import com.winlator.glibc.xenvironment.EnvironmentComponent;
import com.winlator.glibc.xserver.SHMSegmentManager;
import com.winlator.glibc.xserver.XServer;

public class SysVSharedMemoryComponent extends EnvironmentComponent {
    private XConnectorEpoll connector;
    public final UnixSocketConfig socketConfig;
    private SysVSharedMemory sysVSharedMemory;
    private final XServer xServer;

    public SysVSharedMemoryComponent(XServer xServer, UnixSocketConfig socketConfig) {
        this.xServer = xServer;
        this.socketConfig = socketConfig;
    }

    @Override
    public void start() {
        if (connector != null) return;
        sysVSharedMemory = new SysVSharedMemory();
        connector = new XConnectorEpoll(socketConfig, new SysVSHMConnectionHandler(sysVSharedMemory), new SysVSHMRequestHandler());
        connector.start();

        xServer.setSHMSegmentManager(new SHMSegmentManager(sysVSharedMemory));
    }

    @Override
    public void stop() {
        if (connector != null) {
            connector.stop();
            connector = null;
        }

        sysVSharedMemory.deleteAll();
    }
}
