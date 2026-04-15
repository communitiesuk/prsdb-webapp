package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.EpcState

class CheckEpcAnswersStepConfigTests {
    private val mockState: EpcState = mock()
    private val mockHasEpcStep: HasEpcStep = mock()
    private val mockIsEpcRequiredStep: IsEpcRequiredStep = mock()
    private val mockEpcExemptionStep: EpcExemptionStep = mock()
    private val mockEpcAgeCheckStep: EpcAgeCheckStep = mock()
    private val mockEpcEnergyRatingCheckStep: EpcEnergyRatingCheckStep = mock()
    private val mockMeesExemptionStep: MeesExemptionStep = mock()
    private val mockEpcInDateAtStartOfTenancyCheckStep: EpcInDateAtStartOfTenancyCheckStep = mock()

    private val stepConfig = CheckEpcAnswersStepConfig(mock())

    @BeforeEach
    fun setupCommonMocks() {
        whenever(mockState.isEpcRequiredStep).thenReturn(mockIsEpcRequiredStep)
    }

    @Nested
    inner class WhenProvideLater {
        @BeforeEach
        fun setup() {
            whenever(mockState.hasEpcStep).thenReturn(mockHasEpcStep)
            whenever(mockHasEpcStep.outcome).thenReturn(HasEpcMode.PROVIDE_LATER)
        }

        @Test
        fun `returns SKIPPED_OCCUPIED when occupied`() {
            whenever(mockState.isOccupied).thenReturn(true)
            assertEquals(EpcScenario.SKIPPED_OCCUPIED, stepConfig.determineScenario(mockState))
        }

        @Test
        fun `returns SKIPPED_UNOCCUPIED when not occupied`() {
            whenever(mockState.isOccupied).thenReturn(false)
            assertEquals(EpcScenario.SKIPPED_UNOCCUPIED, stepConfig.determineScenario(mockState))
        }
    }

    @Nested
    inner class WhenNoEpcPath {
        @BeforeEach
        fun setup() {
            whenever(mockState.hasEpcStep).thenReturn(mockHasEpcStep)
            whenever(mockHasEpcStep.outcome).thenReturn(HasEpcMode.NO_EPC)
            whenever(mockState.isEpcRequiredStep).thenReturn(mockIsEpcRequiredStep)
            whenever(mockIsEpcRequiredStep.isStepReachable).thenReturn(true)
            whenever(mockState.epcExemptionStep).thenReturn(mockEpcExemptionStep)
        }

        @Test
        fun `returns NO_EPC_EXEMPT when exemption step is reachable`() {
            whenever(mockEpcExemptionStep.isStepReachable).thenReturn(true)
            assertEquals(EpcScenario.NO_EPC_EXEMPT, stepConfig.determineScenario(mockState))
        }

        @Test
        fun `returns NO_EPC_NO_EXEMPTION_OCCUPIED when occupied and exemption step is not reachable`() {
            whenever(mockState.isOccupied).thenReturn(true)
            assertEquals(EpcScenario.NO_EPC_NO_EXEMPTION_OCCUPIED, stepConfig.determineScenario(mockState))
        }

        @Test
        fun `returns NO_EPC_NO_EXEMPTION_UNOCCUPIED when not occupied and exemption step is not reachable`() {
            whenever(mockState.isOccupied).thenReturn(false)
            assertEquals(EpcScenario.NO_EPC_NO_EXEMPTION_UNOCCUPIED, stepConfig.determineScenario(mockState))
        }

        @Test
        fun `returns no EPC scenario when hasEpcStep outcome is HAS_EPC but isEpcRequiredStep is reachable`() {
            whenever(mockHasEpcStep.outcome).thenReturn(HasEpcMode.HAS_EPC)
            whenever(mockState.isOccupied).thenReturn(true)
            assertEquals(EpcScenario.NO_EPC_NO_EXEMPTION_OCCUPIED, stepConfig.determineScenario(mockState))
        }
    }

    @Nested
    inner class WhenEpcPresentAndNotExpired {
        @BeforeEach
        fun setup() {
            whenever(mockState.hasEpcStep).thenReturn(mockHasEpcStep)
            whenever(mockHasEpcStep.outcome).thenReturn(HasEpcMode.HAS_EPC)
            whenever(mockState.epcAgeCheckStep).thenReturn(mockEpcAgeCheckStep)
            whenever(mockState.epcEnergyRatingCheckStep).thenReturn(mockEpcEnergyRatingCheckStep)
        }

        @Test
        fun `returns VALID_EPC when energy rating is not low`() {
            assertEquals(EpcScenario.VALID_EPC, stepConfig.determineScenario(mockState))
        }

        @Test
        fun `returns LOW_ENERGY_EPC_MEES_EXEMPTION when energy rating is low and MEES exemption step is reachable`() {
            whenever(mockEpcEnergyRatingCheckStep.outcome).thenReturn(EpcEnergyRatingCheckMode.EPC_LOW_ENERGY_RATING)
            whenever(mockState.meesExemptionStep).thenReturn(mockMeesExemptionStep)
            whenever(mockMeesExemptionStep.isStepReachable).thenReturn(true)
            assertEquals(EpcScenario.LOW_ENERGY_EPC_MEES_EXEMPTION, stepConfig.determineScenario(mockState))
        }

        @Test
        fun `returns LOW_ENERGY_EPC_NO_EXEMPTION_OCCUPIED when energy rating is low, no exemption, and occupied`() {
            whenever(mockEpcEnergyRatingCheckStep.outcome).thenReturn(EpcEnergyRatingCheckMode.EPC_LOW_ENERGY_RATING)
            whenever(mockState.meesExemptionStep).thenReturn(mockMeesExemptionStep)
            whenever(mockState.isOccupied).thenReturn(true)
            assertEquals(EpcScenario.LOW_ENERGY_EPC_NO_EXEMPTION_OCCUPIED, stepConfig.determineScenario(mockState))
        }

        @Test
        fun `returns LOW_ENERGY_EPC_NO_EXEMPTION_UNOCCUPIED when energy rating is low, no exemption, and not occupied`() {
            whenever(mockEpcEnergyRatingCheckStep.outcome).thenReturn(EpcEnergyRatingCheckMode.EPC_LOW_ENERGY_RATING)
            whenever(mockState.meesExemptionStep).thenReturn(mockMeesExemptionStep)
            whenever(mockState.isOccupied).thenReturn(false)
            assertEquals(EpcScenario.LOW_ENERGY_EPC_NO_EXEMPTION_UNOCCUPIED, stepConfig.determineScenario(mockState))
        }
    }

    @Nested
    inner class WhenEpcPresentAndExpired {
        @BeforeEach
        fun setup() {
            whenever(mockState.hasEpcStep).thenReturn(mockHasEpcStep)
            whenever(mockHasEpcStep.outcome).thenReturn(HasEpcMode.HAS_EPC)
            whenever(mockState.epcAgeCheckStep).thenReturn(mockEpcAgeCheckStep)
            whenever(mockEpcAgeCheckStep.outcome).thenReturn(EpcAgeCheckMode.EPC_OLDER_THAN_10_YEARS)
        }

        @Test
        fun `returns EPC_EXPIRED_UNOCCUPIED when not occupied`() {
            whenever(mockState.isOccupied).thenReturn(false)
            assertEquals(EpcScenario.EPC_EXPIRED_UNOCCUPIED, stepConfig.determineScenario(mockState))
        }

        @Test
        fun `returns EPC_EXPIRED_NOT_IN_DATE_OCCUPIED when occupied and EPC was not in date at tenancy start`() {
            whenever(mockState.isOccupied).thenReturn(true)
            whenever(mockState.epcInDateAtStartOfTenancyCheckStep).thenReturn(mockEpcInDateAtStartOfTenancyCheckStep)
            whenever(mockEpcInDateAtStartOfTenancyCheckStep.outcome)
                .thenReturn(EpcInDateAtStartOfTenancyCheckMode.NOT_IN_DATE)
            assertEquals(EpcScenario.EPC_EXPIRED_NOT_IN_DATE_OCCUPIED, stepConfig.determineScenario(mockState))
        }

        @Test
        fun `returns EPC_EXPIRED_IN_DATE_OCCUPIED when occupied, in date at tenancy start, and energy rating is not low`() {
            whenever(mockState.isOccupied).thenReturn(true)
            whenever(mockState.epcInDateAtStartOfTenancyCheckStep).thenReturn(mockEpcInDateAtStartOfTenancyCheckStep)
            whenever(mockEpcInDateAtStartOfTenancyCheckStep.outcome).thenReturn(EpcInDateAtStartOfTenancyCheckMode.IN_DATE)
            whenever(mockState.epcEnergyRatingCheckStep).thenReturn(mockEpcEnergyRatingCheckStep)
            assertEquals(EpcScenario.EPC_EXPIRED_IN_DATE_OCCUPIED, stepConfig.determineScenario(mockState))
        }

        @Test
        fun `returns LOW_ENERGY_EPC_EXPIRED_IN_DATE_MEES_EXEMPTION_OCCUPIED when occupied, in date, low rating, and exemption reachable`() {
            whenever(mockState.isOccupied).thenReturn(true)
            whenever(mockState.epcInDateAtStartOfTenancyCheckStep).thenReturn(mockEpcInDateAtStartOfTenancyCheckStep)
            whenever(mockEpcInDateAtStartOfTenancyCheckStep.outcome).thenReturn(EpcInDateAtStartOfTenancyCheckMode.IN_DATE)
            whenever(mockState.epcEnergyRatingCheckStep).thenReturn(mockEpcEnergyRatingCheckStep)
            whenever(mockEpcEnergyRatingCheckStep.outcome).thenReturn(EpcEnergyRatingCheckMode.EPC_LOW_ENERGY_RATING)
            whenever(mockState.meesExemptionStep).thenReturn(mockMeesExemptionStep)
            whenever(mockMeesExemptionStep.isStepReachable).thenReturn(true)
            assertEquals(
                EpcScenario.LOW_ENERGY_EPC_EXPIRED_IN_DATE_MEES_EXEMPTION_OCCUPIED,
                stepConfig.determineScenario(mockState),
            )
        }

        @Test
        fun `returns LOW_ENERGY_EPC_EXPIRED_IN_DATE_NO_EXEMPTION_OCCUPIED when occupied, in date, low rating, and no exemption`() {
            whenever(mockState.isOccupied).thenReturn(true)
            whenever(mockState.epcInDateAtStartOfTenancyCheckStep).thenReturn(mockEpcInDateAtStartOfTenancyCheckStep)
            whenever(mockEpcInDateAtStartOfTenancyCheckStep.outcome).thenReturn(EpcInDateAtStartOfTenancyCheckMode.IN_DATE)
            whenever(mockState.epcEnergyRatingCheckStep).thenReturn(mockEpcEnergyRatingCheckStep)
            whenever(mockEpcEnergyRatingCheckStep.outcome).thenReturn(EpcEnergyRatingCheckMode.EPC_LOW_ENERGY_RATING)
            whenever(mockState.meesExemptionStep).thenReturn(mockMeesExemptionStep)
            assertEquals(
                EpcScenario.LOW_ENERGY_EPC_EXPIRED_IN_DATE_NO_EXEMPTION_OCCUPIED,
                stepConfig.determineScenario(mockState),
            )
        }
    }
}
