package ground_control;

import data.Estado;
import data.Missao;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import protocols.http.HTTPGC;

public class RoversInfo {

     private final HTTPGC http;
     private final Map<String, Estado> estados = new ConcurrentHashMap<>();
     private final Map<String, Missao> missoes = new ConcurrentHashMap<>();

     public RoversInfo(String baseUrl) {
        this.http = new HTTPGC(baseUrl);
     }

     public void atualizarDados() {
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
               Thread.sleep(1000);
          } catch (Exception e) {
               e.printStackTrace();
          }
     }

     public Map<String,Estado> getEstados() {
          return estados;
     }

     public Map<String,Missao> getMissoes() {
          return missoes;
     }
}