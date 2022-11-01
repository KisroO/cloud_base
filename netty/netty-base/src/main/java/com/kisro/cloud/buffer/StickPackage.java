package com.kisro.cloud.buffer;

import java.nio.ByteBuffer;

import static com.kisro.cloud.util.ByteBufferUtil.debugAll;

/**
 * 解决粘包问题
 * <p>
 * 原始数据有3条为
 * Hello,world\n
 * I'm zhangsan\n
 * How are you?\n
 * <p>
 * 变成了下面的两个 byteBuffer (黏包，半包)
 * Hello,world\nI'm zhangsan\nHo
 * w are you?\n
 * <p>
 * 将错乱的数据恢复成原始的按 \n 分隔的数据
 *
 * @author Kisro
 * @since 2022/10/31
 **/
public class StickPackage {
    public static void main(String[] args) {
        ByteBuffer source = ByteBuffer.allocate(32);
        //                     11            24
        source.put("Hello,world\nI'm zhangsan\nHo".getBytes());
        split(source);

        source.put("w are you?\nhaha!\n".getBytes());
        split(source);
    }

    private static void split(ByteBuffer source) {
        // 切换到读模式
        source.flip();
        int oldLimit = source.limit();
        System.out.println("old limit: " + oldLimit);
        for (int i = 0; i < oldLimit; i++) {
            // 使用get(i) position不会往前走
            if (source.get(i) == '\n') {
                System.out.println("i: " + i);
                System.out.println("before put position: " + source.position());
                // 长度
                ByteBuffer target = ByteBuffer.allocate(i + 1 - source.position());
                // 0~limit 一个完整的消息
                source.limit(i + 1);
                // put进 target 内部使用的get()，所以source的position会往前走
                target.put(source);
                System.out.println("after before position: " + source.position());
                debugAll(target);
                source.limit(oldLimit);
            }
        }
        // 剩余数据往前压缩
        source.compact();
    }
}
