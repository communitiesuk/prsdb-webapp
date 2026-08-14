package uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.update.governingBody

import kotlinx.datetime.LocalDate
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.constants.enums.GoverningBodyMemberType
import uk.gov.communities.prsdb.webapp.exceptions.PrsdbWebException
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.tasks.OrgGovBodyMembersTask
import uk.gov.communities.prsdb.webapp.models.dataModels.AddressDataModel
import uk.gov.communities.prsdb.webapp.models.dataModels.GoverningBodyMemberDataModel
import uk.gov.communities.prsdb.webapp.services.LandlordService

@ExtendWith(MockitoExtension::class)
class CompleteGoverningBodyUpdateStepConfigTests {
    @Mock
    private lateinit var mockLandlordService: LandlordService

    @Mock
    private lateinit var mockState: UpdateGoverningBodyJourneyState

    @Mock
    private lateinit var mockTask: OrgGovBodyMembersTask

    private fun setupStepConfig() = CompleteGoverningBodyUpdateStepConfig(mockLandlordService)

    @Test
    fun `afterStepIsReached passes sorted member list to landlord service`() {
        val stepConfig = setupStepConfig()
        val firstMember =
            GoverningBodyMemberDataModel(
                name = "First Member",
                type = GoverningBodyMemberType.DIRECTOR,
                dateOfBirth = LocalDate(2001, 1, 1),
                address = AddressDataModel("1 Example Street"),
            )
        val secondMember =
            GoverningBodyMemberDataModel(
                name = "Second Member",
                type = GoverningBodyMemberType.TRUSTEE,
                dateOfBirth = LocalDate(2002, 2, 2),
                address = AddressDataModel("2 Example Street"),
            )
        whenever(mockState.orgGovBodyMembersTask).thenReturn(mockTask)
        whenever(mockTask.governingBodyMembersMap).thenReturn(
            mapOf(
                2 to secondMember,
                1 to firstMember,
            ),
        )

        stepConfig.afterStepIsReached(mockState)

        val membersCaptor = argumentCaptor<List<GoverningBodyMemberDataModel>>()
        verify(mockLandlordService).updateOrganisationLandlordGoverningBodyMembers(membersCaptor.capture())
        assertEquals(listOf(firstMember, secondMember), membersCaptor.firstValue)
    }

    @Test
    fun `afterStepIsReached throws when member state is missing`() {
        val stepConfig = setupStepConfig()
        whenever(mockState.orgGovBodyMembersTask).thenReturn(mockTask)
        whenever(mockTask.governingBodyMembersMap).thenReturn(null)

        assertThrows<PrsdbWebException> {
            stepConfig.afterStepIsReached(mockState)
        }
    }

    @Test
    fun `resolveNextDestination deletes journey and returns default destination`() {
        val stepConfig = setupStepConfig()
        val defaultDestination = Destination.ExternalUrl("redirect")

        val result = stepConfig.resolveNextDestination(mockState, defaultDestination)

        verify(mockState).deleteJourney()
        assertEquals(defaultDestination, result)
    }
}
