package com.winlator.glibc.winhandler;

public interface OnGetProcessInfoListener {
    void onGetProcessInfo(int index, int count, ProcessInfo processInfo);
}
