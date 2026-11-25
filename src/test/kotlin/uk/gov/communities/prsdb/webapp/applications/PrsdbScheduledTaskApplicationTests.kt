package uk.gov.communities.prsdb.webapp.applications

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.core.io.support.PathMatchingResourcePatternResolver
import org.springframework.core.type.classreading.CachingMetadataReaderFactory
import uk.gov.communities.prsdb.webapp.annotations.taskAnnotations.PrsdbScheduledTask
import uk.gov.communities.prsdb.webapp.application.DefaultScheduledTaskApplicationRunner

class PrsdbScheduledTaskApplicationTests {
    @Test
    fun `all non-default scheduled tasks have names ending with scheduled-task`() {
        val metadataReaderFactory = CachingMetadataReaderFactory()
        val taskNames =
            PathMatchingResourcePatternResolver()
                .getResources("classpath*:uk/gov/communities/prsdb/webapp/**/*.class")
                .mapNotNull { resource ->
                    val metadataReader = metadataReaderFactory.getMetadataReader(resource)
                    if (metadataReader.classMetadata.className == DefaultScheduledTaskApplicationRunner::class.java.name) {
                        null
                    } else {
                        metadataReader.annotationMetadata
                            .getAnnotationAttributes(PrsdbScheduledTask::class.java.name)
                            ?.get("name") as? String
                    }
                }
        val invalidTaskNames = taskNames.filterNot { it.endsWith("scheduled-task") }

        assertTrue(
            invalidTaskNames.isEmpty(),
            "Scheduled task names must end with 'scheduled-task'. See the prsdb-infra repository's ReadMe for more details. " +
                "Invalid names: $invalidTaskNames",
        )
    }
}
