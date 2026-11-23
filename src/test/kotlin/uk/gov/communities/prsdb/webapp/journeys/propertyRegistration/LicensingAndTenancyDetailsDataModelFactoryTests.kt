package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.constants.enums.BillsIncluded
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
import uk.gov.communities.prsdb.webapp.models.dataModels.BillsIncludedDataModel
import uk.gov.communities.prsdb.webapp.models.dataModels.LicensingDetailsDataModel
import uk.gov.communities.prsdb.webapp.models.dataModels.TenancyDetailsDataModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.FurnishedStatusFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NewNumberOfPeopleFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NumberOfHouseholdsFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.RentAmountFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.RentFrequencyFormModel
import java.math.BigDecimal

@ExtendWith(MockitoExtension::class)
class LicensingAndTenancyDetailsDataModelFactoryTests {
    private val factory = LicensingAndTenancyDetailsDataModelFactory()

    @Nested
    inner class FromLicensingState {
        @Mock
        private lateinit var mockLicensingTask: LicensingTask

        @Test
        fun `maps the licensing type and licence number`() {
            // Arrange
            whenever(mockLicensingTask.getLicensingType()).thenReturn(LicensingType.SELECTIVE_LICENCE)
            whenever(mockLicensingTask.getLicenceNumberOrNull()).thenReturn("SL-123")

            // Act
            val result = factory.fromLicensingState(mockLicensingTask)

            // Assert
            assertEquals(LicensingDetailsDataModel(LicensingType.SELECTIVE_LICENCE, "SL-123"), result)
        }

        @Test
        fun `throws when licensing is to be provided later`() {
            // Arrange
            whenever(mockLicensingTask.getLicensingType()).thenReturn(LicensingType.PROVIDE_LATER)

            // Act & Assert
            assertThrows<IllegalStateException> { factory.fromLicensingState(mockLicensingTask) }
        }
    }

    @Nested
    inner class FromTenancyDetailsState {
        @Mock
        private lateinit var mockTenancyDetailsTask: TenancyDetailsTask

        @Mock
        private lateinit var mockHouseholdsAndTenantsTask: HouseholdsAndTenantsTask

        @Mock
        private lateinit var mockHouseholdStep: HouseholdStep

        @Mock
        private lateinit var mockTenantsStep: TenantsStep

        @Mock
        private lateinit var mockRentIncludesBillsTask: RentIncludesBillsTask

        @Mock
        private lateinit var mockFurnishedStatusStep: FurnishedStatusStep

        @Mock
        private lateinit var mockRentFrequencyAndAmountTask: RentFrequencyAndAmountTask

        @Mock
        private lateinit var mockRentFrequencyStep: RentFrequencyStep

        @Mock
        private lateinit var mockRentAmountStep: RentAmountStep

        @BeforeEach
        fun setUp() {
            whenever(mockTenancyDetailsTask.householdsAndTenantsTask).thenReturn(mockHouseholdsAndTenantsTask)
            whenever(mockHouseholdsAndTenantsTask.households).thenReturn(mockHouseholdStep)
            whenever(mockHouseholdsAndTenantsTask.tenants).thenReturn(mockTenantsStep)
            whenever(mockHouseholdStep.formModel).thenReturn(NumberOfHouseholdsFormModel().apply { numberOfHouseholds = "2" })
            whenever(mockTenantsStep.formModel).thenReturn(NewNumberOfPeopleFormModel().apply { numberOfPeople = "4" })
            whenever(mockTenancyDetailsTask.rentIncludesBillsTask).thenReturn(mockRentIncludesBillsTask)
            whenever(mockTenancyDetailsTask.furnishedStatus).thenReturn(mockFurnishedStatusStep)
            whenever(mockFurnishedStatusStep.formModel)
                .thenReturn(FurnishedStatusFormModel().apply { furnishedStatus = FurnishedStatus.FURNISHED })
            whenever(mockTenancyDetailsTask.rentFrequencyAndAmountTask).thenReturn(mockRentFrequencyAndAmountTask)
            whenever(mockRentFrequencyAndAmountTask.rentFrequency).thenReturn(mockRentFrequencyStep)
            whenever(mockRentFrequencyAndAmountTask.rentAmount).thenReturn(mockRentAmountStep)
            whenever(mockRentAmountStep.formModel).thenReturn(RentAmountFormModel().apply { rentAmount = "400.50" })
        }

        @Test
        fun `maps the tenancy details answers`() {
            // Arrange
            whenever(mockRentIncludesBillsTask.getBillsIncludedOrNull())
                .thenReturn(BillsIncludedDataModel("GAS,WATER", listOf(BillsIncluded.GAS, BillsIncluded.WATER), "Cleaning"))
            whenever(mockRentFrequencyStep.formModel).thenReturn(RentFrequencyFormModel().apply { rentFrequency = RentFrequency.OTHER })
            whenever(mockRentFrequencyAndAmountTask.getCustomRentFrequencyIfSelected()).thenReturn("Fortnightly")

            // Act
            val result = factory.fromTenancyDetailsState(mockTenancyDetailsTask)

            // Assert
            assertEquals(
                TenancyDetailsDataModel(
                    numberOfHouseholds = 2,
                    numberOfPeople = 4,
                    billsIncludedList = "GAS,WATER",
                    customBillsIncluded = "Cleaning",
                    furnishedStatus = FurnishedStatus.FURNISHED,
                    rentFrequency = RentFrequency.OTHER,
                    customRentFrequency = "Fortnightly",
                    rentAmount = BigDecimal("400.50"),
                ),
                result,
            )
        }

        @Test
        fun `leaves the bills empty when rent does not include bills`() {
            // Arrange
            whenever(mockRentIncludesBillsTask.getBillsIncludedOrNull()).thenReturn(null)
            whenever(mockRentFrequencyStep.formModel).thenReturn(RentFrequencyFormModel().apply { rentFrequency = RentFrequency.MONTHLY })

            // Act
            val result = factory.fromTenancyDetailsState(mockTenancyDetailsTask)

            // Assert
            assertEquals(null, result.billsIncludedList)
            assertEquals(null, result.customBillsIncluded)
        }
    }
}
