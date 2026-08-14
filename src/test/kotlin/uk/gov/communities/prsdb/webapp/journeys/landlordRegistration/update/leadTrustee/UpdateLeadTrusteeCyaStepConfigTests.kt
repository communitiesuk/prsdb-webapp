package uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.update.leadTrustee

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.LeadTrusteeDobStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.LeadTrusteeEmailStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.LeadTrusteeNameStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.LeadTrusteePhoneStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.tasks.LeadTrusteeTask
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.LookupAddressStep
import uk.gov.communities.prsdb.webapp.journeys.shared.tasks.TrusteeAddressTask
import uk.gov.communities.prsdb.webapp.models.dataModels.AddressDataModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.LeadTrusteeDobFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.LeadTrusteeEmailFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.LeadTrusteeNameFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.LeadTrusteePhoneFormModel
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.SummaryCardViewModel
import java.time.LocalDate

@ExtendWith(MockitoExtension::class)
class UpdateLeadTrusteeCyaStepConfigTests {
    @Mock
    private lateinit var mockState: UpdateLeadTrusteeJourneyState

    private lateinit var stepConfig: UpdateLeadTrusteeCyaStepConfig

    @BeforeEach
    fun setUp() {
        stepConfig = UpdateLeadTrusteeCyaStepConfig()
    }

    @Nested
    inner class GetStepSpecificContentTests {
        @BeforeEach
        fun setUpContentTests() {
            val mockLeadTrusteeTask = mockLeadTrusteeTask()
            whenever(mockState.leadTrusteeTask).thenReturn(mockLeadTrusteeTask)
            whenever(mockState.getCyaJourneyId(mockLeadTrusteeTask.leadTrusteeNameStep)).thenReturn("cya-id")
            whenever(mockState.getCyaJourneyId(mockLeadTrusteeTask.leadTrusteeDobStep)).thenReturn("cya-id")
            whenever(mockState.getCyaJourneyId(mockLeadTrusteeTask.leadTrusteeEmailStep)).thenReturn("cya-id")
            whenever(mockState.getCyaJourneyId(mockLeadTrusteeTask.leadTrusteePhoneStep)).thenReturn("cya-id")
            whenever(mockState.getCyaJourneyId(mockLeadTrusteeTask.trusteeAddressTask.lookupAddressStep)).thenReturn("cya-id")
        }

        @Test
        fun `content includes lead trustee card with all five rows`() {
            val content = stepConfig.getStepSpecificContent(mockState)

            val leadTrusteeCard = content["leadTrusteeCard"] as SummaryCardViewModel
            assertEquals(5, leadTrusteeCard.summaryList.size)
            assertEquals("landlordDetails.org.leadTrusteeName", leadTrusteeCard.summaryList[0].fieldHeading)
            assertEquals("landlordDetails.org.leadTrusteeDateOfBirth", leadTrusteeCard.summaryList[1].fieldHeading)
            assertEquals("landlordDetails.org.leadTrusteeEmail", leadTrusteeCard.summaryList[2].fieldHeading)
            assertEquals("landlordDetails.org.leadTrusteePhone", leadTrusteeCard.summaryList[3].fieldHeading)
            assertEquals("landlordDetails.org.leadTrusteeAddress", leadTrusteeCard.summaryList[4].fieldHeading)
        }

        @Test
        fun `content includes correct field values`() {
            val content = stepConfig.getStepSpecificContent(mockState)

            val leadTrusteeCard = content["leadTrusteeCard"] as SummaryCardViewModel
            assertEquals("Jane Doe", leadTrusteeCard.summaryList[0].fieldValue)
            assertEquals(LocalDate.of(1990, 1, 1), leadTrusteeCard.summaryList[1].fieldValue)
            assertEquals("jane@example.com", leadTrusteeCard.summaryList[2].fieldValue)
            assertEquals("07123456789", leadTrusteeCard.summaryList[3].fieldValue)
            assertEquals(listOf("1 Test Street", "London", "SW1A 1AA"), leadTrusteeCard.summaryList[4].fieldValue)
        }

        @Test
        fun `content includes title, warning, and submit button text`() {
            val content = stepConfig.getStepSpecificContent(mockState)

            assertEquals("landlordDetails.update.title", content["title"])
            assertEquals(true, content["showWarning"])
            assertEquals("forms.buttons.confirmAndSubmitUpdate", content["submitButtonText"])
        }

        @Test
        fun `lead trustee card has correct heading`() {
            val content = stepConfig.getStepSpecificContent(mockState)

            val leadTrusteeCard = content["leadTrusteeCard"] as SummaryCardViewModel
            assertEquals("landlordDetails.org.leadTrusteeHeading", leadTrusteeCard.title)
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
