package protocols.tcp;

import core.Rover;
import data.Estado;
import data.EstadoOperacional;
import data.Mensagem;
import data.TipoMensagem;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

public class TelemetryStreamRover {
    private final Rover rover; 
    private final Socket socket;
    private final InetAddress ip_NaveMae;
    private final int porta_NaveMae;
    private volatile boolean running = true;

    public TelemetryStreamRover(Rover rover, InetAddress ip_NaveMae, int porta_NaveMae) throws IOException{
        this.rover = rover;
        this.socket = new Socket(ip_NaveMae, porta_NaveMae);
        this.ip_NaveMae = ip_NaveMae;
        this.porta_NaveMae = porta_NaveMae;
        System.out.println("[" + rover.getId() + " - TS] Conectado à NaveMae na porta: " + porta_NaveMae);
    }

    public void startTSRover (){
        Thread t = new Thread(() -> handlerTSRover(), "Thread-TS-" + rover.getId());
        t.start();
    }

    public void handlerTSRover(){
        try{
            DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
            int freqFixa = 1000; // 1s

            while(running){
                // Vai buscar o estado do Rover
                Estado estAtual = rover.tiraFotoEstado();

                // Verifica se esta parado
                boolean estaParado = (estAtual.getEstadoOperacional() == EstadoOperacional.PARADO)
                                  || (estAtual.getEstadoOperacional() == EstadoOperacional.ESPERA_MISSAO)
                                  || (estAtual.getEstadoOperacional() == EstadoOperacional.INOPERACIONAL);

                Thread.sleep(freqFixa);

                byte[] payload = estAtual.toByteArray();
                Mensagem m = new Mensagem(TipoMensagem.TS_TCP, 
                                    rover.getId(), 
                                    rover.getIP(), 
                                    rover.getPorta(), 
                                    "NaveMae", 
                                    ip_NaveMae, 
                                    porta_NaveMae, 
                                    payload
                );

                byte[] msgBytes = m.toByteArray();
                dos.writeInt(msgBytes.length);
                dos.write(msgBytes);
                dos.flush();

                if(estaParado){
                    //System.out.printf("[%s - TS] Enviou estado (Não em Missão) para NaveMae\n", rover.getId());
                }else{
                    //System.out.printf("[%s - TS] Enviou estado (Em Missão) para NaveMae\n", rover.getId());
                }
            }
        }catch(Exception e){
            System.out.printf("[ERRO %s - TS] Handler: " + e.getMessage() + "\n", rover.getId());
            e.printStackTrace(); 
        }
    }
}
