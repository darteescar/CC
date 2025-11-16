package data;

import java.io.FileReader;
import java.util.concurrent.BlockingQueue;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class Parser {

    public static void parseMissoes(BlockingQueue<Missao> queue, String filePath) {
        try {
            // Ler o ficheiro JSON
            Object obj = new JSONParser().parse(new FileReader(filePath));

            // O ficheiro tem um array de objetos Missao
            JSONArray array = (JSONArray) obj;

            // Iterar pelas missões
            for (Object o : array) {
                JSONObject jo = (JSONObject) o;

                int id = ((Long) jo.get("id")).intValue();
                double x1 = ((Number) jo.get("x1")).doubleValue();
                double y1 = ((Number) jo.get("y1")).doubleValue();
                double x2 = ((Number) jo.get("x2")).doubleValue();
                double y2 = ((Number) jo.get("y2")).doubleValue();
                String tarefa = (String) jo.get("tarefa");
                int duracao = ((Long) jo.get("duracao")).intValue();
                int freqUpdate = ((Long) jo.get("freq_update")).intValue();

                // Criar a Missao
                Missao m = new Missao(id, x1, y1, x2, y2, tarefa, duracao, freqUpdate);
                //System.out.println(m.toString());

                // Adicionar Missao a queue
                queue.offer(m);
            }

            System.out.println("[Parser] Missoes carregadas: " + queue.size());

        } catch (Exception e) {
            System.out.println("[Parser - ERRO] Falha ao ler ficheiro JSON: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
