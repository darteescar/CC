import data.*;
import java.net.InetAddress;
import protocols.MissionLink;
import protocols.TelemetryStream;

public class Rover {
    private final String id; // R-x
    private final InetAddress ip;
    private final int porta;
    private Estado estado;

    private MissionLink ml;
    private TelemetryStream ts;

    public Rover(String id, InetAddress ip, int porta){
        this.id = id;
        this.ip = ip;
        this.porta = porta;
        this.estado = new Estado();
        //this.ml = new MissionLink(porta);
        this.ts = new TelemetryStream(ip,porta);
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

    public void startComms(){
        EstadoOperacional estado_atual = this.getEstado().getEstadoOperacional();

        if (estado_atual.) {
            System.out.println("Rover " + this.id + " já está em missão. Não é possível iniciar comunicação.");
            // envia que ja esta em Missao à nave-mãe
            return;
        }

        if () {
            System.out.println("Rover " + this.id + " está com falha no sistema. Não é possível iniciar comunicação.");
            // envia falha à nave-mãe
            return;
        }
        

    }

    public void start() {
        // Inicia a comunicação com a nave-mãe
        
        
        if (this.emMissao) {
            // envia que ja esta em Missao
            return;
        }

        if 


    }

    

    
}