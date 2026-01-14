package uk.gov.communities.prsdb.webapp.journeys.updateLicensing

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.constants.enums.LicensingType
import uk.gov.communities.prsdb.webapp.exceptions.NotNullFormModelValueIsNullException.Companion.notNullValue
import uk.gov.communities.prsdb.webapp.forms.PageData
import uk.gov.communities.prsdb.webapp.journeys.AbstractGenericRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.UnrecoverableJourneyStateException
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.journeys.shared.helpers.LicensingDetailsHelper
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.CheckAnswersFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.LicensingTypeFormModel
import uk.gov.communities.prsdb.webapp.services.PropertyOwnershipService

@JourneyFrameworkComponent
class UpdateLicensingCyaStepConfig(
    private val licensingDetailsHelper: LicensingDetailsHelper,
    private val propertyOwnershipService: PropertyOwnershipService,
) : AbstractGenericRequestableStepConfig<Complete, CheckAnswersFormModel, UpdateLicensingJourneyState>() {
    override val formModelClass = CheckAnswersFormModel::class

    private lateinit var childJourneyId: String

    override fun getStepSpecificContent(state: UpdateLicensingJourneyState): Map<String, Any> {
        if (state.cyaChildJourneyId == null) {
            state.initialiseCyaChildJourney()
        }

        childJourneyId = state.cyaChildJourneyId
            ?: throw UnrecoverableJourneyStateException(state.journeyId, "CYA child journey ID should be initialised")

        return mapOf(
            "title" to "propertyDetails.update.title",
            "showWarning" to true,
            "submitButtonText" to "forms.buttons.confirmAndSubmitUpdate",
            "insetText" to true,
            "summaryListData" to licensingDetailsHelper.getCheckYourAnswersSummaryList(state, childJourneyId),
            "submittedFilteredJourneyData" to CheckAnswersFormModel.serializeJourneyData(state.getSubmittedStepData()),
            "summaryName" to
                if (isRemovingLicensing(state)) {
                    "forms.update.checkLicensing.remove.summaryName"
                } else {
                    "forms.update.checkLicensing.update.summaryName"
                },
        )
    }

    private fun isRemovingLicensing(state: UpdateLicensingJourneyState): Boolean {
        val newLicensingType = state.licensingTypeStep.formModel.notNullValue(LicensingTypeFormModel::licensingType)

        return state.hasOriginalLicense!! && newLicensingType == LicensingType.NO_LICENSING
    }

    override fun enrichSubmittedDataBeforeValidation(
        state: UpdateLicensingJourneyState,
        formData: PageData,
    ): PageData =
        super.enrichSubmittedDataBeforeValidation(state, formData) +
            (CheckAnswersFormModel::storedJourneyData.name to state.getSubmittedStepData())

    override fun afterStepDataIsAdded(state: UpdateLicensingJourneyState) {
        propertyOwnershipService.updateLicensing(
            state.propertyId!!,
            state.licensingTypeStep.formModel.notNullValue(LicensingTypeFormModel::licensingType),
            state.getLicenceNumberOrNull(),
        )
    }

    override fun resolveNextDestination(
        state: UpdateLicensingJourneyState,
        defaultDestination: Destination,
    ): Destination {
        state.deleteJourney()
        return defaultDestination
    }

    override fun chooseTemplate(state: UpdateLicensingJourneyState): String = "forms/checkAnswersForm"

    override fun mode(state: UpdateLicensingJourneyState): Complete? = getFormModelFromStateOrNull(state)?.let { Complete.COMPLETE }
}

@JourneyFrameworkComponent
final class UpdateLicensingCheckAnswersStep(
    stepConfig: UpdateLicensingCyaStepConfig,
) : RequestableStep<Complete, CheckAnswersFormModel, UpdateLicensingJourneyState>(stepConfig)
