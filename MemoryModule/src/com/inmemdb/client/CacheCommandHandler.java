package com.inmemdb.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import com.inmemdb.CommandDocumentParse;
import com.inmemdb.Parser;
import com.inmemdb.ReddisFactory;

import java.nio.charset.StandardCharsets;

public class CacheCommandHandler implements Runnable {
	private static ConcurrentHashMap<String, String> cacheMap = new ConcurrentHashMap<>();
	Socket clientSocket;

	public CacheCommandHandler() {
		
	}
	public CacheCommandHandler(Socket sock) {
		clientSocket = sock;
	}

	

	@Override
	public void run() {
		try (OutputStreamWriter out = new OutputStreamWriter(clientSocket.getOutputStream(), StandardCharsets.UTF_8);
				BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()))) {
			List<Object> lineData = null;
			Parser parser=ReddisFactory.getParser("Command");
			
			while ((lineData = parser.parseData(in)) != null) {
				String cmd = (String) lineData.get(0);
				System.out.println("Responsding: " + cmd);
				switch (cmd.toLowerCase()) {
				case "ping":
					out.write("+PONG\r\n");
					break;
				case "echo":
					out.write(String.join("\r\n", lineData.stream().skip(1).toArray(String[]::new)) + "\r\n");
					break;
				case "set":
					out.write("+OK\r\n");
					String key = (String) lineData.get(2);
					cacheMap.put(key, String.join("\r\n", lineData.stream().skip(3).toArray(String[]::new)) + "\r\n");
					break;
				case "get":
					key = (String) lineData.get(2);
					out.write(cacheMap.getOrDefault(key, "$-1\r\n"));
					break;
				default:
					return;
				}
				out.flush();
			}
		} catch (IOException e) {
			System.out.println("IOException: " + e.getMessage());
		} catch (RuntimeException e) {
			System.out.println("RuntimeException: " + e.getMessage());
		}
	}
}
