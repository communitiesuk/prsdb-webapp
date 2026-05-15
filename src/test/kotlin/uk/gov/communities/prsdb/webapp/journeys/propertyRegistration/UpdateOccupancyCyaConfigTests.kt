package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration

import kotlinx.datetime.Clock
import kotlinx.datetime.toJavaInstant
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito.lenient
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.argThat
import org.mockito.kotlin.eq
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.context.MessageSource
import uk.gov.communities.prsdb.webapp.exceptions.UpdateConflictException
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.BedroomsStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.BillsIncludedStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.FurnishedStatusStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.HouseholdStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.OccupiedStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.RentAmountStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.RentFrequencyStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.TenantsStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.update.occupancy.UpdateOccupancyCyaConfig
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.update.occupancy.UpdateOccupancyCyaStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.update.occupancy.UpdateOccupancyJourneyState
import uk.gov.communities.prsdb.webapp.journeys.shared.helpers.OccupancyDetailsHelper
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.FurnishedStatusFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NewNumberOfPeopleFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NumberOfBedroomsFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NumberOfHouseholdsFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.OccupancyFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.RentAmountFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.RentFrequencyFormModel
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.PropertyUpdateConfirmation
import uk.gov.communities.prsdb.webapp.services.AbsoluteUrlProvider
import uk.gov.communities.prsdb.webapp.services.EmailNotificationService
import uk.gov.communities.prsdb.webapp.services.PropertyOwnershipService
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.AlwaysTrueValidator
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLandlordData
import java.net.URI

@ExtendWith(MockitoExtension::class)
class UpdateOccupancyCyaConfigTests {
    @Mock
    private lateinit var mockOccupancyDetailsHelper: OccupancyDetailsHelper

    @Mock
    private lateinit var mockPropertyOwnershipService: PropertyOwnershipService

    @Mock
    private lateinit var mockMessageSource: MessageSource

    @Mock
    private lateinit var mockEmailNotificationService: EmailNotificationService<PropertyUpdateConfirmation>

    @Mock
    private lateinit var mockAbsoluteUrlProvider: AbsoluteUrlProvider

    @Mock
    private lateinit var mockState: UpdateOccupancyJourneyState

    @Mock
    private lateinit var mockOccupiedStep: OccupiedStep

    @Mock
    private lateinit var mockOccupancyFormModel: OccupancyFormModel

    @Mock
    private lateinit var mockHouseholdStep: HouseholdStep

    @Mock
    private lateinit var mockNumberOfHouseholdsFormModel: NumberOfHouseholdsFormModel

    @Mock
    private lateinit var mockTenantsStep: TenantsStep

    @Mock
    private lateinit var mockNumberOfTenantsFormModel: NewNumberOfPeopleFormModel

    @Mock
    private lateinit var mockBedroomsStep: BedroomsStep

    @Mock
    private lateinit var mockNumberOfBedroomsFormModel: NumberOfBedroomsFormModel

    @Mock
    private lateinit var mockFurnishedStatusStep: FurnishedStatusStep

    @Mock
    private lateinit var mockFurnishedStatusFormModel: FurnishedStatusFormModel

    @Mock
    private lateinit var mockRentFrequencyStep: RentFrequencyStep

    @Mock
    private lateinit var mockRentFrequencyFormModel: RentFrequencyFormModel

    @Mock
    private lateinit var mockRentAmountStep: RentAmountStep

    @Mock
    private lateinit var mockRentAmountFormModel: RentAmountFormModel

    @Mock
    private lateinit var mockBillsIncludedStep: BillsIncludedStep

    private val propertyId = 123L
    private val initialLastModifiedDate = Clock.System.now().toJavaInstant()

    @Mock
    private lateinit var stepConfig: UpdateOccupancyCyaConfig

    @BeforeEach
    fun setUp() {
        stepConfig =
            UpdateOccupancyCyaConfig(
                occupancyDetailsHelper = mockOccupancyDetailsHelper,
                propertyOwnershipService = mockPropertyOwnershipService,
                messageSource = mockMessageSource,
                updateConfirmationEmailService = mockEmailNotificationService,
                absoluteUrlProvider = mockAbsoluteUrlProvider,
            )
        stepConfig.routeSegment = UpdateOccupancyCyaStep.ROUTE_SEGMENT
        stepConfig.validator = AlwaysTrueValidator()
        stepConfig.afterStepIsReached(mockState)
        whenever(mockState.propertyId).thenReturn(propertyId)
        whenever(mockState.lastModifiedDate).thenReturn(initialLastModifiedDate.toString())
        whenever(mockState.occupied).thenReturn(mockOccupiedStep)
        whenever(mockOccupiedStep.formModel).thenReturn(mockOccupancyFormModel)
        whenever(mockOccupancyFormModel.occupied).thenReturn(true)
        lenient().`when`(mockState.wasOccupied).thenReturn(false)
        lenient().`when`(mockState.households).thenReturn(mockHouseholdStep)
        lenient().`when`(mockHouseholdStep.formModel).thenReturn(mockNumberOfHouseholdsFormModel)
        lenient().`when`(mockNumberOfHouseholdsFormModel.numberOfHouseholds).thenReturn("2")
        lenient().`when`(mockState.tenants).thenReturn(mockTenantsStep)
        lenient().`when`(mockTenantsStep.formModel).thenReturn(mockNumberOfTenantsFormModel)
        lenient().`when`(mockNumberOfTenantsFormModel.numberOfPeople).thenReturn("5")
        lenient().`when`(mockState.bedrooms).thenReturn(mockBedroomsStep)
        lenient().`when`(mockBedroomsStep.formModel).thenReturn(mockNumberOfBedroomsFormModel)
        lenient().`when`(mockNumberOfBedroomsFormModel.numberOfBedrooms).thenReturn("3")
        lenient().`when`(mockState.getBillsIncludedOrNull()).thenReturn(null)
        lenient().`when`(mockState.furnishedStatus).thenReturn(mockFurnishedStatusStep)
        lenient().`when`(mockFurnishedStatusStep.formModel).thenReturn(mockFurnishedStatusFormModel)
        lenient().`when`(mockFurnishedStatusFormModel.furnishedStatus).thenReturn(null)
        lenient().`when`(mockState.rentFrequency).thenReturn(mockRentFrequencyStep)
        lenient().`when`(mockRentFrequencyStep.formModel).thenReturn(mockRentFrequencyFormModel)
        lenient().`when`(mockRentFrequencyFormModel.rentFrequency).thenReturn(null)
        lenient().`when`(mockState.getCustomRentFrequencyIfSelected()).thenReturn(null)
        lenient().`when`(mockState.rentAmount).thenReturn(mockRentAmountStep)
        lenient().`when`(mockRentAmountStep.formModel).thenReturn(mockRentAmountFormModel)
        lenient().`when`(mockRentAmountFormModel.rentAmount).thenReturn("500")
    }

    @Test
    fun `afterStepDataIsAdded sends confirmation email to primary landlord`() {
        val landlordEmail = "landlord@example.com"
        val landlord = MockLandlordData.createLandlord(email = landlordEmail)
        val propertyOwnership = MockLandlordData.createPropertyOwnership(id = propertyId, primaryLandlord = landlord)
        whenever(mockPropertyOwnershipService.getPropertyOwnership(propertyId)).thenReturn(propertyOwnership)
        whenever(mockAbsoluteUrlProvider.buildLandlordDashboardUri()).thenReturn(URI("http://example.com"))

        stepConfig.afterStepDataIsAdded(mockState)

        verify(mockEmailNotificationService).sendEmail(eq(landlordEmail), any<PropertyUpdateConfirmation>())
    }

    @Test
    fun `afterStepDataIsAdded sends confirmation email listing households and tenants when transitioning unoccupied to occupied`() {
        val propertyOwnership = MockLandlordData.createPropertyOwnership(id = propertyId)
        whenever(mockPropertyOwnershipService.getPropertyOwnership(propertyId)).thenReturn(propertyOwnership)
        whenever(mockAbsoluteUrlProvider.buildLandlordDashboardUri()).thenReturn(URI("http://example.com"))
        whenever(mockState.wasOccupied).thenReturn(false)
        whenever(mockOccupancyFormModel.occupied).thenReturn(true)

        stepConfig.afterStepDataIsAdded(mockState)

        verify(mockEmailNotificationService).sendEmail(
            any(),
            argThat<PropertyUpdateConfirmation> {
                this.updatedBullets ==
                    listOf(
                        "Whether the property is occupied by tenants",
                        "The number of households living in this property",
                        "The number of people living in this property",
                    )
            },
        )
    }

    @Test
    fun `afterStepDataIsAdded sends confirmation email with only the occupancy bullet when property was already occupied`() {
        val propertyOwnership = MockLandlordData.createPropertyOwnership(id = propertyId)
        whenever(mockPropertyOwnershipService.getPropertyOwnership(propertyId)).thenReturn(propertyOwnership)
        whenever(mockAbsoluteUrlProvider.buildLandlordDashboardUri()).thenReturn(URI("http://example.com"))
        whenever(mockState.wasOccupied).thenReturn(true)
        whenever(mockOccupancyFormModel.occupied).thenReturn(true)

        stepConfig.afterStepDataIsAdded(mockState)

        verify(mockEmailNotificationService).sendEmail(
            any(),
            argThat<PropertyUpdateConfirmation> { this.updatedBullets == listOf("Whether the property is occupied by tenants") },
        )
    }

    @Test
    fun `afterStepDataIsAdded sends confirmation email with only the occupancy bullet when transitioning occupied to unoccupied`() {
        val propertyOwnership = MockLandlordData.createPropertyOwnership(id = propertyId)
        whenever(mockPropertyOwnershipService.getPropertyOwnership(propertyId)).thenReturn(propertyOwnership)
        whenever(mockAbsoluteUrlProvider.buildLandlordDashboardUri()).thenReturn(URI("http://example.com"))
        whenever(mockState.wasOccupied).thenReturn(true)
        whenever(mockOccupancyFormModel.occupied).thenReturn(false)

        stepConfig.afterStepDataIsAdded(mockState)

        verify(mockEmailNotificationService).sendEmail(
            any(),
            argThat<PropertyUpdateConfirmation> { this.updatedBullets == listOf("Whether the property is occupied by tenants") },
        )
    }

    @Test
    fun `afterStepDataIsAdded sends confirmation email with only the occupancy bullet when property remains unoccupied`() {
        val propertyOwnership = MockLandlordData.createPropertyOwnership(id = propertyId)
        whenever(mockPropertyOwnershipService.getPropertyOwnership(propertyId)).thenReturn(propertyOwnership)
        whenever(mockAbsoluteUrlProvider.buildLandlordDashboardUri()).thenReturn(URI("http://example.com"))
        whenever(mockState.wasOccupied).thenReturn(false)
        whenever(mockOccupancyFormModel.occupied).thenReturn(false)

        stepConfig.afterStepDataIsAdded(mockState)

        verify(mockEmailNotificationService).sendEmail(
            any(),
            argThat<PropertyUpdateConfirmation> { this.updatedBullets == listOf("Whether the property is occupied by tenants") },
        )
    }

    @Test
    fun `afterStepDataIsAdded deletes the journey then rethrows when it gets an UpdateConflictException`() {
        // Arrange
        whenever(
            mockPropertyOwnershipService.updateOccupancy(
                id = propertyId,
                numberOfHouseholds = 2,
                numberOfPeople = 5,
                numBedrooms = 3,
                billsIncludedList = null,
                customBillsIncluded = null,
                furnishedStatus = null,
                rentFrequency = null,
                customRentFrequency = null,
                rentAmount = "500".toBigDecimal(),
                initialLastModifiedDate = initialLastModifiedDate,
            ),
        ).thenThrow(UpdateConflictException::class.java)

        // Act, assert
        assertThrows<UpdateConflictException> { stepConfig.afterStepDataIsAdded(mockState) }

        verify(mockState).deleteJourney()
    }
}
