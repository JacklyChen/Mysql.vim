package com.wsdjeg.mysqlvim.server;

import com.wsdjeg.mysqlvim.server.Protocol;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.sql.Date;
import java.util.Iterator;

public class Server {
    private static final int BUFSIZE = 256;
    private static final int TIMEOUT = 3000;
    public static void main (String[] args) throws IOException {
        if (args.length < 1) {
            throw new IllegalArgumentException("Parameter(s):<port>...");
        }
        Selector selector = Selector.open();
        for (String arg : args) {
            ServerSocketChannel listnChannal = ServerSocketChannel.open();
            listnChannal.bind(new InetSocketAddress(Integer.parseInt(arg)));
            listnChannal.configureBlocking(false);
            listnChannal.register(selector, SelectionKey.OP_ACCEPT);
        }
        Protocol protocol = new ProtocolImpl(BUFSIZE);  
        //不断轮询select方法，获取准备好的信道所关联的Key集  
        while (true){  
            //一直等待,直至有信道准备好了I/O操作  
            if (selector.select(TIMEOUT) == 0){  
                //在等待信道准备的同时，也可以异步地执行其他任务，  
                //这里只是简单地打印"."  
                System.out.print(".");  
                continue;  
            }  
            //获取准备好的信道所关联的Key集合的iterator实例  
            Iterator<SelectionKey> keyIter = selector.selectedKeys().iterator();  
            //循环取得集合中的每个键值  
            while (keyIter.hasNext()){  
                SelectionKey key = keyIter.next();   
                //如果服务端信道感兴趣的I/O操作为accept  
                if (key.isAcceptable()){  
                    protocol.handleAccept(key);  
                }  
                //如果客户端信道感兴趣的I/O操作为read  
                if (key.isReadable()){  
                    protocol.handleRead(key);  
                }  
                //如果该键值有效，并且其对应的客户端信道感兴趣的I/O操作为write  
                if (key.isValid() && key.isWritable()) {  
                    protocol.handleWrite(key);  
                }  
                //这里需要手动从键集中移除当前的key  
                keyIter.remove();   
            }  
        }  
    }
}
