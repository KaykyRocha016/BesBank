package architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.*;
import static com.tngtech.archunit.library.Architectures.layeredArchitecture;

public class ArchitectureTest {

    private static JavaClasses importedClasses;

    @BeforeAll
    static void setup() {
        importedClasses = new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .importPackages("org.example");
    }

    // =========================================================================
    // ADR 1: Event Broker Architecture
    // =========================================================================
    @Nested
    @DisplayName("ADR 1: Event Broker - Desacoplamento entre Produtores e Consumidores")
    class EventBrokerArchitectureTest {

        @Test
        @DisplayName("Produtores de eventos não devem chamar projeções diretamente")
        void producersShouldNotDependOnProjections() {
            ArchRule rule = noClasses()
                    .that().resideInAPackage("..application.service..")
                    .and().haveSimpleNameContaining("CommandHandler")
                    .or().haveSimpleNameContaining("EventStore")
                    .or().resideInAPackage("..domain..")
                    .should().dependOnClassesThat()
                    .resideInAPackage("..application.projection..")
                    .because("CommandHandlers e EventStore não devem chamar projeções - use Event Broker (Kafka)");

            rule.check(importedClasses);
        }

        @Test
        @DisplayName("EventPublisher deve ser a única classe que acessa Kafka Producer")
        void onlyEventPublisherShouldAccessKafkaProducer() {
            ArchRule rule = noClasses()
                    .that().resideOutsideOfPackage("..service.implementation..")
                    .should().dependOnClassesThat()
                    .haveNameMatching(".*KafkaProducer.*")
                    .because("Apenas EventPublisher deve acessar Kafka Producer diretamente");

            rule.check(importedClasses);
        }

        @Test
        @DisplayName("EventConsumer deve ser a única classe que acessa Kafka Consumer")
        void onlyEventConsumerShouldAccessKafkaConsumer() {
            ArchRule rule = noClasses()
                    .that().resideOutsideOfPackage("..service.implementation..")
                    .should().dependOnClassesThat()
                    .haveNameMatching(".*KafkaConsumer.*")
                    .because("Apenas EventConsumer deve acessar Kafka Consumer diretamente");

            rule.check(importedClasses);
        }

        @Test
        @DisplayName("Classes de domínio não devem depender de infraestrutura de mensageria")
        void domainShouldNotDependOnMessagingInfrastructure() {
            ArchRule rule = noClasses()
                    .that().resideInAPackage("..domain..")
                    .should().dependOnClassesThat()
                    .resideInAnyPackage("org.apache.kafka..", "org.springframework.kafka..")
                    .because("Domínio deve permanecer puro, sem conhecimento de infraestrutura");

            rule.check(importedClasses);
        }
    }

    // =========================================================================
    // ADR 2: CQRS Pattern
    // =========================================================================
    @Nested
    @DisplayName("ADR 2: CQRS - Separação entre Write e Read Models")
    class CQRSArchitectureTest {

        @Test
        @DisplayName("WriteController não deve acessar repositórios de leitura")
        void writeControllerShouldNotAccessReadRepositories() {
            ArchRule rule = noClasses()
                    .that().haveSimpleNameEndingWith("WriteController")
                    .should().dependOnClassesThat()
                    .resideInAPackage("..infrastructure.jpa.read..")
                    .because("Write Side não deve conhecer Read Model (CQRS)");

            rule.check(importedClasses);
        }

        @Test
        @DisplayName("ReadController não deve acessar repositórios de escrita")
        void readControllerShouldNotAccessWriteRepositories() {
            ArchRule rule = noClasses()
                    .that().haveSimpleNameEndingWith("ReadController")
                    .should().dependOnClassesThat()
                    .resideInAPackage("..infrastructure.jpa.write..")
                    .because("Read Side não deve conhecer Write Model (CQRS)");

            rule.check(importedClasses);
        }

        @Test
        @DisplayName("Comandos devem residir no pacote de commands")
        void commandsShouldResideInCommandsPackage() {
            ArchRule rule = classes()
                    .that().haveSimpleNameEndingWith("Command")
                    .should().resideInAPackage("..domain.commands..")
                    .because("Comandos devem estar organizados no pacote correto");

            rule.check(importedClasses);
        }

        @Test
        @DisplayName("Queries/Services de leitura devem usar sufixo QueryService")
        void queriesShouldHaveCorrectNaming() {
            ArchRule rule = classes()
                    .that().resideInAPackage("..application.service..")
                    .and().haveSimpleNameContaining("Query")
                    .should().haveSimpleNameEndingWith("QueryService")
                    .because("Convenção de nomenclatura CQRS");

            rule.check(importedClasses);
        }

        @Test
        @DisplayName("CommandHandler não deve acessar repositórios de leitura")
        void commandHandlerShouldNotAccessReadRepositories() {
            ArchRule rule = noClasses()
                    .that().haveSimpleNameContaining("CommandHandler")
                    .should().dependOnClassesThat()
                    .resideInAPackage("..infrastructure.jpa.read..")
                    .because("Command Handler pertence ao Write Side");

            rule.check(importedClasses);
        }
    }

    // =========================================================================
    // Regras Gerais de Camadas
    // =========================================================================
    @Nested
    @DisplayName("Regras Gerais de Arquitetura em Camadas")
    class LayeredArchitectureTest {

        @Test
        @DisplayName("Arquitetura em camadas deve ser respeitada")
        void layersShouldBeRespected() {
            ArchRule rule = layeredArchitecture()
                    .consideringAllDependencies()
                    .layer("Presentation").definedBy("..presentation..")
                    .layer("Application").definedBy("..application..")
                    .layer("Domain").definedBy("..domain..")
                    .layer("Infrastructure").definedBy("..infrastructure..")

                    .whereLayer("Presentation").mayNotBeAccessedByAnyLayer()
                    .whereLayer("Application").mayOnlyBeAccessedByLayers("Presentation", "Infrastructure")
                    .whereLayer("Domain").mayOnlyBeAccessedByLayers("Presentation", "Application", "Infrastructure");

            rule.check(importedClasses);
        }

        @Test
        @DisplayName("Controllers não devem acessar Aggregates diretamente")
        void controllersShouldNotAccessAggregates() {
            ArchRule rule = noClasses()
                    .that().resideInAPackage("..presentation..")
                    .should().dependOnClassesThat()
                    .resideInAPackage("..domain.aggregate..")
                    .because("Controllers devem usar CommandHandlers, não Aggregates diretamente");

            rule.check(importedClasses);
        }

        @Test
        @DisplayName("Domínio não deve depender de frameworks (exceto Jackson para serialização)")
        void domainShouldNotDependOnFrameworks() {
            ArchRule rule = noClasses()
                    .that().resideInAPackage("..domain..")
                    .should().dependOnClassesThat()
                    .resideInAnyPackage("org.springframework..", "jakarta.persistence..")
                    .because("Domínio deve ser independente de frameworks (Clean Architecture)");

            rule.check(importedClasses);
        }

        @Test
        @DisplayName("Classes de serviço devem ter anotação @Service ou @Component")
        void serviceClassesShouldBeAnnotated() {
            ArchRule rule = classes()
                    .that().resideInAPackage("..application.service..")
                    .and().areNotInterfaces()
                    .should().beAnnotatedWith(Service.class)
                    .orShould().beAnnotatedWith(Component.class)
                    .because("Services e Handlers de aplicação devem ser gerenciados pelo Spring");

            rule.check(importedClasses);
        }
    }
}

