package uk.gov.communities.prsdb.webapp.helpers

import uk.gov.communities.prsdb.webapp.forms.JourneyData
import uk.gov.communities.prsdb.webapp.forms.steps.RegisterLocalCouncilUserStepId
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.EmailFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NameFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.PrivacyNoticeFormModel

class LocalCouncilUserRegistrationJourneyDataHelper : JourneyDataHelper() {
    companion object {
        fun getName(journeyData: JourneyData) =
            getFieldStringValue(
                journeyData,
                RegisterLocalCouncilUserStepId.Name.urlPathSegment,
                NameFormModel::name.name,
            )

        fun getEmail(journeyData: JourneyData) =
            getFieldStringValue(
                journeyData,
                RegisterLocalCouncilUserStepId.Email.urlPathSegment,
                EmailFormModel::emailAddress.name,
            )

        fun getHasAcceptedPrivacyNotice(journeyData: JourneyData) =
            getFieldBooleanValue(
                journeyData,
                RegisterLocalCouncilUserStepId.PrivacyNotice.urlPathSegment,
                PrivacyNoticeFormModel::agreesToPrivacyNotice.name,
            )
    }
}
