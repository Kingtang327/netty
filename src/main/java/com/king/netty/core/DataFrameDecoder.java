package com.king.netty.core;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

/**
 * @author King
 * @date 2021/7/14
 */
public class DataFrameDecoder extends ByteToMessageDecoder {

    /**
     * 帧     头	        长 度	    命 令	    参 数	            校验和
     * 0x55 0xAA	        2byte	    1byte	    0~1476bytes	        2bytes
     *
     * 长度 = 命令字 + 参数 + 校验和 ，不包括帧头和长度字节；
     * 校验和 = 帧头 + 长度 + 命令字 + 参数的字节累加和。
     *
     */

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        // 长度位
        int length = in.readShort();
        // 命令位
        byte cmd = in.readByte();
        // 参数
        byte[] params = new byte[length-3];
        in.readBytes(params);
        // 校验和
        int crc = in.readShort();
        DataFrame dataFrame = new DataFrame(cmd, params, crc);
        // 计算校验和
        if (dataFrame.checkCrc()){
            // 将解析后的数据加入到list中,传递给后续的channelHandler
            out.add(dataFrame);
        };
    }
}
