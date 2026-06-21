package com.dfs.storage.engine;

import java.io.DataInputStream;
import java.net.InetSocketAddress;
import java.nio.channels.Channels;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.file.Path;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

@Service
public class NetworkEngine {
    private static final int TCP_PORT = 9000;
    private final StorageEngine storageEngine;
    private volatile boolean isRunning = true;

    @Autowired
    public NetworkEngine(StorageEngine storageEngine) {
        this.storageEngine = storageEngine;
    }
    
    @PostConstruct
    public void init(){
        Thread.startVirtualThread(()->listenForConnection());
    }

    public void listenForConnection(){
        try(ServerSocketChannel serverSocket = ServerSocketChannel.open()){

            serverSocket.bind(new InetSocketAddress(TCP_PORT));
            System.out.println("Storage node TCP socket active @ "+TCP_PORT);

            while(isRunning){
                SocketChannel clientSocket = serverSocket.accept();
                Thread.startVirtualThread(()->processIncomingData(clientSocket));
            }
        }
        catch(Exception ex){
            System.err.println("Storage node TCP socket issue: "+ex.getMessage());
        }
    }

    public void processIncomingData(SocketChannel socketChannel){
        try{
            // Parsing the incoming TCP message (chunk)

            DataInputStream  incomingData= new DataInputStream(Channels.newInputStream(socketChannel));
            Long chunkId= incomingData.readLong();
            int routingLength= incomingData.readInt();
            byte[] routingBytes = new byte[routingLength];
            incomingData.readFully(routingBytes);
            String routingString = new String (routingBytes); // next nodes to send the incoming chunk for replication 
            long payloadSize = incomingData.readLong();
            // Writing to disk 
            storageEngine.writeData(chunkId, socketChannel,payloadSize);

            // Daisy chaining to next node 
            if (!routingString.isEmpty()){
                String nextNodes []= routingString.split(",");
                String remainingNodes = "";
                if (nextNodes.length >1){
                     remainingNodes=routingString.substring(routingString.indexOf(',')+1);
                } 
                // now sending the chunk to next node with updated remaining node list 
                // TODO : implement the forwarder function and / or service 
            }
              

        }catch(Exception ex){
            throw new RuntimeException("TCP Message processing fault !!!");
        }
    }
    @PreDestroy
    public void stopServer(){
        isRunning=false;
    }

}
