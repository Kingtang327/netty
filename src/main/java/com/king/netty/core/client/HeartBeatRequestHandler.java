package com.king.netty.core.client;

import com.king.netty.core.DataFrame;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;

import java.util.concurrent.TimeUnit;

/**
 * @author King
 * @date 2021/7/14
 */
public class HeartBeatRequestHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        DataFrame dataFrame = (DataFrame) msg;
        switch (dataFrame.getCmd()){
            // 如果是心跳应答, release掉, 因为后续的业务handler关心
            case DataFrame.CMD_HEART_BEAT:
                ReferenceCountUtil.release(msg);
                break;
            // 如果是认证成功的响应, 定时发送心跳
            case DataFrame.CMD_AUTHORIZATION:
                // 使用netty自带的任务处理器, 10s发送一次心跳
                ctx.executor().scheduleAtFixedRate(() -> {
                    ctx.writeAndFlush(DataFrame.getHeartBeatDataFrame());
                }, 0, 10, TimeUnit.SECONDS);
                ctx.fireChannelRead(msg);
                break;
            default:
                // 向后传递消息,让业务handler处理
                ctx.fireChannelRead(msg);
                break;
        }
    }
}
