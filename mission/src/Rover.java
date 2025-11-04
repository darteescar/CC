import java.net.InetAddress;
import java.net.UnknownHostException;

import data.Estado;
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
        // this.ml = new MissionLink(porta);
        // this.ts = new TelemetryStream(porta);
    }
}
