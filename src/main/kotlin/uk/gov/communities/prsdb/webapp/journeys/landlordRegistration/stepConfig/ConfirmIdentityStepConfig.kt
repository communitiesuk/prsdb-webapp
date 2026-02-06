package uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.states.IdentityState
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NoInputFormModel
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.SummaryListRowViewModel

@JourneyFrameworkComponent
class ConfirmIdentityStepConfig : AbstractRequestableStepConfig<Complete, NoInputFormModel, IdentityState>() {
    override val formModelClass = NoInputFormModel::class

    override fun getStepSpecificContent(state: IdentityState) =
        mapOf(
            "submitButtonText" to "forms.buttons.confirmAndContinue",
            "identitySummaryList" to getIdentitySummaryList(state),
        )

    override fun chooseTemplate(state: IdentityState) = "forms/confirmIdentityForm"

    override fun mode(state: IdentityState) = getFormModelFromStateOrNull(state)?.let { Complete.COMPLETE }

    private fun getIdentitySummaryList(state: IdentityState) =
        listOf(
            SummaryListRowViewModel(
                "forms.confirmDetails.rowHeading.name",
                state.getNotNullVerifiedIdentity().name,
                null,
            ),
            SummaryListRowViewModel(
                "forms.confirmDetails.rowHeading.dob",
                state.getNotNullVerifiedIdentity().birthDate,
                null,
            ),
        )
}

@JourneyFrameworkComponent
final class ConfirmIdentityStep(
    stepConfig: ConfirmIdentityStepConfig,
) : RequestableStep<Complete, NoInputFormModel, IdentityState>(stepConfig) {
    companion object {
        const val ROUTE_SEGMENT = "confirm-identity"
    }
}
