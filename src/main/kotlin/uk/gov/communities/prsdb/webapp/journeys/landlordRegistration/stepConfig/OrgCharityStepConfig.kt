package uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyState
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.shared.YesOrNo
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.OrgCharityFormModel
import uk.gov.communities.prsdb.webapp.models.viewModels.formModels.RadiosViewModel

@JourneyFrameworkComponent
class OrgCharityStepConfig : AbstractRequestableStepConfig<YesOrNo, OrgCharityFormModel, JourneyState>() {
    override val formModelClass = OrgCharityFormModel::class

    override fun getStepSpecificContent(state: JourneyState) =
        mapOf(
            "fieldSetHeading" to "forms.orgCharity.fieldSetHeading",
            "fieldName" to "charity",
            "radioOptions" to RadiosViewModel.yesOrNoRadios(),
            "todoComment" to "TODO: PDJB-1140 - Is your organisation a registered charity",
        )

    override fun chooseTemplate(state: JourneyState) = "forms/todoWithRadios"

    override fun mode(state: JourneyState) =
        getFormModelFromStateOrNull(state)?.charity?.let {
            if (it) YesOrNo.YES else YesOrNo.NO
        }
}

@JourneyFrameworkComponent
final class OrgCharityStep(
    stepConfig: OrgCharityStepConfig,
) : RequestableStep<YesOrNo, OrgCharityFormModel, JourneyState>(stepConfig) {
    companion object {
        const val ROUTE_SEGMENT = "organisation-charity"
    }
}
