package com.king.netty.core.server;

import com.king.netty.core.DataFrame;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.ReadTimeoutException;
import io.netty.util.ReferenceCountUtil;

/**
 * @author King
 * @date 2021/7/14
 */
public class HeartBeatResponseHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        DataFrame dataFrame = (DataFrame) msg;
        // 如果是心跳请求, release掉, 因为后续的业务handler关心
        if (dataFrame.getCmd() == DataFrame.CMD_HEART_BEAT) {
            ctx.writeAndFlush(DataFrame.getHeartBeatDataFrame());
            ReferenceCountUtil.release(msg);
        } else {// 向后传递消息,让业务handler处理
            ctx.fireChannelRead(msg);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        if (cause instanceof ReadTimeoutException){
            // 断开客户端连接
            ctx.close();
            return;
        }
        super.exceptionCaught(ctx, cause);
    }
}
