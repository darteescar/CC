package core;

import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;

import data.*;
import protocols.http.HTTPNM;
import protocols.tcp.TelemetryStreamNM;
import protocols.udp.MissionLinkNM;

import java.net.InetAddress;
import java.net.UnknownHostException;


public class NaveMae {
    private Map<String, Estado> roversEstado;
    private Map<String, InetAddress> roversIP;
    private Map<String, Integer> roversPorta;
    private Map<String, Missao> roversMissao;

    private final BlockingQueue<Missao> queue;

    private final String id = "NaveMae";
    private final int portaUDP = 5000;
    private final int portaTCP = 6000;
    private final int portaHTTP = 7000;
    private final InetAddress ip;

    private MissionLinkNM ml;
    private TelemetryStreamNM ts;
    private HTTPNM http;

    /* ========== Construtor ========== */

    public NaveMae(InetAddress ip){
        this.ip = ip;

        this.queue = new LinkedBlockingQueue<Missao>();

        this.roversEstado = new ConcurrentHashMap<>();
        this.roversIP = new ConcurrentHashMap<>();
        this.roversPorta = new ConcurrentHashMap<>();
        this.roversMissao = new ConcurrentHashMap<>();

        try {
            this.ml = new MissionLinkNM(this.portaUDP, this);
            this.ts = new TelemetryStreamNM(this.portaTCP, this);
            this.http = new HTTPNM(portaHTTP, this);

        } catch (Exception e) {
            System.out.println("[ERRO] Falha ao inicializar NaveMae: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /*======= Getters & Setters ====== */

    public String getID(){
        return this.id;
    }

    public InetAddress getIP(){
        return this.ip;
    }
    
    public int getPortaUDP(){
        return this.portaUDP;
    }

    public int getPortaTCP(){
        return this.portaTCP;
    }

    public int getNumeroRovers(){
        return this.roversEstado.size();
    }

    public Estado getEstadoRover(String idRover){
        return this.roversEstado.get(idRover);
    }

    public Missao getMissaoRover(String idRover){
        return this.roversMissao.get(idRover);
    }

    /* ====== Métodos ====== */

    public boolean conheceRover(String idRover){
        return (this.roversIP.containsKey(idRover)
             && this.roversPorta.containsKey(idRover)
             && this.roversEstado.containsKey(idRover));
    }

    public void adicionaRover(String id, InetAddress ip, int porta, Estado e){
        this.roversIP.put(id, ip);
        this.roversPorta.put(id, porta);
        this.roversEstado.put(id, e);
        System.out.println("[NaveMae] Novo rover adicionado: " + id);
    }

    public Missao getMissaoQueue() throws InterruptedException{
        return this.queue.take();
    }

    public void putMissaoMap(String idRover, Missao missao){
        this.roversMissao.put(idRover, missao);
    }

    public void removeMissaoMap(String idRover){
        this.roversMissao.remove(idRover);
    }

    public void startNaveMae(){
        Parser.parseMissoes(this.queue, "resources/missoes.json");
        this.ml.startMLNaveMae();
        this.ts.startTSNaveMae();
        this.http.start();
        System.out.println("[NaveMae] Todos os serviços foram iniciados\n");
    }

    public void atualizaEstado(String idRover, Estado e){
        this.roversEstado.put(idRover, e);
        System.out.println(e.toString());
        System.out.println("[NaveMae] Estado de " + idRover + " atualizado");
    }

    public static void main(String[] args) {
        if(args.length < 1){
            System.out.println("[Uso] java NaveMae <ip>");
            return;
        }
        try{
            InetAddress ip = InetAddress.getByName(args[0]);
            NaveMae naveMae = new NaveMae(ip);

            naveMae.startNaveMae();

        }catch(UnknownHostException e){
            System.out.println("[NaveMae - ERRO]: problema com IP: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
