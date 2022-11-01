package com.kisro.cloud.net;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

/**
 * @author Kisro
 * @since 2022/10/31
 **/
public class BlockedSocketClient {
    public static void main(String[] args) {
        try {
            SocketChannel sc = SocketChannel.open();
            sc.connect(new InetSocketAddress("localhost", 9999));
            ByteBuffer buffer = ByteBuffer.allocate(10).put("hello".getBytes());
            sc.write(buffer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
