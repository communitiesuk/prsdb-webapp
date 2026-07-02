package uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.constants.enums.CharityRegulator
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyState
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.CharityRegisteredWithFormModel
import uk.gov.communities.prsdb.webapp.models.viewModels.formModels.RadiosButtonViewModel
import uk.gov.communities.prsdb.webapp.models.viewModels.formModels.RadiosDividerViewModel

@JourneyFrameworkComponent
class OrgCharityRegisteredWithStepConfig : AbstractRequestableStepConfig<CharityRegulator, CharityRegisteredWithFormModel, JourneyState>() {
    override val formModelClass = CharityRegisteredWithFormModel::class

    override fun getStepSpecificContent(state: JourneyState) =
        mapOf(
            "fieldSetHeading" to "forms.orgCharityRegisteredWith.fieldSetHeading",
            "detailsSummary" to "forms.orgCharityRegisteredWith.details.summary",
            "detailsText" to "forms.orgCharityRegisteredWith.details.text",
            "radioOptions" to
                listOf(
                    RadiosButtonViewModel(
                        value = CharityRegulator.ENGLAND_AND_WALES,
                        labelMsgKey = "forms.orgCharityRegisteredWith.radios.option.englandAndWales",
                    ),
                    RadiosButtonViewModel(
                        value = CharityRegulator.NORTHERN_IRELAND,
                        labelMsgKey = "forms.orgCharityRegisteredWith.radios.option.northernIreland",
                    ),
                    RadiosButtonViewModel(
                        value = CharityRegulator.SCOTLAND,
                        labelMsgKey = "forms.orgCharityRegisteredWith.radios.option.scotland",
                    ),
                    RadiosDividerViewModel("forms.radios.dividerText"),
                    RadiosButtonViewModel(
                        value = CharityRegulator.NONE,
                        labelMsgKey = "forms.orgCharityRegisteredWith.radios.option.none",
                    ),
                ),
        )

    override fun chooseTemplate(state: JourneyState) = "forms/charityRegisteredWithForm"

    override fun mode(state: JourneyState) = getFormModelFromStateOrNull(state)?.charityRegisteredWith
}

@JourneyFrameworkComponent
final class OrgCharityRegisteredWithStep(
    stepConfig: OrgCharityRegisteredWithStepConfig,
) : RequestableStep<CharityRegulator, CharityRegisteredWithFormModel, JourneyState>(stepConfig) {
    companion object {
        const val ROUTE_SEGMENT = "organisation-charity-registered-with"
    }
}
