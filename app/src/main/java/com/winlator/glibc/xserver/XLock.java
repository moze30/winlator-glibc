package com.winlator.glibc.xserver;

public interface XLock extends AutoCloseable {
    @Override
    void close();
}
