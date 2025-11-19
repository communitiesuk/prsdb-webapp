package uk.gov.communities.prsdb.webapp.config

import org.springframework.context.ApplicationListener
import org.springframework.context.event.ContextRefreshedEvent
import org.springframework.stereotype.Component
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.AvailableWhenFeatureFlagDisabled
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.AvailableWhenFeatureFlagEnabled

@Component
class FeatureFlagAnnotationValidator(
    private val requestMappingHandlerMapping: RequestMappingHandlerMapping,
) : ApplicationListener<ContextRefreshedEvent> {
    override fun onApplicationEvent(event: ContextRefreshedEvent) {
        val conflicts =
            requestMappingHandlerMapping.handlerMethods
                .filter { (_, handlerMethod) ->
                    val method = handlerMethod.method
                    val hasEnabled = method.getAnnotation(AvailableWhenFeatureFlagEnabled::class.java) != null
                    val hasDisabled = method.getAnnotation(AvailableWhenFeatureFlagDisabled::class.java) != null
                    hasEnabled && hasDisabled
                }.map { (mappingInfo, handlerMethod) ->
                    "${handlerMethod.beanType.name}#${handlerMethod.method.name} " +
                        "-> ${mappingInfo.patternsCondition?.patterns ?: mappingInfo}"
                }

        if (conflicts.isNotEmpty()) {
            throw IllegalStateException(
                "Conflicting feature flag annotations detected. A handler method must not have both " +
                    "@AvailableWhenFeatureFlagEnabled and @AvailableWhenFeatureFlagDisabled:\n" +
                    conflicts.joinToString("\n"),
            )
        }
    }
}
