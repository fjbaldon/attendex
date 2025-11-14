package com.github.fjbaldon.attendex.platform;

import com.github.fjbaldon.attendex.platform.identity.CustomUserDetails;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.RestController;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

@AnalyzeClasses(packages = "com.github.fjbaldon.attendex.platform", importOptions = ImportOption.DoNotIncludeTests.class)
public class ArchitectureTest {

    private static final String BASE_PACKAGE = "com.github.fjbaldon.attendex.platform";

    @ArchTest
    public static final ArchRule public_classes_must_be_facades_dtos_or_events =
            classes()
                    .that().resideInAPackage(BASE_PACKAGE + ".(*)..")
                    .and().arePublic()
                    .should().haveSimpleNameEndingWith("Facade")
                    .orShould().resideInAPackage("..dto..")
                    .orShould().resideInAPackage("..events..")
                    .orShould().beAssignableTo(CustomUserDetails.class)
                    .orShould().haveSimpleNameEndingWith("Scheduler")
                    .because("Modules should only expose their Facade, DTOs, or Events as their public API. Schedulers and CustomUserDetails are allowed exceptions for framework integration.");

    @ArchTest
    public static final ArchRule dtos_and_events_must_be_public =
            classes()
                    .that().resideInAnyPackage("..dto..", "..events..")
                    .should().bePublic()
                    .because("DTOs and Events are part of the public API and must be accessible by other modules.");

    @ArchTest
    public static final ArchRule internals_must_be_package_private =
            classes()
                    .that().areAnnotatedWith(RestController.class)
                    .or().areAnnotatedWith(Entity.class)
                    .or().areAssignableTo(Repository.class)
                    .should().bePackagePrivate()
                    .because("Internal components like Controllers, Entities, and Repositories should not be exposed outside the module.");

    @ArchTest
    public static final ArchRule controllers_should_only_depend_on_facades =
            noClasses()
                    .that().areAnnotatedWith(RestController.class)
                    .should().dependOnClassesThat().areAnnotatedWith(Repository.class)
                    .orShould().dependOnClassesThat().areAnnotatedWith(Entity.class)
                    .because("Controllers must be simple and delegate all logic to Facades, not interact with persistence directly.");

    @ArchTest
    public static final ArchRule entities_must_adhere_to_table_naming_convention =
            classes()
                    .that().areAnnotatedWith(Entity.class)
                    .should(haveCorrectTableName());

    private static ArchCondition<JavaClass> haveCorrectTableName() {
        return new ArchCondition<JavaClass>("have a @Table annotation with the name 'module_entity'") {
            @Override
            public void check(JavaClass javaClass, ConditionEvents events) {
                String moduleName = javaClass.getPackageName().replace(BASE_PACKAGE + ".", "").split("\\.")[0];
                String entityName = javaClass.getSimpleName().toLowerCase();
                String expectedTableName = moduleName + "_" + entityName;

                if (javaClass.isAnnotatedWith(Table.class)) {
                    Table tableAnnotation = javaClass.getAnnotationOfType(Table.class);
                    String actualTableName = tableAnnotation.name();
                    if (!expectedTableName.equals(actualTableName)) {
                        String message = String.format(
                                "Entity %s in module '%s' is mapped to table '%s' but should be mapped to '%s'.",
                                javaClass.getSimpleName(), moduleName, actualTableName, expectedTableName
                        );
                        events.add(SimpleConditionEvent.violated(javaClass, message));
                    }
                } else {
                    String message = String.format(
                            "Entity %s is missing the @Table annotation.", javaClass.getSimpleName()
                    );
                    events.add(SimpleConditionEvent.violated(javaClass, message));
                }
            }
        };
    }
}
