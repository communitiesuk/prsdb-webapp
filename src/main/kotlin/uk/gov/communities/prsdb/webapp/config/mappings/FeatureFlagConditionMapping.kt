package uk.gov.communities.prsdb.webapp.config.mappings

import org.springframework.web.servlet.mvc.condition.RequestCondition
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.AvailableWhenFeatureFlagDisabled
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.AvailableWhenFeatureFlagEnabled
import uk.gov.communities.prsdb.webapp.config.conditions.FeatureFlaggedRequestCondition
import uk.gov.communities.prsdb.webapp.config.conditions.InverseFeatureFlaggedRequestCondition
import uk.gov.communities.prsdb.webapp.config.managers.FeatureFlagManager
import java.lang.reflect.Method

class FeatureFlagConditionMapping(
    private val featureFlagManager: FeatureFlagManager,
) : RequestMappingHandlerMapping() {
    override fun getCustomMethodCondition(method: Method): RequestCondition<*>? {
        val annotationToEnableMethod = method.getAnnotation(AvailableWhenFeatureFlagEnabled::class.java)
        annotationToEnableMethod?.let {
            return FeatureFlaggedRequestCondition(it.flagName, featureFlagManager)
        }

        val annotationToDisableMethod = method.getAnnotation(AvailableWhenFeatureFlagDisabled::class.java)
        return annotationToDisableMethod?.let {
            return InverseFeatureFlaggedRequestCondition(it.flagName, featureFlagManager)
        }
    }
}
