package com.king.netty.core;

import lombok.Data;

/**
 * @author King
 * @date 2021/7/14
 */
@Data
public class DataFrame {

    public static final byte CMD_HEART_BEAT = 0;
    public static final byte CMD_AUTHORIZATION = 1;
    public static final byte CMD_GET_INFO = 2;

    /**
     * 帧     头	        长 度	    命 令	    参 数	            校验和
     * 0x55 0xAA	        2byte	    1byte	    0~1476bytes	        2bytes
     *
     * 长度 = 命令字 + 参数 + 校验和 ，不包括帧头和长度字节；
     * 校验和 = 帧头 + 长度 + 命令字 + 参数的字节累加和。
     *
     */

    public static final byte[] HEADER = new byte[] {0b01010101, (byte) 0b10101010};

    private byte cmd;

    private byte[] params;

    private int crc;

    public DataFrame(byte cmd, byte[] params, int crc) {
        this.cmd = cmd;
        this.params = params;
        this.crc = crc;
    }

    public DataFrame(byte cmd, byte[] params) {
        this.cmd = cmd;
        this.params = params;
        this.crc = getCrc();
    }

    public boolean checkCrc(){
        return getCrc() == this.crc;
    }

    public int getLength() {
        // 长度 = 命令字 + 参数 + 校验和 ，不包括帧头和长度字节；
        return  1 + params.length + 2;
    }

    public int getCrc(){
        // 校验和 = 帧头 + 长度 + 命令字 + 参数的字节累加和。
        int crc = 0;
        // 帧头
        crc += 0b01010101;
        crc += 0b10101010;
        // 长度
        crc += getLength();
        // 参数和
        for (byte b: params){
            crc += (b & 0xFF);
        }
        return crc;
    }

    public static DataFrame getHeartBeatDataFrame(){
        return new DataFrame(DataFrame.CMD_HEART_BEAT, new byte[]{});
    }

    public static DataFrame getAuthorizationDataFrame(){
        String msg = "{\"username\":\"test\", \"password\":\"abcdef\"}";
        return new DataFrame(DataFrame.CMD_AUTHORIZATION, msg.getBytes());
    }

    @Override
    public String toString() {
        return "DataFrame{" +
                "cmd=" + cmd +
                ", params=" + new String(params) +
                '}';
    }
}
