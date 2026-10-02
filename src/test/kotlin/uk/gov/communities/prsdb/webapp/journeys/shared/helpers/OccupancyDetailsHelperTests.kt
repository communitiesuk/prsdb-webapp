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
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.tasks.TenancyDetailsTask
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.update.tenancyDetails.UpdateTenancyDetailsJourneyState
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
    private lateinit var mockPropertyRegistrationJourneyState: PropertyRegistrationJourneyState

    @Mock
    private lateinit var mockTenancyDetailsTask: TenancyDetailsTask

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

    @BeforeEach
    fun setUp() {
        lenient().`when`(mockPropertyRegistrationJourneyState.tenancyDetailsTask).thenReturn(mockTenancyDetailsTask)
        lenient()
            .`when`(
                mockTenancyDetailsTask.householdsAndTenantsTask,
            ).thenReturn(mockHouseholdsAndTenantsTask)
        lenient()
            .`when`(
                mockTenancyDetailsTask.rentIncludesBillsTask,
            ).thenReturn(mockRentIncludesBillsTask)
        lenient()
            .`when`(
                mockTenancyDetailsTask.rentFrequencyAndAmountTask,
            ).thenReturn(mockRentFrequencyAndAmountTask)
        lenient().`when`(mockTenancyDetailsTask.furnishedStatus).thenReturn(mockFurnishedStatusStep)
        lenient().`when`(mockTenancyState.householdsAndTenantsTask).thenReturn(mockHouseholdsAndTenantsTask)
        lenient().`when`(mockTenancyState.rentIncludesBillsTask).thenReturn(mockRentIncludesBillsTask)
        lenient().`when`(mockTenancyState.rentFrequencyAndAmountTask).thenReturn(mockRentFrequencyAndAmountTask)
        lenient().`when`(mockHouseholdsAndTenantsTask.households).thenReturn(mockHouseholdStep)
        lenient().`when`(mockHouseholdsAndTenantsTask.tenants).thenReturn(mockTenantsStep)
    }

    @Test
    fun `getCheckYourAnswersSummaryList returns no tenancy rows when property is unoccupied`() {
        whenever(mockPropertyRegistrationJourneyState.occupied).thenReturn(mockOccupiedStep)
        whenever(mockOccupiedStep.formModel).thenReturn(OccupancyFormModel().apply { occupied = false })

        val rows = helper.getCheckYourAnswersSummaryList(mockPropertyRegistrationJourneyState, mockMessageSource)

        assertEquals(0, rows.size)
    }

    @Test
    fun `getOccupancySummaryList returns the occupied row for unoccupied properties`() {
        whenever(mockPropertyRegistrationJourneyState.occupied).thenReturn(mockOccupiedStep)
        whenever(mockOccupiedStep.formModel).thenReturn(OccupancyFormModel().apply { occupied = false })
        whenever(mockPropertyRegistrationJourneyState.getCyaJourneyId(mockOccupiedStep)).thenReturn("occupied-cya")

        val rows = helper.getOccupancySummaryList(mockPropertyRegistrationJourneyState)

        assertEquals(1, rows.size)
        assertEquals("forms.checkPropertyAnswers.occupancy.question", rows[0].fieldHeading)
        assertEquals(false, rows[0].fieldValue)
    }

    @Test
    fun `getOccupancySummaryList returns the occupied row for occupied properties`() {
        whenever(mockPropertyRegistrationJourneyState.occupied).thenReturn(mockOccupiedStep)
        whenever(mockOccupiedStep.formModel).thenReturn(OccupancyFormModel().apply { occupied = true })
        whenever(mockPropertyRegistrationJourneyState.getCyaJourneyId(mockOccupiedStep)).thenReturn("occupied-cya")

        val rows = helper.getOccupancySummaryList(mockPropertyRegistrationJourneyState)

        assertEquals(1, rows.size)
        assertEquals("forms.checkPropertyAnswers.occupancy.question", rows[0].fieldHeading)
        assertEquals(true, rows[0].fieldValue)
    }

    @Test
    fun `getCheckYourAnswersSummaryList excludes bedrooms from tenancy rows when property is occupied`() {
        whenever(mockPropertyRegistrationJourneyState.occupied).thenReturn(mockOccupiedStep)
        whenever(mockOccupiedStep.formModel).thenReturn(OccupancyFormModel().apply { occupied = true })

        whenever(mockPropertyRegistrationJourneyState.tenancyDetailsTask.householdsAndTenantsTask).thenReturn(mockHouseholdsAndTenantsTask)
        whenever(mockHouseholdStep.outcome).thenReturn(HouseholdMode.COMPLETE)
        whenever(mockHouseholdStep.formModel).thenReturn(NumberOfHouseholdsFormModel().apply { numberOfHouseholds = "2" })
        whenever(mockTenantsStep.formModel).thenReturn(NewNumberOfPeopleFormModel().apply { numberOfPeople = "5" })
        whenever(mockPropertyRegistrationJourneyState.tenancyDetailsTask.rentIncludesBillsTask).thenReturn(mockRentIncludesBillsTask)
        whenever(mockRentIncludesBillsTask.rentIncludesBills).thenReturn(mockRentIncludesBillsStep)
        whenever(mockPropertyRegistrationJourneyState.getCyaJourneyId(mockRentIncludesBillsStep)).thenReturn("rent-bills-cya")
        whenever(mockPropertyRegistrationJourneyState.tenancyDetailsTask.furnishedStatus).thenReturn(mockFurnishedStatusStep)
        whenever(mockFurnishedStatusStep.formModel).thenReturn(
            FurnishedStatusFormModel().apply { furnishedStatus = FurnishedStatus.FURNISHED },
        )
        whenever(mockPropertyRegistrationJourneyState.getCyaJourneyId(mockFurnishedStatusStep)).thenReturn("furnished-cya")
        whenever(
            mockPropertyRegistrationJourneyState.tenancyDetailsTask.rentFrequencyAndAmountTask,
        ).thenReturn(mockRentFrequencyAndAmountTask)
        whenever(mockRentFrequencyAndAmountTask.rentFrequency).thenReturn(mockRentFrequencyStep)
        whenever(mockRentFrequencyStep.formModel).thenReturn(RentFrequencyFormModel().apply { rentFrequency = RentFrequency.MONTHLY })
        whenever(mockPropertyRegistrationJourneyState.getCyaJourneyId(mockRentFrequencyStep)).thenReturn("frequency-cya")
        whenever(mockRentFrequencyAndAmountTask.rentAmount).thenReturn(mockRentAmountStep)
        lenient().`when`(mockRentAmountStep.formModel).thenReturn(
            RentAmountFormModel().apply { rentAmount = "500" },
        )
        whenever(mockPropertyRegistrationJourneyState.getCyaJourneyId(mockRentAmountStep)).thenReturn("amount-cya")
        whenever(mockPropertyRegistrationJourneyState.getCyaJourneyId(mockHouseholdStep)).thenReturn("households-cya")
        whenever(mockPropertyRegistrationJourneyState.getCyaJourneyId(mockTenantsStep)).thenReturn("tenants-cya")

        val rows = helper.getCheckYourAnswersSummaryList(mockPropertyRegistrationJourneyState, mockMessageSource)

        assertEquals(false, rows.any { it.fieldHeading == "forms.checkPropertyAnswers.tenancyDetails.bedrooms" })
    }

    @Test
    fun `getCheckYourAnswersSummaryList includes tenancy rows when property is occupied`() {
        whenever(mockPropertyRegistrationJourneyState.occupied).thenReturn(mockOccupiedStep)
        whenever(mockOccupiedStep.formModel).thenReturn(OccupancyFormModel().apply { occupied = true })
        whenever(mockPropertyRegistrationJourneyState.tenancyDetailsTask.householdsAndTenantsTask).thenReturn(mockHouseholdsAndTenantsTask)
        whenever(mockHouseholdStep.outcome).thenReturn(HouseholdMode.COMPLETE)
        whenever(mockHouseholdStep.formModel).thenReturn(NumberOfHouseholdsFormModel().apply { numberOfHouseholds = "2" })
        whenever(mockTenantsStep.formModel).thenReturn(NewNumberOfPeopleFormModel().apply { numberOfPeople = "5" })
        whenever(mockPropertyRegistrationJourneyState.getCyaJourneyId(mockHouseholdStep)).thenReturn("households-cya")
        whenever(mockPropertyRegistrationJourneyState.getCyaJourneyId(mockTenantsStep)).thenReturn("tenants-cya")
        whenever(mockPropertyRegistrationJourneyState.tenancyDetailsTask.furnishedStatus).thenReturn(mockFurnishedStatusStep)
        whenever(mockFurnishedStatusStep.formModel).thenReturn(
            FurnishedStatusFormModel().apply { furnishedStatus = FurnishedStatus.FURNISHED },
        )
        whenever(mockPropertyRegistrationJourneyState.getCyaJourneyId(mockFurnishedStatusStep)).thenReturn("furnished-cya")
        whenever(mockPropertyRegistrationJourneyState.tenancyDetailsTask.rentIncludesBillsTask).thenReturn(mockRentIncludesBillsTask)
        whenever(mockRentIncludesBillsTask.rentIncludesBills).thenReturn(mockRentIncludesBillsStep)
        whenever(mockPropertyRegistrationJourneyState.getCyaJourneyId(mockRentIncludesBillsStep)).thenReturn("rent-bills-cya")
        whenever(
            mockPropertyRegistrationJourneyState.tenancyDetailsTask.rentFrequencyAndAmountTask,
        ).thenReturn(mockRentFrequencyAndAmountTask)
        whenever(mockRentFrequencyAndAmountTask.rentFrequency).thenReturn(mockRentFrequencyStep)
        whenever(mockRentFrequencyStep.formModel).thenReturn(RentFrequencyFormModel().apply { rentFrequency = RentFrequency.MONTHLY })
        whenever(mockPropertyRegistrationJourneyState.getCyaJourneyId(mockRentFrequencyStep)).thenReturn("frequency-cya")
        whenever(mockRentFrequencyAndAmountTask.rentAmount).thenReturn(mockRentAmountStep)
        lenient().`when`(mockRentAmountStep.formModel).thenReturn(
            RentAmountFormModel().apply { rentAmount = "500" },
        )
        whenever(mockPropertyRegistrationJourneyState.getCyaJourneyId(mockRentAmountStep)).thenReturn("amount-cya")

        val rows = helper.getCheckYourAnswersSummaryList(mockPropertyRegistrationJourneyState, mockMessageSource)

        assertEquals(6, rows.size)
        assertEquals("forms.checkPropertyAnswers.tenancyDetails.households", rows[0].fieldHeading)
        assertEquals("forms.checkPropertyAnswers.tenancyDetails.people", rows[1].fieldHeading)
        assertEquals("forms.checkPropertyAnswers.tenancyDetails.furnishedStatus", rows[2].fieldHeading)
        assertEquals("forms.checkPropertyAnswers.tenancyDetails.rentIncludesBills", rows[3].fieldHeading)
        assertEquals("forms.checkPropertyAnswers.tenancyDetails.rentFrequency", rows[4].fieldHeading)
        assertEquals("forms.checkPropertyAnswers.tenancyDetails.rentAmount", rows[5].fieldHeading)
    }

    @Test
    fun `getCheckYourAnswersSummaryList uses provide later tenancy row when households are deferred`() {
        whenever(mockPropertyRegistrationJourneyState.occupied).thenReturn(mockOccupiedStep)
        whenever(mockOccupiedStep.formModel).thenReturn(OccupancyFormModel().apply { occupied = true })
        whenever(mockPropertyRegistrationJourneyState.tenancyDetailsTask.householdsAndTenantsTask).thenReturn(mockHouseholdsAndTenantsTask)
        whenever(mockHouseholdStep.outcome).thenReturn(HouseholdMode.PROVIDE_THIS_LATER)
        whenever(mockPropertyRegistrationJourneyState.getCyaJourneyId(mockHouseholdStep)).thenReturn("households-cya")

        val rows = helper.getCheckYourAnswersSummaryList(mockPropertyRegistrationJourneyState, mockMessageSource)

        assertEquals(1, rows.size)
        assertEquals("forms.checkPropertyAnswers.tenancyDetails.tenancyDetailsRow", rows[0].fieldHeading)
        assertEquals("forms.checkPropertyAnswers.tenancyDetails.provideLater", rows[0].fieldValue)
    }

    @Test
    fun `getCheckYourTenancyDetailsAnswersSummaryList omits rent rows when provide later is true`() {
        whenever(mockTenancyState.provideTenancyDetailsLater).thenReturn(true)
        whenever(mockHouseholdStep.outcome).thenReturn(HouseholdMode.PROVIDE_THIS_LATER)
        whenever(mockTenancyState.getCyaJourneyId(mockHouseholdStep)).thenReturn("households-cya")

        val rows = helper.getCheckYourTenancyDetailsAnswersSummaryList(mockTenancyState, mockMessageSource)

        assertEquals(1, rows.size)
        assertEquals("forms.checkPropertyAnswers.tenancyDetails.tenancyDetailsRow", rows[0].fieldHeading)
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
        assertEquals("forms.checkPropertyAnswers.tenancyDetails.tenancyDetailsRow", rows[0].fieldHeading)
        assertEquals("forms.checkPropertyAnswers.tenancyDetails.provideLater", rows[0].fieldValue)
        assertEquals(true, rows[0].actions[0].url.contains("journeyId=journey-123"))
    }
}
