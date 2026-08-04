package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.LicensingState
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NoInputFormModel

@JourneyFrameworkComponent
class ProvideLicensingLaterStepConfig : AbstractRequestableStepConfig<Complete, NoInputFormModel, LicensingState>() {
    override val formModelClass = NoInputFormModel::class

    override fun getStepSpecificContent(state: LicensingState) =
        mapOf(
            "submitButtonText" to if (state.isOccupied == true) "forms.buttons.continue" else "forms.buttons.saveAndContinue",
        )

    override fun chooseTemplate(state: LicensingState): String =
        state.isOccupied?.let { isOccupied ->
            if (isOccupied) "forms/provideLicensingLaterOccupiedForm" else "forms/provideLicensingLaterUnoccupiedForm"
        } ?: throw IllegalStateException("ProvideLicensingLaterStep should not be reachable before isOccupied is set")

    override fun mode(state: LicensingState) = getFormModelFromStateOrNull(state)?.let { Complete.COMPLETE }
}

@JourneyFrameworkComponent
final class ProvideLicensingLaterStep(
    stepConfig: ProvideLicensingLaterStepConfig,
) : RequestableStep<Complete, NoInputFormModel, LicensingState>(stepConfig) {
    companion object {
        const val ROUTE_SEGMENT = "provide-licensing-later"
    }
}
