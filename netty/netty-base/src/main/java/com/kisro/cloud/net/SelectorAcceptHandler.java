package com.kisro.cloud.net;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

/**
 * @author Kisro
 * @since 2022/10/31
 **/
@Slf4j
public class SelectorAcceptHandler {
    public static void main(String[] args) {
        try (ServerSocketChannel ssc = ServerSocketChannel.open()) {
            ssc.bind(new InetSocketAddress(9999));
            Selector selector = Selector.open();
            ssc.configureBlocking(false);
            ssc.register(selector, SelectionKey.OP_ACCEPT);

            while (true) {
                int count = selector.select();
//                int selectNow = selector.selectNow();
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
                        log.debug("{}", sc);
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
