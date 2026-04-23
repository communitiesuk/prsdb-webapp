package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.update.updateLicensing

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.constants.enums.LicensingType
import uk.gov.communities.prsdb.webapp.exceptions.NotNullFormModelValueIsNullException.Companion.notNullValue
import uk.gov.communities.prsdb.webapp.journeys.shared.helpers.LicensingDetailsHelper
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.AbstractCheckYourAnswersStep
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.AbstractCheckYourAnswersStepConfig
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.CheckAnswersFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.LicensingTypeFormModel
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.PropertyUpdateConfirmation
import uk.gov.communities.prsdb.webapp.services.AbsoluteUrlProvider
import uk.gov.communities.prsdb.webapp.services.EmailNotificationService
import uk.gov.communities.prsdb.webapp.services.PropertyOwnershipService

@JourneyFrameworkComponent
class UpdateLicensingCyaConfig(
    private val licensingDetailsHelper: LicensingDetailsHelper,
    private val propertyOwnershipService: PropertyOwnershipService,
    private val updateConfirmationEmailService: EmailNotificationService<PropertyUpdateConfirmation>,
    private val absoluteUrlProvider: AbsoluteUrlProvider,
) : AbstractCheckYourAnswersStepConfig<UpdateLicensingJourneyState>() {
    override fun getStepSpecificContent(state: UpdateLicensingJourneyState): Map<String, Any?> =
        mapOf(
            "title" to "propertyDetails.update.title",
            "showWarning" to true,
            "submitButtonText" to "forms.buttons.confirmAndSubmitUpdate",
            "insetText" to true,
            "summaryListData" to
                licensingDetailsHelper.getCheckYourAnswersSummaryList(
                    state,
                ),
            "submittedFilteredJourneyData" to CheckAnswersFormModel.serializeJourneyData(state.getSubmittedStepData()),
            "summaryName" to
                if (isRemovingLicensing(state)) {
                    "forms.update.checkLicensing.remove.summaryName"
                } else {
                    "forms.update.checkLicensing.update.summaryName"
                },
        )

    override fun afterStepDataIsAdded(state: UpdateLicensingJourneyState) {
        propertyOwnershipService.updateLicensing(
            state.propertyId,
            state.licensingTypeStep.formModel.notNullValue(LicensingTypeFormModel::licensingType),
            state.getLicenceNumberOrNull(),
        )
        sendUpdateConfirmationEmail(state)
    }

    private fun sendUpdateConfirmationEmail(state: UpdateLicensingJourneyState) {
        val propertyOwnership = propertyOwnershipService.getPropertyOwnership(state.propertyId)
        updateConfirmationEmailService.sendEmail(
            propertyOwnership.primaryLandlord.email,
            PropertyUpdateConfirmation(
                name = propertyOwnership.primaryLandlord.name,
                multiLineAddress = propertyOwnership.address.toMultiLineAddress(),
                updatedItems = "The licensing information",
                propertyRecordUrl = absoluteUrlProvider.buildComplianceInformationUri(propertyOwnership.id),
            ),
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
) : AbstractCheckYourAnswersStep<UpdateLicensingJourneyState>(stepConfig) {
    companion object {
        const val ROUTE_SEGMENT = "check-licensing-answers"
    }
}
