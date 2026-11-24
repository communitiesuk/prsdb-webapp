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
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.update.tenancyDetails.UpdateTenancyDetailsCyaConfig
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.update.tenancyDetails.UpdateTenancyDetailsJourneyState
import uk.gov.communities.prsdb.webapp.journeys.shared.helpers.OccupancyDetailsHelper
import uk.gov.communities.prsdb.webapp.models.dataModels.TenancyDetailsDataModel
import uk.gov.communities.prsdb.webapp.services.PropertyOwnershipService
import uk.gov.communities.prsdb.webapp.services.PropertyUpdateEmailService
import java.math.BigDecimal

@ExtendWith(MockitoExtension::class)
class UpdateTenancyDetailsCyaConfigTests {
    @Mock
    private lateinit var mockPropertyOwnershipService: PropertyOwnershipService

    @Mock
    private lateinit var mockLicensingAndTenancyDetailsDataModelFactory: LicensingAndTenancyDetailsDataModelFactory

    @Mock
    private lateinit var mockPropertyUpdateEmailService: PropertyUpdateEmailService

    @Mock
    private lateinit var mockState: UpdateTenancyDetailsJourneyState

    private lateinit var stepConfig: UpdateTenancyDetailsCyaConfig

    private val propertyId = 123L
    private val initialLastModifiedDate = Clock.System.now().toJavaInstant()
    private val tenancyDetails =
        TenancyDetailsDataModel(
            numberOfHouseholds = 2,
            numberOfPeople = 5,
            billsIncludedList = null,
            customBillsIncluded = null,
            furnishedStatus = FurnishedStatus.FURNISHED,
            rentFrequency = RentFrequency.MONTHLY,
            customRentFrequency = null,
            rentAmount = BigDecimal("500"),
        )

    @BeforeEach
    fun setUp() {
        stepConfig =
            UpdateTenancyDetailsCyaConfig(
                occupancyDetailsHelper = OccupancyDetailsHelper(),
                propertyOwnershipService = mockPropertyOwnershipService,
                licensingAndTenancyDetailsDataModelFactory = mockLicensingAndTenancyDetailsDataModelFactory,
                messageSource = mock(),
                propertyUpdateEmailService = mockPropertyUpdateEmailService,
            )
        stepConfig.afterStepIsReached(mockState)
        whenever(mockState.propertyId).thenReturn(propertyId)
        whenever(mockState.lastModifiedDate).thenReturn(initialLastModifiedDate.toString())
        whenever(mockLicensingAndTenancyDetailsDataModelFactory.fromTenancyDetailsState(mockState)).thenReturn(tenancyDetails)
    }

    @Test
    fun `afterStepDataIsAdded calls updateTenancyDetails with the tenancy details from the journey`() {
        // Act
        stepConfig.afterStepDataIsAdded(mockState)

        // Assert
        verify(mockPropertyOwnershipService).updateTenancyDetails(
            id = propertyId,
            tenancyDetails = tenancyDetails,
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
                    "Rent details (amount, how often it’s paid, bills and furnishing)",
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
                tenancyDetails = tenancyDetails,
                initialLastModifiedDate = initialLastModifiedDate,
            ),
        ).thenThrow(UpdateConflictException::class.java)

        // Act, assert
        assertThrows<UpdateConflictException> { stepConfig.afterStepDataIsAdded(mockState) }

        verify(mockState).deleteJourney()
    }
}
