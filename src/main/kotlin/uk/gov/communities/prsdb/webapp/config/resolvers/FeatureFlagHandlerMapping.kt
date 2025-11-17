package uk.gov.communities.prsdb.webapp.config.resolvers

import org.springframework.web.servlet.mvc.condition.RequestCondition
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.AvailableWhenFeatureFlagDisabled
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.AvailableWhenFeatureFlagEnabled
import uk.gov.communities.prsdb.webapp.services.FeatureFlagChecker
import java.lang.reflect.Method

class FeatureFlagHandlerMapping(
    private val featureFlagChecker: FeatureFlagChecker,
) : RequestMappingHandlerMapping() {
    override fun getCustomMethodCondition(method: Method): RequestCondition<*>? {
        val methodEnabledByFeatureFlag = method.getAnnotation(AvailableWhenFeatureFlagEnabled::class.java)
        methodEnabledByFeatureFlag?.let {
            return FeatureFlaggedRequestCondition(it.flagName, featureFlagChecker)
        }

        val methodDisabledByFeatureFlag = method.getAnnotation(AvailableWhenFeatureFlagDisabled::class.java)
        return methodDisabledByFeatureFlag?.let {
            return InverseFeatureFlaggedRequestCondition(it.flagName, featureFlagChecker)
        }
    }
}
