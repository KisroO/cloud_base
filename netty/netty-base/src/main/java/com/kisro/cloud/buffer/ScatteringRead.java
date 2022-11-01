package com.kisro.cloud.buffer;

import com.kisro.cloud.util.FileConstants;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import static com.kisro.cloud.util.ByteBufferUtil.debugAll;

/**
 * 分散读取
 *
 * @author Kisro
 * @since 2022/10/31
 **/
public class ScatteringRead {
    public static void main(String[] args) {
        try (RandomAccessFile file = new RandomAccessFile(FileConstants.PARTS3_FILE_PATH, "rw")) {
            FileChannel channel = file.getChannel();
            ByteBuffer buffer1 = ByteBuffer.allocate(3);
            ByteBuffer buffer2 = ByteBuffer.allocate(3);
            ByteBuffer buffer3 = ByteBuffer.allocate(5);
            channel.read(new ByteBuffer[]{buffer1, buffer2, buffer3});
            buffer1.flip();
            buffer2.flip();
            buffer3.flip();
            debugAll(buffer1);
            debugAll(buffer2);
            debugAll(buffer3);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
