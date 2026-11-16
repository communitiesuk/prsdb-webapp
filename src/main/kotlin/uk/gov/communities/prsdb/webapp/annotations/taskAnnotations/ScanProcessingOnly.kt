package uk.gov.communities.prsdb.webapp.annotations.taskAnnotations

import org.springframework.context.annotation.Condition
import org.springframework.context.annotation.ConditionContext
import org.springframework.core.env.Profiles
import org.springframework.core.type.AnnotatedTypeMetadata
import uk.gov.communities.prsdb.webapp.config.ScanResultQueueListenerConfig.Companion.SCAN_RESULT_QUEUE_LISTENER_PROFILES

class ScanProcessingOnly : Condition {
    override fun matches(
        context: ConditionContext,
        metadata: AnnotatedTypeMetadata,
    ): Boolean =
        TaskOnly().matches(context, metadata) ||
            context.environment.acceptsProfiles(Profiles.of(SCAN_RESULT_QUEUE_LISTENER_PROFILES))
}
