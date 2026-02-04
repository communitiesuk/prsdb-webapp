package uk.gov.communities.prsdb.webapp.journeys.propertyDeregistration.stepConfig

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.constants.DEREGISTRATION_REASON_MAX_LENGTH
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.propertyDeregistration.PropertyDeregistrationJourneyState
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.models.dataModels.RegistrationNumberDataModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.PropertyDeregistrationReasonFormModel
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.PropertyDeregistrationConfirmationEmail
import uk.gov.communities.prsdb.webapp.services.EmailNotificationService
import uk.gov.communities.prsdb.webapp.services.PropertyDeregistrationService

@JourneyFrameworkComponent
class ReasonStepConfig(
    private val propertyDeregistrationService: PropertyDeregistrationService,
    private val confirmationEmailSender: EmailNotificationService<PropertyDeregistrationConfirmationEmail>,
) : AbstractRequestableStepConfig<Complete, PropertyDeregistrationReasonFormModel, PropertyDeregistrationJourneyState>() {
    override val formModelClass = PropertyDeregistrationReasonFormModel::class

    override fun getStepSpecificContent(state: PropertyDeregistrationJourneyState) =
        mapOf(
            "fieldSetHeading" to "forms.reason.propertyDeregistration.fieldSetHeading",
            "fieldSetHint" to "forms.reason.propertyDeregistration.fieldSetHint",
            "limit" to DEREGISTRATION_REASON_MAX_LENGTH,
            "submitButtonText" to "forms.buttons.continue",
        )

    override fun chooseTemplate(state: PropertyDeregistrationJourneyState) = "forms/deregistrationReasonForm"

    override fun mode(state: PropertyDeregistrationJourneyState): Complete? = getFormModelFromStateOrNull(state)?.let { Complete.COMPLETE }

    override fun afterStepDataIsAdded(state: PropertyDeregistrationJourneyState) {
        val propertyOwnership = state.getPropertyOwnership()

        val primaryLandlordEmailAddress = propertyOwnership.primaryLandlord.email
        val propertyRegistrationNumber = propertyOwnership.registrationNumber
        val propertyAddress = propertyOwnership.address.singleLineAddress

        propertyDeregistrationService.deregisterProperty(state.propertyOwnershipId)
        propertyDeregistrationService.addDeregisteredPropertyOwnershipIdToSession(state.propertyOwnershipId)

        confirmationEmailSender.sendEmail(
            primaryLandlordEmailAddress,
            PropertyDeregistrationConfirmationEmail(
                RegistrationNumberDataModel.fromRegistrationNumber(propertyRegistrationNumber).toString(),
                propertyAddress,
            ),
        )
    }
}

@JourneyFrameworkComponent
final class ReasonStep(
    stepConfig: ReasonStepConfig,
) : RequestableStep<Complete, PropertyDeregistrationReasonFormModel, PropertyDeregistrationJourneyState>(stepConfig) {
    companion object {
        const val ROUTE_SEGMENT = "reason"
    }
}
