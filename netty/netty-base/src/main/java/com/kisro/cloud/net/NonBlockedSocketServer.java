package com.kisro.cloud.net;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;

import static com.kisro.cloud.util.ByteBufferUtil.debugAll;

/**
 * @author Kisro
 * @since 2022/10/31
 **/
@Slf4j
public class NonBlockedSocketServer {
    public static void main(String[] args) {
        ByteBuffer buffer = ByteBuffer.allocate(16);
        try {
            ServerSocketChannel ssc = ServerSocketChannel.open();
            ssc.bind(new InetSocketAddress(9999));
            ssc.configureBlocking(false);// 非阻塞模式
            // 连接集合
            List<SocketChannel> channels = new ArrayList<>();
            while (true) {
                // accept 建立与客户端连接，SocketChannel用来与客户端通信
                log.debug("connecting...");
                SocketChannel sc = ssc.accept();// 非阻塞，线程还会继续运行，如果没有连接建立，但sc是null
                if (sc != null) {
                    log.debug("connected... {}", sc);
                    sc.configureBlocking(false); // 非阻塞模式
                    channels.add(sc);
                }
                for (SocketChannel channel : channels) {
                    // 接收客户端发送的数据
                    int read = channel.read(buffer);// 非阻塞，线程仍然会继续运行，如果没有读到数据，read 返回 0
                    if (read > 0) {
                        buffer.flip();
                        debugAll(buffer);
                        buffer.clear();
                        log.debug("after read...{}", channel);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
