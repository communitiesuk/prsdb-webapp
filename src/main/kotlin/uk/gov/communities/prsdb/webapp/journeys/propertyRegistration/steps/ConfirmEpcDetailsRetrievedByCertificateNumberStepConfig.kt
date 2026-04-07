package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.exceptions.NotNullFormModelValueIsNullException
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.EpcState
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.ConfirmEpcDetailsRetrievedByCertificateNumberStep.Companion.ROUTE_SEGMENT
import uk.gov.communities.prsdb.webapp.journeys.shared.YesOrNo
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.CheckMatchedEpcFormModel
import uk.gov.communities.prsdb.webapp.models.viewModels.formModels.RadiosButtonViewModel
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.SummaryCardActionViewModel
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.SummaryListRowViewModel
import uk.gov.communities.prsdb.webapp.services.EpcCertificateUrlProvider

@JourneyFrameworkComponent
class ConfirmEpcDetailsRetrievedByCertificateNumberStepConfig(
    private val epcCertificateUrlProvider: EpcCertificateUrlProvider,
) : AbstractConfirmEpcDetailsStepConfig() {
    // TODO PDJB-746 - update content as required
    override fun getStepSpecificContent(state: EpcState) =
        getRelevantEpc(state)?.let { epcDetails ->
            mapOf(
                "summaryCardTitle" to "propertyCompliance.epcTask.confirmEpcDetailsFromCertificateNumber.summaryCard.title",
                "summaryCardActions" to
                    listOf(
                        SummaryCardActionViewModel(
                            text = "propertyCompliance.epcTask.confirmEpcDetailsFromCertificateNumber.summaryCard.viewFullEpc",
                            url = epcCertificateUrlProvider.getEpcCertificateUrl(epcDetails.certificateNumber),
                            opensInNewTab = true,
                        ),
                    ),
                "summaryListRows" to
                    listOf(
                        SummaryListRowViewModel(
                            fieldHeading = "propertyCompliance.epcTask.confirmEpcDetailsFromCertificateNumber.summaryCard.address",
                            fieldValue = epcDetails.singleLineAddress,
                        ),
                        SummaryListRowViewModel(
                            fieldHeading =
                                "propertyCompliance.epcTask.confirmEpcDetailsFromCertificateNumber.summaryCard.energyEfficiencyRating",
                            fieldValue = epcDetails.energyRatingUppercase,
                        ),
                        SummaryListRowViewModel(
                            fieldHeading = "propertyCompliance.epcTask.confirmEpcDetailsFromCertificateNumber.summaryCard.expiryDate",
                            fieldValue = epcDetails.expiryDate,
                        ),
                        SummaryListRowViewModel(
                            fieldHeading =
                                "propertyCompliance.epcTask.confirmEpcDetailsFromCertificateNumber.summaryCard.certificateNumber",
                            fieldValue = epcDetails.certificateNumber,
                        ),
                    ),
                "radioOptions" to
                    listOf(
                        RadiosButtonViewModel(
                            value = true,
                            valueStr = "yes",
                            labelMsgKey = "forms.radios.option.yes.label",
                        ),
                        RadiosButtonViewModel(
                            value = false,
                            valueStr = "no",
                            labelMsgKey = "propertyCompliance.epcTask.confirmEpcDetailsFromCertificateNumber.radios.no.label",
                        ),
                    ),
            )
        } ?: throw NotNullFormModelValueIsNullException(
            "Attempting to access relevantEpc for ConfirmEpcDetailsRetrievedByCertificateNumberStepConfig but it was null.",
        )

    override fun chooseTemplate(state: EpcState): String = "forms/confirmEpcDetailsRetrievedByCertificateNumberForm"

    override fun afterStepIsReached(state: EpcState) {
        if (state.epcRetrievedByCertificateNumberUpdatedSinceUserReview == true) {
            state.clearStepData(ROUTE_SEGMENT)
            state.epcRetrievedByCertificateNumberUpdatedSinceUserReview = false
        }
    }
}

@JourneyFrameworkComponent
final class ConfirmEpcDetailsRetrievedByCertificateNumberStep(
    stepConfig: ConfirmEpcDetailsRetrievedByCertificateNumberStepConfig,
) : JourneyStep.RequestableStep<YesOrNo, CheckMatchedEpcFormModel, EpcState>(stepConfig) {
    companion object {
        const val ROUTE_SEGMENT = "confirm-epc-details"
    }
}
