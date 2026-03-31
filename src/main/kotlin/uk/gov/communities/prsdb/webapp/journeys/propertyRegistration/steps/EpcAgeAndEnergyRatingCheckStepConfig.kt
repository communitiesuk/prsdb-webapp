package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.exceptions.NotNullFormModelValueIsNullException
import uk.gov.communities.prsdb.webapp.journeys.AbstractInternalStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.EpcState

@JourneyFrameworkComponent
class EpcAgeAndEnergyRatingCheckStepConfig : AbstractInternalStepConfig<EpcAgeAndEnergyRatingCheckMode, EpcState>() {
    override fun mode(state: EpcState): EpcAgeAndEnergyRatingCheckMode? {
        val epcDetails =
            state.acceptedEpc
                ?: throw NotNullFormModelValueIsNullException("acceptedEpc must be present before evaluating EPC age and energy rating")
        if (epcDetails.isPastExpiryDate()) return EpcAgeAndEnergyRatingCheckMode.EPC_OLDER_THAN_10_YEARS
        if (!(epcDetails.isEnergyRatingEOrBetter())) return EpcAgeAndEnergyRatingCheckMode.EPC_LOW_ENERGY_RATING
        return EpcAgeAndEnergyRatingCheckMode.EPC_COMPLIANT
    }
}

@JourneyFrameworkComponent
final class EpcAgeAndEnergyRatingCheckStep(
    stepConfig: EpcAgeAndEnergyRatingCheckStepConfig,
) : JourneyStep.InternalStep<EpcAgeAndEnergyRatingCheckMode, EpcState>(stepConfig)

enum class EpcAgeAndEnergyRatingCheckMode {
    EPC_COMPLIANT,
    EPC_OLDER_THAN_10_YEARS,
    EPC_LOW_ENERGY_RATING,
}
