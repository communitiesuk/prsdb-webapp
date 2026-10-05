package uk.gov.communities.prsdb.webapp.integration

import jakarta.persistence.EntityExistsException
import jakarta.persistence.EntityManagerFactory
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.fail
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.mockito.Mockito.verifyNoInteractions
import org.mockito.kotlin.any
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.never
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.orm.jpa.EntityManagerHolder
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.support.TransactionSynchronizationManager
import org.springframework.transaction.support.TransactionTemplate
import uk.gov.communities.prsdb.webapp.clients.GovUkPayClient
import uk.gov.communities.prsdb.webapp.constants.enums.PaymentStatus
import uk.gov.communities.prsdb.webapp.database.entity.PropertyOwnership
import uk.gov.communities.prsdb.webapp.database.repository.LandlordRepository
import uk.gov.communities.prsdb.webapp.database.repository.PaymentRepository
import uk.gov.communities.prsdb.webapp.database.repository.PropertyOwnershipRepository
import uk.gov.communities.prsdb.webapp.exceptions.GovUkPayException
import uk.gov.communities.prsdb.webapp.models.dataModels.govUkPay.GovUkPayPaymentStatus
import uk.gov.communities.prsdb.webapp.services.PaymentService
import uk.gov.communities.prsdb.webapp.testHelpers.JourneyTestHelper
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockGovUkPayData.Companion.createGovUkPayPayment
import java.time.Instant
import java.time.MonthDay
import kotlin.test.assertContains

class PaymentServicePersistenceTests : IntegrationTestWithMutableData("data-local.sql") {
    @Autowired
    private lateinit var paymentRepository: PaymentRepository

    @Autowired
    private lateinit var landlordRepository: LandlordRepository

    @Autowired
    private lateinit var propertyOwnershipRepository: PropertyOwnershipRepository

    @Autowired
    private lateinit var transactionManager: PlatformTransactionManager

    @Autowired
    private lateinit var entityManagerFactory: EntityManagerFactory

    @Autowired
    private lateinit var paymentService: PaymentService

    @MockitoBean
    private lateinit var mockGovUkPayClient: GovUkPayClient

    @AfterEach
    fun clearSecurityContext() {
        SecurityContextHolder.clearContext()
    }

    @Test
    fun `updateStatusIfCurrentStatusIn updates the status and last modified date when the payment has an expected status`() {
        // Act
        val rowsUpdated =
            paymentRepository.updateStatusIfCurrentStatusIn(
                CREATED_PAYMENT_ID,
                listOf(PaymentStatus.CREATED),
                PaymentStatus.CAPTURABLE,
                NOW,
            )

        // Assert
        assertEquals(1, rowsUpdated)
        val payment = paymentRepository.findById(CREATED_PAYMENT_ID).get()
        assertEquals(PaymentStatus.CAPTURABLE, payment.status)
        assertEquals(NOW, payment.lastModifiedDate)
    }

    @ParameterizedTest
    @ValueSource(strings = [SUCCEEDED_PAYMENT_ID, FAILED_PAYMENT_ID, CANCELLED_PAYMENT_ID])
    fun `updateStatusIfCurrentStatusIn does not overwrite a finished payment's status`(paymentId: String) {
        // Arrange
        val originalStatus = paymentRepository.findStatusByPaymentId(paymentId)

        // Act
        val rowsUpdated =
            paymentRepository.updateStatusIfCurrentStatusIn(
                paymentId,
                PaymentStatus.IN_PROGRESS_STATUSES,
                PaymentStatus.FAILED,
                NOW,
            )

        // Assert
        assertEquals(0, rowsUpdated)
        assertEquals(originalStatus, paymentRepository.findStatusByPaymentId(paymentId))
    }

    @Test
    fun `findStatusByPaymentId returns the payment's status, or null if the payment does not exist`() {
        // Act, Assert
        assertEquals(PaymentStatus.CAPTURABLE, paymentRepository.findStatusByPaymentId(CAPTURABLE_PAYMENT_ID))
        assertNull(paymentRepository.findStatusByPaymentId("unknown-payment"))
    }

    @Test
    fun `finalisePayment links the payment to the registered property and marks it succeeded`() {
        // Act
        val status = paymentService.finalisePayment(CREATED_PAYMENT_ID, PaymentStatus.CAPTURABLE, ::getProperty)

        // Assert
        assertEquals(PaymentStatus.SUCCEEDED, status)
        verify(mockGovUkPayClient).capturePayment(CREATED_PAYMENT_ID)
        assertPaymentSucceededForProperty(CREATED_PAYMENT_ID)
    }

    @Test
    fun `finalisePayment rolls back the property registration and cancels the payment when capture fails`() {
        // Arrange
        doThrow(GovUkPayException("Capture failed")).whenever(mockGovUkPayClient).capturePayment(CREATED_PAYMENT_ID)

        // Act
        val status =
            paymentService.finalisePayment(CREATED_PAYMENT_ID, PaymentStatus.CAPTURABLE) {
                setLandlordAnniversary()
                getProperty()
            }

        // Assert
        assertEquals(PaymentStatus.CANCELLED, status)
        verify(mockGovUkPayClient).cancelPayment(CREATED_PAYMENT_ID)
        assertNull(landlordRepository.findById(LANDLORD_ID).get().anniversary)
        val payment = paymentRepository.findById(CREATED_PAYMENT_ID).get()
        assertEquals(PaymentStatus.CANCELLED, payment.status)
        assertNull(payment.associatedProperty)
        assertNotNull(payment.associatedIncompleteProperty)
    }

    @Test
    fun `finalisePayment cancels the payment without capturing it when registration leaves the transaction marked for rollback`() {
        // Act
        val status =
            paymentService.finalisePayment(CREATED_PAYMENT_ID, PaymentStatus.CAPTURABLE) {
                setLandlordAnniversary()
                // Simulates a failed transactional call whose exception was caught during registration
                TransactionTemplate(transactionManager).executeWithoutResult { it.setRollbackOnly() }
                getProperty()
            }

        // Assert
        assertEquals(PaymentStatus.CANCELLED, status)
        verify(mockGovUkPayClient, never()).capturePayment(any())
        verify(mockGovUkPayClient).cancelPayment(CREATED_PAYMENT_ID)
        assertNull(landlordRepository.findById(LANDLORD_ID).get().anniversary)
        assertEquals(PaymentStatus.CANCELLED, paymentRepository.findStatusByPaymentId(CREATED_PAYMENT_ID))
    }

    @Test
    fun `finalisePayment commits the cancellation independently of the caller's transaction when registration fails`() {
        // Act
        val status =
            TransactionTemplate(transactionManager).execute {
                paymentService.finalisePayment(CREATED_PAYMENT_ID, PaymentStatus.CAPTURABLE) {
                    throw EntityExistsException("Address already registered")
                }
            }

        // Assert
        assertEquals(PaymentStatus.CANCELLED, status)
        verify(mockGovUkPayClient).cancelPayment(CREATED_PAYMENT_ID)
        assertEquals(PaymentStatus.CANCELLED, paymentRepository.findStatusByPaymentId(CREATED_PAYMENT_ID))
    }

    @Test
    fun `finalising a payment again returns its status without registering the property or capturing it again`() {
        // Arrange
        var registrationCount = 0
        val registerProperty = {
            registrationCount++
            getProperty()
        }
        paymentService.finalisePayment(CREATED_PAYMENT_ID, PaymentStatus.CAPTURABLE, registerProperty)

        // Act
        val status = paymentService.finalisePayment(CREATED_PAYMENT_ID, PaymentStatus.CAPTURABLE, registerProperty)

        // Assert
        assertEquals(PaymentStatus.SUCCEEDED, status)
        assertEquals(1, registrationCount)
        verify(mockGovUkPayClient, times(1)).capturePayment(CREATED_PAYMENT_ID)
    }

    @Test
    fun `finalisePayment does not register the property for a payment that is already being finalised`() {
        // Act
        val status =
            paymentService.finalisePayment(CAPTURABLE_PAYMENT_ID, PaymentStatus.CAPTURABLE) {
                fail("Property should not be registered")
            }

        // Assert
        assertEquals(PaymentStatus.CAPTURABLE, status)
        verifyNoInteractions(mockGovUkPayClient)
    }

    @Test
    fun `finalisePayment succeeds when the payment is already loaded in an open-in-view persistence context`() {
        // Arrange
        val entityManager = entityManagerFactory.createEntityManager()
        TransactionSynchronizationManager.bindResource(entityManagerFactory, EntityManagerHolder(entityManager))

        try {
            paymentRepository.findById(CREATED_PAYMENT_ID).get()

            // Act
            val status = paymentService.finalisePayment(CREATED_PAYMENT_ID, PaymentStatus.CAPTURABLE, ::getProperty)

            // Assert
            assertEquals(PaymentStatus.SUCCEEDED, status)
        } finally {
            TransactionSynchronizationManager.unbindResource(entityManagerFactory)
            entityManager.close()
        }
        assertPaymentSucceededForProperty(CREATED_PAYMENT_ID)
    }

    @Test
    fun `createPropertyRegistrationPayment does not overwrite a payment that is finalised while it is being reconciled`() {
        // Arrange
        JourneyTestHelper.setMockUser(USER_ID)
        whenever(mockGovUkPayClient.getPayment(CAPTURABLE_PAYMENT_ID))
            .thenReturn(createGovUkPayPayment(CAPTURABLE_PAYMENT_ID, GovUkPayPaymentStatus.CANCELLED))
        whenever(mockGovUkPayClient.getPayment(CREATED_PAYMENT_ID)).thenAnswer {
            // Finalised after the existing payments are loaded, but before this payment's outcome is recorded
            paymentService.finalisePayment(CREATED_PAYMENT_ID, PaymentStatus.CAPTURABLE, ::getProperty)
            createGovUkPayPayment(CREATED_PAYMENT_ID, GovUkPayPaymentStatus.SUCCESS)
        }

        // Act
        val exception =
            assertThrows<IllegalStateException> {
                paymentService.createPropertyRegistrationPayment(JOURNEY_ID, "https://example.test/return", "user@example.test")
            }

        // Assert
        assertContains(exception.message!!, CREATED_PAYMENT_ID)
        assertPaymentSucceededForProperty(CREATED_PAYMENT_ID)
        assertEquals(PaymentStatus.CANCELLED, paymentRepository.findStatusByPaymentId(CAPTURABLE_PAYMENT_ID))
        verify(mockGovUkPayClient, never()).createPayment(any())
    }

    private fun getProperty(): PropertyOwnership = propertyOwnershipRepository.findById(PROPERTY_OWNERSHIP_ID).get()

    private fun setLandlordAnniversary() {
        val landlord = landlordRepository.findById(LANDLORD_ID).get()
        landlord.setAnniversaryIfAbsent(MonthDay.of(1, 1))
        landlordRepository.saveAndFlush(landlord)
    }

    private fun assertPaymentSucceededForProperty(paymentId: String) {
        val payment = paymentRepository.findById(paymentId).get()
        assertEquals(PaymentStatus.SUCCEEDED, payment.status)
        assertEquals(PROPERTY_OWNERSHIP_ID, payment.associatedProperty?.id)
        assertNull(payment.associatedIncompleteProperty)
        assertEquals(getProperty().renewalDate, payment.forPeriodEnding)
    }

    companion object {
        private const val CREATED_PAYMENT_ID = "created-payment"
        private const val CAPTURABLE_PAYMENT_ID = "capturable-payment"
        private const val SUCCEEDED_PAYMENT_ID = "succeeded-payment"
        private const val FAILED_PAYMENT_ID = "failed-payment"
        private const val CANCELLED_PAYMENT_ID = "cancelled-payment"
        private const val LANDLORD_ID = 1L
        private const val PROPERTY_OWNERSHIP_ID = 1L
        private const val USER_ID = "urn:fdc:gov.uk:2022:UVWXY"
        private const val JOURNEY_ID = "example-incomplete-journey-with-payments"
        private val NOW = Instant.parse("2026-01-01T12:00:00Z")
    }
}
