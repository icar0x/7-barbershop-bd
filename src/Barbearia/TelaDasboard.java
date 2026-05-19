package Barbearia;

import org.jfree.chart.*;
import org.jfree.chart.plot.*;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.category.CategoryDataset;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.*;

class TelaDashboard extends PainelBase {

    // ── KPI labels ──────────────────────────────────────────────
    private JLabel lblTotalAgendamentos  = criarValorKpi("–");
    private JLabel lblFaturamentoTotal   = criarValorKpi("–");
    private JLabel lblTicketMedio        = criarValorKpi("–");
    private JLabel lblBarbeiroDestaque   = criarValorKpi("–");

    // ── Chart panels ────────────────────────────────────────────
    private JPanel areaBarras  = new JPanel(new BorderLayout());
    private JPanel areaPizza   = new JPanel(new BorderLayout());
    private JPanel areaLinha   = new JPanel(new BorderLayout());

    // ── Filter ──────────────────────────────────────────────────
    private JComboBox<String> combFiltro;

    public TelaDashboard() {
        add(criarTitulo("📊  Dashboard de KPIs"), BorderLayout.NORTH);

        JPanel corpo = new JPanel(new BorderLayout(0, 20));
        corpo.setOpaque(false);

        // Filtro de status
        JPanel painelFiltro = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        painelFiltro.setOpaque(false);
        combFiltro = new JComboBox<>(new String[]{"Todos", "Agendado", "Concluído", "Cancelado"});
        estilizarCombo(combFiltro);
        combFiltro.setPreferredSize(new Dimension(180, 34));
        JButton btnAtualizar = criarBotao("🔄 Atualizar", COR_DOURADO);
        painelFiltro.add(criarLabel("Filtrar por status: "));
        painelFiltro.add(combFiltro);
        painelFiltro.add(btnAtualizar);
        corpo.add(painelFiltro, BorderLayout.NORTH);

        // KPIs
        JPanel painelKpis = new JPanel(new GridLayout(1, 4, 16, 0));
        painelKpis.setOpaque(false);
        painelKpis.add(criarCardKpi("📅 Total Agendamentos", lblTotalAgendamentos, new Color(80, 160, 230)));
        painelKpis.add(criarCardKpi("💰 Faturamento Total",  lblFaturamentoTotal,  new Color(80, 200, 140)));
        painelKpis.add(criarCardKpi("🧾 Ticket Médio",       lblTicketMedio,       new Color(200, 130, 80)));
        painelKpis.add(criarCardKpi("🏆 Barbeiro Destaque",  lblBarbeiroDestaque,  new Color(212, 175, 55)));

        // Gráficos
        JPanel painelGraficos = new JPanel(new GridLayout(1, 3, 16, 0));
        painelGraficos.setOpaque(false);

        configurarAreaGrafico(areaBarras, "Faturamento por Barbeiro");
        configurarAreaGrafico(areaPizza,  "Serviços por Tipo");
        configurarAreaGrafico(areaLinha,  "Agendamentos por Mês");

        painelGraficos.add(areaBarras);
        painelGraficos.add(areaPizza);
        painelGraficos.add(areaLinha);

        JPanel centro = new JPanel(new BorderLayout(0, 20));
        centro.setOpaque(false);
        centro.add(painelKpis,    BorderLayout.NORTH);
        centro.add(painelGraficos, BorderLayout.CENTER);

        corpo.add(centro, BorderLayout.CENTER);
        add(corpo, BorderLayout.CENTER);

        btnAtualizar.addActionListener(e -> carregarTudo());
        carregarTudo();
    }

    // ── Carrega tudo ────────────────────────────────────────────
    private void carregarTudo() {
        String status = (String) combFiltro.getSelectedItem();
        String filtro = "Todos".equals(status) ? "" : status;
        carregarKpis(filtro);
        carregarGraficoBarras(filtro);
        carregarGraficoPizza(filtro);
        carregarGraficoLinha(filtro);
    }

    // ── KPIs ────────────────────────────────────────────────────
    private void carregarKpis(String filtro) {
        String where = filtro.isEmpty() ? "" : "WHERE status = '" + filtro + "'";
        String sql = "SELECT COUNT(*) AS total, " +
                "COALESCE(SUM(valor), 0) AS faturamento, " +
                "COALESCE(AVG(valor), 0) AS ticket " +
                "FROM agendamentos " + where;

        String sqlDestaque = "SELECT b.nome FROM barbeiros b " +
                "INNER JOIN agendamentos a ON a.barbeiro_id = b.id " +
                (filtro.isEmpty() ? "" : "WHERE a.status = '" + filtro + "' ") +
                "GROUP BY b.id, b.nome ORDER BY SUM(a.valor) DESC LIMIT 1";

        try (Connection conn = Conexao.conectar()) {
            ResultSet rs = conn.createStatement().executeQuery(sql);
            if (rs.next()) {
                lblTotalAgendamentos.setText(String.valueOf(rs.getInt("total")));
                lblFaturamentoTotal.setText("R$ " + String.format("%.2f", rs.getDouble("faturamento")));
                lblTicketMedio.setText("R$ " + String.format("%.2f", rs.getDouble("ticket")));
            }
            ResultSet rs2 = conn.createStatement().executeQuery(sqlDestaque);
            lblBarbeiroDestaque.setText(rs2.next() ? rs2.getString("nome") : "–");
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Erro KPIs: " + ex.getMessage());
        }
    }

    // ── Gráfico de Barras — Faturamento por Barbeiro ─────────────
    private void carregarGraficoBarras(String filtro) {
        String sql = "SELECT b.nome, COALESCE(SUM(a.valor), 0) AS faturamento " +
                "FROM barbeiros b LEFT JOIN agendamentos a ON a.barbeiro_id = b.id " +
                (filtro.isEmpty() ? "" : "AND a.status = '" + filtro + "' ") +
                "GROUP BY b.id, b.nome ORDER BY faturamento DESC";

        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        try (Connection conn = Conexao.conectar();
             ResultSet rs = conn.createStatement().executeQuery(sql)) {
            while (rs.next()) {
                dataset.addValue(rs.getDouble("faturamento"), "Faturamento", rs.getString("nome"));
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Erro gráfico barras: " + ex.getMessage());
        }

        JFreeChart chart = ChartFactory.createBarChart(
                null, "Barbeiro", "R$", dataset,
                PlotOrientation.VERTICAL, false, true, false);
        estilizarChart(chart);

        CategoryPlot plot = chart.getCategoryPlot();
        plot.setBackgroundPaint(new Color(22, 18, 38));
        plot.setRangeGridlinePaint(new Color(50, 42, 75));
        plot.getDomainAxis().setTickLabelFont(new Font("Verdana", Font.PLAIN, 10));
        plot.getDomainAxis().setTickLabelPaint(new Color(180, 170, 200));
        plot.getRangeAxis().setTickLabelPaint(new Color(180, 170, 200));

        BarRenderer renderer = (BarRenderer) plot.getRenderer();
        renderer.setSeriesPaint(0, new Color(212, 175, 55));
        renderer.setShadowVisible(false);
        renderer.setBarPainter(new org.jfree.chart.renderer.category.StandardBarPainter());

        substituirGrafico(areaBarras, new ChartPanel(chart));
    }

    // ── Gráfico de Pizza — Serviços por Tipo ─────────────────────
    private void carregarGraficoPizza(String filtro) {
        String sql = "SELECT servico, COUNT(*) AS qtd FROM agendamentos " +
                (filtro.isEmpty() ? "" : "WHERE status = '" + filtro + "' ") +
                "GROUP BY servico ORDER BY qtd DESC LIMIT 6";

        DefaultPieDataset dataset = new DefaultPieDataset();
        try (Connection conn = Conexao.conectar();
             ResultSet rs = conn.createStatement().executeQuery(sql)) {
            while (rs.next()) {
                dataset.setValue(rs.getString("servico"), rs.getInt("qtd"));
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Erro gráfico pizza: " + ex.getMessage());
        }

        JFreeChart chart = ChartFactory.createPieChart(null, dataset, true, true, false);
        estilizarChart(chart);

        PiePlot plot = (PiePlot) chart.getPlot();
        plot.setBackgroundPaint(new Color(22, 18, 38));
        plot.setOutlineVisible(false);
        plot.setLabelFont(new Font("Verdana", Font.PLAIN, 10));
        plot.setLabelPaint(Color.WHITE);
        plot.setLabelBackgroundPaint(new Color(40, 35, 60));
        plot.setLabelOutlinePaint(null);
        plot.setLabelShadowPaint(null);

        Color[] cores = {
                new Color(212, 175, 55), new Color(80, 160, 230),
                new Color(80, 200, 140), new Color(200, 100, 80),
                new Color(160, 100, 200), new Color(100, 200, 200)
        };
        int i = 0;
        for (Object key : dataset.getKeys()) {
            plot.setSectionPaint((Comparable<?>) key, cores[i % cores.length]);
            i++;
        }

        substituirGrafico(areaPizza, new ChartPanel(chart));
    }

    // ── Gráfico de Linha — Agendamentos por Mês ──────────────────
    private void carregarGraficoLinha(String filtro) {
        String sql = "SELECT TO_CHAR(data_hora, 'MM/YYYY') AS mes, COUNT(*) AS qtd " +
                "FROM agendamentos " +
                (filtro.isEmpty() ? "" : "WHERE status = '" + filtro + "' ") +
                "GROUP BY TO_CHAR(data_hora, 'MM/YYYY'), DATE_TRUNC('month', data_hora) " +
                "ORDER BY DATE_TRUNC('month', data_hora)";

        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        try (Connection conn = Conexao.conectar();
             ResultSet rs = conn.createStatement().executeQuery(sql)) {
            while (rs.next()) {
                dataset.addValue(rs.getInt("qtd"), "Agendamentos", rs.getString("mes"));
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Erro gráfico linha: " + ex.getMessage());
        }

        JFreeChart chart = ChartFactory.createLineChart(
                null, "Mês", "Qtd", dataset,
                PlotOrientation.VERTICAL, false, true, false);
        estilizarChart(chart);

        CategoryPlot plot = chart.getCategoryPlot();
        plot.setBackgroundPaint(new Color(22, 18, 38));
        plot.setRangeGridlinePaint(new Color(50, 42, 75));
        plot.getDomainAxis().setTickLabelFont(new Font("Verdana", Font.PLAIN, 10));
        plot.getDomainAxis().setTickLabelPaint(new Color(180, 170, 200));
        plot.getRangeAxis().setTickLabelPaint(new Color(180, 170, 200));

        org.jfree.chart.renderer.category.LineAndShapeRenderer renderer =
                (org.jfree.chart.renderer.category.LineAndShapeRenderer) plot.getRenderer();
        renderer.setSeriesPaint(0, new Color(212, 175, 55));
        renderer.setSeriesStroke(0, new BasicStroke(2.5f));
        renderer.setSeriesShapesVisible(0, true);

        substituirGrafico(areaLinha, new ChartPanel(chart));
    }

    // ── Helpers visuais ─────────────────────────────────────────
    private void estilizarChart(JFreeChart chart) {
        chart.setBackgroundPaint(new Color(22, 18, 38));
        chart.setBorderVisible(false);
        if (chart.getLegend() != null) {
            chart.getLegend().setBackgroundPaint(new Color(22, 18, 38));
            chart.getLegend().setItemPaint(COR_TEXTO);
        }
    }

    private void substituirGrafico(JPanel area, ChartPanel chartPanel) {
        chartPanel.setBackground(new Color(22, 18, 38));
        chartPanel.setOpaque(true);
        chartPanel.setMouseWheelEnabled(true);
        chartPanel.setDomainZoomable(true);
        chartPanel.setRangeZoomable(true);
        area.removeAll();
        area.add(chartPanel, BorderLayout.CENTER);
        area.revalidate();
        area.repaint();
    }

    private void configurarAreaGrafico(JPanel area, String titulo) {
        area.setBackground(COR_CARD);
        area.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(50, 42, 75), 1),
                new EmptyBorder(10, 10, 10, 10)
        ));
        JLabel lbl = new JLabel(titulo, SwingConstants.CENTER);
        lbl.setFont(new Font("Verdana", Font.BOLD, 12));
        lbl.setForeground(COR_DOURADO);
        lbl.setBorder(new EmptyBorder(0, 0, 8, 0));
        area.add(lbl, BorderLayout.NORTH);
    }

    private JPanel criarCardKpi(String titulo, JLabel valorLabel, Color corDestaque) {
        JPanel card = new JPanel(new BorderLayout(0, 6));
        card.setBackground(COR_CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(corDestaque.darker(), 1),
                new EmptyBorder(16, 16, 16, 16)
        ));

        JLabel lblTitulo = new JLabel(titulo, SwingConstants.CENTER);
        lblTitulo.setFont(new Font("Verdana", Font.BOLD, 11));
        lblTitulo.setForeground(new Color(180, 170, 200));

        valorLabel.setHorizontalAlignment(SwingConstants.CENTER);
        valorLabel.setForeground(corDestaque);

        card.add(lblTitulo,  BorderLayout.NORTH);
        card.add(valorLabel, BorderLayout.CENTER);
        return card;
    }

    private JLabel criarValorKpi(String texto) {
        JLabel lbl = new JLabel(texto, SwingConstants.CENTER);
        lbl.setFont(new Font("Georgia", Font.BOLD, 22));
        return lbl;
    }

    private void estilizarCombo(JComboBox<String> combo) {
        combo.setBackground(COR_CAMPO);
        combo.setForeground(Color.WHITE);
        combo.setFont(new Font("Verdana", Font.PLAIN, 13));
    }
}