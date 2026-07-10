package uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.constants.enums.LandlordType
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyState
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.LandlordTypeFormModel
import uk.gov.communities.prsdb.webapp.models.viewModels.formModels.RadiosButtonViewModel

@JourneyFrameworkComponent("landlordTypeStepConfig")
class LandlordTypeStepConfig : AbstractRequestableStepConfig<LandlordTypeMode, LandlordTypeFormModel, JourneyState>() {
    override val formModelClass = LandlordTypeFormModel::class

    override fun getStepSpecificContent(state: JourneyState) =
        mapOf(
            "fieldSetHeading" to "forms.landlordType.fieldSetHeading",
            "partnershipSummary" to "forms.landlordType.partnership.summary",
            "partnershipParagraphOne" to "forms.landlordType.partnership.paragraph.one",
            "partnershipParagraphTwo" to "forms.landlordType.partnership.paragraph.two",
            "radioOptions" to
                listOf(
                    RadiosButtonViewModel(
                        value = LandlordType.INDIVIDUAL,
                        labelMsgKey = "forms.landlordType.radios.individual.label",
                        hintMsgKey = "forms.landlordType.radios.individual.hint",
                    ),
                    RadiosButtonViewModel(
                        value = LandlordType.ORGANISATION,
                        labelMsgKey = "forms.landlordType.radios.organisation.label",
                        hintMsgKey = "forms.landlordType.radios.organisation.hint",
                    ),
                ),
        )

    override fun chooseTemplate(state: JourneyState) = "forms/landlordTypeForm"

    override fun mode(state: JourneyState) =
        getFormModelFromStateOrNull(state)?.landlordType?.let {
            when (it) {
                LandlordType.INDIVIDUAL -> LandlordTypeMode.INDIVIDUAL
                LandlordType.ORGANISATION -> LandlordTypeMode.ORGANISATION
            }
        }
}

@JourneyFrameworkComponent("landlordTypeStep")
final class LandlordTypeStep(
    stepConfig: LandlordTypeStepConfig,
) : RequestableStep<LandlordTypeMode, LandlordTypeFormModel, JourneyState>(stepConfig) {
    companion object {
        const val ROUTE_SEGMENT = "landlord-type"
    }
}

enum class LandlordTypeMode {
    INDIVIDUAL,
    ORGANISATION,
}
