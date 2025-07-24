package uk.gov.communities.prsdb.webapp.integration

import jakarta.servlet.http.HttpSession
import jakarta.validation.Validation
import jakarta.validation.Validator
import jakarta.validation.ValidatorFactory
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.context.annotation.Import
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

@Import(Validator::class)
class PropertyComplianceOriginalJourneyDataTest {
    companion object {
        @JvmStatic
        fun getOriginalJourneyDataTestCases(): List<PropertyCompliance> =
            listOf(
                PropertyComplianceBuilder.createWithInDateCerts(),
                PropertyComplianceBuilder.createWithExpiredCerts(),
                PropertyComplianceBuilder.createWithNaturallyExpiredCerts(),
                PropertyComplianceBuilder.createWithCertExemptions(),
                PropertyComplianceBuilder.createWithMissingCerts(),
            )

        // TODO PRSD-1313 - Update to use the final submission step ID from the journey factory as prior submissions will be included
        val finalSubmissionStepId = PropertyComplianceStepId.GasSafetyUpdateCheckYourAnswers
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
    fun setUpBeforeAll() {
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
    fun getOriginalJourneyData(originalRecord: PropertyCompliance) {
        // Arrange
        val originalJourneyData = PropertyComplianceOriginalJourneyData.fromPropertyCompliance(originalRecord)
        val journeyDataService = mock<JourneyDataService>()

        whenever(journeyDataServiceFactory.create(any())).thenReturn(journeyDataService)
        whenever(journeyDataService.getJourneyDataFromSession()).thenReturn(originalJourneyData)
        whenever(propertyComplianceRepository.findByPropertyOwnership_Id(any())).thenReturn(originalRecord)

        // TODO PRSD-1313 - ensure EPC lookup is mocked correctly for each test case

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

        verify(propertyComplianceRepository).save(eq(originalRecord))
    }
}
