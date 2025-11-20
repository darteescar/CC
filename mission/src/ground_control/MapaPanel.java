package ground_control;

import data.Estado;
import data.Missao;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.util.Map;
import javax.swing.JPanel;

public class MapaPanel extends JPanel {
     private Image fundo;
     private Map<String, Estado> estados;
     private Map<String, Missao> missoes;
     private Map<String, Color> coresRovers;

     public MapaPanel(Image fundo, Map<String, Estado> estados, Map<String, Color> coresRovers, Map<String, Missao> missoes) {
          this.fundo = fundo;
          this.estados = estados;
          this.coresRovers = coresRovers;
          this.missoes = missoes;
     }

     @Override
     protected void paintComponent(Graphics g) {
          super.paintComponent(g);
          g.drawImage(fundo, 0, 0, getWidth(), getHeight(), this);

          // Desenha rovers como pontos com cores consistentes
          for (int i = 1; i <= estados.size(); i++) {
               String nome = "R-" + i;
               Estado e = estados.get(nome);
               Missao m = missoes.get(nome);
               Color cor = coresRovers.get(nome);

               g.setColor(cor);
               int x = (int) (e.getX() * getWidth() / 100); // Supondo que X varia de 0 a 100
               int y = (int) (e.getY() * getHeight() / 100); // Supondo que Y varia de 0 a 100
               g.fillOval(x - 5, y - 5, 20, 20);
          
               double x1 = m.getX1();
               double y1 = m.getY1();
               double x2 = m.getX2();
               double y2 = m.getY2();

               double largura = x2-x1;
               double altura = y2-y1;

               int px = (int) (x1 * getWidth() / 100);
               int py = (int) (y1 * getHeight() / 100);
               int larguraPx = (int) (largura * getWidth() / 100);
               int alturaPx = (int) (altura * getHeight() / 100);

               g.setColor(cor);

               g.drawRect(px, py, larguraPx, alturaPx);
          }
     }
}
