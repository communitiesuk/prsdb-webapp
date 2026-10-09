package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.update.correspondenceEmail

import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.ObjectFactory
import org.springframework.mock.web.MockHttpSession
import uk.gov.communities.prsdb.webapp.journeys.JourneyStateService
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.CorrespondenceEmailStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.CorrespondenceEmailStepConfig
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.FinishCyaJourneyConfig
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.FinishCyaJourneyStep
import uk.gov.communities.prsdb.webapp.services.CurrentUserService
import uk.gov.communities.prsdb.webapp.services.PropertyOwnershipService
import java.time.Instant
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class UpdateCorrespondenceEmailJourneyFactoryTests {
    private val session = MockHttpSession()
    private val propertyOwnershipService = mock<PropertyOwnershipService>()
    private val currentUserService = mock<CurrentUserService>()
    private val lastModifiedDate = Instant.parse("2026-09-01T12:00:00Z")
    private lateinit var state: UpdateCorrespondenceEmailJourney
    private val factory =
        UpdateCorrespondenceEmailJourneyFactory(
            ObjectFactory { createState().also { state = it } },
            propertyOwnershipService,
            currentUserService,
        )

    init {
        JourneyStateService(session, mock(), mock()).initialiseJourneyWithId("journey-id")
        whenever(propertyOwnershipService.getLastModifiedDate(1)).thenReturn(lastModifiedDate)
        whenever(currentUserService.getCurrentEmail()).thenReturn("account@example.com")
    }

    @Test
    fun `createJourneySteps initialises the state with the property last modified date and account email`() {
        // Act
        factory.createJourneySteps(1)

        // Assert
        assertEquals(1, state.propertyId)
        assertEquals(lastModifiedDate.toString(), state.lastModifiedDate)
        assertEquals("account@example.com", state.loggedInLandlordEmailAtStartOfJourney)
        assertTrue(state.isStateInitialized)
    }

    @Test
    fun `createJourneySteps preserves the initialised state on subsequent calls`() {
        // Arrange
        factory.createJourneySteps(1)
        whenever(currentUserService.getCurrentEmail()).thenReturn("changed@example.com")

        // Act
        factory.createJourneySteps(1)

        // Assert
        assertEquals("account@example.com", state.loggedInLandlordEmailAtStartOfJourney)
        verify(currentUserService, times(1)).getCurrentEmail()
        verify(propertyOwnershipService, times(1)).getLastModifiedDate(1)
    }

    @Test
    fun `createJourneySteps returns only the email step with a back link to CYA when checking answers`() {
        // Arrange
        factory.createJourneySteps(1)
        state.checkingAnswersFor = CorrespondenceEmailStep.ROUTE_SEGMENT
        state.cyaUrlPath = UpdateCorrespondenceEmailCyaStep.ROUTE_SEGMENT

        // Act
        val routes = factory.createJourneySteps(1)

        // Assert
        assertEquals(setOf(CorrespondenceEmailStep.ROUTE_SEGMENT), routes.keys)
        assertEquals(state.returnToCyaPageDestination.toUrlStringOrNull(), state.correspondenceEmailStep.backUrl)
    }

    private fun createState(): UpdateCorrespondenceEmailJourney {
        val journeyStateService = JourneyStateService(session, mock(), mock()).apply { setJourneyId("journey-id") }
        return UpdateCorrespondenceEmailJourney(
            CorrespondenceEmailStep(CorrespondenceEmailStepConfig()),
            UpdateCorrespondenceEmailCyaStep(UpdateCorrespondenceEmailCyaConfig(propertyOwnershipService, mock())),
            FinishCyaJourneyStep(FinishCyaJourneyConfig()),
            journeyStateService,
            ObjectFactory { createState() },
        )
    }
}
