// Código completo com as alterações sugeridas: cores inicializadas apenas uma vez
// e mantidas ao longo de toda a aplicação.

package ground_control;

import data.Estado;
import data.Missao;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import protocols.http.HTTPGC;

public class GroundControl {
    private HTTPGC http;

    public GroundControl(String urlNaveMae) {
        this.http = new HTTPGC(urlNaveMae);
    }

    public static void main(String[] args) {
        GroundControl gc = new GroundControl("http://10.0.6.21:7000");
        gc.run();
    }

    public void run() {
        Map<String, Estado> estados = new ConcurrentHashMap<>();
        Map<String, Missao> missoes = new ConcurrentHashMap<>();
        GroundControlGUI gui = new GroundControlGUI(estados, missoes);

        while (true) {
            try {
                int num_rovers = http.getNumeroRovers();
                for (int i = 1; i <= num_rovers; i++) {
                    String nome = "R-" + i;
                    Estado estado = http.getEstadoRover(nome);
                    Missao missao = http.getMissaoRover(nome);
                    if (estado != null) {
                        estados.put(nome, estado);
                    }
                    if (missao != null) {
                        missoes.put(nome, missao);
                    }
                }

                gui.atualizar(estados);

                Thread.sleep(1000);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

        
}