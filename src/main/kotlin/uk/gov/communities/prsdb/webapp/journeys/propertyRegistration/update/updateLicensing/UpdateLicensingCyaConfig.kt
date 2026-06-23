package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.update.updateLicensing

import kotlinx.datetime.Instant
import kotlinx.datetime.toJavaInstant
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.constants.enums.LicensingType
import uk.gov.communities.prsdb.webapp.exceptions.NotNullFormModelValueIsNullException.Companion.notNullValue
import uk.gov.communities.prsdb.webapp.exceptions.UpdateConflictException
import uk.gov.communities.prsdb.webapp.journeys.shared.helpers.LicensingDetailsHelper
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.AbstractCheckYourAnswersStep
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.AbstractCheckYourAnswersStepConfig
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.LicensingTypeFormModel
import uk.gov.communities.prsdb.webapp.services.PropertyOwnershipService
import uk.gov.communities.prsdb.webapp.services.PropertyUpdateEmailService

@JourneyFrameworkComponent
class UpdateLicensingCyaConfig(
    private val licensingDetailsHelper: LicensingDetailsHelper,
    private val propertyOwnershipService: PropertyOwnershipService,
    private val propertyUpdateEmailService: PropertyUpdateEmailService,
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
            "summaryName" to
                if (isRemovingLicensing(state)) {
                    "forms.update.checkLicensing.remove.summaryName"
                } else {
                    "forms.update.checkLicensing.update.summaryName"
                },
        )

    override fun afterStepDataIsAdded(state: UpdateLicensingJourneyState) {
        try {
            propertyOwnershipService.updateLicensing(
                state.propertyId,
                state.licensingTypeStep.formModel.notNullValue(LicensingTypeFormModel::licensingType),
                state.getLicenceNumberOrNull(),
                Instant.parse(state.lastModifiedDate).toJavaInstant(),
            )
        } catch (ex: UpdateConflictException) {
            state.deleteJourney()
            throw ex
        }
        sendUpdateConfirmationEmail(state)
    }

    private fun sendUpdateConfirmationEmail(state: UpdateLicensingJourneyState) {
        propertyUpdateEmailService.sendUpdateEmails(state.propertyId, listOf("The licensing information"))
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
