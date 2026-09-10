package uk.gov.communities.prsdb.webapp.annotations.webAnnotations

import org.springframework.context.annotation.Condition
import org.springframework.context.annotation.ConditionContext
import org.springframework.core.type.AnnotatedTypeMetadata

class FeatureFlagOverridesEnabled : Condition {
    override fun matches(
        context: ConditionContext,
        metadata: AnnotatedTypeMetadata,
    ): Boolean = context.environment.getProperty("features.overrides-enabled", Boolean::class.java, false)
}
