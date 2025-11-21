package ground_control;

import data.Estado;
import data.Missao;
import java.awt.*;
import java.util.Map;
import javax.swing.*;

public class GroundControlGUI {
     private JPanel roversPanel;
     private JScrollPane scrollPane;
     private MapaPanel mapaPanel;
     private final Color [] cores = {
          Color.RED, Color.BLUE, Color.GREEN, Color.ORANGE,
          Color.MAGENTA, Color.CYAN, Color.PINK, Color.YELLOW
     };

     public GroundControlGUI(Map<String, Estado> estados, Map<String, Missao> missoes) {

          // ---------- Configuração da Janela ----------
          JFrame frame = new JFrame("Ground Control");
          frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
          frame.setSize(1400, 800);
          frame.setLocationRelativeTo(null);
          frame.setLayout(null);

          // ---------- Lista de Rovers com Scroll ----------
          roversPanel = new JPanel();
          roversPanel.setLayout(new BoxLayout(roversPanel, BoxLayout.Y_AXIS));
          roversPanel.setBackground(Color.LIGHT_GRAY);

          scrollPane = new JScrollPane(roversPanel);
          scrollPane.setBounds(10, 10, 450, 745);
          scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
          scrollPane.getVerticalScrollBar().setUnitIncrement(16);
          frame.add(scrollPane);

          // ---------- Painel do Mapa ----------
          ImageIcon mapaImg = new ImageIcon(getClass().getResource("/mapa.jpg"));
          if (mapaImg.getIconWidth() == -1) {
               System.err.println("Erro ao carregar a imagem do mapa.");
          }
          mapaPanel = new MapaPanel(mapaImg.getImage(), estados, cores, missoes);
          mapaPanel.setBounds(470, 10, 920, 745);
          frame.add(mapaPanel);

          frame.setVisible(true);
     }

     public void atualizar(Map<String, Estado> estados) {
          atualizarRovers(estados);
          mapaPanel.repaint();
     }

     private void atualizarRovers(Map<String, Estado> estados) {
          roversPanel.removeAll();

          JLabel label = new JLabel("Rovers Panel");
          label.setFont(new Font("Arial", Font.BOLD, 24));
          label.setAlignmentX(JLabel.CENTER_ALIGNMENT);
          roversPanel.add(label);

          int i = 0;
          for (String nome : estados.keySet()) {
               Estado estado = estados.get(nome);
               Color cor = cores[i % cores.length];

               JPanel roverPanel = rover_simple(nome, estado, cor);
               roverPanel.setAlignmentX(JPanel.CENTER_ALIGNMENT);
               roversPanel.add(roverPanel);

               i++;
          }

          roversPanel.revalidate();
          roversPanel.repaint();
     }

     private JPanel rover_simple(String nome, Estado estado, Color corFundo) {
          JPanel panel = new JPanel();
          panel.setBackground(Color.WHITE);
          panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

          JLabel label = new JLabel("Rover: " + nome);
          label.setFont(new Font("Arial", Font.PLAIN, 24));
          label.setForeground(corFundo);
          label.setAlignmentX(JLabel.CENTER_ALIGNMENT);

          String estadoRover = switch (estado.getEstadoOperacional()) {
               case EM_MISSAO -> "Em Missão";
               case ESPERA_MISSAO -> "Espera Missão";
               case A_CAMINHO -> "A Caminho";
               case PARADO -> "Parado";
               default -> "Inoperacional";
          };

          int bateria = estado.getBateria();
          String corBateria = bateria >= 75 ? "green" : (bateria >= 40 ? "orange" : "red");

          JLabel estadoLabel = new JLabel(
               "<html>Posição: (" + String.format("%.1f", estado.getX()) + ", " + String.format("%.1f", estado.getY()) + ")<br/>" +
               "Estado Operacional: " + estadoRover + "<br/>" +
               "Bateria: <span style='color:" + corBateria + "'>" + bateria + "%</span><br/>" +
               "Velocidade: " + String.format("%.1f", estado.getVelocidade()) + " m/s</html>"
          );
          estadoLabel.setFont(new Font("Arial", Font.PLAIN, 22));
          estadoLabel.setAlignmentX(JLabel.CENTER_ALIGNMENT);

          panel.add(label);
          panel.add(estadoLabel);
          return panel;
     }

}