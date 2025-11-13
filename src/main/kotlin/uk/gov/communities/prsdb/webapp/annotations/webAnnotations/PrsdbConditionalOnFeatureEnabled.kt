package uk.gov.communities.prsdb.webapp.annotations.webAnnotations

import org.springframework.context.annotation.Condition
import org.springframework.context.annotation.ConditionContext
import org.springframework.context.annotation.Conditional
import org.springframework.core.type.AnnotatedTypeMetadata
import uk.gov.communities.prsdb.webapp.constants.EXAMPLE_FEATURE_FLAG_ONE

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@Conditional(FeatureCondition::class)
annotation class PrsdbConditionalOnFeatureEnabled(
    val featureName: String,
)

class FeatureCondition : Condition {
    override fun matches(
        context: ConditionContext,
        metadata: AnnotatedTypeMetadata,
    ): Boolean {
        val attributes = metadata.getAnnotationAttributes(PrsdbConditionalOnFeatureEnabled::class.java.name)
        val featureName = attributes?.get("featureName") as? String ?: return false

/*        val featureFlagManager = context.beanFactory?.getBean(FeatureFlagManager::class.java)
        return featureFlagManager?.checkFeature(featureName) ?: false*/

        return featureName == EXAMPLE_FEATURE_FLAG_ONE
    }
}
