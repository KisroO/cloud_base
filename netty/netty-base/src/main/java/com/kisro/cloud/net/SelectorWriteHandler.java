package com.kisro.cloud.net;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.Set;

/**
 * @author Kisro
 * @since 2022/10/31
 **/
@Slf4j
public class SelectorWriteHandler {
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
                    iterator.remove();
                    // 判断事件类型
                    if (selectionKey.isAcceptable()) {
                        // 必须处理
                        SocketChannel sc = ssc.accept();
                        // 非阻塞模式
                        sc.configureBlocking(false);
                        // 注册 read 事件
                        sc.register(selector, SelectionKey.OP_READ);
                        // 向客户端发送内容
                        StringBuilder sb = new StringBuilder();
                        for (int i = 0; i < 3000000; i++) {
                            sb.append("a");
                        }
                        ByteBuffer buffer = Charset.defaultCharset().encode(sb.toString());
                        int write = sc.write(buffer);
                        // write 表示实际写了多少字节
                        log.debug("实际写入字节数：{}", write);
                        // 如果有剩余未读字节，才需要关注写事件
                        if (buffer.hasRemaining()) {
                            // read 1  write 4
                            // 在原有关注事件的基础上，多关注 写事件
                            selectionKey.interestOps(selectionKey.interestOps() + SelectionKey.OP_WRITE);
                            // 把 buffer 作为附件加入 selectionKey
                            selectionKey.attach(buffer);
                        }
                    } else if (selectionKey.isWritable()) { // 可写事件
                        ByteBuffer buffer = (ByteBuffer) selectionKey.attachment();
                        SocketChannel sc = (SocketChannel) selectionKey.channel();
                        int write = sc.write(buffer);
                        log.debug("实际写入字节：{}", write);
                        if (!buffer.hasRemaining()) {
                            // 写入完毕
                            selectionKey.interestOps(selectionKey.interestOps() - SelectionKey.OP_WRITE);
                            selectionKey.attach(null);
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
