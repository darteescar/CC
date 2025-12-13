package ground_control;

import data.Estado;
import data.Missao;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import protocols.http.HTTPGC;

public class RoversInfo {

     private final HTTPGC http;
     private Map<String, Estado> estados = new ConcurrentHashMap<>();
     private Map<String, Missao> missoesAtuais = new ConcurrentHashMap<>();
     private List<Missao> missoesConcluidas = new ArrayList<>();
     private Map<String, File> reports = new ConcurrentHashMap<>();

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
                         missoesAtuais.put(nome, missao);
                    } 
                    catch (RuntimeException ignored) {
                         // ignora 404 (caso raro de race condition)
                    }
               }
               Map<String, File> novosReports = http.getMapReports();

               // remover reports antigos que já não existem
               reports.keySet().removeIf(id -> !novosReports.containsKey(id));

               // adicionar/atualizar reports novos
               for (Map.Entry<String, File> e : novosReports.entrySet()) {
                    reports.put(e.getKey(), e.getValue());
               }

               missoesConcluidas.clear();
               missoesConcluidas.addAll(http.getMissoesConcluidas());
               Thread.sleep(1000);
          } catch (Exception e) {
               e.printStackTrace();
          }
     }

     public Map<String,Estado> getEstados() {
          return estados;
     }

     public Map<String,Missao> getMissoesAtuais() {
          return missoesAtuais;
     }

     public List<Missao> getMissoesConcluidas() {
          return missoesConcluidas;
     }

     public Map<String, File> getMapReports() {
          return reports;
     }
}