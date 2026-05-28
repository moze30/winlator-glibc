package com.winlator.glibc.xserver.requests;

import static com.winlator.glibc.xserver.XClientRequestHandler.RESPONSE_CODE_SUCCESS;

import com.winlator.glibc.xconnector.XInputStream;
import com.winlator.glibc.xconnector.XOutputStream;
import com.winlator.glibc.xconnector.XStreamLock;
import com.winlator.glibc.xserver.XClient;
import com.winlator.glibc.xserver.errors.XRequestError;
import com.winlator.glibc.xserver.extensions.Extension;

import java.io.IOException;

public abstract class ExtensionRequests {
    public static void queryExtension(XClient client, XInputStream inputStream, XOutputStream outputStream) throws IOException, XRequestError {
        short length = inputStream.readShort();
        inputStream.skip(2);
        String name = inputStream.readString8(length);
        Extension extension = client.xServer.getExtensionByName(name);
        try (XStreamLock lock = outputStream.lock()) {
            outputStream.writeByte(RESPONSE_CODE_SUCCESS);
            outputStream.writeByte((byte)0);
            outputStream.writeShort(client.getSequenceNumber());
            outputStream.writeInt(0);

            if (extension != null) {
                outputStream.writeByte((byte)1);
                outputStream.writeByte(extension.getMajorOpcode());
                outputStream.writeByte(extension.getFirstEventId());
                outputStream.writeByte(extension.getFirstErrorId());
                outputStream.writePad(20);
            }
            else {
                outputStream.writeByte((byte)0);
                outputStream.writePad(23);
            }
        }
    }
}
