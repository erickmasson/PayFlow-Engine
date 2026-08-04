# PayFlow Engine

**API Backend para Processamento de Transações Financeiras e Carteira Digital**

Este repositório contém o código-fonte do **PayFlow Engine**, um projeto de estudo aplicado desenvolvido para simular padrões de arquitetura e desafios de engenharia de software comuns em ambientes de produção de alta disponibilidade e sistemas financeiros distribuídos.

---

## 1. Contexto e Propósito Acadêmico

O objetivo principal deste projeto é explorar e consolidar conceitos avançados de desenvolvimento backend com Java e Spring Boot, afastando-se de implementações simplificadas do tipo "CRUD". A aplicação foi projetada para lidar com problemas reais da indústria, tais como:

*   **Integridade de Dados e Concorrência:** Garantia de consistência em operações de débito e crédito simultâneas em uma mesma carteira (*Race Conditions*).
*   **Idempotência:** Prevenção de reprocessamento indesejado de requisições financeiras duplicadas decorrentes de falhas de rede.
*   **Processamento Assíncrono:** Desacoplamento do fluxo principal de pagamento da emissão de notificações e eventos secundários.
*   **Testabilidade e Qualidade:** Cobertura de regras de negócio por meio de testes unitários isolados e integração contínua (CI).
*   **Separabilidade de Responsabilidades:** Manutenção de código limpo, desacoplando configurações de infraestrutura (como documentação Swagger) das regras de domínio e controladores.

---

## 2. Tecnologias e Ferramentas Utilizadas

A escolha da stack tecnológica reflete o ecossistema moderno de desenvolvimento em Java:

| Categoria | Tecnologia / Biblioteca | Função no Sistema |
| :--- | :--- | :--- |
| **Linguagem / Runtime** | Java 21 (Amazon Corretto) | Linguagem de programação e ambiente de execução principal. |
| **Framework Base** | Spring Boot 4.1 | Framework para estruturação da API REST, injeção de dependências e JPA. |
| **Persistência de Dados** | PostgreSQL + Flyway | Banco de dados relacional e versionamento automatizado do schema SQL. |
| **Cache & Idempotência** | Redis (Spring Data Redis) | Armazenamento temporário em memória para validação da chave `X-Idempotency-Key`. |
| **Mensageria Assíncrona** | RabbitMQ (Spring AMQP) | Broker de mensageria para processamento de eventos pós-transação. |
| **Segurança** | Spring Security + Auth0 java-jwt | Autenticação *stateless* baseada em tokens JWT e controle de acesso (RBAC). |
| **Testes Automatizados** | JUnit 5 + Mockito | Suíte de testes unitários focada na validação da camada de serviço (`TransferService`). |
| **Automação & Containerização** | Docker & GitHub Actions | Containerização da aplicação e pipeline automatizada de integração contínua (CI). |
| **Documentação** | Springdoc OpenAPI (Swagger UI) | Geração de documentação interativa da API, configurada de forma desacoplada via `OpenApiCustomizer`. |

---

## 3. Decisões Arquiteturais e Implementação

### 3.1. Controle de Concorrência e Transacionalidade
Para evitar inconsistências de saldo em cenários de requisições concorrentes sobre a mesma carteira, utilizou-se o mecanismo de **Lock Pessimista** na camada de persistência (`@Lock(LockModeType.PESSIMISTIC_WRITE)`). Isso garante isolamento estrito durante o cálculo e atualização de saldos.

### 3.2. Mecanismo de Idempotência com AOP
A verificação de requisições duplicadas foi abstraída da camada de controladores através de **Programação Orientada a Aspectos (AOP)**:
1. O cliente envia o cabeçalho HTTP `X-Idempotency-Key`.
2. Um aspecto intercepta a requisição e verifica no **Redis** se a chave já foi processada.
3. Se processada, a resposta armazenada no cache é retornada imediatamente.
4. Caso contrário, a execução prossegue e o resultado é salvo no Redis com tempo de vida (TTL) pré-definido.

### 3.3. Comunicação Assíncrona via Mensageria
O encerramento do fluxo financeiro aciona a publicação do evento `TransactionCompletedEvent` em uma *Exchange* do **RabbitMQ**. Um *Consumer* consome a mensagem de forma assíncrona para simular o processamento de notificações (e-mail/webhooks), garantindo que a resposta ao cliente não seja bloqueada por tarefas secundárias.

### 3.4. Estratégia de Testes
A validação das regras de negócio (como restrição de transferência por perfis `LOJISTA` e verificação de saldo suficiente) foi concentrada em **Testes Unitários puramente focados no domínio** usando JUnit 5 e Mockito. Isso assegura execução na casa dos milissegundos, independente de instâncias externas de banco de dados ou contêineres Docker.

---

## 4. Objetivos e Cobertura de Requisitos

- [x] Modelagem relacional e migrations com Flyway.
- [x] Autenticação e autorização via JWT com segregação por papéis (`CLIENTE` e `LOJISTA`).
- [x] Regra de negócio de transferência transacional entre carteiras.
- [x] Bloqueio contra concorrência agressiva de saldo.
- [x] Camada de idempotência integrada com Redis via Anotação Customizada/AOP.
- [x] Eventos de integração com RabbitMQ para disparo assíncrono.
- [x] Suíte de testes unitários da camada de serviço executando sem dependências de infraestrutura.
- [x] Configuração dinâmica da documentação OpenAPI desacoplada do código de negócio.
- [x] Construção de imagem Docker multi-stage e pipeline de CI via GitHub Actions.

---

## 5. Como Executar a Aplicação

### Pré-requisitos
*   **JDK 21** ou superior
*   **Docker** e **Docker Compose**
*   **Maven 3.9+** (ou utilitário `mvnw` incluído)

### 1. Subir os Serviços de Infraestrutura
Utilize o Docker Compose para iniciar os contêineres de PostgreSQL, Redis e RabbitMQ:

```bash
docker-compose up -d
```

### 2. Compilar e Executar a API
Execute a aplicação Spring Boot localmente:

```bash
./mvnw spring-boot:run
```

A API estará acessível em `http://localhost:8080`.

### 3. Executar os Testes Unitários
Para rodar a suíte completa de testes automatizados:

```bash
./mvnw clean test
```

### 4. Acessar a Documentação
Com a aplicação ativa, a documentação Swagger interativa pode ser acessada em:
`http://localhost:8080/swagger-ui.html`

---

## 6. Considerações Finais

Este projeto cumpriu seu papel como ambiente controlado de aprendizagem, demonstrando a aplicação prática de conceitos de integridade transacional, desacoplamento e automação de testes dentro de uma arquitetura resiliente.