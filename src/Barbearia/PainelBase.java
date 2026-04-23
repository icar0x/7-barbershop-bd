package Barbearia;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;

public abstract class PainelBase extends JPanel {

    protected static final Color COR_FUNDO   = new Color(15, 15, 25);
    protected static final Color COR_CARD    = new Color(22, 18, 38);
    protected static final Color COR_DOURADO = new Color(212, 175, 55);
    protected static final Color COR_TEXTO   = new Color(230, 230, 230);
    protected static final Color COR_CAMPO   = new Color(32, 26, 52);

    protected DefaultTableModel modeloTabela;
    protected JTable tabela;

    public PainelBase() {
        setBackground(COR_FUNDO);
        setLayout(new BorderLayout(0, 0));
        setBorder(new EmptyBorder(25, 25, 25, 25));
    }

    protected JLabel criarTitulo(String texto) {
        JLabel lbl = new JLabel(texto);
        lbl.setFont(new Font("Georgia", Font.BOLD, 22));
        lbl.setForeground(COR_DOURADO);
        lbl.setBorder(new EmptyBorder(0, 0, 18, 0));
        return lbl;
    }

    protected JTextField criarCampo() {
        JTextField campo = new JTextField();
        campo.setFont(new Font("Verdana", Font.PLAIN, 13));
        campo.setBackground(COR_CAMPO);
        campo.setForeground(Color.WHITE);
        campo.setCaretColor(COR_DOURADO);
        campo.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(60, 50, 90), 1),
                new EmptyBorder(7, 10, 7, 10)
        ));
        return campo;
    }

    protected JLabel criarLabel(String texto) {
        JLabel lbl = new JLabel(texto);
        lbl.setFont(new Font("Verdana", Font.BOLD, 11));
        lbl.setForeground(new Color(180, 170, 200));
        return lbl;
    }

    protected JButton criarBotao(String texto, Color cor) {
        JButton btn = new JButton(texto);
        btn.setFont(new Font("Verdana", Font.BOLD, 12));
        btn.setBackground(cor);
        btn.setForeground(new Color(15, 15, 25));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setOpaque(true);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(9, 18, 9, 18));
        return btn;
    }

    protected JTable criarTabela(String[] colunas) {
        modeloTabela = new DefaultTableModel(colunas, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tabela = new JTable(modeloTabela);
        tabela.setBackground(COR_CARD);
        tabela.setForeground(COR_TEXTO);
        tabela.setFont(new Font("Verdana", Font.PLAIN, 12));
        tabela.setRowHeight(30);
        tabela.setGridColor(new Color(40, 35, 60));
        tabela.setSelectionBackground(new Color(80, 60, 120));
        tabela.setSelectionForeground(Color.WHITE);
        tabela.setShowVerticalLines(false);

        JTableHeader header = tabela.getTableHeader();
        header.setBackground(COR_DOURADO);
        header.setForeground(new Color(15, 15, 25));
        header.setFont(new Font("Verdana", Font.BOLD, 12));
        header.setBorder(BorderFactory.createEmptyBorder());

        return tabela;
    }

    protected JPanel criarCard() {
        JPanel card = new JPanel();
        card.setBackground(COR_CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(50, 42, 75), 1),
                new EmptyBorder(18, 18, 18, 18)
        ));
        return card;
    }
}
