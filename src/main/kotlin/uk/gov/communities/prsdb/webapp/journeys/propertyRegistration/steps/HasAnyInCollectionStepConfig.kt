package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.AbstractInternalStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyState
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep
import uk.gov.communities.prsdb.webapp.journeys.shared.AnyMembers

@JourneyFrameworkComponent
class HasAnyInCollectionStepConfig : AbstractInternalStepConfig<AnyMembers, JourneyState>() {
    lateinit var collectionMap: Map<*, *>

    override fun mode(state: JourneyState) = if (collectionMap.isNotEmpty()) AnyMembers.SOME_MEMBERS else AnyMembers.NO_MEMBERS
}

@JourneyFrameworkComponent
final class HasAnyInCollectionStep(
    stepConfig: HasAnyInCollectionStepConfig,
) : JourneyStep.InternalStep<AnyMembers, JourneyState>(stepConfig)
