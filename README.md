# ✂️ 7 Barbershop

> Sistema de Gestão — 7 Barbershop com interface gráfica (Java Swing) integrada ao PostgreSQL.

---

## 📋 Sobre o Projeto

Sistema desenvolvido para gerenciar o funcionamento de uma barbearia, permitindo o cadastro e controle de **clientes**, **barbeiros** e **agendamentos** de serviços. Possui tela de login, operações completas de CRUD, consultas com INNER JOIN e LEFT JOIN, relatórios de faturamento e um **dashboard interativo de KPIs** com gráficos em tempo real.

Desenvolvido como trabalho da disciplina de **Banco de Dados** — UNIFSA (2026/1).

---

## 🗂️ Estrutura do Repositório

```
barbearia/
├── src/
│   └── barbearia/
│       ├── Conexao.java          # Configuração da conexão com o BD
│       ├── TelaLogin.java        # Tela de autenticação
│       ├── TelaPrincipal.java    # Menu principal com navegação lateral
│       ├── PainelBase.java       # Classe base com estilos reutilizáveis
│       ├── TelaClientes.java     # CRUD de Clientes
│       ├── TelaBarbeiros.java    # CRUD de Barbeiros
│       ├── TelaAgendamentos.java # CRUD de Agendamentos (com INNER JOIN)
│       ├── TelaRelatorios.java   # Relatórios com LEFT JOIN
│       └── TelaDashboard.java    # Dashboard de KPIs com gráficos
└── README.md
```

---

## 🛠️ Tecnologias Utilizadas

| Tecnologia    | Versão     | Uso                            |
|---------------|------------|--------------------------------|
| Java          | 17+        | Linguagem principal             |
| Java Swing    | (JDK)      | Interface gráfica               |
| PostgreSQL    | 15+        | Banco de dados relacional       |
| JDBC          | 42.x       | Conectividade Java ↔ BD         |
| JFreeChart    | 1.0.19     | Gráficos do dashboard de KPIs   |
| JCommon       | 1.0.23     | Dependência do JFreeChart       |

---

## 🗄️ Modelagem do Banco de Dados

### Tabelas

| Tabela         | Descrição                                           |
|----------------|-----------------------------------------------------|
| `usuarios`     | Usuários do sistema (login)                         |
| `clientes`     | Clientes da barbearia                               |
| `barbeiros`    | Barbeiros cadastrados                               |
| `agendamentos` | Agendamentos de serviços (FK → clientes, barbeiros) |

### Relacionamentos
- `agendamentos.cliente_id` → `clientes.id` (FK)
- `agendamentos.barbeiro_id` → `barbeiros.id` (FK)

---

## ⚙️ Como Executar

### 1. Pré-requisitos
- Java JDK 17 ou superior instalado
- PostgreSQL instalado e rodando
- Driver JDBC do PostgreSQL (`postgresql-42.x.x.jar`) no classpath
- JFreeChart (`jfreechart-1.0.19.jar` e `jcommon-1.0.23.jar`) no classpath

### 2. Configurar o Banco de Dados

```sql
-- No psql ou PgAdmin, crie o banco:
CREATE DATABASE 7barbershop_db;

-- Depois execute os scripts na ordem:
-- 1. ddl/create_tables.sql
-- 2. dml/dados_exemplo.sql
```

### 3. Configurar a Conexão

Edite o arquivo `src/barbearia/Conexao.java` com suas credenciais:

```java
private static final String URL     = "jdbc:postgresql://localhost:5432/7barbershop_db";
private static final String USUARIO = "postgres";   // seu usuário
private static final String SENHA   = "postgres";   // sua senha
```

### 4. Compilar e Executar

```bash
# Compile (inclua os JARs no classpath)
javac -cp ".;postgresql-42.7.3.jar;jfreechart-1.0.19.jar;jcommon-1.0.23.jar" src/barbearia/*.java -d out/

# Execute
java -cp ".;postgresql-42.7.3.jar;jfreechart-1.0.19.jar;jcommon-1.0.23.jar;out/" barbearia.TelaLogin
```

> No Linux/Mac substitua `;` por `:` no classpath.

### 5. Login Padrão

| Usuário | Senha     |
|---------|-----------|
| admin   | admin123  |

---

## 🔍 Consultas SQL Utilizadas

### INNER JOIN — Agendamentos com Cliente e Barbeiro
```sql
SELECT a.id, c.nome AS cliente, b.nome AS barbeiro,
       a.servico, a.valor, a.data_hora, a.status
FROM agendamentos a
INNER JOIN clientes  c ON c.id = a.cliente_id
INNER JOIN barbeiros b ON b.id = a.barbeiro_id
ORDER BY a.data_hora DESC;
```

### LEFT JOIN — Faturamento por Barbeiro (inclui sem agendamentos)
```sql
SELECT b.nome, b.especialidade,
       COUNT(a.id) AS total, COALESCE(SUM(a.valor), 0) AS faturamento
FROM barbeiros b
LEFT JOIN agendamentos a ON a.barbeiro_id = b.id AND a.status = 'Concluído'
GROUP BY b.id, b.nome, b.especialidade
ORDER BY faturamento DESC;
```

### LEFT JOIN — Total de Agendamentos por Cliente
```sql
SELECT c.nome, c.telefone,
       COUNT(a.id) AS total, COALESCE(SUM(a.valor), 0) AS gasto
FROM clientes c
LEFT JOIN agendamentos a ON a.cliente_id = c.id
GROUP BY c.id, c.nome, c.telefone
ORDER BY total DESC;
```

### Agrupamento — Agendamentos por Mês
```sql
SELECT TO_CHAR(data_hora, 'MM/YYYY') AS mes, COUNT(*) AS qtd
FROM agendamentos
GROUP BY TO_CHAR(data_hora, 'MM/YYYY'), DATE_TRUNC('month', data_hora)
ORDER BY DATE_TRUNC('month', data_hora);
```

---

## 📊 Dashboard de KPIs

O sistema conta com um dashboard interativo acessível pelo menu lateral, exibindo:

| Indicador           | Descrição                                      |
|---------------------|------------------------------------------------|
| Total Agendamentos  | Contagem total de agendamentos (COUNT)         |
| Faturamento Total   | Soma de todos os valores (SUM)                 |
| Ticket Médio        | Valor médio por agendamento (AVG)              |
| Barbeiro Destaque   | Barbeiro com maior faturamento                 |

### Gráficos
- 📊 **Barras** — Faturamento por barbeiro
- 🥧 **Pizza** — Distribuição de serviços por tipo
- 📈 **Linha** — Evolução de agendamentos por mês

Todos os gráficos possuem **filtro dinâmico por status** (Todos / Agendado / Concluído / Cancelado) e são atualizados em tempo real direto do banco de dados.

---

📧 Disciplina: Banco de Dados | Prof. Anderson Costa