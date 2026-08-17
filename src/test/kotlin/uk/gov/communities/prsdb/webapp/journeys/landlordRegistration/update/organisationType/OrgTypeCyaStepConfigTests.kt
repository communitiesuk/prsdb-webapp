package uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.update.organisationType

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.constants.enums.OrgType
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.LeadTrusteeDobStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.LeadTrusteeEmailStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.LeadTrusteeNameStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.LeadTrusteePhoneStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgTypeStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.tasks.LeadTrusteeTask
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.LookupAddressStep
import uk.gov.communities.prsdb.webapp.journeys.shared.tasks.TrusteeAddressTask
import uk.gov.communities.prsdb.webapp.models.dataModels.AddressDataModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.LeadTrusteeDobFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.LeadTrusteeEmailFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.LeadTrusteeNameFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.LeadTrusteePhoneFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.OrgTypeFormModel
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.SummaryCardViewModel
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.SummaryListRowViewModel
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockMessageSource
import java.time.LocalDate

@ExtendWith(MockitoExtension::class)
class OrgTypeCyaStepConfigTests {
    @Mock
    private lateinit var mockState: UpdateOrganisationTypeJourneyState

    @Mock
    private lateinit var mockOrgTypeStep: OrgTypeStep

    @Mock
    private lateinit var mockOrgTypeUpdateRoutingStep: OrgTypeUpdateRoutingStep

    @Mock
    private lateinit var mockOrgTypeFormModel: OrgTypeFormModel

    private val mockMessageSource = MockMessageSource()

    private lateinit var stepConfig: OrgTypeCyaStepConfig

    @BeforeEach
    fun setUp() {
        stepConfig = OrgTypeCyaStepConfig(mockMessageSource)
    }

    @Nested
    inner class GetStepSpecificContentTests {
        @BeforeEach
        fun setUpContentTests() {
            whenever(mockState.orgTypeStep).thenReturn(mockOrgTypeStep)
            whenever(mockState.orgTypeUpdateRoutingStep).thenReturn(mockOrgTypeUpdateRoutingStep)
            whenever(mockOrgTypeStep.formModel).thenReturn(mockOrgTypeFormModel)
        }

        @Test
        fun `summary list contains only org type row when trust is unchanged`() {
            whenever(mockOrgTypeFormModel.getSelectedOrgTypes()).thenReturn(listOf(OrgType.COMPANY))
            whenever(mockOrgTypeUpdateRoutingStep.outcome).thenReturn(OrgTypeUpdateRouteMode.TRUST_UNCHANGED)
            whenever(mockState.getCyaJourneyId(mockOrgTypeStep)).thenReturn("cya-journey-id")

            val content = stepConfig.getStepSpecificContent(mockState)

            @Suppress("UNCHECKED_CAST")
            val summaryList = content["summaryListData"] as List<SummaryListRowViewModel>
            assertEquals(1, summaryList.size)
            assertEquals("landlordDetails.org.organisationType", summaryList[0].fieldHeading)
        }

        @Test
        fun `summary list contains only org type row when removing trust`() {
            whenever(mockOrgTypeFormModel.getSelectedOrgTypes()).thenReturn(listOf(OrgType.COMPANY))
            whenever(mockOrgTypeUpdateRoutingStep.outcome).thenReturn(OrgTypeUpdateRouteMode.REMOVING_TRUST)
            whenever(mockState.getCyaJourneyId(mockOrgTypeStep)).thenReturn("cya-journey-id")

            val content = stepConfig.getStepSpecificContent(mockState)

            @Suppress("UNCHECKED_CAST")
            val summaryList = content["summaryListData"] as List<SummaryListRowViewModel>
            assertEquals(1, summaryList.size)
        }

        @Test
        fun `summary list contains org type and lead trustee card when adding trust`() {
            whenever(mockOrgTypeFormModel.getSelectedOrgTypes()).thenReturn(listOf(OrgType.COMPANY, OrgType.TRUST))
            whenever(mockOrgTypeUpdateRoutingStep.outcome).thenReturn(OrgTypeUpdateRouteMode.ADDING_TRUST)
            whenever(mockState.getCyaJourneyId(mockOrgTypeStep)).thenReturn("cya-journey-id")

            val mockLeadTrusteeTask = mockLeadTrusteeTask()
            whenever(mockState.leadTrusteeTask).thenReturn(mockLeadTrusteeTask)
            whenever(mockState.getCyaJourneyId(mockLeadTrusteeTask.leadTrusteeNameStep)).thenReturn("cya-trustee-id")
            whenever(mockState.getCyaJourneyId(mockLeadTrusteeTask.leadTrusteeDobStep)).thenReturn("cya-trustee-id")
            whenever(mockState.getCyaJourneyId(mockLeadTrusteeTask.leadTrusteeEmailStep)).thenReturn("cya-trustee-id")
            whenever(mockState.getCyaJourneyId(mockLeadTrusteeTask.leadTrusteePhoneStep)).thenReturn("cya-trustee-id")
            whenever(mockState.getCyaJourneyId(mockLeadTrusteeTask.trusteeAddressTask.lookupAddressStep)).thenReturn("cya-trustee-id")

            val content = stepConfig.getStepSpecificContent(mockState)

            @Suppress("UNCHECKED_CAST")
            val summaryList = content["summaryListData"] as List<SummaryListRowViewModel>
            assertEquals(1, summaryList.size)
            assertEquals("landlordDetails.org.organisationType", summaryList[0].fieldHeading)

            val leadTrusteeCard = content["leadTrusteeCard"] as SummaryCardViewModel
            assertEquals(5, leadTrusteeCard.summaryList.size)
            assertEquals("landlordDetails.org.leadTrusteeName", leadTrusteeCard.summaryList[0].fieldHeading)
            assertEquals("landlordDetails.org.leadTrusteeDateOfBirth", leadTrusteeCard.summaryList[1].fieldHeading)
            assertEquals("landlordDetails.org.leadTrusteeEmail", leadTrusteeCard.summaryList[2].fieldHeading)
            assertEquals("landlordDetails.org.leadTrusteePhone", leadTrusteeCard.summaryList[3].fieldHeading)
            assertEquals("landlordDetails.org.leadTrusteeAddress", leadTrusteeCard.summaryList[4].fieldHeading)
        }

        @Test
        fun `org type row displays comma-separated resolved org type names`() {
            whenever(mockOrgTypeFormModel.getSelectedOrgTypes()).thenReturn(listOf(OrgType.COMPANY, OrgType.CHARITY))
            whenever(mockOrgTypeUpdateRoutingStep.outcome).thenReturn(OrgTypeUpdateRouteMode.TRUST_UNCHANGED)
            whenever(mockState.getCyaJourneyId(mockOrgTypeStep)).thenReturn("cya-journey-id")

            val content = stepConfig.getStepSpecificContent(mockState)

            @Suppress("UNCHECKED_CAST")
            val summaryList = content["summaryListData"] as List<SummaryListRowViewModel>
            val orgTypeValue = summaryList[0].fieldValue as String
            assertEquals(
                "Message for registerAsALandlord.orgType.company, Message for registerAsALandlord.orgType.charity",
                orgTypeValue,
            )
        }
    }

    @Nested
    inner class ResolveNextDestinationTests {
        @Test
        fun `resolveNextDestination returns default destination without deleting journey`() {
            val defaultDestination = Destination.Nowhere()

            val result = stepConfig.resolveNextDestination(mockState, defaultDestination)

            assertEquals(defaultDestination, result)
            verify(mockState, never()).deleteJourney()
        }
    }

    private fun mockLeadTrusteeTask(): LeadTrusteeTask {
        val nameFormModel = mock<LeadTrusteeNameFormModel> { on { name } doReturn "Jane Doe" }
        val nameStep = mock<LeadTrusteeNameStep> { on { formModel } doReturn nameFormModel }

        val dobFormModel = mock<LeadTrusteeDobFormModel> { on { toLocalDateOrNull() } doReturn LocalDate.of(1990, 1, 1) }
        val dobStep = mock<LeadTrusteeDobStep> { on { formModel } doReturn dobFormModel }

        val emailFormModel = mock<LeadTrusteeEmailFormModel> { on { emailAddress } doReturn "jane@example.com" }
        val emailStep = mock<LeadTrusteeEmailStep> { on { formModel } doReturn emailFormModel }

        val phoneFormModel = mock<LeadTrusteePhoneFormModel> { on { phoneNumber } doReturn "07123456789" }
        val phoneStep = mock<LeadTrusteePhoneStep> { on { formModel } doReturn phoneFormModel }

        val address = mock<AddressDataModel> { on { toMultiLineAddress() } doReturn "1 Test Street\nLondon\nSW1A 1AA" }
        val lookupAddressStep = mock<LookupAddressStep>()
        val addressTask =
            mock<TrusteeAddressTask> {
                on { getAddress() } doReturn address
                on { this.lookupAddressStep } doReturn lookupAddressStep
            }

        return mock<LeadTrusteeTask> {
            on { leadTrusteeNameStep } doReturn nameStep
            on { leadTrusteeDobStep } doReturn dobStep
            on { leadTrusteeEmailStep } doReturn emailStep
            on { leadTrusteePhoneStep } doReturn phoneStep
            on { trusteeAddressTask } doReturn addressTask
        }
    }
}
