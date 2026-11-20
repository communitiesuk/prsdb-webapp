package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.update.freeholders

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException
import uk.gov.communities.prsdb.webapp.constants.enums.OwnershipType
import uk.gov.communities.prsdb.webapp.services.PropertyOwnershipService
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLandlordData

class UpdateFreeholdersJourneyFactoryTests {
    private val propertyOwnershipService = mock<PropertyOwnershipService>()
    private val factory =
        UpdateFreeholdersJourneyFactory(
            { mock<UpdateFreeholdersJourney>() },
            propertyOwnershipService,
        )

    @Test
    fun `createJourneySteps returns 404 when ownership type does not require freeholder records`() {
        // Arrange
        val propertyOwnership =
            MockLandlordData.createPropertyOwnership(
                id = 1,
                ownershipType = OwnershipType.SOLE_FREEHOLD,
            )
        whenever(propertyOwnershipService.getPropertyOwnership(propertyOwnership.id)).thenReturn(propertyOwnership)

        // Act
        val exception =
            assertThrows(ResponseStatusException::class.java) {
                factory.createJourneySteps(propertyOwnership.id)
            }

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, exception.statusCode)
    }
}
