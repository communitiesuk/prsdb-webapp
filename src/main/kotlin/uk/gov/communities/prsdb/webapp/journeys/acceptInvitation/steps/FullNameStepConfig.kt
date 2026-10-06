package uk.gov.communities.prsdb.webapp.journeys.acceptInvitation.steps

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.constants.NAME_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.acceptInvitation.AcceptInvitationJourneyState
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NameFormModel

@JourneyFrameworkComponent("acceptOrganisationalLandlordUserInvitationFullNameStepConfig")
class FullNameStepConfig :
    AbstractRequestableStepConfig<Complete, NameFormModel, AcceptInvitationJourneyState>() {
    override val formModelClass = NameFormModel::class

    override fun getStepSpecificContent(state: AcceptInvitationJourneyState): Map<String, Any?> =
        mapOf(
            "fieldSetHeading" to "forms.name.fieldSetHeading",
            "submitButtonText" to "forms.buttons.saveAndContinue",
        )

    override fun chooseTemplate(state: AcceptInvitationJourneyState) = "forms/nameForm"

    override fun mode(state: AcceptInvitationJourneyState) = getFormModelFromStateOrNull(state)?.let { Complete.COMPLETE }
}

@JourneyFrameworkComponent("acceptInvitationFullNameStep")
final class FullNameStep(
    stepConfig: FullNameStepConfig,
) : RequestableStep<Complete, NameFormModel, AcceptInvitationJourneyState>(stepConfig) {
    companion object {
        const val ROUTE_SEGMENT = NAME_PATH_SEGMENT
    }
}
