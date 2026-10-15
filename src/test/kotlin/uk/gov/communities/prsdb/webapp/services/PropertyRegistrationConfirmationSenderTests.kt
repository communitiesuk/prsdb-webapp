package uk.gov.communities.prsdb.webapp.services

import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.argThat
import org.mockito.kotlin.eq
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.config.managers.FeatureFlagManager
import uk.gov.communities.prsdb.webapp.constants.PROPERTY_REGISTRATION_PHASE_TWO
import uk.gov.communities.prsdb.webapp.constants.enums.PaymentStatus
import uk.gov.communities.prsdb.webapp.constants.enums.RegistrationNumberType
import uk.gov.communities.prsdb.webapp.database.entity.RegistrationNumber
import uk.gov.communities.prsdb.webapp.models.dataModels.RegistrationNumberDataModel
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.PropertyRegistrationConfirmationEmail
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.PropertyRegistrationConfirmationWithPaymentEmail
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLandlordData
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockPaymentData
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockPropertyRegistrationData
import java.net.URI
import java.time.Instant
import java.time.LocalDate

class PropertyRegistrationConfirmationSenderTests {
    private val currentUserEmail = "admin@example.com"
    private val dashboardUri = URI("https://gov.uk")

    @Nested
    @ExtendWith(MockitoExtension::class)
    inner class PaymentsFlagOff {
        @Mock
        private lateinit var mockAbsoluteUrlProvider: AbsoluteUrlProvider

        @Mock
        private lateinit var mockConfirmationEmailSender: EmailNotificationService<PropertyRegistrationConfirmationEmail>

        @Mock
        private lateinit var mockFeatureFlagManager: FeatureFlagManager

        @Mock
        private lateinit var mockCurrentUserService: CurrentUserService

        @InjectMocks
        private lateinit var confirmationSender: PropertyRegistrationConfirmationSenderImplFlagOff

        @Test
        fun `sendConfirmationEmail sends the email to the current user with the property and dashboard details`() {
            // Arrange
            val landlord = MockLandlordData.createIndividualLandlord()
            val registrationNumber = RegistrationNumber(RegistrationNumberType.PROPERTY, 5678)
            val propertyOwnership =
                MockLandlordData.createPropertyOwnership(
                    landlords = mutableSetOf(landlord),
                    registrationNumber = registrationNumber,
                    currentNumTenants = 2,
                )
            whenever(mockCurrentUserService.getCurrentEmail()).thenReturn(currentUserEmail)
            whenever(mockAbsoluteUrlProvider.buildLandlordDashboardUri()).thenReturn(dashboardUri)

            // Act
            confirmationSender.sendConfirmationEmail(
                MockPropertyRegistrationData.createPropertyRegistrationDataModel(),
                landlord,
                propertyOwnership,
            )

            // Assert
            verify(mockConfirmationEmailSender).sendEmail(
                eq(currentUserEmail),
                argThat<PropertyRegistrationConfirmationEmail> { email ->
                    email.prn == RegistrationNumberDataModel.fromRegistrationNumber(registrationNumber).toString() &&
                        email.multiLineAddress == propertyOwnership.address.toMultiLineAddress() &&
                        email.prsdUrl == dashboardUri.toString() &&
                        email.isOccupied == propertyOwnership.isOccupied
                },
            )
        }

        @Test
        fun `sendConfirmationEmail includes the joint landlord emails and delegation status from the registration data`() {
            // Arrange
            val landlord = MockLandlordData.createIndividualLandlord()
            val propertyOwnership = MockLandlordData.createPropertyOwnership(landlords = mutableSetOf(landlord))
            val jointLandlordEmails = listOf("joint1@example.com", "joint2@example.com")
            whenever(mockCurrentUserService.getCurrentEmail()).thenReturn(currentUserEmail)
            whenever(mockAbsoluteUrlProvider.buildLandlordDashboardUri()).thenReturn(dashboardUri)

            // Act
            confirmationSender.sendConfirmationEmail(
                MockPropertyRegistrationData.createPropertyRegistrationDataModel(
                    jointLandlordEmails = jointLandlordEmails,
                    isDelegatedToLettingAgent = true,
                ),
                landlord,
                propertyOwnership,
            )

            // Assert
            verify(mockConfirmationEmailSender).sendEmail(
                eq(currentUserEmail),
                argThat<PropertyRegistrationConfirmationEmail> { email ->
                    email.jointLandlordEmails == jointLandlordEmails && email.isDelegatedToLettingAgent
                },
            )
        }

        @Test
        fun `sendConfirmationEmail passes the provide-later flags and enabled phase two flag to the email`() {
            // Arrange
            val landlord = MockLandlordData.createIndividualLandlord()
            val propertyOwnership = MockLandlordData.createPropertyOwnership(landlords = mutableSetOf(landlord))
            whenever(mockCurrentUserService.getCurrentEmail()).thenReturn(currentUserEmail)
            whenever(mockAbsoluteUrlProvider.buildLandlordDashboardUri()).thenReturn(dashboardUri)
            whenever(mockFeatureFlagManager.checkFeature(PROPERTY_REGISTRATION_PHASE_TWO)).thenReturn(true)

            // Act
            confirmationSender.sendConfirmationEmail(
                MockPropertyRegistrationData.createPropertyRegistrationDataModel(
                    licenseProvideLater = true,
                    gasSafetyCertProvideLater = true,
                    electricalSafetyCertProvideLater = true,
                    epcProvideLater = true,
                    tenancyProvideLater = true,
                ),
                landlord,
                propertyOwnership,
            )

            // Assert
            verify(mockConfirmationEmailSender).sendEmail(
                eq(currentUserEmail),
                argThat<PropertyRegistrationConfirmationEmail> { email ->
                    email.isPdjb939PhaseTwoEnabled &&
                        email.licenseProvideLater &&
                        email.gasSafetyCertProvideLater &&
                        email.electricalSafetyCertProvideLater &&
                        email.epcProvideLater &&
                        email.tenancyProvideLater
                },
            )
        }

        @Test
        fun `sendConfirmationEmail marks phase two as disabled when the feature flag is off`() {
            // Arrange
            val landlord = MockLandlordData.createIndividualLandlord()
            val propertyOwnership = MockLandlordData.createPropertyOwnership(landlords = mutableSetOf(landlord))
            whenever(mockCurrentUserService.getCurrentEmail()).thenReturn(currentUserEmail)
            whenever(mockAbsoluteUrlProvider.buildLandlordDashboardUri()).thenReturn(dashboardUri)
            whenever(mockFeatureFlagManager.checkFeature(PROPERTY_REGISTRATION_PHASE_TWO)).thenReturn(false)

            // Act
            confirmationSender.sendConfirmationEmail(
                MockPropertyRegistrationData.createPropertyRegistrationDataModel(),
                landlord,
                propertyOwnership,
            )

            // Assert
            verify(mockConfirmationEmailSender).sendEmail(
                eq(currentUserEmail),
                argThat<PropertyRegistrationConfirmationEmail> { email -> !email.isPdjb939PhaseTwoEnabled },
            )
        }
    }

    @Nested
    @ExtendWith(MockitoExtension::class)
    inner class PaymentsFlagOn {
        @Mock
        private lateinit var mockAbsoluteUrlProvider: AbsoluteUrlProvider

        @Mock
        private lateinit var mockConfirmationEmailSender: EmailNotificationService<PropertyRegistrationConfirmationWithPaymentEmail>

        @Mock
        private lateinit var mockFeatureFlagManager: FeatureFlagManager

        @Mock
        private lateinit var mockCurrentUserService: CurrentUserService

        @InjectMocks
        private lateinit var confirmationSender: PropertyRegistrationConfirmationSenderImplFlagOn

        @Test
        fun `sendConfirmationEmail sends the email with the payment and renewal details`() {
            // Arrange
            val landlord = MockLandlordData.createIndividualLandlord()
            val propertyOwnership =
                MockLandlordData.createPropertyOwnership(
                    landlords = mutableSetOf(landlord),
                    renewalDate = LocalDate.of(2027, 12, 16),
                )
            propertyOwnership.payments.add(
                MockPaymentData.createPayment(
                    amountInPence = 61,
                    paymentCreatedAt = Instant.parse("2026-12-16T10:15:30Z"),
                    status = PaymentStatus.SUCCEEDED,
                ),
            )
            whenever(mockCurrentUserService.getCurrentEmail()).thenReturn(currentUserEmail)
            whenever(mockAbsoluteUrlProvider.buildLandlordDashboardUri()).thenReturn(dashboardUri)

            // Act
            confirmationSender.sendConfirmationEmail(
                MockPropertyRegistrationData.createPropertyRegistrationDataModel(),
                landlord,
                propertyOwnership,
            )

            // Assert
            verify(mockConfirmationEmailSender).sendEmail(
                eq(currentUserEmail),
                argThat<PropertyRegistrationConfirmationWithPaymentEmail> { email ->
                    email.paymentAmount == "£0.61" &&
                        email.paymentDate == "16 December 2026" &&
                        email.renewalDate == "16 December 2027" &&
                        email.prsdUrl == dashboardUri.toString() &&
                        email.multiLineAddress == propertyOwnership.address.toMultiLineAddress()
                },
            )
        }

        @Test
        fun `sendConfirmationEmail passes the registration data details to the email`() {
            // Arrange
            val landlord = MockLandlordData.createIndividualLandlord()
            val propertyOwnership = MockLandlordData.createPropertyOwnership(landlords = mutableSetOf(landlord))
            propertyOwnership.payments.add(MockPaymentData.createPayment(status = PaymentStatus.SUCCEEDED))
            val jointLandlordEmails = listOf("joint1@example.com", "joint2@example.com")
            whenever(mockCurrentUserService.getCurrentEmail()).thenReturn(currentUserEmail)
            whenever(mockAbsoluteUrlProvider.buildLandlordDashboardUri()).thenReturn(dashboardUri)
            whenever(mockFeatureFlagManager.checkFeature(PROPERTY_REGISTRATION_PHASE_TWO)).thenReturn(true)

            // Act
            confirmationSender.sendConfirmationEmail(
                MockPropertyRegistrationData.createPropertyRegistrationDataModel(
                    jointLandlordEmails = jointLandlordEmails,
                    isDelegatedToLettingAgent = true,
                    licenseProvideLater = true,
                    gasSafetyCertProvideLater = true,
                    electricalSafetyCertProvideLater = true,
                    epcProvideLater = true,
                    tenancyProvideLater = true,
                ),
                landlord,
                propertyOwnership,
            )

            // Assert
            verify(mockConfirmationEmailSender).sendEmail(
                eq(currentUserEmail),
                argThat<PropertyRegistrationConfirmationWithPaymentEmail> { email ->
                    email.jointLandlordEmails == jointLandlordEmails &&
                        email.isDelegatedToLettingAgent &&
                        email.isPdjb939PhaseTwoEnabled &&
                        email.licenseProvideLater &&
                        email.gasSafetyCertProvideLater &&
                        email.electricalSafetyCertProvideLater &&
                        email.epcProvideLater &&
                        email.tenancyProvideLater
                },
            )
        }

        @Test
        fun `sendConfirmationEmail ignores payments that have not succeeded`() {
            // Arrange
            val landlord = MockLandlordData.createIndividualLandlord()
            val propertyOwnership = MockLandlordData.createPropertyOwnership(landlords = mutableSetOf(landlord))
            propertyOwnership.payments.add(MockPaymentData.createPayment(paymentId = "succeeded", status = PaymentStatus.SUCCEEDED))
            propertyOwnership.payments.add(MockPaymentData.createPayment(paymentId = "failed", status = PaymentStatus.FAILED))
            whenever(mockCurrentUserService.getCurrentEmail()).thenReturn(currentUserEmail)
            whenever(mockAbsoluteUrlProvider.buildLandlordDashboardUri()).thenReturn(dashboardUri)

            // Act
            confirmationSender.sendConfirmationEmail(
                MockPropertyRegistrationData.createPropertyRegistrationDataModel(),
                landlord,
                propertyOwnership,
            )

            // Assert
            verify(mockConfirmationEmailSender).sendEmail(eq(currentUserEmail), any<PropertyRegistrationConfirmationWithPaymentEmail>())
        }

        @Test
        fun `sendConfirmationEmail throws and sends no email when there is no successful payment`() {
            // Arrange
            val landlord = MockLandlordData.createIndividualLandlord()
            val propertyOwnership = MockLandlordData.createPropertyOwnership(landlords = mutableSetOf(landlord))
            propertyOwnership.payments.add(MockPaymentData.createPayment(status = PaymentStatus.FAILED))

            // Act, Assert
            assertThrows<IllegalStateException> {
                confirmationSender.sendConfirmationEmail(
                    MockPropertyRegistrationData.createPropertyRegistrationDataModel(),
                    landlord,
                    propertyOwnership,
                )
            }
            verify(mockConfirmationEmailSender, never()).sendEmail(any(), any<PropertyRegistrationConfirmationWithPaymentEmail>())
        }

        @Test
        fun `sendConfirmationEmail throws and sends no email when there are multiple successful payments`() {
            // Arrange
            val landlord = MockLandlordData.createIndividualLandlord()
            val propertyOwnership = MockLandlordData.createPropertyOwnership(landlords = mutableSetOf(landlord))
            propertyOwnership.payments.add(MockPaymentData.createPayment(paymentId = "first", status = PaymentStatus.SUCCEEDED))
            propertyOwnership.payments.add(MockPaymentData.createPayment(paymentId = "second", status = PaymentStatus.SUCCEEDED))

            // Act, Assert
            assertThrows<IllegalStateException> {
                confirmationSender.sendConfirmationEmail(
                    MockPropertyRegistrationData.createPropertyRegistrationDataModel(),
                    landlord,
                    propertyOwnership,
                )
            }
            verify(mockConfirmationEmailSender, never()).sendEmail(any(), any<PropertyRegistrationConfirmationWithPaymentEmail>())
        }
    }
}
