package ground_control;

import data.Estado;
import data.Missao;
import java.awt.*;
import java.io.File;
import java.util.List;
import java.util.Map;
import javax.swing.*;

public class GroundControlGUI {

    private final RoverPanel roverList;
    private final MissoesConcluidasPanel missoesConcluidasPanel;
    private final MapaPanel mapa;
    private final MissoesAtuaisPanel missoesAtuaisPanel;

     public GroundControlGUI(Map<String,Estado> estados,
                             Map<String,Missao> missoesAtuais,
                             List<Missao> missoesConcluidas,
                             Map<String,File> reports) {

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
        roverList = new RoverPanel(estados, cores, reports, missoesAtuais);
        JScrollPane roversScroll = new JScrollPane(roverList);
        roversScroll.setPreferredSize(new Dimension(300, 840));
        roversScroll.setMinimumSize(new Dimension(300, 0));
        roversScroll.setMaximumSize(new Dimension(300, Integer.MAX_VALUE));
        roversScroll.getVerticalScrollBar().setPreferredSize(new Dimension(20, 0));
        roversScroll.getVerticalScrollBar().setUnitIncrement(20);

        // ---------------------- MISSÕES ATUAIS ----------------------
        missoesAtuaisPanel = new MissoesAtuaisPanel(missoesAtuais, cores);
        JScrollPane missoesAtuaisScroll = new JScrollPane(missoesAtuaisPanel);
        missoesAtuaisScroll.setPreferredSize(new Dimension(330, 800));
        missoesAtuaisScroll.setMinimumSize(new Dimension(330, 100));
        missoesAtuaisScroll.setMaximumSize(new Dimension(330, Integer.MAX_VALUE));
        missoesAtuaisScroll.getVerticalScrollBar().setPreferredSize(new Dimension(20, 0));
        missoesAtuaisScroll.getVerticalScrollBar().setUnitIncrement(20);

        // ---------------------- MISSÕES CONCLUÍDAS ---------------------
        missoesConcluidasPanel = new MissoesConcluidasPanel(missoesConcluidas);
        JScrollPane missoesConcluidasScroll = new JScrollPane(missoesConcluidasPanel);
        missoesConcluidasScroll.setPreferredSize(new Dimension(330, 840));
        missoesConcluidasScroll.setMinimumSize(new Dimension(330, 0));
        missoesConcluidasScroll.setMaximumSize(new Dimension(330, Integer.MAX_VALUE));
        missoesConcluidasScroll.getVerticalScrollBar().setPreferredSize(new Dimension(20, 0));
        missoesConcluidasScroll.getVerticalScrollBar().setUnitIncrement(20);

        // container para colocar missoes organizadas verticalmente
        JPanel missoesContainer = new JPanel();
        missoesContainer.setLayout(new BoxLayout(missoesContainer, BoxLayout.Y_AXIS));
        missoesContainer.setPreferredSize(new Dimension(330, 840));
        missoesContainer.setMinimumSize(new Dimension(330, 0));
        missoesContainer.setMaximumSize(new Dimension(330, Integer.MAX_VALUE));
        missoesContainer.add(missoesAtuaisScroll);
        missoesContainer.add(Box.createRigidArea(new Dimension(0, 10)));
        missoesContainer.add(missoesConcluidasScroll);

        // ---------------------- MAPA ------------------------
        ImageIcon img = new ImageIcon(getClass().getResource("/mapa.jpg"));
        mapa = new MapaPanel(img.getImage(), estados, cores, missoesAtuais);
        mapa.setMinimumSize(new Dimension(600, 840));
        mapa.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
        mapa.setPreferredSize(new Dimension(810, 840));

        // Adicionar ao painel horizontal
        mainPanel.add(roversScroll);
        mainPanel.add(Box.createRigidArea(new Dimension(10, 0))); // 10px de espaçamento
        mainPanel.add(missoesContainer);
        mainPanel.add(Box.createRigidArea(new Dimension(10, 0))); // 10px de espaçamento
        mainPanel.add(mapa);

        // Adicionar à frame
        frame.add(mainPanel, BorderLayout.CENTER);

        frame.setVisible(true);
    }

    public void atualizar() {
        SwingUtilities.invokeLater(() -> {
            roverList.atualizar();
            missoesAtuaisPanel.atualizar();
            missoesConcluidasPanel.atualizar();
            mapa.repaint();
        });
    }
}