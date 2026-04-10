package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.EpcRegistrationCyaSummaryRowsFactory
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.EpcState
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NoInputFormModel
import uk.gov.communities.prsdb.webapp.services.EpcCertificateUrlProvider

@JourneyFrameworkComponent
class CheckEpcAnswersStepConfig(
    private val epcCertificateUrlProvider: EpcCertificateUrlProvider,
) : AbstractRequestableStepConfig<Complete, NoInputFormModel, EpcState>() {
    override val formModelClass = NoInputFormModel::class

    override fun getStepSpecificContent(state: EpcState): Map<String, Any?> {
        val factory = EpcRegistrationCyaSummaryRowsFactory(epcCertificateUrlProvider, state)
        return mapOf(
            "epcCardTitle" to factory.createEpcCardTitle(),
            "epcCardActions" to factory.createEpcCardActions(),
            "epcCardRows" to factory.createEpcCardRows(),
            "epcExpiredText" to factory.showEpcExpiredText(),
            "tenancyCheckRows" to factory.createTenancyCheckRows(),
            "meetsRequirementsInset" to factory.showMeetsRequirementsInset(),
            "lowRatingText" to factory.showLowRatingText(),
            "additionalRows" to factory.createAdditionalRows(),
            "lowRatingOccupiedInset" to factory.showLowRatingOccupiedInset(),
            "nonEpcRows" to factory.createNonEpcRows(),
            "occupiedNoEpcInset" to factory.showOccupiedNoEpcInset(),
        )
    }

    override fun chooseTemplate(state: EpcState) = "forms/checkEpcAnswersForm"

    override fun mode(state: EpcState) = getFormModelFromStateOrNull(state)?.let { Complete.COMPLETE }
}

@JourneyFrameworkComponent
final class CheckEpcAnswersStep(
    stepConfig: CheckEpcAnswersStepConfig,
) : RequestableStep<Complete, NoInputFormModel, EpcState>(stepConfig) {
    companion object {
        const val ROUTE_SEGMENT = "check-epc-answers"
    }
}
