package protocols;
import data.Mensagem;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;

public class TelemetryStream {
     private Socket socket;
     private ServerSocket serverSocket;

     /* ======== CONSTRUTOR DO SERVIDOR (NAVE-MÃE) ======== */
     public TelemetryStream(int porta) {
          try {
               this.serverSocket = new ServerSocket(porta);
               System.out.println("Aguardando conexão na porta " + porta + "...");
          } catch (IOException e) {
               System.out.println("Erro ao abrir a porta " + porta + " - " + e.getMessage());
          }
     }

     /* ======== CONSTRUTOR DO CLIENTE (ROVER) ======== */
     public TelemetryStream(InetAddress ip, int porta) {
          try {
               this.socket = new Socket(ip, porta);
               System.out.println("Conectado ao servidor na porta " + porta + "...");   
          } catch (UnknownHostException e) {
               System.out.println("Erro: Endereço IP desconhecido - " + e.getMessage());
          } catch (IOException e) {
               System.out.println("Erro ao conectar ao servidor na porta " + porta + " - " + e.getMessage());
          }
     }

     public void startServerNaveMae(){
          while (true) { 
              try {
                    Socket clientSocket = serverSocket.accept();
                    System.out.println("Conexão aceita de " + clientSocket.getInetAddress());
                    // Cria uma thread para lidar com cada rover
                    new Thread(() -> handleClient(clientSocket)).start();
              } catch (IOException e) {
                  System.out.println("Erro ao aceitar conexão - " + e.getMessage());
              }
          }
     }

     public void handleClient(Socket clientSocket) {
          try (DataInputStream dis = new DataInputStream(clientSocket.getInputStream())) {
               while (true) {
                    int length;
                    try {
                         // 1️⃣ Lê os primeiros 4 bytes: o tamanho da mensagem
                         length = dis.readInt();
                    } catch (EOFException e) {
                         System.out.println("[TelemetryStream] Conexão encerrada pelo rover.");
                         break;
                    }

                    // 2️⃣ Lê exatamente 'length' bytes do corpo da mensagem
                    byte[] data = new byte[length];
                    dis.readFully(data);

                    // 3️⃣ Reconstrói a mensagem a partir do corpo
                    Mensagem msg = Mensagem.fromByteArray(data);

                    // 4️⃣ Mostra os dados
                    System.out.printf("[TelemetryStream] Rover %s | Tipo: %s | Seq: %d | Payload: %s%n",
                              msg.getIdOrg(),
                              msg.getTipo(),
                              msg.getSeqNumber(),
                              new String(msg.getPayload(), StandardCharsets.UTF_8)
                    );
               }

          } catch (IOException e) {
               System.err.println("[TelemetryStream] Erro na ligação: " + e.getMessage());
          } finally {
               try {
                    clientSocket.close();
               } catch (IOException e) {
                    System.err.println("Erro ao fechar socket: " + e.getMessage());
               }
          }
     }

     public Mensagem receberMensagem() {
          try {
               DataInputStream dis = new DataInputStream(socket.getInputStream());
               int len = dis.readInt();
               byte[] data = new byte[len];
               dis.readFully(data);
               return Mensagem.fromByteArray(data);
          } catch (IOException e) {
               System.err.println("Erro ao receber mensagem: " + e.getMessage());
               return null;
          }
     }

     public void enviarMensagem(Mensagem mensagem) {
          try {
               byte[] data = mensagem.toByteArray();
               DataOutputStream out = new DataOutputStream(socket.getOutputStream());
               out.writeInt(data.length);
               out.write(data);
               out.flush();
          } catch (IOException e) {
               System.err.println("Erro ao enviar mensagem: " + e.getMessage());
          }
     }

}