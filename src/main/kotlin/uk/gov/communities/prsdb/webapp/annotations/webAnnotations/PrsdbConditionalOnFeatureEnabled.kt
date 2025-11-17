package uk.gov.communities.prsdb.webapp.annotations.webAnnotations

import org.springframework.beans.factory.support.BeanDefinitionBuilder
import org.springframework.context.annotation.Condition
import org.springframework.context.annotation.ConditionContext
import org.springframework.context.annotation.Conditional
import org.springframework.core.type.AnnotatedTypeMetadata
import uk.gov.communities.prsdb.webapp.config.FeatureFlagConfig

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

        val registry = context.registry
        val beanName = "featureFlagConfigForConditionTag"

        // Ensure a bean definition exists early (so other condition checks can see it)
        if (!registry.containsBeanDefinition(beanName)) {
            val bd =
                BeanDefinitionBuilder
                    .genericBeanDefinition(FeatureFlagConfig::class.java)
                    .beanDefinition
            registry.registerBeanDefinition(beanName, bd)
        }

        // Try to resolve the bean from the bean factory (may be null in some phases)
        val beanFactory = context.beanFactory
        val featureFlagConfig =
            beanFactory?.let {
                if (it.containsBean(beanName)) it.getBean(beanName) as? FeatureFlagConfig else null
            }

        val manager = featureFlagConfig?.featureFlagManager()

        // If we have an instance use it; otherwise, check the bean definition existed and assume disabled fallback
        return manager?.let { m ->
            // consult FF4j API safely
            m.exist(featureName) && m.check(featureName)
        } ?: false
    }
}
