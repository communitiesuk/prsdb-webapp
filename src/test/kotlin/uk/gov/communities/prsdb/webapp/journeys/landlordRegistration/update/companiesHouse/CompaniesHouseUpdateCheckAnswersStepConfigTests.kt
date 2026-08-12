package uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.update.companiesHouse

import kotlinx.datetime.LocalDate
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito.lenient
import org.mockito.Mockito.verifyNoInteractions
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.constants.enums.GoverningBodyMemberType
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgCompanyNumberStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgGovBodyMemberListStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgIsRegisteredCompanyStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.tasks.OrgGovBodyMembersTask
import uk.gov.communities.prsdb.webapp.models.dataModels.AddressDataModel
import uk.gov.communities.prsdb.webapp.models.dataModels.GoverningBodyMemberDataModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.OrgCompanyNumberFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.OrgIsRegisteredCompanyFormModel
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.SummaryCardViewModel
import uk.gov.communities.prsdb.webapp.services.LandlordService

@ExtendWith(MockitoExtension::class)
class CompaniesHouseUpdateCheckAnswersStepConfigTests {
    @Mock
    private lateinit var mockLandlordService: LandlordService

    @Mock
    private lateinit var mockState: UpdateCompaniesHouseJourneyState

    @Mock
    private lateinit var mockTask: UpdateCompaniesHouseTask

    @Mock
    private lateinit var mockIsRegisteredCompanyStep: OrgIsRegisteredCompanyStep

    @Mock
    private lateinit var mockCompanyNumberStep: OrgCompanyNumberStep

    @Mock
    private lateinit var mockGovBodyMembersTask: OrgGovBodyMembersTask

    @Mock
    private lateinit var mockMemberListStep: OrgGovBodyMemberListStep

    @Mock
    private lateinit var mockIsRegisteredCompanyFormModel: OrgIsRegisteredCompanyFormModel

    @Mock
    private lateinit var mockCompanyNumberFormModel: OrgCompanyNumberFormModel

    private lateinit var stepConfig: CompaniesHouseUpdateCheckAnswersStepConfig

    private val members =
        mapOf(
            2 to member("Second Member", GoverningBodyMemberType.PARTNER),
            1 to member("First Member", GoverningBodyMemberType.DIRECTOR),
        )

    @BeforeEach
    fun setUp() {
        stepConfig = CompaniesHouseUpdateCheckAnswersStepConfig(mockLandlordService)

        whenever(mockState.updateCompaniesHouseTask).thenReturn(mockTask)
        lenient().`when`(mockTask.orgIsRegisteredCompanyStep).thenReturn(mockIsRegisteredCompanyStep)
        lenient().`when`(mockIsRegisteredCompanyStep.formModel).thenReturn(mockIsRegisteredCompanyFormModel)
        lenient().`when`(mockTask.orgCompanyNumberStep).thenReturn(mockCompanyNumberStep)
        lenient().`when`(mockCompanyNumberStep.formModel).thenReturn(mockCompanyNumberFormModel)
        lenient().`when`(mockTask.orgGovBodyMembersTask).thenReturn(mockGovBodyMembersTask)
        lenient().`when`(mockGovBodyMembersTask.orgGovBodyMemberListStep).thenReturn(mockMemberListStep)
        lenient().`when`(mockState.getCyaJourneyId(any())).thenReturn("cya-journey-id")
        lenient().`when`(mockIsRegisteredCompanyStep.urlPath).thenReturn("organisation-companies-house")
        lenient().`when`(mockCompanyNumberStep.urlPath).thenReturn("organisation-company-number")
        lenient().`when`(mockMemberListStep.urlPath).thenReturn("organisation-governing-body-member-list")
    }

    @Test
    fun `afterStepDataIsAdded persists the company number when registered with Companies House`() {
        whenever(mockIsRegisteredCompanyFormModel.companiesHouse).thenReturn(true)
        whenever(mockCompanyNumberFormModel.companyNumber).thenReturn("12345678")

        stepConfig.afterStepDataIsAdded(mockState)

        verify(mockLandlordService).updateOrganisationalLandlordToRegisteredCompany("12345678")
        verifyNoInteractions(mockGovBodyMembersTask)
    }

    @Test
    fun `afterStepDataIsAdded persists the governing body members when not registered with Companies House`() {
        whenever(mockIsRegisteredCompanyFormModel.companiesHouse).thenReturn(false)
        whenever(mockTask.governingBodyMembersMap).thenReturn(members)

        stepConfig.afterStepDataIsAdded(mockState)

        verify(mockLandlordService).updateOrganisationalLandlordToNonRegisteredCompany(members.values.toList())
    }

    @Test
    fun `getStepSpecificContent renders the company variant with the two company detail rows`() {
        whenever(mockIsRegisteredCompanyFormModel.companiesHouse).thenReturn(true)
        whenever(mockCompanyNumberFormModel.companyNumber).thenReturn("12345678")

        val content = stepConfig.getStepSpecificContent(mockState)

        assertEquals(true, content["companyVariant"])
        assertEquals(2, (content["summaryListData"] as List<*>).size)
        assertTrue((content["governingBodyMemberCards"] as List<*>).isEmpty())
    }

    @Test
    fun `getStepSpecificContent renders the non-company variant with a card per governing body member`() {
        whenever(mockIsRegisteredCompanyFormModel.companiesHouse).thenReturn(false)
        whenever(mockTask.governingBodyMembersMap).thenReturn(members)

        val content = stepConfig.getStepSpecificContent(mockState)

        assertEquals(false, content["companyVariant"])
        assertEquals(1, (content["summaryListData"] as List<*>).size)
        val cards = content["governingBodyMemberCards"] as List<*>
        assertEquals(2, cards.size)
        assertEquals("1", (cards.first() as SummaryCardViewModel).cardNumber)
    }

    @Test
    fun `getStepSpecificContent omits the company number row when not registered with Companies House`() {
        whenever(mockIsRegisteredCompanyFormModel.companiesHouse).thenReturn(false)

        val content = stepConfig.getStepSpecificContent(mockState)

        assertFalse((content["summaryListData"] as List<*>).isEmpty())
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
