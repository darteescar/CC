// Código completo com as alterações sugeridas: cores inicializadas apenas uma vez
// e mantidas ao longo de toda a aplicação.

package ground_control;

import data.Estado;
import data.Missao;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class GroundControl {
    //private HTTP http = new HTTP();
    
    public static void main(String[] args) {
        
        Map<String, Estado> estados = new ConcurrentHashMap<>();
        Map<String, Missao> missoes = new ConcurrentHashMap<>();

        GroundControlGUI gui = new GroundControlGUI(estados, missoes);
        
        while (true) {

            int num_rovers = this.http.getNumeroRovers();
            for (int i = 1; i <= num_rovers; i++) {
                String nome = "R-" + i;
                Estado estado = this.http.getEstadoRover(nome);
                Missao missao = this.http.getMissaoRover(nome);
                if (estado != null) {
                    estados.put(nome, estado);
                }
            }

            // Atualizar GUI
            gui.atualizar(estados);

            try {Thread.sleep(1000);} catch (InterruptedException e) {e.printStackTrace();}

        }
    }
        
}