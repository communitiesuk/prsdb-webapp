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
            "fieldSetHeading" to "registerAsALandlord.landlordType.fieldSetHeading",
            "partnershipSummary" to "registerAsALandlord.landlordType.partnership.summary",
            "englishPartnershipHeading" to "registerAsALandlord.landlordType.partnership.english.heading",
            "englishPartnershipParagraphOne" to "registerAsALandlord.landlordType.partnership.english.paragraph.one",
            "englishPartnershipParagraphTwo" to "registerAsALandlord.landlordType.partnership.english.paragraph.two",
            "llpPartnershipHeading" to "registerAsALandlord.landlordType.partnership.llp.heading",
            "llpPartnershipParagraph" to "registerAsALandlord.landlordType.partnership.llp.paragraph",
            "radioOptions" to
                listOf(
                    RadiosButtonViewModel(
                        value = LandlordType.INDIVIDUAL,
                        labelMsgKey = "registerAsALandlord.landlordType.radios.individual.label",
                        hintMsgKey = "registerAsALandlord.landlordType.radios.individual.hint",
                    ),
                    RadiosButtonViewModel(
                        value = LandlordType.ORGANISATION,
                        labelMsgKey = "registerAsALandlord.landlordType.radios.organisation.label",
                        hintMsgKey = "registerAsALandlord.landlordType.radios.organisation.hint",
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
