package uk.gov.communities.prsdb.webapp.forms.pages

import org.springframework.web.servlet.ModelAndView
import uk.gov.communities.prsdb.webapp.constants.EPC_ACCEPTABLE_RATING_RANGE
import uk.gov.communities.prsdb.webapp.forms.JourneyData
import uk.gov.communities.prsdb.webapp.forms.pages.cya.EicrSummaryRowsFactory
import uk.gov.communities.prsdb.webapp.forms.pages.cya.GasSafetySummaryRowsFactory
import uk.gov.communities.prsdb.webapp.forms.steps.PropertyComplianceStepId
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyComplianceJourneyDataExtensions.Companion.getAcceptedEpcDetails
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyComplianceJourneyDataExtensions.Companion.getDidTenancyStartBeforeEpcExpiry
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyComplianceJourneyDataExtensions.Companion.getEpcExemptionReason
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyComplianceJourneyDataExtensions.Companion.getHasCompletedEpcExemptionConfirmation
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyComplianceJourneyDataExtensions.Companion.getHasCompletedEpcExpired
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyComplianceJourneyDataExtensions.Companion.getHasCompletedEpcMissing
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyComplianceJourneyDataExtensions.Companion.getHasCompletedEpcNotFound
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
    val gasSafetyDataFactory =
        GasSafetySummaryRowsFactory(
            doesDataHaveGasSafetyCert = { data -> data.getHasGasSafetyCert()!! },
            gasSafetyStartingStep = PropertyComplianceStepId.GasSafety,
            changeExemptionStep = PropertyComplianceStepId.GasSafetyExemption,
        )
    val eicrDataFactory =
        EicrSummaryRowsFactory(
            doesDataHaveEicr = { data -> data.getHasEICR()!! },
            eicrSafetyStartingStep = PropertyComplianceStepId.EICR,
            changeExemptionStep = PropertyComplianceStepId.EicrExemption,
        )

    override fun addPageContentToModel(
        modelAndView: ModelAndView,
        filteredJourneyData: JourneyData,
    ) {
        modelAndView.addObject("propertyAddress", propertyAddressProvider())
        modelAndView.addObject("gasSafetyData", gasSafetyDataFactory.createRows(filteredJourneyData))
        modelAndView.addObject("eicrData", eicrDataFactory.createRows(filteredJourneyData))
        modelAndView.addObject("epcData", getEpcData(filteredJourneyData))
        modelAndView.addObject("responsibilityData", getResponsibilityData(filteredJourneyData))
    }

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
