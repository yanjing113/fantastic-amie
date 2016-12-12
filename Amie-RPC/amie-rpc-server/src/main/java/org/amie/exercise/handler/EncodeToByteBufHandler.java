package org.amie.exercise.handler;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.amie.exercise.bean.Request;

import com.google.gson.Gson;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

public class EncodeToByteBufHandler extends MessageToByteEncoder {
    Class<?> encodeType;
    public EncodeToByteBufHandler(Class<?> encodeType){
    	this.encodeType = encodeType;
    	System.out.println("encode type "+ encodeType.getName());
    }

    protected void encode(ChannelHandlerContext channelHandlerContext, Object o, ByteBuf byteBuf) throws Exception {
       
        System.out.println("in encode : in o "+ o);
        if(encodeType.isInstance(o)){
        	byte[] buffer = objectToBytes(o);
            byteBuf.writeBytes(buffer);
        }
       
    }
    public static byte[] objectToBytes(Object object) throws Exception{
        ByteArrayOutputStream byteArrayOutputStream = null;
        ObjectOutputStream objectOutputStream=null;
        try {
            byteArrayOutputStream = new ByteArrayOutputStream();
            objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
            objectOutputStream.writeObject(object);
            return byteArrayOutputStream.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        }finally{
            if(objectOutputStream!=null){
                objectOutputStream.close();
            }
            if(byteArrayOutputStream!=null){
                byteArrayOutputStream.close();
            }
        }
        return null;
    }
    
    
}
