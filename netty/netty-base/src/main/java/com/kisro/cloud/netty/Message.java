package com.kisro.cloud.netty;

import lombok.Data;

/**
 * @author Kisro
 * @since 2022/10/31
 **/
@Data
public class Message {
    // 4字节的魔数
    private int magicNumber;

    // 1 字节的版本
    private byte version;

    // 序列化类型 0->jdk 1->json
    private byte serialType;

    // 指令类型
    private byte commandType;

    // 序列号
    private int sequenceId;


}
