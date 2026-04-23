package Barbearia;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.*;

public class TelaLogin extends JFrame {

    private JTextField campUsuario;
    private JPasswordField campSenha;

    public TelaLogin() {
        setTitle("7 Barbershop - Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(420, 520);
        setLocationRelativeTo(null);
        setResizable(false);

        // Painel principal com fundo escuro
        JPanel painelPrincipal = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, new Color(15, 15, 25),
                        0, getHeight(), new Color(30, 20, 50));
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        painelPrincipal.setLayout(new BoxLayout(painelPrincipal, BoxLayout.Y_AXIS));
        painelPrincipal.setBorder(new EmptyBorder(40, 50, 40, 50));

        // Ícone/Logo
        JLabel icone = new JLabel("✂", SwingConstants.CENTER);
        icone.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 60));
        icone.setForeground(new Color(212, 175, 55));
        icone.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Título
        JLabel titulo = new JLabel("7 BARBERSHOP", SwingConstants.CENTER);
        titulo.setFont(new Font("Georgia", Font.BOLD, 28));
        titulo.setForeground(new Color(212, 175, 55));
        titulo.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitulo = new JLabel("Sistema de Gestão", SwingConstants.CENTER);
        subtitulo.setFont(new Font("Georgia", Font.ITALIC, 13));
        subtitulo.setForeground(new Color(180, 180, 180));
        subtitulo.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Separador dourado
        JSeparator sep = new JSeparator();
        sep.setForeground(new Color(212, 175, 55));
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));

        // Campo usuário
        JLabel lblUsuario = new JLabel("USUÁRIO");
        lblUsuario.setFont(new Font("Verdana", Font.BOLD, 11));
        lblUsuario.setForeground(new Color(212, 175, 55));
        lblUsuario.setAlignmentX(Component.CENTER_ALIGNMENT);

        campUsuario = new JTextField();
        estilizarCampo(campUsuario);

        // Campo senha
        JLabel lblSenha = new JLabel("SENHA");
        lblSenha.setFont(new Font("Verdana", Font.BOLD, 11));
        lblSenha.setForeground(new Color(212, 175, 55));
        lblSenha.setAlignmentX(Component.CENTER_ALIGNMENT);

        campSenha = new JPasswordField();
        estilizarCampo(campSenha);

        // Botão entrar
        JButton btnEntrar = new JButton("ENTRAR");
        btnEntrar.setFont(new Font("Verdana", Font.BOLD, 14));
        btnEntrar.setBackground(new Color(212, 175, 55));
        btnEntrar.setForeground(new Color(15, 15, 25));
        btnEntrar.setFocusPainted(false);
        btnEntrar.setBorderPainted(false);
        btnEntrar.setOpaque(true);
        btnEntrar.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnEntrar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));
        btnEntrar.setAlignmentX(Component.CENTER_ALIGNMENT);

        btnEntrar.addActionListener(e -> fazerLogin());
        campSenha.addActionListener(e -> fazerLogin());

        // Hover no botão
        btnEntrar.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) {
                btnEntrar.setBackground(new Color(240, 200, 80));
            }
            public void mouseExited(java.awt.event.MouseEvent e) {
                btnEntrar.setBackground(new Color(212, 175, 55));
            }
        });

        // Montagem
        painelPrincipal.add(icone);
        painelPrincipal.add(Box.createVerticalStrut(10));
        painelPrincipal.add(titulo);
        painelPrincipal.add(Box.createVerticalStrut(4));
        painelPrincipal.add(subtitulo);
        painelPrincipal.add(Box.createVerticalStrut(20));
        painelPrincipal.add(sep);
        painelPrincipal.add(Box.createVerticalStrut(25));
        painelPrincipal.add(lblUsuario);
        painelPrincipal.add(Box.createVerticalStrut(6));
        painelPrincipal.add(campUsuario);
        painelPrincipal.add(Box.createVerticalStrut(16));
        painelPrincipal.add(lblSenha);
        painelPrincipal.add(Box.createVerticalStrut(6));
        painelPrincipal.add(campSenha);
        painelPrincipal.add(Box.createVerticalStrut(28));
        painelPrincipal.add(btnEntrar);

        setContentPane(painelPrincipal);
        setVisible(true);
    }

    private void estilizarCampo(JTextField campo) {
        campo.setFont(new Font("Verdana", Font.PLAIN, 14));
        campo.setBackground(new Color(40, 35, 60));
        campo.setForeground(Color.WHITE);
        campo.setCaretColor(new Color(212, 175, 55));
        campo.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(212, 175, 55), 1),
                new EmptyBorder(8, 12, 8, 12)
        ));
        campo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));
    }

    private void fazerLogin() {
        String usuario = campUsuario.getText().trim();
        String senha = new String(campSenha.getPassword()).trim();

        if (usuario.isEmpty() || senha.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Preencha todos os campos!", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try (Connection conn = Conexao.conectar()) {
            String sql = "SELECT id FROM usuarios WHERE username = ? AND senha = ?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, usuario);
            ps.setString(2, senha);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                dispose();
                new TelaPrincipal();
            } else {
                JOptionPane.showMessageDialog(this, "Usuário ou senha incorretos!", "Erro de Login", JOptionPane.ERROR_MESSAGE);
                campSenha.setText("");
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Erro ao conectar ao banco:\n" + ex.getMessage(),
                    "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(TelaLogin::new);
    }
}