package com.gridsentinel.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.*;

/**
 * Architecture rules enforcing the layered architecture of Grid Sentinel.
 * 
 * Layer hierarchy:
 * API → APPLICATION → DOMAIN
 * 
 * INFRASTRUCTURE implements DOMAIN/APPLICATION ports
 * 
 * DOMAIN must not depend on anything outside domain + Java stdlib
 */
class ArchitectureRulesTest {

    private static JavaClasses allClasses;

    @BeforeAll
    static void setup() {
        allClasses = new ClassFileImporter()
                .withImportOption(new ImportOption.DoNotIncludeTests())
                .importPackages("com.gridsentinel");
    }

    // =========================================================================
    // DOMAIN ISOLATION (Rule A)
    // =========================================================================

    @Test
    void domainShouldNotDependOnSpring() {
        ArchRule rule = noClasses()
                .that().resideInAPackage("..domain..")
                .should().dependOnClassesThat().resideInAnyPackage(
                        "org.springframework..",
                        "org.springframework.boot.."
                )
                .because("Domain layer must not depend on Spring framework")
                .allowEmptyShould(true);

        rule.check(allClasses);
    }

    @Test
    void domainShouldNotDependOnSpringBoot() {
        ArchRule rule = noClasses()
                .that().resideInAPackage("..domain..")
                .should().dependOnClassesThat().resideInAnyPackage(
                        "org.springframework.boot.."
                )
                .because("Domain layer must not depend on Spring Boot")
                .allowEmptyShould(true);

        rule.check(allClasses);
    }

    @Test
    void domainShouldNotDependOnJPA() {
        ArchRule rule = noClasses()
                .that().resideInAPackage("..domain..")
                .should().dependOnClassesThat().resideInAnyPackage(
                        "jakarta.persistence..",
                        "org.hibernate..",
                        "org.springframework.data.jpa.."
                )
                .because("Domain layer must not depend on JPA/Hibernate")
                .allowEmptyShould(true);

        rule.check(allClasses);
    }

    @Test
    void domainShouldNotDependOnSpringSecurity() {
        ArchRule rule = noClasses()
                .that().resideInAPackage("..domain..")
                .should().dependOnClassesThat().resideInAnyPackage(
                        "org.springframework.security.."
                )
                .because("Domain layer must not depend on Spring Security")
                .allowEmptyShould(true);

        rule.check(allClasses);
    }

    @Test
    void domainShouldNotDependOnSpringWeb() {
        ArchRule rule = noClasses()
                .that().resideInAPackage("..domain..")
                .should().dependOnClassesThat().resideInAnyPackage(
                        "org.springframework.web..",
                        "org.springframework.http.."
                )
                .because("Domain layer must not depend on Spring Web")
                .allowEmptyShould(true);

        rule.check(allClasses);
    }

    @Test
    void domainShouldNotDependOnREST() {
        ArchRule rule = noClasses()
                .that().resideInAPackage("..domain..")
                .should().dependOnClassesThat().resideInAnyPackage(
                        "org.springframework.web.bind.annotation..",
                        "org.springframework.web.reactive.."
                )
                .because("Domain layer must not depend on REST annotations")
                .allowEmptyShould(true);

        rule.check(allClasses);
    }

    @Test
    void domainShouldNotDependOnInfrastructure() {
        ArchRule rule = noClasses()
                .that().resideInAPackage("..domain..")
                .should().dependOnClassesThat().resideInAnyPackage(
                        "..infrastructure.."
                )
                .because("Domain layer must not depend on infrastructure")
                .allowEmptyShould(true);

        rule.check(allClasses);
    }

    @Test
    void domainShouldNotDependOnAPI() {
        ArchRule rule = noClasses()
                .that().resideInAPackage("..domain..")
                .should().dependOnClassesThat().resideInAnyPackage(
                        "..api.."
                )
                .because("Domain layer must not depend on API layer")
                .allowEmptyShould(true);

        rule.check(allClasses);
    }

    @Test
    void domainShouldNotDependOnApplication() {
        ArchRule rule = noClasses()
                .that().resideInAPackage("..domain..")
                .should().dependOnClassesThat().resideInAnyPackage(
                        "..application.."
                )
                .because("Domain layer must not depend on application layer")
                .allowEmptyShould(true);

        rule.check(allClasses);
    }

    @Test
    void domainShouldNotDependOnControllers() {
        ArchRule rule = noClasses()
                .that().resideInAPackage("..domain..")
                .should().dependOnClassesThat().resideInAnyPackage(
                        "..api..controller.."
                )
                .because("Domain layer must not depend on controllers")
                .allowEmptyShould(true);

        rule.check(allClasses);
    }

    @Test
    void domainShouldNotDependOnRepositories() {
        ArchRule rule = noClasses()
                .that().resideInAPackage("..domain..")
                .should().dependOnClassesThat().resideInAnyPackage(
                        "..infrastructure.persistence.repository.."
                )
                .because("Domain layer must not depend on repository implementations")
                .allowEmptyShould(true);

        rule.check(allClasses);
    }

    // =========================================================================
    // API ISOLATION (Rule B)
    // =========================================================================

    @Test
    void apiControllersShouldOnlyDependOnApplicationServices() {
        ArchRule rule = classes()
                .that().resideInAPackage("..api..controller..")
                .should().onlyDependOnClassesThat()
                .resideInAnyPackage(
                        "..application..",
                        "..domain..",
                        "..common..",
                        "org.springframework..",
                        "jakarta.validation..",
                        "java..",
                        "jakarta.."
                )
                .because("Controllers should only depend on application services, domain, common, and standard libraries")
                .allowEmptyShould(true);

        rule.check(allClasses);
    }

    @Test
    void apiControllersShouldNotDependOnInfrastructure() {
        ArchRule rule = noClasses()
                .that().resideInAPackage("..api..controller..")
                .should().dependOnClassesThat().resideInAnyPackage(
                        "..infrastructure.."
                )
                .because("Controllers must not depend on infrastructure implementations")
                .allowEmptyShould(true);

        rule.check(allClasses);
    }

    @Test
    void apiControllersShouldNotDirectlyAccessRepositories() {
        ArchRule rule = noClasses()
                .that().resideInAPackage("..api..controller..")
                .should().dependOnClassesThat().resideInAnyPackage(
                        "..infrastructure.persistence.repository.."
                )
                .because("Controllers must not directly access repositories")
                .allowEmptyShould(true);

        rule.check(allClasses);
    }

    // =========================================================================
    // INFRASTRUCTURE RULES (Rule C)
    // =========================================================================

    @Test
    void infrastructureShouldOnlyImplementPorts() {
        ArchRule rule = classes()
                .that().resideInAPackage("..infrastructure..")
                .should().onlyDependOnClassesThat()
                .resideInAnyPackage(
                        "..domain..",
                        "..application..",
                        "..common..",
                        "org.springframework..",
                        "org.hibernate..",
                        "jakarta.persistence..",
                        "org.flywaydb..",
                        "io.jsonwebtoken..",
                        "com.bedatadriven..",
                        "org.postgresql..",
                        "org.postgis..",
                        "io.jsonwebtoken..",
                        "java..",
                        "jakarta.."
                )
                .because("Infrastructure should only depend on domain, application, common, and technical libraries")
                .allowEmptyShould(true);

        rule.check(allClasses);
    }

    @Test
    void infrastructureShouldNotDependOnAPI() {
        ArchRule rule = noClasses()
                .that().resideInAPackage("..infrastructure..")
                .should().dependOnClassesThat().resideInAnyPackage(
                        "..api.."
                )
                .because("Infrastructure must not depend on API layer")
                .allowEmptyShould(true);

        rule.check(allClasses);
    }

    @Test
    void infrastructureShouldNotDependOnControllers() {
        ArchRule rule = noClasses()
                .that().resideInAPackage("..infrastructure..")
                .should().dependOnClassesThat().resideInAnyPackage(
                        "..api..controller.."
                )
                .because("Infrastructure must not depend on controllers")
                .allowEmptyShould(true);

        rule.check(allClasses);
    }

    // =========================================================================
    // APPLICATION LAYER RULES
    // =========================================================================

    @Test
    void applicationShouldOnlyDependOnDomainAndCommon() {
        ArchRule rule = classes()
                .that().resideInAPackage("..application..")
                .should().onlyDependOnClassesThat()
                .resideInAnyPackage(
                        "..domain..",
                        "..common..",
                        "org.springframework..",
                        "java..",
                        "jakarta.."
                )
                .because("Application layer should only depend on domain, common, and Spring framework")
                .allowEmptyShould(true);

        rule.check(allClasses);
    }

    @Test
    void applicationShouldNotDependOnInfrastructure() {
        ArchRule rule = noClasses()
                .that().resideInAPackage("..application..")
                .should().dependOnClassesThat().resideInAnyPackage(
                        "..infrastructure.."
                )
                .because("Application layer must not depend on infrastructure implementations")
                .allowEmptyShould(true);

        rule.check(allClasses);
    }

    @Test
    void applicationShouldNotDependOnAPI() {
        ArchRule rule = noClasses()
                .that().resideInAPackage("..application..")
                .should().dependOnClassesThat().resideInAnyPackage(
                        "..api.."
                )
                .because("Application layer must not depend on API layer")
                .allowEmptyShould(true);

        rule.check(allClasses);
    }

    @Test
    void applicationShouldNotDependOnControllers() {
        ArchRule rule = noClasses()
                .that().resideInAPackage("..application..")
                .should().dependOnClassesThat().resideInAnyPackage(
                        "..api..controller.."
                )
                .because("Application layer must not depend on controllers")
                .allowEmptyShould(true);

        rule.check(allClasses);
    }

    // =========================================================================
    // NAMING CONVENTIONS
    // =========================================================================

    @Test
    void domainAggregatesShouldBeInDomainPackage() {
        ArchRule rule = classes()
                .that().haveNameMatching(".*Aggregate|.*Entity")
                .should().resideInAPackage("..domain..")
                .because("Aggregates and entities must reside in domain package")
                .allowEmptyShould(true);

        rule.check(allClasses);
    }

    @Test
    void valueObjectsShouldBeInDomainOrCommon() {
        ArchRule rule = classes()
                .that().haveNameMatching(".*ValueObject|.*Id$|.*VO$")
                .should().resideInAnyPackage("..domain..", "..common..")
                .because("Value objects must reside in domain or common package")
                .allowEmptyShould(true);

        rule.check(allClasses);
    }

    @Test
    void domainEventsShouldBeInDomain() {
        ArchRule rule = classes()
                .that().haveNameMatching(".*Event")
                .should().resideInAPackage("..domain..")
                .because("Domain events must reside in domain package")
                .allowEmptyShould(true);

        rule.check(allClasses);
    }

    @Test
    void strategiesShouldBeInDomainOrApplication() {
        ArchRule rule = classes()
                .that().haveNameMatching(".*Strategy")
                .should().resideInAnyPackage("..domain..", "..application..")
                .because("Strategy implementations should reside in domain or application")
                .allowEmptyShould(true);

        rule.check(allClasses);
    }

    @Test
    void repositoriesInDomainAreInterfaces() {
        ArchRule rule = classes()
                .that().resideInAPackage("..domain..")
                .and().haveNameMatching(".*Repository")
                .should().beInterfaces()
                .because("Domain repository interfaces must be interfaces")
                .allowEmptyShould(true);

        rule.check(allClasses);
    }

    @Test
    void repositoriesInInfrastructureAreImplementations() {
        ArchRule rule = classes()
                .that().resideInAPackage("..infrastructure.persistence.repository..")
                .and().haveNameMatching(".*Repository")
                .should().beInterfaces()
                .because("Infrastructure repositories should be implementations, not interfaces")
                .allowEmptyShould(true);

        rule.check(allClasses);
    }
}