package com.winlator.glibc.xconnector;

public interface ConnectionHandler {
    void handleConnectionShutdown(Client client);

    void handleNewConnection(Client client);
}