package com.inmemdb.store;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
public class Main {
  private static ConcurrentHashMap<String, String> setDict =
      new ConcurrentHashMap<>();
  /**
   *
   * @param in
   * @return An Array in the format: [command, type, ele1, type, ele2,....]
   */
  public static List<Object> parserCommand(BufferedReader in) {
    List<Object> ret = new ArrayList<>();
    try {
      String line1 = in.readLine();
      if (line1 == null) {
        ret.add("exit");
        return ret;
      }
      if (line1.charAt(0) != '*') {
        throw new RuntimeException("ERR command must be an array ");
      }
      int nEle = Integer.parseInt(line1.substring(1));
      in.readLine();          // skip len - 2nd line
      ret.add(in.readLine()); // read command - 3rd line
      System.out.println("Received command " + ret.get(0) +
                         ", number of element " + nEle);
      // read data
      String line = null;
      for (int i = 1; i < nEle && (line = in.readLine()) != null; i++) {
        if (line.isEmpty())
          continue;
        char type = line.charAt(0);
        switch (type) {
        case '$':
          System.out.println("parse bulk string: " + line);
          // int slen = Integer.parseInt(line.substring(1));
          ret.add(line);
          ret.add(in.readLine());
          // TODO check string len
          break;
        case ':':
          System.out.println("parse int: " + line);
          ret.add(String.valueOf(type));
          ret.add(Integer.parseInt(line.substring(1)));
          break;
        default:
          System.out.println("default: " + line);
          break;
        }
      }
    } catch (IOException e) {
      System.out.println("Parse failed " + e.getMessage());
      throw new RuntimeException(e);
    } catch (Exception e) {
      System.out.println("Parse failed " + e.getMessage());
    }
    System.out.println("Command: " +
                       String.join(" ", ret.stream().toArray(String[] ::new)));
    return ret;
  }
  public static void main(String[] args) {
    // You can use print statements as follows for debugging, they'll be visible
    // when running tests.
    System.out.println("Logs from your program will appear here!");
    //      Uncomment this block to pass the first stage
    ServerSocket serverSocket;
    Socket clientSocket = null;
    int port = 6379;
    ExecutorService es = null;
    try {
      serverSocket = new ServerSocket(port);
      // Since the tester restarts your program quite often, setting
      // SO_REUSEADDR ensures that we don't run into 'Address already in use'
      // errors
      serverSocket.setReuseAddress(true);
      // Wait for connection from client.
      es = Executors.newFixedThreadPool(4);
      class Handler implements Runnable {
        Socket clientSocket;
        public Handler(Socket sock) { clientSocket = sock; }
        @Override
        public void run() {
          try (OutputStreamWriter out = new OutputStreamWriter(
                   clientSocket.getOutputStream(), StandardCharsets.UTF_8);
               BufferedReader in = new BufferedReader(
                   new InputStreamReader(clientSocket.getInputStream()))) {
            List<Object> command = null;
            while ((command = parserCommand(in)) != null) {
              String cmd = (String)command.get(0);
              System.out.println("Responsding: " + cmd);
              switch (cmd.toLowerCase()) {
              case "ping":
                out.write("+PONG\r\n");
                break;
              case "echo":
                out.write(String.join("\r\n", command.stream().skip(1).toArray(
                                                  String[] ::new)) +
                          "\r\n");
                break;
              case "set":
                out.write("+OK\r\n");
                String key = (String)command.get(2);
                setDict.put(
                    key, String.join("\r\n", command.stream().skip(3).toArray(
                                                 String[] ::new)) +
                             "\r\n");
                break;
              case "get":
                key = (String)command.get(2);
                out.write(setDict.getOrDefault(key, "$-1\r\n"));
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
      while ((clientSocket = serverSocket.accept()) != null) {
        es.submit(new Handler(clientSocket));
      }
    } catch (IOException e) {
      System.out.println("IOException: " + e.getMessage());
    } finally {
      try {
        if (clientSocket != null) {
          clientSocket.close();
        }
        if (es != null) {
          es.shutdown();
        }
      } catch (IOException e) {
        System.out.println("IOException: " + e.getMessage());
      }
    }
  }
}
