package com.kisro.cloud.net;

import java.io.IOException;
import java.net.Socket;

/**
 * @author Kisro
 * @since 2022/10/31
 **/
public class SelectorClient {
    public static void main(String[] args) {
        try (Socket socket = new Socket("localhost", 9999)) {
            System.out.println(socket);
            socket.getOutputStream().write("world".getBytes());
            System.in.read();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
