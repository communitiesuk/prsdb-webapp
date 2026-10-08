package uk.gov.communities.prsdb.webapp.integration

import jakarta.persistence.EntityExistsException
import jakarta.persistence.EntityManagerFactory
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
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
import uk.gov.communities.prsdb.webapp.database.entity.LandlordIncompletePropertyId
import uk.gov.communities.prsdb.webapp.database.entity.PropertyOwnership
import uk.gov.communities.prsdb.webapp.database.repository.LandlordIncompletePropertiesRepository
import uk.gov.communities.prsdb.webapp.database.repository.LandlordRepository
import uk.gov.communities.prsdb.webapp.database.repository.PaymentRepository
import uk.gov.communities.prsdb.webapp.database.repository.PropertyOwnershipRepository
import uk.gov.communities.prsdb.webapp.database.repository.SavedJourneyStateRepository
import uk.gov.communities.prsdb.webapp.exceptions.GovUkPayException
import uk.gov.communities.prsdb.webapp.models.dataModels.govUkPay.GovUkPayPaymentStatus
import uk.gov.communities.prsdb.webapp.services.PaymentService
import uk.gov.communities.prsdb.webapp.services.PropertyRegistrationService
import uk.gov.communities.prsdb.webapp.testHelpers.JourneyTestHelper
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockGovUkPayData.Companion.createGovUkPayPayment
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockPaymentData.Companion.createPayment
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockPropertyRegistrationData
import java.time.Instant
import java.time.MonthDay
import java.time.temporal.ChronoUnit
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException
import kotlin.test.assertContains

class PaymentServicePersistenceTests : IntegrationTestWithMutableData("data-local.sql") {
    @Autowired
    private lateinit var paymentRepository: PaymentRepository

    @Autowired
    private lateinit var landlordRepository: LandlordRepository

    @Autowired
    private lateinit var propertyOwnershipRepository: PropertyOwnershipRepository

    @Autowired
    private lateinit var landlordIncompletePropertiesRepository: LandlordIncompletePropertiesRepository

    @Autowired
    private lateinit var savedJourneyStateRepository: SavedJourneyStateRepository

    @Autowired
    private lateinit var transactionManager: PlatformTransactionManager

    @Autowired
    private lateinit var entityManagerFactory: EntityManagerFactory

    @Autowired
    private lateinit var paymentService: PaymentService

    @MockitoBean
    private lateinit var mockGovUkPayClient: GovUkPayClient

    @MockitoBean
    private lateinit var mockPropertyRegistrationService: PropertyRegistrationService

    private val registrationData = MockPropertyRegistrationData.createPropertyRegistrationDataModel()

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
        // Arrange
        whenever(mockPropertyRegistrationService.registerProperty(registrationData)).thenAnswer { getProperty() }

        // Act
        val status = paymentService.finalisePayment(CREATED_PAYMENT_ID, PaymentStatus.CAPTURABLE, registrationData, deleteJourney = {})

        // Assert
        assertEquals(PaymentStatus.SUCCEEDED, status)
        verify(mockGovUkPayClient).capturePayment(CREATED_PAYMENT_ID)
        assertPaymentSucceededForProperty()
    }

    @Test
    fun `finalisePayment rolls back the property registration and cancels the payment when capture fails`() {
        // Arrange
        doThrow(GovUkPayException("Capture failed")).whenever(mockGovUkPayClient).capturePayment(CREATED_PAYMENT_ID)
        whenever(mockPropertyRegistrationService.registerProperty(registrationData)).thenAnswer {
            setLandlordAnniversary()
            getProperty()
        }

        // Act
        val status = paymentService.finalisePayment(CREATED_PAYMENT_ID, PaymentStatus.CAPTURABLE, registrationData, deleteJourney = {})

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
        // Arrange
        whenever(mockPropertyRegistrationService.registerProperty(registrationData)).thenAnswer {
            setLandlordAnniversary()
            // Simulates a failed transactional call whose exception was caught during registration
            TransactionTemplate(transactionManager).executeWithoutResult { it.setRollbackOnly() }
            getProperty()
        }

        // Act
        val status = paymentService.finalisePayment(CREATED_PAYMENT_ID, PaymentStatus.CAPTURABLE, registrationData, deleteJourney = {})

        // Assert
        assertEquals(PaymentStatus.CANCELLED, status)
        verify(mockGovUkPayClient, never()).capturePayment(any())
        verify(mockGovUkPayClient).cancelPayment(CREATED_PAYMENT_ID)
        assertNull(landlordRepository.findById(LANDLORD_ID).get().anniversary)
        assertEquals(PaymentStatus.CANCELLED, paymentRepository.findStatusByPaymentId(CREATED_PAYMENT_ID))
    }

    @Test
    fun `finalisePayment commits the cancellation independently of the caller's transaction when registration fails`() {
        // Arrange
        whenever(mockPropertyRegistrationService.registerProperty(registrationData))
            .thenThrow(EntityExistsException("Address already registered"))

        // Act
        val status =
            TransactionTemplate(transactionManager).execute {
                paymentService.finalisePayment(CREATED_PAYMENT_ID, PaymentStatus.CAPTURABLE, registrationData, deleteJourney = {})
            }

        // Assert
        assertEquals(PaymentStatus.CANCELLED, status)
        verify(mockGovUkPayClient).cancelPayment(CREATED_PAYMENT_ID)
        assertEquals(PaymentStatus.CANCELLED, paymentRepository.findStatusByPaymentId(CREATED_PAYMENT_ID))
    }

    @Test
    fun `finalising a payment again returns its status without registering the property or capturing it again`() {
        // Arrange
        whenever(mockPropertyRegistrationService.registerProperty(registrationData)).thenAnswer { getProperty() }
        paymentService.finalisePayment(CREATED_PAYMENT_ID, PaymentStatus.CAPTURABLE, registrationData, deleteJourney = {})

        // Act
        val status = paymentService.finalisePayment(CREATED_PAYMENT_ID, PaymentStatus.CAPTURABLE, registrationData, deleteJourney = {})

        // Assert
        assertEquals(PaymentStatus.SUCCEEDED, status)
        verify(mockPropertyRegistrationService, times(1)).registerProperty(registrationData)
        verify(mockGovUkPayClient, times(1)).capturePayment(CREATED_PAYMENT_ID)
    }

    @Test
    fun `finalisePayment succeeds when the payment is already loaded in an open-in-view persistence context`() {
        // Arrange
        whenever(mockPropertyRegistrationService.registerProperty(registrationData)).thenAnswer { getProperty() }
        val entityManager = entityManagerFactory.createEntityManager()
        TransactionSynchronizationManager.bindResource(entityManagerFactory, EntityManagerHolder(entityManager))

        try {
            paymentRepository.findById(CREATED_PAYMENT_ID).get()

            // Act
            val status = paymentService.finalisePayment(CREATED_PAYMENT_ID, PaymentStatus.CAPTURABLE, registrationData, deleteJourney = {})

            // Assert
            assertEquals(PaymentStatus.SUCCEEDED, status)
        } finally {
            TransactionSynchronizationManager.unbindResource(entityManagerFactory)
            entityManager.close()
        }
        assertPaymentSucceededForProperty()
    }

    @Test
    fun `createPropertyRegistrationPayment does not overwrite a payment that is finalised while it is being reconciled`() {
        // Arrange
        JourneyTestHelper.setMockUser(USER_ID)
        whenever(mockPropertyRegistrationService.registerProperty(registrationData)).thenAnswer { getProperty() }
        whenever(mockGovUkPayClient.getPayment(CAPTURABLE_PAYMENT_ID))
            .thenReturn(createGovUkPayPayment(CAPTURABLE_PAYMENT_ID, GovUkPayPaymentStatus.CANCELLED))
        whenever(mockGovUkPayClient.getPayment(CREATED_PAYMENT_ID)).thenAnswer {
            // Finalised after the existing payments are loaded, but before this payment's outcome is recorded
            paymentService.finalisePayment(CREATED_PAYMENT_ID, PaymentStatus.CAPTURABLE, registrationData, deleteJourney = {})
            createGovUkPayPayment(CREATED_PAYMENT_ID, GovUkPayPaymentStatus.SUCCESS)
        }

        // Act
        val exception =
            assertThrows<IllegalStateException> {
                paymentService.createPropertyRegistrationPayment(JOURNEY_ID, "https://example.test/return", "user@example.test")
            }

        // Assert
        assertContains(exception.message!!, CREATED_PAYMENT_ID)
        assertPaymentSucceededForProperty()
        assertEquals(PaymentStatus.CANCELLED, paymentRepository.findStatusByPaymentId(CAPTURABLE_PAYMENT_ID))
        verify(mockGovUkPayClient, never()).createPayment(any())
    }

    @Test
    fun `finalisePayment cancels the property's other in-progress payments and leaves its finished payments alone`() {
        // Arrange
        whenever(mockPropertyRegistrationService.registerProperty(registrationData)).thenAnswer { getProperty() }

        // Act
        val status = paymentService.finalisePayment(CREATED_PAYMENT_ID, PaymentStatus.CAPTURABLE, registrationData, deleteJourney = {})

        // Assert
        assertEquals(PaymentStatus.SUCCEEDED, status)
        val otherPayment = paymentRepository.findById(CAPTURABLE_PAYMENT_ID).get()
        assertEquals(PaymentStatus.CANCELLED, otherPayment.status)
        assertNotNull(otherPayment.lastModifiedDate)
        assertEquals(PaymentStatus.FAILED, paymentRepository.findStatusByPaymentId(FAILED_PAYMENT_ID))
        assertEquals(PaymentStatus.CANCELLED, paymentRepository.findStatusByPaymentId(CANCELLED_PAYMENT_ID))
        verify(mockGovUkPayClient).cancelPayment(CAPTURABLE_PAYMENT_ID)
        verify(mockGovUkPayClient, never()).cancelPayment(FAILED_PAYMENT_ID)
        verify(mockGovUkPayClient, never()).cancelPayment(CANCELLED_PAYMENT_ID)
    }

    @Test
    fun `finalisePayment cancels the payment without registering the property when another payment for the property has succeeded`() {
        // Arrange
        paymentRepository.updateStatusIfCurrentStatusIn(
            CAPTURABLE_PAYMENT_ID,
            listOf(PaymentStatus.CAPTURABLE),
            PaymentStatus.SUCCEEDED,
            NOW,
        )

        // Act
        val status = paymentService.finalisePayment(CREATED_PAYMENT_ID, PaymentStatus.CAPTURABLE, registrationData, deleteJourney = {})

        // Assert
        assertEquals(PaymentStatus.CANCELLED, status)
        verify(mockPropertyRegistrationService, never()).registerProperty(any())
        verify(mockGovUkPayClient, never()).capturePayment(any())
        verify(mockGovUkPayClient).cancelPayment(CREATED_PAYMENT_ID)
        val payment = paymentRepository.findById(CREATED_PAYMENT_ID).get()
        assertEquals(PaymentStatus.CANCELLED, payment.status)
        assertNull(payment.associatedProperty)
        assertEquals(PaymentStatus.SUCCEEDED, paymentRepository.findStatusByPaymentId(CAPTURABLE_PAYMENT_ID))
    }

    @Test
    fun `finalisePayment holds a lock on the incomplete property until the property is registered`() {
        // Arrange
        val executor = Executors.newFixedThreadPool(2)
        val registrationStarted = CountDownLatch(1)
        val finishRegistration = CountDownLatch(1)
        whenever(mockPropertyRegistrationService.registerProperty(registrationData)).thenAnswer {
            registrationStarted.countDown()
            finishRegistration.await(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            getProperty()
        }

        try {
            val finalisation =
                executor.submit<PaymentStatus> {
                    paymentService.finalisePayment(CREATED_PAYMENT_ID, PaymentStatus.CAPTURABLE, registrationData, deleteJourney = {})
                }
            assertTrue(registrationStarted.await(TIMEOUT_SECONDS, TimeUnit.SECONDS), "Registration did not start")

            // Act
            val lockWaiter =
                executor.submit {
                    TransactionTemplate(transactionManager).executeWithoutResult {
                        landlordIncompletePropertiesRepository.findByIdForUpdate(INCOMPLETE_PROPERTY_ID)
                    }
                }

            // Assert
            assertThrows<TimeoutException>("The incomplete property could be locked while it was being registered") {
                lockWaiter.get(500, TimeUnit.MILLISECONDS)
            }
            finishRegistration.countDown()
            assertEquals(PaymentStatus.SUCCEEDED, finalisation.get(TIMEOUT_SECONDS, TimeUnit.SECONDS))
            lockWaiter.get(TIMEOUT_SECONDS, TimeUnit.SECONDS)
        } finally {
            finishRegistration.countDown()
            executor.shutdown()
            executor.awaitTermination(TIMEOUT_SECONDS, TimeUnit.SECONDS)
        }
    }

    @Test
    fun `finalisePayment deletes the journey without deleting the finalised payment`() {
        // Arrange
        whenever(mockPropertyRegistrationService.registerProperty(registrationData)).thenAnswer { getProperty() }

        // Act
        val status = paymentService.finalisePayment(CREATED_PAYMENT_ID, PaymentStatus.CAPTURABLE, registrationData, ::deleteJourney)

        // Assert
        assertEquals(PaymentStatus.SUCCEEDED, status)
        assertNull(savedJourneyStateRepository.findByJourneyIdAndUser_Id(JOURNEY_ID, USER_ID))
        assertFalse(landlordIncompletePropertiesRepository.existsById(INCOMPLETE_PROPERTY_ID))
        assertFalse(paymentRepository.existsById(CAPTURABLE_PAYMENT_ID))
        assertFalse(paymentRepository.existsById(FAILED_PAYMENT_ID))
        assertPaymentSucceededForProperty()
        verify(mockGovUkPayClient).capturePayment(CREATED_PAYMENT_ID)
        verify(mockGovUkPayClient).cancelPayment(CAPTURABLE_PAYMENT_ID)
    }

    @Test
    fun `finalisePayment keeps the journey and the property's other payments when the payment cannot be captured`() {
        // Arrange
        whenever(mockPropertyRegistrationService.registerProperty(registrationData)).thenAnswer { getProperty() }
        doThrow(GovUkPayException("Capture failed")).whenever(mockGovUkPayClient).capturePayment(CREATED_PAYMENT_ID)

        // Act
        val status = paymentService.finalisePayment(CREATED_PAYMENT_ID, PaymentStatus.CAPTURABLE, registrationData, ::deleteJourney)

        // Assert
        assertEquals(PaymentStatus.CANCELLED, status)
        assertNotNull(savedJourneyStateRepository.findByJourneyIdAndUser_Id(JOURNEY_ID, USER_ID))
        val payment = paymentRepository.findById(CREATED_PAYMENT_ID).get()
        assertEquals(PaymentStatus.CANCELLED, payment.status)
        assertEquals(INCOMPLETE_PROPERTY_ID, payment.associatedIncompleteProperty?.id)
        assertEquals(PaymentStatus.CAPTURABLE, paymentRepository.findStatusByPaymentId(CAPTURABLE_PAYMENT_ID))
        verify(mockGovUkPayClient, never()).cancelPayment(CAPTURABLE_PAYMENT_ID)
    }

    @Test
    fun `finalisePayment leaves a payment it could neither finalise nor cancel recorded as capturable`() {
        // Arrange
        whenever(mockPropertyRegistrationService.registerProperty(registrationData)).thenAnswer { getProperty() }
        doThrow(GovUkPayException("Capture failed")).whenever(mockGovUkPayClient).capturePayment(CREATED_PAYMENT_ID)
        doThrow(GovUkPayException("Cancel failed")).whenever(mockGovUkPayClient).cancelPayment(CREATED_PAYMENT_ID)
        whenever(mockGovUkPayClient.getPayment(CREATED_PAYMENT_ID)).thenThrow(GovUkPayException("GOV.UK Pay unavailable"))

        // Act
        assertThrows<GovUkPayException> {
            paymentService.finalisePayment(CREATED_PAYMENT_ID, PaymentStatus.CAPTURABLE, registrationData, ::deleteJourney)
        }

        // Assert
        assertEquals(PaymentStatus.CAPTURABLE, paymentRepository.findStatusByPaymentId(CREATED_PAYMENT_ID))
    }

    @Test
    fun `finalisePayment can be retried after a failure that could not be cancelled on GOV UK Pay`() {
        // Arrange
        whenever(mockPropertyRegistrationService.registerProperty(registrationData)).thenAnswer { getProperty() }
        doThrow(GovUkPayException("Capture failed")).doNothing().whenever(mockGovUkPayClient).capturePayment(CREATED_PAYMENT_ID)
        doThrow(GovUkPayException("Cancel failed")).whenever(mockGovUkPayClient).cancelPayment(CREATED_PAYMENT_ID)
        whenever(mockGovUkPayClient.getPayment(CREATED_PAYMENT_ID)).thenThrow(GovUkPayException("GOV.UK Pay unavailable"))
        assertThrows<GovUkPayException> {
            paymentService.finalisePayment(CREATED_PAYMENT_ID, PaymentStatus.CAPTURABLE, registrationData, ::deleteJourney)
        }

        // Act
        val status = paymentService.finalisePayment(CREATED_PAYMENT_ID, PaymentStatus.CAPTURABLE, registrationData, ::deleteJourney)

        // Assert
        assertEquals(PaymentStatus.SUCCEEDED, status)
        verify(mockGovUkPayClient, times(2)).capturePayment(CREATED_PAYMENT_ID)
        assertPaymentSucceededForProperty()
    }

    @Test
    fun `finalisePayment cancels a payment that has been deleted along with its journey`() {
        // Arrange
        TransactionTemplate(transactionManager).executeWithoutResult { deleteJourney() }

        // Act
        val status = paymentService.finalisePayment(CAPTURABLE_PAYMENT_ID, PaymentStatus.CAPTURABLE, registrationData, ::deleteJourney)

        // Assert
        assertEquals(PaymentStatus.CANCELLED, status)
        verify(mockGovUkPayClient).cancelPayment(CAPTURABLE_PAYMENT_ID)
        verify(mockGovUkPayClient, never()).capturePayment(any())
    }

    @Test
    fun `getPropertyRegistrationPaymentStatus returns the GOV UK Pay status of the journey's latest payment`() {
        // Arrange
        JourneyTestHelper.setMockUser(USER_ID)
        val incompleteProperty = landlordIncompletePropertiesRepository.findById(INCOMPLETE_PROPERTY_ID).get()
        paymentRepository.save(
            createPayment(
                paymentId = LATEST_PAYMENT_ID,
                paymentCreatedAt = Instant.now().plus(1, ChronoUnit.DAYS),
                incompleteProperty = incompleteProperty,
            ),
        )
        whenever(mockGovUkPayClient.getPayment(LATEST_PAYMENT_ID))
            .thenReturn(createGovUkPayPayment(LATEST_PAYMENT_ID, GovUkPayPaymentStatus.CAPTURABLE))

        // Act
        val paymentStatusCheck = paymentService.getPropertyRegistrationPaymentStatus(JOURNEY_ID)

        // Assert
        assertEquals(LATEST_PAYMENT_ID, paymentStatusCheck.paymentId)
        assertEquals(PaymentStatus.CAPTURABLE, paymentStatusCheck.status)
    }

    private fun getProperty(): PropertyOwnership = propertyOwnershipRepository.findById(PROPERTY_OWNERSHIP_ID).get()

    private fun deleteJourney() = savedJourneyStateRepository.deleteByJourneyIdAndUser_Id(JOURNEY_ID, USER_ID)

    private fun setLandlordAnniversary() {
        val landlord = landlordRepository.findById(LANDLORD_ID).get()
        landlord.setAnniversaryIfAbsent(MonthDay.of(1, 1))
        landlordRepository.saveAndFlush(landlord)
    }

    private fun assertPaymentSucceededForProperty() {
        val payment = paymentRepository.findById(CREATED_PAYMENT_ID).get()
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
        private const val LATEST_PAYMENT_ID = "latest-payment"
        private const val LANDLORD_ID = 1L
        private const val PROPERTY_OWNERSHIP_ID = 1L
        private const val USER_ID = "urn:fdc:gov.uk:2022:UVWXY"
        private const val JOURNEY_ID = "example-incomplete-journey-with-payments"
        private val INCOMPLETE_PROPERTY_ID = LandlordIncompletePropertyId(USER_ID, 6L)
        private const val TIMEOUT_SECONDS = 10L
        private val NOW = Instant.parse("2026-01-01T12:00:00Z")
    }
}
