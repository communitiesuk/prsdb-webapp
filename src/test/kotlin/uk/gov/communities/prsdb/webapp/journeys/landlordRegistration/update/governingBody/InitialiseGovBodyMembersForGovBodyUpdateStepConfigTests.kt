package uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.update.governingBody

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito.verifyNoInteractions
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.constants.enums.GoverningBodyMemberType
import uk.gov.communities.prsdb.webapp.database.entity.OrganisationGoverningBodyMember
import uk.gov.communities.prsdb.webapp.database.entity.OrganisationalLandlord
import uk.gov.communities.prsdb.webapp.models.dataModels.GoverningBodyMemberDataModel
import uk.gov.communities.prsdb.webapp.services.UserToLandlordService
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.AlwaysTrueValidator
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLandlordData
import java.time.LocalDate

@ExtendWith(MockitoExtension::class)
class InitialiseGovBodyMembersForGovBodyUpdateStepConfigTests {
    @Mock
    lateinit var mockUserToLandlordService: UserToLandlordService

    @Mock
    lateinit var mockState: UpdateGoverningBodyJourneyState

    @Test
    fun `afterStepIsReached populates members from database when not yet initialised`() {
        val stepConfig = setupStepConfig()
        val address = MockLandlordData.createAddress()
        val mockOrgLandlord = mock<OrganisationalLandlord>()
        val member1 =
            OrganisationGoverningBodyMember(mockOrgLandlord, GoverningBodyMemberType.DIRECTOR, "Alice", LocalDate.of(1990, 1, 1), address)
        val member2 =
            OrganisationGoverningBodyMember(mockOrgLandlord, GoverningBodyMemberType.TRUSTEE, "Bob", LocalDate.of(1985, 6, 15), address)
        whenever(mockOrgLandlord.governingBodyMembers).thenReturn(listOf(member1, member2))
        whenever(mockState.governingBodyMembersInitialised).thenReturn(null)
        whenever(mockUserToLandlordService.getCurrentOrganisationLandlordForUser()).thenReturn(mockOrgLandlord)

        stepConfig.afterStepIsReached(mockState)

        verify(mockState).governingBodyMembersMap =
            mapOf(
                1 to GoverningBodyMemberDataModel.fromEntity(member1),
                2 to GoverningBodyMemberDataModel.fromEntity(member2),
            )
        verify(mockState).nextGoverningBodyMemberId = 3
        verify(mockState).governingBodyMembersInitialised = true
    }

    @Test
    fun `afterStepIsReached does not re-initialise when already initialised`() {
        val stepConfig = setupStepConfig()
        whenever(mockState.governingBodyMembersInitialised).thenReturn(true)

        stepConfig.afterStepIsReached(mockState)

        verifyNoInteractions(mockUserToLandlordService)
    }

    private fun setupStepConfig(): InitialiseGovBodyMembersForGovBodyUpdateStepConfig {
        val stepConfig = InitialiseGovBodyMembersForGovBodyUpdateStepConfig(mockUserToLandlordService)
        stepConfig.urlPath = InitialiseGovBodyMembersForGovBodyUpdateStep.ROUTE_SEGMENT
        stepConfig.validator = AlwaysTrueValidator()
        return stepConfig
    }
}
