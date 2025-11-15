package ground_control;

import data.Estado;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.util.Map;
import javax.swing.JPanel;

public class MapaPanel extends JPanel {
     private Image fundo;
     private Map<String, Estado> estados;
     private Map<String, Color> coresRovers;

     public MapaPanel(Image fundo, Map<String, Estado> estados, Map<String, Color> coresRovers) {
          this.fundo = fundo;
          this.estados = estados;
          this.coresRovers = coresRovers;
     }

     @Override
     protected void paintComponent(Graphics g) {
          super.paintComponent(g);
          g.drawImage(fundo, 0, 0, getWidth(), getHeight(), this);

          // Desenha rovers como pontos com cores consistentes
          for (int i = 1; i <= estados.size(); i++) {
               String nome = "R-" + i;
               Estado e = estados.get(nome);
               Color cor = coresRovers.get(nome);
               g.setColor(cor);

               int x = (int) (e.getX() * getWidth() / 100); // Supondo que X varia de 0 a 100
               int y = (int) (e.getY() * getHeight() / 100); // Supondo que Y varia de 0 a 100
               g.fillOval(x - 5, y - 5, 20, 20);
          }
     }
}
