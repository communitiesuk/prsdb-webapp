package uk.gov.communities.prsdb.webapp.config.mappings

import org.springframework.web.servlet.mvc.condition.RequestCondition
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.AvailableWhenFeatureFlagDisabled
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.AvailableWhenFeatureFlagEnabled
import uk.gov.communities.prsdb.webapp.config.conditions.FeatureFlaggedRequestCondition
import uk.gov.communities.prsdb.webapp.config.conditions.InverseFeatureFlaggedRequestCondition
import uk.gov.communities.prsdb.webapp.config.managers.FeatureFlagManager
import java.lang.reflect.Method

class FeatureFlagHandlerMapping(
    private val featureFlagManager: FeatureFlagManager,
) : RequestMappingHandlerMapping() {
    override fun getCustomMethodCondition(method: Method): RequestCondition<*>? {
        val methodEnabledByFeatureFlag = method.getAnnotation(AvailableWhenFeatureFlagEnabled::class.java)
        methodEnabledByFeatureFlag?.let {
            return FeatureFlaggedRequestCondition(it.flagName, featureFlagManager)
        }

        val methodDisabledByFeatureFlag = method.getAnnotation(AvailableWhenFeatureFlagDisabled::class.java)
        return methodDisabledByFeatureFlag?.let {
            return InverseFeatureFlaggedRequestCondition(it.flagName, featureFlagManager)
        }
    }
}
