package uk.gov.communities.prsdb.webapp.annotations.taskAnnotations

import org.springframework.context.annotation.Conditional
import org.springframework.core.annotation.AliasFor
import org.springframework.stereotype.Service

@Service
@Conditional(ScanProcessingOnly::class)
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class PrsdbScanProcessingService(
    @get:AliasFor(annotation = Service::class) val value: String = "",
)
