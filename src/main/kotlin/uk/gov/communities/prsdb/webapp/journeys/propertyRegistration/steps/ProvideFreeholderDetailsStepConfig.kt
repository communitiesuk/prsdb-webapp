package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.constants.FORM_MODEL_ATTR_NAME
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.FormData
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.FreeholdersDetailsState
import uk.gov.communities.prsdb.webapp.journeys.shared.YesOrNo
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.FreeholderProvideDetailsFormModel
import uk.gov.communities.prsdb.webapp.models.viewModels.formModels.RadiosViewModel

@JourneyFrameworkComponent
class ProvideFreeholderDetailsStepConfig :
    AbstractRequestableStepConfig<YesOrNo, FreeholderProvideDetailsFormModel, FreeholdersDetailsState>() {
    override val formModelClass = FreeholderProvideDetailsFormModel::class

    override fun getStepSpecificContent(state: FreeholdersDetailsState): Map<String, Any?> =
        mapOf(
            "fieldSetHeading" to
                if (state.isJointFreeholder) {
                    "registerProperty.freeholders.provideDetails.fieldSetHeading.joint"
                } else {
                    "registerProperty.freeholders.provideDetails.fieldSetHeading.nonJoint"
                },
            "radioOptions" to
                RadiosViewModel.yesOrNoRadios(
                    yesHint = "registerProperty.freeholders.provideDetails.radios.option.yes.hint",
                    noHint = "registerProperty.freeholders.provideDetails.radios.option.no.hint",
                ),
        )

    override fun enrichSubmittedDataBeforeValidation(
        state: FreeholdersDetailsState,
        formData: FormData,
    ): FormData =
        super.enrichSubmittedDataBeforeValidation(state, formData) +
            (FreeholderProvideDetailsFormModel::isJointFreeholder.name to state.isJointFreeholder)

    override fun resolvePageContent(
        state: FreeholdersDetailsState,
        defaultContent: Map<String, Any?>,
    ): Map<String, Any?> {
        val formModel = defaultContent[FORM_MODEL_ATTR_NAME] as? FreeholderProvideDetailsFormModel
        if (formModel?.provideDetails != null) return defaultContent

        val editingFreeholder = state.editingFreeholder ?: return defaultContent
        val prepopulatedFormModel = formModel ?: FreeholderProvideDetailsFormModel()
        prepopulatedFormModel.provideDetails = editingFreeholder.detailsKnown
        return defaultContent + (FORM_MODEL_ATTR_NAME to prepopulatedFormModel)
    }

    override fun chooseTemplate(state: FreeholdersDetailsState) = "forms/provideFreeholderDetailsForm"

    override fun mode(state: FreeholdersDetailsState) =
        getFormModelFromStateOrNull(state)?.provideDetails?.let {
            if (it) YesOrNo.YES else YesOrNo.NO
        }
}

@JourneyFrameworkComponent
final class ProvideFreeholderDetailsStep(
    stepConfig: ProvideFreeholderDetailsStepConfig,
) : RequestableStep<YesOrNo, FreeholderProvideDetailsFormModel, FreeholdersDetailsState>(stepConfig) {
    companion object {
        const val ROUTE_SEGMENT = "freeholder-provide-details"
    }
}
