package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration

import kotlinx.datetime.Clock
import kotlinx.datetime.toJavaInstant
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.argThat
import org.mockito.kotlin.eq
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.context.MessageSource
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
        whenever(mockState.households).thenReturn(mockHouseholdStep)
        whenever(mockHouseholdStep.formModel).thenReturn(mockNumberOfHouseholdsFormModel)
        whenever(mockNumberOfHouseholdsFormModel.numberOfHouseholds).thenReturn("2")
        whenever(mockState.tenants).thenReturn(mockTenantsStep)
        whenever(mockTenantsStep.formModel).thenReturn(mockNumberOfTenantsFormModel)
        whenever(mockNumberOfTenantsFormModel.numberOfPeople).thenReturn("5")
        whenever(mockState.bedrooms).thenReturn(mockBedroomsStep)
        whenever(mockBedroomsStep.formModel).thenReturn(mockNumberOfBedroomsFormModel)
        whenever(mockNumberOfBedroomsFormModel.numberOfBedrooms).thenReturn("3")
        whenever(mockState.getBillsIncludedOrNull()).thenReturn(null)
        whenever(mockState.furnishedStatus).thenReturn(mockFurnishedStatusStep)
        whenever(mockFurnishedStatusStep.formModel).thenReturn(mockFurnishedStatusFormModel)
        whenever(mockFurnishedStatusFormModel.furnishedStatus).thenReturn(null)
        whenever(mockState.rentFrequency).thenReturn(mockRentFrequencyStep)
        whenever(mockRentFrequencyStep.formModel).thenReturn(mockRentFrequencyFormModel)
        whenever(mockRentFrequencyFormModel.rentFrequency).thenReturn(null)
        whenever(mockState.getCustomRentFrequencyIfSelected()).thenReturn(null)
        whenever(mockState.rentAmount).thenReturn(mockRentAmountStep)
        whenever(mockRentAmountStep.formModel).thenReturn(mockRentAmountFormModel)
        whenever(mockRentAmountFormModel.rentAmount).thenReturn("500")
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
    fun `afterStepDataIsAdded sends confirmation email with correct updated items`() {
        val propertyOwnership = MockLandlordData.createPropertyOwnership(id = propertyId)
        whenever(mockPropertyOwnershipService.getPropertyOwnership(propertyId)).thenReturn(propertyOwnership)
        whenever(mockAbsoluteUrlProvider.buildLandlordDashboardUri()).thenReturn(URI("http://example.com"))

        stepConfig.afterStepDataIsAdded(mockState)

        verify(mockEmailNotificationService).sendEmail(
            any(),
            argThat<PropertyUpdateConfirmation> { this.updatedBullets == listOf("Whether the property is occupied by tenants") },
        )
    }
}
