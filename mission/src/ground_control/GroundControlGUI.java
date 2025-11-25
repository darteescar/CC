package ground_control;

import data.Estado;
import data.Missao;
import java.awt.*;
import java.util.Map;
import javax.swing.*;

public class GroundControlGUI {

     private final RoverListPanel roverList;
     private final MapaPanel mapa;

     public GroundControlGUI(Map<String,Estado> estados,
                              Map<String,Missao> missoes) {

          JFrame frame = new JFrame("Ground Control");
          frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
          frame.setSize(1400, 800);
          frame.setLayout(null);

          Color[] cores = {
                    Color.RED, Color.BLUE, Color.GREEN, Color.ORANGE,
                    Color.MAGENTA, Color.CYAN, Color.PINK, Color.YELLOW
          };

          roverList = new RoverListPanel(estados, cores);

          JScrollPane scroll = new JScrollPane(roverList);
          scroll.setBounds(10, 10, 450, 745);
          frame.add(scroll);

          ImageIcon img = new ImageIcon(getClass().getResource("/mapa.jpg"));
          mapa = new MapaPanel(img.getImage(), estados, cores, missoes);
          mapa.setBounds(470, 10, 920, 745);
          frame.add(mapa);

          frame.setVisible(true);
     }

     public void atualizar() {
          roverList.atualizar();
          mapa.repaint();
     }
}
