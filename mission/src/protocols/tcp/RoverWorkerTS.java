package protocols.tcp;

import core.NaveMae;
import data.Estado;
import data.Mensagem;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;

public class RoverWorkerTS implements Runnable{
    private final Socket socket;
    private final NaveMae nm;

    public RoverWorkerTS(Socket socket, NaveMae nm){
        this.socket = socket;
        this.nm = nm;
    }

    @Override
    public void run(){
        try(DataInputStream dis = new DataInputStream(socket.getInputStream())){
            while(true){
                // Ler o tamanho da mensagem
                int length = dis.readInt();

                // Ler a mensagem em si
                byte[] msgBytes = new byte[length];
                dis.readFully(msgBytes);

                Mensagem m = Mensagem.fromByteArray(msgBytes);
                String idRover = m.getIdOrg();
                Estado e = new Estado();
                e.fromByteArray(m.getPayload());
                //System.out.printf("[WorkerTS] Estado recebido de: " + idRover + "\n");

                // NaveMae atualiza o Estado do Rover (idRover)
                nm.atualizaEstado(idRover, e);
            }
        }catch(IOException e ){
            System.out.println("[WorkerTS - ERRO] while da Thread " + e.getMessage());
            e.printStackTrace();
        }
    }
}
