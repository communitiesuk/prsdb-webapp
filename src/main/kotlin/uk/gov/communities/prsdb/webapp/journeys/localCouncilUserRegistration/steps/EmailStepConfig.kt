package uk.gov.communities.prsdb.webapp.journeys.localCouncilUserRegistration.steps

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.localCouncilUserRegistration.LocalCouncilUserRegistrationJourneyState
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.EmailFormModel

@JourneyFrameworkComponent
class EmailStepConfig : AbstractRequestableStepConfig<Complete, EmailFormModel, LocalCouncilUserRegistrationJourneyState>() {
    override val formModelClass = EmailFormModel::class

    override fun getStepSpecificContent(state: LocalCouncilUserRegistrationJourneyState): Map<String, Any?> =
        mapOf(
            "title" to "registerLocalCouncilUser.title",
            "fieldSetHeading" to "registerLocalCouncilUser.email.fieldSetHeading",
            "fieldSetHint" to "registerLocalCouncilUser.email.fieldSetHint",
            "label" to "registerLocalCouncilUser.email.label",
            "submitButtonText" to "forms.buttons.continue",
        )

    override fun chooseTemplate(state: LocalCouncilUserRegistrationJourneyState): String = "forms/emailForm"

    override fun mode(state: LocalCouncilUserRegistrationJourneyState): Complete? {
        val formModel = getFormModelFromStateOrNull(state)
        return formModel?.let { Complete.COMPLETE }
    }

    override fun afterStepIsReached(state: LocalCouncilUserRegistrationJourneyState) {
        // Pre-populate email from invitation on first visit
        if (getFormModelFromStateOrNull(state) == null) {
            val invitation = state.getInvitation()
            val emailForm = EmailFormModel.fromLocalCouncilInvitation(invitation)
            // Convert form model to PageData and add to state
            state.addStepData(
                routeSegment,
                mapOf(
                    "emailAddress" to emailForm.emailAddress,
                ),
            )
        }
    }
}

@JourneyFrameworkComponent
final class EmailStep(
    stepConfig: EmailStepConfig,
) : RequestableStep<Complete, EmailFormModel, LocalCouncilUserRegistrationJourneyState>(stepConfig)
