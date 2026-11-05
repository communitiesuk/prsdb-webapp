package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.constants.FORM_MODEL_ATTR_NAME
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.FormData
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.FreeholdersDetailsState
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.FreeholderEmailFormModel

@JourneyFrameworkComponent
class FreeholderEmailStepConfig :
    AbstractRequestableStepConfig<Complete, FreeholderEmailFormModel, FreeholdersDetailsState>() {
    override val formModelClass = FreeholderEmailFormModel::class

    override fun getStepSpecificContent(state: FreeholdersDetailsState): Map<String, Any?> =
        mapOf(
            "fieldSetHeading" to "registerProperty.freeholders.email.fieldSetHeading",
            "fieldSetHeadingParam" to state.freeholderNameStep.formModelOrNull?.name,
            "submitButtonText" to "forms.buttons.saveAndContinue",
        )

    override fun chooseTemplate(state: FreeholdersDetailsState) = "forms/emailForm"

    override fun enrichSubmittedDataBeforeValidation(
        state: FreeholdersDetailsState,
        formData: FormData,
    ): FormData {
        val existingFreeholderEmails =
            state.freeholdersMap
                .orEmpty()
                .filterKeys { it != state.editingFreeholderId }
                .values
                .mapNotNull { it.email }

        return super.enrichSubmittedDataBeforeValidation(state, formData) +
            (FreeholderEmailFormModel::existingFreeholderEmails.name to existingFreeholderEmails)
    }

    override fun resolvePageContent(
        state: FreeholdersDetailsState,
        defaultContent: Map<String, Any?>,
    ): Map<String, Any?> {
        val formModel = defaultContent[FORM_MODEL_ATTR_NAME] as? FreeholderEmailFormModel
        if (!formModel?.emailAddress.isNullOrBlank()) return defaultContent

        val savedEmail = state.editingFreeholder?.email ?: return defaultContent
        val formModelToDisplay = formModel ?: FreeholderEmailFormModel()
        formModelToDisplay.emailAddress = savedEmail

        return defaultContent + (FORM_MODEL_ATTR_NAME to formModelToDisplay)
    }

    override fun mode(state: FreeholdersDetailsState): Complete? = getFormModelFromStateOrNull(state)?.let { Complete.COMPLETE }
}

@JourneyFrameworkComponent
final class FreeholderEmailStep(
    stepConfig: FreeholderEmailStepConfig,
) : RequestableStep<Complete, FreeholderEmailFormModel, FreeholdersDetailsState>(stepConfig) {
    companion object {
        const val ROUTE_SEGMENT = "freeholder-email"
    }
}
