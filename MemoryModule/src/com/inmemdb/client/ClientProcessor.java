package com.inmemdb.client;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ClientProcessor {

	public static void main(String args[]) {
		
		ServerSocket serverSocket;
	    Socket clientSocket = null;
	    int port = 6379;
	    ExecutorService threadPoolReddisProvider = null;
	    
	    try {
	        serverSocket = new ServerSocket(port);
	        threadPoolReddisProvider = Executors.newFixedThreadPool(10);
	        while ((clientSocket = serverSocket.accept()) != null) {
	        	threadPoolReddisProvider.submit(new CacheCommandHandler(clientSocket));
	          }
	        } catch (IOException e) {
	          System.out.println("IOException: " + e.getMessage());
	        } finally {
	          try {
	            if (clientSocket != null) {
	              clientSocket.close();
	            }
	            if (threadPoolReddisProvider != null) {
	            	threadPoolReddisProvider.shutdown();
	            }
	          } catch (IOException e) {
	            System.out.println("IOException: " + e.getMessage());
	          }
	        }
	    
	}
	
	
	
}
