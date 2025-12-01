package protocols.udp;

import data.Estado;
import data.Mensagem;
import data.Report;
import data.TipoMensagem;
import core.NaveMae;

import java.io.IOException;
import java.net.*;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public class MissionLinkNM {
    private final DatagramSocket socket;
    private final EnvioML envioML;
    private final NaveMae nm;
    private volatile boolean running = true;

    // Mapa de workers para cada Rover
    private final Map<String, RoverWorkerML> workers = new ConcurrentHashMap<>(); // idRover/Thread que trata do Rover

    /* ====== Construtor ====== */

    public MissionLinkNM(int porta, NaveMae nm) throws SocketException {
        this.socket = new DatagramSocket(porta);
        this.envioML = new EnvioML(socket);
        this.nm = nm; 
        System.out.println("[NaveMae - ML] A escutar de ML na porta: " + porta);
    }

    /* ====== Métodos ====== */

    public void startMLNaveMae(){
        // Cria a Thread para o protocolo ML para a NaveMae
        new Thread(() -> {
            while(running){
                try{    
                    // Receber a mensagem
                    byte[] buffer = new byte[65507];
                    DatagramPacket pacote = new DatagramPacket(buffer, buffer.length);
                    this.socket.receive(pacote);

                    // Descodifica a Mensagem
                    Mensagem temp = Mensagem.fromByteArray(pacote.getData());
                    TipoMensagem tp = temp.getTipo();

                    Mensagem m;
                    if(tp == TipoMensagem.ML_FRAMES
                    || tp == TipoMensagem.ML_REPORT
                    || tp == TipoMensagem.ML_END
                    || tp == TipoMensagem.ML_FINACK
                    ){
                        m = Report.fromByteArray(pacote.getData());
                    }else{
                        m = temp;
                    }   

                    String idRover = m.getIdOrg();
                    InetAddress ipRover = m.getIpOrg();
                    int portaRover = m.getPortaOrg();

                    // Adiciona o Rover a NaveMae caso ela não o conheça
                    if(!nm.conheceRover(idRover)){
                        nm.adicionaRover(idRover, ipRover, portaRover, new Estado());
                    }

                    // Cria worker dedicado caso ele nao exista
                    RoverWorkerML roverWorker = workers.computeIfAbsent(idRover, id ->{
                        RoverWorkerML w = new RoverWorkerML(idRover, ipRover, portaRover, envioML, nm);
                        new Thread(w, "WorkerML - " + id).start();
                        return w;
                    });

                    // Envia a mensagem para a Thread correspondente do Rover (Worker)
                    roverWorker.addMensagemQueue(m);
                }catch(IOException e){
                    System.out.println("[NaveMae - ML - ERRO] Thread ML da NaveMae: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }, "Thread-ML-NaveMae").start();
    }

    public void stopMLNaveMae() {
        running = false;
        socket.close();
        envioML.stop();
        workers.values().forEach(RoverWorkerML :: paraCiclo);
        System.out.println("[NaveMae - ML] Encerrando");
    }
}
