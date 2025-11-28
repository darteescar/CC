package ground_control;

import data.Estado;
import data.Missao;
import java.awt.*;
import java.util.Map;
import javax.swing.*;

public class GroundControlGUI {
     private final RoverListPanel roverList;
     private final MapaPanel mapa;
     private final MissoesConcluidasListPanel missoesList;

     public GroundControlGUI(Map<String,Estado> estados,
                              Map<String,Missao> missoes) {

          Color[] cores = {
               Color.RED, Color.BLUE, Color.GREEN, Color.ORANGE,
               Color.MAGENTA, Color.CYAN, Color.PINK, Color.YELLOW
          };

          // ---------------------- Janela Principal ----------------------
          JFrame frame = new JFrame("Ground Control");
          frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
          frame.setSize(1480, 900);
          frame.setLayout(null);

          // ---------------------- ScrollBar dos Rovers ----------------------
          roverList = new RoverListPanel(estados, cores);
          JScrollPane scroll = new JScrollPane(roverList);
          scroll.setBounds(10, 10, 300, 840); // x,y, comprimento, largura
          frame.add(scroll);

          // ---------------------- ScrollBar das Missões ----------------------
          missoesList = new MissoesConcluidasListPanel(missoes);
          JScrollPane scroll2 = new JScrollPane(missoesList);
          scroll2.setBounds(320, 10, 330, 840); // x,y, comprimento, largura
          

          frame.add(scroll2);

          // ---------------------- Mapa ----------------------
          ImageIcon img = new ImageIcon(getClass().getResource("mapa.jpg"));
          mapa = new MapaPanel(img.getImage(), estados, cores, missoes);
          mapa.setBounds(660, 10, 810, 840);
          frame.add(mapa);


          frame.setVisible(true);
     }

     public void atualizar() {
          roverList.atualizar();
          mapa.repaint();
          missoesList.atualizar();
     }
}
