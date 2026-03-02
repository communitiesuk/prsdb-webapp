package uk.gov.communities.prsdb.webapp.journeys.landlordDeregistration.stepConfig

import org.springframework.security.core.context.SecurityContextHolder
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.AbstractInternalStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.InternalStep
import uk.gov.communities.prsdb.webapp.journeys.landlordDeregistration.LandlordDeregistrationJourneyState
import uk.gov.communities.prsdb.webapp.services.PropertyOwnershipService

@JourneyFrameworkComponent
class CheckHasRegisteredPropertiesStepConfig(
    private val propertyOwnershipService: PropertyOwnershipService,
) : AbstractInternalStepConfig<HasRegisteredPropertiesStatus, LandlordDeregistrationJourneyState>() {
    override fun mode(state: LandlordDeregistrationJourneyState): HasRegisteredPropertiesStatus {
        val baseUserId = SecurityContextHolder.getContext().authentication.name
        return if (propertyOwnershipService.doesLandlordHaveRegisteredProperties(baseUserId)) {
            HasRegisteredPropertiesStatus.HAS_PROPERTIES
        } else {
            HasRegisteredPropertiesStatus.NO_PROPERTIES
        }
    }
}

@JourneyFrameworkComponent
class CheckHasRegisteredPropertiesStep(
    stepConfig: CheckHasRegisteredPropertiesStepConfig,
) : InternalStep<HasRegisteredPropertiesStatus, LandlordDeregistrationJourneyState>(stepConfig)

enum class HasRegisteredPropertiesStatus {
    HAS_PROPERTIES,
    NO_PROPERTIES,
}
