package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.update.correspondenceEmail

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.ObjectFactory
import org.springframework.mock.web.MockHttpSession
import uk.gov.communities.prsdb.webapp.controllers.PropertyDetailsController
import uk.gov.communities.prsdb.webapp.exceptions.PrsdbWebException
import uk.gov.communities.prsdb.webapp.journeys.JourneyStateService
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.CorrespondenceEmailStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.CorrespondenceEmailStepConfig
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.FinishCyaJourneyConfig
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.FinishCyaJourneyStep
import uk.gov.communities.prsdb.webapp.models.viewModels.SectionHeaderViewModel
import uk.gov.communities.prsdb.webapp.services.PropertyOwnershipService
import uk.gov.communities.prsdb.webapp.services.UserToLandlordService
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLandlordData
import java.time.Instant
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class UpdateCorrespondenceEmailJourneyFactoryTests {
    private val session = MockHttpSession()
    private val propertyOwnershipService = mock<PropertyOwnershipService>()
    private val userToLandlordService = mock<UserToLandlordService>()
    private val lastModifiedDate = Instant.parse("2026-09-01T12:00:00Z")
    private lateinit var state: UpdateCorrespondenceEmailJourney
    private val factory =
        UpdateCorrespondenceEmailJourneyFactory(
            ObjectFactory { createState().also { state = it } },
            propertyOwnershipService,
            userToLandlordService,
        )

    init {
        JourneyStateService(session, mock(), mock()).initialiseJourneyWithId("journey-id")
        whenever(propertyOwnershipService.getLastModifiedDate(1)).thenReturn(lastModifiedDate)
        whenever(userToLandlordService.getCurrentLandlordForUser())
            .thenReturn(MockLandlordData.createIndividualLandlord(email = "account@example.com"))
    }

    @Test
    fun `initialising the journey snapshots the property timestamp and account email with no prefilled answer`() {
        val routes = factory.createJourneySteps(1)

        assertEquals(1, state.propertyId)
        assertEquals(lastModifiedDate.toString(), state.lastModifiedDate)
        assertEquals("account@example.com", state.loggedInLandlordEmailAtStartOfJourney)
        assertTrue(state.isStateInitialized)
        assertNull(state.correspondenceEmailStep.formModelOrNull)
        assertEquals(
            setOf(CorrespondenceEmailStep.ROUTE_SEGMENT, UpdateCorrespondenceEmailCyaStep.ROUTE_SEGMENT),
            routes.keys,
        )
        assertFalse(state.cyaStep.isStepReachable)
        assertEquals(PropertyDetailsController.getPropertyDetailsPath(1), state.correspondenceEmailStep.backUrl)
        assertUpdateContent()
    }

    @Test
    fun `subsequent requests preserve the original account snapshot`() {
        factory.createJourneySteps(1)
        whenever(userToLandlordService.getCurrentLandlordForUser())
            .thenReturn(MockLandlordData.createIndividualLandlord(email = "changed@example.com"))

        factory.createJourneySteps(1)

        assertEquals("account@example.com", state.loggedInLandlordEmailAtStartOfJourney)
        verify(userToLandlordService, times(1)).getCurrentLandlordForUser()
        verify(propertyOwnershipService, times(1)).getLastModifiedDate(1)
    }

    @Test
    fun `a different property cannot reuse the journey state`() {
        factory.createJourneySteps(1)

        assertThrows<PrsdbWebException> { factory.createJourneySteps(2) }
    }

    @Test
    fun `a CYA child exposes only the email question and returns back to CYA`() {
        factory.createJourneySteps(1)
        state.checkingAnswersFor = CorrespondenceEmailStep.ROUTE_SEGMENT
        state.cyaUrlPath = UpdateCorrespondenceEmailCyaStep.ROUTE_SEGMENT

        val routes = factory.createJourneySteps(1)

        assertEquals(setOf(CorrespondenceEmailStep.ROUTE_SEGMENT), routes.keys)
        assertEquals(state.returnToCyaPageDestination.toUrlStringOrNull(), state.correspondenceEmailStep.backUrl)
        assertUpdateContent()
    }

    private fun assertUpdateContent() {
        val content = state.correspondenceEmailStep.getPageVisitContent()
        assertEquals("forms.buttons.continue", content["submitButtonText"])
        assertEquals("propertyDetails.update.title", content["title"])
        assertEquals(
            SectionHeaderViewModel("registerProperty.taskList.aboutYourProperty.correspondence", 0, 0, false),
            content["sectionHeaderInfo"],
        )
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
