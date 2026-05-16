package com.winlator.glibc.xserver.extensions;

import com.winlator.glibc.xconnector.XInputStream;
import com.winlator.glibc.xconnector.XOutputStream;
import com.winlator.glibc.xserver.XClient;
import com.winlator.glibc.xserver.errors.XRequestError;

import java.io.IOException;

public interface Extension {
    String getName();

    byte getMajorOpcode();

    byte getFirstErrorId();

    byte getFirstEventId();

    void handleRequest(XClient client, XInputStream inputStream, XOutputStream outputStream) throws IOException, XRequestError;
}
