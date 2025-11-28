package ground_control;

import data.Missao;
import java.awt.*;
import java.util.Map;
import javax.swing.*;

public class MissoesConcluidasListPanel extends JPanel {
     private final Map<String,Missao> missoesConcluidas;

     public MissoesConcluidasListPanel (Map<String,Missao> missoes) {
          this.missoesConcluidas = missoes;

          setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
          setBackground(Color.WHITE);
     }

     public void atualizar() {
          removeAll();

          for (var entry : missoesConcluidas.entrySet()) {
               add(criarPainel(entry.getValue()));
          }

          revalidate();
          repaint();
     }

     private JPanel criarPainel(Missao m) {
          JPanel p = new JPanel();
          p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
          p.setBackground(Color.WHITE);

          String id = "Missão " + String.valueOf(m.getId());
          JLabel titulo = new JLabel(id);
          titulo.setFont(new Font("Arial", Font.BOLD, 20));
          titulo.setAlignmentX(CENTER_ALIGNMENT);

          JLabel info = new JLabel(String.format(
          "<html>"
               + "<b>ID:</b> %d<br>"
               + "<b>Área:</b> (%.1f, %.1f) → (%.1f, %.1f)<br>"
               + "<b>Tarefa:</b> %s<br>"
               + "<b>Duração:</b> %d min<br>"
               + "<b>Freq. Update:</b> %d s"
               + "</html>",
          m.getId(),
          m.getX1(), m.getY1(),
          m.getX2(), m.getY2(),
          m.getTarefa(),
          m.getDuracao(),
          m.getFreqUpdate()
          ));
          info.setFont(new Font("Arial", Font.PLAIN, 18));
          info.setAlignmentX(CENTER_ALIGNMENT);

          p.add(titulo);
          p.add(info);

          return p;
     }
}