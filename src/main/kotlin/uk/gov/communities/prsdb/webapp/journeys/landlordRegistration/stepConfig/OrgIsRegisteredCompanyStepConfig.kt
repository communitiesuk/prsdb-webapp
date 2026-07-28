package uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyState
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.shared.YesOrNo
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.OrgIsRegisteredCompanyFormModel
import uk.gov.communities.prsdb.webapp.models.viewModels.formModels.RadiosViewModel

@JourneyFrameworkComponent
class OrgIsRegisteredCompanyStepConfig : AbstractRequestableStepConfig<YesOrNo, OrgIsRegisteredCompanyFormModel, JourneyState>() {
    override val formModelClass = OrgIsRegisteredCompanyFormModel::class

    override fun getStepSpecificContent(state: JourneyState) =
        mapOf(
            "fieldSetHeading" to "registerAsALandlord.orgIsRegisteredCompany.fieldSetHeading",
            "fieldName" to "companiesHouse",
            "radioOptions" to RadiosViewModel.yesOrNoRadios(),
        )

    override fun chooseTemplate(state: JourneyState) = "forms/orgIsRegisteredCompanyForm"

    override fun mode(state: JourneyState) =
        getFormModelFromStateOrNull(state)?.companiesHouse?.let {
            if (it) YesOrNo.YES else YesOrNo.NO
        }
}

@JourneyFrameworkComponent
final class OrgIsRegisteredCompanyStep(
    stepConfig: OrgIsRegisteredCompanyStepConfig,
) : RequestableStep<YesOrNo, OrgIsRegisteredCompanyFormModel, JourneyState>(stepConfig) {
    companion object {
        const val ROUTE_SEGMENT = "organisation-companies-house"
    }
}
