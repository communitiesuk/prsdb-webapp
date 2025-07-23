package uk.gov.communities.prsdb.webapp.forms.pages

import kotlinx.datetime.DatePeriod
import kotlinx.datetime.plus
import org.springframework.web.servlet.ModelAndView
import uk.gov.communities.prsdb.webapp.constants.EICR_VALIDITY_YEARS
import uk.gov.communities.prsdb.webapp.constants.EPC_ACCEPTABLE_RATING_RANGE
import uk.gov.communities.prsdb.webapp.constants.GAS_SAFETY_CERT_VALIDITY_YEARS
import uk.gov.communities.prsdb.webapp.constants.enums.EicrExemptionReason
import uk.gov.communities.prsdb.webapp.constants.enums.GasSafetyExemptionReason
import uk.gov.communities.prsdb.webapp.forms.JourneyData
import uk.gov.communities.prsdb.webapp.forms.steps.PropertyComplianceStepId
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyComplianceJourneyDataExtensions.Companion.getAcceptedEpcDetails
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyComplianceJourneyDataExtensions.Companion.getDidTenancyStartBeforeEpcExpiry
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyComplianceJourneyDataExtensions.Companion.getEicrExemptionOtherReason
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyComplianceJourneyDataExtensions.Companion.getEicrExemptionReason
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyComplianceJourneyDataExtensions.Companion.getEicrIssueDate
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyComplianceJourneyDataExtensions.Companion.getEpcExemptionReason
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyComplianceJourneyDataExtensions.Companion.getGasSafetyCertEngineerNum
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyComplianceJourneyDataExtensions.Companion.getGasSafetyCertExemptionOtherReason
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyComplianceJourneyDataExtensions.Companion.getGasSafetyCertExemptionReason
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyComplianceJourneyDataExtensions.Companion.getGasSafetyCertIssueDate
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyComplianceJourneyDataExtensions.Companion.getHasCompletedEicrExemptionConfirmation
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyComplianceJourneyDataExtensions.Companion.getHasCompletedEicrExemptionMissing
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyComplianceJourneyDataExtensions.Companion.getHasCompletedEicrUploadConfirmation
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyComplianceJourneyDataExtensions.Companion.getHasCompletedEpcExemptionConfirmation
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyComplianceJourneyDataExtensions.Companion.getHasCompletedEpcExpired
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyComplianceJourneyDataExtensions.Companion.getHasCompletedEpcMissing
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyComplianceJourneyDataExtensions.Companion.getHasCompletedEpcNotFound
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyComplianceJourneyDataExtensions.Companion.getHasCompletedGasSafetyExemptionConfirmation
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyComplianceJourneyDataExtensions.Companion.getHasCompletedGasSafetyExemptionMissing
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyComplianceJourneyDataExtensions.Companion.getHasCompletedGasSafetyUploadConfirmation
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyComplianceJourneyDataExtensions.Companion.getHasEICR
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyComplianceJourneyDataExtensions.Companion.getHasFireSafetyDeclaration
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyComplianceJourneyDataExtensions.Companion.getHasGasSafetyCert
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyComplianceJourneyDataExtensions.Companion.getMeesExemptionReason
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.SummaryListRowViewModel
import uk.gov.communities.prsdb.webapp.services.EpcCertificateUrlProvider
import uk.gov.communities.prsdb.webapp.services.JourneyDataService

class PropertyComplianceCheckAnswersPage(
    journeyDataService: JourneyDataService,
    private val epcCertificateUrlProvider: EpcCertificateUrlProvider,
    private val propertyAddressProvider: () -> String,
) : CheckAnswersPage(
        content = emptyMap(),
        journeyDataService = journeyDataService,
        templateName = "forms/propertyComplianceCheckAnswersForm",
    ) {
    override fun addPageContentToModel(
        modelAndView: ModelAndView,
        filteredJourneyData: JourneyData,
    ) {
        modelAndView.addObject("propertyAddress", propertyAddressProvider())
        modelAndView.addObject("gasSafetyData", getGasSafetyData(filteredJourneyData))
        modelAndView.addObject("eicrData", getEicrData(filteredJourneyData))
        modelAndView.addObject("epcData", getEpcData(filteredJourneyData))
        modelAndView.addObject("responsibilityData", getResponsibilityData(filteredJourneyData))
    }

    private fun getGasSafetyData(filteredJourneyData: JourneyData) =
        mutableListOf<SummaryListRowViewModel>()
            .apply {
                add(getGasSafetyCertStatusRow(filteredJourneyData))
                if (filteredJourneyData.getHasGasSafetyCert()!!) {
                    addAll(getGasSafetyCertDetailRows(filteredJourneyData))
                } else {
                    add(getGasSafetyExemptionRow(filteredJourneyData))
                }
            }.toList()

    private fun getEicrData(filteredJourneyData: JourneyData) =
        mutableListOf<SummaryListRowViewModel>()
            .apply {
                add(getEicrStatusRow(filteredJourneyData))
                if (filteredJourneyData.getHasEICR()!!) {
                    addAll(getEicrDetailRows(filteredJourneyData))
                } else {
                    add(getEicrExemptionRow(filteredJourneyData))
                }
            }.toList()

    private fun getEpcData(filteredJourneyData: JourneyData) =
        mutableListOf<SummaryListRowViewModel>()
            .apply {
                add(getEpcStatusRow(filteredJourneyData))
                if (filteredJourneyData.getAcceptedEpcDetails() != null) {
                    addAll(getEpcDetailRows(filteredJourneyData))
                } else {
                    add(getEpcExemptionRow(filteredJourneyData))
                }
            }.toList()

    private fun getResponsibilityData(filteredJourneyData: JourneyData) =
        listOf(
            SummaryListRowViewModel.forCheckYourAnswersPage(
                "forms.checkComplianceAnswers.responsibilities.fireSafety",
                filteredJourneyData.getHasFireSafetyDeclaration()!!,
                PropertyComplianceStepId.FireSafetyDeclaration.urlPathSegment,
            ),
            SummaryListRowViewModel.forCheckYourAnswersPage(
                "forms.checkComplianceAnswers.responsibilities.keepPropertySafe",
                true,
                PropertyComplianceStepId.KeepPropertySafe.urlPathSegment,
                actionValue = "forms.links.view",
            ),
            SummaryListRowViewModel.forCheckYourAnswersPage(
                "forms.checkComplianceAnswers.responsibilities.responsibilityToTenants",
                true,
                PropertyComplianceStepId.ResponsibilityToTenants.urlPathSegment,
                actionValue = "forms.links.view",
            ),
        )

    private fun getGasSafetyCertStatusRow(filteredJourneyData: JourneyData): SummaryListRowViewModel {
        val fieldValue =
            // TODO PRSD-976: Add link to gas safety cert (or appropriate message if virus scan failed)
            if (filteredJourneyData.getHasCompletedGasSafetyUploadConfirmation()) {
                "forms.checkComplianceAnswers.gasSafety.download"
            } else if (filteredJourneyData.getHasCompletedGasSafetyExemptionConfirmation()) {
                "forms.checkComplianceAnswers.certificate.notRequired"
            } else if (filteredJourneyData.getHasCompletedGasSafetyExemptionMissing()) {
                "forms.checkComplianceAnswers.certificate.notAdded"
            } else {
                "forms.checkComplianceAnswers.certificate.expired"
            }

        return SummaryListRowViewModel.forCheckYourAnswersPage(
            "forms.checkComplianceAnswers.gasSafety.certificate",
            fieldValue,
            PropertyComplianceStepId.GasSafety.urlPathSegment,
        )
    }

    private fun getGasSafetyCertDetailRows(filteredJourneyData: JourneyData) =
        mutableListOf<SummaryListRowViewModel>()
            .apply {
                val issueDate = filteredJourneyData.getGasSafetyCertIssueDate()!!
                addAll(
                    listOf(
                        SummaryListRowViewModel.forCheckYourAnswersPage(
                            "forms.checkComplianceAnswers.certificate.issueDate",
                            issueDate,
                            PropertyComplianceStepId.GasSafetyIssueDate.urlPathSegment,
                        ),
                        SummaryListRowViewModel.forCheckYourAnswersPage(
                            "forms.checkComplianceAnswers.certificate.validUntil",
                            issueDate.plus(DatePeriod(years = GAS_SAFETY_CERT_VALIDITY_YEARS)),
                            null,
                        ),
                    ),
                )

                val engineerNum = filteredJourneyData.getGasSafetyCertEngineerNum()
                if (engineerNum != null) {
                    add(
                        SummaryListRowViewModel.forCheckYourAnswersPage(
                            "forms.checkComplianceAnswers.gasSafety.engineerNumber",
                            engineerNum,
                            PropertyComplianceStepId.GasSafetyEngineerNum.urlPathSegment,
                        ),
                    )
                }
            }.toList()

    private fun getGasSafetyExemptionRow(filteredJourneyData: JourneyData): SummaryListRowViewModel {
        val fieldValue: Any =
            when (val exemptionReason = filteredJourneyData.getGasSafetyCertExemptionReason()) {
                null -> "commonText.none"
                GasSafetyExemptionReason.OTHER -> listOf(exemptionReason, filteredJourneyData.getGasSafetyCertExemptionOtherReason())
                else -> exemptionReason
            }

        return SummaryListRowViewModel.forCheckYourAnswersPage(
            "forms.checkComplianceAnswers.certificate.exemption",
            fieldValue,
            PropertyComplianceStepId.GasSafetyExemption.urlPathSegment,
        )
    }

    private fun getEicrStatusRow(filteredJourneyData: JourneyData): SummaryListRowViewModel {
        val fieldValue =
            // TODO PRSD-976: Add link to gas safety cert (or appropriate message if virus scan failed)
            if (filteredJourneyData.getHasCompletedEicrUploadConfirmation()) {
                "forms.checkComplianceAnswers.eicr.download"
            } else if (filteredJourneyData.getHasCompletedEicrExemptionConfirmation()) {
                "forms.checkComplianceAnswers.certificate.notRequired"
            } else if (filteredJourneyData.getHasCompletedEicrExemptionMissing()) {
                "forms.checkComplianceAnswers.certificate.notAdded"
            } else {
                "forms.checkComplianceAnswers.certificate.expired"
            }

        return SummaryListRowViewModel.forCheckYourAnswersPage(
            "forms.checkComplianceAnswers.eicr.certificate",
            fieldValue,
            PropertyComplianceStepId.EICR.urlPathSegment,
        )
    }

    private fun getEicrDetailRows(filteredJourneyData: JourneyData): List<SummaryListRowViewModel> {
        val issueDate = filteredJourneyData.getEicrIssueDate()!!
        return listOf(
            SummaryListRowViewModel.forCheckYourAnswersPage(
                "forms.checkComplianceAnswers.certificate.issueDate",
                issueDate,
                PropertyComplianceStepId.EicrIssueDate.urlPathSegment,
            ),
            SummaryListRowViewModel.forCheckYourAnswersPage(
                "forms.checkComplianceAnswers.certificate.validUntil",
                issueDate.plus(DatePeriod(years = EICR_VALIDITY_YEARS)),
                null,
            ),
        )
    }

    private fun getEicrExemptionRow(filteredJourneyData: JourneyData): SummaryListRowViewModel {
        val fieldValue: Any =
            when (val exemptionReason = filteredJourneyData.getEicrExemptionReason()) {
                null -> "commonText.none"
                EicrExemptionReason.OTHER -> listOf(exemptionReason, filteredJourneyData.getEicrExemptionOtherReason())
                else -> exemptionReason
            }

        return SummaryListRowViewModel.forCheckYourAnswersPage(
            "forms.checkComplianceAnswers.certificate.exemption",
            fieldValue,
            PropertyComplianceStepId.EicrExemption.urlPathSegment,
        )
    }

    private fun getEpcStatusRow(filteredJourneyData: JourneyData): SummaryListRowViewModel {
        val fieldValue =
            if (filteredJourneyData.getHasCompletedEpcExemptionConfirmation()) {
                "forms.checkComplianceAnswers.certificate.notRequired"
            } else if (filteredJourneyData.getHasCompletedEpcExpired()) {
                "forms.checkComplianceAnswers.epc.viewExpired"
            } else if (filteredJourneyData.getHasCompletedEpcMissing() || filteredJourneyData.getHasCompletedEpcNotFound()) {
                "forms.checkComplianceAnswers.certificate.notAdded"
            } else {
                "forms.checkComplianceAnswers.epc.view"
            }

        val certificateNumber = filteredJourneyData.getAcceptedEpcDetails()?.certificateNumber
        val valueUrl =
            if (certificateNumber != null) {
                epcCertificateUrlProvider.getEpcCertificateUrl(certificateNumber)
            } else {
                null
            }

        return SummaryListRowViewModel.forCheckYourAnswersPage(
            "forms.checkComplianceAnswers.epc.certificate",
            fieldValue,
            PropertyComplianceStepId.EPC.urlPathSegment,
            valueUrl,
        )
    }

    private fun getEpcDetailRows(filteredJourneyData: JourneyData) =
        mutableListOf<SummaryListRowViewModel>()
            .apply {
                val epcDetails = filteredJourneyData.getAcceptedEpcDetails()!!
                add(
                    SummaryListRowViewModel.forCheckYourAnswersPage(
                        "forms.checkComplianceAnswers.epc.expiryDate",
                        epcDetails.expiryDate,
                        null,
                    ),
                )

                val expiryCheckResult = filteredJourneyData.getDidTenancyStartBeforeEpcExpiry()
                if (expiryCheckResult != null) {
                    add(
                        SummaryListRowViewModel.forCheckYourAnswersPage(
                            "forms.checkComplianceAnswers.epc.expiryCheck",
                            expiryCheckResult,
                            PropertyComplianceStepId.EpcExpiryCheck.urlPathSegment,
                        ),
                    )
                }

                add(
                    SummaryListRowViewModel.forCheckYourAnswersPage(
                        "forms.checkComplianceAnswers.epc.energyRating",
                        epcDetails.energyRating.uppercase(),
                        null,
                    ),
                )

                if (epcDetails.energyRating.uppercase() !in EPC_ACCEPTABLE_RATING_RANGE) {
                    val changeUrl =
                        if (filteredJourneyData.getHasCompletedEpcExpired()) {
                            PropertyComplianceStepId.EPC.urlPathSegment
                        } else {
                            PropertyComplianceStepId.MeesExemptionReason.urlPathSegment
                        }

                    add(
                        SummaryListRowViewModel.forCheckYourAnswersPage(
                            "forms.checkComplianceAnswers.epc.meesExemption",
                            filteredJourneyData.getMeesExemptionReason() ?: "commonText.none",
                            changeUrl,
                        ),
                    )
                }
            }.toList()

    private fun getEpcExemptionRow(filteredJourneyData: JourneyData): SummaryListRowViewModel {
        val changeUrl =
            if (filteredJourneyData.getHasCompletedEpcMissing() || filteredJourneyData.getHasCompletedEpcNotFound()) {
                PropertyComplianceStepId.EPC.urlPathSegment
            } else {
                PropertyComplianceStepId.EpcExemptionReason.urlPathSegment
            }

        return SummaryListRowViewModel.forCheckYourAnswersPage(
            "forms.checkComplianceAnswers.epc.exemption",
            filteredJourneyData.getEpcExemptionReason() ?: "commonText.none",
            changeUrl,
        )
    }
}
