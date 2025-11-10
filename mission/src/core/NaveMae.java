package core;

import java.util.Map;
import java.util.HashMap;
import java.util.PriorityQueue;
import java.util.Queue;
import data.*;
import protocols.tcp.TelemetryStreamNM;
import protocols.udp.MissionLinkNM;

import java.net.InetAddress;
import java.net.UnknownHostException;


public class NaveMae {
    private Map<String, Estado> roversEstado;
    private Map<String, InetAddress> roversIP;
    private Map<String, Integer> roversPorta;

    private Queue<Missao> queue;

    private final String id = "NaveMae";
    private final int portaUDP = 5000;
    private final int portaTCP = 6000;
    private final InetAddress ip;

    private MissionLinkNM ml;
    private TelemetryStreamNM ts;

    /* ========== Construtor ========== */

    public NaveMae(InetAddress ip){
        this.ip = ip;

        this.queue = new PriorityQueue<Missao>();

        this.roversEstado = new HashMap<>();
        this.roversIP = new HashMap<>();
        this.roversPorta = new HashMap<>();

        try {
            this.ml = new MissionLinkNM(this.portaUDP);
            this.ts = new TelemetryStreamNM(this.portaTCP);

        } catch (Exception e) {
            System.out.println("[ERRO] Falha ao inicializar NaveMae: " + e.getMessage());
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

    public Missao getMissaoQueue(){
        return this.queue.poll();
    }

    public void startNaveMae(){
        this.ml.startMLNaveMae(this);
        this.ts.startTLNaveMae(this);
        System.out.println("[NaveMae] Todos os serviços foram conectados");
    }

    public static void main(String[] args) {
        if(args.length < 3 + 3*Integer.parseInt(args[2])){
            System.out.println("[Uso] java MainNaveMae @ip");
            return;
        }
        try{
            InetAddress ip = InetAddress.getByName(args[1]);
            NaveMae naveMae = new NaveMae(ip);
            naveMae.startNaveMae();

        }catch(UnknownHostException e){
            System.out.println("[NaveMae - ERRO]: problema com IP: " + e.getMessage());
        }
    }
}
