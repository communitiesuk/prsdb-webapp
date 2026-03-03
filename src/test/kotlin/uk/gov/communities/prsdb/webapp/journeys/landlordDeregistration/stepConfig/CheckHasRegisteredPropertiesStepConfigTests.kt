package uk.gov.communities.prsdb.webapp.journeys.landlordDeregistration.stepConfig

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever
import org.springframework.security.core.context.SecurityContextHolder
import uk.gov.communities.prsdb.webapp.journeys.landlordDeregistration.LandlordDeregistrationJourneyState
import uk.gov.communities.prsdb.webapp.services.PropertyOwnershipService
import uk.gov.communities.prsdb.webapp.testHelpers.JourneyTestHelper

@ExtendWith(MockitoExtension::class)
class CheckHasRegisteredPropertiesStepConfigTests {
    @Mock
    lateinit var mockPropertyOwnershipService: PropertyOwnershipService

    @Mock
    lateinit var mockState: LandlordDeregistrationJourneyState

    private val baseUserId = "test-user-id"

    @AfterEach
    fun tearDown() {
        SecurityContextHolder.clearContext()
    }

    @Test
    fun `mode returns HAS_PROPERTIES when landlord has registered properties`() {
        JourneyTestHelper.setMockUser(baseUserId)
        val stepConfig = CheckHasRegisteredPropertiesStepConfig(mockPropertyOwnershipService)
        whenever(mockPropertyOwnershipService.doesLandlordHaveRegisteredProperties(baseUserId)).thenReturn(true)

        val result = stepConfig.mode(mockState)

        assertEquals(HasRegisteredPropertiesStatus.HAS_PROPERTIES, result)
    }

    @Test
    fun `mode returns NO_PROPERTIES when landlord has no registered properties`() {
        JourneyTestHelper.setMockUser(baseUserId)
        val stepConfig = CheckHasRegisteredPropertiesStepConfig(mockPropertyOwnershipService)
        whenever(mockPropertyOwnershipService.doesLandlordHaveRegisteredProperties(baseUserId)).thenReturn(false)

        val result = stepConfig.mode(mockState)

        assertEquals(HasRegisteredPropertiesStatus.NO_PROPERTIES, result)
    }
}
