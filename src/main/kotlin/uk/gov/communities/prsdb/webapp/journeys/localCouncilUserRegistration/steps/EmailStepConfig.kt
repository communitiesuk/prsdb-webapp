package uk.gov.communities.prsdb.webapp.journeys.localCouncilUserRegistration.steps

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyState
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.EmailFormModel
import uk.gov.communities.prsdb.webapp.services.LocalCouncilInvitationService

@JourneyFrameworkComponent("localCouncilUserRegistrationEmailStepConfig")
class EmailStepConfig(
    private val invitationService: LocalCouncilInvitationService,
) : AbstractRequestableStepConfig<Complete, EmailFormModel, JourneyState>() {
    override val formModelClass = EmailFormModel::class

    override fun getStepSpecificContent(state: JourneyState) =
        mapOf(
            "fieldSetHeading" to "registerLocalCouncilUser.email.fieldSetHeading",
            "fieldSetHint" to "registerLocalCouncilUser.email.fieldSetHint",
            "label" to "registerLocalCouncilUser.email.label",
            "submitButtonText" to "forms.buttons.continue",
        )

    override fun chooseTemplate(state: JourneyState): String = "forms/emailForm"

    override fun mode(state: JourneyState) = getFormModelFromStateOrNull(state)?.let { Complete.COMPLETE }

    override fun resolvePageContent(
        state: JourneyState,
        defaultContent: Map<String, Any?>,
    ): Map<String, Any?> {
        val formModel = defaultContent["formModel"] as? EmailFormModel
        if (formModel?.emailAddress == null) {
            val token = invitationService.getTokenFromSession()
            if (token != null) {
                val prePopulatedFormModel =
                    EmailFormModel.fromLocalCouncilInvitation(
                        invitationService.getInvitationFromToken(token),
                    )
                return defaultContent + ("formModel" to prePopulatedFormModel)
            }
        }
        return defaultContent
    }
}

@JourneyFrameworkComponent("localCouncilUserRegistrationEmailStep")
final class EmailStep(
    stepConfig: EmailStepConfig,
) : RequestableStep<Complete, EmailFormModel, JourneyState>(stepConfig)
