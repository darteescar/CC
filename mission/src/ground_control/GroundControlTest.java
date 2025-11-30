package ground_control;

import data.Estado;
import data.EstadoOperacional;
import data.Missao;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class GroundControlTest {

    public static void main(String[] args) {
        Map<String, Estado> estados = new HashMap<>();
        Map<String, Missao> missoes = new HashMap<>();
        List<Missao> missoesConcluidas = new ArrayList<>();
        Random rand = new Random();

        for (int i = 1; i <= 10; i++) {
            String nome = "Rover " + i;

            // Estado fake
            estados.put(nome, new Estado(
                    rand.nextDouble() * 100,
                    rand.nextDouble() * 100,
                    EstadoOperacional.PARADO,
                    100 - rand.nextInt(50),
                    rand.nextFloat() * 5
            ));
        }

        for (int i = 0; i < 10; i++) {
            String nome = "Rover " + i;
            // Missão fake
            missoes.put(nome, new Missao(
                    i,
                    rand.nextDouble() * 50,
                    rand.nextDouble() * 50,
                    50 + rand.nextDouble() * 50,
                    50 + rand.nextDouble() * 50,
                    "Exploração",
                    60,
                    10
            ));
        }

        for (int i = 0 ; i < 5; i++){
            missoesConcluidas.add(new Missao(
                    i,
                    rand.nextDouble() * 50,
                    rand.nextDouble() * 50,
                    50 + rand.nextDouble() * 50,
                    50 + rand.nextDouble() * 50,
                    "Exploração",
                    60,
                    10
            ));
        }

        GroundControlGUI gui = new GroundControlGUI(estados, missoes, missoesConcluidas);

        // Atualizar posições aleatoriamente
        new Thread(() -> {
            while (true) {
                try {
                    for (Estado e : estados.values()) {
                        e.setX(rand.nextDouble() * 100);
                        e.setY(rand.nextDouble() * 100);
                        e.setVelocidade(rand.nextFloat() * 5);
                        e.setBateria(Math.max(0, e.getBateria() - rand.nextInt(3)));
                    }
                    gui.atualizar();
                    Thread.sleep(1000);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }
        }).start();
    }
}