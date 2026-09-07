package uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import uk.gov.communities.prsdb.webapp.constants.enums.LicensingType
import uk.gov.communities.prsdb.webapp.database.entity.License
import uk.gov.communities.prsdb.webapp.database.entity.PropertyOwnership
import uk.gov.communities.prsdb.webapp.helpers.DateTimeHelper
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLandlordData.Companion.createOccupiedPropertyOwnership
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLandlordData.Companion.createUnoccupiedPropertyOwnership
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockMessageSource
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockPropertyComplianceData.Companion.createPropertyCompliance
import java.time.LocalDate

class LettingAgentPropertyDetailsViewModelTests {
    private val mockMessageSource = MockMessageSource()

    // A property "occupied when registered" has a lastOccupiedDate matching its registration (created) date,
    // which is what starts the 28-day provide-later deadline.
    private val occupiedAtRegistrationDate = LocalDate.of(2025, 1, 1)
    private val occupiedAtRegistrationInstant =
        occupiedAtRegistrationDate.atStartOfDay(DateTimeHelper.UK_ZONE).toInstant()

    private fun validCompliance(propertyOwnership: PropertyOwnership) = createPropertyCompliance(propertyOwnership = propertyOwnership)

    private fun missingCompliance(propertyOwnership: PropertyOwnership) =
        createPropertyCompliance(propertyOwnership = propertyOwnership, gasSafetyCertIssueDate = null)

    private fun provideLaterCompliance(propertyOwnership: PropertyOwnership) =
        validCompliance(propertyOwnership).apply { gasSafetyCertProvideLater = true }

    private fun expiredCompliance(propertyOwnership: PropertyOwnership) =
        createPropertyCompliance(
            propertyOwnership = propertyOwnership,
            electricalSafetyExpiryDate = LocalDate.now().minusDays(1),
        )

    @Test
    fun `the provide-details inset is shown when licensing details are outstanding and there is a deadline`() {
        val propertyOwnership =
            createOccupiedPropertyOwnership(
                createdDate = occupiedAtRegistrationInstant,
                lastOccupiedDate = occupiedAtRegistrationDate,
                licenseProvideLater = true,
            )

        val viewModel =
            LettingAgentPropertyDetailsViewModel(propertyOwnership, validCompliance(propertyOwnership), mockMessageSource)

        assertTrue(viewModel.showProvideDetailsInset)
        assertEquals(1, viewModel.licensingSection.size)
    }

    @Test
    fun `the provide-details inset is shown when tenancy details are outstanding and there is a deadline`() {
        val propertyOwnership =
            createOccupiedPropertyOwnership(
                createdDate = occupiedAtRegistrationInstant,
                lastOccupiedDate = occupiedAtRegistrationDate,
                tenancyProvideLater = true,
            )

        val viewModel =
            LettingAgentPropertyDetailsViewModel(propertyOwnership, validCompliance(propertyOwnership), mockMessageSource)

        assertTrue(viewModel.showProvideDetailsInset)
        assertEquals(1, viewModel.tenancySection.size)
    }

    @Test
    fun `the provide-details inset is shown when compliance is provide-later and there is a deadline`() {
        val propertyOwnership =
            createOccupiedPropertyOwnership(
                createdDate = occupiedAtRegistrationInstant,
                lastOccupiedDate = occupiedAtRegistrationDate,
                licenseProvideLater = false,
                tenancyProvideLater = false,
            )

        val viewModel =
            LettingAgentPropertyDetailsViewModel(propertyOwnership, provideLaterCompliance(propertyOwnership), mockMessageSource)

        assertTrue(viewModel.showProvideDetailsInset)
    }

    @Test
    fun `the provide-details inset is hidden when compliance is missing but not provide-later`() {
        val propertyOwnership =
            createOccupiedPropertyOwnership(
                createdDate = occupiedAtRegistrationInstant,
                lastOccupiedDate = occupiedAtRegistrationDate,
                licenseProvideLater = false,
                tenancyProvideLater = false,
            )

        val viewModel =
            LettingAgentPropertyDetailsViewModel(propertyOwnership, missingCompliance(propertyOwnership), mockMessageSource)

        assertFalse(viewModel.showProvideDetailsInset)
    }

    @Test
    fun `the provide-details inset is hidden when compliance is expired but not provide-later`() {
        val propertyOwnership =
            createOccupiedPropertyOwnership(
                createdDate = occupiedAtRegistrationInstant,
                lastOccupiedDate = occupiedAtRegistrationDate,
                licenseProvideLater = false,
                tenancyProvideLater = false,
            )

        val viewModel =
            LettingAgentPropertyDetailsViewModel(propertyOwnership, expiredCompliance(propertyOwnership), mockMessageSource)

        assertFalse(viewModel.showProvideDetailsInset)
    }

    @Test
    fun `the provide-details inset is hidden when details are outstanding but there is no deadline`() {
        val propertyOwnership =
            createOccupiedPropertyOwnership(
                createdDate = occupiedAtRegistrationInstant,
                lastOccupiedDate = occupiedAtRegistrationDate.plusDays(30),
                licenseProvideLater = true,
                tenancyProvideLater = true,
            )

        val viewModel =
            LettingAgentPropertyDetailsViewModel(propertyOwnership, validCompliance(propertyOwnership), mockMessageSource)

        assertFalse(viewModel.showProvideDetailsInset)
    }

    @Test
    fun `the provide-details inset is hidden when all details and compliance are provided`() {
        val propertyOwnership =
            createOccupiedPropertyOwnership(
                createdDate = occupiedAtRegistrationInstant,
                lastOccupiedDate = occupiedAtRegistrationDate,
                licenseProvideLater = false,
                tenancyProvideLater = false,
            )

        val viewModel =
            LettingAgentPropertyDetailsViewModel(propertyOwnership, validCompliance(propertyOwnership), mockMessageSource)

        assertFalse(viewModel.showProvideDetailsInset)
    }

    @Test
    fun `constructing the view model throws when the property is not occupied`() {
        val propertyOwnership = createUnoccupiedPropertyOwnership()

        assertThrows<IllegalStateException> {
            LettingAgentPropertyDetailsViewModel(propertyOwnership, validCompliance(propertyOwnership), mockMessageSource)
        }
    }

    @Test
    fun `the licensing section shows the licensing type and number rows when licensing details are provided`() {
        val propertyOwnership =
            createOccupiedPropertyOwnership(license = License(LicensingType.HMO_MANDATORY_LICENCE, "L1234"))

        val viewModel =
            LettingAgentPropertyDetailsViewModel(propertyOwnership, validCompliance(propertyOwnership), mockMessageSource)

        assertEquals(
            listOf(
                "propertyDetails.propertyRecord.licensingInformation.licensingType",
                "propertyDetails.propertyRecord.licensingInformation.licensingNumber",
            ),
            viewModel.licensingSection.map { it.fieldHeading },
        )
    }
}
