package ground_control;

import data.Estado;
import data.Missao;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.List;
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
                List<String> rovers = http.getListaRovers();

                for (String nome : rovers) {

                    try {
                        Estado estado = http.getEstadoRover(nome);
                        estados.put(nome, estado);

                        Missao missao = http.getMissaoRover(nome);
                        if (missao != null)
                            missoes.put(nome, missao);

                    } catch (RuntimeException ignored) {
                        // ignora 404 (caso raro de race condition)
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
