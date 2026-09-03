package uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import uk.gov.communities.prsdb.webapp.constants.enums.LicensingType
import uk.gov.communities.prsdb.webapp.database.entity.License
import uk.gov.communities.prsdb.webapp.database.entity.PropertyOwnership
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLandlordData.Companion.createOccupiedPropertyOwnership
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLandlordData.Companion.createUnoccupiedPropertyOwnership
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockMessageSource
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockPropertyComplianceData.Companion.createPropertyCompliance

class LettingAgentPropertyDetailsViewModelTests {
    private val mockMessageSource = MockMessageSource()

    private fun validCompliance(propertyOwnership: PropertyOwnership) = createPropertyCompliance(propertyOwnership = propertyOwnership)

    private fun outstandingCompliance(propertyOwnership: PropertyOwnership) =
        createPropertyCompliance(propertyOwnership = propertyOwnership, gasSafetyCertIssueDate = null)

    @Test
    fun `the provide-details inset is shown when licensing details are outstanding`() {
        val propertyOwnership = createOccupiedPropertyOwnership(licenseProvideLater = true)

        val viewModel =
            LettingAgentPropertyDetailsViewModel(propertyOwnership, validCompliance(propertyOwnership), mockMessageSource)

        assertTrue(viewModel.showProvideDetailsInset)
        assertTrue(viewModel.provideDetailsInsetText.isNotBlank())
        assertEquals(1, viewModel.licensingSection.size)
    }

    @Test
    fun `the provide-details inset is shown when tenancy details are outstanding`() {
        val propertyOwnership = createOccupiedPropertyOwnership(tenancyProvideLater = true)

        val viewModel =
            LettingAgentPropertyDetailsViewModel(propertyOwnership, validCompliance(propertyOwnership), mockMessageSource)

        assertTrue(viewModel.showProvideDetailsInset)
        assertEquals(1, viewModel.tenancySection.size)
    }

    @Test
    fun `the provide-details inset is shown when compliance certificates are outstanding`() {
        val propertyOwnership = createOccupiedPropertyOwnership()

        val viewModel =
            LettingAgentPropertyDetailsViewModel(propertyOwnership, outstandingCompliance(propertyOwnership), mockMessageSource)

        assertTrue(viewModel.showProvideDetailsInset)
    }

    @Test
    fun `the provide-details inset is hidden when all details and compliance are provided`() {
        val propertyOwnership =
            createOccupiedPropertyOwnership(licenseProvideLater = false, tenancyProvideLater = false)

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
