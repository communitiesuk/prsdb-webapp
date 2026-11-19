package uk.gov.communities.prsdb.webapp.services

import jakarta.servlet.http.HttpSession
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.verify
import uk.gov.communities.prsdb.webapp.constants.REACTIVATED_PROPERTY_OWNERSHIP_ID

@ExtendWith(MockitoExtension::class)
class PropertyReactivationServiceTests {
    @Mock
    private lateinit var mockActiveInactiveService: ActiveInactiveService

    @Mock
    private lateinit var mockSession: HttpSession

    @InjectMocks
    private lateinit var propertyReactivationService: PropertyReactivationService

    private val propertyOwnershipId = 1L

    @Test
    fun `reactivateProperty reactivates the property and stores it in the session as the last property reactivated`() {
        // Act
        propertyReactivationService.reactivateProperty(propertyOwnershipId)

        // Assert
        verify(mockActiveInactiveService).reactivateProperty(propertyOwnershipId)
        verify(mockSession).setAttribute(REACTIVATED_PROPERTY_OWNERSHIP_ID, propertyOwnershipId)
    }
}
