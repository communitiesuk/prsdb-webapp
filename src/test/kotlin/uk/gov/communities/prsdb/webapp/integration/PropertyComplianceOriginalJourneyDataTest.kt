package uk.gov.communities.prsdb.webapp.integration

import jakarta.servlet.http.HttpSession
import jakarta.validation.Validation
import jakarta.validation.ValidatorFactory
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Named
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.validation.beanvalidation.SpringValidatorAdapter
import uk.gov.communities.prsdb.webapp.database.entity.PropertyCompliance
import uk.gov.communities.prsdb.webapp.database.repository.PropertyComplianceRepository
import uk.gov.communities.prsdb.webapp.forms.journeys.PropertyComplianceOriginalJourneyData
import uk.gov.communities.prsdb.webapp.forms.journeys.factories.PropertyComplianceUpdateJourneyFactory
import uk.gov.communities.prsdb.webapp.forms.steps.PropertyComplianceStepId
import uk.gov.communities.prsdb.webapp.services.EpcCertificateUrlProvider
import uk.gov.communities.prsdb.webapp.services.EpcLookupService
import uk.gov.communities.prsdb.webapp.services.JourneyDataService
import uk.gov.communities.prsdb.webapp.services.PropertyComplianceService
import uk.gov.communities.prsdb.webapp.services.PropertyOwnershipService
import uk.gov.communities.prsdb.webapp.services.factories.JourneyDataServiceFactory
import uk.gov.communities.prsdb.webapp.testHelpers.builders.PropertyComplianceBuilder

class PropertyComplianceOriginalJourneyDataTest {
    companion object {
        @JvmStatic
        fun getOriginalJourneyDataTestCases(): List<Arguments> =
            listOf(
                Arguments.of(
                    Named.of("with in date certs", PropertyComplianceBuilder.createWithInDateCerts()),
                    namedExactlyTheSame,
                ),
                Arguments.of(
                    Named.of("with expired certs", PropertyComplianceBuilder.createWithExpiredCerts()),
                    namedExactlyTheSame,
                ),
                Arguments.of(
                    Named.of("with naturally expired certs", PropertyComplianceBuilder.createWithNaturallyExpiredCerts()),
                    Named.of("an expired equivalent", ::isUpdatedAnExpiredVersionOfOriginal),
                ),
                Arguments.of(
                    Named.of("with cert exemptions", PropertyComplianceBuilder.createWithCertExemptions()),
                    namedExactlyTheSame,
                ),
                Arguments.of(
                    Named.of("with missing certs", PropertyComplianceBuilder.createWithMissingCerts()),
                    namedExactlyTheSame,
                ),
            )

        val namedExactlyTheSame = Named.of("exactly the same", ::areAllComplianceValuesTheSame)

        // TODO PRSD-1313 - Update to use the final submission step ID from the journey factory as prior submissions will be included
        val finalSubmissionStepId = PropertyComplianceStepId.GasSafetyUpdateCheckYourAnswers

        fun areAllComplianceValuesTheSame(
            original: PropertyCompliance,
            updated: PropertyCompliance,
        ): Boolean {
            return original.gasSafetyCertS3Key == updated.gasSafetyCertS3Key &&
                original.gasSafetyCertIssueDate == updated.gasSafetyCertIssueDate &&
                original.gasSafetyCertEngineerNum == updated.gasSafetyCertEngineerNum &&
                original.gasSafetyCertExemptionReason == updated.gasSafetyCertExemptionReason &&
                original.gasSafetyCertExemptionOtherReason == updated.gasSafetyCertExemptionOtherReason
            // TODO PRSD-1248 - check EICR values match the original record
            // TODO PRSD-1313 - check EPC values match the orignal record
        }

        fun isUpdatedAnExpiredVersionOfOriginal(
            original: PropertyCompliance,
            updated: PropertyCompliance,
        ): Boolean {
            return updated.gasSafetyCertS3Key == null &&
                original.gasSafetyCertIssueDate == updated.gasSafetyCertIssueDate &&
                updated.gasSafetyCertEngineerNum == null &&
                original.gasSafetyCertExemptionReason == updated.gasSafetyCertExemptionReason &&
                original.gasSafetyCertExemptionOtherReason == updated.gasSafetyCertExemptionOtherReason
            // TODO PRSD-1248 - check EICR values match the original record
            // TODO PRSD-1313 - check EPC values match the orignal record
        }
    }

    private lateinit var propertyComplianceRepository: PropertyComplianceRepository
    private lateinit var propertyOwnershipService: PropertyOwnershipService
    private lateinit var session: HttpSession

    private lateinit var journeyDataServiceFactory: JourneyDataServiceFactory
    private lateinit var epcLookupService: EpcLookupService
    private lateinit var validatorFactory: ValidatorFactory
    private lateinit var validator: SpringValidatorAdapter

    private lateinit var propertyComplianceService: PropertyComplianceService
    private lateinit var journeyFactory: PropertyComplianceUpdateJourneyFactory

    @BeforeEach
    fun setUp() {
        propertyComplianceRepository = mock()
        propertyOwnershipService = mock()
        session = mock()
        propertyComplianceService =
            PropertyComplianceService(
                propertyComplianceRepository = propertyComplianceRepository,
                propertyOwnershipService = propertyOwnershipService,
                session = session,
            )

        journeyDataServiceFactory = mock()
        epcLookupService = mock()

        validatorFactory = Validation.buildDefaultValidatorFactory()
        validator = SpringValidatorAdapter(validatorFactory.validator)

        this.journeyFactory =
            PropertyComplianceUpdateJourneyFactory(
                validator = validator,
                journeyDataServiceFactory = journeyDataServiceFactory,
                propertyComplianceService = propertyComplianceService,
                propertyOwnershipService = propertyOwnershipService,
                epcCertificateUrlProvider = EpcCertificateUrlProvider("http://example.com/epc/"),
                epcLookupService = epcLookupService,
            )
    }

    @AfterEach
    fun tearDown() {
        validatorFactory.close()
    }

    @ParameterizedTest
    @MethodSource("getOriginalJourneyDataTestCases")
    fun `A compliance record leads to original journey data that, allows retrieving the final submission step`(
        originalRecord: PropertyCompliance,
    ) {
        // Arrange
        val originalJourneyData = PropertyComplianceOriginalJourneyData.fromPropertyCompliance(originalRecord)
        val journeyDataService = mock<JourneyDataService>()

        whenever(journeyDataServiceFactory.create(any())).thenReturn(journeyDataService)
        whenever(journeyDataService.getJourneyDataFromSession()).thenReturn(originalJourneyData)
        whenever(propertyComplianceRepository.findByPropertyOwnership_Id(any())).thenReturn(PropertyComplianceBuilder().build())

        // Act
        val journey =
            journeyFactory.create(
                stepName = finalSubmissionStepId.urlPathSegment,
                propertyOwnershipId = 1L,
                checkingAnswersFor = null,
            )

        val finalTemplate = journey.getModelAndViewForStep()

        // Assert
        assertFalse(finalTemplate.viewName?.contains("redirect")!!)
    }

    @ParameterizedTest(name = "A compliance record {0} leads to original journey data that recreates a record which is {1}")
    @MethodSource("getOriginalJourneyDataTestCases")
    fun `Original journey data recreates the original record`(
        originalRecord: PropertyCompliance,
        complianceRecordsMatch: (PropertyCompliance, PropertyCompliance) -> Boolean,
    ) {
        // Arrange
        val originalJourneyData = PropertyComplianceOriginalJourneyData.fromPropertyCompliance(originalRecord)
        val journeyDataService = mock<JourneyDataService>()

        whenever(journeyDataServiceFactory.create(any())).thenReturn(journeyDataService)
        whenever(journeyDataService.getJourneyDataFromSession()).thenReturn(originalJourneyData)
        whenever(propertyComplianceRepository.findByPropertyOwnership_Id(any())).thenReturn(PropertyComplianceBuilder().build())

        // TODO PRSD-1313 - ensure EPC lookup is mocked correctly for each test case

        // Act
        val journey =
            journeyFactory.create(
                stepName = finalSubmissionStepId.urlPathSegment,
                propertyOwnershipId = 1L,
                checkingAnswersFor = null,
            )

        journey.completeStep(
            formData = mapOf(),
            principal = mock(),
        )

        val complianceCaptor = org.mockito.kotlin.argumentCaptor<PropertyCompliance>()
        verify(propertyComplianceRepository).save(complianceCaptor.capture())

        val savedCompliance = complianceCaptor.firstValue

        // Assert
        assertTrue(complianceRecordsMatch(originalRecord, savedCompliance))
    }
}
