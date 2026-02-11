package uk.gov.communities.prsdb.webapp.journeys.localCouncilUserRegistration.steps

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.constants.FORM_MODEL_ATTR_NAME
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.localCouncilUserRegistration.LocalCouncilUserRegistrationJourneyState
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.EmailFormModel

@JourneyFrameworkComponent("localCouncilUserRegistrationEmailStepConfig")
class EmailStepConfig : AbstractRequestableStepConfig<Complete, EmailFormModel, LocalCouncilUserRegistrationJourneyState>() {
    override val formModelClass = EmailFormModel::class

    override fun getStepSpecificContent(state: LocalCouncilUserRegistrationJourneyState) =
        mapOf(
            "fieldSetHeading" to "registerLocalCouncilUser.email.fieldSetHeading",
            "fieldSetHint" to "registerLocalCouncilUser.email.fieldSetHint",
            "label" to "registerLocalCouncilUser.email.label",
            "submitButtonText" to "forms.buttons.continue",
        )

    override fun chooseTemplate(state: LocalCouncilUserRegistrationJourneyState): String = "forms/emailForm"

    override fun mode(state: LocalCouncilUserRegistrationJourneyState) = getFormModelFromStateOrNull(state)?.let { Complete.COMPLETE }

    override fun resolvePageContent(
        state: LocalCouncilUserRegistrationJourneyState,
        defaultContent: Map<String, Any?>,
    ): Map<String, Any?> {
        val formModel = defaultContent[FORM_MODEL_ATTR_NAME] as? EmailFormModel
        if (formModel?.emailAddress == null) {
            val prePopulatedFormModel = EmailFormModel.fromLocalCouncilInvitation(state.invitation)
            return defaultContent + (FORM_MODEL_ATTR_NAME to prePopulatedFormModel)
        }
        return defaultContent
    }
}

@JourneyFrameworkComponent("localCouncilUserRegistrationEmailStep")
final class EmailStep(
    stepConfig: EmailStepConfig,
) : RequestableStep<Complete, EmailFormModel, LocalCouncilUserRegistrationJourneyState>(stepConfig)
