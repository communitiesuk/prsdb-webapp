package uk.gov.communities.prsdb.webapp.models.viewModels.landlordsTab

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import uk.gov.communities.prsdb.webapp.constants.enums.RegistrationNumberType
import uk.gov.communities.prsdb.webapp.database.entity.RegistrationNumber
import uk.gov.communities.prsdb.webapp.models.dataModels.RegistrationNumberDataModel
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLandlordData

class PropertyLandlordsTabViewModelTests {
    @Test
    fun `fromPropertyOwnership marks the current user's card as isCurrentUser`() {
        val baseUser = MockLandlordData.createPrsdbUser(id = "current-user")
        val landlord = MockLandlordData.createLandlord(baseUser = baseUser)
        val propertyOwnership = MockLandlordData.createPropertyOwnership(primaryLandlord = landlord)

        val viewModel =
            PropertyLandlordsTabViewModel.fromPropertyOwnership(propertyOwnership, currentBaseUserId = "current-user")

        assertEquals(1, viewModel.registeredLandlords.size)
        assertTrue(viewModel.registeredLandlords.single().isCurrentUser)
    }

    @Test
    fun `fromPropertyOwnership marks the card as not isCurrentUser when the base user id does not match`() {
        val baseUser = MockLandlordData.createPrsdbUser(id = "someone-else")
        val landlord = MockLandlordData.createLandlord(baseUser = baseUser)
        val propertyOwnership = MockLandlordData.createPropertyOwnership(primaryLandlord = landlord)

        val viewModel =
            PropertyLandlordsTabViewModel.fromPropertyOwnership(propertyOwnership, currentBaseUserId = "current-user")

        assertFalse(viewModel.registeredLandlords.single().isCurrentUser)
    }

    @Test
    fun `fromPropertyOwnership populates the card with the landlord's name, registration number and email`() {
        val landlord =
            MockLandlordData.createLandlord(
                name = "Jane Doe",
                email = "jane@example.com",
                registrationNumber = RegistrationNumber(RegistrationNumberType.LANDLORD, 1234567890L),
            )
        val propertyOwnership = MockLandlordData.createPropertyOwnership(primaryLandlord = landlord)

        val viewModel = PropertyLandlordsTabViewModel.fromPropertyOwnership(propertyOwnership, currentBaseUserId = "x")

        val card = viewModel.registeredLandlords.single()
        assertEquals("Jane Doe", card.name)
        assertEquals("jane@example.com", card.email)
        assertEquals(
            RegistrationNumberDataModel.fromRegistrationNumber(landlord.registrationNumber).toString(),
            card.landlordRegistrationNumber,
        )
    }

    @Test
    fun `fromPropertyOwnership returns empty lists for pendingInvitations, expiredInvitations and joinRequests`() {
        // TODO PDJB-299: when sibling tickets land that populate these lists,
        // these assertions should be updated to reflect the real data.
        val propertyOwnership = MockLandlordData.createPropertyOwnership()

        val viewModel = PropertyLandlordsTabViewModel.fromPropertyOwnership(propertyOwnership, currentBaseUserId = "x")

        assertTrue(viewModel.pendingInvitations.isEmpty())
        assertTrue(viewModel.expiredInvitations.isEmpty())
        assertTrue(viewModel.joinRequests.isEmpty())
    }
}
