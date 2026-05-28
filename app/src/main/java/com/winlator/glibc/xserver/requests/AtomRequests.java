package com.winlator.glibc.xserver.requests;

import static com.winlator.glibc.xserver.XClientRequestHandler.RESPONSE_CODE_SUCCESS;

import com.winlator.glibc.xconnector.XInputStream;
import com.winlator.glibc.xconnector.XOutputStream;
import com.winlator.glibc.xconnector.XStreamLock;
import com.winlator.glibc.xserver.Atom;
import com.winlator.glibc.xserver.XClient;
import com.winlator.glibc.xserver.errors.BadAtom;
import com.winlator.glibc.xserver.errors.XRequestError;

import java.io.IOException;

public abstract class AtomRequests {
    public static void internAtom(XClient client, XInputStream inputStream, XOutputStream outputStream) throws IOException, XRequestError {
        boolean onlyIfExists = client.getRequestData() == 1;
        short length = inputStream.readShort();
        inputStream.skip(2);
        String name = inputStream.readString8(length);
        int id = onlyIfExists ? Atom.getId(name) : Atom.internAtom(name);
        if (id < 0) throw new BadAtom(id);

        try (XStreamLock lock = outputStream.lock()) {
            outputStream.writeByte(RESPONSE_CODE_SUCCESS);
            outputStream.writeByte((byte)0);
            outputStream.writeShort(client.getSequenceNumber());
            outputStream.writeInt(0);
            outputStream.writeInt(id);
            outputStream.writePad(20);
        }
    }
}
