import java.io.*;
import java.net.*;
//import java.util.Scanner;

public class Servidor {
    public static void main(String[] args) {
        int porta = 5000;

        try (ServerSocket servidorSocket = new ServerSocket(porta)) {
            System.out.println("Servidor aguardando conexão na porta " + porta + "...");

            Socket socket = servidorSocket.accept();
            System.out.println("Cliente conectado: " + socket.getInetAddress());

            BufferedReader entrada = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String mensagem = entrada.readLine();
            System.out.println("Mensagem recebida: " + mensagem);

            socket.close();

            
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
