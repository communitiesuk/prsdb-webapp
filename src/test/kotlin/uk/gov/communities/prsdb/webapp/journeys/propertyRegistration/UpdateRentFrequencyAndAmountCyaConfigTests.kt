package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration

import kotlinx.datetime.Clock
import kotlinx.datetime.toJavaInstant
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.constants.enums.RentFrequency
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.RentAmountStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.RentFrequencyStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.update.rentFrequencyAndAmount.UpdateRentFrequencyAndAmountCyaConfig
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.update.rentFrequencyAndAmount.UpdateRentFrequencyAndAmountJourneyState
import uk.gov.communities.prsdb.webapp.journeys.shared.helpers.OccupancyDetailsHelper
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.RentAmountFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.RentFrequencyFormModel
import uk.gov.communities.prsdb.webapp.services.PropertyOwnershipService
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockMessageSource
import java.math.BigDecimal

@ExtendWith(MockitoExtension::class)
class UpdateRentFrequencyAndAmountCyaConfigTests {
    @Mock
    private lateinit var mockPropertyOwnershipService: PropertyOwnershipService

    @Mock
    private lateinit var mockState: UpdateRentFrequencyAndAmountJourneyState

    @Mock
    private lateinit var stepConfig: UpdateRentFrequencyAndAmountCyaConfig

    @Mock
    private lateinit var mockRentFrequencyStep: RentFrequencyStep

    @Mock
    private lateinit var mockRentAmountStep: RentAmountStep

    @Mock
    private lateinit var mockRentFrequencyFormModel: RentFrequencyFormModel

    @Mock
    private lateinit var mockRentAMountFormModel: RentAmountFormModel

    @Mock
    private val mockMessageSource = MockMessageSource()

    private val propertyId = 123L
    private val rentFrequency = RentFrequency.OTHER
    private val customRentFrequency = "Five weeks"
    private val rentAmount = BigDecimal(200)
    private val initialLastModifiedDate = Clock.System.now().toJavaInstant()

    @BeforeEach
    fun setUp() {
        stepConfig =
            UpdateRentFrequencyAndAmountCyaConfig(
                occupancyDetailsHelper = OccupancyDetailsHelper(),
                propertyOwnershipService = mockPropertyOwnershipService,
                messageSource = mockMessageSource,
            )
        whenever(mockState.propertyId).thenReturn(propertyId)
        whenever(mockState.rentFrequency).thenReturn(mockRentFrequencyStep)
        whenever(mockState.rentAmount).thenReturn(mockRentAmountStep)
        whenever(mockState.lastModifiedDate).thenReturn(initialLastModifiedDate.toString())
        whenever(mockRentFrequencyStep.formModel).thenReturn(mockRentFrequencyFormModel)
        whenever(mockRentAmountStep.formModel).thenReturn(mockRentAMountFormModel)
        whenever(mockRentFrequencyFormModel.rentFrequency).thenReturn(rentFrequency)
        whenever(mockState.getCustomRentFrequencyIfSelected()).thenReturn(customRentFrequency)
        whenever(mockRentAMountFormModel.rentAmount).thenReturn(rentAmount.toString())
    }

    @Test
    fun `afterStepDataIsAdded calls updateRentFrequencyAndAmount on propertyOwnershipService`() {
        // Act
        stepConfig.afterStepDataIsAdded(mockState)

        // Assert
        verify(mockPropertyOwnershipService).updateRentFrequencyAndAmount(
            id = propertyId,
            rentFrequency = rentFrequency,
            customRentFrequency = customRentFrequency,
            rentAmount = rentAmount,
            initialLastModifiedDate = initialLastModifiedDate,
        )
    }
}
