package uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyState
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.shared.YesOrNo
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.OrgIsRegisteredCharityFormModel
import uk.gov.communities.prsdb.webapp.models.viewModels.formModels.RadiosViewModel

@JourneyFrameworkComponent
class OrgIsRegisteredCharityStepConfig : AbstractRequestableStepConfig<YesOrNo, OrgIsRegisteredCharityFormModel, JourneyState>() {
    override val formModelClass = OrgIsRegisteredCharityFormModel::class

    override fun getStepSpecificContent(state: JourneyState) =
        mapOf(
            "fieldSetHeading" to "registerAsALandlord.orgIsRegisteredCharity.fieldSetHeading",
            "fieldSetHint" to "registerAsALandlord.orgIsRegisteredCharity.fieldSetHint",
            "radioOptions" to RadiosViewModel.yesOrNoRadios(),
        )

    override fun chooseTemplate(state: JourneyState) = "forms/orgIsRegisteredCharityForm"

    override fun mode(state: JourneyState) =
        getFormModelFromStateOrNull(state)?.charity?.let {
            if (it) YesOrNo.YES else YesOrNo.NO
        }
}

@JourneyFrameworkComponent
final class OrgIsRegisteredCharityStep(
    stepConfig: OrgIsRegisteredCharityStepConfig,
) : RequestableStep<YesOrNo, OrgIsRegisteredCharityFormModel, JourneyState>(stepConfig) {
    companion object {
        const val ROUTE_SEGMENT = "organisation-charity"
    }
}
