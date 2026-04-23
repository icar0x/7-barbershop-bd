package Barbearia;

import Barbearia.Conexao;
import Barbearia.PainelBase;

import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class TelaBarbeiros extends PainelBase {

    private JTextField campNome, campEspecialidade, campTelefone;
    private JCheckBox campAtivo;
    private int idSelecionado = -1;

    public TelaBarbeiros() {
        add(criarTitulo("💈  Gerenciar Barbeiros"), BorderLayout.NORTH);

        JPanel card = criarCard();
        card.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        campNome          = criarCampo();
        campEspecialidade = criarCampo();
        campTelefone      = criarCampo();
        campAtivo         = new JCheckBox("Ativo");
        campAtivo.setOpaque(false);
        campAtivo.setForeground(COR_TEXTO);
        campAtivo.setSelected(true);

        int r = 0;
        adicionarLinhaCampo(card, gbc, r++, "Nome *",          campNome);
        adicionarLinhaCampo(card, gbc, r++, "Especialidade",   campEspecialidade);
        adicionarLinhaCampo(card, gbc, r++, "Telefone",        campTelefone);

        gbc.gridwidth = 1; gbc.gridx = 0; gbc.gridy = r; gbc.weightx = 0.3;
        card.add(criarLabel("Status"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.7;
        card.add(campAtivo, gbc);
        r++;

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

        JScrollPane scroll = new JScrollPane(criarTabela(
                new String[]{"ID", "Nome", "Especialidade", "Telefone", "Ativo"}));
        scroll.getViewport().setBackground(COR_CARD);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(50, 42, 75), 1));

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, card, scroll);
        split.setDividerLocation(320);
        split.setOpaque(false); split.setBorder(null); split.setBackground(COR_FUNDO);
        add(split, BorderLayout.CENTER);

        btnSalvar.addActionListener(e -> salvar());
        btnAtualizar.addActionListener(e -> atualizar());
        btnExcluir.addActionListener(e -> excluir());
        btnLimpar.addActionListener(e -> limpar());
        tabela.getSelectionModel().addListSelectionListener(e -> preencherFormulario());

        carregar();
    }

    private void adicionarLinhaCampo(JPanel card, GridBagConstraints gbc, int row, String label, JTextField campo) {
        gbc.gridwidth = 1; gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.3;
        card.add(criarLabel(label), gbc);
        gbc.gridx = 1; gbc.weightx = 0.7;
        card.add(campo, gbc);
    }

    private void carregar() {
        modeloTabela.setRowCount(0);
        String sql = "SELECT id, nome, especialidade, telefone, ativo FROM barbeiros ORDER BY nome";
        try (Connection conn = Conexao.conectar();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                modeloTabela.addRow(new Object[]{
                        rs.getInt("id"), rs.getString("nome"),
                        rs.getString("especialidade"), rs.getString("telefone"),
                        rs.getBoolean("ativo") ? "Sim" : "Não"
                });
            }
        } catch (SQLException ex) { JOptionPane.showMessageDialog(this, "Erro: " + ex.getMessage()); }
    }

    private void salvar() {
        String nome = campNome.getText().trim();
        if (nome.isEmpty()) { JOptionPane.showMessageDialog(this, "Nome é obrigatório!"); return; }
        String sql = "INSERT INTO barbeiros (nome, especialidade, telefone, ativo) VALUES (?, ?, ?, ?)";
        try (Connection conn = Conexao.conectar(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nome); ps.setString(2, campEspecialidade.getText().trim());
            ps.setString(3, campTelefone.getText().trim()); ps.setBoolean(4, campAtivo.isSelected());
            ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "Barbeiro cadastrado!");
            limpar(); carregar();
        } catch (SQLException ex) { JOptionPane.showMessageDialog(this, "Erro: " + ex.getMessage()); }
    }

    private void atualizar() {
        if (idSelecionado < 0) { JOptionPane.showMessageDialog(this, "Selecione um barbeiro!"); return; }
        String sql = "UPDATE barbeiros SET nome=?, especialidade=?, telefone=?, ativo=? WHERE id=?";
        try (Connection conn = Conexao.conectar(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, campNome.getText().trim()); ps.setString(2, campEspecialidade.getText().trim());
            ps.setString(3, campTelefone.getText().trim()); ps.setBoolean(4, campAtivo.isSelected());
            ps.setInt(5, idSelecionado); ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "Barbeiro atualizado!");
            limpar(); carregar();
        } catch (SQLException ex) { JOptionPane.showMessageDialog(this, "Erro: " + ex.getMessage()); }
    }

    private void excluir() {
        if (idSelecionado < 0) { JOptionPane.showMessageDialog(this, "Selecione um barbeiro!"); return; }
        int c = JOptionPane.showConfirmDialog(this, "Excluir este barbeiro?", "Confirmar", JOptionPane.YES_NO_OPTION);
        if (c != JOptionPane.YES_OPTION) return;
        try (Connection conn = Conexao.conectar();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM barbeiros WHERE id=?")) {
            ps.setInt(1, idSelecionado); ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "Barbeiro excluído!");
            limpar(); carregar();
        } catch (SQLException ex) { JOptionPane.showMessageDialog(this, "Erro: " + ex.getMessage()); }
    }

    private void preencherFormulario() {
        int row = tabela.getSelectedRow();
        if (row < 0) return;
        idSelecionado = (int) modeloTabela.getValueAt(row, 0);
        campNome.setText((String) modeloTabela.getValueAt(row, 1));
        campEspecialidade.setText(modeloTabela.getValueAt(row, 2) == null ? "" : (String) modeloTabela.getValueAt(row, 2));
        campTelefone.setText(modeloTabela.getValueAt(row, 3) == null ? "" : (String) modeloTabela.getValueAt(row, 3));
        campAtivo.setSelected("Sim".equals(modeloTabela.getValueAt(row, 4)));
    }

    private void limpar() {
        idSelecionado = -1;
        campNome.setText(""); campEspecialidade.setText(""); campTelefone.setText("");
        campAtivo.setSelected(true); tabela.clearSelection();
    }
}