package uk.gov.communities.prsdb.webapp.services

import jakarta.persistence.EntityExistsException
import jakarta.servlet.http.HttpSession
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.verifyNoInteractions
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.eq
import org.mockito.kotlin.inOrder
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.http.HttpStatus
import org.springframework.transaction.TransactionStatus
import org.springframework.transaction.support.SimpleTransactionStatus
import org.springframework.transaction.support.TransactionTemplate
import org.springframework.web.server.ResponseStatusException
import uk.gov.communities.prsdb.webapp.constants.LANDLORD_REACTIVATED_BY_LAST_PROPERTY_REACTIVATION
import uk.gov.communities.prsdb.webapp.constants.LAST_PROPERTY_REACTIVATION_WAS_PAID
import uk.gov.communities.prsdb.webapp.constants.REACTIVATED_PROPERTY_OWNERSHIP_ID
import uk.gov.communities.prsdb.webapp.constants.enums.CertificateType
import uk.gov.communities.prsdb.webapp.constants.enums.FurnishedStatus
import uk.gov.communities.prsdb.webapp.constants.enums.LicensingType
import uk.gov.communities.prsdb.webapp.constants.enums.PaymentStatus
import uk.gov.communities.prsdb.webapp.constants.enums.RentFrequency
import uk.gov.communities.prsdb.webapp.database.entity.Payment
import uk.gov.communities.prsdb.webapp.database.entity.PropertyOwnership
import uk.gov.communities.prsdb.webapp.exceptions.PaymentQuoteExpiredException
import uk.gov.communities.prsdb.webapp.exceptions.PropertyAlreadyReactivatedException
import uk.gov.communities.prsdb.webapp.helpers.DateTimeHelper
import uk.gov.communities.prsdb.webapp.models.dataModels.ElectricalSafetyDetailsDataModel
import uk.gov.communities.prsdb.webapp.models.dataModels.EpcDetailsDataModel
import uk.gov.communities.prsdb.webapp.models.dataModels.GasSafetyDetailsDataModel
import uk.gov.communities.prsdb.webapp.models.dataModels.LicensingDetailsDataModel
import uk.gov.communities.prsdb.webapp.models.dataModels.PropertyPaymentQuote
import uk.gov.communities.prsdb.webapp.models.dataModels.PropertyReactivationDataModel
import uk.gov.communities.prsdb.webapp.models.dataModels.TenancyDetailsDataModel
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLandlordData
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockPaymentData
import java.math.BigDecimal
import java.time.LocalDate
import java.util.function.Consumer

@ExtendWith(MockitoExtension::class)
class PropertyReactivationServiceTests {
    @Mock
    private lateinit var mockPropertyOwnershipService: PropertyOwnershipService

    @Mock
    private lateinit var mockPropertyComplianceService: PropertyComplianceService

    @Mock
    private lateinit var mockActivationService: ActivationService

    @Mock
    private lateinit var mockAddressAvailabilityService: AddressAvailabilityService

    @Mock
    private lateinit var mockLandlordActiveStatusService: LandlordActiveStatusService

    @Mock
    private lateinit var mockSession: HttpSession

    @Mock
    private lateinit var mockPropertyReactivationEmailService: PropertyReactivationEmailService

    @Mock
    private lateinit var mockUserToLandlordService: UserToLandlordService

    @Mock
    private lateinit var mockPaymentService: PaymentService

    @Mock
    private lateinit var mockPaymentReconciliationService: PaymentReconciliationService

    @Mock
    private lateinit var mockTransactionTemplate: TransactionTemplate

    @InjectMocks
    private lateinit var propertyReactivationService: PropertyReactivationService

    private val propertyOwnershipId = 1L
    private val uprn = 2L
    private val landlord = MockLandlordData.createIndividualLandlord()
    private val propertyOwnership =
        MockLandlordData.createPropertyOwnership(
            id = propertyOwnershipId,
            isActive = false,
            landlords = mutableSetOf(landlord),
            address = MockLandlordData.createAddress(uprn = uprn),
        )
    private val freeQuote = PropertyPaymentQuote(amountInPence = 0, quoteDate = TODAY, renewalDate = QUOTED_RENEWAL_DATE)
    private val paidQuote = PropertyPaymentQuote(amountInPence = PAID_AMOUNT, quoteDate = TODAY, renewalDate = QUOTED_RENEWAL_DATE)
    private var isInTransaction = false

    private fun stubTransaction() {
        doAnswer { invocation ->
            isInTransaction = true
            try {
                invocation.getArgument<Consumer<TransactionStatus>>(0).accept(SimpleTransactionStatus())
            } finally {
                isInTransaction = false
            }
        }.whenever(mockTransactionTemplate).executeWithoutResult(any())
    }

    private fun stubFreeReactivation(
        property: PropertyOwnership = propertyOwnership,
        isLandlordInactive: Boolean = false,
    ) {
        stubTransaction()
        whenever(mockPropertyOwnershipService.getInactivePropertyOwnershipForCurrentLandlord(propertyOwnershipId)).thenReturn(property)
        whenever(mockLandlordActiveStatusService.isCurrentLandlordInactive()).thenReturn(isLandlordInactive)
        property.address.uprn?.let {
            whenever(mockAddressAvailabilityService.hasAnotherActiveRegistration(it, propertyOwnershipId)).thenReturn(false)
        }
        whenever(mockActivationService.claimPropertyForReactivation(propertyOwnershipId)).thenReturn(property)
        whenever(mockUserToLandlordService.getCurrentLandlordForUser()).thenReturn(landlord)
        whenever(mockPaymentService.calculatePropertyReactivationPaymentQuote(eq(property), eq(landlord), any())).thenReturn(freeQuote)
    }

    private fun paymentForProperty(
        quoteDate: LocalDate = TODAY,
        amountInPence: Int = PAID_AMOUNT,
        forPeriodEnding: LocalDate = QUOTED_RENEWAL_DATE,
    ) = MockPaymentData.createReactivationPayment(
        paymentId = "payment-id",
        amountInPence = amountInPence,
        quoteDate = quoteDate,
        forPeriodEnding = forPeriodEnding,
        status = PaymentStatus.SUCCEEDED,
        propertyOwnership = propertyOwnership,
    )

    private fun stubClaimSucceeds() {
        whenever(mockAddressAvailabilityService.hasAnotherActiveRegistration(uprn, propertyOwnershipId)).thenReturn(false)
        whenever(mockActivationService.claimPropertyForReactivation(propertyOwnershipId)).thenReturn(propertyOwnership)
    }

    @Nested
    inner class ReactivatePropertyWithoutPayment {
        @Test
        fun `claims the property, saves the details provided, then reactivates it with the quoted renewal date`() {
            // Arrange
            stubFreeReactivation()
            val licensing = LicensingDetailsDataModel(LicensingType.NO_LICENSING, null)
            val gasSafety = GasSafetyDetailsDataModel(hasGasSupply = false, issueDate = null, uploadIds = emptyList())
            val electricalSafety = ElectricalSafetyDetailsDataModel(CertificateType.Eicr, LocalDate.of(2030, 1, 1), null, null, listOf(2L))
            val epc = EpcDetailsDataModel("https://epc.example/cert", LocalDate.of(2031, 1, 1), "C", null, null, null)
            val tenancyDetails =
                TenancyDetailsDataModel(1, 2, null, null, FurnishedStatus.FURNISHED, RentFrequency.MONTHLY, null, BigDecimal("400"))

            // Act
            propertyReactivationService.reactivatePropertyWithoutPayment(
                propertyOwnershipId,
                PropertyReactivationDataModel(licensing, gasSafety, electricalSafety, epc, tenancyDetails),
            )

            // Assert
            inOrder(
                mockAddressAvailabilityService,
                mockActivationService,
                mockPaymentService,
                mockPropertyOwnershipService,
                mockPropertyComplianceService,
            ) {
                verify(mockAddressAvailabilityService).hasAnotherActiveRegistration(uprn, propertyOwnershipId)
                verify(mockActivationService).claimPropertyForReactivation(propertyOwnershipId)
                verify(mockPaymentService).calculatePropertyReactivationPaymentQuote(eq(propertyOwnership), eq(landlord), any())
                verify(mockPropertyOwnershipService).saveReactivationLicensing(propertyOwnership, licensing)
                verify(mockPropertyOwnershipService).saveReactivationTenancyDetails(propertyOwnership, tenancyDetails)
                verify(mockPropertyComplianceService).saveReactivationComplianceData(propertyOwnership, gasSafety, electricalSafety, epc)
                verify(mockActivationService).reactivateClaimedProperty(propertyOwnership, QUOTED_RENEWAL_DATE, null)
            }
        }

        @Test
        fun `doesn't save any details when the property had none to provide`() {
            // Arrange
            stubFreeReactivation()

            // Act
            propertyReactivationService.reactivatePropertyWithoutPayment(propertyOwnershipId, PropertyReactivationDataModel())

            // Assert
            verify(mockPropertyOwnershipService, never()).saveReactivationLicensing(any(), any())
            verify(mockPropertyOwnershipService, never()).saveReactivationTenancyDetails(any(), any())
            verifyNoInteractions(mockPropertyComplianceService)
            verify(mockActivationService).reactivateClaimedProperty(propertyOwnership, QUOTED_RENEWAL_DATE, null)
        }

        @Test
        fun `stores the property in the session as the last property reactivated, without a payment`() {
            // Arrange
            stubFreeReactivation()

            // Act
            propertyReactivationService.reactivatePropertyWithoutPayment(propertyOwnershipId, PropertyReactivationDataModel())

            // Assert
            verify(mockSession).setAttribute(REACTIVATED_PROPERTY_OWNERSHIP_ID, propertyOwnershipId)
            verify(mockSession).setAttribute(LANDLORD_REACTIVATED_BY_LAST_PROPERTY_REACTIVATION, false)
            verify(mockSession).setAttribute(LAST_PROPERTY_REACTIVATION_WAS_PAID, false)
        }

        @Test
        fun `sends a confirmation email for an active landlord`() {
            // Arrange
            stubFreeReactivation()

            // Act
            propertyReactivationService.reactivatePropertyWithoutPayment(propertyOwnershipId, PropertyReactivationDataModel())

            // Assert
            verify(mockPropertyReactivationEmailService).sendConfirmationEmail(propertyOwnershipId, landlordReactivated = false)
        }

        @Test
        fun `records and emails that the landlord was reactivated when they were inactive before the property was reactivated`() {
            // Arrange
            stubFreeReactivation(isLandlordInactive = true)

            // Act
            propertyReactivationService.reactivatePropertyWithoutPayment(propertyOwnershipId, PropertyReactivationDataModel())

            // Assert
            inOrder(mockLandlordActiveStatusService, mockActivationService) {
                verify(mockLandlordActiveStatusService).isCurrentLandlordInactive()
                verify(mockActivationService).reactivateClaimedProperty(propertyOwnership, QUOTED_RENEWAL_DATE, null)
            }
            verify(mockSession).setAttribute(LANDLORD_REACTIVATED_BY_LAST_PROPERTY_REACTIVATION, true)
            verify(mockPropertyReactivationEmailService).sendConfirmationEmail(propertyOwnershipId, landlordReactivated = true)
        }

        @Test
        fun `still reactivates the property when the confirmation email fails to send`() {
            // Arrange
            stubFreeReactivation()
            whenever(mockPropertyReactivationEmailService.sendConfirmationEmail(any(), any())).thenThrow(RuntimeException("Notify is down"))

            // Act & Assert
            assertDoesNotThrow {
                propertyReactivationService.reactivatePropertyWithoutPayment(propertyOwnershipId, PropertyReactivationDataModel())
            }
            verify(mockActivationService).reactivateClaimedProperty(propertyOwnership, QUOTED_RENEWAL_DATE, null)
            verify(mockSession).setAttribute(REACTIVATED_PROPERTY_OWNERSHIP_ID, propertyOwnershipId)
        }

        @Test
        fun `notifies joint landlords of the reactivation`() {
            // Arrange
            stubFreeReactivation()

            // Act
            propertyReactivationService.reactivatePropertyWithoutPayment(propertyOwnershipId, PropertyReactivationDataModel())

            // Assert
            verify(mockPropertyReactivationEmailService).sendJointLandlordNotifications(propertyOwnershipId)
        }

        @Test
        fun `still sends the confirmation email when the joint landlord notifications fail`() {
            // Arrange
            stubFreeReactivation()
            whenever(mockPropertyReactivationEmailService.sendJointLandlordNotifications(any()))
                .thenThrow(RuntimeException("Notify is down"))

            // Act & Assert
            assertDoesNotThrow {
                propertyReactivationService.reactivatePropertyWithoutPayment(propertyOwnershipId, PropertyReactivationDataModel())
            }
            verify(mockPropertyReactivationEmailService).sendConfirmationEmail(propertyOwnershipId, landlordReactivated = false)
        }

        @Test
        fun `throws an EntityExistsException without claiming or saving when another property has the address`() {
            // Arrange
            stubTransaction()
            whenever(mockPropertyOwnershipService.getInactivePropertyOwnershipForCurrentLandlord(propertyOwnershipId))
                .thenReturn(propertyOwnership)
            whenever(mockAddressAvailabilityService.hasAnotherActiveRegistration(uprn, propertyOwnershipId)).thenReturn(true)

            // Act & Assert
            assertThrows<EntityExistsException> {
                propertyReactivationService.reactivatePropertyWithoutPayment(propertyOwnershipId, PropertyReactivationDataModel())
            }

            verifyNoInteractions(
                mockActivationService,
                mockPaymentService,
                mockPropertyComplianceService,
                mockSession,
                mockPropertyReactivationEmailService,
                mockPaymentReconciliationService,
            )
            verify(mockPropertyOwnershipService, never()).saveReactivationLicensing(any(), any())
        }

        @Test
        fun `reactivates the property without checking its address when it has no UPRN`() {
            // Arrange
            val propertyWithoutUprn =
                MockLandlordData.createPropertyOwnership(id = propertyOwnershipId, isActive = false, landlords = mutableSetOf(landlord))
            stubFreeReactivation(property = propertyWithoutUprn)

            // Act
            propertyReactivationService.reactivatePropertyWithoutPayment(propertyOwnershipId, PropertyReactivationDataModel())

            // Assert
            verify(mockActivationService).reactivateClaimedProperty(propertyWithoutUprn, QUOTED_RENEWAL_DATE, null)
            verifyNoInteractions(mockAddressAvailabilityService)
        }

        @Test
        fun `throws without saving, recording or cancelling anything when a joint landlord has already claimed the property`() {
            // Arrange
            stubTransaction()
            whenever(mockPropertyOwnershipService.getInactivePropertyOwnershipForCurrentLandlord(propertyOwnershipId))
                .thenReturn(propertyOwnership)
            whenever(mockAddressAvailabilityService.hasAnotherActiveRegistration(uprn, propertyOwnershipId)).thenReturn(false)
            whenever(mockActivationService.claimPropertyForReactivation(propertyOwnershipId))
                .thenThrow(PropertyAlreadyReactivatedException(propertyOwnershipId))

            // Act & Assert
            assertThrows<PropertyAlreadyReactivatedException> {
                propertyReactivationService.reactivatePropertyWithoutPayment(propertyOwnershipId, PropertyReactivationDataModel())
            }

            verify(mockActivationService, never()).reactivateClaimedProperty(any(), any(), anyOrNull())
            verify(mockPropertyOwnershipService, never()).saveReactivationLicensing(any(), any())
            verifyNoInteractions(mockPaymentService, mockSession, mockPropertyReactivationEmailService, mockPaymentReconciliationService)
        }

        @Test
        fun `cancels any created payment for the property after the reactivation transaction`() {
            // Arrange
            stubFreeReactivation()
            var wasCancelledInTransaction: Boolean? = null
            whenever(mockPaymentReconciliationService.cancelCreatedPaymentForProperty(propertyOwnershipId)).thenAnswer {
                wasCancelledInTransaction = isInTransaction
                PaymentStatus.CANCELLED
            }

            // Act
            propertyReactivationService.reactivatePropertyWithoutPayment(propertyOwnershipId, PropertyReactivationDataModel())

            // Assert
            assertEquals(false, wasCancelledInTransaction)
        }

        @Test
        fun `still completes when cancelling a created payment for the property fails`() {
            // Arrange
            stubFreeReactivation()
            whenever(mockPaymentReconciliationService.cancelCreatedPaymentForProperty(propertyOwnershipId))
                .thenThrow(RuntimeException("Database unavailable"))

            // Act & Assert
            assertDoesNotThrow {
                propertyReactivationService.reactivatePropertyWithoutPayment(propertyOwnershipId, PropertyReactivationDataModel())
            }
            verify(mockSession).setAttribute(REACTIVATED_PROPERTY_OWNERSHIP_ID, propertyOwnershipId)
        }
    }

    @Nested
    inner class ReactivatePropertyForPayment {
        @Test
        fun `claims the property, saves the details, then reactivates it until the end of the paid period`() {
            // Arrange
            whenever(mockUserToLandlordService.getCurrentLandlordForUser()).thenReturn(landlord)
            stubClaimSucceeds()
            whenever(mockPaymentService.calculatePropertyReactivationPaymentQuote(eq(propertyOwnership), eq(landlord), any()))
                .thenReturn(paidQuote)
            val licensing = LicensingDetailsDataModel(LicensingType.NO_LICENSING, null)

            // Act
            propertyReactivationService.reactivatePropertyForPayment(
                paymentForProperty(),
                PropertyReactivationDataModel(licensing = licensing),
            )

            // Assert
            inOrder(mockAddressAvailabilityService, mockActivationService, mockPaymentService, mockPropertyOwnershipService) {
                verify(mockAddressAvailabilityService).hasAnotherActiveRegistration(uprn, propertyOwnershipId)
                verify(mockActivationService).claimPropertyForReactivation(propertyOwnershipId)
                verify(mockPaymentService).calculatePropertyReactivationPaymentQuote(eq(propertyOwnership), eq(landlord), any())
                verify(mockPropertyOwnershipService).saveReactivationLicensing(propertyOwnership, licensing)
                verify(mockActivationService).reactivateClaimedProperty(propertyOwnership, QUOTED_RENEWAL_DATE, QUOTED_RENEWAL_DATE)
            }
            verifyNoInteractions(mockTransactionTemplate, mockPaymentReconciliationService)
        }

        @Test
        fun `records the paid reactivation in the session and sends the confirmation email`() {
            // Arrange
            whenever(mockUserToLandlordService.getCurrentLandlordForUser()).thenReturn(landlord)
            whenever(mockLandlordActiveStatusService.isCurrentLandlordInactive()).thenReturn(true)
            stubClaimSucceeds()
            whenever(mockPaymentService.calculatePropertyReactivationPaymentQuote(eq(propertyOwnership), eq(landlord), any()))
                .thenReturn(paidQuote)

            // Act
            propertyReactivationService.reactivatePropertyForPayment(paymentForProperty(), PropertyReactivationDataModel())

            // Assert
            verify(mockSession).setAttribute(REACTIVATED_PROPERTY_OWNERSHIP_ID, propertyOwnershipId)
            verify(mockSession).setAttribute(LANDLORD_REACTIVATED_BY_LAST_PROPERTY_REACTIVATION, true)
            verify(mockSession).setAttribute(LAST_PROPERTY_REACTIVATION_WAS_PAID, true)
            verify(mockPropertyReactivationEmailService).sendConfirmationEmail(propertyOwnershipId, landlordReactivated = true)
        }

        @Test
        fun `throws a 404 without claiming when the current landlord isn't a landlord of the paid-for property`() {
            // Arrange
            whenever(mockUserToLandlordService.getCurrentLandlordForUser()).thenReturn(MockLandlordData.createIndividualLandlord())

            // Act & Assert
            val exception =
                assertThrows<ResponseStatusException> {
                    propertyReactivationService.reactivatePropertyForPayment(paymentForProperty(), PropertyReactivationDataModel())
                }

            assertEquals(HttpStatus.NOT_FOUND, exception.statusCode)
            verifyNoInteractions(mockActivationService, mockPropertyOwnershipService, mockSession)
        }

        @Test
        fun `throws an EntityExistsException without claiming when another property has the address`() {
            // Arrange
            whenever(mockUserToLandlordService.getCurrentLandlordForUser()).thenReturn(landlord)
            whenever(mockAddressAvailabilityService.hasAnotherActiveRegistration(uprn, propertyOwnershipId)).thenReturn(true)

            // Act & Assert
            assertThrows<EntityExistsException> {
                propertyReactivationService.reactivatePropertyForPayment(paymentForProperty(), PropertyReactivationDataModel())
            }

            verifyNoInteractions(mockActivationService, mockPropertyOwnershipService, mockSession)
        }

        @Test
        fun `throws without saving when a joint landlord has already claimed the property`() {
            // Arrange
            whenever(mockUserToLandlordService.getCurrentLandlordForUser()).thenReturn(landlord)
            whenever(mockAddressAvailabilityService.hasAnotherActiveRegistration(uprn, propertyOwnershipId)).thenReturn(false)
            whenever(mockActivationService.claimPropertyForReactivation(propertyOwnershipId))
                .thenThrow(PropertyAlreadyReactivatedException(propertyOwnershipId))

            // Act & Assert
            assertThrows<PropertyAlreadyReactivatedException> {
                propertyReactivationService.reactivatePropertyForPayment(paymentForProperty(), PropertyReactivationDataModel())
            }

            verify(mockActivationService, never()).reactivateClaimedProperty(any(), any(), anyOrNull())
            verifyNoInteractions(mockPaymentService, mockPropertyOwnershipService, mockSession)
        }

        @Test
        fun `throws without saving when the payment wasn't quoted today`() {
            assertQuoteExpiredWithoutReactivating(paymentForProperty(quoteDate = TODAY.minusDays(1)))
        }

        @Test
        fun `throws without saving when today's quote is for a different amount`() {
            assertQuoteExpiredWithoutReactivating(paymentForProperty(amountInPence = PAID_AMOUNT + 1))
        }

        @Test
        fun `throws without saving when today's quote ends on a different date`() {
            assertQuoteExpiredWithoutReactivating(paymentForProperty(forPeriodEnding = QUOTED_RENEWAL_DATE.plusDays(1)))
        }

        private fun assertQuoteExpiredWithoutReactivating(payment: Payment) {
            // Arrange
            whenever(mockUserToLandlordService.getCurrentLandlordForUser()).thenReturn(landlord)
            stubClaimSucceeds()
            whenever(mockPaymentService.calculatePropertyReactivationPaymentQuote(eq(propertyOwnership), eq(landlord), any()))
                .thenReturn(paidQuote)

            // Act & Assert
            assertThrows<PaymentQuoteExpiredException> {
                propertyReactivationService.reactivatePropertyForPayment(payment, PropertyReactivationDataModel())
            }

            verify(mockActivationService, never()).reactivateClaimedProperty(any(), any(), anyOrNull())
            verifyNoInteractions(mockPropertyOwnershipService, mockSession, mockPropertyReactivationEmailService)
        }
    }

    @Test
    fun `wasLandlordReactivatedByLastReactivation returns true when the session flag is true`() {
        // Arrange
        whenever(mockSession.getAttribute(LANDLORD_REACTIVATED_BY_LAST_PROPERTY_REACTIVATION)).thenReturn(true)

        // Act, Assert
        assertTrue(propertyReactivationService.wasLandlordReactivatedByLastReactivation())
    }

    @Test
    fun `wasLandlordReactivatedByLastReactivation returns false when the session flag is missing`() {
        // Arrange
        whenever(mockSession.getAttribute(LANDLORD_REACTIVATED_BY_LAST_PROPERTY_REACTIVATION)).thenReturn(null)

        // Act, Assert
        assertFalse(propertyReactivationService.wasLandlordReactivatedByLastReactivation())
    }

    @Test
    fun `wasLastReactivationPaid returns true when the session flag is true`() {
        // Arrange
        whenever(mockSession.getAttribute(LAST_PROPERTY_REACTIVATION_WAS_PAID)).thenReturn(true)

        // Act, Assert
        assertTrue(propertyReactivationService.wasLastReactivationPaid())
    }

    @Test
    fun `wasLastReactivationPaid returns false when the session flag is missing`() {
        // Arrange
        whenever(mockSession.getAttribute(LAST_PROPERTY_REACTIVATION_WAS_PAID)).thenReturn(null)

        // Act, Assert
        assertFalse(propertyReactivationService.wasLastReactivationPaid())
    }

    companion object {
        private val TODAY = LocalDate.now(DateTimeHelper.UK_ZONE)
        private val QUOTED_RENEWAL_DATE = LocalDate.of(2028, 6, 1)
        private const val PAID_AMOUNT = 1234
    }
}
