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
 * 阻塞式的网络服务端
 *
 * @author Kisro
 * @since 2022/10/31
 **/
@Slf4j
public class BlockedSocketServer {
    public static void main(String[] args) {
        ByteBuffer buffer = ByteBuffer.allocate(16);
        try {
            ServerSocketChannel ssc = ServerSocketChannel.open();
            ssc.bind(new InetSocketAddress(9999));
            // 连接集合
            List<SocketChannel> channels = new ArrayList<>();
            while (true) {
                // accept 建立与客户端连接，SocketChannel用来与客户端通信
                log.debug("connecting...");
                SocketChannel sc = ssc.accept();// 阻塞方法，线程停止运行
                log.debug("connected... {}", sc);
                channels.add(sc);
                for (SocketChannel channel : channels) {
                    // 接收客户端发送的数据
                    log.debug("before read... {}", channel);
                    channel.read(buffer);// 阻塞方法，线程停止运行
                    buffer.flip();
                    debugAll(buffer);
                    buffer.clear();
                    log.debug("after read... {}", channel);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
