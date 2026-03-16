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
import uk.gov.communities.prsdb.webapp.constants.enums.BillsIncluded
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.update.rentIncludesBills.UpdateRentIncludesBillsCyaConfig
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.update.rentIncludesBills.UpdateRentIncludesBillsJourneyState
import uk.gov.communities.prsdb.webapp.journeys.shared.helpers.OccupancyDetailsHelper
import uk.gov.communities.prsdb.webapp.models.dataModels.BillsIncludedDataModel
import uk.gov.communities.prsdb.webapp.services.PropertyOwnershipService
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockMessageSource

@ExtendWith(MockitoExtension::class)
class UpdateRentIncludesBillsCyaConfigTests {
    @Mock
    private lateinit var mockPropertyOwnershipService: PropertyOwnershipService

    @Mock
    private lateinit var mockState: UpdateRentIncludesBillsJourneyState

    @Mock
    private lateinit var stepConfig: UpdateRentIncludesBillsCyaConfig

    @Mock
    private val mockMessageSource = MockMessageSource()

    private val propertyId = 123L
    private val billsIncludedList = "ELECTRICITY,WATER,SOMETHING_ELSE"
    private val customBillsIncluded = "Cat sitting"
    private val initialLastModifiedDate = Clock.System.now().toJavaInstant()
    private val billsIncludedDataModel =
        BillsIncludedDataModel(
            standardBillsIncludedListAsString = billsIncludedList,
            customBillsIncluded = customBillsIncluded,
            standardBillsIncludedListAsEnums = listOf(BillsIncluded.ELECTRICITY, BillsIncluded.WATER, BillsIncluded.SOMETHING_ELSE),
        )

    @BeforeEach
    fun setUp() {
        stepConfig =
            UpdateRentIncludesBillsCyaConfig(
                occupancyDetailsHelper = OccupancyDetailsHelper(),
                propertyOwnershipService = mockPropertyOwnershipService,
                messageSource = mockMessageSource,
            )
        whenever(mockState.propertyId).thenReturn(propertyId)
        whenever(mockState.lastModifiedDate).thenReturn(initialLastModifiedDate.toString())
        whenever(mockState.getBillsIncludedOrNull()).thenReturn(billsIncludedDataModel)
    }

    @Test
    fun `afterStepDataIsAdded calls updateHouseholdsAndTenants on propertyOwnershipService`() {
        // Act
        stepConfig.afterStepDataIsAdded(mockState)

        // Assert
        verify(mockPropertyOwnershipService).updateRentIncludesBills(
            id = propertyId,
            billsIncludedList = billsIncludedList,
            customBillsIncluded = customBillsIncluded,
            initialLastModifiedDate = initialLastModifiedDate,
        )
    }
}
