package com.kisro.cloud.net;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

import static com.kisro.cloud.util.ByteBufferUtil.debugAll;

/**
 * @author Kisro
 * @since 2022/10/31
 **/
@Slf4j
public class SelectorReadHandler {
    public static void main(String[] args) {
        try (ServerSocketChannel ssc = ServerSocketChannel.open()) {
            ssc.bind(new InetSocketAddress(9999));
            Selector selector = Selector.open();
            ssc.configureBlocking(false);// 非阻塞模式
            // 注册 accept 事件
            ssc.register(selector, SelectionKey.OP_ACCEPT);

            while (true) {
                int count = selector.select();
                if (count <= 0) {
                    continue;
                }
                // 获取所有事件
                Set<SelectionKey> selectionKeys = selector.selectedKeys();
                // 遍历事件，逐一处理
                Iterator<SelectionKey> iterator = selectionKeys.iterator();
                while (iterator.hasNext()) {
                    SelectionKey selectionKey = iterator.next();
                    // 判断事件类型
                    if (selectionKey.isAcceptable()) {
                        ServerSocketChannel c = (ServerSocketChannel) selectionKey.channel();
                        // 必须处理
                        SocketChannel sc = c.accept();
                        // 非阻塞模式
                        sc.configureBlocking(false);
                        // 注册 read 事件
                        sc.register(selector, SelectionKey.OP_READ);
                        log.debug("{}", sc);
                    } else if (selectionKey.isReadable()) { // 可读事件
                        SocketChannel sc = (SocketChannel) selectionKey.channel();
                        ByteBuffer buffer = ByteBuffer.allocate(128);
                        // 读取数据
                        int readLen = sc.read(buffer);
                        if (readLen == -1) {
                            // 读取完毕 取消事件
                            selectionKey.cancel();
                            sc.close();
                        } else {
                            buffer.flip();
                            debugAll(buffer);
                        }
                    }
                    // 处理完毕，将事件移除
                    iterator.remove();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
