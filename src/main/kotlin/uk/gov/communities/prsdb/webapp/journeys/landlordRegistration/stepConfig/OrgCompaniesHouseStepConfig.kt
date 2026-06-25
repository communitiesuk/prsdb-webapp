package uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyState
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.shared.YesOrNo
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.OrgCompaniesHouseFormModel
import uk.gov.communities.prsdb.webapp.models.viewModels.formModels.RadiosViewModel

@JourneyFrameworkComponent
class OrgCompaniesHouseStepConfig : AbstractRequestableStepConfig<YesOrNo, OrgCompaniesHouseFormModel, JourneyState>() {
    override val formModelClass = OrgCompaniesHouseFormModel::class

    override fun getStepSpecificContent(state: JourneyState) =
        mapOf(
            "fieldSetHeading" to "forms.orgCompaniesHouse.fieldSetHeading",
            "fieldName" to "companiesHouse",
            "radioOptions" to RadiosViewModel.yesOrNoRadios(),
        )

    override fun chooseTemplate(state: JourneyState) = "forms/todoWithRadios"

    override fun mode(state: JourneyState) =
        getFormModelFromStateOrNull(state)?.companiesHouse?.let {
            if (it) YesOrNo.YES else YesOrNo.NO
        }
}

@JourneyFrameworkComponent
final class OrgCompaniesHouseStep(
    stepConfig: OrgCompaniesHouseStepConfig,
) : RequestableStep<YesOrNo, OrgCompaniesHouseFormModel, JourneyState>(stepConfig) {
    companion object {
        const val ROUTE_SEGMENT = "organisation-companies-house"
    }
}
