package uk.gov.communities.prsdb.webapp.journeys.updateLicensing

import jakarta.persistence.EntityExistsException
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.constants.enums.LicensingType
import uk.gov.communities.prsdb.webapp.exceptions.NotNullFormModelValueIsNullException.Companion.notNullValue
import uk.gov.communities.prsdb.webapp.forms.PageData
import uk.gov.communities.prsdb.webapp.journeys.AbstractGenericStepConfig
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.UnrecoverableJourneyStateException
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.CheckAnswersFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.LicensingTypeFormModel
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.SummaryListRowViewModel

@JourneyFrameworkComponent
class UpdateLicensingCyaStepConfig : AbstractGenericStepConfig<Complete, CheckAnswersFormModel, UpdateLicensingJourneyState>() {
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
            "summaryListData" to getLicensingDetailsSummaryList(state),
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
        val existingLicensingType = state.originalLicenseData?.licenseType
        val newLicensingType = state.licensingTypeStep.formModel.notNullValue(LicensingTypeFormModel::licensingType)

        return existingLicensingType != null &&
            existingLicensingType != LicensingType.NO_LICENSING &&
            newLicensingType == LicensingType.NO_LICENSING
    }

    override fun enrichSubmittedDataBeforeValidation(
        state: UpdateLicensingJourneyState,
        formData: PageData,
    ): PageData =
        super.enrichSubmittedDataBeforeValidation(state, formData) +
            (CheckAnswersFormModel::storedJourneyData.name to state.getSubmittedStepData())

    override fun afterStepDataIsAdded(state: UpdateLicensingJourneyState) {
        try {
            TODO("Implement updating the licensing details in the property registration service")
        } catch (_: EntityExistsException) {
        }
    }

    // QQ -  we should extract this to a shared helper with the registration CYA page
    private fun getLicensingDetailsSummaryList(state: UpdateLicensingJourneyState): List<SummaryListRowViewModel> =
        state.licensingTypeStep.formModel.notNullValue(LicensingTypeFormModel::licensingType).let { licensingType ->
            listOfNotNull(
                SummaryListRowViewModel.forCheckYourAnswersPage(
                    "forms.checkPropertyAnswers.propertyDetails.licensingType",
                    licensingType,
                    Destination.VisitableStep(state.licensingTypeStep, childJourneyId),
                ),
                when (licensingType) {
                    LicensingType.HMO_MANDATORY_LICENCE -> (state.getLicenceNumber() to state.hmoMandatoryLicenceStep)
                    LicensingType.HMO_ADDITIONAL_LICENCE -> (state.getLicenceNumber() to state.hmoAdditionalLicenceStep)
                    LicensingType.SELECTIVE_LICENCE -> (state.getLicenceNumber() to state.selectiveLicenceStep)
                    else -> null
                }?.let { (licenceNumber, step) ->
                    SummaryListRowViewModel.forCheckYourAnswersPage(
                        "propertyDetails.propertyRecord.licensingInformation.licensingNumber",
                        licenceNumber,
                        Destination(step),
                    )
                },
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
