package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.exceptions.NotNullFormModelValueIsNullException
import uk.gov.communities.prsdb.webapp.journeys.AbstractInternalStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.EpcState

@JourneyFrameworkComponent
class EpcAgeCheckStepConfig : AbstractInternalStepConfig<EpcAgeCheckMode, EpcState>() {
    override fun mode(state: EpcState): EpcAgeCheckMode? {
        val epcDetails =
            state.acceptedEpc
                ?: throw NotNullFormModelValueIsNullException("acceptedEpc must be present before evaluating EPC age")
        return if (epcDetails.isPastExpiryDate()) EpcAgeCheckMode.EXPIRED else EpcAgeCheckMode.CURRENT
    }
}

@JourneyFrameworkComponent
final class EpcAgeCheckStep(
    stepConfig: EpcAgeCheckStepConfig,
) : JourneyStep.InternalStep<EpcAgeCheckMode, EpcState>(stepConfig)

enum class EpcAgeCheckMode {
    CURRENT,
    EXPIRED,
}
