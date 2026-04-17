package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.constants.enums.EpcExemptionReason
import uk.gov.communities.prsdb.webapp.constants.enums.MeesExemptionReason
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.EpcState
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.EpcAgeCheckMode
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.EpcAgeCheckStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.EpcEnergyRatingCheckMode
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.EpcEnergyRatingCheckStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.EpcExemptionStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.EpcInDateAtStartOfTenancyCheckMode
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.EpcInDateAtStartOfTenancyCheckStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.EpcScenario
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.HasEpcMode
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.HasEpcStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.HasMeesExemptionStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.IsEpcRequiredStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.MeesExemptionStep
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.EpcExemptionFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.EpcInDateAtStartOfTenancyCheckFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.IsEpcRequiredFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.MeesExemptionCheckFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.MeesExemptionReasonFormModel
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.SummaryListRowViewModel
import uk.gov.communities.prsdb.webapp.services.EpcCertificateUrlProvider
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockEpcData
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class EpcRegistrationCyaSummaryRowsFactoryTests {
    private val mockEpcCertificateUrlProvider: EpcCertificateUrlProvider = mock()

    private val mockHasEpcStep: HasEpcStep = mock()
    private val mockEpcAgeCheckStep: EpcAgeCheckStep = mock()
    private val mockEpcEnergyRatingCheckStep: EpcEnergyRatingCheckStep = mock()
    private val mockEpcInDateAtStartOfTenancyCheckStep: EpcInDateAtStartOfTenancyCheckStep = mock()
    private val mockHasMeesExemptionStep: HasMeesExemptionStep = mock()
    private val mockMeesExemptionStep: MeesExemptionStep = mock()
    private val mockIsEpcRequiredStep: IsEpcRequiredStep = mock()
    private val mockEpcExemptionStep: EpcExemptionStep = mock()
    private val mockState: EpcState = mock()

    private val epcUrl = "https://find-energy-certificate.service.gov.uk/energy-certificate/0000-0000-0000-0892-1563"
    private val validEpc = MockEpcData.createEpcDataModel(energyRating = "C")
    private val lowRatingEpc = MockEpcData.createEpcDataModel(energyRating = "F")

    @BeforeEach
    fun setupMocks() {
        whenever(mockState.hasEpcStep).thenReturn(mockHasEpcStep)
        whenever(mockState.epcAgeCheckStep).thenReturn(mockEpcAgeCheckStep)
        whenever(mockState.epcEnergyRatingCheckStep).thenReturn(mockEpcEnergyRatingCheckStep)
        whenever(mockState.epcInDateAtStartOfTenancyCheckStep).thenReturn(mockEpcInDateAtStartOfTenancyCheckStep)
        whenever(mockState.hasMeesExemptionStep).thenReturn(mockHasMeesExemptionStep)
        whenever(mockState.meesExemptionStep).thenReturn(mockMeesExemptionStep)
        whenever(mockState.isEpcRequiredStep).thenReturn(mockIsEpcRequiredStep)
        whenever(mockState.epcExemptionStep).thenReturn(mockEpcExemptionStep)

        whenever(mockHasEpcStep.routeSegment).thenReturn(HasEpcStep.ROUTE_SEGMENT)
        whenever(mockEpcInDateAtStartOfTenancyCheckStep.routeSegment).thenReturn(EpcInDateAtStartOfTenancyCheckStep.ROUTE_SEGMENT)
        whenever(mockHasMeesExemptionStep.routeSegment).thenReturn(HasMeesExemptionStep.ROUTE_SEGMENT)
        whenever(mockMeesExemptionStep.routeSegment).thenReturn(MeesExemptionStep.ROUTE_SEGMENT)
        whenever(mockIsEpcRequiredStep.routeSegment).thenReturn(IsEpcRequiredStep.ROUTE_SEGMENT)
        whenever(mockEpcExemptionStep.routeSegment).thenReturn(EpcExemptionStep.ROUTE_SEGMENT)
        whenever(mockState.journeyId).thenReturn("")

        whenever(mockHasEpcStep.currentJourneyId).thenReturn("")
        whenever(mockEpcInDateAtStartOfTenancyCheckStep.currentJourneyId).thenReturn("")
        whenever(mockHasMeesExemptionStep.currentJourneyId).thenReturn("")
        whenever(mockMeesExemptionStep.currentJourneyId).thenReturn("")
        whenever(mockIsEpcRequiredStep.currentJourneyId).thenReturn("")
        whenever(mockEpcExemptionStep.currentJourneyId).thenReturn("")
    }

    private fun setupStateForScenario(scenario: EpcScenario) {
        when (scenario) {
            EpcScenario.SKIPPED_OCCUPIED -> {
                whenever(mockHasEpcStep.outcome).thenReturn(HasEpcMode.PROVIDE_LATER)
                whenever(mockState.isOccupied).thenReturn(true)
            }

            EpcScenario.SKIPPED_UNOCCUPIED -> {
                whenever(mockHasEpcStep.outcome).thenReturn(HasEpcMode.PROVIDE_LATER)
                whenever(mockState.isOccupied).thenReturn(false)
            }

            EpcScenario.NO_EPC_EXEMPT -> {
                whenever(mockState.acceptedEpc).thenReturn(null)
                whenever(mockEpcExemptionStep.outcome).thenReturn(Complete.COMPLETE)
            }

            EpcScenario.NO_EPC_NO_EXEMPTION_OCCUPIED -> {
                whenever(mockState.acceptedEpc).thenReturn(null)
                whenever(mockState.isOccupied).thenReturn(true)
            }

            EpcScenario.NO_EPC_NO_EXEMPTION_UNOCCUPIED -> {
                whenever(mockState.acceptedEpc).thenReturn(null)
                whenever(mockState.isOccupied).thenReturn(false)
            }

            EpcScenario.VALID_EPC -> {
                whenever(mockState.acceptedEpc).thenReturn(validEpc)
                whenever(mockEpcAgeCheckStep.outcome).thenReturn(EpcAgeCheckMode.EPC_10_YEARS_OR_NEWER)
                whenever(mockEpcEnergyRatingCheckStep.outcome).thenReturn(EpcEnergyRatingCheckMode.EPC_MEETS_ENERGY_REQUIREMENTS)
            }

            EpcScenario.LOW_ENERGY_EPC_MEES_EXEMPTION -> {
                whenever(mockState.acceptedEpc).thenReturn(validEpc)
                whenever(mockEpcAgeCheckStep.outcome).thenReturn(EpcAgeCheckMode.EPC_10_YEARS_OR_NEWER)
                whenever(mockEpcEnergyRatingCheckStep.outcome).thenReturn(EpcEnergyRatingCheckMode.EPC_LOW_ENERGY_RATING)
                whenever(mockMeesExemptionStep.outcome).thenReturn(Complete.COMPLETE)
            }

            EpcScenario.LOW_ENERGY_EPC_NO_EXEMPTION_OCCUPIED -> {
                whenever(mockState.acceptedEpc).thenReturn(validEpc)
                whenever(mockEpcAgeCheckStep.outcome).thenReturn(EpcAgeCheckMode.EPC_10_YEARS_OR_NEWER)
                whenever(mockEpcEnergyRatingCheckStep.outcome).thenReturn(EpcEnergyRatingCheckMode.EPC_LOW_ENERGY_RATING)
                whenever(mockState.isOccupied).thenReturn(true)
            }

            EpcScenario.LOW_ENERGY_EPC_NO_EXEMPTION_UNOCCUPIED -> {
                whenever(mockState.acceptedEpc).thenReturn(validEpc)
                whenever(mockEpcAgeCheckStep.outcome).thenReturn(EpcAgeCheckMode.EPC_10_YEARS_OR_NEWER)
                whenever(mockEpcEnergyRatingCheckStep.outcome).thenReturn(EpcEnergyRatingCheckMode.EPC_LOW_ENERGY_RATING)
                whenever(mockState.isOccupied).thenReturn(false)
            }

            EpcScenario.EPC_EXPIRED_UNOCCUPIED -> {
                whenever(mockState.acceptedEpc).thenReturn(validEpc)
                whenever(mockEpcAgeCheckStep.outcome).thenReturn(EpcAgeCheckMode.EPC_OLDER_THAN_10_YEARS)
                whenever(mockState.isOccupied).thenReturn(false)
            }

            EpcScenario.EPC_EXPIRED_NOT_IN_DATE_OCCUPIED -> {
                whenever(mockState.acceptedEpc).thenReturn(validEpc)
                whenever(mockEpcAgeCheckStep.outcome).thenReturn(EpcAgeCheckMode.EPC_OLDER_THAN_10_YEARS)
                whenever(mockState.isOccupied).thenReturn(true)
                whenever(mockEpcInDateAtStartOfTenancyCheckStep.outcome).thenReturn(EpcInDateAtStartOfTenancyCheckMode.NOT_IN_DATE)
            }

            EpcScenario.EPC_EXPIRED_IN_DATE_OCCUPIED -> {
                whenever(mockState.acceptedEpc).thenReturn(validEpc)
                whenever(mockEpcAgeCheckStep.outcome).thenReturn(EpcAgeCheckMode.EPC_OLDER_THAN_10_YEARS)
                whenever(mockState.isOccupied).thenReturn(true)
                whenever(mockEpcInDateAtStartOfTenancyCheckStep.outcome).thenReturn(EpcInDateAtStartOfTenancyCheckMode.IN_DATE)
                whenever(mockEpcEnergyRatingCheckStep.outcome).thenReturn(EpcEnergyRatingCheckMode.EPC_MEETS_ENERGY_REQUIREMENTS)
            }

            EpcScenario.LOW_ENERGY_EPC_EXPIRED_IN_DATE_MEES_EXEMPTION_OCCUPIED -> {
                whenever(mockState.acceptedEpc).thenReturn(validEpc)
                whenever(mockEpcAgeCheckStep.outcome).thenReturn(EpcAgeCheckMode.EPC_OLDER_THAN_10_YEARS)
                whenever(mockState.isOccupied).thenReturn(true)
                whenever(mockEpcInDateAtStartOfTenancyCheckStep.outcome).thenReturn(EpcInDateAtStartOfTenancyCheckMode.IN_DATE)
                whenever(mockEpcEnergyRatingCheckStep.outcome).thenReturn(EpcEnergyRatingCheckMode.EPC_LOW_ENERGY_RATING)
                whenever(mockMeesExemptionStep.outcome).thenReturn(Complete.COMPLETE)
            }

            EpcScenario.LOW_ENERGY_EPC_EXPIRED_IN_DATE_NO_EXEMPTION_OCCUPIED -> {
                whenever(mockState.acceptedEpc).thenReturn(validEpc)
                whenever(mockEpcAgeCheckStep.outcome).thenReturn(EpcAgeCheckMode.EPC_OLDER_THAN_10_YEARS)
                whenever(mockState.isOccupied).thenReturn(true)
                whenever(mockEpcInDateAtStartOfTenancyCheckStep.outcome).thenReturn(EpcInDateAtStartOfTenancyCheckMode.IN_DATE)
                whenever(mockEpcEnergyRatingCheckStep.outcome).thenReturn(EpcEnergyRatingCheckMode.EPC_LOW_ENERGY_RATING)
            }
        }
    }

    @Test
    fun `createEpcCardTitle returns heading key when scenario is an EPC card scenario`() {
        // Arrange
        setupStateForScenario(EpcScenario.VALID_EPC)

        // Act
        val title = EpcRegistrationCyaSummaryRowsFactory(mockEpcCertificateUrlProvider, mockState).createEpcCardTitle()

        // Assert
        assertEquals("propertyCompliance.epcTask.checkEpcAnswers.epc.yourEpc", title)
    }

    @Test
    fun `createEpcCardTitle returns null when scenario is not an EPC card scenario`() {
        // Arrange
        setupStateForScenario(EpcScenario.SKIPPED_OCCUPIED)

        // Act
        val title = EpcRegistrationCyaSummaryRowsFactory(mockEpcCertificateUrlProvider, mockState).createEpcCardTitle()

        // Assert
        assertNull(title)
    }

    @Test
    fun `createEpcCardRows returns rows for unexpired compliant EPC`() {
        // Arrange
        setupStateForScenario(EpcScenario.VALID_EPC)
        whenever(mockEpcCertificateUrlProvider.getEpcCertificateUrl(validEpc.certificateNumber)).thenReturn(epcUrl)

        val expectedRows =
            listOf(
                SummaryListRowViewModel.forCheckYourAnswersPage(
                    "propertyCompliance.epcTask.checkEpcAnswers.epc.address",
                    validEpc.singleLineAddress,
                    null as String?,
                ),
                SummaryListRowViewModel.forCheckYourAnswersPage(
                    "propertyCompliance.epcTask.checkEpcAnswers.epc.energyRating",
                    validEpc.energyRatingUppercase,
                    null as String?,
                ),
                SummaryListRowViewModel.forCheckYourAnswersPage(
                    "propertyCompliance.epcTask.checkEpcAnswers.epc.expiryDate",
                    validEpc.expiryDateAsJavaLocalDate,
                    null as String?,
                ),
                SummaryListRowViewModel.forCheckYourAnswersPage(
                    "propertyCompliance.epcTask.checkEpcAnswers.epc.certificateNumber",
                    validEpc.certificateNumber,
                    null as String?,
                ),
            )

        // Act
        val rows = EpcRegistrationCyaSummaryRowsFactory(mockEpcCertificateUrlProvider, mockState).createEpcCardRows()

        // Assert
        assertEquals(expectedRows, rows)
    }

    @Test
    fun `createEpcCardRows returns null when scenario is not an EPC card scenario`() {
        // Arrange
        setupStateForScenario(EpcScenario.SKIPPED_OCCUPIED)

        // Act
        val rows = EpcRegistrationCyaSummaryRowsFactory(mockEpcCertificateUrlProvider, mockState).createEpcCardRows()

        // Assert
        assertNull(rows)
    }

    @Test
    fun `createEpcCardActions returns view action when card is visible`() {
        // Arrange
        setupStateForScenario(EpcScenario.VALID_EPC)
        whenever(mockEpcCertificateUrlProvider.getEpcCertificateUrl(validEpc.certificateNumber)).thenReturn(epcUrl)

        // Act
        val actions = EpcRegistrationCyaSummaryRowsFactory(mockEpcCertificateUrlProvider, mockState).createEpcCardActions()

        // Assert
        assertEquals("propertyCompliance.epcTask.checkEpcAnswers.epc.viewFullEpc", actions?.first()?.text)
        assertEquals(epcUrl, actions?.first()?.url)
        assertTrue(actions?.first()?.opensInNewTab ?: false)
    }

    @Test
    fun `createEpcCardActions returns null when scenario is not an EPC card scenario`() {
        // Arrange
        setupStateForScenario(EpcScenario.SKIPPED_OCCUPIED)

        // Act
        val actions = EpcRegistrationCyaSummaryRowsFactory(mockEpcCertificateUrlProvider, mockState).createEpcCardActions()

        // Assert
        assertNull(actions)
    }

    @Test
    fun `getEpcExpiredTextKey returns expired key when scenario is expired`() {
        // Arrange
        setupStateForScenario(EpcScenario.EPC_EXPIRED_IN_DATE_OCCUPIED)

        // Act
        val result = EpcRegistrationCyaSummaryRowsFactory(mockEpcCertificateUrlProvider, mockState).getEpcExpiredTextKey()

        // Assert
        assertEquals("propertyCompliance.epcTask.checkEpcAnswers.epc.expired", result)
    }

    @Test
    fun `getEpcExpiredTextKey returns expired key for LOW_ENERGY_EPC_EXPIRED_IN_DATE_MEES_EXEMPTION_OCCUPIED scenario`() {
        // Arrange
        setupStateForScenario(EpcScenario.LOW_ENERGY_EPC_EXPIRED_IN_DATE_MEES_EXEMPTION_OCCUPIED)

        // Act
        val result = EpcRegistrationCyaSummaryRowsFactory(mockEpcCertificateUrlProvider, mockState).getEpcExpiredTextKey()

        // Assert
        assertEquals("propertyCompliance.epcTask.checkEpcAnswers.epc.expired", result)
    }

    @Test
    fun `getEpcExpiredTextKey returns null when scenario is not expired`() {
        // Arrange
        setupStateForScenario(EpcScenario.VALID_EPC)

        // Act
        val result = EpcRegistrationCyaSummaryRowsFactory(mockEpcCertificateUrlProvider, mockState).getEpcExpiredTextKey()

        // Assert
        assertNull(result)
    }

    @Test
    fun `createTenancyCheckRows returns tenancy row when EPC was in date at start of tenancy`() {
        // Arrange
        setupStateForScenario(EpcScenario.EPC_EXPIRED_IN_DATE_OCCUPIED)
        val formModel = EpcInDateAtStartOfTenancyCheckFormModel().apply { tenancyStartedBeforeExpiry = true }
        whenever(mockEpcInDateAtStartOfTenancyCheckStep.formModelIfReachableOrNull).thenReturn(formModel)

        val expectedRows =
            listOf(
                SummaryListRowViewModel.forCheckYourAnswersPage(
                    "propertyCompliance.epcTask.checkEpcAnswers.epc.tenancyStartCheck",
                    formModel.tenancyStartedBeforeExpiry,
                    null as String?,
                ),
            )

        // Act
        val rows = EpcRegistrationCyaSummaryRowsFactory(mockEpcCertificateUrlProvider, mockState).createTenancyCheckRows()

        // Assert
        assertEquals(expectedRows, rows)
    }

    @Test
    fun `createTenancyCheckRows returns empty list when scenario is not expired`() {
        // Arrange
        setupStateForScenario(EpcScenario.VALID_EPC)

        // Act
        val rows = EpcRegistrationCyaSummaryRowsFactory(mockEpcCertificateUrlProvider, mockState).createTenancyCheckRows()

        // Assert
        assertEquals(emptyList(), rows)
    }

    @Test
    fun `getLowRatingTextKey returns lowRating key when scenario has low energy rating`() {
        // Arrange
        setupStateForScenario(EpcScenario.LOW_ENERGY_EPC_NO_EXEMPTION_OCCUPIED)

        // Act
        val result = EpcRegistrationCyaSummaryRowsFactory(mockEpcCertificateUrlProvider, mockState).getLowRatingTextKey()

        // Assert
        assertEquals("propertyCompliance.epcTask.checkEpcAnswers.epc.lowRating", result)
    }

    @Test
    fun `getLowRatingTextKey returns null when scenario does not have low energy rating`() {
        // Arrange
        setupStateForScenario(EpcScenario.VALID_EPC)

        // Act
        val result = EpcRegistrationCyaSummaryRowsFactory(mockEpcCertificateUrlProvider, mockState).getLowRatingTextKey()

        // Assert
        assertNull(result)
    }

    @Test
    fun `createExemptionReasonRows returns empty list when scenario has no low energy rating`() {
        // Arrange
        setupStateForScenario(EpcScenario.VALID_EPC)

        // Act
        val rows = EpcRegistrationCyaSummaryRowsFactory(mockEpcCertificateUrlProvider, mockState).createExemptionReasonRows()

        // Assert
        assertEquals(emptyList(), rows)
    }

    @Test
    fun `createExemptionReasonRows returns MEES exemption check row when scenario has low rating and no exemption`() {
        // Arrange
        setupStateForScenario(EpcScenario.LOW_ENERGY_EPC_NO_EXEMPTION_OCCUPIED)
        val hasMeesExemptionFormModel = MeesExemptionCheckFormModel().apply { propertyHasExemption = false }
        whenever(mockHasMeesExemptionStep.isStepReachable).thenReturn(true)
        whenever(mockHasMeesExemptionStep.formModelIfReachableOrNull).thenReturn(hasMeesExemptionFormModel)

        val expectedRows =
            listOf(
                SummaryListRowViewModel.forCheckYourAnswersPage(
                    "propertyCompliance.epcTask.checkEpcAnswers.epc.meesExemptionCheck",
                    false,
                    Destination.VisitableStep(mockHasMeesExemptionStep, ""),
                ),
            )

        // Act
        val rows = EpcRegistrationCyaSummaryRowsFactory(mockEpcCertificateUrlProvider, mockState).createExemptionReasonRows()

        // Assert
        assertEquals(expectedRows, rows)
    }

    @Test
    fun `createExemptionReasonRows returns MEES exemption check and exemption type rows when exemption is registered`() {
        // Arrange
        setupStateForScenario(EpcScenario.LOW_ENERGY_EPC_MEES_EXEMPTION)
        val exemptionReason = MeesExemptionReason.THIRD_PARTY_CONSENT
        val hasMeesExemptionFormModel = MeesExemptionCheckFormModel().apply { propertyHasExemption = true }
        whenever(mockHasMeesExemptionStep.isStepReachable).thenReturn(true)
        whenever(mockHasMeesExemptionStep.formModelIfReachableOrNull).thenReturn(hasMeesExemptionFormModel)
        whenever(mockMeesExemptionStep.isStepReachable).thenReturn(true)
        val meesExemptionFormModel = MeesExemptionReasonFormModel().apply { this.exemptionReason = exemptionReason }
        whenever(mockMeesExemptionStep.formModelIfReachableOrNull).thenReturn(meesExemptionFormModel)

        val expectedRows =
            listOf(
                SummaryListRowViewModel.forCheckYourAnswersPage(
                    "propertyCompliance.epcTask.checkEpcAnswers.epc.meesExemptionCheck",
                    true,
                    Destination.VisitableStep(mockHasMeesExemptionStep, ""),
                ),
                SummaryListRowViewModel.forCheckYourAnswersPage(
                    "propertyCompliance.epcTask.checkEpcAnswers.epc.meesExemption",
                    exemptionReason,
                    Destination.VisitableStep(mockMeesExemptionStep, ""),
                ),
            )

        // Act
        val rows = EpcRegistrationCyaSummaryRowsFactory(mockEpcCertificateUrlProvider, mockState).createExemptionReasonRows()

        // Assert
        assertEquals(expectedRows, rows)
    }

    @Test
    fun `getInsetTextKey returns meetsRequirements key for VALID_EPC scenario`() {
        // Arrange
        setupStateForScenario(EpcScenario.VALID_EPC)

        // Act
        val result = EpcRegistrationCyaSummaryRowsFactory(mockEpcCertificateUrlProvider, mockState).getInsetTextKey()

        // Assert
        assertEquals("propertyCompliance.epcTask.checkEpcAnswers.epc.meetsRequirements", result)
    }

    @Test
    fun `getInsetTextKey returns lowRatingOccupiedInset key for LOW_ENERGY_EPC_NO_EXEMPTION_OCCUPIED scenario`() {
        // Arrange
        setupStateForScenario(EpcScenario.LOW_ENERGY_EPC_NO_EXEMPTION_OCCUPIED)

        // Act
        val result = EpcRegistrationCyaSummaryRowsFactory(mockEpcCertificateUrlProvider, mockState).getInsetTextKey()

        // Assert
        assertEquals("propertyCompliance.epcTask.checkEpcAnswers.epc.lowRatingOccupiedInset", result)
    }

    @Test
    fun `getInsetTextKey returns lowRatingOccupiedInset key for EPC_EXPIRED_NOT_IN_DATE_OCCUPIED scenario`() {
        // Arrange
        setupStateForScenario(EpcScenario.EPC_EXPIRED_NOT_IN_DATE_OCCUPIED)

        // Act
        val result = EpcRegistrationCyaSummaryRowsFactory(mockEpcCertificateUrlProvider, mockState).getInsetTextKey()

        // Assert
        assertEquals("propertyCompliance.epcTask.checkEpcAnswers.epc.lowRatingOccupiedInset", result)
    }

    @Test
    fun `getInsetTextKey returns lowRatingOccupiedInset key for LOW_ENERGY_EPC_EXPIRED_IN_DATE_NO_EXEMPTION_OCCUPIED scenario`() {
        // Arrange
        setupStateForScenario(EpcScenario.LOW_ENERGY_EPC_EXPIRED_IN_DATE_NO_EXEMPTION_OCCUPIED)

        // Act
        val result = EpcRegistrationCyaSummaryRowsFactory(mockEpcCertificateUrlProvider, mockState).getInsetTextKey()

        // Assert
        assertEquals("propertyCompliance.epcTask.checkEpcAnswers.epc.lowRatingOccupiedInset", result)
    }

    @Test
    fun `getInsetTextKey returns occupiedNoEpcInset key for NO_EPC_NO_EXEMPTION_OCCUPIED scenario`() {
        // Arrange
        setupStateForScenario(EpcScenario.NO_EPC_NO_EXEMPTION_OCCUPIED)

        // Act
        val result = EpcRegistrationCyaSummaryRowsFactory(mockEpcCertificateUrlProvider, mockState).getInsetTextKey()

        // Assert
        assertEquals("propertyCompliance.epcTask.checkEpcAnswers.occupiedNoEpcInset", result)
    }

    @Test
    fun `getInsetTextKey returns null for non-inset scenario`() {
        // Arrange
        setupStateForScenario(EpcScenario.SKIPPED_OCCUPIED)

        // Act
        val result = EpcRegistrationCyaSummaryRowsFactory(mockEpcCertificateUrlProvider, mockState).getInsetTextKey()

        // Assert
        assertNull(result)
    }

    @Test
    fun `createNonEpcRows returns provideEpcLaterOccupied when scenario is SKIPPED_OCCUPIED`() {
        // Arrange
        setupStateForScenario(EpcScenario.SKIPPED_OCCUPIED)
        val expectedRows =
            listOf(
                SummaryListRowViewModel.forCheckYourAnswersPage(
                    "propertyCompliance.epcTask.checkEpcAnswers.hasEpc.label",
                    "propertyCompliance.epcTask.checkEpcAnswers.hasEpc.provideEpcLaterOccupied",
                    null as String?,
                ),
            )

        // Act
        val rows = EpcRegistrationCyaSummaryRowsFactory(mockEpcCertificateUrlProvider, mockState).createNonEpcRows()

        // Assert
        assertEquals(expectedRows, rows)
    }

    @Test
    fun `createNonEpcRows returns provideEpcLaterUnoccupied when scenario is SKIPPED_UNOCCUPIED`() {
        // Arrange
        setupStateForScenario(EpcScenario.SKIPPED_UNOCCUPIED)
        val expectedRows =
            listOf(
                SummaryListRowViewModel.forCheckYourAnswersPage(
                    "propertyCompliance.epcTask.checkEpcAnswers.hasEpc.label",
                    "propertyCompliance.epcTask.checkEpcAnswers.hasEpc.provideEpcLaterUnoccupied",
                    null as String?,
                ),
            )

        // Act
        val rows = EpcRegistrationCyaSummaryRowsFactory(mockEpcCertificateUrlProvider, mockState).createNonEpcRows()

        // Assert
        assertEquals(expectedRows, rows)
    }

    @Test
    fun `createNonEpcRows returns hasEpc and isEpcRequired rows when scenario is NO_EPC_NO_EXEMPTION_OCCUPIED`() {
        // Arrange
        setupStateForScenario(EpcScenario.NO_EPC_NO_EXEMPTION_OCCUPIED)
        whenever(mockIsEpcRequiredStep.isStepReachable).thenReturn(true)
        val isEpcRequiredFormModel = IsEpcRequiredFormModel().apply { epcRequired = true }
        whenever(mockIsEpcRequiredStep.formModelIfReachableOrNull).thenReturn(isEpcRequiredFormModel)

        val expectedRows =
            listOf(
                SummaryListRowViewModel.forCheckYourAnswersPage(
                    "propertyCompliance.epcTask.checkEpcAnswers.hasEpc.label",
                    "commonText.no",
                    null as String?,
                ),
                SummaryListRowViewModel.forCheckYourAnswersPage(
                    "propertyCompliance.epcTask.checkEpcAnswers.isEpcRequired",
                    true,
                    Destination.VisitableStep(mockIsEpcRequiredStep, ""),
                ),
            )

        // Act
        val rows = EpcRegistrationCyaSummaryRowsFactory(mockEpcCertificateUrlProvider, mockState).createNonEpcRows()

        // Assert
        assertEquals(expectedRows, rows)
    }

    @Test
    fun `createNonEpcRows returns hasEpc, isEpcRequired, and epcExemption rows when scenario is NO_EPC_EXEMPT`() {
        // Arrange
        setupStateForScenario(EpcScenario.NO_EPC_EXEMPT)
        val exemptionReason = EpcExemptionReason.PROTECTED_ARCHITECTURAL_OR_HISTORICAL_MERIT
        whenever(mockIsEpcRequiredStep.isStepReachable).thenReturn(true)
        val isEpcRequiredFormModel = IsEpcRequiredFormModel().apply { epcRequired = false }
        whenever(mockIsEpcRequiredStep.formModelIfReachableOrNull).thenReturn(isEpcRequiredFormModel)
        whenever(mockEpcExemptionStep.isStepReachable).thenReturn(true)
        val epcExemptionFormModel = EpcExemptionFormModel().apply { this.exemptionReason = exemptionReason }
        whenever(mockEpcExemptionStep.formModelIfReachableOrNull).thenReturn(epcExemptionFormModel)

        val expectedRows =
            listOf(
                SummaryListRowViewModel.forCheckYourAnswersPage(
                    "propertyCompliance.epcTask.checkEpcAnswers.hasEpc.label",
                    "commonText.no",
                    null as String?,
                ),
                SummaryListRowViewModel.forCheckYourAnswersPage(
                    "propertyCompliance.epcTask.checkEpcAnswers.isEpcRequired",
                    false,
                    Destination.VisitableStep(mockIsEpcRequiredStep, ""),
                ),
                SummaryListRowViewModel.forCheckYourAnswersPage(
                    "propertyCompliance.epcTask.checkEpcAnswers.epcExemption",
                    exemptionReason,
                    Destination.VisitableStep(mockEpcExemptionStep, ""),
                ),
            )

        // Act
        val rows = EpcRegistrationCyaSummaryRowsFactory(mockEpcCertificateUrlProvider, mockState).createNonEpcRows()

        // Assert
        assertEquals(expectedRows, rows)
    }

    @Test
    fun `createNonEpcRows returns empty list when scenario is an EPC card scenario`() {
        // Arrange
        setupStateForScenario(EpcScenario.VALID_EPC)

        // Act
        val rows = EpcRegistrationCyaSummaryRowsFactory(mockEpcCertificateUrlProvider, mockState).createNonEpcRows()

        // Assert
        assertEquals(emptyList(), rows)
    }
}
