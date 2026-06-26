# VoidPlugin

> **Core library plugin** para servidores Spigot — fornece abstrações de banco de dados, sistema de migrações automáticas, containers de dados tipados e uma camada de storage plugável para todos os plugins dependentes.

---

## 📋 Índice

- [Sobre](#sobre)
- [Requisitos](#requisitos)
- [Instalação](#instalação)
- [Configuração](#configuração)
- [Arquitetura](#arquitetura)
- [Storage Types](#storage-types)
- [Sistema de Tabelas](#sistema-de-tabelas)
- [Migrações Automáticas](#migrações-automáticas)
- [DataContainer](#datacontainer)
- [Criando uma Tabela](#criando-uma-tabela)
- [Criando um Plugin Dependente](#criando-um-plugin-dependente)
- [Build](#build)

---

## Sobre

O **VoidPlugin** é um plugin de núcleo/biblioteca para servidores Spigot. Ele **não adiciona funcionalidades diretamente ao jogo** — seu propósito é fornecer uma base sólida e reutilizável para o ecossistema de plugins, incluindo:

- Abstração de storage com suporte a **MySQL**, **MariaDB**, **HikariCP** e **SQLite**
- Criação e **migração automática de colunas** em tabelas existentes (MySQL e SQLite)
- Sistema de tabelas via **anotação `@DataTableInfo`** com SQL declarativo
- `DataContainer` — wrapper de valores tipados com suporte a JSON e dirty-tracking
- `StorageFactory` para instanciar a implementação correta via `config.yml`
- `NonClosableConnection` — wrapper que protege conexões de serem fechadas por código externo

---

## Requisitos

| Dependência   | Versão mínima |
|---------------|---------------|
| Java          | 11+           |
| Spigot / Paper| 1.8+          |
| Maven         | 3.6+          |

> HikariCP, drivers JDBC (MySQL Connector/J, SQLite) e json-simple são incluídos via **Maven Shade** no JAR final.

---

## Instalação

1. Faça o download do JAR mais recente na aba [Releases](../../releases).
2. Coloque o arquivo em `plugins/` do seu servidor.
3. Inicie o servidor uma vez para gerar o `config.yml`.
4. Configure o tipo de storage e credenciais.
5. Reinicie o servidor.

---

## Configuração

Arquivo gerado automaticamente em `plugins/VoidPlugin/config.yml`:

```yaml
storage:
  # Tipo: mysql | mariadb | hikari | sqlite
  method: hikari

  mysql:
    host: localhost
    port: 3306
    database: minecraft
    username: root
    password: ""

  hikari:
    max-pool-size: 10
    min-idle: 2
    connection-timeout: 30000
    idle-timeout: 600000
    max-lifetime: 1800000

  sqlite:
    file: database
```

---

## Arquitetura

```
org.ltzin/
├── Main.java                              # Plugin principal (JavaPlugin)
│
├── database/
│   ├── DatabaseManager.java               # Orquestra o setup de todas as DataTables
│   │
│   ├── data/
│   │   ├── DataTable.java                 # Classe base abstrata para tabelas
│   │   ├── DataContainer.java             # Wrapper tipado de valores (dirty-tracking, JSON)
│   │   └── interfaces/
│   │       ├── DataTableInfo.java         # Anotação @DataTableInfo (name, create, select…)
│   │       └── AbstractContainer.java     # Base para containers derivados de DataContainer
│   │
│   ├── tables/
│   │   └── VoidlessTable.java             # Tabela padrão: VoidProfile
│   │
│   ├── mysql/
│   │   ├── MySQLDatabase.java             # Implementação JDBC simples (reconexão automática)
│   │   ├── HikariDatabase.java            # Implementação com pool HikariCP
│   │   └── NonClosableConnection.java     # Wrapper que torna close() um no-op
│   │
│   ├── sqlite/
│   │   └── SQLiteDatabase.java            # Implementação SQLite com lock sincronizado
│   │
│   └── storage/
│       ├── StorageFactory.java            # Cria a implementação correta via StorageType
│       ├── StorageMetadata.java           # DTO: connected, ping, sizeBytes
│       ├── implementation/
│       │   └── StorageImplementation.java # Interface base de todos os storages
│       └── type/
│           └── StorageType.java           # Enum: MYSQL, MARIADB, HIKARI, SQLITE
```

---

## Storage Types

| `StorageType` | Identifiers no config  | Classe                 | Descrição                              |
|---------------|------------------------|------------------------|----------------------------------------|
| `MYSQL`       | `mysql`                | `MySQLDatabase`        | JDBC simples, sem pool, reconexão auto |
| `MARIADB`     | `mariadb`              | `MySQLDatabase`        | Mesmo driver MySQL, alias de config    |
| `HIKARI`      | `hikari`, `hikaricp`   | `HikariDatabase`       | Pool de conexões via HikariCP          |
| `SQLITE`      | `sqlite`               | `SQLiteDatabase`       | Arquivo local `.db`, lock sincronizado |

A fábrica é usada assim:

```java
StorageType type = StorageType.parse(config.getString("storage.method"), StorageType.HIKARI);
StorageImplementation storage = new StorageFactory(main).createNewImplementation(type);
storage.init();
```

---

## Sistema de Tabelas

Toda tabela é uma classe que estende `DataTable` e é anotada com `@DataTableInfo`:

```java
@DataTableInfo(
    name   = "VoidProfile",
    create = "CREATE TABLE IF NOT EXISTS `VoidProfile` ("
           + "`name` VARCHAR(32), `cash` LONG, `role` TEXT, "
           + "`created` LONG, `lastlogin` LONG, PRIMARY KEY(`name`)"
           + ") ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE utf8_bin;",
    select = "SELECT * FROM `VoidProfile` WHERE LOWER(`name`) = ?",
    insert = "INSERT INTO `VoidProfile` VALUES (?, ?, ?, ?, ?)",
    update = "UPDATE `VoidProfile` SET `cash`=?, `role`=?, `created`=?, `lastlogin`=? WHERE LOWER(`name`)=?"
)
public class VoidlessTable extends DataTable { ... }
```

As tabelas são registradas de dois jeitos:

```java
// Registro estático (tabelas internas do VoidPlugin)
// Já feito em DataTable via bloco static{}

// Registro de tabelas de outros plugins
DataTable.registerTable(new MinhaTabela());
```

O `DatabaseManager.setupTables()` itera sobre todas as tabelas registradas e chama `table.setup(storage)` para cada uma.

---

## Migrações Automáticas

O `DataTable.setup()` cria a tabela se não existir **e** compara as colunas definidas no `create` com as colunas atuais no banco, adicionando ou modificando automaticamente o que for necessário.

| Banco   | Operação suportada            |
|---------|-------------------------------|
| MySQL   | `ADD COLUMN` + `MODIFY COLUMN`|
| SQLite  | `ADD COLUMN` (sem MODIFY*)    |

> *SQLite não suporta `MODIFY COLUMN` nativamente. Mudanças de tipo em SQLite exigem recriação manual da tabela.

A conversão de SQL MySQL → SQLite é feita automaticamente:
- `ENGINE=InnoDB`, `DEFAULT CHARSET`, `COLLATE` → removidos
- `LONG` → `INTEGER`
- `VARCHAR(n)` → `TEXT`

---

## DataContainer

`DataContainer` é o wrapper central para valores de colunas. Ele rastreia se o valor foi alterado (`dirty-tracking`) e oferece conversão tipada:

```java
DataContainer cash = new DataContainer(1000L);

// Leitura tipada
long valor = cash.getAsLong();
String texto = cash.getAsString();
JSONObject json = cash.getAsJsonObject();

// Modificação (marca como updated)
cash.set(2000L);
cash.addLong(500L);
cash.removeLong(200L);

// Verificar se foi alterado desde o último save
if (cash.isUpdated()) {
    // persistir no banco
    cash.setUpdated(false);
}

// Liberar memória (após remover do cache)
cash.gc();
```

Containers derivados podem ser criados estendendo `AbstractContainer` e obtidos via:

```java
MeuContainer c = dataContainer.getContainer(MeuContainer.class);
```

---

## Criando uma Tabela

```java
@DataTableInfo(
    name   = "MinhaTabela",
    create = "CREATE TABLE IF NOT EXISTS `MinhaTabela` ("
           + "`player` VARCHAR(32), "
           + "`pontos` LONG DEFAULT 0, "
           + "PRIMARY KEY(`player`)"
           + ") ENGINE=InnoDB DEFAULT CHARSET=utf8;",
    select = "SELECT * FROM `MinhaTabela` WHERE `player` = ?",
    insert = "INSERT INTO `MinhaTabela` VALUES (?, ?)",
    update = "UPDATE `MinhaTabela` SET `pontos` = ? WHERE `player` = ?"
)
public class MinhaTabela extends DataTable {

    @Override
    public void init(StorageImplementation storage) {
        // Executado após o setup da tabela — use para índices, seeds, etc.
    }

    @Override
    public Map<String, DataContainer> getDefaultValues() {
        Map<String, DataContainer> defaults = new LinkedHashMap<>();
        defaults.put("pontos", new DataContainer(0L));
        return defaults;
    }
}
```

Registre a tabela antes de `DatabaseManager.setupTables()`:

```java
DataTable.registerTable(new MinhaTabela());
```

---

## Criando um Plugin Dependente

### 1. Declare a dependência no `plugin.yml`

```yaml
name: MeuPlugin
version: 1.0.0
main: com.exemplo.MeuPlugin
depend:
  - VoidPlugin
```

### 2. Adicione o JAR como dependência `provided` no `pom.xml`

```xml
<dependency>
    <groupId>org.ltzin</groupId>
    <artifactId>VoidPlugin</artifactId>
    <version>1.0.0</version>
    <scope>provided</scope>
</dependency>
```

### 3. Registre suas tabelas e acesse o storage

```java
public class MeuPlugin extends JavaPlugin {

    @Override
    public void onEnable() {
        // Registra a tabela antes do setup
        DataTable.registerTable(new MinhaTabela());

        // Obtém o storage já inicializado pelo VoidPlugin
        DatabaseManager manager = VoidPluginAPI.getDatabaseManager();
        StorageImplementation storage = manager.getStorage();

        // Usa uma conexão
        try (Connection conn = storage.getConnection()) {
            // queries aqui
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
```

---

## Build

```bash
# Clonar o repositório
git clone https://github.com/joaomanoelaraujo/VoidPlugin.git
cd VoidPlugin

# Compilar e gerar o JAR com dependências incluídas (shade)
mvn clean package
```

O JAR final estará em `target/` com o sufixo `-shaded.jar`.

---

<div align="center">
  Feito com ☕ por <strong>D4RKK Community</strong>
</div>
