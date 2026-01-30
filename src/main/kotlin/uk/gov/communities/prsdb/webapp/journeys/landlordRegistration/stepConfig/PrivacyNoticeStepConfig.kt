package uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.controllers.LandlordPrivacyNoticeController.Companion.LANDLORD_PRIVACY_NOTICE_ROUTE
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyState
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.LandlordPrivacyNoticeFormModel
import uk.gov.communities.prsdb.webapp.models.viewModels.formModels.CheckboxViewModel

@JourneyFrameworkComponent
class PrivacyNoticeStepConfig : AbstractRequestableStepConfig<Complete, LandlordPrivacyNoticeFormModel, JourneyState>() {
    override val formModelClass = LandlordPrivacyNoticeFormModel::class

    override fun getStepSpecificContent(state: JourneyState) =
        mapOf(
            "submitButtonText" to "forms.buttons.continue",
            "landlordPrivacyNoticeUrl" to LANDLORD_PRIVACY_NOTICE_ROUTE,
            "options" to
                listOf(
                    CheckboxViewModel(
                        value = "true",
                        labelMsgKey = "registerAsALandlord.privacyNotice.checkBox.label",
                    ),
                ),
        )

    override fun chooseTemplate(state: JourneyState) = "forms/landlordPrivacyNoticeForm"

    override fun mode(state: JourneyState) = getFormModelFromStateOrNull(state)?.let { Complete.COMPLETE }
}

@JourneyFrameworkComponent
final class PrivacyNoticeStep(
    stepConfig: PrivacyNoticeStepConfig,
) : RequestableStep<Complete, LandlordPrivacyNoticeFormModel, JourneyState>(stepConfig) {
    companion object {
        const val ROUTE_SEGMENT = "privacy-notice"
    }
}
