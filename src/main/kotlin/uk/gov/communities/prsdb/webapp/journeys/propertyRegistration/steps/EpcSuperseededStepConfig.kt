package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.EpcState
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NoInputFormModel
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.SummaryCardActionViewModel
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.SummaryListRowViewModel
import uk.gov.communities.prsdb.webapp.services.EpcCertificateUrlProvider

@JourneyFrameworkComponent
class EpcSuperseededStepConfig(
    private val epcCertificateUrlProvider: EpcCertificateUrlProvider,
) : AbstractRequestableStepConfig<Complete, NoInputFormModel, EpcState>() {
    override val formModelClass = NoInputFormModel::class

    override fun getStepSpecificContent(state: EpcState): Map<String, Any?> {
        val supersededEpc = state.epcRetrievedByCertificateNumber
        val latestEpc = state.updatedEpcRetrievedByCertificateNumber
        val messageKeyPrefix = "propertyCompliance.epcTask.epcSuperseded"

        return mapOf(
            "supersededEpcSummaryListRows" to
                listOf(
                    SummaryListRowViewModel(
                        fieldHeading = "$messageKeyPrefix.supersededEpc.address",
                        fieldValue = supersededEpc?.singleLineAddress,
                    ),
                    SummaryListRowViewModel(
                        fieldHeading = "$messageKeyPrefix.supersededEpc.energyRating",
                        fieldValue = supersededEpc?.energyRatingUppercase,
                    ),
                    SummaryListRowViewModel(
                        fieldHeading = "$messageKeyPrefix.supersededEpc.expiryDate",
                        fieldValue = supersededEpc?.expiryDateAsJavaLocalDate,
                    ),
                    SummaryListRowViewModel(
                        fieldHeading = "$messageKeyPrefix.supersededEpc.certificateNumber",
                        fieldValue = supersededEpc?.certificateNumber,
                    ),
                ),
            "latestEpcSummaryListRows" to
                listOf(
                    SummaryListRowViewModel(
                        fieldHeading = "$messageKeyPrefix.latestEpc.address",
                        fieldValue = latestEpc?.singleLineAddress,
                    ),
                    SummaryListRowViewModel(
                        fieldHeading = "$messageKeyPrefix.latestEpc.energyRating",
                        fieldValue = latestEpc?.energyRatingUppercase,
                    ),
                    SummaryListRowViewModel(
                        fieldHeading = "$messageKeyPrefix.latestEpc.expiryDate",
                        fieldValue = latestEpc?.expiryDateAsJavaLocalDate,
                    ),
                    SummaryListRowViewModel(
                        fieldHeading = "$messageKeyPrefix.latestEpc.certificateNumber",
                        fieldValue = latestEpc?.certificateNumber,
                    ),
                ),
            "ticketPanelHeading" to "$messageKeyPrefix.latestEpc.heading",
            "ticketPanelLink" to
                latestEpc?.let {
                    SummaryCardActionViewModel(
                        text = "$messageKeyPrefix.latestEpc.viewFullEpc",
                        url = epcCertificateUrlProvider.getEpcCertificateUrl(it.certificateNumber),
                        opensInNewTab = true,
                    )
                },
            "searchAgainUrl" to Destination(state.findYourEpcStep).toUrlStringOrNull(),
        )
    }

    override fun chooseTemplate(state: EpcState) = "forms/confirmUpdatedEpcForm"

    override fun mode(state: EpcState) = getFormModelFromStateOrNull(state)?.let { Complete.COMPLETE }

    override fun afterStepDataIsAdded(state: EpcState) {
        state.acceptedEpc = state.updatedEpcRetrievedByCertificateNumber
    }
}

@JourneyFrameworkComponent
final class EpcSuperseededStep(
    stepConfig: EpcSuperseededStepConfig,
) : RequestableStep<Complete, NoInputFormModel, EpcState>(stepConfig) {
    companion object {
        const val ROUTE_SEGMENT = "epc-superseded"
    }
}
