package uk.gov.communities.prsdb.webapp.config

import org.springframework.context.ApplicationListener
import org.springframework.context.event.ContextRefreshedEvent
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.AvailableWhenFeatureDisabled
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.AvailableWhenFeatureEnabled
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebComponent

@PrsdbWebComponent
class FeatureFlagAnnotationValidator(
    private val requestMappingHandlerMapping: RequestMappingHandlerMapping,
) : ApplicationListener<ContextRefreshedEvent> {
    override fun onApplicationEvent(event: ContextRefreshedEvent) {
        val conflicts =
            requestMappingHandlerMapping.handlerMethods
                .filter { (_, handlerMethod) ->
                    val method = handlerMethod.method
                    val hasEnabled = method.getAnnotation(AvailableWhenFeatureEnabled::class.java) != null
                    val hasDisabled = method.getAnnotation(AvailableWhenFeatureDisabled::class.java) != null
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
