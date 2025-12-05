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