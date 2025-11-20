package ground_control;

import data.Estado;
import data.Missao;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.util.Map;
import javax.swing.JPanel;

public class MapaPanel extends JPanel {
     private final Image fundo;
     private final Map<String, Estado> estados;
     private final Map<String, Missao> missoes;
     private final Map<String, Color> coresRovers;

     public MapaPanel(Image fundo, Map<String, Estado> estados, 
                      Map<String, Color> coresRovers, Map<String, Missao> missoes) {

          this.fundo = fundo;
          this.estados = estados;
          this.coresRovers = coresRovers;
          this.missoes = missoes;
     }

     @Override
     protected void paintComponent(Graphics g) {
          super.paintComponent(g);

          // Fundo
          g.drawImage(fundo, 0, 0, getWidth(), getHeight(), this);

          // Desenhar rovers e missões existentes
          for (String nome : estados.keySet()) {

               Estado e = estados.get(nome);
               Missao m = missoes.get(nome);
               Color cor = coresRovers.get(nome);

               if (cor == null) 
                   cor = Color.WHITE; // fallback seguro

               if (m != null)
                    desenhaAreaMissao(g, m, cor);

               if (e != null)
                    desenhaRover(g, e, cor);
          }
     }

     /** Desenha o rover como um ponto colorido */
     private void desenhaRover(Graphics g, Estado e, Color cor) {
          g.setColor(cor);

          int x = (int) (e.getX() * getWidth() / 100.0);
          int y = (int) (e.getY() * getHeight() / 100.0);

          g.fillOval(x - 5, y - 5, 20, 20);
     }

     /** Desenha a área da missão correspondente */
     private void desenhaAreaMissao(Graphics g, Missao m, Color cor) {
          g.setColor(cor);

          int px = (int) (m.getX1() * getWidth() / 100.0);
          int py = (int) (m.getY1() * getHeight() / 100.0);
          int largura = (int) ((m.getX2() - m.getX1()) * getWidth() / 100.0);
          int altura = (int) ((m.getY2() - m.getY1()) * getHeight() / 100.0);

          g.drawRect(px, py, largura, altura);
     }
}
