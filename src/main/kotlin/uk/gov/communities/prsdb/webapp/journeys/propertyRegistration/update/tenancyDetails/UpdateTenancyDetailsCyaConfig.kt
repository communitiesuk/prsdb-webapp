package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.update.tenancyDetails

import kotlinx.datetime.Instant
import kotlinx.datetime.toJavaInstant
import org.springframework.context.MessageSource
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.exceptions.UpdateConflictException
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.LicensingAndTenancyDetailsDataModelFactory
import uk.gov.communities.prsdb.webapp.journeys.shared.helpers.OccupancyDetailsHelper
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.AbstractCheckYourAnswersStep
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.AbstractCheckYourAnswersStepConfig
import uk.gov.communities.prsdb.webapp.services.PropertyOwnershipService
import uk.gov.communities.prsdb.webapp.services.PropertyUpdateEmailService

@JourneyFrameworkComponent
class UpdateTenancyDetailsCyaConfig(
    private val occupancyDetailsHelper: OccupancyDetailsHelper,
    private val propertyOwnershipService: PropertyOwnershipService,
    private val licensingAndTenancyDetailsDataModelFactory: LicensingAndTenancyDetailsDataModelFactory,
    private val messageSource: MessageSource,
    private val propertyUpdateEmailService: PropertyUpdateEmailService,
) : AbstractCheckYourAnswersStepConfig<UpdateTenancyDetailsJourneyState>() {
    override fun getStepSpecificContent(state: UpdateTenancyDetailsJourneyState): Map<String, Any?> =
        mapOf(
            "title" to "forms.update.checkTenancyDetails.pageTitle",
            "showWarning" to true,
            "submitButtonText" to "forms.buttons.confirmAndSubmitUpdate",
            "insetText" to true,
            "summaryListData" to occupancyDetailsHelper.getCheckYourAnswersSummaryListWithoutProvideLater(state, messageSource),
            "summaryName" to "forms.update.checkOccupancy.occupied.summaryName",
        )

    override fun afterStepDataIsAdded(state: UpdateTenancyDetailsJourneyState) {
        val tenancyDetails = licensingAndTenancyDetailsDataModelFactory.fromTenancyDetailsState(state)
        try {
            propertyOwnershipService.updateTenancyDetails(
                id = state.propertyId,
                tenancyDetails = tenancyDetails,
                initialLastModifiedDate = Instant.parse(state.lastModifiedDate).toJavaInstant(),
            )
        } catch (ex: UpdateConflictException) {
            state.deleteJourney()
            throw ex
        }
        sendUpdateConfirmationEmail(state)
    }

    private fun sendUpdateConfirmationEmail(state: UpdateTenancyDetailsJourneyState) {
        propertyUpdateEmailService.sendUpdateEmails(
            state.propertyId,
            listOf(
                "The number of households living in this property",
                "The number of people living in this property",
                "Rent details (amount, how often it’s paid, bills and furnishing)",
            ),
        )
    }
}

@JourneyFrameworkComponent
final class UpdateTenancyDetailsCyaStep(
    stepConfig: UpdateTenancyDetailsCyaConfig,
) : AbstractCheckYourAnswersStep<UpdateTenancyDetailsJourneyState>(stepConfig) {
    companion object {
        const val ROUTE_SEGMENT = "tenancy-details-check-your-answers"
    }
}
