package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.exceptions.NotNullFormModelValueIsNullException
import uk.gov.communities.prsdb.webapp.journeys.AbstractInternalStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.EpcState

@JourneyFrameworkComponent
class EpcEnergyRatingCheckStepConfig : AbstractInternalStepConfig<EpcEnergyRatingCheckMode, EpcState>() {
    override fun mode(state: EpcState): EpcEnergyRatingCheckMode? {
        val epcDetails =
            state.acceptedEpc
                ?: throw NotNullFormModelValueIsNullException("acceptedEpc must be present before evaluating EPC energy rating")
        return if (epcDetails.isEnergyRatingEOrBetter()) {
            EpcEnergyRatingCheckMode.MEETS_REQUIREMENTS
        } else {
            EpcEnergyRatingCheckMode.BELOW_THRESHOLD
        }
    }
}

@JourneyFrameworkComponent
final class EpcEnergyRatingCheckStep(
    stepConfig: EpcEnergyRatingCheckStepConfig,
) : JourneyStep.InternalStep<EpcEnergyRatingCheckMode, EpcState>(stepConfig)

enum class EpcEnergyRatingCheckMode {
    MEETS_REQUIREMENTS,
    BELOW_THRESHOLD,
}
