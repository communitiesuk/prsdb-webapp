package uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.propertyComplianceViewModels

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertIterableEquals
import org.junit.jupiter.api.Named.named
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments.arguments
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.springframework.context.MessageSource
import uk.gov.communities.prsdb.webapp.constants.GET_NEW_EPC_URL
import uk.gov.communities.prsdb.webapp.constants.PROVIDE_LATER_DEADLINE_DAYS
import uk.gov.communities.prsdb.webapp.constants.enums.EpcExemptionReason
import uk.gov.communities.prsdb.webapp.constants.enums.MeesExemptionReason
import uk.gov.communities.prsdb.webapp.database.entity.PropertyCompliance
import uk.gov.communities.prsdb.webapp.helpers.converters.MessageKeyConverter
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.EpcExpiredInsetViewModel
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.SummaryCardSupplementarySection
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.SummaryListRowViewModel
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.TagValue
import uk.gov.communities.prsdb.webapp.testHelpers.builders.PropertyComplianceBuilder
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

class EpcViewModelFactoryTests {
    @ParameterizedTest(name = "{0}")
    @MethodSource("provideEpcRows")
    fun `fromEntity returns the correct summary rows`(
        propertyCompliance: PropertyCompliance,
        expectedRows: List<SummaryListRowViewModel>,
    ) {
        val epcRows = epcViewModelFactory.fromEntity(propertyCompliance)

        assertIterableEquals(epcRows, expectedRows)
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("provideInsetTextKeys")
    fun `getInsetTextKey returns the correct key`(
        propertyCompliance: PropertyCompliance,
        expectedKey: String?,
    ) {
        val insetTextKey = epcViewModelFactory.getInsetTextKey(propertyCompliance)

        assertEquals(expectedKey, insetTextKey)
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("provideSupplementarySections")
    fun `getSupplementarySections returns the correct sections`(
        propertyCompliance: PropertyCompliance,
        expectedSections: List<SummaryCardSupplementarySection>,
    ) {
        val sections = epcViewModelFactory.getSupplementarySections(propertyCompliance)

        assertEquals(expectedSections, sections)
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("provideEpcExpiredInsetViewModel")
    fun `getEpcExpiredInsetViewModel returns the correct view model`(
        propertyCompliance: PropertyCompliance,
        expectedViewModel: EpcExpiredInsetViewModel?,
    ) {
        val result = epcViewModelFactory.getEpcExpiredInsetViewModel(propertyCompliance)

        assertEquals(expectedViewModel, result)
    }

    companion object {
        private val mockMessageSource: MessageSource = mock()
        private val epcViewModelFactory = EpcViewModelFactory(mockMessageSource)

        private val lastOccupiedDate = LocalDate.of(2025, 1, 15)
        private val deadlineDate = lastOccupiedDate.plusDays(PROVIDE_LATER_DEADLINE_DAYS.toLong())
        private val formattedDeadline = deadlineDate.format(DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.UK))
        private val expectedDeadlineText = "Provide EPC details later (before $formattedDeadline)"

        private val naturallyExpiredExpiryDate = LocalDate.now().minusYears(1)
        private val formattedNaturallyExpiredDate =
            naturallyExpiredExpiryDate.format(DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.UK))
        private val expectedNaturallyExpiredInsetViewModel =
            EpcExpiredInsetViewModel(
                expiryDate = formattedNaturallyExpiredDate,
                linkUrl = GET_NEW_EPC_URL,
            )

        init {
            whenever(
                mockMessageSource.getMessage(
                    eq("propertyDetails.complianceInformation.energyPerformance.occupiedWithDeadline"),
                    eq(arrayOf(formattedDeadline)),
                    any(),
                ),
            ).thenReturn(expectedDeadlineText)
        }

        private val compliant = PropertyComplianceBuilder.createWithInDateCerts()
        private val expired = PropertyComplianceBuilder.createWithOnlyEpcExpiredCert(propertyIsOccupied = true)
        private val expiredUnoccupied = PropertyComplianceBuilder.createWithOnlyEpcExpiredCert()
        private val expiredWithTenancyBeforeExpiry =
            PropertyComplianceBuilder()
                .withOccupiedPropertyOwnership()
                .withGasSafetyCert()
                .withElectricalSafety()
                .withElectricalCertType()
                .withExpiredEpc()
                .withTenancyStartedBeforeEpcExpiry(true)
                .build()
        private val expiredWithTenancyBeforeExpiryAndLowRating =
            PropertyComplianceBuilder()
                .withOccupiedPropertyOwnership()
                .withGasSafetyCert()
                .withElectricalSafety()
                .withElectricalCertType()
                .withExpiredEpc()
                .withLowEpcRating()
                .withTenancyStartedBeforeEpcExpiry(true)
                .build()
        private val expiredWithTenancyBeforeExpiryAndLowRatingAndMeesExemption =
            PropertyComplianceBuilder()
                .withOccupiedPropertyOwnership()
                .withGasSafetyCert()
                .withElectricalSafety()
                .withElectricalCertType()
                .withExpiredEpc()
                .withLowEpcRating()
                .withTenancyStartedBeforeEpcExpiry(true)
                .withMeesExemption()
                .build()
        private val exempt = PropertyComplianceBuilder.createWithCertExemptions(epcExemption = EpcExemptionReason.DUE_FOR_DEMOLITION)
        private val missing = PropertyComplianceBuilder.createWithMissingCerts()
        private val missingOccupied = PropertyComplianceBuilder.createWithMissingCerts(propertyIsOccupied = true)
        private val missingOccupiedProvideLater =
            PropertyComplianceBuilder()
                .withOccupiedPropertyOwnership(lastOccupiedDate)
                .withElectricalCertType()
                .withEpcProvideLater()
                .build()
        private val missingOccupiedNoCert =
            PropertyComplianceBuilder()
                .withOccupiedPropertyOwnership(lastOccupiedDate)
                .withElectricalCertType()
                .build()
        private val meesCompliant = PropertyComplianceBuilder.createWithInDateCertsAndLowEpcRatingAndMeesExemptionReason()
        private val meesMissingExemptionReason = PropertyComplianceBuilder.createWithInDateCertsAndLowEpcRating()
        private val meesMissingExemptionReasonOccupied =
            PropertyComplianceBuilder.createWithInDateCertsAndLowEpcRating(
                propertyIsOccupied = true,
            )
        private val naturallyExpired =
            PropertyComplianceBuilder()
                .withOccupiedPropertyOwnership()
                .withGasSafetyCert()
                .withElectricalSafety()
                .withElectricalCertType()
                .withExpiredEpc()
                .withTenancyStartedBeforeEpcExpiry(null)
                .build()
        private val naturallyExpiredUnoccupied =
            PropertyComplianceBuilder()
                .withUnoccupiedPropertyOwnership()
                .withGasSafetyCert()
                .withElectricalSafety()
                .withElectricalCertType()
                .withExpiredEpc()
                .withTenancyStartedBeforeEpcExpiry(null)
                .build()
        private val naturallyExpiredWithLowRating =
            PropertyComplianceBuilder()
                .withOccupiedPropertyOwnership()
                .withGasSafetyCert()
                .withElectricalSafety()
                .withElectricalCertType()
                .withExpiredEpc()
                .withLowEpcRating()
                .withTenancyStartedBeforeEpcExpiry(null)
                .build()
        private val naturallyExpiredWithLowRatingAndMeesExemption =
            PropertyComplianceBuilder()
                .withOccupiedPropertyOwnership()
                .withGasSafetyCert()
                .withElectricalSafety()
                .withElectricalCertType()
                .withExpiredEpc()
                .withLowEpcRating()
                .withMeesExemption(MeesExemptionReason.WALL_INSULATION)
                .withTenancyStartedBeforeEpcExpiry(null)
                .build()

        @JvmStatic
        private fun provideEpcRows() =
            arrayOf(
                arguments(
                    named(
                        "with compliant epc",
                        compliant,
                    ),
                    listOf(
                        SummaryListRowViewModel(
                            "propertyDetails.complianceInformation.certificateStatus",
                            TagValue.VALID,
                        ),
                        SummaryListRowViewModel(
                            "propertyDetails.complianceInformation.energyPerformance.energyRating",
                            compliant.epcEnergyRating?.uppercase(),
                        ),
                        SummaryListRowViewModel(
                            "propertyDetails.complianceInformation.energyPerformance.expiryDate",
                            compliant.epcExpiryDate,
                        ),
                        SummaryListRowViewModel(
                            "propertyDetails.complianceInformation.energyPerformance.certificateNumber",
                            compliant.epcUrl?.substringAfterLast("/"),
                        ),
                    ),
                ),
                arguments(
                    named(
                        "with expired epc",
                        expired,
                    ),
                    listOf(
                        SummaryListRowViewModel(
                            "propertyDetails.complianceInformation.certificateStatus",
                            TagValue.EXPIRED,
                        ),
                        SummaryListRowViewModel(
                            "propertyDetails.complianceInformation.energyPerformance.energyRating",
                            expired.epcEnergyRating?.uppercase(),
                        ),
                        SummaryListRowViewModel(
                            "propertyDetails.complianceInformation.energyPerformance.expiryDate",
                            expired.epcExpiryDate,
                        ),
                        SummaryListRowViewModel(
                            "propertyDetails.complianceInformation.energyPerformance.certificateNumber",
                            expired.epcUrl?.substringAfterLast("/"),
                        ),
                    ),
                ),
                arguments(
                    named(
                        "with expired epc and unoccupied property",
                        expiredUnoccupied,
                    ),
                    listOf(
                        SummaryListRowViewModel(
                            "propertyDetails.complianceInformation.certificateStatus",
                            TagValue.EXPIRED,
                        ),
                        SummaryListRowViewModel(
                            "propertyDetails.complianceInformation.energyPerformance.energyRating",
                            expiredUnoccupied.epcEnergyRating?.uppercase(),
                        ),
                        SummaryListRowViewModel(
                            "propertyDetails.complianceInformation.energyPerformance.expiryDate",
                            expiredUnoccupied.epcExpiryDate,
                        ),
                        SummaryListRowViewModel(
                            "propertyDetails.complianceInformation.energyPerformance.certificateNumber",
                            expiredUnoccupied.epcUrl?.substringAfterLast("/"),
                        ),
                    ),
                ),
                arguments(
                    named(
                        "with expired epc but tenancy started before expiry",
                        expiredWithTenancyBeforeExpiry,
                    ),
                    listOf(
                        SummaryListRowViewModel(
                            "propertyDetails.complianceInformation.certificateStatus",
                            TagValue.VALID,
                        ),
                        SummaryListRowViewModel(
                            "propertyDetails.complianceInformation.energyPerformance.energyRating",
                            expiredWithTenancyBeforeExpiry.epcEnergyRating?.uppercase(),
                        ),
                        SummaryListRowViewModel(
                            "propertyDetails.complianceInformation.energyPerformance.expiryDate",
                            expiredWithTenancyBeforeExpiry.epcExpiryDate,
                        ),
                        SummaryListRowViewModel(
                            "propertyDetails.complianceInformation.energyPerformance.certificateNumber",
                            expiredWithTenancyBeforeExpiry.epcUrl?.substringAfterLast("/"),
                        ),
                    ),
                ),
                arguments(
                    named(
                        "with expired epc, tenancy before expiry, and low rating (MEES rows in supplementary)",
                        expiredWithTenancyBeforeExpiryAndLowRating,
                    ),
                    listOf(
                        SummaryListRowViewModel(
                            "propertyDetails.complianceInformation.certificateStatus",
                            TagValue.EXPIRED,
                        ),
                        SummaryListRowViewModel(
                            "propertyDetails.complianceInformation.energyPerformance.energyRating",
                            expiredWithTenancyBeforeExpiryAndLowRating.epcEnergyRating?.uppercase(),
                        ),
                        SummaryListRowViewModel(
                            "propertyDetails.complianceInformation.energyPerformance.expiryDate",
                            expiredWithTenancyBeforeExpiryAndLowRating.epcExpiryDate,
                        ),
                        SummaryListRowViewModel(
                            "propertyDetails.complianceInformation.energyPerformance.certificateNumber",
                            expiredWithTenancyBeforeExpiryAndLowRating.epcUrl?.substringAfterLast("/"),
                        ),
                    ),
                ),
                arguments(
                    named(
                        "with epc exemption",
                        exempt,
                    ),
                    listOf(
                        SummaryListRowViewModel(
                            "propertyDetails.complianceInformation.energyPerformance.hasEpc",
                            "commonText.no",
                        ),
                        SummaryListRowViewModel(
                            "propertyDetails.complianceInformation.energyPerformance.isEpcRequired",
                            "commonText.no",
                        ),
                        SummaryListRowViewModel(
                            "propertyDetails.complianceInformation.energyPerformance.epcExemption",
                            MessageKeyConverter.convert(EpcExemptionReason.DUE_FOR_DEMOLITION),
                        ),
                    ),
                ),
                arguments(
                    named(
                        "without epc and unoccupied",
                        missing,
                    ),
                    listOf(
                        SummaryListRowViewModel(
                            "propertyDetails.complianceInformation.energyPerformance.hasEpc",
                            "propertyDetails.complianceInformation.energyPerformance.provideEpcLaterUnoccupied",
                        ),
                    ),
                ),
                arguments(
                    named(
                        "without epc and occupied (no cert)",
                        missingOccupiedNoCert,
                    ),
                    listOf(
                        SummaryListRowViewModel(
                            "propertyDetails.complianceInformation.energyPerformance.hasEpc",
                            "commonText.no",
                        ),
                        SummaryListRowViewModel(
                            "propertyDetails.complianceInformation.energyPerformance.isEpcRequired",
                            "commonText.yes",
                        ),
                    ),
                ),
                arguments(
                    named(
                        "without epc and occupied (provide later)",
                        missingOccupiedProvideLater,
                    ),
                    listOf(
                        SummaryListRowViewModel(
                            "propertyDetails.complianceInformation.energyPerformance.hasEpc",
                            expectedDeadlineText,
                        ),
                    ),
                ),
                arguments(
                    named(
                        "with low rating epc and mees exemption",
                        meesCompliant,
                    ),
                    listOf(
                        SummaryListRowViewModel(
                            "propertyDetails.complianceInformation.certificateStatus",
                            TagValue.VALID,
                        ),
                        SummaryListRowViewModel(
                            "propertyDetails.complianceInformation.energyPerformance.energyRating",
                            meesCompliant.epcEnergyRating?.uppercase(),
                        ),
                        SummaryListRowViewModel(
                            "propertyDetails.complianceInformation.energyPerformance.expiryDate",
                            meesCompliant.epcExpiryDate,
                        ),
                        SummaryListRowViewModel(
                            "propertyDetails.complianceInformation.energyPerformance.certificateNumber",
                            meesCompliant.epcUrl?.substringAfterLast("/"),
                        ),
                    ),
                ),
                arguments(
                    named(
                        "with low rating epc and without mees exemption",
                        meesMissingExemptionReason,
                    ),
                    listOf(
                        SummaryListRowViewModel(
                            "propertyDetails.complianceInformation.energyPerformance.energyRating",
                            meesMissingExemptionReason.epcEnergyRating?.uppercase(),
                        ),
                        SummaryListRowViewModel(
                            "propertyDetails.complianceInformation.energyPerformance.expiryDate",
                            meesMissingExemptionReason.epcExpiryDate,
                        ),
                        SummaryListRowViewModel(
                            "propertyDetails.complianceInformation.energyPerformance.certificateNumber",
                            meesMissingExemptionReason.epcUrl?.substringAfterLast("/"),
                        ),
                    ),
                ),
                arguments(
                    named(
                        "with low rating epc and without mees exemption (occupied)",
                        meesMissingExemptionReasonOccupied,
                    ),
                    listOf(
                        SummaryListRowViewModel(
                            "propertyDetails.complianceInformation.energyPerformance.energyRating",
                            meesMissingExemptionReasonOccupied.epcEnergyRating?.uppercase(),
                        ),
                        SummaryListRowViewModel(
                            "propertyDetails.complianceInformation.energyPerformance.expiryDate",
                            meesMissingExemptionReasonOccupied.epcExpiryDate,
                        ),
                        SummaryListRowViewModel(
                            "propertyDetails.complianceInformation.energyPerformance.certificateNumber",
                            meesMissingExemptionReasonOccupied.epcUrl?.substringAfterLast("/"),
                        ),
                    ),
                ),
                arguments(
                    named(
                        "with naturally expired epc (occupied)",
                        naturallyExpired,
                    ),
                    listOf(
                        SummaryListRowViewModel(
                            "propertyDetails.complianceInformation.certificateStatus",
                            TagValue.EXPIRED,
                        ),
                        SummaryListRowViewModel(
                            "propertyDetails.complianceInformation.energyPerformance.energyRating",
                            naturallyExpired.epcEnergyRating?.uppercase(),
                        ),
                        SummaryListRowViewModel(
                            "propertyDetails.complianceInformation.energyPerformance.expiryDate",
                            naturallyExpired.epcExpiryDate,
                        ),
                        SummaryListRowViewModel(
                            "propertyDetails.complianceInformation.energyPerformance.certificateNumber",
                            naturallyExpired.epcUrl?.substringAfterLast("/"),
                        ),
                    ),
                ),
                arguments(
                    named(
                        "with naturally expired epc (unoccupied)",
                        naturallyExpiredUnoccupied,
                    ),
                    listOf(
                        SummaryListRowViewModel(
                            "propertyDetails.complianceInformation.certificateStatus",
                            TagValue.EXPIRED,
                        ),
                        SummaryListRowViewModel(
                            "propertyDetails.complianceInformation.energyPerformance.energyRating",
                            naturallyExpiredUnoccupied.epcEnergyRating?.uppercase(),
                        ),
                        SummaryListRowViewModel(
                            "propertyDetails.complianceInformation.energyPerformance.expiryDate",
                            naturallyExpiredUnoccupied.epcExpiryDate,
                        ),
                        SummaryListRowViewModel(
                            "propertyDetails.complianceInformation.energyPerformance.certificateNumber",
                            naturallyExpiredUnoccupied.epcUrl?.substringAfterLast("/"),
                        ),
                    ),
                ),
                arguments(
                    named(
                        "with naturally expired epc and low rating (occupied)",
                        naturallyExpiredWithLowRating,
                    ),
                    listOf(
                        SummaryListRowViewModel(
                            "propertyDetails.complianceInformation.certificateStatus",
                            TagValue.EXPIRED,
                        ),
                        SummaryListRowViewModel(
                            "propertyDetails.complianceInformation.energyPerformance.energyRating",
                            naturallyExpiredWithLowRating.epcEnergyRating?.uppercase(),
                        ),
                        SummaryListRowViewModel(
                            "propertyDetails.complianceInformation.energyPerformance.expiryDate",
                            naturallyExpiredWithLowRating.epcExpiryDate,
                        ),
                        SummaryListRowViewModel(
                            "propertyDetails.complianceInformation.energyPerformance.certificateNumber",
                            naturallyExpiredWithLowRating.epcUrl?.substringAfterLast("/"),
                        ),
                    ),
                ),
                arguments(
                    named(
                        "with naturally expired epc, low rating, with exemption (occupied)",
                        naturallyExpiredWithLowRatingAndMeesExemption,
                    ),
                    listOf(
                        SummaryListRowViewModel(
                            "propertyDetails.complianceInformation.certificateStatus",
                            TagValue.EXPIRED,
                        ),
                        SummaryListRowViewModel(
                            "propertyDetails.complianceInformation.energyPerformance.energyRating",
                            naturallyExpiredWithLowRatingAndMeesExemption.epcEnergyRating?.uppercase(),
                        ),
                        SummaryListRowViewModel(
                            "propertyDetails.complianceInformation.energyPerformance.expiryDate",
                            naturallyExpiredWithLowRatingAndMeesExemption.epcExpiryDate,
                        ),
                        SummaryListRowViewModel(
                            "propertyDetails.complianceInformation.energyPerformance.certificateNumber",
                            naturallyExpiredWithLowRatingAndMeesExemption.epcUrl?.substringAfterLast("/"),
                        ),
                    ),
                ),
            )

        @JvmStatic
        private fun provideInsetTextKeys() =
            arrayOf(
                arguments(named("with compliant epc", compliant), null),
                arguments(named("without epc and unoccupied", missing), null),
                arguments(
                    named("without epc and occupied (no cert)", missingOccupiedNoCert),
                    "propertyDetails.complianceInformation.energyPerformance.occupiedNoEpcInset",
                ),
                arguments(named("without epc and occupied (provide later)", missingOccupiedProvideLater), null),
                arguments(named("with epc exemption", exempt), null),
                arguments(
                    named("with expired epc (occupied, no tenancy before expiry)", expired),
                    "propertyDetails.complianceInformation.energyPerformance.occupiedNoEpcInset",
                ),
                arguments(named("with expired epc (unoccupied)", expiredUnoccupied), null),
                arguments(
                    named(
                        "with expired epc, tenancy before expiry, low rating, no exemption (occupied)",
                        expiredWithTenancyBeforeExpiryAndLowRating,
                    ),
                    "propertyDetails.complianceInformation.energyPerformance.occupiedNoEpcInset",
                ),
                arguments(
                    named(
                        "with expired epc, tenancy before expiry, low rating, with exemption",
                        expiredWithTenancyBeforeExpiryAndLowRatingAndMeesExemption,
                    ),
                    null,
                ),
                arguments(named("with expired epc, tenancy before expiry (no low rating)", expiredWithTenancyBeforeExpiry), null),
                arguments(
                    named("with low rating epc, no exemption, occupied (non-expired)", meesMissingExemptionReasonOccupied),
                    "propertyDetails.complianceInformation.energyPerformance.occupiedNoEpcInset",
                ),
                arguments(named("with low rating epc, no exemption, unoccupied (non-expired)", meesMissingExemptionReason), null),
                arguments(named("with low rating epc, with mees exemption (non-expired)", meesCompliant), null),
                arguments(named("with naturally expired epc (occupied)", naturallyExpired), null),
                arguments(named("with naturally expired epc (unoccupied)", naturallyExpiredUnoccupied), null),
                arguments(
                    named("with naturally expired epc and low rating (occupied)", naturallyExpiredWithLowRating),
                    "propertyDetails.complianceInformation.energyPerformance.occupiedNoEpcInset",
                ),
                arguments(
                    named(
                        "with naturally expired epc, low rating, with exemption (occupied)",
                        naturallyExpiredWithLowRatingAndMeesExemption,
                    ),
                    null,
                ),
            )

        @JvmStatic
        private fun provideSupplementarySections() =
            arrayOf(
                arguments(named("with compliant epc", compliant), emptyList<SummaryCardSupplementarySection>()),
                arguments(
                    named("with expired epc (occupied, no tenancy before expiry)", expired),
                    listOf(
                        SummaryCardSupplementarySection(
                            bodyTextKey = "propertyDetails.complianceInformation.energyPerformance.epcHasExpired",
                            rows =
                                listOf(
                                    SummaryListRowViewModel(
                                        "propertyDetails.complianceInformation.energyPerformance.wasEpcValidWhenTenancyBegan",
                                        "commonText.no",
                                    ),
                                ),
                        ),
                    ),
                ),
                arguments(
                    named("with expired epc (unoccupied)", expiredUnoccupied),
                    listOf(
                        SummaryCardSupplementarySection(
                            bodyTextKey = "propertyDetails.complianceInformation.energyPerformance.epcHasExpired",
                            rows =
                                listOf(
                                    SummaryListRowViewModel(
                                        "propertyDetails.complianceInformation.energyPerformance.wasEpcValidWhenTenancyBegan",
                                        "commonText.no",
                                    ),
                                ),
                        ),
                    ),
                ),
                arguments(
                    named("with expired epc but tenancy started before expiry", expiredWithTenancyBeforeExpiry),
                    listOf(
                        SummaryCardSupplementarySection(
                            bodyTextKey = "propertyDetails.complianceInformation.energyPerformance.epcHasExpired",
                            rows =
                                listOf(
                                    SummaryListRowViewModel(
                                        "propertyDetails.complianceInformation.energyPerformance.wasEpcValidWhenTenancyBegan",
                                        "commonText.yes",
                                    ),
                                ),
                        ),
                    ),
                ),
                arguments(
                    named(
                        "with expired epc, tenancy before expiry, low rating, and mees exemption",
                        expiredWithTenancyBeforeExpiryAndLowRatingAndMeesExemption,
                    ),
                    listOf(
                        SummaryCardSupplementarySection(
                            bodyTextKey = "propertyDetails.complianceInformation.energyPerformance.epcHasExpired",
                            rows =
                                listOf(
                                    SummaryListRowViewModel(
                                        "propertyDetails.complianceInformation.energyPerformance.wasEpcValidWhenTenancyBegan",
                                        "commonText.yes",
                                    ),
                                ),
                        ),
                        SummaryCardSupplementarySection(
                            bodyTextKey = "propertyDetails.complianceInformation.energyPerformance.lowRatingText",
                            rows =
                                listOf(
                                    SummaryListRowViewModel(
                                        "propertyDetails.complianceInformation.energyPerformance.energyEfficiencyExemption",
                                        MessageKeyConverter.convert(true),
                                    ),
                                    SummaryListRowViewModel(
                                        "propertyDetails.complianceInformation.energyPerformance.registeredExemption",
                                        MessageKeyConverter.convert(MeesExemptionReason.PROPERTY_DEVALUATION),
                                    ),
                                ),
                        ),
                    ),
                ),
                arguments(
                    named(
                        "with expired epc, tenancy before expiry, and low rating without mees exemption",
                        expiredWithTenancyBeforeExpiryAndLowRating,
                    ),
                    listOf(
                        SummaryCardSupplementarySection(
                            bodyTextKey = "propertyDetails.complianceInformation.energyPerformance.epcHasExpired",
                            rows =
                                listOf(
                                    SummaryListRowViewModel(
                                        "propertyDetails.complianceInformation.energyPerformance.wasEpcValidWhenTenancyBegan",
                                        "commonText.yes",
                                    ),
                                ),
                        ),
                        SummaryCardSupplementarySection(
                            bodyTextKey = "propertyDetails.complianceInformation.energyPerformance.lowRatingText",
                            rows =
                                listOf(
                                    SummaryListRowViewModel(
                                        "propertyDetails.complianceInformation.energyPerformance.energyEfficiencyExemption",
                                        MessageKeyConverter.convert(false),
                                    ),
                                ),
                        ),
                    ),
                ),
                arguments(named("without epc", missing), emptyList<SummaryCardSupplementarySection>()),
                arguments(
                    named("with low rating epc and mees exemption (non-expired)", meesCompliant),
                    listOf(
                        SummaryCardSupplementarySection(
                            bodyTextKey = "propertyDetails.complianceInformation.energyPerformance.lowRatingText",
                            rows =
                                listOf(
                                    SummaryListRowViewModel(
                                        "propertyDetails.complianceInformation.energyPerformance.energyEfficiencyExemption",
                                        MessageKeyConverter.convert(true),
                                    ),
                                    SummaryListRowViewModel(
                                        "propertyDetails.complianceInformation.energyPerformance.registeredExemption",
                                        MessageKeyConverter.convert(MeesExemptionReason.PROPERTY_DEVALUATION),
                                    ),
                                ),
                        ),
                    ),
                ),
                arguments(
                    named("with low rating epc, no exemption (non-expired)", meesMissingExemptionReason),
                    listOf(
                        SummaryCardSupplementarySection(
                            bodyTextKey = "propertyDetails.complianceInformation.energyPerformance.lowRatingText",
                            rows =
                                listOf(
                                    SummaryListRowViewModel(
                                        "propertyDetails.complianceInformation.energyPerformance.energyEfficiencyExemption",
                                        MessageKeyConverter.convert(false),
                                    ),
                                ),
                        ),
                    ),
                ),
                arguments(
                    named("with low rating epc, no exemption, occupied (non-expired)", meesMissingExemptionReasonOccupied),
                    listOf(
                        SummaryCardSupplementarySection(
                            bodyTextKey = "propertyDetails.complianceInformation.energyPerformance.lowRatingText",
                            rows =
                                listOf(
                                    SummaryListRowViewModel(
                                        "propertyDetails.complianceInformation.energyPerformance.energyEfficiencyExemption",
                                        MessageKeyConverter.convert(false),
                                    ),
                                ),
                        ),
                    ),
                ),
                arguments(
                    named("with naturally expired epc (occupied)", naturallyExpired),
                    emptyList<SummaryCardSupplementarySection>(),
                ),
                arguments(
                    named("with naturally expired epc (unoccupied)", naturallyExpiredUnoccupied),
                    emptyList<SummaryCardSupplementarySection>(),
                ),
                arguments(
                    named("with naturally expired epc and low rating (occupied)", naturallyExpiredWithLowRating),
                    listOf(
                        SummaryCardSupplementarySection(
                            bodyTextKey = "propertyDetails.complianceInformation.energyPerformance.lowRatingText",
                            rows =
                                listOf(
                                    SummaryListRowViewModel(
                                        "propertyDetails.complianceInformation.energyPerformance.energyEfficiencyExemption",
                                        MessageKeyConverter.convert(false),
                                    ),
                                ),
                        ),
                    ),
                ),
                arguments(
                    named(
                        "with naturally expired epc, low rating, with exemption (occupied)",
                        naturallyExpiredWithLowRatingAndMeesExemption,
                    ),
                    listOf(
                        SummaryCardSupplementarySection(
                            bodyTextKey = "propertyDetails.complianceInformation.energyPerformance.lowRatingText",
                            rows =
                                listOf(
                                    SummaryListRowViewModel(
                                        "propertyDetails.complianceInformation.energyPerformance.energyEfficiencyExemption",
                                        MessageKeyConverter.convert(true),
                                    ),
                                    SummaryListRowViewModel(
                                        "propertyDetails.complianceInformation.energyPerformance.registeredExemption",
                                        MessageKeyConverter.convert(MeesExemptionReason.WALL_INSULATION),
                                    ),
                                ),
                        ),
                    ),
                ),
            )

        @JvmStatic
        private fun provideEpcExpiredInsetViewModel() =
            arrayOf(
                arguments(named("with compliant epc", compliant), null),
                arguments(named("with expired epc (tenancy not before expiry)", expired), null),
                arguments(named("with expired epc (unoccupied)", expiredUnoccupied), null),
                arguments(named("with expired epc, tenancy before expiry", expiredWithTenancyBeforeExpiry), null),
                arguments(named("without epc", missing), null),
                arguments(named("with low rating epc, no exemption (non-expired)", meesMissingExemptionReason), null),
                arguments(
                    named("with naturally expired epc (occupied)", naturallyExpired),
                    expectedNaturallyExpiredInsetViewModel,
                ),
                arguments(
                    named("with naturally expired epc (unoccupied)", naturallyExpiredUnoccupied),
                    null,
                ),
                arguments(
                    named("with naturally expired epc and low rating (occupied)", naturallyExpiredWithLowRating),
                    null,
                ),
                arguments(
                    named(
                        "with naturally expired epc, low rating, with exemption (occupied)",
                        naturallyExpiredWithLowRatingAndMeesExemption,
                    ),
                    expectedNaturallyExpiredInsetViewModel,
                ),
            )
    }
}
