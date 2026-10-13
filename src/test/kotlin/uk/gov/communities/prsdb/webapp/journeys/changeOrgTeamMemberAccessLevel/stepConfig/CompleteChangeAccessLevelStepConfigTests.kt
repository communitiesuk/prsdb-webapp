package uk.gov.communities.prsdb.webapp.journeys.changeOrgTeamMemberAccessLevel.stepConfig

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes
import org.springframework.web.servlet.DispatcherServlet
import org.springframework.web.servlet.FlashMap
import org.springframework.web.servlet.support.RequestContextUtils
import uk.gov.communities.prsdb.webapp.constants.enums.OrganisationalLandlordUserRole
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.changeOrgTeamMemberAccessLevel.ChangeOrgTeamMemberAccessLevelJourneyState
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.services.OrganisationalLandlordUserService
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLandlordData

@ExtendWith(MockitoExtension::class)
class CompleteChangeAccessLevelStepConfigTests {
    @Mock
    private lateinit var mockOrganisationalLandlordUserService: OrganisationalLandlordUserService

    @Mock
    private lateinit var mockState: ChangeOrgTeamMemberAccessLevelJourneyState

    private lateinit var stepConfig: CompleteChangeAccessLevelStepConfig

    private lateinit var mockRequest: MockHttpServletRequest

    private val organisationalLandlordUserId = 42L

    @BeforeEach
    fun setUp() {
        stepConfig = CompleteChangeAccessLevelStepConfig(mockOrganisationalLandlordUserService)
        mockRequest = MockHttpServletRequest()
        RequestContextHolder.setRequestAttributes(ServletRequestAttributes(mockRequest))
        mockRequest.setAttribute(DispatcherServlet.OUTPUT_FLASH_MAP_ATTRIBUTE, FlashMap())
    }

    @AfterEach
    fun tearDown() {
        RequestContextHolder.resetRequestAttributes()
    }

    @Test
    fun `mode returns COMPLETE`() {
        assertEquals(Complete.COMPLETE, stepConfig.mode(mockState))
    }

    @Test
    fun `afterStepIsReached updates the member's role to the selected role`() {
        // Arrange
        whenever(mockState.organisationalLandlordUserId).thenReturn(organisationalLandlordUserId)
        whenever(mockState.selectedRole).thenReturn(OrganisationalLandlordUserRole.EDITOR)

        // Act
        stepConfig.afterStepIsReached(mockState)

        // Assert
        verify(mockOrganisationalLandlordUserService).updateRole(organisationalLandlordUserId, OrganisationalLandlordUserRole.EDITOR)
    }

    @Test
    fun `resolveNextDestination deletes the journey`() {
        // Arrange
        whenever(mockState.organisationalLandlordUserId).thenReturn(organisationalLandlordUserId)
        whenever(mockState.selectedRole).thenReturn(OrganisationalLandlordUserRole.EDITOR)
        whenever(mockOrganisationalLandlordUserService.getOrganisationalLandlordUserOrNull(organisationalLandlordUserId))
            .thenReturn(MockLandlordData.createOrganisationalLandlordUser(name = "Beth Admin"))

        // Act
        stepConfig.resolveNextDestination(mockState, Destination.ExternalUrl("redirect"))

        // Assert
        verify(mockState).deleteJourney()
    }

    @Test
    fun `resolveNextDestination adds the member name as a flash attribute`() {
        // Arrange
        whenever(mockState.organisationalLandlordUserId).thenReturn(organisationalLandlordUserId)
        whenever(mockState.selectedRole).thenReturn(OrganisationalLandlordUserRole.EDITOR)
        whenever(mockOrganisationalLandlordUserService.getOrganisationalLandlordUserOrNull(organisationalLandlordUserId))
            .thenReturn(MockLandlordData.createOrganisationalLandlordUser(name = "Beth Admin"))

        // Act
        val result = stepConfig.resolveNextDestination(mockState, Destination.ExternalUrl("redirect"))
        result.toModelAndView()

        // Assert
        val flashMap = RequestContextUtils.getOutputFlashMap(mockRequest)
        assertEquals("Beth Admin", flashMap["accessLevelChangedMemberName"])
    }

    @Test
    fun `resolveNextDestination adds the administrator content key when the new role is ADMIN`() {
        // Arrange
        whenever(mockState.organisationalLandlordUserId).thenReturn(organisationalLandlordUserId)
        whenever(mockState.selectedRole).thenReturn(OrganisationalLandlordUserRole.ADMIN)
        whenever(mockOrganisationalLandlordUserService.getOrganisationalLandlordUserOrNull(organisationalLandlordUserId))
            .thenReturn(MockLandlordData.createOrganisationalLandlordUser(name = "Carl Editor"))

        // Act
        val result = stepConfig.resolveNextDestination(mockState, Destination.ExternalUrl("redirect"))
        result.toModelAndView()

        // Assert
        val flashMap = RequestContextUtils.getOutputFlashMap(mockRequest)
        assertEquals("teamMembers.accessLevelChangedBanner.content.administrator", flashMap["accessLevelChangedBannerContentKey"])
    }

    @Test
    fun `resolveNextDestination adds the editor content key when the new role is EDITOR`() {
        // Arrange
        whenever(mockState.organisationalLandlordUserId).thenReturn(organisationalLandlordUserId)
        whenever(mockState.selectedRole).thenReturn(OrganisationalLandlordUserRole.EDITOR)
        whenever(mockOrganisationalLandlordUserService.getOrganisationalLandlordUserOrNull(organisationalLandlordUserId))
            .thenReturn(MockLandlordData.createOrganisationalLandlordUser(name = "Beth Admin"))

        // Act
        val result = stepConfig.resolveNextDestination(mockState, Destination.ExternalUrl("redirect"))
        result.toModelAndView()

        // Assert
        val flashMap = RequestContextUtils.getOutputFlashMap(mockRequest)
        assertEquals("teamMembers.accessLevelChangedBanner.content.editor", flashMap["accessLevelChangedBannerContentKey"])
    }
}
