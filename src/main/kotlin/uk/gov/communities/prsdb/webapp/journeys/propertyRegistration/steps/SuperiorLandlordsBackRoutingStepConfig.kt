package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.AbstractInternalStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyState
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep
import uk.gov.communities.prsdb.webapp.journeys.shared.AnyMembers
import uk.gov.communities.prsdb.webapp.models.dataModels.SuperiorLandlordDataModel

// Internal routing step that sits in the outer superior landlords task immediately after the superior landlords details
// sub-journey. It reads the collected superior landlords (held by the outer task via its own state) and reports whether
// any remain, so the outer task can route forward when superior landlords exist or back to the "anyone in the ownership
// chain" question once the list has been emptied.
@JourneyFrameworkComponent
class SuperiorLandlordsBackRoutingStepConfig : AbstractInternalStepConfig<AnyMembers, JourneyState>() {
    private lateinit var superiorLandlordsList: () -> Map<Int, SuperiorLandlordDataModel>?

    fun usingSuperiorLandlordsList(
        superiorLandlordsList: () -> Map<Int, SuperiorLandlordDataModel>?,
    ): SuperiorLandlordsBackRoutingStepConfig {
        this.superiorLandlordsList = superiorLandlordsList
        return this
    }

    override fun isSubClassInitialised() = ::superiorLandlordsList.isInitialized

    override fun mode(state: JourneyState): AnyMembers =
        if (superiorLandlordsList().isNullOrEmpty()) AnyMembers.NO_MEMBERS else AnyMembers.SOME_MEMBERS
}

@JourneyFrameworkComponent
class SuperiorLandlordsBackRoutingStep(
    stepConfig: SuperiorLandlordsBackRoutingStepConfig,
) : JourneyStep.InternalStep<AnyMembers, JourneyState>(stepConfig)
