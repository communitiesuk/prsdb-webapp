package uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyState
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.OrgCharityNumberNorthernIrelandFormModel

@JourneyFrameworkComponent
class OrgCharityNumberNorthernIrelandStepConfig :
    AbstractRequestableStepConfig<Complete, OrgCharityNumberNorthernIrelandFormModel, JourneyState>() {
    override val formModelClass = OrgCharityNumberNorthernIrelandFormModel::class

    override fun getStepSpecificContent(state: JourneyState) =
        mapOf(
            "charityUrl" to CHARITY_REGISTER_URL,
        )

    override fun chooseTemplate(state: JourneyState) = "forms/orgCharityNumberForm"

    override fun mode(state: JourneyState) = getFormModelFromStateOrNull(state)?.let { Complete.COMPLETE }

    companion object {
        private const val CHARITY_REGISTER_URL = "https://www.charitycommissionni.org.uk/"
    }
}

@JourneyFrameworkComponent
final class OrgCharityNumberNorthernIrelandStep(
    stepConfig: OrgCharityNumberNorthernIrelandStepConfig,
) : RequestableStep<Complete, OrgCharityNumberNorthernIrelandFormModel, JourneyState>(stepConfig) {
    companion object {
        const val ROUTE_SEGMENT = "organisation-charity-number-northern-ireland"
    }
}
