package protocols.tcp;

import core.NaveMae;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;


public class TelemetryStreamNM {
     private final ServerSocket serverSocket;
     private final NaveMae nm;
     private volatile boolean running = true;

     /* ====== Construtor ====== */

     public TelemetryStreamNM(int porta, NaveMae nm) throws IOException{
          this.serverSocket = new ServerSocket(porta);
          this.nm = nm;
          System.out.println("[NaveMae - TL] A escutar de TL na porta: " + porta);
     }

     /* ====== Métodos ====== */

     // Cria a Thread para o protocolo TS para a NaveMae
     public void startTSNaveMae(){
          Thread t = new Thread(() -> {
               while(running){
                    try{
                         Socket socket = this.serverSocket.accept();
                         new Thread(new RoverWorkerTS(socket, nm)).start();
                    }catch(IOException e){
                         System.out.println("[ERRO NaveMae - TS] " + e.getMessage());
                         e.printStackTrace();
                    }
               }
          }, "Thread-TS-NaveMae");
          t.start();
     }

     public void stopTSNaveMae(){
          this.running = false;
          try {
               this.serverSocket.close();
          } catch (IOException e) {
               System.out.println("[ERRO NaveMae - TS] Stop: " + e.getMessage());
               e.printStackTrace();
          }
     }
}
