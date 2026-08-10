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
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.HouseholdsAndTenantsState
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.BedroomsStep
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
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.update.occupancy.UpdateOccupancyJourneyState
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.update.tenancyDetails.UpdateTenancyDetailsJourneyState
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.FurnishedStatusFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NewNumberOfPeopleFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NumberOfBedroomsFormModel
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
    private lateinit var mockOccupationState: UpdateOccupancyJourneyState

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
    private lateinit var mockBedroomsStep: BedroomsStep

    @Mock
    private lateinit var mockRentIncludesBillsStep: RentIncludesBillsStep

    @Mock
    private lateinit var mockFurnishedStatusStep: FurnishedStatusStep

    @Mock
    private lateinit var mockRentFrequencyStep: RentFrequencyStep

    @Mock
    private lateinit var mockRentAmountStep: RentAmountStep

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
    fun `getCheckYourAnswersSummaryList returns only occupied row when property is unoccupied`() {
        whenever(mockOccupationState.occupied).thenReturn(mockOccupiedStep)
        whenever(mockOccupiedStep.formModel).thenReturn(OccupancyFormModel().apply { occupied = false })
        whenever(mockOccupationState.getCyaJourneyId(mockOccupiedStep)).thenReturn("occupied-cya")

        val rows = helper.getCheckYourAnswersSummaryList(mockOccupationState, mockMessageSource)

        assertEquals(1, rows.size)
        assertEquals("forms.checkPropertyAnswers.tenancyDetails.occupied", rows[0].fieldHeading)
    }

    @Test
    fun `getRestructuredOccupancySummaryList returns the occupied row for unoccupied properties`() {
        whenever(mockOccupationState.occupied).thenReturn(mockOccupiedStep)
        whenever(mockOccupiedStep.formModel).thenReturn(OccupancyFormModel().apply { occupied = false })
        whenever(mockOccupationState.getCyaJourneyId(mockOccupiedStep)).thenReturn("occupied-cya")

        val rows = helper.getRestructuredOccupancySummaryList(mockOccupationState)

        assertEquals(1, rows.size)
        assertEquals("forms.checkPropertyAnswers.occupancy.question", rows[0].fieldHeading)
    }

    @Test
    fun `getCheckYourAnswersSummaryList includes all tenancy rows when property is occupied`() {
        whenever(mockOccupationState.occupied).thenReturn(mockOccupiedStep)
        whenever(mockOccupiedStep.formModel).thenReturn(OccupancyFormModel().apply { occupied = true })
        whenever(mockOccupationState.getCyaJourneyId(mockOccupiedStep)).thenReturn("occupied-cya")

        whenever(mockOccupationState.householdsAndTenantsTask).thenReturn(mockHouseholdsAndTenantsTask)
        whenever(mockHouseholdStep.outcome).thenReturn(HouseholdMode.COMPLETE)
        whenever(mockHouseholdStep.formModel).thenReturn(NumberOfHouseholdsFormModel().apply { numberOfHouseholds = "2" })
        whenever(mockTenantsStep.formModel).thenReturn(NewNumberOfPeopleFormModel().apply { numberOfPeople = "5" })
        whenever(mockOccupationState.bedrooms).thenReturn(mockBedroomsStep)
        whenever(mockBedroomsStep.formModel).thenReturn(NumberOfBedroomsFormModel().apply { numberOfBedrooms = "3" })
        whenever(mockOccupationState.rentIncludesBillsTask).thenReturn(mockRentIncludesBillsTask)
        whenever(mockRentIncludesBillsTask.rentIncludesBills).thenReturn(mockRentIncludesBillsStep)
        whenever(mockOccupationState.getCyaJourneyId(mockRentIncludesBillsStep)).thenReturn("rent-bills-cya")
        whenever(mockOccupationState.furnishedStatus).thenReturn(mockFurnishedStatusStep)
        whenever(mockFurnishedStatusStep.formModel).thenReturn(
            FurnishedStatusFormModel().apply { furnishedStatus = FurnishedStatus.FURNISHED },
        )
        whenever(mockOccupationState.getCyaJourneyId(mockFurnishedStatusStep)).thenReturn("furnished-cya")
        whenever(mockOccupationState.rentFrequencyAndAmountTask).thenReturn(mockRentFrequencyAndAmountTask)
        whenever(mockRentFrequencyAndAmountTask.rentFrequency).thenReturn(mockRentFrequencyStep)
        whenever(mockRentFrequencyStep.formModel).thenReturn(RentFrequencyFormModel().apply { rentFrequency = RentFrequency.MONTHLY })
        whenever(mockOccupationState.getCyaJourneyId(mockRentFrequencyStep)).thenReturn("frequency-cya")
        whenever(mockRentFrequencyAndAmountTask.rentAmount).thenReturn(mockRentAmountStep)
        lenient().`when`(mockRentAmountStep.formModel).thenReturn(
            RentAmountFormModel().apply { rentAmount = "500" },
        )
        whenever(mockOccupationState.getCyaJourneyId(mockRentAmountStep)).thenReturn("amount-cya")
        whenever(mockOccupationState.getCyaJourneyId(mockHouseholdStep)).thenReturn("households-cya")
        whenever(mockOccupationState.getCyaJourneyId(mockTenantsStep)).thenReturn("tenants-cya")
        whenever(mockOccupationState.getCyaJourneyId(mockBedroomsStep)).thenReturn("bedrooms-cya")

        val rows = helper.getCheckYourAnswersSummaryList(mockOccupationState, mockMessageSource)

        assertEquals(8, rows.size)
        assertEquals("forms.checkPropertyAnswers.tenancyDetails.occupied", rows[0].fieldHeading)
        assertEquals("forms.checkPropertyAnswers.tenancyDetails.households", rows[1].fieldHeading)
        assertEquals("forms.checkPropertyAnswers.tenancyDetails.people", rows[2].fieldHeading)
        assertEquals("forms.checkPropertyAnswers.tenancyDetails.bedrooms", rows[3].fieldHeading)
    }

    @Test
    fun `getRestructuredCheckYourAnswersSummaryList includes tenancy rows when property is occupied`() {
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

        val rows = helper.getRestructuredCheckYourAnswersSummaryList(mockOccupationState, mockMessageSource)

        assertEquals(6, rows.size)
        assertEquals("forms.checkPropertyAnswers.tenancyDetails.households", rows[0].fieldHeading)
        assertEquals("forms.checkPropertyAnswers.tenancyDetails.people", rows[1].fieldHeading)
        assertEquals("forms.checkPropertyAnswers.tenancyDetails.furnishedStatus", rows[2].fieldHeading)
        assertEquals("forms.checkPropertyAnswers.tenancyDetails.rentIncludesBills", rows[3].fieldHeading)
        assertEquals("forms.checkPropertyAnswers.tenancyDetails.rentFrequency", rows[4].fieldHeading)
        assertEquals("forms.checkPropertyAnswers.tenancyDetails.rentAmount", rows[5].fieldHeading)
    }

    @Test
    fun `getRestructuredCheckYourAnswersSummaryList uses provide later tenancy row when households are deferred`() {
        whenever(mockOccupationState.occupied).thenReturn(mockOccupiedStep)
        whenever(mockOccupiedStep.formModel).thenReturn(OccupancyFormModel().apply { occupied = true })
        whenever(mockOccupationState.householdsAndTenantsTask).thenReturn(mockHouseholdsAndTenantsTask)
        whenever(mockHouseholdStep.outcome).thenReturn(HouseholdMode.PROVIDE_THIS_LATER)
        whenever(mockOccupationState.getCyaJourneyId(mockHouseholdStep)).thenReturn("households-cya")

        val rows = helper.getRestructuredCheckYourAnswersSummaryList(mockOccupationState, mockMessageSource)

        assertEquals(1, rows.size)
        assertEquals("forms.checkPropertyAnswers.tenancyDetails.restructureAndSkipping.tenancyDetailsRow", rows[0].fieldHeading)
        assertEquals("forms.checkPropertyAnswers.tenancyDetails.provideLater", rows[0].fieldValue)
    }

    @Test
    fun `getCheckYourTenancyDetailsAnswersSummaryList omits rent rows when provide later is true`() {
        whenever(mockTenancyState.provideTenancyDetailsLater).thenReturn(true)
        whenever(mockHouseholdStep.outcome).thenReturn(HouseholdMode.PROVIDE_THIS_LATER)
        whenever(mockTenancyState.getCyaJourneyId(mockHouseholdStep)).thenReturn("households-cya")

        val rows = helper.getCheckYourTenancyDetailsAnswersSummaryList(mockTenancyState, mockMessageSource)

        assertEquals(1, rows.size)
        assertEquals("forms.checkPropertyAnswers.tenancyDetails.restructureAndSkipping.tenancyDetailsRow", rows[0].fieldHeading)
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
        assertEquals("forms.checkPropertyAnswers.tenancyDetails.restructureAndSkipping.tenancyDetailsRow", rows[0].fieldHeading)
        assertEquals("forms.checkPropertyAnswers.tenancyDetails.provideLater", rows[0].fieldValue)
        assertEquals(true, rows[0].actions[0].url.contains("journeyId=journey-123"))
    }
}
