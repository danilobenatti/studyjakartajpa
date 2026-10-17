# Plano de Estudos: Flyway + Jakarta EE 21 + EclipseLink 5 + PostgreSQL

Este documento apresenta o cronograma detalhado de aprendizado e o guia de implementação prática para integrar o Flyway em aplicações corporativas modernas utilizando a especificação **Jakarta EE 21 (Java EE 21)**, o framework de persistência **EclipseLink 5** e o banco de dados **PostgreSQL**.

---

## 🗺️ Cronograma de Estudos (4 Semanas)

### Semana 1: Setup, Docker e Convenções no PostgreSQL
O objetivo é isolar o Flyway e entender como ele interage especificamente com as características do PostgreSQL.
* **Ambiente isolado:** Configuração de um ambiente local reprodutível utilizando Docker Compose para o PostgreSQL.
* **Flyway CLI Independente:** Instalação e uso do terminal (`flyway migrate`, `info`, `validate`, `repair`) conectado ao container.
* **Dialeto Nativo Postgres:** Escrita de scripts SQL usando recursos específicos como chaves primárias do tipo `UUID`, colunas `JSONB` e fuso horário com `TIMESTAMPTZ`.

### Semana 2: Integração com Jakarta EE 21
Aqui você conectará o ciclo de vida do Flyway com a inicialização do seu servidor de aplicação.
* **Gerenciamento de Dependências:** Inclusão dos artefatos `flyway-core` e do driver JDBC do PostgreSQL no `pom.xml` ou `build.gradle`.
* **Gatilho de Inicialização (Startup Trigger):** Criação de um Bean `@Singleton` anotado com `@Startup` para interceptar a inicialização do container Jakarta EE.
* **Execução Programática:** Configuração do Flyway via código Java utilizando o DataSource injetado pelo servidor de aplicação.

### Semana 3: Alinhamento de DDL (EclipseLink vs Flyway)
O maior desafio desta stack é gerenciar a concorrência de quem dita a estrutura do banco de dados.
* **Desativar Automação do ORM:** Configurar o EclipseLink para não alterar ou criar tabelas em ambientes produtivos (`database.action = none`).
* **Validação de Schema:** Garantir que o mapeamento das classes anotadas com `@Entity` seja estritamente compatível com o schema gerado pelo Flyway.
* **Migrações Repetíveis (Prefix `R__`):** Gerenciamento de Views, Triggers e Stored Procedures em PL/pgSQL de forma versionada.

### Semana 4: Cenários Avançados e Ambientes Reais
Preparando a infraestrutura para rodar de forma resiliente em servidores corporativos e clusters.
* **Bloqueio de Concorrência (Locking):** Entender como o Flyway utiliza os *Advisory Locks* nativos do PostgreSQL para impedir que instâncias paralelas corrompam o banco.
* **Estratégia de Baseline:** Como introduzir o Flyway em bancos de dados legados que já possuem tabelas criadas manualmente ou por versões antigas do ORM.

---

## 💻 Exemplo Prático de Implementação

Abaixo encontra-se a arquitetura de código necessária para acoplar o Flyway antes da carga total do EclipseLink.

### 1. Configuração do Maven (`pom.xml`)
Adicione as seguintes dependências ao projeto Jakarta EE:

```xml
<dependencies>
    <!-- Jakarta EE 21 Web Profile / Full API -->
    <dependency>
        <groupId>jakarta.platform</groupId>
        <artifactId>jakarta.jakartaee-api</artifactId>
        <version>11.0.0-M1</version> <!-- Compatível com Jakarta EE 21 -->
        <scope>provided</scope>
    </dependency>

    <!-- Core do Flyway -->
    <dependency>
        <groupId>org.flywaydb</groupId>
        <artifactId>flyway-core</artifactId>
        <version>10.17.0</version>
    </dependency>
    <!-- Suporte do Flyway para PostgreSQL -->
    <dependency>
        <groupId>org.flywaydb</groupId>
        <artifactId>flyway-database-postgresql</artifactId>
        <version>10.17.0</version>
    </dependency>

    <!-- Driver JDBC do PostgreSQL -->
    <dependency>
        <groupId>org.postgresql</groupId>
        <artifactId>postgresql</artifactId>
        <version>42.7.3</version>
    </dependency>
</dependencies>
```

### 2. Configuração de Persistência (`persistence.xml`)
O arquivo deve ser configurado para **apenas validar** o esquema, delegando toda a criação estrutural ao Flyway. O arquivo deve ser salvo em `src/main/resources/META-INF/persistence.xml`.

```xml
<?xml version="1.0" encoding="UTF-8"?>
<persistence version="3.0" xmlns="https://jakarta.ee/xml/ns/persistence"
             xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
             xsi:schemaLocation="https://jakarta.ee/xml/ns/persistence https://jakarta.ee/xml/ns/persistence/persistence_3_0.xsd">
    
    <persistence-unit name="MeuAppPU" transaction-type="JTA">
        <provider>org.eclipse.persistence.jpa.PersistenceProvider</provider>
        <jta-data-source>java:app/jdbc/MeuPostgresDS</jta-data-source>
        
        <properties>
            <!-- Desativa a geração automática de DDL pelo EclipseLink -->
            <property name="jakarta.persistence.schema-generation.database.action" value="none"/>
            
            <!-- Configurações específicas do EclipseLink 5 -->
            <property name="eclipselink.target-database" value="PostgreSQL"/>
            <property name="eclipselink.logging.level" value="FINE"/>
            
            <!-- Opcional: Ativa a validação estrita do modelo contra o banco existente -->
            <property name="eclipselink.weaving" value="static"/>
        </properties>
    </persistence-unit>
</persistence>
```

### 3. Classe de Inicialização Automatizada (`@Startup`)
Esta classe garante que o Flyway execute as migrações no banco de dados assim que o servidor de aplicação carregar o módulo, garantindo que o DataSource já esteja limpo e atualizado antes de qualquer requisição do EclipseLink.

```java
package com.empresa.config;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import jakarta.ejb.Singleton;
import jakarta.ejb.Startup;
import jakarta.ejb.TransactionManagement;
import jakarta.ejb.TransactionManagementType;
import javax.sql.DataSource;
import org.flywaydb.core.Flyway;
import java.util.logging.Logger;

@Singleton
@Startup
@TransactionManagement(TransactionManagementType.BEAN) // Evita conflitos com transações JTA ativas
public class FlywayMigrationConfig {

    private static final Logger LOGGER = Logger.getLogger(FlywayMigrationConfig.class.getName());

    // Injeta o DataSource gerenciado pelo Servidor de Aplicação (WildFly, Payara, GlassFish, etc.)
    @Resource(lookup = "java:app/jdbc/MeuPostgresDS")
    private DataSource dataSource;

    @PostConstruct
    public void initializeDatabase() {
        LOGGER.info("Iniciando controle de versão do banco de dados com Flyway...");

        try {
            if (dataSource == null) {
                LOGGER.severe("Erro grave: O DataSource injetado está nulo. Abortando migração.");
                return;
            }

            // Configuração programática do Flyway para ambiente Jakarta EE
            Flyway flyway = Flyway.configure()
                    .dataSource(dataSource)
                    .locations("db/migration") // Pasta padrão localizada em src/main/resources/db/migration
                    .baselineOnMigrate(true)   // Cria o baseline caso o banco já possua tabelas antigas
                    .load();

            // Executa todas as migrações pendentes
            flyway.migrate();

            LOGGER.info("Banco de dados atualizado com sucesso pelo Flyway!");
        } catch (Exception e) {
            LOGGER.severe("Falha crítica ao executar as migrações do Flyway: " + e.getMessage());
            throw new RuntimeException("Não foi possível inicializar a estrutura do banco de dados", e);
        }
    }
}
```

### 4. Exemplo de Script de Migração (`V1__Criar_Estrutura_Inicial.sql`)
Crie este arquivo no diretório `src/main/resources/db/migration/V1__Criar_Estrutura_Inicial.sql`:

```sql
-- Ativa extensão para geração de UUID se necessário no PostgreSQL
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE tb_usuario (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    nome VARCHAR(150) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    data_cadastro TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    configuracoes JSONB
);

-- Indexação otimizada para o campo JSONB
CREATE INDEX idx_usuario_config ON tb_usuario USING gin (configuracoes);
```

---
Este guia fornece a base sólida e padronizada para gerenciar bancos de dados sob controle de versão contínuo em ambientes Jakarta EE corporativos de larga escala.
