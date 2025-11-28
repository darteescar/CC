package ground_control;

import data.Estado;
import data.Missao;
import java.awt.*;
import java.util.Map;
import javax.swing.*;

public class GroundControlGUI {

    private final RoverListPanel roverList;
    private final MissoesConcluidasListPanel missoesList;
    private final MapaPanel mapa;

     public GroundControlGUI(Map<String,Estado> estados,
                             Map<String,Missao> missoes) {

          Color[] cores = {
                    Color.RED, Color.BLUE, Color.GREEN, Color.ORANGE,
                    Color.MAGENTA, Color.CYAN, Color.PINK, Color.YELLOW
          };

          JFrame frame = new JFrame("Ground Control");
          frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
          frame.setSize(1500, 900);
          frame.setLayout(new BorderLayout());

          // Painel principal horizontal
          JPanel mainPanel = new JPanel();
          mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.X_AXIS));
          mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); // top, left, bottom, right

          // ---------------------- ROVERS ----------------------
          roverList = new RoverListPanel(estados, cores);
          JScrollPane roversScroll = new JScrollPane(roverList);
          roversScroll.setPreferredSize(new Dimension(300, 840));
          roversScroll.setMinimumSize(new Dimension(300, 0));
          roversScroll.setMaximumSize(new Dimension(300, Integer.MAX_VALUE));
          roversScroll.getVerticalScrollBar().setPreferredSize(new Dimension(20, 0));

          // ---------------------- MISSÕES ---------------------
          missoesList = new MissoesConcluidasListPanel(missoes);
          JScrollPane missoesScroll = new JScrollPane(missoesList);
          missoesScroll.setPreferredSize(new Dimension(330, 840));
          missoesScroll.setMinimumSize(new Dimension(330, 0));
          missoesScroll.setMaximumSize(new Dimension(330, Integer.MAX_VALUE));
          missoesScroll.getVerticalScrollBar().setPreferredSize(new Dimension(20, 0));

          // ---------------------- MAPA ------------------------
          ImageIcon img = new ImageIcon(getClass().getResource("/mapa.jpg"));
          mapa = new MapaPanel(img.getImage(), estados, cores, missoes);
          mapa.setPreferredSize(new Dimension(810, 840));
          mapa.setMinimumSize(new Dimension(600, 840));

          // Adicionar ao painel horizontal
          mainPanel.add(roversScroll);
          mainPanel.add(Box.createRigidArea(new Dimension(10, 0))); // 10px de espaçamento
          mainPanel.add(missoesScroll);
          mainPanel.add(Box.createRigidArea(new Dimension(10, 0))); // 10px de espaçamento
          mainPanel.add(mapa);

          // Adicionar à frame
          frame.add(mainPanel, BorderLayout.CENTER);

          frame.setVisible(true);
    }

    public void atualizar() {
        SwingUtilities.invokeLater(() -> {
            roverList.atualizar();
            missoesList.atualizar();
            mapa.repaint();
        });
    }
}