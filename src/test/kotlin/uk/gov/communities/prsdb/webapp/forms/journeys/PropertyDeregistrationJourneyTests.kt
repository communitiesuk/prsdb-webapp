import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.boot.test.mock.mockito.SpyBean
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.server.ResponseStatusException
import uk.gov.communities.prsdb.webapp.forms.journeys.factories.PropertyDeregistrationJourneyFactory
import uk.gov.communities.prsdb.webapp.forms.steps.DeregisterPropertyStepId
import uk.gov.communities.prsdb.webapp.services.JourneyDataService
import uk.gov.communities.prsdb.webapp.services.PropertyOwnershipService
import uk.gov.communities.prsdb.webapp.services.PropertyService
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.AlwaysTrueValidator
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLandlordData.Companion.createPropertyOwnership
import kotlin.test.assertContains

class PropertyDeregistrationJourneyTests {
    val alwaysTrueValidator: AlwaysTrueValidator = AlwaysTrueValidator()

    @MockBean
    private lateinit var mockJourneyDataService: JourneyDataService

    @MockBean
    private lateinit var mockPropertyOwnershipService: PropertyOwnershipService

    @MockBean
    private lateinit var mockPropertyService: PropertyService

    @SpyBean
    private lateinit var propertyDeregistrationJourneyFactory: PropertyDeregistrationJourneyFactory

    @BeforeEach
    fun setup() {
        mockJourneyDataService = mock()
        mockPropertyOwnershipService = mock()
        mockPropertyService = mock()

        propertyDeregistrationJourneyFactory =
            PropertyDeregistrationJourneyFactory(
                alwaysTrueValidator,
                mockJourneyDataService,
                mockPropertyOwnershipService,
                mockPropertyService,
            )
    }

    // TODO: PRSD-697 - should we move this to a JourneyTest class that the others inherit from?
    private fun setMockUser(principalName: String) {
        val authentication = mock<Authentication>()
        whenever(authentication.name).thenReturn(principalName)
        val context = mock<SecurityContext>()
        whenever(context.authentication).thenReturn(authentication)
        SecurityContextHolder.setContext(context)
    }

    @Test
    fun `When the reason step is submitted by an authorised user, the property is deregistered`() {
        val propertyOwnership = createPropertyOwnership()
        val propertyOwnershipId = propertyOwnership.id

        val currentUserId = propertyOwnership.primaryLandlord.baseUser.id
        setMockUser(currentUserId)

        whenever(
            mockPropertyOwnershipService.getIsAuthorizedToDeleteRecord(
                propertyOwnershipId,
                currentUserId,
            ),
        ).thenReturn(true)
        whenever(mockPropertyOwnershipService.retrievePropertyOwnershipById(propertyOwnershipId))
            .thenReturn(propertyOwnership)

        // Act
        propertyDeregistrationJourneyFactory
            .create(propertyOwnershipId)
            .completeStep(DeregisterPropertyStepId.Reason.urlPathSegment, mapOf("reason" to ""), null, mock())

        // Assert
        verify(mockPropertyOwnershipService, times(1)).deletePropertyOwnership(propertyOwnership)
        verify(mockPropertyService, times(1)).deleteProperty(propertyOwnership.property)
    }

    @Test
    fun `When the reason step is submitted by an unauthorised user, an error is thrown`() {
        val propertyOwnership = createPropertyOwnership()
        val propertyOwnershipId = propertyOwnership.id

        val currentUserId = propertyOwnership.primaryLandlord.baseUser.id
        setMockUser(currentUserId)

        whenever(
            mockPropertyOwnershipService.getIsAuthorizedToDeleteRecord(
                propertyOwnershipId,
                currentUserId,
            ),
        ).thenReturn(false)
        whenever(mockPropertyOwnershipService.retrievePropertyOwnershipById(propertyOwnershipId))
            .thenReturn(propertyOwnership)

        // Act, Assert
        val thrown =
            assertThrows(ResponseStatusException::class.java) {
                propertyDeregistrationJourneyFactory
                    .create(propertyOwnershipId)
                    .completeStep(DeregisterPropertyStepId.Reason.urlPathSegment, mapOf("reason" to ""), null, mock())
            }

        // Assert
        assertContains(thrown.message, "The current user is not authorised to delete property ownership $propertyOwnershipId")
        verify(mockPropertyOwnershipService, never()).deletePropertyOwnership(propertyOwnership)
        verify(mockPropertyService, never()).deleteProperty(propertyOwnership.property)
    }
}
