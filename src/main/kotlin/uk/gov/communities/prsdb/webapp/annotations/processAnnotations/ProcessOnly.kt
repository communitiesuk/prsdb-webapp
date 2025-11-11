package uk.gov.communities.prsdb.webapp.annotations.processAnnotations

import org.springframework.context.annotation.Condition
import org.springframework.context.annotation.ConditionContext
import org.springframework.core.type.AnnotatedTypeMetadata

class ProcessOnly : Condition {
    override fun matches(
        context: ConditionContext,
        metadata: AnnotatedTypeMetadata,
    ): Boolean = context.environment.activeProfiles.contains("web-server-deactivated")
}
