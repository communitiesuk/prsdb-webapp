package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.constants.FORM_MODEL_ATTR_NAME
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.SuperiorLandlordsDetailsState
import uk.gov.communities.prsdb.webapp.journeys.shared.YesOrNo
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.SuperiorLandlordProvideDetailsFormModel
import uk.gov.communities.prsdb.webapp.models.viewModels.formModels.RadiosViewModel

@JourneyFrameworkComponent
class ProvideSuperiorLandlordDetailsStepConfig :
    AbstractRequestableStepConfig<YesOrNo, SuperiorLandlordProvideDetailsFormModel, SuperiorLandlordsDetailsState>() {
    override val formModelClass = SuperiorLandlordProvideDetailsFormModel::class

    override fun getStepSpecificContent(state: SuperiorLandlordsDetailsState): Map<String, Any?> =
        mapOf(
            "fieldName" to "provideDetails",
            "fieldSetHeading" to "registerProperty.superiorLandlords.provideDetails.fieldSetHeading",
            "radioOptions" to
                RadiosViewModel.yesOrNoRadios(
                    yesHint = "registerProperty.superiorLandlords.provideDetails.radios.option.yes.hint",
                    noHint = "registerProperty.superiorLandlords.provideDetails.radios.option.no.hint",
                ),
            "submitButtonText" to "forms.buttons.saveAndContinue",
        )

    override fun resolvePageContent(
        state: SuperiorLandlordsDetailsState,
        defaultContent: Map<String, Any?>,
    ): Map<String, Any?> {
        val formModel = defaultContent[FORM_MODEL_ATTR_NAME] as? SuperiorLandlordProvideDetailsFormModel
        if (formModel?.provideDetails != null) return defaultContent

        val editingSuperiorLandlord = state.editingSuperiorLandlord ?: return defaultContent
        val prepopulatedFormModel = formModel ?: SuperiorLandlordProvideDetailsFormModel()
        prepopulatedFormModel.provideDetails = editingSuperiorLandlord.detailsKnown
        return defaultContent + (FORM_MODEL_ATTR_NAME to prepopulatedFormModel)
    }

    override fun chooseTemplate(state: SuperiorLandlordsDetailsState) = "forms/provideDetailsForm"

    override fun mode(state: SuperiorLandlordsDetailsState) =
        getFormModelFromStateOrNull(state)?.provideDetails?.let {
            if (it) YesOrNo.YES else YesOrNo.NO
        }
}

@JourneyFrameworkComponent
final class ProvideSuperiorLandlordDetailsStep(
    stepConfig: ProvideSuperiorLandlordDetailsStepConfig,
) : RequestableStep<YesOrNo, SuperiorLandlordProvideDetailsFormModel, SuperiorLandlordsDetailsState>(stepConfig) {
    companion object {
        const val ROUTE_SEGMENT = "superior-landlord-provide-details"
    }
}
