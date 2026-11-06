package uk.gov.communities.prsdb.webapp.integration

import jakarta.persistence.EntityExistsException
import jakarta.persistence.EntityManagerFactory
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.mockito.kotlin.any
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.eq
import org.mockito.kotlin.never
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.orm.jpa.EntityManagerHolder
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.support.TransactionSynchronization
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
import uk.gov.communities.prsdb.webapp.exceptions.PaymentQuoteExpiredException
import uk.gov.communities.prsdb.webapp.helpers.DateTimeHelper
import uk.gov.communities.prsdb.webapp.models.dataModels.AddressDataModel
import uk.gov.communities.prsdb.webapp.models.dataModels.PropertyRegistrationDataModel
import uk.gov.communities.prsdb.webapp.models.dataModels.PropertyRegistrationPaymentCreationResult
import uk.gov.communities.prsdb.webapp.models.dataModels.govUkPay.GovUkPayCreatePaymentRequest
import uk.gov.communities.prsdb.webapp.models.dataModels.govUkPay.GovUkPayPaymentStatus
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.PaymentRequiresManualInterventionEmail
import uk.gov.communities.prsdb.webapp.services.AddressService
import uk.gov.communities.prsdb.webapp.services.EmailNotificationService
import uk.gov.communities.prsdb.webapp.services.PaymentReconciliationService
import uk.gov.communities.prsdb.webapp.services.PaymentService
import uk.gov.communities.prsdb.webapp.services.PropertyOwnershipService
import uk.gov.communities.prsdb.webapp.services.PropertyRegistrationService
import uk.gov.communities.prsdb.webapp.testHelpers.JourneyTestHelper
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockGovUkPayData.Companion.createGovUkPayCreatedPayment
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockGovUkPayData.Companion.createGovUkPayNotCapturableException
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockGovUkPayData.Companion.createGovUkPayPayment
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockGovUkPayData.Companion.createGovUkPayUnavailableException
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockPaymentData.Companion.createPayment
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockPropertyRegistrationData
import java.time.Instant
import java.time.LocalDate
import java.time.MonthDay
import java.time.temporal.ChronoUnit
import java.util.concurrent.CountDownLatch
import java.util.concurrent.CyclicBarrier
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException
import java.util.concurrent.atomic.AtomicInteger

class PaymentServicePersistenceTests : IntegrationTestWithMutableData("data-local.sql") {
    @MockitoSpyBean
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

    @Autowired
    private lateinit var paymentReconciliationService: PaymentReconciliationService

    @Autowired
    private lateinit var propertyOwnershipService: PropertyOwnershipService

    @Autowired
    private lateinit var addressService: AddressService

    @Autowired
    private lateinit var jdbcTemplate: JdbcTemplate

    @MockitoBean
    private lateinit var mockGovUkPayClient: GovUkPayClient

    @MockitoBean
    private lateinit var mockPropertyRegistrationService: PropertyRegistrationService

    @MockitoBean
    private lateinit var mockManualInterventionEmailSender: EmailNotificationService<PaymentRequiresManualInterventionEmail>

    private val registrationData = MockPropertyRegistrationData.createPropertyRegistrationDataModel()

    private lateinit var originalPeriodEnd: LocalDate

    @BeforeEach
    fun alignFixtureDatesAndRecordOriginalPaymentPeriod() {
        // Payments can only be taken for a property registered on the UK day they were quoted
        jdbcTemplate.update(
            "UPDATE payment SET quote_date = ? WHERE associated_incomplete_property_user_id IS NOT NULL",
            LocalDate.now(DateTimeHelper.UK_ZONE),
        )
        jdbcTemplate.update("UPDATE property_ownership SET created_date = now() WHERE id = ?", PROPERTY_OWNERSHIP_ID)
        originalPeriodEnd = paymentRepository.findById(CREATED_PAYMENT_ID).get().forPeriodEnding
    }

    @AfterEach
    fun clearSecurityContext() {
        SecurityContextHolder.clearContext()
    }

    @Test
    fun `updateStatusIfCurrentStatusIs updates the status and last modified date when the payment has the expected status`() {
        // Act
        val rowsUpdated =
            paymentRepository.updateStatusIfCurrentStatusIs(
                CREATED_PAYMENT_ID,
                PaymentStatus.CREATED,
                PaymentStatus.CANCELLING,
                NOW,
            )

        // Assert
        assertEquals(1, rowsUpdated)
        val payment = paymentRepository.findById(CREATED_PAYMENT_ID).get()
        assertEquals(PaymentStatus.CANCELLING, payment.status)
        assertEquals(NOW, payment.lastModifiedDate)
    }

    @ParameterizedTest
    @ValueSource(strings = [SUCCEEDED_PAYMENT_ID, FAILED_PAYMENT_ID, CANCELLED_PAYMENT_ID])
    fun `updateStatusIfCurrentStatusIs does not overwrite a finished payment's status`(paymentId: String) {
        // Arrange
        val originalStatus = paymentRepository.findStatusByPaymentId(paymentId)

        // Act
        val rowsUpdated =
            paymentRepository.updateStatusIfCurrentStatusIs(
                paymentId,
                PaymentStatus.CREATED,
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
        assertEquals(PaymentStatus.CANCELLING, paymentRepository.findStatusByPaymentId(CANCELLING_PAYMENT_ID))
        assertNull(paymentRepository.findStatusByPaymentId("unknown-payment"))
    }

    @Test
    fun `deleting a journey keeps its payments with no link to the journey`() {
        // Arrange
        val paymentIds = listOf(CREATED_PAYMENT_ID, CANCELLING_PAYMENT_ID, FAILED_PAYMENT_ID, CANCELLED_PAYMENT_ID)
        val originalStatuses = paymentIds.associateWith { paymentRepository.findStatusByPaymentId(it) }

        // Act
        TransactionTemplate(transactionManager).executeWithoutResult { deleteJourney() }

        // Assert
        assertFalse(landlordIncompletePropertiesRepository.existsById(INCOMPLETE_PROPERTY_ID))
        paymentIds.forEach { paymentId ->
            val payment = paymentRepository.findById(paymentId).get()
            assertNull(payment.associatedIncompleteProperty)
            assertEquals(originalStatuses[paymentId], payment.status)
        }
    }

    @Test
    fun `finalisePayment links the payment to the registered property and marks it succeeded`() {
        // Arrange
        whenever(mockPropertyRegistrationService.registerProperty(any())).thenAnswer { getProperty() }

        // Act
        val status = paymentService.finalisePayment(CREATED_PAYMENT_ID, PaymentStatus.CAPTURABLE, registrationData)

        // Assert
        assertEquals(PaymentStatus.SUCCEEDED, status)
        verify(mockGovUkPayClient).capturePayment(CREATED_PAYMENT_ID)
        assertPaymentSucceededForProperty()
    }

    @Test
    fun `finalisePayment persists the original paid period even when registration happens after its renewal date`() {
        // Arrange
        val registrationData =
            MockPropertyRegistrationData.createPropertyRegistrationDataModel(
                addressModel = AddressDataModel.fromManualAddressData("99 Late Payment Road", "Leeds", "LS1 1AA"),
            )
        originalPeriodEnd = LocalDate.of(2025, 6, 12)
        val payment = paymentRepository.findById(CREATED_PAYMENT_ID).get()
        payment.forPeriodEnding = originalPeriodEnd
        paymentRepository.saveAndFlush(payment)
        whenever(mockPropertyRegistrationService.registerProperty(any())).thenAnswer {
            propertyOwnershipService.createPropertyOwnership(
                ownershipType = registrationData.ownershipType,
                isOccupied = registrationData.isOccupied,
                numberOfHouseholds = registrationData.numberOfHouseholds,
                numberOfPeople = registrationData.numberOfPeople,
                registeringLandlord = landlordRepository.findById(LANDLORD_ID).get(),
                anniversary = MonthDay.of(3, 15),
                propertyBuildType = registrationData.propertyType,
                address = addressService.findOrCreateAddress(registrationData.addressModel),
                numBedrooms = registrationData.numBedrooms,
                billsIncludedList = registrationData.billsIncludedList,
                customBillsIncluded = registrationData.customBillsIncluded,
                furnishedStatus = registrationData.furnishedStatus,
                rentFrequency = registrationData.rentFrequency,
                customRentFrequency = registrationData.customRentFrequency,
                rentAmount = registrationData.rentAmount,
                customPropertyType = registrationData.customPropertyType,
                renewalDate = checkNotNull(it.getArgument<PropertyRegistrationDataModel>(0).renewalDate),
                correspondenceEmail = registrationData.correspondenceEmail,
                correspondenceAddressModel = registrationData.correspondenceAddressModel,
            )
        }

        // Act
        val status = paymentService.finalisePayment(CREATED_PAYMENT_ID, PaymentStatus.CAPTURABLE, registrationData)

        // Assert
        assertEquals(PaymentStatus.SUCCEEDED, status)
        verify(mockPropertyRegistrationService).registerProperty(registrationData.copy(renewalDate = originalPeriodEnd))
        verify(mockGovUkPayClient).capturePayment(CREATED_PAYMENT_ID)
        val registeredProperty = paymentRepository.findById(CREATED_PAYMENT_ID).get().associatedProperty!!
        assertPaymentSucceededForProperty(registeredProperty.id)
        assertEquals(originalPeriodEnd, propertyOwnershipRepository.findById(registeredProperty.id).get().renewalDate)
    }

    @Test
    fun `finalisePayment registers the property when capture fails but GOV UK Pay reports the payment as taken`() {
        // Arrange
        whenever(mockPropertyRegistrationService.registerProperty(any())).thenAnswer { getProperty() }
        stubCaptureFailure(createGovUkPayUnavailableException(), GovUkPayPaymentStatus.SUCCESS)

        // Act
        val status = paymentService.finalisePayment(CREATED_PAYMENT_ID, PaymentStatus.CAPTURABLE, registrationData)

        // Assert
        assertEquals(PaymentStatus.SUCCEEDED, status)
        assertPaymentSucceededForProperty()
        verify(mockGovUkPayClient, never()).cancelPayment(any())
        verify(mockManualInterventionEmailSender, never()).sendEmail(any(), any())
    }

    @Test
    fun `finalisePayment rolls back the registration and records what GOV UK Pay reports when capture fails and nothing was taken`() {
        // Arrange
        stubCaptureFailure(createGovUkPayNotCapturableException(), GovUkPayPaymentStatus.CANCELLED)
        whenever(mockPropertyRegistrationService.registerProperty(any())).thenAnswer {
            setLandlordAnniversary()
            getProperty()
        }

        // Act
        val status = paymentService.finalisePayment(CREATED_PAYMENT_ID, PaymentStatus.CAPTURABLE, registrationData)

        // Assert
        assertEquals(PaymentStatus.CANCELLED, status)
        verify(mockGovUkPayClient, never()).cancelPayment(any())
        assertNull(landlordRepository.findById(LANDLORD_ID).get().anniversary)
        val payment = paymentRepository.findById(CREATED_PAYMENT_ID).get()
        assertEquals(PaymentStatus.CANCELLED, payment.status)
        assertNull(payment.associatedProperty)
        assertNotNull(payment.associatedIncompleteProperty)
    }

    @Test
    fun `finalisePayment rolls back the registration and records manual intervention when capture fails and GOV UK Pay's status can't be read`() {
        // Arrange
        doThrow(createGovUkPayUnavailableException()).whenever(mockGovUkPayClient).capturePayment(CREATED_PAYMENT_ID)
        whenever(mockGovUkPayClient.getPaymentWithRetry(CREATED_PAYMENT_ID)).thenThrow(createGovUkPayUnavailableException())
        whenever(mockPropertyRegistrationService.registerProperty(any())).thenAnswer {
            setLandlordAnniversary()
            getProperty()
        }

        // Act
        val status = paymentService.finalisePayment(CREATED_PAYMENT_ID, PaymentStatus.CAPTURABLE, registrationData)

        // Assert
        assertEquals(PaymentStatus.MANUAL_INTERVENTION, status)
        assertNull(landlordRepository.findById(LANDLORD_ID).get().anniversary)
        val payment = paymentRepository.findById(CREATED_PAYMENT_ID).get()
        assertEquals(PaymentStatus.MANUAL_INTERVENTION, payment.status)
        assertNull(payment.associatedProperty)
        assertNotNull(payment.associatedIncompleteProperty)
        verify(mockManualInterventionEmailSender).sendEmail(eq(MANUAL_INTERVENTION_EMAIL_ADDRESS), any())
        verify(mockGovUkPayClient, never()).cancelPayment(any())
    }

    @Test
    fun `finalisePayment rolls back the registration and cancels the payment when the property is registered after the quote date`() {
        // Arrange
        jdbcTemplate.update(
            "UPDATE payment SET quote_date = ? WHERE payment_id = ?",
            LocalDate.now(DateTimeHelper.UK_ZONE).minusDays(1),
            CREATED_PAYMENT_ID,
        )
        whenever(mockPropertyRegistrationService.registerProperty(any())).thenAnswer {
            setLandlordAnniversary()
            getProperty()
        }

        // Act, Assert
        assertThrows<PaymentQuoteExpiredException> {
            paymentService.finalisePayment(CREATED_PAYMENT_ID, PaymentStatus.CAPTURABLE, registrationData)
        }
        verify(mockGovUkPayClient, never()).capturePayment(any())
        verify(mockGovUkPayClient).cancelPayment(CREATED_PAYMENT_ID)
        assertNull(landlordRepository.findById(LANDLORD_ID).get().anniversary)
        assertNotNull(savedJourneyStateRepository.findByJourneyIdAndUser_Id(JOURNEY_ID, USER_ID))
        val payment = paymentRepository.findById(CREATED_PAYMENT_ID).get()
        assertEquals(PaymentStatus.CANCELLED, payment.status)
        assertNull(payment.associatedProperty)
        assertNotNull(payment.associatedIncompleteProperty)
    }

    @Test
    fun `finalisePayment cancels the payment without capturing it when registration leaves the transaction marked for rollback`() {
        // Arrange
        whenever(mockPropertyRegistrationService.registerProperty(any())).thenAnswer {
            setLandlordAnniversary()
            // Simulates a failed transactional call whose exception was caught during registration
            TransactionTemplate(transactionManager).executeWithoutResult { it.setRollbackOnly() }
            getProperty()
        }

        // Act
        val status = paymentService.finalisePayment(CREATED_PAYMENT_ID, PaymentStatus.CAPTURABLE, registrationData)

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
        whenever(mockPropertyRegistrationService.registerProperty(any()))
            .thenThrow(EntityExistsException("Address already registered"))

        // Act
        val status =
            TransactionTemplate(transactionManager).execute {
                paymentService.finalisePayment(CREATED_PAYMENT_ID, PaymentStatus.CAPTURABLE, registrationData)
            }

        // Assert
        assertEquals(PaymentStatus.CANCELLED, status)
        verify(mockGovUkPayClient).cancelPayment(CREATED_PAYMENT_ID)
        assertEquals(PaymentStatus.CANCELLED, paymentRepository.findStatusByPaymentId(CREATED_PAYMENT_ID))
    }

    @Test
    fun `finalising a payment again returns its status without registering the property or capturing it again`() {
        // Arrange
        whenever(mockPropertyRegistrationService.registerProperty(any())).thenAnswer { getProperty() }
        paymentService.finalisePayment(CREATED_PAYMENT_ID, PaymentStatus.CAPTURABLE, registrationData)

        // Act
        val status = paymentService.finalisePayment(CREATED_PAYMENT_ID, PaymentStatus.CAPTURABLE, registrationData)

        // Assert
        assertEquals(PaymentStatus.SUCCEEDED, status)
        verify(mockPropertyRegistrationService, times(1)).registerProperty(registrationData.copy(renewalDate = originalPeriodEnd))
        verify(mockGovUkPayClient, times(1)).capturePayment(CREATED_PAYMENT_ID)
    }

    @Test
    fun `finalisePayment succeeds when the payment is already loaded in an open-in-view persistence context`() {
        // Arrange
        whenever(mockPropertyRegistrationService.registerProperty(any())).thenAnswer { getProperty() }
        val entityManager = entityManagerFactory.createEntityManager()
        TransactionSynchronizationManager.bindResource(entityManagerFactory, EntityManagerHolder(entityManager))

        try {
            paymentRepository.findById(CREATED_PAYMENT_ID).get()

            // Act
            val status = paymentService.finalisePayment(CREATED_PAYMENT_ID, PaymentStatus.CAPTURABLE, registrationData)

            // Assert
            assertEquals(PaymentStatus.SUCCEEDED, status)
        } finally {
            TransactionSynchronizationManager.unbindResource(entityManagerFactory)
            entityManager.close()
        }
        assertPaymentSucceededForProperty()
    }

    @Test
    fun `finalising the same payment twice at once registers the property and captures the payment once`() {
        // Arrange
        val executor = Executors.newFixedThreadPool(2)
        val captureStarted = CountDownLatch(1)
        val finishCapture = CountDownLatch(1)
        whenever(mockPropertyRegistrationService.registerProperty(any())).thenAnswer { getProperty() }
        doAnswer {
            captureStarted.countDown()
            finishCapture.await(TIMEOUT_SECONDS, TimeUnit.SECONDS)
        }.whenever(mockGovUkPayClient).capturePayment(CREATED_PAYMENT_ID)

        try {
            val firstFinalisation =
                executor.submit<PaymentStatus> {
                    paymentService.finalisePayment(CREATED_PAYMENT_ID, PaymentStatus.CAPTURABLE, registrationData)
                }
            assertTrue(captureStarted.await(TIMEOUT_SECONDS, TimeUnit.SECONDS), "Capture did not start")

            // Act
            val secondFinalisation =
                executor.submit<PaymentStatus> {
                    paymentService.finalisePayment(CREATED_PAYMENT_ID, PaymentStatus.CAPTURABLE, registrationData)
                }
            assertThrows<TimeoutException>("The second finalisation did not wait for the first") {
                secondFinalisation.get(500, TimeUnit.MILLISECONDS)
            }
            finishCapture.countDown()

            // Assert
            assertEquals(PaymentStatus.SUCCEEDED, firstFinalisation.get(TIMEOUT_SECONDS, TimeUnit.SECONDS))
            assertEquals(PaymentStatus.SUCCEEDED, secondFinalisation.get(TIMEOUT_SECONDS, TimeUnit.SECONDS))
            verify(mockPropertyRegistrationService).registerProperty(registrationData.copy(renewalDate = originalPeriodEnd))
            verify(mockGovUkPayClient).capturePayment(CREATED_PAYMENT_ID)
            assertPaymentSucceededForProperty()
        } finally {
            finishCapture.countDown()
            executor.shutdown()
            executor.awaitTermination(TIMEOUT_SECONDS, TimeUnit.SECONDS)
        }
    }

    @Test
    fun `finalisePayment does not register the property or capture a payment marked for cancellation`() {
        // Act
        val status = paymentService.finalisePayment(CANCELLING_PAYMENT_ID, PaymentStatus.CAPTURABLE, registrationData)

        // Assert
        assertEquals(PaymentStatus.CANCELLING, status)
        assertEquals(PaymentStatus.CANCELLING, paymentRepository.findStatusByPaymentId(CANCELLING_PAYMENT_ID))
        verify(mockPropertyRegistrationService, never()).registerProperty(any())
        verify(mockGovUkPayClient, never()).capturePayment(any())
    }

    @Test
    fun `markForCancellation waits for a finalisation of the payment that commits, then leaves it succeeded`() {
        // Arrange
        val executor = Executors.newFixedThreadPool(2)
        val captureStarted = CountDownLatch(1)
        val finishCapture = CountDownLatch(1)
        whenever(mockPropertyRegistrationService.registerProperty(any())).thenAnswer { getProperty() }
        doAnswer {
            captureStarted.countDown()
            finishCapture.await(TIMEOUT_SECONDS, TimeUnit.SECONDS)
        }.whenever(mockGovUkPayClient).capturePayment(CREATED_PAYMENT_ID)

        try {
            val finalisation =
                executor.submit<PaymentStatus> {
                    paymentService.finalisePayment(CREATED_PAYMENT_ID, PaymentStatus.CAPTURABLE, registrationData)
                }
            assertTrue(captureStarted.await(TIMEOUT_SECONDS, TimeUnit.SECONDS), "Capture did not start")

            // Act
            val marking = executor.submit<PaymentStatus> { paymentReconciliationService.markForCancellation(CREATED_PAYMENT_ID) }
            assertThrows<TimeoutException>("Marking for cancellation did not wait for the finalisation") {
                marking.get(500, TimeUnit.MILLISECONDS)
            }
            finishCapture.countDown()

            // Assert
            assertEquals(PaymentStatus.SUCCEEDED, finalisation.get(TIMEOUT_SECONDS, TimeUnit.SECONDS))
            assertEquals(PaymentStatus.SUCCEEDED, marking.get(TIMEOUT_SECONDS, TimeUnit.SECONDS))
            verify(mockGovUkPayClient, never()).cancelPayment(any())
            assertPaymentSucceededForProperty()
        } finally {
            finishCapture.countDown()
            executor.shutdown()
            executor.awaitTermination(TIMEOUT_SECONDS, TimeUnit.SECONDS)
        }
    }

    @Test
    fun `markForCancellation waits for a finalisation of the payment that rolls back, then cancels it`() {
        // Arrange
        val executor = Executors.newFixedThreadPool(2)
        val registrationStarted = CountDownLatch(1)
        val finishRegistration = CountDownLatch(1)
        whenever(mockPropertyRegistrationService.registerProperty(any())).thenAnswer {
            registrationStarted.countDown()
            finishRegistration.await(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            throw EntityExistsException("Address already registered")
        }

        try {
            val finalisation =
                executor.submit<PaymentStatus> {
                    paymentService.finalisePayment(CREATED_PAYMENT_ID, PaymentStatus.CAPTURABLE, registrationData)
                }
            assertTrue(registrationStarted.await(TIMEOUT_SECONDS, TimeUnit.SECONDS), "Registration did not start")

            // Act
            val marking = executor.submit<PaymentStatus> { paymentReconciliationService.markForCancellation(CREATED_PAYMENT_ID) }
            assertThrows<TimeoutException>("Marking for cancellation did not wait for the finalisation") {
                marking.get(500, TimeUnit.MILLISECONDS)
            }
            finishRegistration.countDown()

            // Assert
            // Once the finalisation rolls back, either it or the marking can mark the payment, but only one of them cancels it
            val cancelStatuses = setOf(PaymentStatus.CANCELLING, PaymentStatus.CANCELLED)
            assertTrue(finalisation.get(TIMEOUT_SECONDS, TimeUnit.SECONDS) in cancelStatuses)
            assertTrue(marking.get(TIMEOUT_SECONDS, TimeUnit.SECONDS) in cancelStatuses)
            verify(mockGovUkPayClient).cancelPayment(CREATED_PAYMENT_ID)
            verify(mockGovUkPayClient, never()).capturePayment(any())
            val payment = paymentRepository.findById(CREATED_PAYMENT_ID).get()
            assertEquals(PaymentStatus.CANCELLED, payment.status)
            assertNull(payment.associatedProperty)
        } finally {
            finishRegistration.countDown()
            executor.shutdown()
            executor.awaitTermination(TIMEOUT_SECONDS, TimeUnit.SECONDS)
        }
    }

    @Test
    fun `createPropertyRegistrationPayment called twice at once keeps one CREATED payment and cancels the other on GOV UK Pay`() {
        // Arrange
        val executor = Executors.newFixedThreadPool(2)
        val bothPaymentsCreatedOnGovUkPay = CyclicBarrier(2)
        val paymentCount = AtomicInteger()
        JourneyTestHelper.setMockUser(USER_ID)
        val quote = paymentService.getPropertyRegistrationPaymentQuote()
        whenever(mockGovUkPayClient.createPayment(any())).thenAnswer { invocation ->
            val paymentId = "concurrent-payment-${paymentCount.incrementAndGet()}"
            // Neither payment is saved until both are created, so neither request marks the other's payment for cancellation
            bothPaymentsCreatedOnGovUkPay.await(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            createGovUkPayCreatedPayment(
                paymentId = paymentId,
                reference = invocation.getArgument<GovUkPayCreatePaymentRequest>(0).reference,
                nextUrl = "$NEXT_URL_PREFIX$paymentId",
            )
        }

        try {
            // Act
            val results =
                List(2) {
                    executor.submit<PropertyRegistrationPaymentCreationResult> {
                        JourneyTestHelper.setMockUser(USER_ID)
                        paymentService.createPropertyRegistrationPayment(JOURNEY_ID, "user@example.test", quote)
                    }
                }.map { it.get(TIMEOUT_SECONDS, TimeUnit.SECONDS) }

            // Assert
            assertEquals(1, results.count { it == PropertyRegistrationPaymentCreationResult.ConcurrentPaymentStarted })
            val newPayment = results.filterIsInstance<PropertyRegistrationPaymentCreationResult.NewPayment>().single()
            val savedPaymentId = newPayment.nextUrl.removePrefix(NEXT_URL_PREFIX)
            val unsavedPaymentId = setOf("concurrent-payment-1", "concurrent-payment-2").minus(savedPaymentId).single()
            val incompleteProperty = landlordIncompletePropertiesRepository.findById(INCOMPLETE_PROPERTY_ID).get()
            val createdPayment = paymentRepository.findByAssociatedIncompletePropertyAndStatus(incompleteProperty, PaymentStatus.CREATED)
            assertEquals(savedPaymentId, createdPayment?.paymentId)
            assertFalse(paymentRepository.existsById(unsavedPaymentId))
            verify(mockGovUkPayClient).cancelPayment(unsavedPaymentId)
            verify(mockGovUkPayClient, never()).cancelPayment(savedPaymentId)
            verify(mockGovUkPayClient).cancelPayment(CREATED_PAYMENT_ID)
            assertEquals(PaymentStatus.CANCELLED, paymentRepository.findStatusByPaymentId(CREATED_PAYMENT_ID))
        } finally {
            executor.shutdown()
            executor.awaitTermination(TIMEOUT_SECONDS, TimeUnit.SECONDS)
        }
    }

    @Test
    fun `finalisePayment deletes the journey without deleting the finalised payment`() {
        // Arrange
        whenever(mockPropertyRegistrationService.registerProperty(any())).thenAnswer { getProperty() }

        // Act
        val status = paymentService.finalisePayment(CREATED_PAYMENT_ID, PaymentStatus.CAPTURABLE, registrationData)

        // Assert
        assertEquals(PaymentStatus.SUCCEEDED, status)
        assertNull(savedJourneyStateRepository.findByJourneyIdAndUser_Id(JOURNEY_ID, USER_ID))
        assertFalse(landlordIncompletePropertiesRepository.existsById(INCOMPLETE_PROPERTY_ID))
        assertPaymentSucceededForProperty()
        verify(mockGovUkPayClient).capturePayment(CREATED_PAYMENT_ID)
    }

    @Test
    fun `finalisePayment keeps the journey and the property's other payments when the payment cannot be captured`() {
        // Arrange
        whenever(mockPropertyRegistrationService.registerProperty(any())).thenAnswer { getProperty() }
        stubCaptureFailure(createGovUkPayNotCapturableException(), GovUkPayPaymentStatus.CANCELLED)

        // Act
        val status = paymentService.finalisePayment(CREATED_PAYMENT_ID, PaymentStatus.CAPTURABLE, registrationData)

        // Assert
        assertEquals(PaymentStatus.CANCELLED, status)
        assertNotNull(savedJourneyStateRepository.findByJourneyIdAndUser_Id(JOURNEY_ID, USER_ID))
        val payment = paymentRepository.findById(CREATED_PAYMENT_ID).get()
        assertEquals(PaymentStatus.CANCELLED, payment.status)
        assertEquals(INCOMPLETE_PROPERTY_ID, payment.associatedIncompleteProperty?.id)
        assertEquals(INCOMPLETE_PROPERTY_ID, paymentRepository.findById(CANCELLING_PAYMENT_ID).get().associatedIncompleteProperty?.id)
    }

    @Test
    fun `finalisePayment leaves a payment it could neither register nor cancel marked for cancellation without emailing support`() {
        // Arrange
        whenever(mockPropertyRegistrationService.registerProperty(any()))
            .thenThrow(EntityExistsException("Address already registered"))
        doThrow(GovUkPayException("Cancel failed")).whenever(mockGovUkPayClient).cancelPayment(CREATED_PAYMENT_ID)

        // Act
        val status = paymentService.finalisePayment(CREATED_PAYMENT_ID, PaymentStatus.CAPTURABLE, registrationData)

        // Assert
        assertEquals(PaymentStatus.CANCELLING, status)
        val payment = paymentRepository.findById(CREATED_PAYMENT_ID).get()
        assertEquals(PaymentStatus.CANCELLING, payment.status)
        assertNull(payment.associatedProperty)
        verify(mockManualInterventionEmailSender, never()).sendEmail(any(), any())
    }

    @Test
    fun `finalisePayment records manual intervention and emails support when the transaction fails to commit after capture`() {
        // Arrange
        whenever(mockPropertyRegistrationService.registerProperty(any())).thenAnswer { getProperty() }
        doAnswer {
            TransactionSynchronizationManager.registerSynchronization(
                object : TransactionSynchronization {
                    override fun beforeCommit(readOnly: Boolean): Unit = throw IllegalStateException("Commit failed")
                },
            )
        }.whenever(mockGovUkPayClient).capturePayment(CREATED_PAYMENT_ID)

        // Act
        val status = paymentService.finalisePayment(CREATED_PAYMENT_ID, PaymentStatus.CAPTURABLE, registrationData)

        // Assert
        assertEquals(PaymentStatus.MANUAL_INTERVENTION, status)
        assertEquals(PaymentStatus.MANUAL_INTERVENTION, paymentRepository.findStatusByPaymentId(CREATED_PAYMENT_ID))
        verify(mockManualInterventionEmailSender).sendEmail(eq(MANUAL_INTERVENTION_EMAIL_ADDRESS), any())
        verify(mockGovUkPayClient, never()).cancelPayment(any())
    }

    @Test
    fun `finalisePayment cancels a payment whose journey has been deleted`() {
        // Arrange
        TransactionTemplate(transactionManager).executeWithoutResult { deleteJourney() }

        // Act
        val status = paymentService.finalisePayment(CREATED_PAYMENT_ID, PaymentStatus.CAPTURABLE, registrationData)

        // Assert
        assertEquals(PaymentStatus.CANCELLED, status)
        verify(mockGovUkPayClient).cancelPayment(CREATED_PAYMENT_ID)
        verify(mockPropertyRegistrationService, never()).registerProperty(any())
        verify(mockGovUkPayClient, never()).capturePayment(any())
    }

    @Test
    fun `getStoredPropertyRegistrationPaymentStatus returns the stored status of the journey's payment with the given reference`() {
        // Arrange
        JourneyTestHelper.setMockUser(USER_ID)

        // Act
        val paymentStatusCheck = paymentService.getStoredPropertyRegistrationPaymentStatus(JOURNEY_ID, CREATED_PAYMENT_REFERENCE)

        // Assert
        assertEquals(CREATED_PAYMENT_ID, paymentStatusCheck.paymentId)
        assertEquals(PaymentStatus.CREATED, paymentStatusCheck.status)
        verify(mockGovUkPayClient, never()).getPayment(any())
    }

    @Test
    fun `getStoredPropertyRegistrationPaymentStatus does not find a payment with the given reference for another property`() {
        // Arrange
        JourneyTestHelper.setMockUser(USER_ID)

        // Act, Assert
        assertThrows<IllegalStateException> {
            paymentService.getStoredPropertyRegistrationPaymentStatus(JOURNEY_ID, SUCCEEDED_PAYMENT_REFERENCE)
        }
        verify(mockGovUkPayClient, never()).getPayment(any())
    }

    @Test
    fun `getLatestSucceededPaymentForProperty returns the property's most recent succeeded payment`() {
        // Arrange
        val property = propertyOwnershipRepository.findById(SUCCEEDED_PAYMENT_PROPERTY_OWNERSHIP_ID).get()
        val incompleteProperty = landlordIncompletePropertiesRepository.findById(INCOMPLETE_PROPERTY_ID).get()
        paymentRepository.save(
            createPayment(
                paymentId = NEWER_SUCCEEDED_PAYMENT_ID,
                paymentCreatedAt = Instant.now(),
                status = PaymentStatus.SUCCEEDED,
                incompleteProperty = incompleteProperty,
            ).apply { associateWithProperty(property) },
        )
        paymentRepository.save(
            createPayment(
                paymentId = NEWEST_FAILED_PAYMENT_ID,
                paymentCreatedAt = Instant.now().plus(1, ChronoUnit.DAYS),
                status = PaymentStatus.FAILED,
                incompleteProperty = incompleteProperty,
            ).apply { associateWithProperty(property) },
        )

        // Act
        val latestPayment = paymentService.getLatestSucceededPaymentForProperty(property)

        // Assert
        assertEquals(NEWER_SUCCEEDED_PAYMENT_ID, latestPayment?.paymentId)
    }

    @Test
    fun `getLatestSucceededPaymentForProperty returns null when the property has no succeeded payment`() {
        // Act
        val latestPayment = paymentService.getLatestSucceededPaymentForProperty(getProperty())

        // Assert
        assertNull(latestPayment)
    }

    private fun stubCaptureFailure(
        captureFailure: GovUkPayException,
        govUkPayStatus: GovUkPayPaymentStatus,
    ) {
        doThrow(captureFailure).whenever(mockGovUkPayClient).capturePayment(CREATED_PAYMENT_ID)
        whenever(mockGovUkPayClient.getPaymentWithRetry(CREATED_PAYMENT_ID))
            .thenReturn(createGovUkPayPayment(paymentId = CREATED_PAYMENT_ID, status = govUkPayStatus))
    }

    private fun getProperty(): PropertyOwnership = propertyOwnershipRepository.findById(PROPERTY_OWNERSHIP_ID).get()

    private fun deleteJourney() = savedJourneyStateRepository.deleteByJourneyIdAndUser_Id(JOURNEY_ID, USER_ID)

    private fun setLandlordAnniversary() {
        val landlord = landlordRepository.findById(LANDLORD_ID).get()
        landlord.setAnniversaryIfAbsent(MonthDay.of(1, 1))
        landlordRepository.saveAndFlush(landlord)
    }

    private fun assertPaymentSucceededForProperty(propertyId: Long = PROPERTY_OWNERSHIP_ID) {
        val payment = paymentRepository.findById(CREATED_PAYMENT_ID).get()
        assertEquals(PaymentStatus.SUCCEEDED, payment.status)
        assertEquals(propertyId, payment.associatedProperty?.id)
        assertNull(payment.associatedIncompleteProperty)
        assertEquals(originalPeriodEnd, payment.forPeriodEnding)
    }

    companion object {
        private const val CREATED_PAYMENT_ID = "created-payment"
        private const val CREATED_PAYMENT_REFERENCE = "created-payment-reference"
        private const val CANCELLING_PAYMENT_ID = "cancelling-payment"
        private const val SUCCEEDED_PAYMENT_ID = "succeeded-payment"
        private const val SUCCEEDED_PAYMENT_REFERENCE = "succeeded-payment-reference"
        private const val FAILED_PAYMENT_ID = "failed-payment"
        private const val CANCELLED_PAYMENT_ID = "cancelled-payment"
        private const val NEXT_URL_PREFIX = "https://pay.example.test/"
        private const val NEWER_SUCCEEDED_PAYMENT_ID = "newer-succeeded-payment"
        private const val NEWEST_FAILED_PAYMENT_ID = "newest-failed-payment"
        private const val LANDLORD_ID = 1L
        private const val PROPERTY_OWNERSHIP_ID = 1L
        private const val SUCCEEDED_PAYMENT_PROPERTY_OWNERSHIP_ID = 51L
        private const val USER_ID = "urn:fdc:gov.uk:2022:UVWXY"
        private const val JOURNEY_ID = "example-incomplete-journey-with-payments"
        private val INCOMPLETE_PROPERTY_ID = LandlordIncompletePropertyId(USER_ID, 6L)
        private const val TIMEOUT_SECONDS = 10L
        private const val MANUAL_INTERVENTION_EMAIL_ADDRESS = "payments-support@example.com"
        private val NOW = Instant.parse("2026-01-01T12:00:00Z")
    }
}
