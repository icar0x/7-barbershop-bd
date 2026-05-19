# ✂️ 7 Barbershop

> Sistema de Gestão — 7 Barbershop com interface gráfica (Java Swing) integrada ao PostgreSQL.

---

## 📋 Sobre o Projeto

Sistema desenvolvido para gerenciar o funcionamento de uma barbearia, permitindo o cadastro e controle de **clientes**, **barbeiros** e **agendamentos** de serviços. Possui tela de login, operações completas de CRUD, consultas com INNER JOIN e LEFT JOIN, além de relatórios de faturamento.

Desenvolvido como trabalho da disciplina de **Banco de Dados** — UNIFSA (2026/1).

---

## 🗂️ Estrutura do Repositório

```
barbearia/

├── src/
│   └── barbearia/
│       ├── Conexao.java         # Configuração da conexão com o BD
│       ├── TelaLogin.java       # Tela de autenticação
│       ├── TelaPrincipal.java   # Menu principal com navegação lateral
│       ├── PainelBase.java      # Classe base com estilos reutilizáveis
│       ├── TelaClientes.java    # CRUD de Clientes
│       ├── TelaBarbeiros.java   # CRUD de Barbeiros
│       ├── TelaAgendamentos.java# CRUD de Agendamentos (com INNER JOIN)
│       └── TelaRelatorios.java  # Relatórios com LEFT JOIN
└── README.md
```

---

## 🛠️ Tecnologias Utilizadas

| Tecnologia    | Versão     | Uso                        |
|---------------|------------|----------------------------|
| Java          | 17+        | Linguagem principal         |
| Java Swing    | (JDK)      | Interface gráfica           |
| PostgreSQL    | 15+        | Banco de dados relacional   |
| JDBC          | 42.x       | Conectividade Java ↔ BD     |

---

## 🗄️ Modelagem do Banco de Dados

### Tabelas

| Tabela         | Descrição                                        |
|----------------|--------------------------------------------------|
| `usuarios`     | Usuários do sistema (login)                      |
| `clientes`     | Clientes da barbearia                            |
| `barbeiros`    | Barbeiros cadastrados                            |
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
# Compile (inclua o driver JDBC no classpath)
javac -cp ".;postgresql-42.7.3.jar" src/barbearia/*.java -d out/

# Execute
java -cp ".;postgresql-42.7.3.jar;out/" barbearia.TelaLogin
```

> No Linux/Mac substitua `;` por `:` no classpath.

### 5. Login Padrão

| Usuário | Senha     |
|---------|-----------|
| admin   | admin123  |

---

## 🔍 Consultas com JOIN

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
## 
📧 Disciplina: Banco de Dados | Prof. Anderson Costa
