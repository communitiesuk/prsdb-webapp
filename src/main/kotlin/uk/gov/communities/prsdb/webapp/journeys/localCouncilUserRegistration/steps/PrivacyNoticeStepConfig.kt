package uk.gov.communities.prsdb.webapp.journeys.localCouncilUserRegistration.steps

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.controllers.LocalCouncilPrivacyNoticeController
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyState
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.LocalCouncilPrivacyNoticeFormModel
import uk.gov.communities.prsdb.webapp.models.viewModels.formModels.CheckboxViewModel

@JourneyFrameworkComponent("localCouncilUserRegistrationPrivacyNoticeStepConfig")
class PrivacyNoticeStepConfig : AbstractRequestableStepConfig<Complete, LocalCouncilPrivacyNoticeFormModel, JourneyState>() {
    override val formModelClass = LocalCouncilPrivacyNoticeFormModel::class

    override fun getStepSpecificContent(state: JourneyState) =
        mapOf(
            "submitButtonText" to "forms.buttons.continue",
            "localCouncilPrivacyNoticeUrl" to LocalCouncilPrivacyNoticeController.LOCAL_COUNCIL_PRIVACY_NOTICE_ROUTE,
            "options" to
                listOf(
                    CheckboxViewModel(
                        value = "true",
                        labelMsgKey = "registerLocalCouncilUser.privacyNotice.checkBox.label",
                    ),
                ),
        )

    override fun chooseTemplate(state: JourneyState): String = "forms/localCouncilPrivacyNoticeForm"

    override fun mode(state: JourneyState) = getFormModelFromStateOrNull(state)?.let { Complete.COMPLETE }
}

@JourneyFrameworkComponent("localCouncilUserRegistrationPrivacyNoticeStep")
final class PrivacyNoticeStep(
    stepConfig: PrivacyNoticeStepConfig,
) : RequestableStep<Complete, LocalCouncilPrivacyNoticeFormModel, JourneyState>(stepConfig)
