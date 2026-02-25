package uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps

import kotlinx.datetime.toJavaLocalDate
import org.springframework.context.MessageSource
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.database.entity.PropertyCompliance
import uk.gov.communities.prsdb.webapp.helpers.extensions.MessageSourceExtensions.Companion.getMessageForKey
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.EicrCyaSummaryRowsFactory
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.EpcCyaSummaryRowsFactory
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.GasSafetyCyaSummaryRowsFactory
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.PropertyComplianceJourneyState
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.AbstractCheckYourAnswersStep
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.AbstractCheckYourAnswersStepConfig
import uk.gov.communities.prsdb.webapp.models.dataModels.RegistrationNumberDataModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.CheckAnswersFormModel
import uk.gov.communities.prsdb.webapp.models.viewModels.PropertyComplianceConfirmationMessageKeys
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.EmailBulletPointList
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.FullPropertyComplianceConfirmationEmail
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.PartialPropertyComplianceConfirmationEmail
import uk.gov.communities.prsdb.webapp.services.AbsoluteUrlProvider
import uk.gov.communities.prsdb.webapp.services.EmailNotificationService
import uk.gov.communities.prsdb.webapp.services.EpcCertificateUrlProvider
import uk.gov.communities.prsdb.webapp.services.PropertyComplianceService
import uk.gov.communities.prsdb.webapp.services.PropertyOwnershipService
import uk.gov.communities.prsdb.webapp.services.UploadService

@JourneyFrameworkComponent
class PropertyComplianceCyaStepConfig(
    private val uploadService: UploadService,
    private val propertyOwnershipService: PropertyOwnershipService,
    private val epcCertificateUrlProvider: EpcCertificateUrlProvider,
    private val propertyComplianceService: PropertyComplianceService,
    private val messageSource: MessageSource,
    private val fullPropertyComplianceConfirmationEmailService: EmailNotificationService<FullPropertyComplianceConfirmationEmail>,
    private val partialPropertyComplianceConfirmationEmailService: EmailNotificationService<PartialPropertyComplianceConfirmationEmail>,
    private val absoluteUrlProvider: AbsoluteUrlProvider,
) : AbstractCheckYourAnswersStepConfig<PropertyComplianceJourneyState>() {
    override fun chooseTemplate(state: PropertyComplianceJourneyState) = "forms/propertyComplianceCheckAnswersForm"

    override fun getStepSpecificContent(state: PropertyComplianceJourneyState) =
        mapOf(
            "propertyAddress" to propertyOwnershipService.getPropertyOwnership(state.propertyId).address.singleLineAddress,
            "gasSafetyData" to getGasSafetyData(state),
            "eicrData" to getEicrData(state),
            "epcData" to getEpcData(state),
            "submittedFilteredJourneyData" to CheckAnswersFormModel.serializeJourneyData(state.getSubmittedStepData()),
        )

    override fun afterStepDataIsAdded(state: PropertyComplianceJourneyState) {
        val epcDetails = state.acceptedEpc

        val propertyCompliance =
            propertyComplianceService.createPropertyCompliance(
                propertyOwnershipId = state.propertyId,
                gasSafetyCertUploadId = state.getGasSafetyCertificateFileUploadIdIfReachable(),
                gasSafetyCertIssueDate = state.getGasSafetyCertificateIssueDateIfReachable()?.toJavaLocalDate(),
                gasSafetyCertEngineerNum = state.gasSafetyEngineerNumberStep.formModelIfReachableOrNull?.engineerNumber,
                gasSafetyCertExemptionReason = state.gasSafetyExemptionReasonStep.formModelIfReachableOrNull?.exemptionReason,
                gasSafetyCertExemptionOtherReason = state.gasSafetyExemptionOtherReasonStep.formModelIfReachableOrNull?.otherReason,
                eicrUploadId = state.getEicrCertificateFileUploadId(),
                eicrIssueDate = state.getEicrCertificateIssueDate()?.toJavaLocalDate(),
                eicrExemptionReason = state.eicrExemptionReasonStep.formModelIfReachableOrNull?.exemptionReason,
                eicrExemptionOtherReason = state.eicrExemptionOtherReasonStep.formModelIfReachableOrNull?.otherReason,
                epcUrl = epcDetails?.let { epcCertificateUrlProvider.getEpcCertificateUrl(it.certificateNumber) },
                epcExpiryDate = epcDetails?.expiryDate?.toJavaLocalDate(),
                tenancyStartedBeforeEpcExpiry = state.epcExpiryCheckStep.formModelIfReachableOrNull?.tenancyStartedBeforeExpiry,
                epcEnergyRating = epcDetails?.energyRating,
                epcExemptionReason = state.epcExemptionReasonStep.formModelIfReachableOrNull?.exemptionReason,
                epcMeesExemptionReason = state.meesExemptionReasonStep.formModelIfReachableOrNull?.exemptionReason,
            )

        sendConfirmationEmail(propertyCompliance)

        propertyComplianceService.addToPropertiesWithComplianceAddedThisSession(state.propertyId)

        // TODO PDJB-467 - delete the savedJourneyState for the incomplete compliance.
    }

    private fun sendConfirmationEmail(propertyCompliance: PropertyCompliance) {
        val landlordEmail = propertyCompliance.propertyOwnership.primaryLandlord.email
        val propertyAddress = propertyCompliance.propertyOwnership.address.singleLineAddress

        val confirmationMsgKeys = PropertyComplianceConfirmationMessageKeys(propertyCompliance)
        val compliantMsgs = confirmationMsgKeys.compliantMsgKeys.map { messageSource.getMessageForKey(it) }
        val nonCompliantMsgs = confirmationMsgKeys.nonCompliantMsgKeys.map { messageSource.getMessageForKey(it) }

        if (nonCompliantMsgs.isEmpty()) {
            fullPropertyComplianceConfirmationEmailService.sendEmail(
                landlordEmail,
                FullPropertyComplianceConfirmationEmail(
                    propertyAddress,
                    EmailBulletPointList(compliantMsgs),
                    absoluteUrlProvider.buildLandlordDashboardUri().toString(),
                ),
            )
        } else {
            partialPropertyComplianceConfirmationEmailService.sendEmail(
                landlordEmail,
                PartialPropertyComplianceConfirmationEmail(
                    propertyAddress,
                    RegistrationNumberDataModel.fromRegistrationNumber(propertyCompliance.propertyOwnership.registrationNumber),
                    EmailBulletPointList(nonCompliantMsgs),
                    absoluteUrlProvider.buildComplianceInformationUri(propertyCompliance.propertyOwnership.id).toString(),
                ),
            )
        }
    }

    fun getGasSafetyData(state: PropertyComplianceJourneyState) =
        GasSafetyCyaSummaryRowsFactory(
            (state.gasSafetyStep.outcome == GasSafetyMode.HAS_CERTIFICATE),
            Destination.VisitableStep(state.gasSafetyStep, childJourneyId),
            Destination.VisitableStep(state.gasSafetyExemptionStep, childJourneyId),
            uploadService,
            state,
            childJourneyId,
        ).createRows()

    fun getEicrData(state: PropertyComplianceJourneyState) =
        EicrCyaSummaryRowsFactory(
            (state.eicrStep.outcome == EicrMode.HAS_CERTIFICATE),
            Destination.VisitableStep(state.eicrStep, childJourneyId),
            Destination.VisitableStep(state.eicrExemptionStep, childJourneyId),
            uploadService,
            state,
            childJourneyId,
        ).createRows()

    fun getEpcData(state: PropertyComplianceJourneyState) =
        EpcCyaSummaryRowsFactory(
            Destination.VisitableStep(state.epcQuestionStep, childJourneyId),
            epcCertificateUrlProvider,
            state,
            childJourneyId,
        ).createRows()
}

@JourneyFrameworkComponent
final class PropertyComplianceCyaStep(
    stepConfig: PropertyComplianceCyaStepConfig,
) : AbstractCheckYourAnswersStep<PropertyComplianceJourneyState>(stepConfig)
