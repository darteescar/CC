import java.util.Map;
import java.util.HashMap;
import java.util.PriorityQueue;
import java.util.Queue;
import data.*;
import protocols.MissionLink;
import protocols.TelemetryStream;

import java.net.InetAddress;
import java.net.UnknownHostException;


public class NaveMae {
    private Map<String, Estado> roversEstado;
    private Map<String, InetAddress> roversIP;
    private Map<String, Integer> roversPorta;

    //private final String id = "0000";
    private InetAddress endIP;
    private int porta;

    private Queue<Missao> queue;

    private MissionLink ml;
    private TelemetryStream ts;

    public NaveMae(InetAddress ip, int porta, Map<String, Estado> m1, Map<String, InetAddress> m2, Map<String, Integer> m3 ){
        this.setRoversEstado(m1);
        this.setRoversIP(m2);
        this.setRoversPorta(m3);
        this.endIP = ip;
        this.porta = porta;

        this.queue = new PriorityQueue<Missao>();
        // this.ml = new MissionLink(porta);
        // this.ts = new TelemetryStream(porta);
        
    }

    public Map<String, Estado> getRoversEstado() {
        Map<String, Estado> novo = new HashMap<>();
        for(Map.Entry<String,Estado> m: this.roversEstado.entrySet()){
            novo.put(m.getKey(),m.getValue());
        }
        return novo;
    }

    public void setRoversEstado(Map<String, Estado> roversEstado2) {
        Map<String, Estado> novo = new HashMap<>();
        for(Map.Entry<String,Estado> m: roversEstado2.entrySet()){
            novo.put(m.getKey(),m.getValue());
        }
        this.roversEstado = novo;
    }

    public Map<String, InetAddress> getRoversIP() {
        Map<String, InetAddress> novo = new HashMap<>();
        for(Map.Entry<String,InetAddress> m: this.roversIP.entrySet()){
            novo.put(m.getKey(),m.getValue());
        }
        return novo;
    }

    public void setRoversIP(Map<String, InetAddress> roversIP2) {
        Map<String, InetAddress> novo = new HashMap<>();
        for(Map.Entry<String,InetAddress> m: roversIP2.entrySet()){
            novo.put(m.getKey(),m.getValue());
        }
        this.roversIP = novo;
    }

    public Map<String, Integer> getRoversPorta() {
        Map<String, Integer> novo = new HashMap<>();
        for(Map.Entry<String,Integer> m: this.roversPorta.entrySet()){
            novo.put(m.getKey(),m.getValue());
        }
        return novo;
    }

    public void setRoversPorta(Map<String, Integer> roversPorta2) {
        Map<String, Integer> novo = new HashMap<>();
        for(Map.Entry<String,Integer> m: roversPorta2.entrySet()){
            novo.put(m.getKey(),m.getValue());
        }
        this.roversPorta = novo;
    }


    



}
