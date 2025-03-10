package uk.gov.communities.prsdb.webapp.forms.journeys

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.boot.test.mock.mockito.SpyBean
import uk.gov.communities.prsdb.webapp.forms.journeys.factories.PropertyDeregistrationJourneyFactory
import uk.gov.communities.prsdb.webapp.forms.steps.DeregisterPropertyStepId
import uk.gov.communities.prsdb.webapp.services.AddressDataService
import uk.gov.communities.prsdb.webapp.services.JourneyDataService
import uk.gov.communities.prsdb.webapp.services.PropertyOwnershipService
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.AlwaysTrueValidator
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLandlordData.Companion.createPropertyOwnership

class PropertyDeregistrationJourneyTests {
    val alwaysTrueValidator: AlwaysTrueValidator = AlwaysTrueValidator()

    @MockBean
    private lateinit var mockJourneyDataService: JourneyDataService

    @MockBean
    private lateinit var mockPropertyOwnershipService: PropertyOwnershipService

    @MockBean
    private lateinit var mockAddressDataService: AddressDataService

    @SpyBean
    private lateinit var propertyDeregistrationJourneyFactory: PropertyDeregistrationJourneyFactory

    @BeforeEach
    fun setup() {
        mockJourneyDataService = mock()
        mockPropertyOwnershipService = mock()
        mockAddressDataService = mock()

        propertyDeregistrationJourneyFactory =
            PropertyDeregistrationJourneyFactory(
                alwaysTrueValidator,
                mockJourneyDataService,
                mockPropertyOwnershipService,
                mockAddressDataService,
            )
    }

    @Test
    fun `When a journey is created, the address is retrieved from the session instead of the database if it is available`() {
        // Arrange
        val propertyOwnership = createPropertyOwnership()
        val propertyOwnershipId = propertyOwnership.id

        whenever(mockAddressDataService.getCachedSingleLineAddressForPropertyOwnershipId(propertyOwnershipId))
            .thenReturn(propertyOwnership.property.address.singleLineAddress)

        // Act
        propertyDeregistrationJourneyFactory.create(propertyOwnershipId)

        // Assert
        verify(mockPropertyOwnershipService, never()).retrievePropertyOwnershipById(propertyOwnershipId)
        verify(mockAddressDataService, times(1)).getCachedSingleLineAddressForPropertyOwnershipId(propertyOwnershipId)
    }

    @Test
    fun `When a journey is created, the address is retrieved from the database and cached if it was not available`() {
        // Arrange
        val propertyOwnership = createPropertyOwnership()
        val propertyOwnershipId = propertyOwnership.id

        whenever(mockPropertyOwnershipService.retrievePropertyOwnershipById(propertyOwnershipId))
            .thenReturn(propertyOwnership)

        // Act
        propertyDeregistrationJourneyFactory.create(propertyOwnershipId)

        // Assert
        verify(mockPropertyOwnershipService, times(1)).retrievePropertyOwnershipById(propertyOwnershipId)
        verify(mockAddressDataService, times(1))
            .cacheSingleLineAddressForPropertyOwnershipId(propertyOwnershipId, propertyOwnership.property.address.singleLineAddress)
    }

    @Test
    fun `When the user exits the journey by submitting no on the are you sure step, the address is cleared from the cache`() {
        // Arrange
        val propertyOwnership = createPropertyOwnership()
        val propertyOwnershipId = propertyOwnership.id

        whenever(mockPropertyOwnershipService.retrievePropertyOwnershipById(propertyOwnershipId))
            .thenReturn(propertyOwnership)

        // Act
        propertyDeregistrationJourneyFactory
            .create(propertyOwnershipId)
            .completeStep(DeregisterPropertyStepId.AreYouSure.urlPathSegment, mapOf("wantsToProceed" to false), null, mock())

        verify(mockAddressDataService, times(1)).clearCachedSingleLineAddressForPropertyOwnershipId(propertyOwnershipId)
    }
}
