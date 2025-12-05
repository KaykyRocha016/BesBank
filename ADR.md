# 1. Uso da arquitetura baseada em eventos com EventBroker
## Status: Aceita
### Contexto
Os produtores de eventos os criarão para serem consumidos por outras classes de modo que uma camada não se comunique diretamente com a outra.

### Decisão
Adotaremos o Estilo arquitetural Event Broker visando desacoplar 
as classes produtoras e consumidoras de eventos.

Através disso, poderemos utilizar a comunicação assíncrona para que os produtores de eventos não precisem esperar o processamento das requisições.

### Consequências
O Desacoplamento entre os produtores e consumidores possibilitará um aumento na escalabilidade e da elasticidade do sistema já que mais consumidores podem ser alocados facilmente.

Também teremos um ganho de resiliência já que caso os consumidores estejam indisponíveis, o evento estará registrado no broker e seu processamento posterior poderá ser feito.

### Conformidade
A conformidade será garantida por testes no ArchUnit que verificarão as dependências entre as classes

# 2. Adoção do Padrão CQRS (Command Query Responsibility Segregation)
## Status: Aceita
### Contexto
O sistema bancário BesBank requer operações de escrita (depósitos, saques) e leitura (consulta de saldo) com características distintas. As operações de escrita demandam validações complexas e consistência forte, enquanto as consultas necessitam de alta desempenho e disponibilidade.

### Decisão
Adotaremos o padrão CQRS separando completamente as responsabilidades de comando (escrita) e consulta (leitura).

#### Write Side:
WriteController recebe comandos (DepositCommand, WithdrawCommand)

AccountCommandHandler orquestra validações e persistência

AccountAggregate contém lógica de negócio

Banco bank_write armazena eventos (account_event)

#### Read Side:

ReadController atende consultas de saldo

AccountQueryService acessa dados otimizados

AccountProjection projeta eventos em modelo de leitura

Banco bank_read armazena projeções (account_read)

### Consequências
#### Positivas:

**Performance:** Queries otimizadas independentemente sem impactar comandos

**Escalabilidade:** Read e Write podem escalar separadamente conforme demanda

**Modelo de dados específico:** Cada lado tem schema otimizado para seu propósito

**Eventual Consistency:** Leituras não bloqueiam escritas

**Manutenibilidade:** Lógica de negócio isolada do modelo de consulta

#### Negativas:

**Complexidade aumentada:** Dois modelos de dados para manter

**Consistência eventual:** Delay entre escrita e disponibilidade na leitura (mitigado pelo Kafka)

**Overhead operacional:** Dois bancos PostgreSQL para gerenciar

### Conformidade
A conformidade será garantida através de:

Testes ArchUnit verificando que WriteController não acessa repositórios de leitura

Testes ArchUnit garantindo que ReadController não acessa repositórios de escrita

Convenção de nomenclatura: classes *Command* no write side, *Query* no read side

Separação física: org.example.infrastructure.jpa.write vs org.example.infrastructure.jpa.read


# 3. Seleção do Apache Kafka como Message Broker
## Status: Aceita

### Contexto
Com a decisão de utilizar **CQRS (ADR 002)**, criou-se a necessidade de sincronizar o banco de dados de escrita (`bank_write`) com o banco de dados de leitura (`bank_read`).

Como as operações de escrita geram eventos de domínio (ex: `AccountCreated`, `MoneyDeposited`), precisamos de um mecanismo de transporte que garanta:
1. **Entrega garantida:** Nenhum evento financeiro pode ser perdido.
2. **Ordenação estrita:** O cálculo do saldo depende da ordem exata das transações.
3. **Persistência de log:** Capacidade de reprocessar eventos caso a projeção de leitura precise ser reconstruída (Replayability).

### Decisão
Adotaremos o **Apache Kafka** como a plataforma de streaming de eventos distribuídos.

O fluxo de dados será configurado da seguinte forma:
1. O **Write Side** publicará eventos no tópico `account-events` após a persistência do comando.
2. O **Read Side** atuará como consumidor através do consumer-group `balance-projector-group`.
3. O Kafka servirá como a "Single Source of Truth" (Fonte Única da Verdade) temporária para o trânsito de dados entre os modelos.

### Consequências
#### Positivas:
* **Capacidade de Replay:** Diferente de filas tradicionais, o Kafka retém as mensagens por um período configurado. Isso permite que, se o banco de leitura (`bank_read`) for corrompido ou precisarmos criar uma nova projeção (ex: relatório de auditoria), podemos reprocessar todos os eventos do início.
* **Ordenação por Partição:** O Kafka garante a ordem dos eventos dentro de uma partição. Utilizando o ID da conta como chave de partição, garantimos que todas as transações de um mesmo cliente sejam processadas na ordem exata.
* **Desacoplamento Temporal:** O serviço de leitura pode estar fora do ar para manutenção; quando voltar, ele apenas lê o offset de onde parou, sem perda de dados.

#### Negativas:
* **Complexidade de Infraestrutura:** Exige gerenciamento de brokers e Zookeeper/KRaft (mitigado pelo uso de containers Docker no ambiente de desenvolvimento).
* **Consistência Eventual:** Haverá um "lag" (atraso) inevitável entre a confirmação da transação e a atualização do saldo na consulta.

### Conformidade
A conformidade será garantida através de:
* **Configuração:** Validação da propriedade `spring.kafka.consumer.group-id` para garantir que projetores façam parte do grupo `balance-projector-group`.