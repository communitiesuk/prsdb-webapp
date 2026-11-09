package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.constants.FORM_MODEL_ATTR_NAME
import uk.gov.communities.prsdb.webapp.constants.enums.FreeholderContactType
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.FreeholdersDetailsState
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.FreeholderContactTypeFormModel
import uk.gov.communities.prsdb.webapp.models.viewModels.formModels.RadiosButtonViewModel

@JourneyFrameworkComponent
class FreeholderContactTypeStepConfig :
    AbstractRequestableStepConfig<FreeholderContactType, FreeholderContactTypeFormModel, FreeholdersDetailsState>() {
    override val formModelClass = FreeholderContactTypeFormModel::class

    override fun getStepSpecificContent(state: FreeholdersDetailsState): Map<String, Any?> =
        mapOf(
            "fieldName" to "contactType",
            "fieldSetHeading" to "registerProperty.freeholders.contactType.fieldSetHeading",
            "fieldSetHeadingParam" to state.freeholderNameStep.formModelOrNull?.name,
            "fieldSetHint" to "registerProperty.freeholders.contactType.fieldSetHint",
            "radioOptions" to
                listOf(
                    RadiosButtonViewModel(
                        value = FreeholderContactType.EMAIL,
                        labelMsgKey = "registerProperty.freeholders.contactType.radios.option.email.label",
                        hintMsgKey = "registerProperty.freeholders.contactType.radios.option.email.hint",
                    ),
                    RadiosButtonViewModel(
                        value = FreeholderContactType.POSTAL_ADDRESS,
                        labelMsgKey = "registerProperty.freeholders.contactType.radios.option.postalAddress.label",
                    ),
                ),
            "submitButtonText" to "forms.buttons.continue",
        )

    override fun resolvePageContent(
        state: FreeholdersDetailsState,
        defaultContent: Map<String, Any?>,
    ): Map<String, Any?> {
        val formModel = defaultContent[FORM_MODEL_ATTR_NAME] as? FreeholderContactTypeFormModel
        if (formModel?.contactType != null) return defaultContent

        val editingFreeholder = state.editingFreeholder ?: return defaultContent
        val prepopulatedFormModel = formModel ?: FreeholderContactTypeFormModel()
        prepopulatedFormModel.contactType =
            when {
                editingFreeholder.email != null -> FreeholderContactType.EMAIL
                editingFreeholder.address != null -> FreeholderContactType.POSTAL_ADDRESS
                else -> null
            }
        return defaultContent + (FORM_MODEL_ATTR_NAME to prepopulatedFormModel)
    }

    override fun chooseTemplate(state: FreeholdersDetailsState) = "forms/contactTypeForm"

    override fun mode(state: FreeholdersDetailsState) = getFormModelFromStateOrNull(state)?.contactType
}

@JourneyFrameworkComponent
final class FreeholderContactTypeStep(
    stepConfig: FreeholderContactTypeStepConfig,
) : RequestableStep<FreeholderContactType, FreeholderContactTypeFormModel, FreeholdersDetailsState>(stepConfig) {
    companion object {
        const val ROUTE_SEGMENT = "freeholder-contact-type"
    }
}
