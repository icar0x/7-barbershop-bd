package Barbearia;

import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class TelaClientes extends PainelBase {

    private JTextField campNome, campTelefone, campEmail, campFiltro;
    private int idSelecionado = -1;

    public TelaClientes() {
        // Título
        add(criarTitulo("  Gerenciar Clientes"), BorderLayout.NORTH);

        // Formulário (card esquerdo)
        JPanel card = criarCard();
        card.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        campNome     = criarCampo();
        campTelefone = criarCampo();
        campEmail    = criarCampo();

        int r = 0;
        adicionarLinhaCampo(card, gbc, r++, "Nome *",     campNome);
        adicionarLinhaCampo(card, gbc, r++, "Telefone",   campTelefone);
        adicionarLinhaCampo(card, gbc, r++, "E-mail",     campEmail);

        // Botões
        JPanel painelBotoes = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        painelBotoes.setOpaque(false);

        JButton btnSalvar   = criarBotao(" Salvar",   COR_DOURADO);
        JButton btnAtualizar = criarBotao("✏ Atualizar", new Color(80, 160, 230));
        JButton btnExcluir  = criarBotao("🗑 Excluir",  new Color(220, 80, 80));
        JButton btnLimpar   = criarBotao(" Limpar",   new Color(100, 90, 120));

        painelBotoes.add(btnSalvar);
        painelBotoes.add(btnAtualizar);
        painelBotoes.add(btnExcluir);
        painelBotoes.add(btnLimpar);

        gbc.gridx = 0; gbc.gridy = r; gbc.gridwidth = 2;
        gbc.insets = new Insets(14, 5, 5, 5);
        card.add(painelBotoes, gbc);

        // Filtro + tabela
        JPanel painelDireito = new JPanel(new BorderLayout(0, 10));
        painelDireito.setOpaque(false);

        JPanel painelFiltro = new JPanel(new BorderLayout(8, 0));
        painelFiltro.setOpaque(false);
        campFiltro = criarCampo();
        campFiltro.putClientProperty("JTextField.placeholderText", "Buscar por nome...");
        JButton btnFiltrar = criarBotao("🔍 Filtrar", new Color(80, 200, 140));
        JButton btnTodos   = criarBotao("↺ Todos",    new Color(100, 90, 120));
        JPanel painelBtnFiltro = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        painelBtnFiltro.setOpaque(false);
        painelBtnFiltro.add(btnFiltrar);
        painelBtnFiltro.add(btnTodos);
        painelFiltro.add(criarLabel("Filtrar:"), BorderLayout.WEST);
        painelFiltro.add(campFiltro, BorderLayout.CENTER);
        painelFiltro.add(painelBtnFiltro, BorderLayout.EAST);

        JScrollPane scroll = new JScrollPane(criarTabela(
                new String[]{"ID", "Nome", "Telefone", "E-mail", "Cadastro"}));
        scroll.getViewport().setBackground(COR_CARD);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(50, 42, 75), 1));

        painelDireito.add(painelFiltro, BorderLayout.NORTH);
        painelDireito.add(scroll, BorderLayout.CENTER);

        // Layout central
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, card, painelDireito);
        split.setDividerLocation(320);
        split.setOpaque(false);
        split.setBorder(null);
        split.setBackground(COR_FUNDO);
        add(split, BorderLayout.CENTER);

        // Ações
        btnSalvar.addActionListener(e -> salvar());
        btnAtualizar.addActionListener(e -> atualizar());
        btnExcluir.addActionListener(e -> excluir());
        btnLimpar.addActionListener(e -> limpar());
        btnFiltrar.addActionListener(e -> carregar(campFiltro.getText().trim()));
        btnTodos.addActionListener(e -> { campFiltro.setText(""); carregar(""); });

        tabela.getSelectionModel().addListSelectionListener(e -> preencherFormulario());

        carregar("");
    }

    private void adicionarLinhaCampo(JPanel card, GridBagConstraints gbc, int row, String label, JTextField campo) {
        gbc.gridwidth = 1;
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.3;
        card.add(criarLabel(label), gbc);
        gbc.gridx = 1; gbc.weightx = 0.7;
        card.add(campo, gbc);
    }

    private void carregar(String filtro) {
        modeloTabela.setRowCount(0);
        String sql = "SELECT id, nome, telefone, email, data_cadastro FROM clientes " +
                (filtro.isEmpty() ? "" : "WHERE LOWER(nome) LIKE LOWER(?) ") +
                "ORDER BY nome";
        try (Connection conn = Conexao.conectar();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            if (!filtro.isEmpty()) ps.setString(1, "%" + filtro + "%");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                modeloTabela.addRow(new Object[]{
                        rs.getInt("id"), rs.getString("nome"), rs.getString("telefone"),
                        rs.getString("email"), rs.getDate("data_cadastro")
                });
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Erro: " + ex.getMessage());
        }
    }

    private void salvar() {
        String nome = campNome.getText().trim();
        if (nome.isEmpty()) { JOptionPane.showMessageDialog(this, "Nome é obrigatório!"); return; }
        String sql = "INSERT INTO clientes (nome, telefone, email) VALUES (?, ?, ?)";
        try (Connection conn = Conexao.conectar(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nome);
            ps.setString(2, campTelefone.getText().trim());
            ps.setString(3, campEmail.getText().trim());
            ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "Cliente cadastrado com sucesso!");
            limpar(); carregar("");
        } catch (SQLException ex) { JOptionPane.showMessageDialog(this, "Erro: " + ex.getMessage()); }
    }

    private void atualizar() {
        if (idSelecionado < 0) { JOptionPane.showMessageDialog(this, "Selecione um cliente na tabela!"); return; }
        String nome = campNome.getText().trim();
        if (nome.isEmpty()) { JOptionPane.showMessageDialog(this, "Nome é obrigatório!"); return; }
        String sql = "UPDATE clientes SET nome=?, telefone=?, email=? WHERE id=?";
        try (Connection conn = Conexao.conectar(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nome); ps.setString(2, campTelefone.getText().trim());
            ps.setString(3, campEmail.getText().trim()); ps.setInt(4, idSelecionado);
            ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "Cliente atualizado!");
            limpar(); carregar("");
        } catch (SQLException ex) { JOptionPane.showMessageDialog(this, "Erro: " + ex.getMessage()); }
    }

    private void excluir() {
        if (idSelecionado < 0) { JOptionPane.showMessageDialog(this, "Selecione um cliente!"); return; }
        int confirm = JOptionPane.showConfirmDialog(this, "Excluir este cliente?", "Confirmar", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;
        try (Connection conn = Conexao.conectar();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM clientes WHERE id=?")) {
            ps.setInt(1, idSelecionado); ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "Cliente excluído!");
            limpar(); carregar("");
        } catch (SQLException ex) { JOptionPane.showMessageDialog(this, "Erro: " + ex.getMessage()); }
    }

    private void preencherFormulario() {
        int row = tabela.getSelectedRow();
        if (row < 0) return;
        idSelecionado = (int) modeloTabela.getValueAt(row, 0);
        campNome.setText((String) modeloTabela.getValueAt(row, 1));
        campTelefone.setText(modeloTabela.getValueAt(row, 2) == null ? "" : (String) modeloTabela.getValueAt(row, 2));
        campEmail.setText(modeloTabela.getValueAt(row, 3) == null ? "" : (String) modeloTabela.getValueAt(row, 3));
    }

    private void limpar() {
        idSelecionado = -1;
        campNome.setText(""); campTelefone.setText(""); campEmail.setText("");
        tabela.clearSelection();
    }
}
