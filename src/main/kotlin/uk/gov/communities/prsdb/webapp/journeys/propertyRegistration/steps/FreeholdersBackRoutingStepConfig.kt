package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.AbstractInternalStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyState
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep
import uk.gov.communities.prsdb.webapp.journeys.shared.AnyMembers
import uk.gov.communities.prsdb.webapp.models.dataModels.FreeholderDataModel

// Internal routing step that sits in the outer freeholders task immediately after the reusable freeholders members
// sub-journey. It reads the collected freeholders (held by the outer task via its own state) and reports whether any
// remain, so the outer task can route forward when freeholders exist or back to the freeholders intro once the list
// has been emptied.
@JourneyFrameworkComponent
class FreeholdersBackRoutingStepConfig : AbstractInternalStepConfig<AnyMembers, JourneyState>() {
    private lateinit var freeholdersList: () -> Map<Int, FreeholderDataModel>?

    fun usingFreeholdersList(freeholdersList: () -> Map<Int, FreeholderDataModel>?): FreeholdersBackRoutingStepConfig {
        this.freeholdersList = freeholdersList
        return this
    }

    override fun isSubClassInitialised() = ::freeholdersList.isInitialized

    override fun mode(state: JourneyState): AnyMembers =
        if (freeholdersList().isNullOrEmpty()) AnyMembers.NO_MEMBERS else AnyMembers.SOME_MEMBERS
}

@JourneyFrameworkComponent
class FreeholdersBackRoutingStep(
    stepConfig: FreeholdersBackRoutingStepConfig,
) : JourneyStep.InternalStep<AnyMembers, JourneyState>(stepConfig)
