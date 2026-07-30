package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.exceptions.NotNullFormModelValueIsNullException
import uk.gov.communities.prsdb.webapp.journeys.AbstractInternalStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.EpcDetailState

@JourneyFrameworkComponent
class EpcAgeCheckStepConfig : AbstractInternalStepConfig<EpcAgeCheckMode, EpcDetailState>() {
    override fun mode(state: EpcDetailState): EpcAgeCheckMode? {
        val epcDetails =
            state.acceptedEpc
                ?: throw NotNullFormModelValueIsNullException("acceptedEpc must be present before evaluating EPC age")
        return if (epcDetails.isPastExpiryDate()) EpcAgeCheckMode.EPC_OLDER_THAN_10_YEARS else EpcAgeCheckMode.EPC_10_YEARS_OR_NEWER
    }
}

@JourneyFrameworkComponent
final class EpcAgeCheckStep(
    stepConfig: EpcAgeCheckStepConfig,
) : JourneyStep.InternalStep<EpcAgeCheckMode, EpcDetailState>(stepConfig)

enum class EpcAgeCheckMode {
    EPC_10_YEARS_OR_NEWER,
    EPC_OLDER_THAN_10_YEARS,
}
