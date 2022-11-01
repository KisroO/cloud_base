package com.kisro.cloud.buffer;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import static com.kisro.cloud.util.ByteBufferUtil.debugAll;

/**
 * @author Kisro
 * @since 2022/10/31
 * 字符串和Buffer互转
 **/
public class Buffer01 {
    public static void main(String[] args) {
        ByteBuffer buffer1 = StandardCharsets.UTF_8.encode("你好");
        ByteBuffer buffer2 = Charset.forName("utf-8").encode("你好");
        debugAll(buffer1);
        debugAll(buffer2);
        CharBuffer buffer3 = StandardCharsets.UTF_8.decode(buffer1);
        System.out.println(buffer3.getClass());
        System.out.println(buffer3.toString());
    }
}
