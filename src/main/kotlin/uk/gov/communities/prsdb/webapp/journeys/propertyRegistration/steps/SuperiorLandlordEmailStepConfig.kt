package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.constants.FORM_MODEL_ATTR_NAME
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.FormData
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.SuperiorLandlordsDetailsState
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.SuperiorLandlordEmailFormModel

@JourneyFrameworkComponent
class SuperiorLandlordEmailStepConfig :
    AbstractRequestableStepConfig<Complete, SuperiorLandlordEmailFormModel, SuperiorLandlordsDetailsState>() {
    override val formModelClass = SuperiorLandlordEmailFormModel::class

    override fun getStepSpecificContent(state: SuperiorLandlordsDetailsState): Map<String, Any?> =
        mapOf(
            "fieldSetHeading" to "registerProperty.superiorLandlords.email.fieldSetHeading",
            "fieldSetHeadingParam" to state.superiorLandlordNameStep.formModelOrNull?.name,
            "submitButtonText" to "forms.buttons.saveAndContinue",
        )

    override fun chooseTemplate(state: SuperiorLandlordsDetailsState) = "forms/emailForm"

    override fun enrichSubmittedDataBeforeValidation(
        state: SuperiorLandlordsDetailsState,
        formData: FormData,
    ): FormData {
        val existingSuperiorLandlordEmails =
            state.superiorLandlordsMap
                .orEmpty()
                .filterKeys { it != state.editingSuperiorLandlordId }
                .values
                .mapNotNull { it.email }

        return super.enrichSubmittedDataBeforeValidation(state, formData) +
            (SuperiorLandlordEmailFormModel::existingSuperiorLandlordEmails.name to existingSuperiorLandlordEmails)
    }

    override fun resolvePageContent(
        state: SuperiorLandlordsDetailsState,
        defaultContent: Map<String, Any?>,
    ): Map<String, Any?> {
        val formModel = defaultContent[FORM_MODEL_ATTR_NAME] as? SuperiorLandlordEmailFormModel
        if (!formModel?.emailAddress.isNullOrBlank()) return defaultContent

        val savedEmail = state.editingSuperiorLandlord?.email ?: return defaultContent
        val formModelToDisplay = formModel ?: SuperiorLandlordEmailFormModel()
        formModelToDisplay.emailAddress = savedEmail

        return defaultContent + (FORM_MODEL_ATTR_NAME to formModelToDisplay)
    }

    override fun mode(state: SuperiorLandlordsDetailsState): Complete? = getFormModelFromStateOrNull(state)?.let { Complete.COMPLETE }
}

@JourneyFrameworkComponent
final class SuperiorLandlordEmailStep(
    stepConfig: SuperiorLandlordEmailStepConfig,
) : RequestableStep<Complete, SuperiorLandlordEmailFormModel, SuperiorLandlordsDetailsState>(stepConfig) {
    companion object {
        const val ROUTE_SEGMENT = "superior-landlord-email"
    }
}
