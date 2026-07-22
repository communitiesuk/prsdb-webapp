package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration

import kotlinx.datetime.Clock
import kotlinx.datetime.toJavaInstant
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.constants.enums.FurnishedStatus
import uk.gov.communities.prsdb.webapp.constants.enums.RentFrequency
import uk.gov.communities.prsdb.webapp.exceptions.UpdateConflictException
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.BedroomsStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.FurnishedStatusStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.HouseholdStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.RentAmountStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.RentFrequencyStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.TenantsStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.tasks.HouseholdsAndTenantsTask
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.tasks.RentFrequencyAndAmountTask
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.tasks.RentIncludesBillsTask
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.update.tenancyDetails.UpdateTenancyDetailsCyaConfig
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.update.tenancyDetails.UpdateTenancyDetailsJourneyState
import uk.gov.communities.prsdb.webapp.journeys.shared.helpers.OccupancyDetailsHelper
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.FurnishedStatusFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NewNumberOfPeopleFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NumberOfBedroomsFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NumberOfHouseholdsFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.RentAmountFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.RentFrequencyFormModel
import uk.gov.communities.prsdb.webapp.services.PropertyOwnershipService
import uk.gov.communities.prsdb.webapp.services.PropertyUpdateEmailService
import java.math.BigDecimal

@ExtendWith(MockitoExtension::class)
class UpdateTenancyDetailsCyaConfigTests {
    @Mock
    private lateinit var mockPropertyOwnershipService: PropertyOwnershipService

    @Mock
    private lateinit var mockPropertyUpdateEmailService: PropertyUpdateEmailService

    @Mock
    private lateinit var mockState: UpdateTenancyDetailsJourneyState

    @Mock
    private lateinit var mockHouseholdsAndTenantsTask: HouseholdsAndTenantsTask

    @Mock
    private lateinit var mockRentIncludesBillsTask: RentIncludesBillsTask

    @Mock
    private lateinit var mockRentFrequencyAndAmountTask: RentFrequencyAndAmountTask

    @Mock
    private lateinit var mockHouseholdStep: HouseholdStep

    @Mock
    private lateinit var mockTenantsStep: TenantsStep

    @Mock
    private lateinit var mockBedroomsStep: BedroomsStep

    @Mock
    private lateinit var mockFurnishedStatusStep: FurnishedStatusStep

    @Mock
    private lateinit var mockRentFrequencyStep: RentFrequencyStep

    @Mock
    private lateinit var mockRentAmountStep: RentAmountStep

    @Mock
    private lateinit var mockNumberOfHouseholdsFormModel: NumberOfHouseholdsFormModel

    @Mock
    private lateinit var mockNumberOfTenantsFormModel: NewNumberOfPeopleFormModel

    @Mock
    private lateinit var mockNumberOfBedroomsFormModel: NumberOfBedroomsFormModel

    @Mock
    private lateinit var mockFurnishedStatusFormModel: FurnishedStatusFormModel

    @Mock
    private lateinit var mockRentFrequencyFormModel: RentFrequencyFormModel

    @Mock
    private lateinit var mockRentAmountFormModel: RentAmountFormModel

    private lateinit var stepConfig: UpdateTenancyDetailsCyaConfig

    private val propertyId = 123L
    private val numberOfHouseholds = 2
    private val numberOfTenants = 5
    private val numberOfBedrooms = 3
    private val rentAmount = "500"
    private val initialLastModifiedDate = Clock.System.now().toJavaInstant()

    @BeforeEach
    fun setUp() {
        stepConfig =
            UpdateTenancyDetailsCyaConfig(
                occupancyDetailsHelper = OccupancyDetailsHelper(),
                propertyOwnershipService = mockPropertyOwnershipService,
                messageSource = mock(),
                propertyUpdateEmailService = mockPropertyUpdateEmailService,
            )
        stepConfig.afterStepIsReached(mockState)
        whenever(mockState.propertyId).thenReturn(propertyId)
        whenever(mockState.householdsAndTenantsTask).thenReturn(mockHouseholdsAndTenantsTask)
        whenever(mockState.rentIncludesBillsTask).thenReturn(mockRentIncludesBillsTask)
        whenever(mockState.rentFrequencyAndAmountTask).thenReturn(mockRentFrequencyAndAmountTask)
        whenever(mockHouseholdsAndTenantsTask.households).thenReturn(mockHouseholdStep)
        whenever(mockHouseholdsAndTenantsTask.tenants).thenReturn(mockTenantsStep)
        whenever(mockState.bedrooms).thenReturn(mockBedroomsStep)
        whenever(mockState.furnishedStatus).thenReturn(mockFurnishedStatusStep)
        whenever(mockRentFrequencyAndAmountTask.rentFrequency).thenReturn(mockRentFrequencyStep)
        whenever(mockRentFrequencyAndAmountTask.rentAmount).thenReturn(mockRentAmountStep)
        whenever(mockState.lastModifiedDate).thenReturn(initialLastModifiedDate.toString())
        whenever(mockHouseholdStep.formModel).thenReturn(mockNumberOfHouseholdsFormModel)
        whenever(mockTenantsStep.formModel).thenReturn(mockNumberOfTenantsFormModel)
        whenever(mockBedroomsStep.formModel).thenReturn(mockNumberOfBedroomsFormModel)
        whenever(mockFurnishedStatusStep.formModel).thenReturn(mockFurnishedStatusFormModel)
        whenever(mockRentFrequencyStep.formModel).thenReturn(mockRentFrequencyFormModel)
        whenever(mockRentAmountStep.formModel).thenReturn(mockRentAmountFormModel)
        whenever(mockNumberOfHouseholdsFormModel.numberOfHouseholds).thenReturn(numberOfHouseholds.toString())
        whenever(mockNumberOfTenantsFormModel.numberOfPeople).thenReturn(numberOfTenants.toString())
        whenever(mockNumberOfBedroomsFormModel.numberOfBedrooms).thenReturn(numberOfBedrooms.toString())
        whenever(mockFurnishedStatusFormModel.furnishedStatus).thenReturn(FurnishedStatus.FURNISHED)
        whenever(mockRentFrequencyFormModel.rentFrequency).thenReturn(RentFrequency.MONTHLY)
        whenever(mockRentAmountFormModel.rentAmount).thenReturn(rentAmount)
    }

    @Test
    fun `afterStepDataIsAdded calls updateTenancyDetails on propertyOwnershipService`() {
        // Act
        stepConfig.afterStepDataIsAdded(mockState)

        // Assert
        verify(mockPropertyOwnershipService).updateTenancyDetails(
            id = propertyId,
            numberOfHouseholds = numberOfHouseholds,
            numberOfPeople = numberOfTenants,
            numBedrooms = numberOfBedrooms,
            billsIncludedList = null,
            customBillsIncluded = null,
            furnishedStatus = FurnishedStatus.FURNISHED,
            rentFrequency = RentFrequency.MONTHLY,
            customRentFrequency = null,
            rentAmount = BigDecimal(rentAmount),
            initialLastModifiedDate = initialLastModifiedDate,
        )
    }

    @Test
    fun `afterStepDataIsAdded sends update emails with the correct updated items`() {
        // Act
        stepConfig.afterStepDataIsAdded(mockState)

        // Assert
        verify(mockPropertyUpdateEmailService).sendUpdateEmails(
            eq(propertyId),
            eq(
                listOf(
                    "The number of households living in this property",
                    "The number of people living in this property",
                    "The number of bedrooms in this property",
                    "Whether the rent includes bills",
                    "Whether the property is furnished",
                    "How often the rent is charged",
                    "The amount of rent charged",
                ),
            ),
        )
    }

    @Test
    fun `afterStepDataIsAdded deletes the journey then rethrows when it gets an UpdateConflictException`() {
        // Arrange
        whenever(
            mockPropertyOwnershipService.updateTenancyDetails(
                id = propertyId,
                numberOfHouseholds = numberOfHouseholds,
                numberOfPeople = numberOfTenants,
                numBedrooms = numberOfBedrooms,
                billsIncludedList = null,
                customBillsIncluded = null,
                furnishedStatus = FurnishedStatus.FURNISHED,
                rentFrequency = RentFrequency.MONTHLY,
                customRentFrequency = null,
                rentAmount = BigDecimal(rentAmount),
                initialLastModifiedDate = initialLastModifiedDate,
            ),
        ).thenThrow(UpdateConflictException::class.java)

        // Act, assert
        assertThrows<UpdateConflictException> { stepConfig.afterStepDataIsAdded(mockState) }

        verify(mockState).deleteJourney()
    }
}
