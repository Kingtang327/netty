package com.king.netty.core;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * @author King
 * @date 2021/7/14
 */
public class DataFrameEncoder extends MessageToByteEncoder<DataFrame> {

    @Override
    protected void encode(ChannelHandlerContext ctx, DataFrame msg, ByteBuf out) throws Exception {
        // 写出帧头
        out.writeBytes(DataFrame.HEADER);
        // 写出长度
        out.writeShort(msg.getLength());
        // 写出命令
        out.writeByte(msg.getCmd());
        // 参 数
        out.writeBytes(msg.getParams());
        // 校验和
        out.writeShort(msg.getCrc());
    }
}
