package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.constants.enums.EpcExemptionReason
import uk.gov.communities.prsdb.webapp.constants.enums.MeesExemptionReason
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.EpcState
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.EpcExemptionStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.EpcInDateAtStartOfTenancyCheckStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.EpcScenario
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.HasEpcStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.HasMeesExemptionStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.IsEpcRequiredStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.MeesExemptionStep
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

    @Test
    fun `createEpcCardTitle returns heading key when scenario is an EPC card scenario`() {
        // Act
        val title =
            EpcRegistrationCyaSummaryRowsFactory(
                mockEpcCertificateUrlProvider,
                mockState,
                EpcScenario.VALID_EPC,
            ).createEpcCardTitle()

        // Assert
        assertEquals("propertyCompliance.epcTask.checkEpcAnswers.epc.yourEpc", title)
    }

    @Test
    fun `createEpcCardTitle returns null when scenario is not an EPC card scenario`() {
        // Act
        val title =
            EpcRegistrationCyaSummaryRowsFactory(
                mockEpcCertificateUrlProvider,
                mockState,
                EpcScenario.SKIPPED_OCCUPIED,
            ).createEpcCardTitle()

        // Assert
        assertNull(title)
    }

    @Test
    fun `createEpcCardTitle returns null when scenario is EPC_EXPIRED_UNOCCUPIED`() {
        // Act
        val title =
            EpcRegistrationCyaSummaryRowsFactory(
                mockEpcCertificateUrlProvider,
                mockState,
                EpcScenario.EPC_EXPIRED_UNOCCUPIED,
            ).createEpcCardTitle()

        // Assert
        assertNull(title)
    }

    @Test
    fun `createEpcCardTitle returns null when scenario is LOW_ENERGY_EPC_NO_EXEMPTION_UNOCCUPIED`() {
        // Act
        val title =
            EpcRegistrationCyaSummaryRowsFactory(
                mockEpcCertificateUrlProvider,
                mockState,
                EpcScenario.LOW_ENERGY_EPC_NO_EXEMPTION_UNOCCUPIED,
            ).createEpcCardTitle()

        // Assert
        assertNull(title)
    }

    @Test
    fun `createEpcCardRows returns rows for unexpired compliant EPC`() {
        // Arrange
        whenever(mockState.acceptedEpc).thenReturn(validEpc)
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
        val rows = EpcRegistrationCyaSummaryRowsFactory(mockEpcCertificateUrlProvider, mockState, EpcScenario.VALID_EPC).createEpcCardRows()

        // Assert
        assertEquals(expectedRows, rows)
    }

    @Test
    fun `createEpcCardRows returns null when scenario is not an EPC card scenario`() {
        // Arrange -- no acceptedEpc

        // Act
        val rows =
            EpcRegistrationCyaSummaryRowsFactory(
                mockEpcCertificateUrlProvider,
                mockState,
                EpcScenario.SKIPPED_OCCUPIED,
            ).createEpcCardRows()

        // Assert
        assertNull(rows)
    }

    @Test
    fun `createEpcCardActions returns view action when card is visible`() {
        // Arrange
        whenever(mockState.acceptedEpc).thenReturn(validEpc)
        whenever(mockEpcCertificateUrlProvider.getEpcCertificateUrl(validEpc.certificateNumber)).thenReturn(epcUrl)

        // Act
        val actions =
            EpcRegistrationCyaSummaryRowsFactory(
                mockEpcCertificateUrlProvider,
                mockState,
                EpcScenario.VALID_EPC,
            ).createEpcCardActions()

        // Assert
        assertEquals("propertyCompliance.epcTask.checkEpcAnswers.epc.viewFullEpc", actions?.first()?.text)
        assertEquals(epcUrl, actions?.first()?.url)
        assertTrue(actions?.first()?.opensInNewTab ?: false)
    }

    @Test
    fun `createEpcCardActions returns null when scenario is not an EPC card scenario`() {
        // Arrange -- no acceptedEpc

        // Act
        val actions =
            EpcRegistrationCyaSummaryRowsFactory(
                mockEpcCertificateUrlProvider,
                mockState,
                EpcScenario.SKIPPED_OCCUPIED,
            ).createEpcCardActions()

        // Assert
        assertNull(actions)
    }

    @Test
    fun `getEpcExpiredTextKey returns expired key when scenario is expired`() {
        // Act
        val result =
            EpcRegistrationCyaSummaryRowsFactory(
                mockEpcCertificateUrlProvider,
                mockState,
                EpcScenario.EPC_EXPIRED_IN_DATE_OCCUPIED,
            ).getEpcExpiredTextKey()

        // Assert
        assertEquals("propertyCompliance.epcTask.checkEpcAnswers.epc.expired", result)
    }

    @Test
    fun `getEpcExpiredTextKey returns null when scenario is not expired`() {
        // Act
        val result =
            EpcRegistrationCyaSummaryRowsFactory(
                mockEpcCertificateUrlProvider,
                mockState,
                EpcScenario.VALID_EPC,
            ).getEpcExpiredTextKey()

        // Assert
        assertNull(result)
    }

    @Test
    fun `createTenancyCheckRows returns tenancy row when EPC was in date at start of tenancy`() {
        // Arrange
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
        val rows =
            EpcRegistrationCyaSummaryRowsFactory(
                mockEpcCertificateUrlProvider,
                mockState,
                EpcScenario.EPC_EXPIRED_IN_DATE_OCCUPIED,
            ).createTenancyCheckRows()

        // Assert
        assertEquals(expectedRows, rows)
    }

    @Test
    fun `createTenancyCheckRows returns tenancy row when EPC was not in date at start of tenancy`() {
        // Arrange
        val formModel = EpcInDateAtStartOfTenancyCheckFormModel().apply { tenancyStartedBeforeExpiry = false }
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
        val rows =
            EpcRegistrationCyaSummaryRowsFactory(
                mockEpcCertificateUrlProvider,
                mockState,
                EpcScenario.EPC_EXPIRED_NOT_IN_DATE_OCCUPIED,
            ).createTenancyCheckRows()

        // Assert
        assertEquals(expectedRows, rows)
    }

    @Test
    fun `createTenancyCheckRows returns empty list when scenario is not expired`() {
        // Act
        val rows =
            EpcRegistrationCyaSummaryRowsFactory(
                mockEpcCertificateUrlProvider,
                mockState,
                EpcScenario.VALID_EPC,
            ).createTenancyCheckRows()

        // Assert
        assertEquals(emptyList(), rows)
    }

    @Test
    fun `getLowRatingTextKey returns lowRating key when scenario has low energy rating`() {
        // Act
        val result =
            EpcRegistrationCyaSummaryRowsFactory(
                mockEpcCertificateUrlProvider,
                mockState,
                EpcScenario.LOW_ENERGY_EPC_NO_EXEMPTION_OCCUPIED,
            ).getLowRatingTextKey()

        // Assert
        assertEquals("propertyCompliance.epcTask.checkEpcAnswers.epc.lowRating", result)
    }

    @Test
    fun `getLowRatingTextKey returns null when scenario does not have low energy rating`() {
        // Act
        val result =
            EpcRegistrationCyaSummaryRowsFactory(
                mockEpcCertificateUrlProvider,
                mockState,
                EpcScenario.VALID_EPC,
            ).getLowRatingTextKey()

        // Assert
        assertNull(result)
    }

    @Test
    fun `createAdditionalRows returns empty list when scenario has no low energy rating`() {
        // Act
        val rows =
            EpcRegistrationCyaSummaryRowsFactory(
                mockEpcCertificateUrlProvider,
                mockState,
                EpcScenario.VALID_EPC,
            ).createAdditionalRows()

        // Assert
        assertEquals(emptyList(), rows)
    }

    @Test
    fun `createAdditionalRows returns MEES exemption check row when scenario has low rating and no exemption`() {
        // Arrange
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
        val rows =
            EpcRegistrationCyaSummaryRowsFactory(
                mockEpcCertificateUrlProvider,
                mockState,
                EpcScenario.LOW_ENERGY_EPC_NO_EXEMPTION_OCCUPIED,
            ).createAdditionalRows()

        // Assert
        assertEquals(expectedRows, rows)
    }

    @Test
    fun `createAdditionalRows returns MEES exemption check and exemption type rows when exemption is registered`() {
        // Arrange
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
        val rows =
            EpcRegistrationCyaSummaryRowsFactory(
                mockEpcCertificateUrlProvider,
                mockState,
                EpcScenario.LOW_ENERGY_EPC_MEES_EXEMPTION,
            ).createAdditionalRows()

        // Assert
        assertEquals(expectedRows, rows)
    }

    @Test
    fun `getInsetTextKey returns meetsRequirements key for VALID_EPC scenario`() {
        // Act
        val result = EpcRegistrationCyaSummaryRowsFactory(mockEpcCertificateUrlProvider, mockState, EpcScenario.VALID_EPC).getInsetTextKey()

        // Assert
        assertEquals("propertyCompliance.epcTask.checkEpcAnswers.epc.meetsRequirements", result)
    }

    @Test
    fun `getInsetTextKey returns meetsRequirements key for EPC_EXPIRED_IN_DATE_OCCUPIED scenario`() {
        // Act
        val result =
            EpcRegistrationCyaSummaryRowsFactory(
                mockEpcCertificateUrlProvider,
                mockState,
                EpcScenario.EPC_EXPIRED_IN_DATE_OCCUPIED,
            ).getInsetTextKey()

        // Assert
        assertEquals("propertyCompliance.epcTask.checkEpcAnswers.epc.meetsRequirements", result)
    }

    @Test
    fun `getInsetTextKey returns lowRatingOccupiedInset key for LOW_ENERGY_EPC_NO_EXEMPTION_OCCUPIED scenario`() {
        // Act
        val result =
            EpcRegistrationCyaSummaryRowsFactory(
                mockEpcCertificateUrlProvider,
                mockState,
                EpcScenario.LOW_ENERGY_EPC_NO_EXEMPTION_OCCUPIED,
            ).getInsetTextKey()

        // Assert
        assertEquals("propertyCompliance.epcTask.checkEpcAnswers.epc.lowRatingOccupiedInset", result)
    }

    @Test
    fun `getInsetTextKey returns lowRatingOccupiedInset key for EPC_EXPIRED_NOT_IN_DATE_OCCUPIED scenario`() {
        // Act
        val result =
            EpcRegistrationCyaSummaryRowsFactory(
                mockEpcCertificateUrlProvider,
                mockState,
                EpcScenario.EPC_EXPIRED_NOT_IN_DATE_OCCUPIED,
            ).getInsetTextKey()

        // Assert
        assertEquals("propertyCompliance.epcTask.checkEpcAnswers.epc.lowRatingOccupiedInset", result)
    }

    @Test
    fun `getInsetTextKey returns lowRatingOccupiedInset key for LOW_ENERGY_EPC_EXPIRED_IN_DATE_NO_EXEMPTION_OCCUPIED scenario`() {
        // Act
        val result =
            EpcRegistrationCyaSummaryRowsFactory(
                mockEpcCertificateUrlProvider,
                mockState,
                EpcScenario.LOW_ENERGY_EPC_EXPIRED_IN_DATE_NO_EXEMPTION_OCCUPIED,
            ).getInsetTextKey()

        // Assert
        assertEquals("propertyCompliance.epcTask.checkEpcAnswers.epc.lowRatingOccupiedInset", result)
    }

    @Test
    fun `getInsetTextKey returns occupiedNoEpcInset key for NO_EPC_NO_EXEMPTION_OCCUPIED scenario`() {
        // Act
        val result =
            EpcRegistrationCyaSummaryRowsFactory(
                mockEpcCertificateUrlProvider,
                mockState,
                EpcScenario.NO_EPC_NO_EXEMPTION_OCCUPIED,
            ).getInsetTextKey()

        // Assert
        assertEquals("propertyCompliance.epcTask.checkEpcAnswers.occupiedNoEpcInset", result)
    }

    @Test
    fun `getInsetTextKey returns null for non-inset scenario`() {
        // Act
        val result =
            EpcRegistrationCyaSummaryRowsFactory(
                mockEpcCertificateUrlProvider,
                mockState,
                EpcScenario.SKIPPED_OCCUPIED,
            ).getInsetTextKey()

        // Assert
        assertNull(result)
    }

    @Test
    fun `createNonEpcRows returns provideEpcLaterOccupied when scenario is SKIPPED_OCCUPIED`() {
        val expectedRows =
            listOf(
                SummaryListRowViewModel.forCheckYourAnswersPage(
                    "propertyCompliance.epcTask.checkEpcAnswers.hasEpc.label",
                    "propertyCompliance.epcTask.checkEpcAnswers.hasEpc.provideEpcLaterOccupied",
                    null as String?,
                ),
            )

        // Act
        val rows =
            EpcRegistrationCyaSummaryRowsFactory(
                mockEpcCertificateUrlProvider,
                mockState,
                EpcScenario.SKIPPED_OCCUPIED,
            ).createNonEpcRows()

        // Assert
        assertEquals(expectedRows, rows)
    }

    @Test
    fun `createNonEpcRows returns provideEpcLaterUnoccupied when scenario is SKIPPED_UNOCCUPIED`() {
        val expectedRows =
            listOf(
                SummaryListRowViewModel.forCheckYourAnswersPage(
                    "propertyCompliance.epcTask.checkEpcAnswers.hasEpc.label",
                    "propertyCompliance.epcTask.checkEpcAnswers.hasEpc.provideEpcLaterUnoccupied",
                    null as String?,
                ),
            )

        // Act
        val rows =
            EpcRegistrationCyaSummaryRowsFactory(
                mockEpcCertificateUrlProvider,
                mockState,
                EpcScenario.SKIPPED_UNOCCUPIED,
            ).createNonEpcRows()

        // Assert
        assertEquals(expectedRows, rows)
    }

    @Test
    fun `createNonEpcRows returns provideEpcLaterUnoccupied when scenario is EPC_EXPIRED_UNOCCUPIED`() {
        val expectedRows =
            listOf(
                SummaryListRowViewModel.forCheckYourAnswersPage(
                    "propertyCompliance.epcTask.checkEpcAnswers.hasEpc.label",
                    "propertyCompliance.epcTask.checkEpcAnswers.hasEpc.provideEpcLaterUnoccupied",
                    null as String?,
                ),
            )

        // Act
        val rows =
            EpcRegistrationCyaSummaryRowsFactory(
                mockEpcCertificateUrlProvider,
                mockState,
                EpcScenario.EPC_EXPIRED_UNOCCUPIED,
            ).createNonEpcRows()

        // Assert
        assertEquals(expectedRows, rows)
    }

    @Test
    fun `createNonEpcRows returns provideEpcLaterUnoccupied when scenario is LOW_ENERGY_EPC_NO_EXEMPTION_UNOCCUPIED`() {
        val expectedRows =
            listOf(
                SummaryListRowViewModel.forCheckYourAnswersPage(
                    "propertyCompliance.epcTask.checkEpcAnswers.hasEpc.label",
                    "propertyCompliance.epcTask.checkEpcAnswers.hasEpc.provideEpcLaterUnoccupied",
                    null as String?,
                ),
            )

        // Act
        val rows =
            EpcRegistrationCyaSummaryRowsFactory(
                mockEpcCertificateUrlProvider,
                mockState,
                EpcScenario.LOW_ENERGY_EPC_NO_EXEMPTION_UNOCCUPIED,
            ).createNonEpcRows()

        // Assert
        assertEquals(expectedRows, rows)
    }

    @Test
    fun `createNonEpcRows returns hasEpc and isEpcRequired rows when scenario is NO_EPC_NO_EXEMPTION_OCCUPIED`() {
        // Arrange
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
        val rows =
            EpcRegistrationCyaSummaryRowsFactory(
                mockEpcCertificateUrlProvider,
                mockState,
                EpcScenario.NO_EPC_NO_EXEMPTION_OCCUPIED,
            ).createNonEpcRows()

        // Assert
        assertEquals(expectedRows, rows)
    }

    @Test
    fun `createNonEpcRows returns provideEpcLaterUnoccupied when scenario is NO_EPC_NO_EXEMPTION_UNOCCUPIED`() {
        val expectedRows =
            listOf(
                SummaryListRowViewModel.forCheckYourAnswersPage(
                    "propertyCompliance.epcTask.checkEpcAnswers.hasEpc.label",
                    "propertyCompliance.epcTask.checkEpcAnswers.hasEpc.provideEpcLaterUnoccupied",
                    null as String?,
                ),
            )

        // Act
        val rows =
            EpcRegistrationCyaSummaryRowsFactory(
                mockEpcCertificateUrlProvider,
                mockState,
                EpcScenario.NO_EPC_NO_EXEMPTION_UNOCCUPIED,
            ).createNonEpcRows()

        // Assert
        assertEquals(expectedRows, rows)
    }

    @Test
    fun `createNonEpcRows returns hasEpc, isEpcRequired, and epcExemption rows when scenario is NO_EPC_EXEMPT`() {
        // Arrange
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
        val rows =
            EpcRegistrationCyaSummaryRowsFactory(
                mockEpcCertificateUrlProvider,
                mockState,
                EpcScenario.NO_EPC_EXEMPT,
            ).createNonEpcRows()

        // Assert
        assertEquals(expectedRows, rows)
    }

    @Test
    fun `createNonEpcRows returns empty list when scenario is an EPC card scenario`() {
        // Act
        val rows = EpcRegistrationCyaSummaryRowsFactory(mockEpcCertificateUrlProvider, mockState, EpcScenario.VALID_EPC).createNonEpcRows()

        // Assert
        assertEquals(emptyList(), rows)
    }
}
