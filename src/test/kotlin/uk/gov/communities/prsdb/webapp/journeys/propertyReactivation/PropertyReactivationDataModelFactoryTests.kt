package uk.gov.communities.prsdb.webapp.journeys.propertyReactivation

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.constants.enums.FurnishedStatus
import uk.gov.communities.prsdb.webapp.constants.enums.LicensingType
import uk.gov.communities.prsdb.webapp.constants.enums.RentFrequency
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.FurnishedStatusStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.HouseholdStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.RentAmountStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.RentFrequencyStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.TenantsStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.tasks.HouseholdsAndTenantsTask
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.tasks.LicensingTask
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.tasks.RentFrequencyAndAmountTask
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.tasks.RentIncludesBillsTask
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.tasks.TenancyDetailsTask
import uk.gov.communities.prsdb.webapp.models.dataModels.PropertyReactivationDataModel
import uk.gov.communities.prsdb.webapp.models.dataModels.ReactivationLicensingDataModel
import uk.gov.communities.prsdb.webapp.models.dataModels.ReactivationTenancyDetailsDataModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.FurnishedStatusFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NewNumberOfPeopleFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NumberOfHouseholdsFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.RentAmountFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.RentFrequencyFormModel
import java.math.BigDecimal

@ExtendWith(MockitoExtension::class)
class PropertyReactivationDataModelFactoryTests {
    @Mock
    private lateinit var mockState: PropertyReactivationJourneyState

    private lateinit var factory: PropertyReactivationDataModelFactory

    @BeforeEach
    fun setUp() {
        factory = PropertyReactivationDataModelFactory()
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
        setUpLicensing(LicensingType.SELECTIVE_LICENCE, "SL-123")

        // Act
        val result = factory.fromJourneyState(mockState)

        // Assert
        assertEquals(
            PropertyReactivationDataModel(licensing = ReactivationLicensingDataModel(LicensingType.SELECTIVE_LICENCE, "SL-123")),
            result,
        )
    }

    @Test
    fun `fromJourneyState throws when licensing is to be provided later`() {
        // Arrange
        whenever(mockState.requiredTaskTypes).thenReturn(listOf(ReactivationTaskType.LICENSING))
        setUpLicensing(LicensingType.PROVIDE_LATER, licenceNumber = null, stubLicenceNumber = false)

        // Act & Assert
        assertThrows<IllegalStateException> { factory.fromJourneyState(mockState) }
    }

    @Test
    fun `fromJourneyState maps the tenancy details answers when tenancy details were required`() {
        // Arrange
        whenever(mockState.requiredTaskTypes).thenReturn(listOf(ReactivationTaskType.TENANCY_DETAILS))
        val mockTenancyDetailsTask = mock<TenancyDetailsTask>()
        whenever(mockState.tenancyDetailsTask).thenReturn(mockTenancyDetailsTask)
        val mockHouseholdsAndTenantsTask = mock<HouseholdsAndTenantsTask>()
        val mockHouseholdStep = mock<HouseholdStep>()
        val mockTenantsStep = mock<TenantsStep>()
        whenever(mockTenancyDetailsTask.householdsAndTenantsTask).thenReturn(mockHouseholdsAndTenantsTask)
        whenever(mockHouseholdsAndTenantsTask.households).thenReturn(mockHouseholdStep)
        whenever(mockHouseholdsAndTenantsTask.tenants).thenReturn(mockTenantsStep)
        whenever(mockHouseholdStep.formModel).thenReturn(NumberOfHouseholdsFormModel().apply { numberOfHouseholds = "2" })
        whenever(mockTenantsStep.formModel).thenReturn(NewNumberOfPeopleFormModel().apply { numberOfPeople = "4" })
        whenever(mockTenancyDetailsTask.rentIncludesBillsTask).thenReturn(mock<RentIncludesBillsTask>())
        val mockFurnishedStatusStep = mock<FurnishedStatusStep>()
        whenever(mockTenancyDetailsTask.furnishedStatus).thenReturn(mockFurnishedStatusStep)
        whenever(mockFurnishedStatusStep.formModel)
            .thenReturn(FurnishedStatusFormModel().apply { furnishedStatus = FurnishedStatus.FURNISHED })
        val mockRentFrequencyAndAmountTask = mock<RentFrequencyAndAmountTask>()
        val mockRentFrequencyStep = mock<RentFrequencyStep>()
        val mockRentAmountStep = mock<RentAmountStep>()
        whenever(mockTenancyDetailsTask.rentFrequencyAndAmountTask).thenReturn(mockRentFrequencyAndAmountTask)
        whenever(mockRentFrequencyAndAmountTask.rentFrequency).thenReturn(mockRentFrequencyStep)
        whenever(mockRentFrequencyAndAmountTask.rentAmount).thenReturn(mockRentAmountStep)
        whenever(mockRentFrequencyStep.formModel).thenReturn(RentFrequencyFormModel().apply { rentFrequency = RentFrequency.MONTHLY })
        whenever(mockRentAmountStep.formModel).thenReturn(RentAmountFormModel().apply { rentAmount = "400.50" })

        // Act
        val result = factory.fromJourneyState(mockState)

        // Assert
        assertEquals(
            PropertyReactivationDataModel(
                tenancyDetails =
                    ReactivationTenancyDetailsDataModel(
                        numberOfHouseholds = 2,
                        numberOfPeople = 4,
                        billsIncludedList = null,
                        customBillsIncluded = null,
                        furnishedStatus = FurnishedStatus.FURNISHED,
                        rentFrequency = RentFrequency.MONTHLY,
                        customRentFrequency = null,
                        rentAmount = BigDecimal("400.50"),
                    ),
            ),
            result,
        )
    }

    private fun setUpLicensing(
        licensingType: LicensingType,
        licenceNumber: String?,
        stubLicenceNumber: Boolean = true,
    ) {
        val mockLicensingTask = mock<LicensingTask>()
        whenever(mockState.licensingTask).thenReturn(mockLicensingTask)
        whenever(mockLicensingTask.getLicensingType()).thenReturn(licensingType)
        if (stubLicenceNumber) {
            whenever(mockLicensingTask.getLicenceNumberOrNull()).thenReturn(licenceNumber)
        }
    }
}
