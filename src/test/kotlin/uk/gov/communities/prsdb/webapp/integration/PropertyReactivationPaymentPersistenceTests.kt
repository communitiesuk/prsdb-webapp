package uk.gov.communities.prsdb.webapp.integration

import jakarta.persistence.EntityManagerFactory
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.any
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpSession
import org.springframework.orm.jpa.EntityManagerHolder
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.support.TransactionSynchronizationManager
import org.springframework.transaction.support.TransactionTemplate
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes
import uk.gov.communities.prsdb.webapp.clients.GovUkPayClient
import uk.gov.communities.prsdb.webapp.constants.REACTIVATED_PROPERTY_OWNERSHIP_ID
import uk.gov.communities.prsdb.webapp.constants.enums.PaymentStatus
import uk.gov.communities.prsdb.webapp.database.repository.PaymentRepository
import uk.gov.communities.prsdb.webapp.database.repository.PropertyOwnershipRepository
import uk.gov.communities.prsdb.webapp.database.repository.PrsdbUserRepository
import uk.gov.communities.prsdb.webapp.helpers.DateTimeHelper
import uk.gov.communities.prsdb.webapp.models.dataModels.PaymentCreationResult
import uk.gov.communities.prsdb.webapp.models.dataModels.PropertyPaymentQuote
import uk.gov.communities.prsdb.webapp.models.dataModels.PropertyReactivationDataModel
import uk.gov.communities.prsdb.webapp.models.dataModels.govUkPay.GovUkPayCreatePaymentRequest
import uk.gov.communities.prsdb.webapp.services.PaymentService
import uk.gov.communities.prsdb.webapp.services.PropertyReactivationService
import uk.gov.communities.prsdb.webapp.testHelpers.JourneyTestHelper
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockGovUkPayData.Companion.createGovUkPayCreatedPayment
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockPaymentData.Companion.createReactivationPayment
import java.time.LocalDate
import java.util.concurrent.CountDownLatch
import java.util.concurrent.CyclicBarrier
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException
import java.util.concurrent.atomic.AtomicInteger

class PropertyReactivationPaymentPersistenceTests : IntegrationTestWithMutableData(
    listOf(
        "data-mockuser-landlord-with-sole-and-joint-properties.sql",
        "data-overlay-joint-property-and-co-owner-are-inactive.sql",
    ),
) {
    @Autowired
    private lateinit var paymentRepository: PaymentRepository

    @Autowired
    private lateinit var propertyOwnershipRepository: PropertyOwnershipRepository

    @Autowired
    private lateinit var prsdbUserRepository: PrsdbUserRepository

    @Autowired
    private lateinit var transactionManager: PlatformTransactionManager

    @Autowired
    private lateinit var jdbcTemplate: JdbcTemplate

    @Autowired
    private lateinit var entityManagerFactory: EntityManagerFactory

    @Autowired
    private lateinit var paymentService: PaymentService

    @Autowired
    private lateinit var propertyReactivationService: PropertyReactivationService

    @MockitoBean
    private lateinit var mockGovUkPayClient: GovUkPayClient

    private val transactionTemplate get() = TransactionTemplate(transactionManager)

    private val quote =
        PropertyPaymentQuote(1234, LocalDate.now(DateTimeHelper.UK_ZONE), LocalDate.now(DateTimeHelper.UK_ZONE).plusYears(1))

    private val session = MockHttpSession()

    @BeforeEach
    fun setUpRequest() {
        RequestContextHolder.setRequestAttributes(ServletRequestAttributes(MockHttpServletRequest().also { it.setSession(session) }))
    }

    @AfterEach
    fun tearDown() {
        SecurityContextHolder.clearContext()
        RequestContextHolder.resetRequestAttributes()
    }

    @Test
    fun `a property can't have two CREATED payments at once`() {
        paymentRepository.saveAndFlush(createStoredPropertyPayment("first-payment-id"))

        val exception =
            assertThrows<DataIntegrityViolationException> {
                paymentRepository.saveAndFlush(createStoredPropertyPayment("second-payment-id"))
            }

        assertTrue(exception.mostSpecificCause.message!!.contains("uc_payment_one_created_per_property"))
    }

    @Test
    fun `a second claim waits for the first to commit, then claims nothing`() {
        val executor = Executors.newFixedThreadPool(2)
        val firstClaimMade = CountDownLatch(1)
        val commitFirstClaim = CountDownLatch(1)
        try {
            val firstClaim =
                executor.submit<Int> {
                    transactionTemplate.execute {
                        propertyOwnershipRepository.claimForReactivation(JOINT_PROPERTY_ID).also {
                            firstClaimMade.countDown()
                            commitFirstClaim.await(TIMEOUT_SECONDS, TimeUnit.SECONDS)
                        }
                    }
                }
            assertTrue(firstClaimMade.await(TIMEOUT_SECONDS, TimeUnit.SECONDS), "The first claim was not made")

            val secondClaim =
                executor.submit<Int> { transactionTemplate.execute { propertyOwnershipRepository.claimForReactivation(JOINT_PROPERTY_ID) } }
            assertThrows<TimeoutException>("The second claim did not wait") { secondClaim.get(500, TimeUnit.MILLISECONDS) }
            commitFirstClaim.countDown()

            assertEquals(1, firstClaim.get(TIMEOUT_SECONDS, TimeUnit.SECONDS))
            assertEquals(0, secondClaim.get(TIMEOUT_SECONDS, TimeUnit.SECONDS))
        } finally {
            commitFirstClaim.countDown()
            executor.shutdown()
            executor.awaitTermination(TIMEOUT_SECONDS, TimeUnit.SECONDS)
        }
    }

    @Test
    fun `starting a reactivation payment cancels another landlord's live payment for the property`() {
        paymentRepository.saveAndFlush(createStoredPropertyPayment("other-landlord-payment-id", payingUserId = LANDLORD_2_USER_ID))
        stubGovUkPayCreatePayment { "new-payment-id" }
        JourneyTestHelper.setMockUser(LANDLORD_1_USER_ID)

        val result =
            withOpenInViewEntityManager {
                paymentService.createPropertyReactivationPayment(JOINT_PROPERTY_ID, JOURNEY_ID, EMAIL, quote)
            }

        assertEquals(PaymentCreationResult.NewPayment("${NEXT_URL_PREFIX}new-payment-id"), result)
        verify(mockGovUkPayClient).cancelPayment("other-landlord-payment-id")
        assertEquals(PaymentStatus.CANCELLED, paymentRepository.findStatusByPaymentId("other-landlord-payment-id"))
        assertEquals(
            "new-payment-id",
            paymentRepository.findByAssociatedProperty_IdAndStatus(JOINT_PROPERTY_ID, PaymentStatus.CREATED)?.paymentId,
        )
        assertEquals(
            LANDLORD_1_USER_ID,
            jdbcTemplate.queryForObject("SELECT paying_user_id FROM payment WHERE payment_id = ?", String::class.java, "new-payment-id"),
        )
    }

    @Test
    fun `reactivating without payment reactivates the property, then cancels another landlord's live payment for it`() {
        paymentRepository.saveAndFlush(createStoredPropertyPayment("other-landlord-payment-id", payingUserId = LANDLORD_2_USER_ID))
        JourneyTestHelper.setMockUser(LANDLORD_1_USER_ID)

        propertyReactivationService.reactivatePropertyWithoutPayment(JOINT_PROPERTY_ID, PropertyReactivationDataModel())

        assertEquals(
            true,
            jdbcTemplate.queryForObject("SELECT is_active FROM property_ownership WHERE id = ?", Boolean::class.java, JOINT_PROPERTY_ID),
        )
        verify(mockGovUkPayClient).cancelPayment("other-landlord-payment-id")
        assertEquals(PaymentStatus.CANCELLED, paymentRepository.findStatusByPaymentId("other-landlord-payment-id"))
        assertEquals(JOINT_PROPERTY_ID, session.getAttribute(REACTIVATED_PROPERTY_OWNERSHIP_ID))
    }

    @Test
    fun `two landlords starting reactivation payments at once keep one CREATED payment and cancel the other on GOV UK Pay`() {
        val executor = Executors.newFixedThreadPool(2)
        val bothPaymentsCreatedOnGovUkPay = CyclicBarrier(2)
        val paymentCount = AtomicInteger()
        stubGovUkPayCreatePayment {
            // Neither payment is saved until both are created, so neither request cancels the other's payment first
            "concurrent-payment-${paymentCount.incrementAndGet()}".also {
                bothPaymentsCreatedOnGovUkPay.await(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            }
        }

        try {
            val results =
                listOf(LANDLORD_1_USER_ID, LANDLORD_2_USER_ID)
                    .map { userId ->
                        executor.submit<PaymentCreationResult> {
                            JourneyTestHelper.setMockUser(userId)
                            withOpenInViewEntityManager {
                                paymentService.createPropertyReactivationPayment(JOINT_PROPERTY_ID, JOURNEY_ID, EMAIL, quote)
                            }
                        }
                    }.map { it.get(TIMEOUT_SECONDS, TimeUnit.SECONDS) }

            assertEquals(1, results.count { it == PaymentCreationResult.ConcurrentPaymentStarted })
            val savedPaymentId =
                results.filterIsInstance<PaymentCreationResult.NewPayment>().single().nextUrl.removePrefix(NEXT_URL_PREFIX)
            val unsavedPaymentId = setOf("concurrent-payment-1", "concurrent-payment-2").minus(savedPaymentId).single()
            assertEquals(
                savedPaymentId,
                paymentRepository.findByAssociatedProperty_IdAndStatus(JOINT_PROPERTY_ID, PaymentStatus.CREATED)?.paymentId,
            )
            assertFalse(paymentRepository.existsById(unsavedPaymentId))
            verify(mockGovUkPayClient).cancelPayment(unsavedPaymentId)
        } finally {
            executor.shutdown()
            executor.awaitTermination(TIMEOUT_SECONDS, TimeUnit.SECONDS)
        }
    }

    // createPropertyReactivationPayment isn't transactional, so it relies on the web request's open-in-view persistence context
    private fun <T> withOpenInViewEntityManager(action: () -> T): T {
        val entityManager = entityManagerFactory.createEntityManager()
        TransactionSynchronizationManager.bindResource(entityManagerFactory, EntityManagerHolder(entityManager))
        try {
            return action()
        } finally {
            TransactionSynchronizationManager.unbindResource(entityManagerFactory)
            entityManager.close()
        }
    }

    private fun stubGovUkPayCreatePayment(nextPaymentId: () -> String) {
        whenever(mockGovUkPayClient.createPayment(any())).thenAnswer { invocation ->
            val paymentId = nextPaymentId()
            createGovUkPayCreatedPayment(
                paymentId = paymentId,
                reference = invocation.getArgument<GovUkPayCreatePaymentRequest>(0).reference,
                nextUrl = "$NEXT_URL_PREFIX$paymentId",
            )
        }
    }

    private fun createStoredPropertyPayment(
        paymentId: String,
        payingUserId: String = LANDLORD_1_USER_ID,
    ) = createReactivationPayment(
        paymentId = paymentId,
        reference = "reference-$paymentId",
        propertyOwnership = propertyOwnershipRepository.findById(JOINT_PROPERTY_ID).get(),
        payingUser = prsdbUserRepository.findById(payingUserId).get(),
    )

    companion object {
        private const val JOINT_PROPERTY_ID = 2L
        private const val LANDLORD_1_USER_ID = "urn:fdc:gov.uk:2022:UVWXY"
        private const val LANDLORD_2_USER_ID = "urn:fdc:gov.uk:2022:ABCDE"
        private const val TIMEOUT_SECONDS = 10L
        private const val JOURNEY_ID = "reactivation-journey"
        private const val EMAIL = "user@example.test"
        private const val NEXT_URL_PREFIX = "https://pay.example.test/"
    }
}
