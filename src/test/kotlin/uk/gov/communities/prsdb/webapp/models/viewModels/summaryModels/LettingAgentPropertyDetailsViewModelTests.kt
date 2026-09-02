package uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import uk.gov.communities.prsdb.webapp.controllers.LettingAgentUpdateHouseholdsAndTenantsController
import uk.gov.communities.prsdb.webapp.controllers.LettingAgentUpdateTenancyDetailsController
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.HouseholdStep
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLandlordData.Companion.createOccupiedPropertyOwnership
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLandlordData.Companion.createUnoccupiedPropertyOwnership
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockMessageSource
import java.util.UUID

class LettingAgentPropertyDetailsViewModelTests {
    private val mockMessageSource = MockMessageSource()

    private val token: UUID = UUID.fromString("3334abcd-5678-abcd-1234-567abcd2222b")

    @Test
    fun `the provide-details banner is shown when licensing details are outstanding`() {
        val propertyOwnership = createOccupiedPropertyOwnership(licenseProvideLater = true)

        val viewModel =
            LettingAgentPropertyDetailsViewModel(
                propertyOwnership,
                complianceAllValid = true,
                token = token,
                messageSource = mockMessageSource,
            )

        assertTrue(viewModel.showProvideDetailsBanner)
        assertTrue(viewModel.provideDetailsBannerText.isNotBlank())
        assertEquals(1, viewModel.licensingSection.size)
    }

    @Test
    fun `the provide-details banner is shown when tenancy details are outstanding`() {
        val propertyOwnership = createOccupiedPropertyOwnership(tenancyProvideLater = true)

        val viewModel =
            LettingAgentPropertyDetailsViewModel(
                propertyOwnership,
                complianceAllValid = true,
                token = token,
                messageSource = mockMessageSource,
            )

        assertTrue(viewModel.showProvideDetailsBanner)
        assertEquals(1, viewModel.tenancySection.size)
    }

    @Test
    fun `the provide-details banner is shown when compliance certificates are outstanding`() {
        val propertyOwnership = createOccupiedPropertyOwnership()

        val viewModel =
            LettingAgentPropertyDetailsViewModel(
                propertyOwnership,
                complianceAllValid = false,
                token = token,
                messageSource = mockMessageSource,
            )

        assertTrue(viewModel.showProvideDetailsBanner)
    }

    @Test
    fun `the provide-details banner is hidden when all details and compliance are provided`() {
        val propertyOwnership =
            createOccupiedPropertyOwnership(licenseProvideLater = false, tenancyProvideLater = false)

        val viewModel =
            LettingAgentPropertyDetailsViewModel(
                propertyOwnership,
                complianceAllValid = true,
                token = token,
                messageSource = mockMessageSource,
            )

        assertFalse(viewModel.showProvideDetailsBanner)
    }

    @Test
    fun `constructing the view model throws when the property is not occupied`() {
        val propertyOwnership = createUnoccupiedPropertyOwnership()

        assertThrows<IllegalStateException> {
            LettingAgentPropertyDetailsViewModel(
                propertyOwnership,
                complianceAllValid = true,
                token = token,
                messageSource = mockMessageSource,
            )
        }
    }

    @Test
    fun `the licensing section shows the licensing type and number rows when licensing details are provided`() {
        val propertyOwnership = createOccupiedPropertyOwnership()

        val viewModel =
            LettingAgentPropertyDetailsViewModel(
                propertyOwnership,
                complianceAllValid = true,
                token = token,
                messageSource = mockMessageSource,
            )

        assertEquals(
            listOf("propertyDetails.propertyRecord.licensingInformation.licensingType"),
            viewModel.licensingSection.map { it.fieldHeading },
        )
    }

    @Test
    fun `the households row has a change link to the letting-agent update-households-and-tenants journey`() {
        val propertyOwnership =
            createOccupiedPropertyOwnership(licenseProvideLater = false, tenancyProvideLater = false)

        val viewModel =
            LettingAgentPropertyDetailsViewModel(
                propertyOwnership,
                complianceAllValid = true,
                token = token,
                messageSource = mockMessageSource,
            )

        val householdsRow =
            viewModel.tenancySection.single {
                it.fieldHeading == "propertyDetails.propertyRecord.tenancyAndRentalInformation.numberOfHouseholds.rowName"
            }
        assertEquals(
            LettingAgentUpdateHouseholdsAndTenantsController.getRoute(token, HouseholdStep.ROUTE_SEGMENT),
            householdsRow.actions.single().url,
        )
    }

    @Test
    fun `the tenants row has no change link`() {
        val propertyOwnership =
            createOccupiedPropertyOwnership(licenseProvideLater = false, tenancyProvideLater = false)

        val viewModel =
            LettingAgentPropertyDetailsViewModel(
                propertyOwnership,
                complianceAllValid = true,
                token = token,
                messageSource = mockMessageSource,
            )

        val tenantsRow =
            viewModel.tenancySection.single {
                it.fieldHeading == "propertyDetails.propertyRecord.tenancyAndRentalInformation.numberOfPeople"
            }
        assertFalse(tenantsRow.hasActions)
    }

    @Test
    fun `the tenancy provide-later row has a change link to the letting-agent update-tenancy-details journey`() {
        val propertyOwnership = createOccupiedPropertyOwnership(tenancyProvideLater = true)

        val viewModel =
            LettingAgentPropertyDetailsViewModel(
                propertyOwnership,
                complianceAllValid = true,
                token = token,
                messageSource = mockMessageSource,
            )

        val tenancyRow = viewModel.tenancySection.single()
        assertEquals(
            LettingAgentUpdateTenancyDetailsController.getRoute(token, HouseholdStep.ROUTE_SEGMENT),
            tenancyRow.actions.single().url,
        )
    }
}
