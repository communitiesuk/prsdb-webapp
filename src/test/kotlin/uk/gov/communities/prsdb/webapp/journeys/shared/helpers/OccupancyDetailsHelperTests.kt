package uk.gov.communities.prsdb.webapp.journeys.shared.helpers

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito.lenient
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever
import org.springframework.context.MessageSource
import uk.gov.communities.prsdb.webapp.constants.enums.FurnishedStatus
import uk.gov.communities.prsdb.webapp.constants.enums.RentFrequency
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.PropertyRegistrationJourneyState
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.HouseholdsAndTenantsState
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.TenancyDetailsState
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.BillsIncludedStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.FurnishedStatusStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.HouseholdMode
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.HouseholdStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.OccupiedStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.RentAmountStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.RentFrequencyStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.RentIncludesBillsStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.TenantsStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.tasks.HouseholdsAndTenantsTask
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.tasks.RentFrequencyAndAmountTask
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.tasks.RentIncludesBillsTask
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.update.tenancyDetails.UpdateTenancyDetailsJourneyState
import uk.gov.communities.prsdb.webapp.journeys.shared.states.CheckYourAnswersJourneyState
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.FurnishedStatusFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NewNumberOfPeopleFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NumberOfHouseholdsFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.OccupancyFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.RentAmountFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.RentFrequencyFormModel

@ExtendWith(MockitoExtension::class)
class OccupancyDetailsHelperTests {
    private val helper = OccupancyDetailsHelper()

    @Mock
    private lateinit var mockMessageSource: MessageSource

    @Mock
    private lateinit var mockOccupationState: PropertyRegistrationJourneyState

    @Mock
    private lateinit var mockTenancyState: UpdateTenancyDetailsJourneyState

    @Mock
    private lateinit var mockHouseholdsAndTenantsTask: HouseholdsAndTenantsTask

    @Mock
    private lateinit var mockHouseholdsAndTenantsState: HouseholdsAndTenantsState

    @Mock
    private lateinit var mockRentIncludesBillsTask: RentIncludesBillsTask

    @Mock
    private lateinit var mockRentFrequencyAndAmountTask: RentFrequencyAndAmountTask

    @Mock
    private lateinit var mockOccupiedStep: OccupiedStep

    @Mock
    private lateinit var mockHouseholdStep: HouseholdStep

    @Mock
    private lateinit var mockTenantsStep: TenantsStep

    @Mock
    private lateinit var mockRentIncludesBillsStep: RentIncludesBillsStep

    @Mock
    private lateinit var mockFurnishedStatusStep: FurnishedStatusStep

    @Mock
    private lateinit var mockRentFrequencyStep: RentFrequencyStep

    @Mock
    private lateinit var mockRentAmountStep: RentAmountStep

    @Mock
    private lateinit var mockCyaState: CheckYourAnswersJourneyState

    @Mock
    private lateinit var mockTenancyDetailsState: TenancyDetailsState

    @Mock
    private lateinit var mockBillsIncludedStep: BillsIncludedStep

    @BeforeEach
    fun setUp() {
        lenient().`when`(mockOccupationState.householdsAndTenantsTask).thenReturn(mockHouseholdsAndTenantsTask)
        lenient().`when`(mockOccupationState.rentIncludesBillsTask).thenReturn(mockRentIncludesBillsTask)
        lenient().`when`(mockOccupationState.rentFrequencyAndAmountTask).thenReturn(mockRentFrequencyAndAmountTask)
        lenient().`when`(mockTenancyState.householdsAndTenantsTask).thenReturn(mockHouseholdsAndTenantsTask)
        lenient().`when`(mockTenancyState.rentIncludesBillsTask).thenReturn(mockRentIncludesBillsTask)
        lenient().`when`(mockTenancyState.rentFrequencyAndAmountTask).thenReturn(mockRentFrequencyAndAmountTask)
        lenient().`when`(mockHouseholdsAndTenantsTask.households).thenReturn(mockHouseholdStep)
        lenient().`when`(mockHouseholdsAndTenantsTask.tenants).thenReturn(mockTenantsStep)
    }

    @Test
    fun `getCheckYourAnswersSummaryList returns no tenancy rows when property is unoccupied`() {
        whenever(mockOccupationState.occupied).thenReturn(mockOccupiedStep)
        whenever(mockOccupiedStep.formModel).thenReturn(OccupancyFormModel().apply { occupied = false })

        val rows = helper.getCheckYourAnswersSummaryList(mockOccupationState, mockMessageSource)

        assertEquals(0, rows.size)
    }

    @Test
    fun `getOccupancySummaryList returns the occupied row for unoccupied properties`() {
        whenever(mockOccupationState.occupied).thenReturn(mockOccupiedStep)
        whenever(mockOccupiedStep.formModel).thenReturn(OccupancyFormModel().apply { occupied = false })
        whenever(mockOccupationState.getCyaJourneyId(mockOccupiedStep)).thenReturn("occupied-cya")

        val rows = helper.getOccupancySummaryList(mockOccupationState)

        assertEquals(1, rows.size)
        assertEquals("forms.checkPropertyAnswers.occupancy.question", rows[0].fieldHeading)
        assertEquals(false, rows[0].fieldValue)
    }

    @Test
    fun `getOccupancySummaryList returns the occupied row for occupied properties`() {
        whenever(mockOccupationState.occupied).thenReturn(mockOccupiedStep)
        whenever(mockOccupiedStep.formModel).thenReturn(OccupancyFormModel().apply { occupied = true })
        whenever(mockOccupationState.getCyaJourneyId(mockOccupiedStep)).thenReturn("occupied-cya")

        val rows = helper.getOccupancySummaryList(mockOccupationState)

        assertEquals(1, rows.size)
        assertEquals("forms.checkPropertyAnswers.occupancy.question", rows[0].fieldHeading)
        assertEquals(true, rows[0].fieldValue)
    }

    @Test
    fun `getCheckYourAnswersSummaryList includes tenancy rows when property is occupied`() {
        whenever(mockOccupationState.occupied).thenReturn(mockOccupiedStep)
        whenever(mockOccupiedStep.formModel).thenReturn(OccupancyFormModel().apply { occupied = true })
        whenever(mockOccupationState.householdsAndTenantsTask).thenReturn(mockHouseholdsAndTenantsTask)
        whenever(mockHouseholdStep.outcome).thenReturn(HouseholdMode.COMPLETE)
        whenever(mockHouseholdStep.formModel).thenReturn(NumberOfHouseholdsFormModel().apply { numberOfHouseholds = "2" })
        whenever(mockTenantsStep.formModel).thenReturn(NewNumberOfPeopleFormModel().apply { numberOfPeople = "5" })
        whenever(mockOccupationState.getCyaJourneyId(mockHouseholdStep)).thenReturn("households-cya")
        whenever(mockOccupationState.getCyaJourneyId(mockTenantsStep)).thenReturn("tenants-cya")
        whenever(mockOccupationState.furnishedStatus).thenReturn(mockFurnishedStatusStep)
        whenever(mockFurnishedStatusStep.formModel).thenReturn(
            FurnishedStatusFormModel().apply { furnishedStatus = FurnishedStatus.FURNISHED },
        )
        whenever(mockOccupationState.getCyaJourneyId(mockFurnishedStatusStep)).thenReturn("furnished-cya")
        whenever(mockOccupationState.rentIncludesBillsTask).thenReturn(mockRentIncludesBillsTask)
        whenever(mockRentIncludesBillsTask.rentIncludesBills).thenReturn(mockRentIncludesBillsStep)
        whenever(mockOccupationState.getCyaJourneyId(mockRentIncludesBillsStep)).thenReturn("rent-bills-cya")
        whenever(mockOccupationState.rentFrequencyAndAmountTask).thenReturn(mockRentFrequencyAndAmountTask)
        whenever(mockRentFrequencyAndAmountTask.rentFrequency).thenReturn(mockRentFrequencyStep)
        whenever(mockRentFrequencyStep.formModel).thenReturn(RentFrequencyFormModel().apply { rentFrequency = RentFrequency.MONTHLY })
        whenever(mockOccupationState.getCyaJourneyId(mockRentFrequencyStep)).thenReturn("frequency-cya")
        whenever(mockRentFrequencyAndAmountTask.rentAmount).thenReturn(mockRentAmountStep)
        lenient().`when`(mockRentAmountStep.formModel).thenReturn(
            RentAmountFormModel().apply { rentAmount = "500" },
        )
        whenever(mockOccupationState.getCyaJourneyId(mockRentAmountStep)).thenReturn("amount-cya")

        val rows = helper.getCheckYourAnswersSummaryList(mockOccupationState, mockMessageSource)

        assertEquals(6, rows.size)
        assertEquals("tenancyDetails.checkAnswers.households", rows[0].fieldHeading)
        assertEquals("tenancyDetails.checkAnswers.people", rows[1].fieldHeading)
        assertEquals("tenancyDetails.checkAnswers.furnishedStatus", rows[2].fieldHeading)
        assertEquals("tenancyDetails.checkAnswers.rentIncludesBills", rows[3].fieldHeading)
        assertEquals("tenancyDetails.checkAnswers.rentFrequency", rows[4].fieldHeading)
        assertEquals("tenancyDetails.checkAnswers.rentAmount", rows[5].fieldHeading)
    }

    @Test
    fun `getCheckYourAnswersSummaryList uses provide later tenancy row when households are deferred`() {
        whenever(mockOccupationState.occupied).thenReturn(mockOccupiedStep)
        whenever(mockOccupiedStep.formModel).thenReturn(OccupancyFormModel().apply { occupied = true })
        whenever(mockOccupationState.householdsAndTenantsTask).thenReturn(mockHouseholdsAndTenantsTask)
        whenever(mockHouseholdStep.outcome).thenReturn(HouseholdMode.PROVIDE_THIS_LATER)
        whenever(mockOccupationState.getCyaJourneyId(mockHouseholdStep)).thenReturn("households-cya")

        val rows = helper.getCheckYourAnswersSummaryList(mockOccupationState, mockMessageSource)

        assertEquals(1, rows.size)
        assertEquals("tenancyDetails.checkAnswers.tenancyDetailsRow", rows[0].fieldHeading)
        assertEquals("tenancyDetails.checkAnswers.provideLater", rows[0].fieldValue)
    }

    @Test
    fun `getCheckYourTenancyDetailsAnswersSummaryList omits rent rows when provide later is true`() {
        whenever(mockTenancyState.provideTenancyDetailsLater).thenReturn(true)
        whenever(mockHouseholdStep.outcome).thenReturn(HouseholdMode.PROVIDE_THIS_LATER)
        whenever(mockTenancyState.getCyaJourneyId(mockHouseholdStep)).thenReturn("households-cya")

        val rows = helper.getCheckYourTenancyDetailsAnswersSummaryList(mockTenancyState, mockMessageSource)

        assertEquals(1, rows.size)
        assertEquals("tenancyDetails.checkAnswers.tenancyDetailsRow", rows[0].fieldHeading)
    }

    @Test
    fun `getCheckYourHouseHoldsAndTenantsAnswersSummaryList uses provided later destination when supplied`() {
        whenever(mockHouseholdsAndTenantsState.households).thenReturn(mockHouseholdStep)
        whenever(mockHouseholdStep.outcome).thenReturn(HouseholdMode.PROVIDE_THIS_LATER)

        val rows =
            helper.getCheckYourHouseHoldsAndTenantsAnswersSummaryList(
                mockTenancyState,
                mockHouseholdsAndTenantsState,
                Destination.StepRoute("custom-route", "journey-123"),
            )

        assertEquals(1, rows.size)
        assertEquals("tenancyDetails.checkAnswers.tenancyDetailsRow", rows[0].fieldHeading)
        assertEquals("tenancyDetails.checkAnswers.provideLater", rows[0].fieldValue)
        assertEquals(true, rows[0].actions[0].url.contains("journeyId=journey-123"))
    }

    @Test
    fun `getTenancyDetailsSummaryList returns household, people, furnishing and rent rows in order`() {
        stubTenancyDetailsState()

        val rows = helper.getTenancyDetailsSummaryList(mockCyaState, mockTenancyDetailsState, mockMessageSource)

        assertEquals(6, rows.size)
        assertEquals("tenancyDetails.checkAnswers.households", rows[0].fieldHeading)
        assertEquals("tenancyDetails.checkAnswers.people", rows[1].fieldHeading)
        assertEquals("tenancyDetails.checkAnswers.furnishedStatus", rows[2].fieldHeading)
        assertEquals("tenancyDetails.checkAnswers.rentIncludesBills", rows[3].fieldHeading)
        assertEquals("tenancyDetails.checkAnswers.rentFrequency", rows[4].fieldHeading)
        assertEquals("tenancyDetails.checkAnswers.rentAmount", rows[5].fieldHeading)
    }

    @Test
    fun `getTenancyDetailsSummaryList includes the bills included row when rent includes bills`() {
        stubTenancyDetailsState()
        whenever(mockRentIncludesBillsTask.doesRentIncludeBills()).thenReturn(true)
        // Lenient because Mockito's strict stubs treat getBillsIncluded(MessageSource) and the billsIncluded getter as one method
        lenient().`when`(mockRentIncludesBillsTask.getBillsIncluded(mockMessageSource)).thenReturn("Gas")
        whenever(mockRentIncludesBillsTask.billsIncluded).thenReturn(mockBillsIncludedStep)
        whenever(mockCyaState.getCyaJourneyId(mockBillsIncludedStep)).thenReturn("bills-included-cya")

        val rows = helper.getTenancyDetailsSummaryList(mockCyaState, mockTenancyDetailsState, mockMessageSource)

        assertEquals(7, rows.size)
        assertEquals("tenancyDetails.checkAnswers.billsIncluded", rows[4].fieldHeading)
    }

    @Test
    fun `getTenancyDetailsSummaryList only returns the provide later row when tenancy details are deferred`() {
        whenever(mockTenancyDetailsState.provideTenancyDetailsLater).thenReturn(true)
        whenever(mockTenancyDetailsState.householdsAndTenantsTask).thenReturn(mockHouseholdsAndTenantsTask)
        whenever(mockHouseholdStep.outcome).thenReturn(HouseholdMode.PROVIDE_THIS_LATER)
        whenever(mockCyaState.getCyaJourneyId(mockHouseholdStep)).thenReturn("households-cya")

        val rows = helper.getTenancyDetailsSummaryList(mockCyaState, mockTenancyDetailsState, mockMessageSource)

        assertEquals(1, rows.size)
        assertEquals("tenancyDetails.checkAnswers.tenancyDetailsRow", rows[0].fieldHeading)
    }

    private fun stubTenancyDetailsState() {
        whenever(mockTenancyDetailsState.householdsAndTenantsTask).thenReturn(mockHouseholdsAndTenantsTask)
        whenever(mockHouseholdStep.outcome).thenReturn(HouseholdMode.COMPLETE)
        whenever(mockHouseholdStep.formModel).thenReturn(NumberOfHouseholdsFormModel().apply { numberOfHouseholds = "2" })
        whenever(mockTenantsStep.formModel).thenReturn(NewNumberOfPeopleFormModel().apply { numberOfPeople = "5" })
        whenever(mockCyaState.getCyaJourneyId(mockHouseholdStep)).thenReturn("households-cya")
        whenever(mockCyaState.getCyaJourneyId(mockTenantsStep)).thenReturn("tenants-cya")
        whenever(mockTenancyDetailsState.furnishedStatus).thenReturn(mockFurnishedStatusStep)
        whenever(mockFurnishedStatusStep.formModel).thenReturn(
            FurnishedStatusFormModel().apply { furnishedStatus = FurnishedStatus.FURNISHED },
        )
        whenever(mockCyaState.getCyaJourneyId(mockFurnishedStatusStep)).thenReturn("furnished-cya")
        whenever(mockTenancyDetailsState.rentIncludesBillsTask).thenReturn(mockRentIncludesBillsTask)
        whenever(mockRentIncludesBillsTask.rentIncludesBills).thenReturn(mockRentIncludesBillsStep)
        whenever(mockCyaState.getCyaJourneyId(mockRentIncludesBillsStep)).thenReturn("rent-bills-cya")
        whenever(mockTenancyDetailsState.rentFrequencyAndAmountTask).thenReturn(mockRentFrequencyAndAmountTask)
        whenever(mockRentFrequencyAndAmountTask.rentFrequency).thenReturn(mockRentFrequencyStep)
        whenever(mockRentFrequencyStep.formModel).thenReturn(RentFrequencyFormModel().apply { rentFrequency = RentFrequency.MONTHLY })
        whenever(mockCyaState.getCyaJourneyId(mockRentFrequencyStep)).thenReturn("frequency-cya")
        whenever(mockRentFrequencyAndAmountTask.rentAmount).thenReturn(mockRentAmountStep)
        lenient().`when`(mockRentAmountStep.formModel).thenReturn(RentAmountFormModel().apply { rentAmount = "500" })
        whenever(mockCyaState.getCyaJourneyId(mockRentAmountStep)).thenReturn("amount-cya")
    }
}
