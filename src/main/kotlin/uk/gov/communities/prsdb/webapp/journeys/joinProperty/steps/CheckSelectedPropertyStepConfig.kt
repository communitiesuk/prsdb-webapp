package uk.gov.communities.prsdb.webapp.journeys.joinProperty.steps

import org.springframework.security.core.context.SecurityContextHolder
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.exceptions.PrsdbWebException
import uk.gov.communities.prsdb.webapp.journeys.AbstractInternalStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.InternalStep
import uk.gov.communities.prsdb.webapp.journeys.joinProperty.states.JoinPropertyAddressSearchState
import uk.gov.communities.prsdb.webapp.services.AddressAvailabilityService

@JourneyFrameworkComponent
class CheckSelectedPropertyStepConfig(
    private val addressAvailabilityService: AddressAvailabilityService,
) : AbstractInternalStepConfig<SelectedPropertyCheckResult, JoinPropertyAddressSearchState>() {
    override fun mode(state: JoinPropertyAddressSearchState): SelectedPropertyCheckResult {
        val selectedOption =
            state.selectPropertyStep.formModel.selectedOption
                ?: throw PrsdbWebException("No property selected in select-property step")

        val uprn =
            state.getMatchingAddress(selectedOption)?.uprn
                ?: throw PrsdbWebException("Selected property has no UPRN")

        if (!addressAvailabilityService.isAddressOwned(uprn)) {
            return SelectedPropertyCheckResult.PROPERTY_NOT_REGISTERED
        }

        val baseUserId = SecurityContextHolder.getContext().authentication.name
        if (addressAvailabilityService.isAddressOwnedByUser(uprn, baseUserId)) {
            return SelectedPropertyCheckResult.ALREADY_LANDLORD
        }

        return SelectedPropertyCheckResult.ELIGIBLE_TO_JOIN
    }
}

@JourneyFrameworkComponent
class CheckSelectedPropertyStep(
    stepConfig: CheckSelectedPropertyStepConfig,
) : InternalStep<SelectedPropertyCheckResult, JoinPropertyAddressSearchState>(stepConfig)

enum class SelectedPropertyCheckResult {
    PROPERTY_NOT_REGISTERED,
    ALREADY_LANDLORD,
    ELIGIBLE_TO_JOIN,
}
