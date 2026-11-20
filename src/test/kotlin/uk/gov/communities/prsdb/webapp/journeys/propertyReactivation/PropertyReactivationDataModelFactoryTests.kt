package uk.gov.communities.prsdb.webapp.journeys.propertyReactivation

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.constants.enums.FurnishedStatus
import uk.gov.communities.prsdb.webapp.constants.enums.LicensingType
import uk.gov.communities.prsdb.webapp.constants.enums.RentFrequency
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.LicensingAndTenancyDetailsDataModelFactory
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.tasks.LicensingTask
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.tasks.TenancyDetailsTask
import uk.gov.communities.prsdb.webapp.models.dataModels.LicensingDetailsDataModel
import uk.gov.communities.prsdb.webapp.models.dataModels.PropertyReactivationDataModel
import uk.gov.communities.prsdb.webapp.models.dataModels.TenancyDetailsDataModel
import java.math.BigDecimal

@ExtendWith(MockitoExtension::class)
class PropertyReactivationDataModelFactoryTests {
    @Mock
    private lateinit var mockLicensingAndTenancyDetailsDataModelFactory: LicensingAndTenancyDetailsDataModelFactory

    @Mock
    private lateinit var mockState: PropertyReactivationJourneyState

    private lateinit var factory: PropertyReactivationDataModelFactory

    @BeforeEach
    fun setUp() {
        factory = PropertyReactivationDataModelFactory(mockLicensingAndTenancyDetailsDataModelFactory)
    }

    @Test
    fun `fromJourneyState leaves every section empty when the property had no details to provide`() {
        // Arrange
        whenever(mockState.requiredTaskTypes).thenReturn(emptyList())

        // Act
        val result = factory.fromJourneyState(mockState)

        // Assert
        assertEquals(PropertyReactivationDataModel(), result)
    }

    @Test
    fun `fromJourneyState only populates the licensing section when only licensing was required`() {
        // Arrange
        whenever(mockState.requiredTaskTypes).thenReturn(listOf(ReactivationTaskType.LICENSING))
        val licensingTask = mock<LicensingTask>()
        whenever(mockState.licensingTask).thenReturn(licensingTask)
        val licensing = LicensingDetailsDataModel(LicensingType.SELECTIVE_LICENCE, "SL-123")
        whenever(mockLicensingAndTenancyDetailsDataModelFactory.fromLicensingState(licensingTask)).thenReturn(licensing)

        // Act
        val result = factory.fromJourneyState(mockState)

        // Assert
        assertEquals(PropertyReactivationDataModel(licensing = licensing), result)
    }

    @Test
    fun `fromJourneyState only populates the tenancy details section when only tenancy details were required`() {
        // Arrange
        whenever(mockState.requiredTaskTypes).thenReturn(listOf(ReactivationTaskType.TENANCY_DETAILS))
        val tenancyDetailsTask = mock<TenancyDetailsTask>()
        whenever(mockState.tenancyDetailsTask).thenReturn(tenancyDetailsTask)
        val tenancyDetails =
            TenancyDetailsDataModel(2, 4, null, null, FurnishedStatus.FURNISHED, RentFrequency.MONTHLY, null, BigDecimal("400.50"))
        whenever(mockLicensingAndTenancyDetailsDataModelFactory.fromTenancyDetailsState(tenancyDetailsTask)).thenReturn(tenancyDetails)

        // Act
        val result = factory.fromJourneyState(mockState)

        // Assert
        assertEquals(PropertyReactivationDataModel(tenancyDetails = tenancyDetails), result)
    }
}
