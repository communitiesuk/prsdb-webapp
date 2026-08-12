package uk.gov.communities.prsdb.webapp.journeys.organisationalLandlordDeregistration.stepConfig

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.test.util.ReflectionTestUtils
import uk.gov.communities.prsdb.webapp.journeys.organisationalLandlordDeregistration.OrganisationalLandlordDeregistrationJourneyState
import uk.gov.communities.prsdb.webapp.services.LandlordDeregistrationService
import uk.gov.communities.prsdb.webapp.services.SecurityContextService
import uk.gov.communities.prsdb.webapp.services.SwapToIndividualNudgeEmailService
import uk.gov.communities.prsdb.webapp.services.UserToLandlordService
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLandlordData

@ExtendWith(MockitoExtension::class)
class DeregisterStepConfigTests {
    @Mock
    lateinit var mockLandlordDeregistrationService: LandlordDeregistrationService

    @Mock
    lateinit var mockUserToLandlordService: UserToLandlordService

    @Mock
    lateinit var mockSecurityContextService: SecurityContextService

    @Mock
    lateinit var mockSwapToIndividualNudgeEmailService: SwapToIndividualNudgeEmailService

    @Mock
    lateinit var mockState: OrganisationalLandlordDeregistrationJourneyState

    @Test
    fun `afterStepIsReached deregisters the organisational landlord`() {
        val stepConfig = setupStepConfig()
        val orgLandlord = setupMocks()

        stepConfig.afterStepIsReached(mockState)

        verify(mockLandlordDeregistrationService).deregisterOrganisationalLandlord(orgLandlord)
    }

    @Test
    fun `afterStepIsReached refreshes security context`() {
        val stepConfig = setupStepConfig()
        setupMocks()

        stepConfig.afterStepIsReached(mockState)

        verify(mockSecurityContextService).refreshContext()
    }

    @Test
    fun `afterStepIsReached calls nudge email service for jointly owned properties`() {
        val stepConfig = setupStepConfig()
        val orgLandlord = MockLandlordData.createOrgLandlord()
        ReflectionTestUtils.setField(orgLandlord, "id", 1L)
        val coLandlord = MockLandlordData.createIndividualLandlord()
        ReflectionTestUtils.setField(coLandlord, "id", 2L)
        val jointProperty = MockLandlordData.createPropertyOwnership(landlords = mutableSetOf(orgLandlord, coLandlord))
        whenever(mockUserToLandlordService.getCurrentOrganisationLandlordForUser()).thenReturn(orgLandlord)

        stepConfig.afterStepIsReached(mockState)

        verify(mockSwapToIndividualNudgeEmailService).sendNudgeEmailIfApplicable(jointProperty)
    }

    private fun setupMocks() =
        MockLandlordData.createOrgLandlord().also { orgLandlord ->
            whenever(mockUserToLandlordService.getCurrentOrganisationLandlordForUser()).thenReturn(orgLandlord)
        }

    private fun setupStepConfig() =
        DeregisterStepConfig(
            mockLandlordDeregistrationService,
            mockUserToLandlordService,
            mockSecurityContextService,
            mockSwapToIndividualNudgeEmailService,
        )
}
