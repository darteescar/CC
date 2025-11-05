package protocols;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

import data.Mensagem;

public class MissionLink {  
    private final DatagramSocket socket;
    private final int TIMEOUT = 2*1000;
    // private final int MAX_SEG; tamanho máximo do segmento

    public MissionLink(int porta) throws SocketException{
        this.socket = new DatagramSocket(porta);
    }

    public void sendMenssagem(byte[] mensagem, InetAddress ip_destino, int porta_destino) throws Exception{
        DatagramPacket pacote = new DatagramPacket(mensagem,mensagem.length ,ip_destino, porta_destino);
        this.socket.send(pacote);
    }

    public Mensagem receiveMensagem() throws Exception{
        byte[] buffer = new byte[65507];
        DatagramPacket pacote = new DatagramPacket(buffer, buffer.length);
        socket.receive(pacote);
        Mensagem m = Mensagem.fromByteArray();
        return m;
    }

    public void handlerRover(InetAddress ip_rover, int porta_rover, InetAddress ip_mae, int porta_mae){

    }




}
