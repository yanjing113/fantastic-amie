package org.amie.exercise.handler;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import com.google.gson.Gson;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.util.List;

public class DecodeFromByteBufHandler extends ByteToMessageDecoder {
    Class<?> decodeType;

    public DecodeFromByteBufHandler(Class<?> decodeType){
        super();
        decodeType = decodeType;
    }


    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list)
            throws Exception {
    	if (byteBuf.readableBytes() < 4) {
            return;
        }
        System.out.println("in decode ...byteBuf.readableBytes() " + byteBuf.readableBytes());
        byte[] buffer = new byte[byteBuf.readableBytes()];
        byteBuf.readBytes(buffer);
        byteBuf.clear();
        Object o = bytesToObject(buffer);
        System.out.println("in decode : object "+o);
       /* Gson gson = new Gson();
        System.out.println("Json message " + new String(buffer));
        String text = new String(buffer);*/
        
        list.add(o);
       
       
    }
    public static Object bytesToObject(byte[] bytes) throws Exception{
        ByteArrayInputStream byteArrayInputStream = null;
        ObjectInputStream objectInputStream = null;
        try{
            byteArrayInputStream = new ByteArrayInputStream(bytes);
            objectInputStream = new ObjectInputStream(byteArrayInputStream);
            Object object = objectInputStream.readObject();
            return object;
        }catch (Exception e){
            e.printStackTrace();
        }finally{
            if(byteArrayInputStream!=null){
                byteArrayInputStream.close();
            }
            if(objectInputStream!=null){
                objectInputStream.close();
            }
        }
        return null;
    }
}
