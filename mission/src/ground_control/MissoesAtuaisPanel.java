package ground_control;

import data.Missao;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import javax.swing.*;
import javax.swing.border.TitledBorder;

public class MissoesAtuaisPanel extends JPanel {
     private final Color[] cores;
     private Map<String,Missao> missoesAtuais;

     public MissoesAtuaisPanel(Map<String,Missao> missoes, Color[] cores){
          this.missoesAtuais = missoes;
          this.cores = cores;

          setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
          setBackground(Color.GRAY);
          setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createEmptyBorder(),
            "Missões Atuais",
            TitledBorder.CENTER,
            TitledBorder.TOP,
            new Font("Arial", Font.BOLD, 24),
            Color.WHITE
          ));
     }

     public void atualizar() {
          removeAll();

          List<String> lista = new ArrayList<>(this.missoesAtuais.keySet());
          Collections.sort(lista);

          int i = 0;
          for (String nome : lista) {
               add(criarPainel(missoesAtuais.get(nome), cores[i % cores.length]));
               i++;
          }

          revalidate();
          repaint();
     }

     private JPanel criarPainel(Missao m, Color cor) {
          JPanel p = new JPanel();
          p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
          p.setBackground(Color.WHITE);

          String id = "Missão " + String.valueOf(m.getId());
          JLabel titulo = new JLabel(id);
          titulo.setFont(new Font("Arial", Font.BOLD, 20));
          titulo.setForeground(cor);
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
