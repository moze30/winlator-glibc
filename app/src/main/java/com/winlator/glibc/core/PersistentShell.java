package com.winlator.glibc.core;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

public class PersistentShell {
    private Process process;
    private OutputStreamWriter writer;
    private BufferedReader reader;

    // 初始化：创建一次 root shell 进程
    public void initShell() {
        try {
            if (process == null) {
                // 启动 root 权限的 sh 进程（持久运行）
                // TODO 要redirect不然收不到吧
                process = new ProcessBuilder("/bin/sh")
                        .redirectErrorStream(true)
                        .start();
                // 进程输入流：写入命令
                writer = new OutputStreamWriter(process.getOutputStream());
                // 进程输出流：读取结果
                reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 向持久进程发送命令，读取返回值
     * @return 仅读取一行
     */
    public String execCommand(String command) {
        try {
            if (writer == null || reader == null) return "";

            // 1. 写入命令（必须加 \n 回车执行）
            writer.write(command);
            writer.write('\n');
            writer.flush();

            // 2. 读取结果
            return  reader.readLine();
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    // 销毁进程（退出APP时调用）
    public void destroy() {
        try {
            if (writer != null) writer.close();
            if (reader != null) reader.close();
            if (process != null) process.destroy();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}