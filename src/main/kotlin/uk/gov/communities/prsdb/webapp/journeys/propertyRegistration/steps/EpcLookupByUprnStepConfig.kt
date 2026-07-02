package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.AbstractInternalStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.EpcState
import uk.gov.communities.prsdb.webapp.services.EpcLookupService

@JourneyFrameworkComponent
class EpcLookupByUprnStepConfig(
    private val epcLookupService: EpcLookupService,
) : AbstractInternalStepConfig<EpcLookupByUprnMode, EpcState>() {
    override fun mode(state: EpcState): EpcLookupByUprnMode? =
        if (state.epcRetrievedByUprn != null) EpcLookupByUprnMode.EPC_FOUND else EpcLookupByUprnMode.NOT_FOUND

    override fun afterStepIsReached(state: EpcState) {
        val previousEpcRetrievedByUprn = state.epcRetrievedByUprn
        val newEpcRetrievedByUprn = state.uprn?.let { epcLookupService.getEpcByUprn(it) }
        state.epcRetrievedByUprn = newEpcRetrievedByUprn
        if (newEpcRetrievedByUprn != previousEpcRetrievedByUprn) {
            state.epcRetrievedByUprnUpdatedSinceUserReview = true
        }
    }
}

@JourneyFrameworkComponent
final class EpcLookupByUprnStep(
    stepConfig: EpcLookupByUprnStepConfig,
) : JourneyStep.InternalStep<EpcLookupByUprnMode, EpcState>(stepConfig)

enum class EpcLookupByUprnMode {
    EPC_FOUND,
    NOT_FOUND,
}
