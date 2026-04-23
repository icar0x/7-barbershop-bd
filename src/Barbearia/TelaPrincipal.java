package Barbearia;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class TelaPrincipal extends JFrame {

    private static final Color COR_FUNDO       = new Color(15, 15, 25);
    private static final Color COR_LATERAL      = new Color(22, 18, 38);
    private static final Color COR_DOURADO      = new Color(212, 175, 55);
    private static final Color COR_TEXTO        = new Color(230, 230, 230);
    private static final Color COR_HOVER        = new Color(35, 28, 58);

    private JPanel painelConteudo;

    public TelaPrincipal() {
        setTitle("7 Barbershop");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1100, 680);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // Barra lateral
        JPanel lateral = criarLateral();
        add(lateral, BorderLayout.WEST);

        // Área de conteúdo
        painelConteudo = new JPanel(new BorderLayout());
        painelConteudo.setBackground(COR_FUNDO);
        add(painelConteudo, BorderLayout.CENTER);

        // Mostrar tela inicial
        mostrarPainel(new TelaClientes());

        setVisible(true);
    }

    private JPanel criarLateral() {
        JPanel lateral = new JPanel();
        lateral.setPreferredSize(new Dimension(210, 0));
        lateral.setBackground(COR_LATERAL);
        lateral.setLayout(new BoxLayout(lateral, BoxLayout.Y_AXIS));
        lateral.setBorder(new EmptyBorder(20, 0, 20, 0));

        // Logo
        JLabel logo = new JLabel("✂ 7 BARBERSHOP", SwingConstants.CENTER);
        logo.setFont(new Font("Georgia", Font.BOLD, 16));
        logo.setForeground(COR_DOURADO);
        logo.setAlignmentX(Component.CENTER_ALIGNMENT);
        logo.setBorder(new EmptyBorder(0, 0, 20, 0));

        JSeparator sep = new JSeparator();
        sep.setForeground(new Color(50, 40, 80));
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));

        lateral.add(logo);
        lateral.add(sep);
        lateral.add(Box.createVerticalStrut(15));

        lateral.add(criarBotaoMenu("👤  Clientes",    () -> mostrarPainel(new TelaClientes())));
        lateral.add(criarBotaoMenu("💈  Barbeiros",   () -> mostrarPainel(new TelaBarbeiros())));
        lateral.add(criarBotaoMenu("📅  Agendamentos",() -> mostrarPainel(new TelaAgendamentos())));
        lateral.add(criarBotaoMenu("📊  Relatórios",  () -> mostrarPainel(new TelaRelatorios())));

        lateral.add(Box.createVerticalGlue());

        JSeparator sep2 = new JSeparator();
        sep2.setForeground(new Color(50, 40, 80));
        sep2.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        lateral.add(sep2);
        lateral.add(Box.createVerticalStrut(10));

        lateral.add(criarBotaoMenu("🚪  Sair", () -> {
            dispose();
            new TelaLogin();
        }));

        return lateral;
    }

    private JButton criarBotaoMenu(String texto, Runnable acao) {
        JButton btn = new JButton(texto);
        btn.setFont(new Font("Verdana", Font.PLAIN, 13));
        btn.setForeground(COR_TEXTO);
        btn.setBackground(COR_LATERAL);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setOpaque(true);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setBorder(new EmptyBorder(12, 25, 12, 10));
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) {
                btn.setBackground(COR_HOVER);
                btn.setForeground(COR_DOURADO);
            }
            public void mouseExited(java.awt.event.MouseEvent e) {
                btn.setBackground(COR_LATERAL);
                btn.setForeground(COR_TEXTO);
            }
        });

        btn.addActionListener(e -> acao.run());
        return btn;
    }

    public void mostrarPainel(JPanel painel) {
        painelConteudo.removeAll();
        painelConteudo.add(painel, BorderLayout.CENTER);
        painelConteudo.revalidate();
        painelConteudo.repaint();
    }
}