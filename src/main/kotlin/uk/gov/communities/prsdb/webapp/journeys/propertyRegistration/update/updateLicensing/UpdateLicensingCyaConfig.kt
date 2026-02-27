package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.update.updateLicensing

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.constants.enums.LicensingType
import uk.gov.communities.prsdb.webapp.exceptions.NotNullFormModelValueIsNullException.Companion.notNullValue
import uk.gov.communities.prsdb.webapp.journeys.shared.helpers.LicensingDetailsHelper
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.AbstractCheckYourAnswersStep
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.AbstractCheckYourAnswersStepConfig2
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.CheckAnswersFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.LicensingTypeFormModel
import uk.gov.communities.prsdb.webapp.services.PropertyOwnershipService

enum class UpdateLicensingCheckableElements {
    LICENSING,
}

@JourneyFrameworkComponent
class UpdateLicensingCyaConfig(
    private val licensingDetailsHelper: LicensingDetailsHelper,
    private val propertyOwnershipService: PropertyOwnershipService,
) : AbstractCheckYourAnswersStepConfig2<UpdateLicensingCheckableElements, UpdateLicensingJourneyState>() {
    override fun getStepSpecificContent(state: UpdateLicensingJourneyState): Map<String, Any?> {
        UpdateLicensingCheckableElements.entries.forEach { checkableElement ->
            val newId = state.generateJourneyId("${checkableElement.name} for ${state.journeyId}")
            state.initialiseCyaChildJourney(newId, checkableElement)
        }

        return mapOf(
            "title" to "propertyDetails.update.title",
            "showWarning" to true,
            "submitButtonText" to "forms.buttons.confirmAndSubmitUpdate",
            "insetText" to true,
            "summaryListData" to
                licensingDetailsHelper.getCheckYourAnswersSummaryList(
                    state,
                    state.getCyaJourneyId(UpdateLicensingCheckableElements.LICENSING),
                ),
            "submittedFilteredJourneyData" to CheckAnswersFormModel.serializeJourneyData(state.getSubmittedStepData()),
            "summaryName" to
                if (isRemovingLicensing(state)) {
                    "forms.update.checkLicensing.remove.summaryName"
                } else {
                    "forms.update.checkLicensing.update.summaryName"
                },
        )
    }

    override fun afterStepDataIsAdded(state: UpdateLicensingJourneyState) {
        propertyOwnershipService.updateLicensing(
            state.propertyId,
            state.licensingTypeStep.formModel.notNullValue(LicensingTypeFormModel::licensingType),
            state.getLicenceNumberOrNull(),
        )
    }

    private fun isRemovingLicensing(state: UpdateLicensingJourneyState): Boolean {
        val newLicensingType = state.licensingTypeStep.formModel.notNullValue(LicensingTypeFormModel::licensingType)
        return state.hasOriginalLicense && newLicensingType == LicensingType.NO_LICENSING
    }
}

@JourneyFrameworkComponent
final class UpdateLicensingCyaStep(
    stepConfig: UpdateLicensingCyaConfig,
) : AbstractCheckYourAnswersStep<UpdateLicensingCheckableElements, UpdateLicensingJourneyState>(stepConfig)
