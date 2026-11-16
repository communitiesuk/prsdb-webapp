package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.constants.FORM_MODEL_ATTR_NAME
import uk.gov.communities.prsdb.webapp.constants.enums.SuperiorLandlordContactType
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.SuperiorLandlordsDetailsState
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.SuperiorLandlordContactTypeFormModel
import uk.gov.communities.prsdb.webapp.models.viewModels.formModels.RadiosButtonViewModel

@JourneyFrameworkComponent
class SuperiorLandlordContactTypeStepConfig :
    AbstractRequestableStepConfig<SuperiorLandlordContactType, SuperiorLandlordContactTypeFormModel, SuperiorLandlordsDetailsState>() {
    override val formModelClass = SuperiorLandlordContactTypeFormModel::class

    override fun getStepSpecificContent(state: SuperiorLandlordsDetailsState): Map<String, Any?> =
        mapOf(
            "fieldName" to "contactType",
            "fieldSetHeading" to "registerProperty.superiorLandlords.contactType.fieldSetHeading",
            "fieldSetHeadingParam" to state.superiorLandlordNameStep.formModelOrNull?.name,
            "fieldSetHint" to "registerProperty.superiorLandlords.contactType.fieldSetHint",
            "radioOptions" to
                listOf(
                    RadiosButtonViewModel(
                        value = SuperiorLandlordContactType.EMAIL,
                        labelMsgKey = "registerProperty.superiorLandlords.contactType.radios.option.email.label",
                        hintMsgKey = "registerProperty.superiorLandlords.contactType.radios.option.email.hint",
                    ),
                    RadiosButtonViewModel(
                        value = SuperiorLandlordContactType.POSTAL_ADDRESS,
                        labelMsgKey = "registerProperty.superiorLandlords.contactType.radios.option.postalAddress.label",
                    ),
                ),
            "submitButtonText" to "forms.buttons.continue",
        )

    override fun resolvePageContent(
        state: SuperiorLandlordsDetailsState,
        defaultContent: Map<String, Any?>,
    ): Map<String, Any?> {
        val formModel = defaultContent[FORM_MODEL_ATTR_NAME] as? SuperiorLandlordContactTypeFormModel
        if (formModel?.contactType != null) return defaultContent

        val editingSuperiorLandlord = state.editingSuperiorLandlord ?: return defaultContent
        val prepopulatedFormModel = formModel ?: SuperiorLandlordContactTypeFormModel()
        prepopulatedFormModel.contactType =
            when {
                editingSuperiorLandlord.email != null -> SuperiorLandlordContactType.EMAIL
                editingSuperiorLandlord.address != null -> SuperiorLandlordContactType.POSTAL_ADDRESS
                else -> null
            }
        return defaultContent + (FORM_MODEL_ATTR_NAME to prepopulatedFormModel)
    }

    override fun chooseTemplate(state: SuperiorLandlordsDetailsState) = "forms/contactTypeForm"

    override fun mode(state: SuperiorLandlordsDetailsState) = getFormModelFromStateOrNull(state)?.contactType
}

@JourneyFrameworkComponent
final class SuperiorLandlordContactTypeStep(
    stepConfig: SuperiorLandlordContactTypeStepConfig,
) : RequestableStep<SuperiorLandlordContactType, SuperiorLandlordContactTypeFormModel, SuperiorLandlordsDetailsState>(stepConfig) {
    companion object {
        const val ROUTE_SEGMENT = "superior-landlord-contact-type"
    }
}
