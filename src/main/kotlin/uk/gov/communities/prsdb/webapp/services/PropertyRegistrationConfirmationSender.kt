package uk.gov.communities.prsdb.webapp.services

import org.springframework.context.annotation.Conditional
import org.springframework.context.annotation.Primary
import org.springframework.stereotype.Service
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbFlip
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.WebServerOnly
import uk.gov.communities.prsdb.webapp.config.managers.FeatureFlagManager
import uk.gov.communities.prsdb.webapp.constants.PAYMENTS
import uk.gov.communities.prsdb.webapp.constants.PROPERTY_REGISTRATION_PHASE_TWO
import uk.gov.communities.prsdb.webapp.constants.enums.PaymentStatus
import uk.gov.communities.prsdb.webapp.database.entity.Landlord
import uk.gov.communities.prsdb.webapp.database.entity.PropertyOwnership
import uk.gov.communities.prsdb.webapp.helpers.DateTimeHelper
import uk.gov.communities.prsdb.webapp.helpers.PaymentHelper
import uk.gov.communities.prsdb.webapp.models.dataModels.PropertyRegistrationDataModel
import uk.gov.communities.prsdb.webapp.models.dataModels.RegistrationNumberDataModel
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.PropertyRegistrationConfirmationEmail
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.PropertyRegistrationConfirmationWithPaymentEmail
import java.time.format.DateTimeFormatter
import java.util.Locale

// Implementations use @Service rather than @PrsdbWebService so FF4J can read their bean names without creating every bean
@PrsdbFlip(name = PAYMENTS, alterBean = "property-registration-confirmation-sender-flag-on")
interface PropertyRegistrationConfirmationSender {
    fun sendConfirmationEmail(
        registrationData: PropertyRegistrationDataModel,
        landlord: Landlord,
        propertyOwnership: PropertyOwnership,
    )
}

@Primary
@Conditional(WebServerOnly::class)
@Service("property-registration-confirmation-sender-flag-off")
class PropertyRegistrationConfirmationSenderImplFlagOff(
    private val absoluteUrlProvider: AbsoluteUrlProvider,
    private val confirmationEmailSender: EmailNotificationService<PropertyRegistrationConfirmationEmail>,
    private val featureFlagManager: FeatureFlagManager,
    private val currentUserService: CurrentUserService,
) : PropertyRegistrationConfirmationSender {
    override fun sendConfirmationEmail(
        registrationData: PropertyRegistrationDataModel,
        landlord: Landlord,
        propertyOwnership: PropertyOwnership,
    ) = with(registrationData) {
        // TODO: PDJB-1274: Update emails to account for org landlord (check which org email address to use, currently registrant)
        confirmationEmailSender.sendEmail(
            currentUserService.getCurrentEmail(),
            PropertyRegistrationConfirmationEmail(
                RegistrationNumberDataModel
                    .fromRegistrationNumber(propertyOwnership.registrationNumber)
                    .toString(),
                propertyOwnership.address.toMultiLineAddress(),
                absoluteUrlProvider.buildLandlordDashboardUri().toString(),
                propertyOwnership.isOccupied,
                jointLandlordEmails,
                isDelegatedToLettingAgent,
                isPdjb939PhaseTwoEnabled = featureFlagManager.checkFeature(PROPERTY_REGISTRATION_PHASE_TWO),
                licenseProvideLater = licenseProvideLater,
                gasSafetyCertProvideLater = gasSafetyCertProvideLater,
                electricalSafetyCertProvideLater = electricalSafetyCertProvideLater,
                epcProvideLater = epcProvideLater,
                tenancyProvideLater = tenancyProvideLater,
            ),
        )
    }
}

@Conditional(WebServerOnly::class)
@Service("property-registration-confirmation-sender-flag-on")
class PropertyRegistrationConfirmationSenderImplFlagOn(
    private val absoluteUrlProvider: AbsoluteUrlProvider,
    private val confirmationEmailSender: EmailNotificationService<PropertyRegistrationConfirmationWithPaymentEmail>,
    private val featureFlagManager: FeatureFlagManager,
    private val currentUserService: CurrentUserService,
) : PropertyRegistrationConfirmationSender {
    override fun sendConfirmationEmail(
        registrationData: PropertyRegistrationDataModel,
        landlord: Landlord,
        propertyOwnership: PropertyOwnership,
    ) = with(registrationData) {
        val successfulPayments = propertyOwnership.payments.filter { it.status == PaymentStatus.SUCCEEDED }
        if (successfulPayments.size != 1) {
            val message =
                "ALERT: Expected exactly one successful payment for newly registered property ownership ${propertyOwnership.id} " +
                    "but found ${successfulPayments.size}, so the registration confirmation email was not sent"
            println(message)
            throw IllegalStateException(message)
        }
        val payment = successfulPayments.single()

        // TODO: PDJB-1274: Update emails to account for org landlord (check which org email address to use, currently registrant)
        confirmationEmailSender.sendEmail(
            currentUserService.getCurrentEmail(),
            PropertyRegistrationConfirmationWithPaymentEmail(
                RegistrationNumberDataModel
                    .fromRegistrationNumber(propertyOwnership.registrationNumber)
                    .toString(),
                propertyOwnership.address.toMultiLineAddress(),
                absoluteUrlProvider.buildLandlordDashboardUri().toString(),
                propertyOwnership.isOccupied,
                paymentAmount = PaymentHelper.formatPence(payment.amountInPence),
                paymentDate = payment.paymentCreatedAt.atZone(DateTimeHelper.UK_ZONE).toLocalDate().format(DATE_FORMATTER),
                renewalDate = propertyOwnership.renewalDate.format(DATE_FORMATTER),
                jointLandlordEmails = jointLandlordEmails,
                isDelegatedToLettingAgent = isDelegatedToLettingAgent,
                isPdjb939PhaseTwoEnabled = featureFlagManager.checkFeature(PROPERTY_REGISTRATION_PHASE_TWO),
                licenseProvideLater = licenseProvideLater,
                gasSafetyCertProvideLater = gasSafetyCertProvideLater,
                electricalSafetyCertProvideLater = electricalSafetyCertProvideLater,
                epcProvideLater = epcProvideLater,
                tenancyProvideLater = tenancyProvideLater,
            ),
        )
    }

    companion object {
        private val DATE_FORMATTER = DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.UK)
    }
}
