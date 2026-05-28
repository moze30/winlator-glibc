package com.winlator.glibc.xserverbridge;

import com.winlator.glibc.winhandler.WinHandler;
import com.winlator.glibc.xserver.Pointer;
import com.winlator.glibc.xserver.XKeycode;

/**
 * 提供函数用于外界与 xserver 的基本交互
 */
public interface IXServerBridge {
    WinHandler getWinHandler();
    int getScreenWidth();
    int getScreenHeight();
    boolean isStretchFullscreen();
    int getPointerX();
    int getPointerY();
    void injectPointerMove(int x, int y);
    void injectPointerMoveDelta(int dx, int dy);
    void injectPointerButtonPress(int btnCode);
    void injectPointerButtonRelease(int btnCode);
    boolean isPointerButtonPressed(int btnCode);
    void injectKeyPress(byte keycode, int keysym);
    void injectKeyRelease(byte keycode);
}

