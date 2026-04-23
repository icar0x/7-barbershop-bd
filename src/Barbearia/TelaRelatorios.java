package Barbearia;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class TelaRelatorios extends PainelBase {

    public TelaRelatorios() {
        add(criarTitulo("📊  Relatórios"), BorderLayout.NORTH);

        JTabbedPane abas = new JTabbedPane();
        abas.setBackground(COR_CARD);
        abas.setForeground(COR_DOURADO);
        abas.setFont(new Font("Verdana", Font.BOLD, 12));

        abas.addTab("Faturamento por Barbeiro", criarAbaFaturamento());
        abas.addTab("Clientes sem Agendamento", criarAbaClientesSemAgendamento());

        add(abas, BorderLayout.CENTER);
    }

    // LEFT JOIN - faturamento por barbeiro incluindo quem não tem agendamentos concluídos
    private JPanel criarAbaFaturamento() {
        JPanel painel = new JPanel(new BorderLayout(0, 10));
        painel.setBackground(COR_FUNDO);
        painel.setBorder(new EmptyBorder(15, 10, 10, 10));

        JLabel desc = new JLabel("Faturamento por barbeiro (LEFT JOIN entre barbeiros e agendamentos concluídos)");
        desc.setFont(new Font("Verdana", Font.ITALIC, 11));
        desc.setForeground(new Color(160, 150, 190));

        DefaultTableModel modelo = new DefaultTableModel(
                new String[]{"Barbeiro", "Especialidade", "Total Serviços", "Faturamento (R$)"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable tabela = criarTabelaEstilizada(modelo);

        JButton btnCarregar = criarBotao("🔄 Carregar", COR_DOURADO);
        btnCarregar.addActionListener(e -> {
            modelo.setRowCount(0);
            String sql = "SELECT b.nome, b.especialidade, " +
                    "COUNT(a.id) AS total, COALESCE(SUM(a.valor), 0) AS faturamento " +
                    "FROM barbeiros b " +
                    "LEFT JOIN agendamentos a ON a.barbeiro_id = b.id AND a.status = 'Concluído' " +
                    "GROUP BY b.id, b.nome, b.especialidade " +
                    "ORDER BY faturamento DESC";
            try (Connection conn = Conexao.conectar();
                 PreparedStatement ps = conn.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    modelo.addRow(new Object[]{
                            rs.getString("nome"), rs.getString("especialidade"),
                            rs.getInt("total"), "R$ " + rs.getString("faturamento")
                    });
                }
            } catch (SQLException ex) { JOptionPane.showMessageDialog(this, "Erro: " + ex.getMessage()); }
        });

        JScrollPane scroll = new JScrollPane(tabela);
        scroll.getViewport().setBackground(COR_CARD);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(50, 42, 75), 1));

        painel.add(desc, BorderLayout.NORTH);
        painel.add(scroll, BorderLayout.CENTER);
        painel.add(btnCarregar, BorderLayout.SOUTH);
        return painel;
    }

    // LEFT JOIN - clientes que nunca fizeram agendamento
    private JPanel criarAbaClientesSemAgendamento() {
        JPanel painel = new JPanel(new BorderLayout(0, 10));
        painel.setBackground(COR_FUNDO);
        painel.setBorder(new EmptyBorder(15, 10, 10, 10));

        JLabel desc = new JLabel("Clientes cadastrados e seu total de agendamentos (LEFT JOIN)");
        desc.setFont(new Font("Verdana", Font.ITALIC, 11));
        desc.setForeground(new Color(160, 150, 190));

        DefaultTableModel modelo = new DefaultTableModel(
                new String[]{"Cliente", "Telefone", "Total Agendamentos", "Total Gasto (R$)"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable tabela = criarTabelaEstilizada(modelo);

        JButton btnCarregar = criarBotao("🔄 Carregar", COR_DOURADO);
        btnCarregar.addActionListener(e -> {
            modelo.setRowCount(0);
            String sql = "SELECT c.nome, c.telefone, " +
                    "COUNT(a.id) AS total, COALESCE(SUM(a.valor), 0) AS gasto " +
                    "FROM clientes c " +
                    "LEFT JOIN agendamentos a ON a.cliente_id = c.id " +
                    "GROUP BY c.id, c.nome, c.telefone " +
                    "ORDER BY total DESC";
            try (Connection conn = Conexao.conectar();
                 PreparedStatement ps = conn.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    modelo.addRow(new Object[]{
                            rs.getString("nome"), rs.getString("telefone"),
                            rs.getInt("total"), "R$ " + rs.getString("gasto")
                    });
                }
            } catch (SQLException ex) { JOptionPane.showMessageDialog(this, "Erro: " + ex.getMessage()); }
        });

        JScrollPane scroll = new JScrollPane(tabela);
        scroll.getViewport().setBackground(COR_CARD);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(50, 42, 75), 1));

        painel.add(desc, BorderLayout.NORTH);
        painel.add(scroll, BorderLayout.CENTER);
        painel.add(btnCarregar, BorderLayout.SOUTH);
        return painel;
    }

    private JTable criarTabelaEstilizada(javax.swing.table.DefaultTableModel modelo) {
        JTable tabela = new JTable(modelo);
        tabela.setBackground(COR_CARD);
        tabela.setForeground(COR_TEXTO);
        tabela.setFont(new Font("Verdana", Font.PLAIN, 12));
        tabela.setRowHeight(30);
        tabela.setGridColor(new Color(40, 35, 60));
        tabela.setSelectionBackground(new Color(80, 60, 120));
        tabela.setShowVerticalLines(false);
        javax.swing.table.JTableHeader header = tabela.getTableHeader();
        header.setBackground(COR_DOURADO);
        header.setForeground(new Color(15, 15, 25));
        header.setFont(new Font("Verdana", Font.BOLD, 12));
        return tabela;
    }
}