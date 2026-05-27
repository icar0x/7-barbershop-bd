package Barbearia;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.StandardBarPainter;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;

/**
 * Tela de Dashboard com KPIs e gráficos.
 * <p>
 * Cobre todos os requisitos do trabalho avaliativo:
 * <ul>
 *   <li>SELECT com WHERE, COUNT, SUM, AVG, MIN, MAX, GROUP BY, ORDER BY ASC e DESC</li>
 *   <li>5 KPIs (cards) e 3 tipos de gráficos (barras, linha, pizza)</li>
 *   <li>Filtro dinâmico de período</li>
 *   <li>Conexão direta com SQL via PreparedStatement</li>
 * </ul>
 *
 * Dependência externa: jfreechart-1.5.4.jar (e jcommon-1.0.24.jar).
 */
public class TelaDashboard extends JPanel {

    // -------------------- ESTILO (tema dark dourado do sistema) --------------------
    private static final Color COR_FUNDO   = new Color(15, 15, 25);
    private static final Color COR_CARD    = new Color(22, 18, 38);
    private static final Color COR_TITULO  = new Color(180, 180, 180);
    private static final Color COR_VALOR   = new Color(212, 175, 55);
    private static final Color COR_ACENTO  = new Color(212, 175, 55);

    private static final Font FONTE_CARD_TITULO = new Font("SansSerif", Font.PLAIN, 13);
    private static final Font FONTE_CARD_VALOR  = new Font("SansSerif", Font.BOLD, 22);
    private static final Font FONTE_SECAO       = new Font("SansSerif", Font.BOLD, 14);

    // -------------------- FORMATAÇÃO --------------------
    private static final NumberFormat MOEDA =
            NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));
    private static final DateTimeFormatter FMT_DIA = DateTimeFormatter.ofPattern("dd/MM");

    // -------------------- COMPONENTES --------------------
    private JComboBox<String> comboPeriodo;
    private JLabel lblFaturamento;
    private JLabel lblTicketMedio;
    private JLabel lblTotalAtendimentos;
    private JLabel lblTaxaCancelamento;
    private JLabel lblMinMax;

    // wrappers que recebem o ChartPanel novo a cada atualização
    private JPanel wrapperBarras;
    private JPanel wrapperLinha;
    private JPanel wrapperPizza;

    // -------------------- CONSTRUTOR --------------------
    public TelaDashboard() {
        setLayout(new BorderLayout(0, 10));
        setBackground(COR_FUNDO);
        setBorder(new EmptyBorder(20, 20, 20, 20));

        add(criarTopo(),  BorderLayout.NORTH);
        add(criarCorpo(), BorderLayout.CENTER);

        // Carrega dados na inicialização
        atualizarDashboard();
    }

    // =====================================================================
    // LAYOUT - TOPO (título + filtros)
    // =====================================================================
    private JPanel criarTopo() {
        JPanel topo = new JPanel(new BorderLayout());
        topo.setBackground(COR_FUNDO);

        JLabel titulo = new JLabel("Dashboard de Indicadores");
        titulo.setFont(new Font("SansSerif", Font.BOLD, 22));
        titulo.setForeground(COR_VALOR);

        JPanel filtros = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        filtros.setBackground(COR_FUNDO);

        JLabel lblPeriodo = new JLabel("Período:");
        lblPeriodo.setFont(FONTE_SECAO);
        lblPeriodo.setForeground(COR_TITULO);

        comboPeriodo = new JComboBox<>(new String[] {
                "Últimos 7 dias",
                "Últimos 30 dias",
                "Este mês",
                "Mês passado",
                "Últimos 90 dias",
                "Todo o período"
        });
        comboPeriodo.setSelectedItem("Últimos 30 dias");
        comboPeriodo.setPreferredSize(new Dimension(160, 28));

        JButton btnAtualizar = new JButton("Atualizar");
        btnAtualizar.setBackground(COR_ACENTO);
        btnAtualizar.setForeground(Color.WHITE);
        btnAtualizar.setFocusPainted(false);
        btnAtualizar.setBorder(BorderFactory.createEmptyBorder(6, 14, 6, 14));
        btnAtualizar.addActionListener(e -> atualizarDashboard());

        filtros.add(lblPeriodo);
        filtros.add(comboPeriodo);
        filtros.add(btnAtualizar);

        topo.add(titulo,  BorderLayout.WEST);
        topo.add(filtros, BorderLayout.EAST);

        return topo;
    }

    // =====================================================================
    // LAYOUT - CORPO (KPI cards no topo + grade de gráficos)
    // =====================================================================
    private JPanel criarCorpo() {
        JPanel corpo = new JPanel(new BorderLayout(0, 15));
        corpo.setBackground(COR_FUNDO);

        // --- KPI cards (5 cards em linha) ---
        lblFaturamento       = new JLabel("R$ 0,00",  SwingConstants.CENTER);
        lblTicketMedio       = new JLabel("R$ 0,00",  SwingConstants.CENTER);
        lblTotalAtendimentos = new JLabel("0",        SwingConstants.CENTER);
        lblTaxaCancelamento  = new JLabel("0,00 %",   SwingConstants.CENTER);
        lblMinMax            = new JLabel("R$ 0 / R$ 0", SwingConstants.CENTER);

        JPanel kpis = new JPanel(new GridLayout(1, 5, 12, 0));
        kpis.setBackground(COR_FUNDO);
        kpis.add(criarKpiCard("Faturamento (período)",      lblFaturamento));
        kpis.add(criarKpiCard("Ticket médio",               lblTicketMedio));
        kpis.add(criarKpiCard("Total de atendimentos",      lblTotalAtendimentos));
        kpis.add(criarKpiCard("Taxa de cancelamento",       lblTaxaCancelamento));
        kpis.add(criarKpiCard("Maior atendimento",  lblMinMax));

        // --- Wrappers de gráficos ---
        wrapperBarras = criarWrapperGrafico();
        wrapperLinha  = criarWrapperGrafico();
        wrapperPizza  = criarWrapperGrafico();

        // layout: linha de cima ocupa largura toda (barras)
        // linha de baixo: 2 colunas (linha temporal + pizza)
        JPanel rowInferior = new JPanel(new GridLayout(1, 2, 12, 0));
        rowInferior.setBackground(COR_FUNDO);
        rowInferior.add(wrapperLinha);
        rowInferior.add(wrapperPizza);

        JPanel graficos = new JPanel(new GridLayout(2, 1, 0, 12));
        graficos.setBackground(COR_FUNDO);
        graficos.add(wrapperBarras);
        graficos.add(rowInferior);

        corpo.add(kpis,     BorderLayout.NORTH);
        corpo.add(graficos, BorderLayout.CENTER);

        return corpo;
    }

    private JPanel criarKpiCard(String titulo, JLabel valor) {
        JPanel card = new JPanel(new BorderLayout(0, 8));
        card.setBackground(COR_CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(50, 40, 80), 1),
                new EmptyBorder(14, 16, 14, 16)));

        JLabel tit = new JLabel(titulo);
        tit.setFont(FONTE_CARD_TITULO);
        tit.setForeground(COR_TITULO);

        valor.setFont(FONTE_CARD_VALOR);
        valor.setForeground(COR_VALOR);
        valor.setHorizontalAlignment(SwingConstants.LEFT);

        card.add(tit,   BorderLayout.NORTH);
        card.add(valor, BorderLayout.CENTER);
        return card;
    }

    private JPanel criarWrapperGrafico() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(COR_CARD);
        p.setBorder(BorderFactory.createLineBorder(new Color(50, 40, 80), 1));
        return p;
    }

    // =====================================================================
    // ATUALIZAÇÃO - dispara todas as queries baseado no período escolhido
    // =====================================================================
    private void atualizarDashboard() {
        Timestamp[] periodo = obterPeriodoSelecionado();
        Timestamp inicio = periodo[0];
        Timestamp fim    = periodo[1];

        try (Connection con = Conexao.conectar()) {
            if (con == null) {
                JOptionPane.showMessageDialog(this,
                        "Não foi possível conectar ao banco de dados.",
                        "Erro de conexão",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
            carregarKpis(con, inicio, fim);
            carregarGraficoBarras(con, inicio, fim);
            carregarGraficoLinha(con, inicio, fim);
            carregarGraficoPizza(con, inicio, fim);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                    "Erro ao carregar dados: " + ex.getMessage(),
                    "Erro SQL",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    /** Converte a opção do JComboBox em um par (data_inicio, data_fim). */
    private Timestamp[] obterPeriodoSelecionado() {
        String opcao = (String) comboPeriodo.getSelectedItem();
        LocalDate hoje = LocalDate.now();
        LocalDateTime inicio;
        LocalDateTime fim = LocalDateTime.of(hoje, LocalTime.MAX);

        switch (opcao) {
            case "Últimos 7 dias":
                inicio = LocalDateTime.of(hoje.minusDays(7), LocalTime.MIN);
                break;
            case "Este mês":
                inicio = LocalDateTime.of(hoje.withDayOfMonth(1), LocalTime.MIN);
                break;
            case "Mês passado":
                YearMonth mp = YearMonth.from(hoje).minusMonths(1);
                inicio = LocalDateTime.of(mp.atDay(1),       LocalTime.MIN);
                fim    = LocalDateTime.of(mp.atEndOfMonth(), LocalTime.MAX);
                break;
            case "Últimos 90 dias":
                inicio = LocalDateTime.of(hoje.minusDays(90), LocalTime.MIN);
                break;
            case "Todo o período":
                inicio = LocalDateTime.of(1900, 1, 1, 0, 0);
                fim    = LocalDateTime.of(9999, 12, 31, 23, 59);
                break;
            case "Últimos 30 dias":
            default:
                inicio = LocalDateTime.of(hoje.minusDays(30), LocalTime.MIN);
                break;
        }
        return new Timestamp[] { Timestamp.valueOf(inicio), Timestamp.valueOf(fim) };
    }

    // =====================================================================
    // KPIs - executa as 5 queries de cards
    // =====================================================================
    private void carregarKpis(Connection con, Timestamp inicio, Timestamp fim) throws SQLException {

        // KPI 1: SUM - Faturamento
        String sqlFat = "SELECT COALESCE(SUM(valor), 0) " +
                        "FROM agendamentos " +
                        "WHERE status = 'Concluído' " +
                        "  AND data_hora BETWEEN ? AND ?";
        lblFaturamento.setText(MOEDA.format(consultarNumero(con, sqlFat, inicio, fim)));

        // KPI 2: AVG - Ticket médio
        String sqlAvg = "SELECT COALESCE(AVG(valor), 0) " +
                        "FROM agendamentos " +
                        "WHERE status = 'Concluído' " +
                        "  AND data_hora BETWEEN ? AND ?";
        lblTicketMedio.setText(MOEDA.format(consultarNumero(con, sqlAvg, inicio, fim)));

        // KPI 3: COUNT - Total de atendimentos
        String sqlCount = "SELECT COUNT(*) " +
                          "FROM agendamentos " +
                          "WHERE data_hora BETWEEN ? AND ?";
        lblTotalAtendimentos.setText(String.valueOf((long) consultarNumero(con, sqlCount, inicio, fim)));

        // KPI 4: Taxa de cancelamento (CASE WHEN + NULLIF)
        String sqlTaxa = "SELECT ROUND(100.0 * SUM(CASE WHEN status = 'Cancelado' THEN 1 ELSE 0 END) " +
                         "       / NULLIF(COUNT(*), 0), 2) " +
                         "FROM agendamentos " +
                         "WHERE data_hora BETWEEN ? AND ?";
        double taxa = consultarNumero(con, sqlTaxa, inicio, fim);
        lblTaxaCancelamento.setText(String.format(new Locale("pt", "BR"), "%.2f %%", taxa));

        // KPI 5: MIN e MAX juntos
        String sqlMinMax = "SELECT COALESCE(MIN(valor), 0), COALESCE(MAX(valor), 0) " +
                           "FROM agendamentos " +
                           "WHERE status = 'Concluído' " +
                           "  AND data_hora BETWEEN ? AND ?";
        try (PreparedStatement ps = con.prepareStatement(sqlMinMax)) {
            ps.setTimestamp(1, inicio);
            ps.setTimestamp(2, fim);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    lblMinMax.setText(MOEDA.format(rs.getDouble(2)));
                }
            }
        }
    }

    /** Helper para queries que retornam um único valor numérico. */
    private double consultarNumero(Connection con, String sql, Timestamp inicio, Timestamp fim)
            throws SQLException {
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setTimestamp(1, inicio);
            ps.setTimestamp(2, fim);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble(1);
                }
            }
        }
        return 0;
    }

    // =====================================================================
    // GRÁFICO 1 (BARRAS) - Faturamento por barbeiro
    // =====================================================================
    private void carregarGraficoBarras(Connection con, Timestamp inicio, Timestamp fim)
            throws SQLException {

        String sql =
                "SELECT b.nome, COALESCE(SUM(a.valor), 0) AS faturamento " +
                "FROM barbeiros b " +
                "LEFT JOIN agendamentos a ON a.barbeiro_id = b.id " +
                "                       AND a.status = 'Concluído' " +
                "                       AND a.data_hora BETWEEN ? AND ? " +
                "GROUP BY b.id, b.nome " +
                "ORDER BY faturamento DESC";

        DefaultCategoryDataset ds = new DefaultCategoryDataset();
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setTimestamp(1, inicio);
            ps.setTimestamp(2, fim);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ds.addValue(rs.getDouble("faturamento"),
                                "Faturamento", rs.getString("nome"));
                }
            }
        }

        JFreeChart chart = ChartFactory.createBarChart(
                "Faturamento por Barbeiro",
                "Barbeiro", "R$",
                ds, PlotOrientation.VERTICAL,
                false, true, false);

        estilizarBarras(chart);
        atualizarWrapper(wrapperBarras, chart);
    }

    private void estilizarBarras(JFreeChart chart) {
    chart.setBackgroundPaint(COR_CARD);
    chart.getTitle().setPaint(COR_VALOR);

    CategoryPlot plot = chart.getCategoryPlot();
    plot.setBackgroundPaint(COR_CARD);
    plot.setOutlinePaint(null);
    plot.setRangeGridlinePaint(new Color(50, 40, 80));
    plot.getDomainAxis().setLabelPaint(COR_TITULO);
    plot.getDomainAxis().setTickLabelPaint(COR_TITULO);
    plot.getRangeAxis().setLabelPaint(COR_TITULO);
    plot.getRangeAxis().setTickLabelPaint(COR_TITULO);

    BarRenderer renderer = (BarRenderer) plot.getRenderer();
    renderer.setBarPainter(new StandardBarPainter());
    renderer.setShadowVisible(false);
    renderer.setSeriesPaint(0, COR_ACENTO);
    }

    // =====================================================================
    // GRÁFICO 2 (LINHA) - Evolução diária do faturamento
    // =====================================================================
    private void carregarGraficoLinha(Connection con, Timestamp inicio, Timestamp fim)
            throws SQLException {

        String sql =
                "SELECT DATE(data_hora) AS dia, COALESCE(SUM(valor), 0) AS faturamento_dia " +
                "FROM agendamentos " +
                "WHERE status = 'Concluído' " +
                "  AND data_hora BETWEEN ? AND ? " +
                "GROUP BY DATE(data_hora) " +
                "ORDER BY dia ASC";

        DefaultCategoryDataset ds = new DefaultCategoryDataset();
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setTimestamp(1, inicio);
            ps.setTimestamp(2, fim);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    LocalDate d = rs.getDate("dia").toLocalDate();
                    ds.addValue(rs.getDouble("faturamento_dia"),
                                "Faturamento", d.format(FMT_DIA));
                }
            }
        }

        JFreeChart chart = ChartFactory.createLineChart(
                "Evolução Diária do Faturamento",
                "Dia", "R$",
                ds, PlotOrientation.VERTICAL,
                false, true, false);

        chart.setBackgroundPaint(COR_CARD);
        chart.getTitle().setPaint(COR_VALOR);

        CategoryPlot plot = chart.getCategoryPlot();
        plot.setBackgroundPaint(COR_CARD);
        plot.setOutlinePaint(null);
        plot.setRangeGridlinePaint(new Color(50, 40, 80));
        plot.getDomainAxis().setLabelPaint(COR_TITULO);
        plot.getDomainAxis().setTickLabelPaint(COR_TITULO);
        plot.getRangeAxis().setLabelPaint(COR_TITULO);
        plot.getRangeAxis().setTickLabelPaint(COR_TITULO);
        plot.getRenderer().setSeriesPaint(0, COR_ACENTO);

        atualizarWrapper(wrapperLinha, chart);
    }

    // =====================================================================
    // GRÁFICO 3 (PIZZA) - Distribuição de serviços
    // =====================================================================
    private void carregarGraficoPizza(Connection con, Timestamp inicio, Timestamp fim)
            throws SQLException {

        String sql =
                "SELECT servico, COUNT(*) AS qtd " +
                "FROM agendamentos " +
                "WHERE status = 'Concluído' " +
                "  AND data_hora BETWEEN ? AND ? " +
                "GROUP BY servico " +
                "ORDER BY qtd DESC";

        @SuppressWarnings({ "rawtypes", "unchecked" })
        DefaultPieDataset ds = new DefaultPieDataset();

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setTimestamp(1, inicio);
            ps.setTimestamp(2, fim);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ds.setValue(rs.getString("servico"), rs.getInt("qtd"));
                }
            }
        }

        JFreeChart chart = ChartFactory.createPieChart(
                "Distribuição de Serviços",
                ds, true, true, false);

        chart.setBackgroundPaint(COR_CARD);
        chart.getTitle().setPaint(COR_VALOR);
        chart.getLegend().setBackgroundPaint(COR_CARD);
        chart.getLegend().setItemPaint(COR_TITULO);

        PiePlot plot = (PiePlot) chart.getPlot();
        plot.setBackgroundPaint(COR_CARD);
        plot.setOutlinePaint(null);
        plot.setLabelFont(new Font("SansSerif", Font.PLAIN, 11));
        plot.setLabelBackgroundPaint(COR_CARD);
        plot.setLabelPaint(COR_TITULO);

        atualizarWrapper(wrapperPizza, chart);
    }

    // =====================================================================
    // HELPER - troca o ChartPanel dentro do wrapper
    // =====================================================================
    private void atualizarWrapper(JPanel wrapper, JFreeChart chart) {
        wrapper.removeAll();
        ChartPanel cp = new ChartPanel(chart);
        cp.setBackground(COR_CARD);
        cp.setMouseWheelEnabled(false);
        wrapper.add(cp, BorderLayout.CENTER);
        wrapper.revalidate();
        wrapper.repaint();
    }
}
