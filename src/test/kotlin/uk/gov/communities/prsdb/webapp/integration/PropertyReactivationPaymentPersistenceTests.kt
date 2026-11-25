package uk.gov.communities.prsdb.webapp.integration

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.support.TransactionTemplate
import uk.gov.communities.prsdb.webapp.constants.enums.PaymentStatus
import uk.gov.communities.prsdb.webapp.database.repository.PaymentRepository
import uk.gov.communities.prsdb.webapp.database.repository.PropertyOwnershipRepository
import uk.gov.communities.prsdb.webapp.database.repository.PrsdbUserRepository
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockPaymentData.Companion.createReactivationPayment
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

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

    private val transactionTemplate get() = TransactionTemplate(transactionManager)

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
    fun `a property can have a CREATED payment as well as finished ones`() {
        paymentRepository.saveAndFlush(createStoredPropertyPayment("cancelled-payment-id", PaymentStatus.CANCELLED))
        paymentRepository.saveAndFlush(createStoredPropertyPayment("succeeded-payment-id", PaymentStatus.SUCCEEDED))

        assertDoesNotThrow { paymentRepository.saveAndFlush(createStoredPropertyPayment("created-payment-id")) }
    }

    @Test
    fun `claimForReactivation activates an inactive property and reports that it has done so`() {
        val claimedRows = transactionTemplate.execute { propertyOwnershipRepository.claimForReactivation(JOINT_PROPERTY_ID) }

        assertEquals(1, claimedRows)
        assertTrue(propertyOwnershipRepository.findById(JOINT_PROPERTY_ID).get().isActive)
    }

    @Test
    fun `claimForReactivation does not claim an active property`() {
        jdbcTemplate.update("UPDATE property_ownership SET is_active = true WHERE id = ?", JOINT_PROPERTY_ID)

        assertEquals(0, transactionTemplate.execute { propertyOwnershipRepository.claimForReactivation(JOINT_PROPERTY_ID) })
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
    fun `existsByIsActiveTrueAndAddress_UprnAndIdNot ignores the property itself`() {
        jdbcTemplate.update("UPDATE property_ownership SET is_active = true WHERE id = ?", JOINT_PROPERTY_ID)

        assertFalse(propertyOwnershipRepository.existsByIsActiveTrueAndAddress_UprnAndIdNot(JOINT_PROPERTY_UPRN, JOINT_PROPERTY_ID))
        assertTrue(propertyOwnershipRepository.existsByIsActiveTrueAndAddress_UprnAndIdNot(JOINT_PROPERTY_UPRN, SOLE_PROPERTY_ID))
    }

    @Test
    fun `findByReferenceAndAssociatedProperty_IdAndPayingUser_Id does not find another landlord's payment`() {
        paymentRepository.saveAndFlush(createStoredPropertyPayment(PAYMENT_ID, payingUserId = LANDLORD_2_USER_ID))

        assertNull(
            paymentRepository.findByReferenceAndAssociatedProperty_IdAndPayingUser_Id(
                "reference-$PAYMENT_ID",
                JOINT_PROPERTY_ID,
                LANDLORD_1_USER_ID,
            ),
        )
    }

    private fun createStoredPropertyPayment(
        paymentId: String,
        status: PaymentStatus = PaymentStatus.CREATED,
        payingUserId: String = LANDLORD_1_USER_ID,
    ) = createReactivationPayment(
        paymentId = paymentId,
        reference = "reference-$paymentId",
        status = status,
        propertyOwnership = propertyOwnershipRepository.findById(JOINT_PROPERTY_ID).get(),
        payingUser = prsdbUserRepository.findById(payingUserId).get(),
    )

    companion object {
        private const val SOLE_PROPERTY_ID = 1L
        private const val JOINT_PROPERTY_ID = 2L
        private const val JOINT_PROPERTY_UPRN = 3L
        private const val LANDLORD_1_USER_ID = "urn:fdc:gov.uk:2022:UVWXY"
        private const val LANDLORD_2_USER_ID = "urn:fdc:gov.uk:2022:ABCDE"
        private const val PAYMENT_ID = "payment-id"
        private const val TIMEOUT_SECONDS = 10L
    }
}
