package org.amie.exercise.handler;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import org.amie.exercise.bean.Request;
import org.amie.exercise.bean.Response;

import java.lang.reflect.Method;
import java.util.Map;

public class RPCServerHandler extends SimpleChannelInboundHandler<Request> {
    Map<String, Object> rpcServices;
    public RPCServerHandler(Map<String, Object> rpcServices ){
        super();
        this.rpcServices = rpcServices;
        System.out.println("rpcServices size "+ this.rpcServices.size());
    }


    protected void channelRead0(ChannelHandlerContext channelHandlerContext, Request request) throws Exception {
        Response response = new Response();
        response.setRequestId(request.getRequestId());

        try{
            Object bean = rpcServices.get(request.getParentClassName());
            Method m = bean.getClass().getMethod(request.getMethodName(),request.getParameterTypes());
            response.setResult(m.invoke(bean,request.getParameters()));
        }catch(Exception e){
            response.setException(e);
        }
        //channelHandlerContext.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
        channelHandlerContext.writeAndFlush(response).addListener(new ChannelFutureListener() {
	        

			public void operationComplete(ChannelFuture future) throws Exception {
				System.out.println("operation complete");
				 future.channel().close();
				
			}
	    });
        /*	注意此处的ChannelFutureListener.CLOSE表示如下listener:
         * ChannelFutureListener CLOSE = new ChannelFutureListener() {
	        public void operationComplete(ChannelFuture future) {
	            future.channel().close();
	        }
	    };*/
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
        System.out.println("server caught exception");
        cause.printStackTrace();
        ctx.close();
    }
}
