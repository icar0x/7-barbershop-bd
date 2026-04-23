package Barbearia;

import Barbearia.Conexao;
import Barbearia.PainelBase;

import javax.swing.*;
import java.awt.*;
import java.sql.*;

class TelaAgendamentos extends PainelBase {

    private JComboBox<String> combCliente, combBarbeiro, combStatus, combFiltroStatus;
    private JTextField campServico, campValor, campDataHora;
    private int idSelecionado = -1;

    public TelaAgendamentos() {
        add(criarTitulo("📅  Gerenciar Agendamentos"), BorderLayout.NORTH);

        JPanel card = criarCard();
        card.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        combCliente  = new JComboBox<>();
        combBarbeiro = new JComboBox<>();
        combStatus   = new JComboBox<>(new String[]{"Agendado", "Concluído", "Cancelado"});
        campServico  = criarCampo();
        campValor    = criarCampo();
        campDataHora = criarCampo();

        estilizarCombo(combCliente); estilizarCombo(combBarbeiro); estilizarCombo(combStatus);
        carregarCombos();

        int r = 0;
        adicionarLinhaCampo(card, gbc, r++, "Cliente *",          combCliente);
        adicionarLinhaCampo(card, gbc, r++, "Barbeiro *",         combBarbeiro);
        adicionarLinhaCampo(card, gbc, r++, "Serviço *",          campServico);
        adicionarLinhaCampo(card, gbc, r++, "Valor (R$) *",       campValor);
        adicionarLinhaCampo(card, gbc, r++, "Data/Hora *",        campDataHora);

        JLabel hint = new JLabel("  ex: 2026-04-25 14:30:00");
        hint.setFont(new Font("Verdana", Font.ITALIC, 10));
        hint.setForeground(new Color(130, 120, 160));
        gbc.gridx = 1; gbc.gridy = r++; gbc.gridwidth = 1;
        card.add(hint, gbc);

        adicionarLinhaCampo(card, gbc, r++, "Status",             combStatus);

        JPanel painelBotoes = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        painelBotoes.setOpaque(false);
        JButton btnSalvar    = criarBotao("💾 Salvar",    COR_DOURADO);
        JButton btnAtualizar = criarBotao("✏️ Atualizar",  new Color(80, 160, 230));
        JButton btnExcluir   = criarBotao("🗑️ Excluir",   new Color(220, 80, 80));
        JButton btnLimpar    = criarBotao("✖ Limpar",    new Color(100, 90, 120));
        painelBotoes.add(btnSalvar); painelBotoes.add(btnAtualizar);
        painelBotoes.add(btnExcluir); painelBotoes.add(btnLimpar);

        gbc.gridx = 0; gbc.gridy = r; gbc.gridwidth = 2;
        gbc.insets = new Insets(14, 5, 5, 5);
        card.add(painelBotoes, gbc);

        // Painel direito (filtro + tabela)
        JPanel painelDireito = new JPanel(new BorderLayout(0, 10));
        painelDireito.setOpaque(false);

        JPanel painelFiltro = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        painelFiltro.setOpaque(false);
        combFiltroStatus = new JComboBox<>(new String[]{"Todos", "Agendado", "Concluído", "Cancelado"});
        estilizarCombo(combFiltroStatus);
        combFiltroStatus.setPreferredSize(new Dimension(150, 34));
        JButton btnFiltrar = criarBotao("🔍 Filtrar", new Color(80, 200, 140));
        painelFiltro.add(criarLabel("Status: "));
        painelFiltro.add(combFiltroStatus);
        painelFiltro.add(btnFiltrar);

        // Tabela com INNER JOIN (mostrada via carregar)
        JScrollPane scroll = new JScrollPane(criarTabela(
                new String[]{"ID", "Cliente", "Barbeiro", "Serviço", "Valor", "Data/Hora", "Status"}));
        scroll.getViewport().setBackground(COR_CARD);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(50, 42, 75), 1));

        painelDireito.add(painelFiltro, BorderLayout.NORTH);
        painelDireito.add(scroll, BorderLayout.CENTER);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, card, painelDireito);
        split.setDividerLocation(340);
        split.setOpaque(false); split.setBorder(null); split.setBackground(COR_FUNDO);
        add(split, BorderLayout.CENTER);

        btnSalvar.addActionListener(e -> salvar());
        btnAtualizar.addActionListener(e -> atualizar());
        btnExcluir.addActionListener(e -> excluir());
        btnLimpar.addActionListener(e -> limpar());
        btnFiltrar.addActionListener(e -> {
            String f = (String) combFiltroStatus.getSelectedItem();
            carregar("Todos".equals(f) ? "" : f);
        });
        tabela.getSelectionModel().addListSelectionListener(e -> preencherFormulario());

        carregar("");
    }

    private void adicionarLinhaCampo(JPanel card, GridBagConstraints gbc, int row, String label, JComponent campo) {
        gbc.gridwidth = 1; gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.35;
        gbc.insets = new Insets(5, 5, 5, 5);
        card.add(criarLabel(label), gbc);
        gbc.gridx = 1; gbc.weightx = 0.65;
        card.add(campo, gbc);
    }

    private void estilizarCombo(JComboBox<String> combo) {
        combo.setBackground(COR_CAMPO);
        combo.setForeground(Color.WHITE);
        combo.setFont(new Font("Verdana", Font.PLAIN, 13));
    }

    private void carregarCombos() {
        try (Connection conn = Conexao.conectar()) {
            ResultSet rs = conn.createStatement().executeQuery("SELECT id, nome FROM clientes ORDER BY nome");
            combCliente.addItem("Selecione...");
            while (rs.next()) combCliente.addItem(rs.getInt("id") + " - " + rs.getString("nome"));

            rs = conn.createStatement().executeQuery("SELECT id, nome FROM barbeiros WHERE ativo=true ORDER BY nome");
            combBarbeiro.addItem("Selecione...");
            while (rs.next()) combBarbeiro.addItem(rs.getInt("id") + " - " + rs.getString("nome"));
        } catch (SQLException ex) { JOptionPane.showMessageDialog(this, "Erro ao carregar dados: " + ex.getMessage()); }
    }

    // INNER JOIN - busca agendamentos com nome do cliente e barbeiro
    private void carregar(String filtroStatus) {
        modeloTabela.setRowCount(0);
        String sql = "SELECT a.id, c.nome AS cliente, b.nome AS barbeiro, " +
                "a.servico, a.valor, a.data_hora, a.status " +
                "FROM agendamentos a " +
                "INNER JOIN clientes c ON c.id = a.cliente_id " +
                "INNER JOIN barbeiros b ON b.id = a.barbeiro_id " +
                (filtroStatus.isEmpty() ? "" : "WHERE a.status = ? ") +
                "ORDER BY a.data_hora DESC";
        try (Connection conn = Conexao.conectar(); PreparedStatement ps = conn.prepareStatement(sql)) {
            if (!filtroStatus.isEmpty()) ps.setString(1, filtroStatus);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                modeloTabela.addRow(new Object[]{
                        rs.getInt("id"), rs.getString("cliente"), rs.getString("barbeiro"),
                        rs.getString("servico"), "R$ " + rs.getString("valor"),
                        rs.getTimestamp("data_hora"), rs.getString("status")
                });
            }
        } catch (SQLException ex) { JOptionPane.showMessageDialog(this, "Erro: " + ex.getMessage()); }
    }

    private int getIdCombo(JComboBox<String> combo) {
        Object sel = combo.getSelectedItem();
        if (sel == null || sel.toString().startsWith("Selecione")) return -1;
        return Integer.parseInt(sel.toString().split(" - ")[0]);
    }

    private void salvar() {
        int clienteId = getIdCombo(combCliente);
        int barbeiroId = getIdCombo(combBarbeiro);
        String servico = campServico.getText().trim();
        String valorStr = campValor.getText().trim();
        String dataHora = campDataHora.getText().trim();

        if (clienteId < 0 || barbeiroId < 0 || servico.isEmpty() || valorStr.isEmpty() || dataHora.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Preencha todos os campos obrigatórios!"); return;
        }
        String sql = "INSERT INTO agendamentos (cliente_id, barbeiro_id, servico, valor, data_hora, status) VALUES (?,?,?,?,?,?)";
        try (Connection conn = Conexao.conectar(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, clienteId); ps.setInt(2, barbeiroId); ps.setString(3, servico);
            ps.setDouble(4, Double.parseDouble(valorStr));
            ps.setTimestamp(5, Timestamp.valueOf(dataHora));
            ps.setString(6, (String) combStatus.getSelectedItem());
            ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "Agendamento salvo!");
            limpar(); carregar("");
        } catch (Exception ex) { JOptionPane.showMessageDialog(this, "Erro: " + ex.getMessage()); }
    }

    private void atualizar() {
        if (idSelecionado < 0) { JOptionPane.showMessageDialog(this, "Selecione um agendamento!"); return; }
        String sql = "UPDATE agendamentos SET servico=?, valor=?, status=? WHERE id=?";
        try (Connection conn = Conexao.conectar(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, campServico.getText().trim());
            ps.setDouble(2, Double.parseDouble(campValor.getText().trim().replace("R$ ", "")));
            ps.setString(3, (String) combStatus.getSelectedItem());
            ps.setInt(4, idSelecionado);
            ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "Agendamento atualizado!");
            limpar(); carregar("");
        } catch (Exception ex) { JOptionPane.showMessageDialog(this, "Erro: " + ex.getMessage()); }
    }

    private void excluir() {
        if (idSelecionado < 0) { JOptionPane.showMessageDialog(this, "Selecione um agendamento!"); return; }
        int c = JOptionPane.showConfirmDialog(this, "Excluir agendamento?", "Confirmar", JOptionPane.YES_NO_OPTION);
        if (c != JOptionPane.YES_OPTION) return;
        try (Connection conn = Conexao.conectar();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM agendamentos WHERE id=?")) {
            ps.setInt(1, idSelecionado); ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "Agendamento excluído!");
            limpar(); carregar("");
        } catch (SQLException ex) { JOptionPane.showMessageDialog(this, "Erro: " + ex.getMessage()); }
    }

    private void preencherFormulario() {
        int row = tabela.getSelectedRow();
        if (row < 0) return;
        idSelecionado = (int) modeloTabela.getValueAt(row, 0);
        campServico.setText((String) modeloTabela.getValueAt(row, 3));
        campValor.setText(modeloTabela.getValueAt(row, 4).toString().replace("R$ ", ""));
        combStatus.setSelectedItem(modeloTabela.getValueAt(row, 6));
    }

    private void limpar() {
        idSelecionado = -1;
        combCliente.setSelectedIndex(0); combBarbeiro.setSelectedIndex(0);
        combStatus.setSelectedIndex(0); campServico.setText("");
        campValor.setText(""); campDataHora.setText(""); tabela.clearSelection();
    }
}
