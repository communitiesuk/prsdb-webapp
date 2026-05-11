package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.update.epc

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.EpcRegistrationCyaSummaryRowsFactory
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NoInputFormModel
import uk.gov.communities.prsdb.webapp.services.EpcCertificateUrlProvider

@JourneyFrameworkComponent
class UpdateCheckEpcAnswersStepConfig(
    private val epcCertificateUrlProvider: EpcCertificateUrlProvider,
) : AbstractRequestableStepConfig<Complete, NoInputFormModel, UpdateEpcJourneyState>() {
    override val formModelClass = NoInputFormModel::class

    override fun getStepSpecificContent(state: UpdateEpcJourneyState): Map<String, Any?> {
        val factory =
            EpcRegistrationCyaSummaryRowsFactory(epcCertificateUrlProvider, state) { step ->
                Destination.VisitableStep(step, state.getCyaJourneyId(step))
            }
        return mapOf(
            "epcCardTitle" to factory.createEpcCardTitle(),
            "epcCardActions" to factory.createEpcCardActions(),
            "epcCardRows" to factory.createEpcCardRows(),
            "epcExpiredTextKey" to factory.getEpcExpiredTextKey(),
            "tenancyCheckRows" to factory.createTenancyCheckRows(),
            "lowRatingTextKey" to factory.getLowRatingTextKey(),
            "exemptionReasonRows" to factory.createExemptionReasonRows(),
            "nonEpcRows" to factory.createNonEpcRows(),
            "insetTextKey" to factory.getInsetTextKey(),
        )
    }

    override fun chooseTemplate(state: UpdateEpcJourneyState) = "forms/checkEpcAnswersForm"

    override fun mode(state: UpdateEpcJourneyState) = getFormModelFromStateOrNull(state)?.let { Complete.COMPLETE }
}

@JourneyFrameworkComponent
final class UpdateCheckEpcAnswersStep(
    stepConfig: UpdateCheckEpcAnswersStepConfig,
) : RequestableStep<Complete, NoInputFormModel, UpdateEpcJourneyState>(stepConfig) {
    companion object {
        const val ROUTE_SEGMENT = "check-epc-answers"
    }
}
