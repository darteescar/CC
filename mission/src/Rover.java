import java.net.InetAddress;
import java.net.UnknownHostException;

import data.*;
import protocols.MissionLink;
import protocols.TelemetryStream;

public class Rover {
    private final String id; // R-x
    private final InetAddress ip;
    private final int porta;
    private Estado estado;

    private MissionLink ml;
    private TelemetryStream ts;

    private boolean emMissao; // Indica se o rover está atualmente em missão

    public Rover(String id, InetAddress ip, int porta){
        this.id = id;
        this.ip = ip;
        this.porta = porta;
        this.estado = new Estado();
        // this.ml = new MissionLink(porta);
        // this.ts = new TelemetryStream(porta);

        this.emMissao = false;
    }

    public Estado getEstado(){
        return this.estado;
    }

    public InetAddress getIP(){
        return this.ip;
    }

    public int getPorta(){
        return this.porta;
    }

    public String getId(){
        return this.id;
    }

    public boolean estaEmMissao(){
        return this.emMissao;
    }

    public void realizarMissao(Missao m){
        this.emMissao = true;
        this.atualizaEstado(m); 
    }

    public void atualizaEstado(Missao m){
        
    }
}
