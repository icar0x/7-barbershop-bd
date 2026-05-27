# ✂️ 7 Barbershop

> Sistema de Gestão — 7 Barbershop com interface gráfica (Java Swing) integrada ao PostgreSQL.

---

## 📋 Sobre o Projeto

Sistema desenvolvido para gerenciar o funcionamento de uma barbearia, permitindo o cadastro e controle de **clientes**, **barbeiros** e **agendamentos** de serviços. Possui tela de login, operações completas de CRUD, consultas com INNER JOIN e LEFT JOIN, relatórios de faturamento e um **dashboard interativo de KPIs** com gráficos em tempo real e filtro dinâmico por período.

Desenvolvido como trabalho da disciplina de **Banco de Dados** — UNIFSA (2026/1).

---

## 🗂️ Estrutura do Repositório

```
7-barbershop-bd/
├── src/
│   └── Barbearia/
│       ├── Conexao.java          # Configuração da conexão com o BD
│       ├── TelaLogin.java        # Tela de autenticação
│       ├── TelaPrincipal.java    # Menu principal com navegação lateral
│       ├── PainelBase.java       # Classe base com estilos reutilizáveis
│       ├── TelaClientes.java     # CRUD de Clientes
│       ├── TelaBarbeiros.java    # CRUD de Barbeiros
│       ├── TelaAgendamentos.java # CRUD de Agendamentos (com INNER JOIN)
│       ├── TelaRelatorios.java   # Relatórios com LEFT JOIN
│       └── TelaDashboard.java    # Dashboard de KPIs com gráficos
├── lib/
│   ├── postgresql-42.7.3.jar      # Driver JDBC do PostgreSQL
│   ├── jfreechart-1.5.4.jar       # Biblioteca de gráficos
│   └── jcommon-1.0.24.jar         # Dependência do JFreeChart
└── README.md
```

---

## 🛠️ Tecnologias Utilizadas

| Tecnologia    | Versão     | Uso                            |
|---------------|------------|--------------------------------|
| Java          | 17+        | Linguagem principal             |
| Java Swing    | (JDK)      | Interface gráfica               |
| PostgreSQL    | 15+        | Banco de dados relacional       |
| JDBC          | 42.7.3     | Conectividade Java ↔ BD         |
| JFreeChart    | 1.5.4      | Gráficos do dashboard de KPIs   |
| JCommon       | 1.0.24     | Dependência do JFreeChart       |

---

## 🗄️ Modelagem do Banco de Dados

### Tabelas

| Tabela         | Descrição                                           |
|----------------|-----------------------------------------------------|
| `usuarios`     | Usuários do sistema (login)                         |
| `clientes`     | Clientes da barbearia                               |
| `barbeiros`    | Barbeiros cadastrados                               |
| `agendamentos` | Agendamentos de serviços (FK → clientes, barbeiros) |

### Schema

```sql
CREATE TABLE usuarios (
    id      SERIAL PRIMARY KEY,
    usuario VARCHAR(50)  NOT NULL UNIQUE,
    senha   VARCHAR(100) NOT NULL
);

CREATE TABLE clientes (
    id            SERIAL PRIMARY KEY,
    nome          VARCHAR(100) NOT NULL,
    telefone      VARCHAR(20),
    email         VARCHAR(100),
    data_cadastro TIMESTAMP DEFAULT NOW()
);

CREATE TABLE barbeiros (
    id            SERIAL PRIMARY KEY,
    nome          VARCHAR(100) NOT NULL,
    especialidade VARCHAR(100),
    telefone      VARCHAR(20),
    ativo         BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE agendamentos (
    id          SERIAL PRIMARY KEY,
    cliente_id  INTEGER NOT NULL REFERENCES clientes(id),
    barbeiro_id INTEGER NOT NULL REFERENCES barbeiros(id),
    servico     VARCHAR(100) NOT NULL,
    valor       NUMERIC(10,2) NOT NULL,
    data_hora   TIMESTAMP NOT NULL,
    status      VARCHAR(20) NOT NULL DEFAULT 'Agendado'
);
```

### Relacionamentos
- `agendamentos.cliente_id` → `clientes.id` (FK)
- `agendamentos.barbeiro_id` → `barbeiros.id` (FK)

---

## ⚙️ Como Executar

### 1. Pré-requisitos
- Java JDK 17 ou superior instalado
- PostgreSQL 15 ou superior instalado e rodando
- Os 3 JARs disponíveis em `lib/`:
  - `postgresql-42.7.3.jar` (driver JDBC)
  - `jfreechart-1.5.4.jar` (gráficos)
  - `jcommon-1.0.24.jar` (dependência do JFreeChart)

### 2. Configurar o Banco de Dados

```sql
-- 1. No psql ou pgAdmin, crie o banco:
CREATE DATABASE 7barbershop_db;

-- 2. Conecte ao banco e crie as tabelas conforme o schema descrito
--    na seção "Modelagem do Banco de Dados" deste README.

-- 3. Crie o usuário padrão para login:
INSERT INTO usuarios (usuario, senha) VALUES ('admin', 'admin123');
```

### 3. Configurar a Conexão

Edite o arquivo `src/Barbearia/Conexao.java` com suas credenciais:

```java
private static final String URL     = "jdbc:postgresql://localhost:5432/7barbershop_db";
private static final String USUARIO = "postgres";   // seu usuário
private static final String SENHA   = "postgres";   // sua senha
```

### 4. Compilar e Executar

```bash
# Linux / macOS
javac -cp ".:lib/postgresql-42.7.3.jar:lib/jfreechart-1.5.4.jar:lib/jcommon-1.0.24.jar" src/Barbearia/*.java -d out/
java  -cp ".:lib/postgresql-42.7.3.jar:lib/jfreechart-1.5.4.jar:lib/jcommon-1.0.24.jar:out/" Barbearia.TelaLogin

# Windows (use ; em vez de :)
javac -cp ".;lib/postgresql-42.7.3.jar;lib/jfreechart-1.5.4.jar;lib/jcommon-1.0.24.jar" src/Barbearia/*.java -d out/
java  -cp ".;lib/postgresql-42.7.3.jar;lib/jfreechart-1.5.4.jar;lib/jcommon-1.0.24.jar;out/" Barbearia.TelaLogin
```

> 💡 No VS Code ou IntelliJ, basta adicionar os 3 JARs como **Referenced Libraries** e rodar o `main()` da classe `TelaLogin`.

### 5. Login Padrão

| Usuário | Senha     |
|---------|-----------|
| admin   | admin123  |

---

## 📊 Dashboard de KPIs

O sistema conta com um dashboard interativo acessível pelo menu lateral, conectado diretamente ao banco via `PreparedStatement` e atualizado em tempo real conforme o período escolhido.

### Filtro Dinâmico de Período

| Opção             | Janela analisada                  |
|-------------------|-----------------------------------|
| Últimos 7 dias    | Período de 7 dias anteriores à data atual |
| Últimos 30 dias   | Período de 30 dias (padrão)       |
| Este mês          | Do dia 1 do mês atual até hoje    |
| Mês passado       | Mês civil anterior completo       |
| Últimos 90 dias   | Período de 90 dias                |
| Todo o período    | Histórico completo                |

### Indicadores (KPIs)

| Indicador             | Função SQL          | Descrição                                  |
|-----------------------|---------------------|--------------------------------------------|
| Faturamento (período) | `SUM`               | Soma do valor dos atendimentos concluídos  |
| Ticket médio          | `AVG`               | Valor médio por atendimento concluído      |
| Total de atendimentos | `COUNT`             | Quantidade total de agendamentos no período|
| Taxa de cancelamento  | `COUNT` + `CASE WHEN` | % de agendamentos com status "Cancelado" |
| Maior atendimento     | `MIN` e `MAX`       | Maior valor cobrado em um único atendimento|

### Gráficos

- 📊 **Barras** — Faturamento por barbeiro (com `LEFT JOIN` para incluir barbeiros sem atendimentos no período)
- 📈 **Linha** — Evolução diária do faturamento (agrupado por `DATE` com `ORDER BY ASC`)
- 🥧 **Pizza** — Distribuição de serviços prestados (agrupado por serviço com `ORDER BY DESC`)

---

## 🔍 Principais Consultas SQL

### INNER JOIN — Agendamentos com Cliente e Barbeiro (usado em `TelaAgendamentos`)
```sql
SELECT a.id, c.nome AS cliente, b.nome AS barbeiro,
       a.servico, a.valor, a.data_hora, a.status
FROM agendamentos a
INNER JOIN clientes  c ON c.id = a.cliente_id
INNER JOIN barbeiros b ON b.id = a.barbeiro_id
ORDER BY a.data_hora DESC;
```

### LEFT JOIN — Faturamento por Barbeiro (usado no dashboard)
```sql
SELECT b.nome,
       COUNT(a.id)               AS total_atendimentos,
       COALESCE(SUM(a.valor), 0) AS faturamento
FROM barbeiros b
LEFT JOIN agendamentos a
       ON a.barbeiro_id = b.id
      AND a.status      = 'Concluído'
      AND a.data_hora BETWEEN ? AND ?
GROUP BY b.id, b.nome
ORDER BY faturamento DESC;
```

### LEFT JOIN — Total de Agendamentos por Cliente (usado em `TelaRelatorios`)
```sql
SELECT c.nome, c.telefone,
       COUNT(a.id) AS total, COALESCE(SUM(a.valor), 0) AS gasto
FROM clientes c
LEFT JOIN agendamentos a ON a.cliente_id = c.id
GROUP BY c.id, c.nome, c.telefone
ORDER BY total DESC;
```

### Agrupamento — Evolução Diária do Faturamento (gráfico de linha)
```sql
SELECT DATE(data_hora)         AS dia,
       COUNT(*)                AS qtd_atendimentos,
       COALESCE(SUM(valor), 0) AS faturamento_dia
FROM agendamentos
WHERE status = 'Concluído'
  AND data_hora BETWEEN ? AND ?
GROUP BY DATE(data_hora)
ORDER BY dia ASC;
```

---

## 👥 Autores

- **Artur Alves de Sousa**
- **Ícaro Ryan Coelho Costa**

📧 Disciplina: Banco de Dados | Prof. Anderson Costa | UNIFSA 2026/1