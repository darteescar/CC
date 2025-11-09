package nave_mae;

import java.util.Map;
import java.util.HashMap;
import java.util.PriorityQueue;
import java.util.Queue;
import data.*;
import protocols.MissionLink;
import protocols.TelemetryStream;

import java.net.InetAddress;


public class NaveMae {
    private Map<String, Estado> roversEstado;
    private Map<String, InetAddress> roversIP;
    private Map<String, Integer> roversPorta;

    private Queue<Missao> queue;

    private final String id = "NaveMae";
    private final int porta = 5000;
    private final InetAddress ip;

    private MissionLink ml;
    private TelemetryStream ts;

    /* ========== Construtor ========== */

    public NaveMae(InetAddress ip){
        this.ip = ip;

        this.queue = new PriorityQueue<Missao>();

        this.roversEstado = new HashMap<>();
        this.roversIP = new HashMap<>();
        this.roversPorta = new HashMap<>();

        try {
            this.ml = new MissionLink(this.porta);
            this.ts = new TelemetryStream(this.porta);

            System.out.println("[NaveMae] inicializada:");
            System.out.println("-> UDP (MissionLink) conectado na porta: " + this.porta);
            System.out.println("-> TCP (TelemetryStream) conectado na porra: " + this.porta);

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
    
    public int getPorta(){
        return this.porta;
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
    }
}
