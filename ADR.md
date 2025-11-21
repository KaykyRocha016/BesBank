# 1.Uso da arquitetura baseada em eventos com EventBroker
## Status: Aceita
### Contexto
Os produtores de eventos os criarão para que sejam consumidos por outras classes de modo que uma camada não se comunique diretamente com a outra.

### Decisão
Adotaremos o Estilo arquitetural Event Broker visando desacoplar 
as classes produtoras e consumidoras de eventos.

Através disso, poderemos utilizar a comunicação assíncrona para que os produtores deventos não precisem esperar o processamento das requisições.

### Consequências
O Desacoplamento entre os produtores e consumidaros possibilitará um aumento na escalabilidade e da elasticidade do sistema já que mais consumidores podem ser alocados facilmente.

Também teremos um ganho de resiliência já que caso os consumidores estejam indisponíveis, o evento estará registrado no broker e seu processamento posterior poderá ser feito.

### Conformidade
A conformidade será garantida através de testes no ArchUnit que verificarão as dependências entre as classes