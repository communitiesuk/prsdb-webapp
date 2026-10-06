package uk.gov.communities.prsdb.webapp.config.featureFlags

import org.ff4j.aop.Flip
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider
import org.springframework.core.type.filter.TypeFilter
import org.springframework.stereotype.Service

class PrsdbFlipImplementationTests {
    @Test
    fun `implementations of PrsdbFlip interfaces name their beans with a Service annotation on the class`() {
        // Arrange
        val scanner = ClassPathScanningCandidateComponentProvider(false)
        scanner.addIncludeFilter(
            TypeFilter { metadataReader, metadataReaderFactory ->
                metadataReader.classMetadata.interfaceNames.any { interfaceName ->
                    val interfaceMetadata = metadataReaderFactory.getMetadataReader(interfaceName).annotationMetadata
                    interfaceMetadata.isAnnotated(Flip::class.java.name) || interfaceMetadata.hasAnnotatedMethods(Flip::class.java.name)
                }
            },
        )

        // Act
        val implementations =
            scanner
                .findCandidateComponents("uk.gov.communities.prsdb.webapp")
                .map { Class.forName(it.beanClassName) }

        // Assert
        assertTrue(implementations.isNotEmpty(), "No implementations of @PrsdbFlip interfaces were found")
        implementations.forEach { implementation ->
            assertFalse(
                implementation.getAnnotation(Service::class.java)?.value.isNullOrBlank(),
                "${implementation.simpleName} needs @Service(\"bean-name\") on the class itself so FF4J can read its bean name",
            )
        }
    }
}
