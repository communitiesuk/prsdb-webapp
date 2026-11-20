package uk.gov.communities.prsdb.webapp.integration

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpSession
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes
import uk.gov.communities.prsdb.webapp.constants.REACTIVATED_PROPERTY_OWNERSHIP_ID
import uk.gov.communities.prsdb.webapp.constants.enums.FurnishedStatus
import uk.gov.communities.prsdb.webapp.constants.enums.LicensingType
import uk.gov.communities.prsdb.webapp.constants.enums.RentFrequency
import uk.gov.communities.prsdb.webapp.models.dataModels.LicensingDetailsDataModel
import uk.gov.communities.prsdb.webapp.models.dataModels.PropertyReactivationDataModel
import uk.gov.communities.prsdb.webapp.models.dataModels.ReactivationGasSafetyDataModel
import uk.gov.communities.prsdb.webapp.models.dataModels.TenancyDetailsDataModel
import uk.gov.communities.prsdb.webapp.services.PropertyReactivationService
import uk.gov.communities.prsdb.webapp.testHelpers.JourneyTestHelper
import java.math.BigDecimal

class PropertyReactivationServicePersistenceTests : IntegrationTestWithMutableData("data-local.sql") {
    @Autowired
    private lateinit var propertyReactivationService: PropertyReactivationService

    @Autowired
    private lateinit var jdbcTemplate: JdbcTemplate

    private val session = MockHttpSession()

    @BeforeEach
    fun setUp() {
        JourneyTestHelper.setMockUser(USER_ID)
        RequestContextHolder.setRequestAttributes(ServletRequestAttributes(MockHttpServletRequest().also { it.setSession(session) }))
    }

    @AfterEach
    fun tearDown() {
        SecurityContextHolder.clearContext()
        RequestContextHolder.resetRequestAttributes()
    }

    @Test
    fun `reactivatePropertyIfAddressAvailable saves nothing and doesn't record the property in the session when reactivating it fails`() {
        // Arrange
        jdbcTemplate.update(
            "UPDATE property_ownership SET address_id = (SELECT address_id FROM property_ownership WHERE id = ?) WHERE id = ?",
            INACTIVE_PROPERTY_ID,
            ACTIVE_PROPERTY_ID,
        )
        val reactivationData =
            PropertyReactivationDataModel(
                licensing = LicensingDetailsDataModel(LicensingType.NO_LICENSING, null),
                gasSafety = ReactivationGasSafetyDataModel(hasGasSupply = false, issueDate = null, uploadIds = emptyList()),
                tenancyDetails =
                    TenancyDetailsDataModel(
                        numberOfHouseholds = 1,
                        numberOfPeople = 2,
                        billsIncludedList = null,
                        customBillsIncluded = null,
                        furnishedStatus = FurnishedStatus.FURNISHED,
                        rentFrequency = RentFrequency.MONTHLY,
                        customRentFrequency = null,
                        rentAmount = BigDecimal("400"),
                    ),
            )

        // Act
        assertThrows<DataIntegrityViolationException> {
            propertyReactivationService.reactivatePropertyIfAddressAvailable(INACTIVE_PROPERTY_ID, uprn = null, reactivationData)
        }

        // Assert
        val propertyOwnership =
            jdbcTemplate.queryForMap(
                "SELECT is_active, license_provide_later, tenancy_provide_later, rent_amount FROM property_ownership WHERE id = ?",
                INACTIVE_PROPERTY_ID,
            )
        assertEquals(false, propertyOwnership["is_active"])
        assertEquals(true, propertyOwnership["license_provide_later"])
        assertEquals(true, propertyOwnership["tenancy_provide_later"])
        assertEquals(0, BigDecimal("123.12").compareTo(propertyOwnership["rent_amount"] as BigDecimal))
        assertEquals(
            true,
            jdbcTemplate.queryForObject(
                "SELECT gas_safety_cert_provide_later FROM property_compliance WHERE property_ownership_id = ?",
                Boolean::class.java,
                INACTIVE_PROPERTY_ID,
            ),
        )
        assertNull(session.getAttribute(REACTIVATED_PROPERTY_OWNERSHIP_ID))
    }

    companion object {
        private const val USER_ID = "urn:fdc:gov.uk:2022:UVWXY"
        private const val INACTIVE_PROPERTY_ID = 52L
        private const val ACTIVE_PROPERTY_ID = 1L
    }
}
