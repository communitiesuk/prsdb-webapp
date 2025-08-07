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
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.validation.beanvalidation.SpringValidatorAdapter
import uk.gov.communities.prsdb.webapp.database.entity.PropertyCompliance
import uk.gov.communities.prsdb.webapp.database.repository.FileUploadRepository
import uk.gov.communities.prsdb.webapp.database.repository.PropertyComplianceRepository
import uk.gov.communities.prsdb.webapp.forms.journeys.PropertyComplianceOriginalJourneyData
import uk.gov.communities.prsdb.webapp.forms.journeys.factories.PropertyComplianceUpdateJourneyFactory
import uk.gov.communities.prsdb.webapp.forms.steps.PropertyComplianceStepId
import uk.gov.communities.prsdb.webapp.forms.steps.factories.PropertyComplianceSharedStepFactory
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
                    false,
                ),
                Arguments.of(
                    Named.of("with expired certs", PropertyComplianceBuilder.createWithExpiredCerts()),
                    namedExactlyTheSame,
                    false,
                ),
                Arguments.of(
                    Named.of("with naturally expired certs", PropertyComplianceBuilder.createWithNaturallyExpiredCerts()),
                    Named.of("an expired equivalent", ::isUpdatedAnExpiredVersionOfOriginal),
                    false,
                ),
                Arguments.of(
                    Named.of("with cert exemptions", PropertyComplianceBuilder.createWithCertExemptions()),
                    namedExactlyTheSame,
                    false,
                ),
                Arguments.of(
                    Named.of("with missing certs", PropertyComplianceBuilder.createWithMissingCerts()),
                    namedExactlyTheSame,
                    false,
                ),
                Arguments.of(
                    Named.of("with low epc rating", PropertyComplianceBuilder.createWithInDateCertsAndLowEpcRating()),
                    namedExactlyTheSame,
                    false,
                ),
                Arguments.of(
                    Named.of(
                        "with low epc rating and mees exemption",
                        PropertyComplianceBuilder.createWithInDateCertsAndLowEpcRatingAndMeesExemptionReason(),
                    ),
                    namedExactlyTheSame,
                    false,
                ),
                Arguments.of(
                    Named.of(
                        "with low epc rating for updating mees only",
                        PropertyComplianceBuilder.createWithInDateCertsAndLowEpcRating(),
                    ),
                    namedExactlyTheSame,
                    true,
                ),
                Arguments.of(
                    Named.of(
                        "with low epc rating and mees exemption for updating mees only",
                        PropertyComplianceBuilder.createWithInDateCertsAndLowEpcRatingAndMeesExemptionReason(),
                    ),
                    namedExactlyTheSame,
                    true,
                ),
            )

        val namedExactlyTheSame = Named.of("exactly the same", ::areAllComplianceValuesTheSame)

        fun areAllComplianceValuesTheSame(
            original: PropertyCompliance,
            updated: PropertyCompliance,
        ): Boolean =
            original.gasSafetyCertS3Key == updated.gasSafetyCertS3Key &&
                original.gasSafetyCertIssueDate == updated.gasSafetyCertIssueDate &&
                original.gasSafetyCertEngineerNum == updated.gasSafetyCertEngineerNum &&
                original.gasSafetyCertExemptionReason == updated.gasSafetyCertExemptionReason &&
                original.gasSafetyCertExemptionOtherReason == updated.gasSafetyCertExemptionOtherReason &&
                original.eicrS3Key == updated.eicrS3Key &&
                original.eicrIssueDate == updated.eicrIssueDate &&
                original.eicrExemptionReason == updated.eicrExemptionReason &&
                original.eicrExemptionOtherReason == updated.eicrExemptionOtherReason &&
                original.epcUrl == updated.epcUrl &&
                original.epcExpiryDate == updated.epcExpiryDate &&
                original.tenancyStartedBeforeEpcExpiry == updated.tenancyStartedBeforeEpcExpiry &&
                original.epcEnergyRating == updated.epcEnergyRating &&
                original.epcExemptionReason == updated.epcExemptionReason &&
                original.epcMeesExemptionReason == updated.epcMeesExemptionReason

        fun isUpdatedAnExpiredVersionOfOriginal(
            original: PropertyCompliance,
            updated: PropertyCompliance,
        ): Boolean =
            updated.gasSafetyCertS3Key == null &&
                original.gasSafetyCertIssueDate == updated.gasSafetyCertIssueDate &&
                updated.gasSafetyCertEngineerNum == null &&
                original.gasSafetyCertExemptionReason == updated.gasSafetyCertExemptionReason &&
                original.gasSafetyCertExemptionOtherReason == updated.gasSafetyCertExemptionOtherReason &&
                updated.eicrS3Key == null &&
                original.eicrIssueDate == updated.eicrIssueDate &&
                original.eicrExemptionReason == updated.eicrExemptionReason &&
                original.eicrExemptionOtherReason == updated.eicrExemptionOtherReason &&
                original.epcUrl == updated.epcUrl &&
                original.epcExpiryDate == updated.epcExpiryDate &&
                original.tenancyStartedBeforeEpcExpiry == updated.tenancyStartedBeforeEpcExpiry &&
                original.epcEnergyRating == updated.epcEnergyRating &&
                original.epcExemptionReason == updated.epcExemptionReason &&
                original.epcMeesExemptionReason == updated.epcMeesExemptionReason
    }

    private lateinit var propertyComplianceRepository: PropertyComplianceRepository
    private lateinit var propertyOwnershipService: PropertyOwnershipService
    private lateinit var session: HttpSession
    private lateinit var fileUploadRepository: FileUploadRepository

    private lateinit var journeyDataServiceFactory: JourneyDataServiceFactory
    private lateinit var epcLookupService: EpcLookupService
    private lateinit var validatorFactory: ValidatorFactory
    private lateinit var validator: SpringValidatorAdapter

    private lateinit var propertyComplianceService: PropertyComplianceService
    private lateinit var journeyFactory: PropertyComplianceUpdateJourneyFactory

    private lateinit var stepFactory: PropertyComplianceSharedStepFactory

    @BeforeEach
    fun setUp() {
        propertyComplianceRepository = mock()
        propertyOwnershipService = mock()
        session = mock()
        fileUploadRepository = mock()
        propertyComplianceService =
            PropertyComplianceService(
                propertyComplianceRepository = propertyComplianceRepository,
                propertyOwnershipService = propertyOwnershipService,
                session = session,
                fileUploadRepository = fileUploadRepository,
            )

        journeyDataServiceFactory = mock()
        epcLookupService = mock()

        validatorFactory = Validation.buildDefaultValidatorFactory()
        validator = SpringValidatorAdapter(validatorFactory.validator)

        stepFactory = mock()

        this.journeyFactory =
            PropertyComplianceUpdateJourneyFactory(
                validator = validator,
                journeyDataServiceFactory = journeyDataServiceFactory,
                propertyComplianceService = propertyComplianceService,
                propertyOwnershipService = propertyOwnershipService,
                epcCertificateUrlProvider = EpcCertificateUrlProvider(PropertyComplianceBuilder.TEST_EPC_BASE_URL),
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
        complianceRecordsMatch: (PropertyCompliance, PropertyCompliance) -> Boolean,
        meesOnlyUpdate: Boolean,
    ) {
        // Arrange
        val finalSubmissionStepId = getFinalSubmissionStepId(meesOnlyUpdate)
        setupStepFactory(meesOnlyUpdate)

        val originalJourneyData = PropertyComplianceOriginalJourneyData.fromPropertyCompliance(originalRecord, stepFactory)
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
        meesOnlyUpdate: Boolean = false,
    ) {
        // Arrange
        val finalSubmissionStepId = getFinalSubmissionStepId(meesOnlyUpdate)
        setupStepFactory(meesOnlyUpdate)
        val originalJourneyData = PropertyComplianceOriginalJourneyData.fromPropertyCompliance(originalRecord, stepFactory)

        val journeyDataService = mock<JourneyDataService>()

        whenever(journeyDataServiceFactory.create(any())).thenReturn(journeyDataService)
        whenever(journeyDataService.getJourneyDataFromSession()).thenReturn(originalJourneyData)
        whenever(propertyComplianceRepository.findByPropertyOwnership_Id(any())).thenReturn(PropertyComplianceBuilder().build())
        whenever(fileUploadRepository.getReferenceById(any())).thenReturn(originalRecord.gasSafetyFileUpload, originalRecord.eicrFileUpload)

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

        val complianceCaptor = argumentCaptor<PropertyCompliance>()
        verify(propertyComplianceRepository).save(complianceCaptor.capture())

        val savedCompliance = complianceCaptor.firstValue

        // Assert
        assertTrue(
            complianceRecordsMatch(originalRecord, savedCompliance),
            complianceRecordUpdateString(originalRecord, savedCompliance),
        )
    }

    private fun complianceRecordUpdateString(
        original: PropertyCompliance,
        new: PropertyCompliance,
    ) = """
        Updated PropertyCompliance values:
        gasSafetyCertS3Key: ${original.gasSafetyCertS3Key} -> ${new.gasSafetyCertS3Key}, 
        gasSafetyCertIssueDate: ${original.gasSafetyCertIssueDate} -> ${new.gasSafetyCertIssueDate},
        gasSafetyCertEngineerNum: ${original.gasSafetyCertEngineerNum} -> ${new.gasSafetyCertEngineerNum},
        gasSafetyCertExemptionReason: ${original.gasSafetyCertExemptionReason} -> ${new.gasSafetyCertExemptionReason},
        gasSafetyCertExemptionOtherReason: ${original.gasSafetyCertExemptionOtherReason} -> ${new.gasSafetyCertExemptionOtherReason},
        eicrS3Key: ${original.eicrS3Key} -> ${new.eicrS3Key},
        eicrIssueDate: ${original.eicrIssueDate} -> ${new.eicrIssueDate},
        eicrExemptionReason: ${original.eicrExemptionReason} -> ${new.eicrExemptionReason},
        eicrExemptionOtherReason: ${original.eicrExemptionOtherReason} -> ${new.eicrExemptionOtherReason},
        epcUrl: ${original.epcUrl} -> ${new.epcUrl},
        epcExpiryDate: ${original.epcExpiryDate} -> ${new.epcExpiryDate},
        tenancyStartedBeforeEpcExpiry: ${original.tenancyStartedBeforeEpcExpiry} -> ${new.tenancyStartedBeforeEpcExpiry},
        epcEnergyRating: ${original.epcEnergyRating} -> ${new.epcEnergyRating},
        epcExemptionReason: ${original.epcExemptionReason} -> ${new.epcExemptionReason},
        epcMeesExemptionReason: ${original.epcMeesExemptionReason} -> ${new.epcMeesExemptionReason}
        """.trimIndent()

    private fun setupStepFactory(meesOnlyUpdate: Boolean) {
        whenever(stepFactory.epcNotAutomatchedStepId).thenReturn(
            if (meesOnlyUpdate) {
                PropertyComplianceStepId.UpdateMeesEpcNotAutomatched
            } else {
                PropertyComplianceStepId.EpcNotAutoMatched
            },
        )
        whenever(stepFactory.checkAutoMatchedEpcStepId).thenReturn(
            if (meesOnlyUpdate) {
                PropertyComplianceStepId.UpdateMeesCheckAutoMatchedEpc
            } else {
                PropertyComplianceStepId.CheckAutoMatchedEpc
            },
        )
        whenever(stepFactory.checkMatchedEpcStepId).thenReturn(
            if (meesOnlyUpdate) {
                PropertyComplianceStepId.UpdateMeesCheckMatchedEpc
            } else {
                PropertyComplianceStepId.CheckMatchedEpc
            },
        )
        whenever(stepFactory.epcLookupStepId).thenReturn(
            if (meesOnlyUpdate) {
                PropertyComplianceStepId.UpdateMeesEpcLookup
            } else {
                PropertyComplianceStepId.EpcLookup
            },
        )
        whenever(stepFactory.epcNotFoundStepId).thenReturn(
            if (meesOnlyUpdate) {
                PropertyComplianceStepId.UpdateMeesEpcNotFound
            } else {
                PropertyComplianceStepId.EpcNotFound
            },
        )
        whenever(stepFactory.epcExpiryCheckStepId).thenReturn(
            if (meesOnlyUpdate) {
                PropertyComplianceStepId.UpdateMeesEpcExpiryCheck
            } else {
                PropertyComplianceStepId.EpcExpiryCheck
            },
        )
        whenever(stepFactory.epcExpiredStepId).thenReturn(
            if (meesOnlyUpdate) {
                PropertyComplianceStepId.UpdateMeesEpcExpired
            } else {
                PropertyComplianceStepId.EpcExpired
            },
        )
        whenever(stepFactory.epcExemptionReasonStepId).thenReturn(
            if (meesOnlyUpdate) {
                PropertyComplianceStepId.UpdateMeesEpcExemptionReason
            } else {
                PropertyComplianceStepId.EpcExemptionReason
            },
        )
        whenever(stepFactory.epcExemptionConfirmationStepId).thenReturn(
            if (meesOnlyUpdate) {
                PropertyComplianceStepId.UpdateMeesEpcExemptionConfirmation
            } else {
                PropertyComplianceStepId.EpcExemptionConfirmation
            },
        )
        whenever(stepFactory.meesExemptionCheckStepId).thenReturn(
            if (meesOnlyUpdate) {
                PropertyComplianceStepId.UpdateMeesMeesExemptionCheck
            } else {
                PropertyComplianceStepId.MeesExemptionCheck
            },
        )
        whenever(stepFactory.meesExemptionReasonStepId).thenReturn(
            if (meesOnlyUpdate) {
                PropertyComplianceStepId.UpdateMeesMeesExemptionReason
            } else {
                PropertyComplianceStepId.MeesExemptionReason
            },
        )
        whenever(stepFactory.meesExemptionConfirmationStepId).thenReturn(
            if (meesOnlyUpdate) {
                PropertyComplianceStepId.UpdateMeesMeesExemptionConfirmation
            } else {
                PropertyComplianceStepId.MeesExemptionConfirmation
            },
        )
        whenever(stepFactory.lowEnergyRatingStepId).thenReturn(
            if (meesOnlyUpdate) {
                PropertyComplianceStepId.UpdateMeesLowEnergyRating
            } else {
                PropertyComplianceStepId.LowEnergyRating
            },
        )
        whenever(stepFactory.epcCheckYourAnswersStepId).thenReturn(
            if (meesOnlyUpdate) {
                PropertyComplianceStepId.UpdateMeesCheckYourAnswers
            } else {
                PropertyComplianceStepId.UpdateEpcCheckYourAnswers
            },
        )
    }

    private fun getFinalSubmissionStepId(meesOnlyUpdate: Boolean) =
        if (meesOnlyUpdate) {
            PropertyComplianceStepId.UpdateMeesCheckYourAnswers
        } else {
            PropertyComplianceStepId.UpdateEpcCheckYourAnswers
        }
}
