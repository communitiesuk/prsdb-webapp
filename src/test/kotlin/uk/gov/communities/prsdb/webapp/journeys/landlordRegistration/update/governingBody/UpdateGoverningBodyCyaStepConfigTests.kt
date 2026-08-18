package uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.update.governingBody

import kotlinx.datetime.LocalDate
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito.lenient
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.constants.enums.GoverningBodyMemberType
import uk.gov.communities.prsdb.webapp.exceptions.PrsdbWebException
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgGovBodyMemberListStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.tasks.OrgGovBodyMembersTask
import uk.gov.communities.prsdb.webapp.journeys.shared.helpers.OrgCompaniesHouseDetailsHelper
import uk.gov.communities.prsdb.webapp.models.dataModels.AddressDataModel
import uk.gov.communities.prsdb.webapp.models.dataModels.GoverningBodyMemberDataModel
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.SummaryCardViewModel
import uk.gov.communities.prsdb.webapp.services.LandlordService

@ExtendWith(MockitoExtension::class)
class UpdateGoverningBodyCyaStepConfigTests {
    @Mock
    private lateinit var mockLandlordService: LandlordService

    @Mock
    private lateinit var mockState: UpdateGoverningBodyJourneyState

    @Mock
    private lateinit var mockTask: OrgGovBodyMembersTask

    @Mock
    private lateinit var mockMemberListStep: OrgGovBodyMemberListStep

    private lateinit var stepConfig: UpdateGoverningBodyCyaStepConfig

    private val firstMember = member("First Member", GoverningBodyMemberType.DIRECTOR)
    private val secondMember = member("Second Member", GoverningBodyMemberType.PARTNER)

    private val members =
        mapOf(
            2 to secondMember,
            1 to firstMember,
        )

    @BeforeEach
    fun setUp() {
        stepConfig = UpdateGoverningBodyCyaStepConfig(mockLandlordService, OrgCompaniesHouseDetailsHelper())

        whenever(mockState.orgGovBodyMembersTask).thenReturn(mockTask)
        lenient().`when`(mockTask.orgGovBodyMemberListStep).thenReturn(mockMemberListStep)
        lenient().`when`(mockState.getCyaJourneyId(any())).thenReturn("cya-journey-id")
        lenient().`when`(mockMemberListStep.urlPath).thenReturn("organisation-governing-body-member-list")
    }

    @Test
    fun `afterStepDataIsAdded passes sorted member list to landlord service`() {
        whenever(mockTask.governingBodyMembersMap).thenReturn(members)

        stepConfig.afterStepDataIsAdded(mockState)

        val membersCaptor = argumentCaptor<List<GoverningBodyMemberDataModel>>()
        verify(mockLandlordService).updateOrganisationLandlordGoverningBodyMembers(membersCaptor.capture())
        assertEquals(listOf(firstMember, secondMember), membersCaptor.firstValue)
    }

    @Test
    fun `afterStepDataIsAdded throws when member state is missing`() {
        whenever(mockTask.governingBodyMembersMap).thenReturn(null)

        assertThrows<PrsdbWebException> {
            stepConfig.afterStepDataIsAdded(mockState)
        }
    }

    @Test
    fun `getStepSpecificContent renders member cards with correct title, warning, and submit button`() {
        whenever(mockTask.governingBodyMembersMap).thenReturn(members)

        val content = stepConfig.getStepSpecificContent(mockState)

        assertEquals("landlordDetails.update.title", content["title"])
        assertEquals(true, content["showWarning"])
        assertEquals("forms.buttons.confirmAndSubmitUpdate", content["submitButtonText"])
    }

    @Test
    fun `getStepSpecificContent renders one card per member in sorted order`() {
        whenever(mockTask.governingBodyMembersMap).thenReturn(members)

        val content = stepConfig.getStepSpecificContent(mockState)

        val cards = content["governingBodyMemberCards"] as List<*>
        assertEquals(2, cards.size)
        assertEquals("1", (cards.first() as SummaryCardViewModel).cardNumber)
        assertEquals("2", (cards.last() as SummaryCardViewModel).cardNumber)
        assertEquals(
            "landlordDetails.org.governingBody.memberCardTitle.director",
            (cards.first() as SummaryCardViewModel).title,
        )
        assertEquals(
            "landlordDetails.org.governingBody.memberCardTitle.partner",
            (cards.last() as SummaryCardViewModel).title,
        )
    }

    @Test
    fun `getStepSpecificContent returns empty cards when no members`() {
        whenever(mockTask.governingBodyMembersMap).thenReturn(null)

        val content = stepConfig.getStepSpecificContent(mockState)

        assertTrue((content["governingBodyMemberCards"] as List<*>).isEmpty())
    }

    private fun member(
        name: String,
        type: GoverningBodyMemberType,
    ) = GoverningBodyMemberDataModel(
        name = name,
        type = type,
        dateOfBirth = LocalDate(1980, 1, 1),
        address = AddressDataModel(singleLineAddress = "1 Test Street, London, SW1A 1AA"),
    )
}
