package com.king.netty.core.server;

import com.king.netty.core.DataFrame;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;

/**
 * @author King
 * @date 2021/7/14
 */
public class AuthorizationResponseHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        DataFrame dataFrame = (DataFrame) msg;
        if (dataFrame.getCmd() == DataFrame.CMD_AUTHORIZATION) {
            String auth = "{\"username\":\"test\", \"password\":\"abcdef\"}";
            byte[] params = dataFrame.getParams();
            if (auth.equals(new String(params))){
                // 认证成功
                ctx.writeAndFlush(new DataFrame(dataFrame.getCmd(), "success".getBytes()));
            }else {
                // 认证失败
                ctx.writeAndFlush(new DataFrame(dataFrame.getCmd(), "fail".getBytes()));
            }
            // 释放消息
            ReferenceCountUtil.release(msg);
        }else {
            // 非认证的请求,交给后续业务处理
            ctx.fireChannelRead(msg);
        }
    }
}
