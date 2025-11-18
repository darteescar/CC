package protocols.udp;

import java.net.InetAddress;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import core.NaveMae;
import data.Mensagem;
import data.Missao;
import data.TipoMensagem;

public class RoverWorkerML implements Runnable{
    private final String idRover;
    private final InetAddress ipRover;
    private final int portaRover;
    private final EnvioML envioML;
    private final NaveMae nm;
    private final BlockingQueue<Mensagem> queue = new LinkedBlockingQueue<>();
    private volatile boolean running = true;

    /* ====== Construtor ====== */

    public RoverWorkerML(String id, InetAddress ip, int porta, EnvioML envioML, NaveMae nm){
        this.idRover = id;
        this.ipRover = ip;
        this.portaRover = porta;
        this.envioML = envioML;
        this.nm = nm;
    }

    /* ====== Métodos ====== */

    // Logica principal da Thread de cada RoverWorer
    @Override
    public void run(){
        System.out.println("[WorkerML - " + idRover + "] Iniciado");
        try{
            while(running){
                Mensagem m = queue.take();
                this.handleMensagem(m);
            }
        }catch(Exception e){
            System.out.println("[WorkerML - " + idRover + " - ERRO] while da Thread " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Handler das mensagens
    public void handleMensagem(Mensagem m){
        TipoMensagem tipo = m.getTipo();
        System.out.println("[WorkerML - " + idRover + "] Recebida mensagem do tipo: " + tipo);

        try{
            switch(tipo) {
                case ML_SYN -> handleSYN();
                case ML_REQUEST -> handleREQUEST();
                case ML_CONFIRM -> handleCONFIRM();
                default -> System.out.println("[ERRO] Tipo não existente: " + tipo);
            }
        }catch(Exception e){
            System.out.println("[WorkerML - " + idRover + "] Handler: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void handleSYN() throws Exception{
        this.nm.removeMissaoMap(idRover);

        Mensagem mSYNACK = new Mensagem(TipoMensagem.ML_SYNACK, 
                                        "NaveMae", 
                                        nm.getIP(), 
                                        nm.getPortaUDP(),
                                        idRover, 
                                        ipRover, 
                                        portaRover, 
                                        null
        );

        // Enviar SYNACK e esperar REQUEST
        envioML.sendMensagem(mSYNACK.toByteArray(), 
                            ipRover, 
                            portaRover,
                            idRover + "_SYNACK"
        );
        System.out.println("[WorkerML - " + idRover + "] SYNACK enviado ao rover: " + idRover);
    }

    public void handleREQUEST() throws Exception{
        // Confirma o SYNACK (parar retransmissão)
        envioML.confirmarRecebimento(idRover + "_SYNACK");

        // Enviar missão
        Missao missao = nm.getMissaoQueue();
        this.nm.putMissaoMap(idRover, missao);

        Mensagem mDATA = new Mensagem(TipoMensagem.ML_DATA, 
                                "NaveMae", 
                                nm.getIP(), 
                                nm.getPortaUDP(),
                                idRover, 
                                ipRover, 
                                portaRover, 
                                missao.toByteArray()
        );

        // Enviar DATA e esperar CONFIRM
        envioML.sendMensagem(mDATA.toByteArray(), 
                            ipRover, 
                            portaRover,
                            idRover + "_DATA"
        );

        System.out.println("[WorkerML - " + idRover + "] Missao enviada ao rover: " + idRover);
    }

    public void handleCONFIRM(){
        // Confirma o DATA(parar retransmissão)
        envioML.confirmarRecebimento(idRover + "_DATA");
        System.out.println("[WorkerML - " + idRover + "] CONFIRM de: " + idRover);
    }

    public void addMensagemQueue(Mensagem m){
        queue.offer(m);
    }

    public void paraCiclo(){
        this.running = false;
    }
}
