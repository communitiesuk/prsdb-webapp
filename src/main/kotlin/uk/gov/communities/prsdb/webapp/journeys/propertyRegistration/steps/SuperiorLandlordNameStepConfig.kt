package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.constants.FORM_MODEL_ATTR_NAME
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.SuperiorLandlordsDetailsState
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.SuperiorLandlordNameFormModel

@JourneyFrameworkComponent
class SuperiorLandlordNameStepConfig :
    AbstractRequestableStepConfig<Complete, SuperiorLandlordNameFormModel, SuperiorLandlordsDetailsState>() {
    override val formModelClass = SuperiorLandlordNameFormModel::class

    override fun getStepSpecificContent(state: SuperiorLandlordsDetailsState): Map<String, Any?> =
        mapOf(
            "fieldSetHeading" to "registerProperty.superiorLandlords.name.fieldSetHeading",
            "fieldSetHint" to "registerProperty.superiorLandlords.name.fieldSetHint",
            "submitButtonText" to "forms.buttons.saveAndContinue",
        )

    override fun resolvePageContent(
        state: SuperiorLandlordsDetailsState,
        defaultContent: Map<String, Any?>,
    ): Map<String, Any?> {
        val formModel = defaultContent[FORM_MODEL_ATTR_NAME] as? SuperiorLandlordNameFormModel
        if (!formModel?.name.isNullOrBlank()) return defaultContent

        val editingSuperiorLandlord = state.editingSuperiorLandlord ?: return defaultContent
        val prepopulatedFormModel = formModel ?: SuperiorLandlordNameFormModel()
        prepopulatedFormModel.name = editingSuperiorLandlord.name
        return defaultContent + (FORM_MODEL_ATTR_NAME to prepopulatedFormModel)
    }

    override fun chooseTemplate(state: SuperiorLandlordsDetailsState) = "forms/nameForm"

    override fun mode(state: SuperiorLandlordsDetailsState): Complete? = getFormModelFromStateOrNull(state)?.let { Complete.COMPLETE }
}

@JourneyFrameworkComponent
final class SuperiorLandlordNameStep(
    stepConfig: SuperiorLandlordNameStepConfig,
) : RequestableStep<Complete, SuperiorLandlordNameFormModel, SuperiorLandlordsDetailsState>(stepConfig) {
    companion object {
        const val ROUTE_SEGMENT = "superior-landlord-name"
    }
}
