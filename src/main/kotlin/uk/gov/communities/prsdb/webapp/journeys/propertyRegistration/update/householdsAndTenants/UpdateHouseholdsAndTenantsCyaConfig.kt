package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.update.householdsAndTenants

import kotlinx.datetime.Instant
import kotlinx.datetime.toJavaInstant
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.exceptions.NotNullFormModelValueIsNullException.Companion.notNullValue
import uk.gov.communities.prsdb.webapp.journeys.shared.helpers.OccupancyDetailsHelper
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.AbstractCheckYourAnswersStep
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.AbstractCheckYourAnswersStepConfig
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.CheckAnswersFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NewNumberOfPeopleFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NumberOfHouseholdsFormModel
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.PropertyUpdateConfirmation
import uk.gov.communities.prsdb.webapp.services.AbsoluteUrlProvider
import uk.gov.communities.prsdb.webapp.services.EmailNotificationService
import uk.gov.communities.prsdb.webapp.services.PropertyOwnershipService

@JourneyFrameworkComponent
class UpdateHouseholdsAndTenantsCyaConfig(
    private val occupancyDetailsHelper: OccupancyDetailsHelper,
    private val propertyOwnershipService: PropertyOwnershipService,
    private val updateConfirmationEmailService: EmailNotificationService<PropertyUpdateConfirmation>,
    private val absoluteUrlProvider: AbsoluteUrlProvider,
) : AbstractCheckYourAnswersStepConfig<UpdateHouseholdsAndTenantsJourneyState>() {
    override fun getStepSpecificContent(state: UpdateHouseholdsAndTenantsJourneyState): Map<String, Any> =
        mapOf(
            "title" to "propertyDetails.update.title",
            "showWarning" to true,
            "submitButtonText" to "forms.buttons.confirmAndSubmitUpdate",
            "insetText" to true,
            "summaryListData" to occupancyDetailsHelper.getCheckYourHouseHoldsAndTenantsAnswersSummaryList(state),
            "submittedFilteredJourneyData" to CheckAnswersFormModel.serializeJourneyData(state.getSubmittedStepData()),
            "summaryName" to "forms.update.checkOccupancy.occupied.summaryName",
        )

    override fun afterStepDataIsAdded(state: UpdateHouseholdsAndTenantsJourneyState) {
        propertyOwnershipService.updateHouseholdsAndTenants(
            id = state.propertyId,
            numberOfHouseholds =
                state.households.formModel
                    .notNullValue(NumberOfHouseholdsFormModel::numberOfHouseholds)
                    .toInt(),
            numberOfPeople =
                state.tenants.formModel
                    .notNullValue(NewNumberOfPeopleFormModel::numberOfPeople)
                    .toInt(),
            initialLastModifiedDate = Instant.parse(state.lastModifiedDate).toJavaInstant(),
        )
        sendUpdateConfirmationEmail(state)
    }

    private fun sendUpdateConfirmationEmail(state: UpdateHouseholdsAndTenantsJourneyState) {
        val propertyOwnership = propertyOwnershipService.getPropertyOwnership(state.propertyId)
        updateConfirmationEmailService.sendEmail(
            propertyOwnership.primaryLandlord.email,
            PropertyUpdateConfirmation(
                name = propertyOwnership.primaryLandlord.name,
                multiLineAddress = propertyOwnership.address.toMultiLineAddress(),
                updatedItems =
                    listOf(
                        "The number of households living in this property",
                        "The number of people living in this property",
                    ).joinToString("\n"),
                propertyRecordUrl = absoluteUrlProvider.buildComplianceInformationUri(propertyOwnership.id),
            ),
        )
    }
}

@JourneyFrameworkComponent
final class UpdateHouseholdsAndTenantsCyaStep(
    stepConfig: UpdateHouseholdsAndTenantsCyaConfig,
) : AbstractCheckYourAnswersStep<UpdateHouseholdsAndTenantsJourneyState>(stepConfig) {
    companion object {
        const val ROUTE_SEGMENT = "households-tenants-check-your-answers"
    }
}
