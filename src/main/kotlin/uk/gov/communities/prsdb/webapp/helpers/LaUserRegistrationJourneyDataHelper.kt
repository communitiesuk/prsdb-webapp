package uk.gov.communities.prsdb.webapp.helpers

import uk.gov.communities.prsdb.webapp.forms.JourneyData
import uk.gov.communities.prsdb.webapp.forms.steps.RegisterLaUserStepId
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.EmailFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NameFormModel

class LaUserRegistrationJourneyDataHelper : JourneyDataHelper() {
    companion object {
        fun getName(journeyData: JourneyData) =
            getFieldStringValue(
                journeyData,
                RegisterLaUserStepId.Name.urlPathSegment,
                NameFormModel::name.name,
            )

        fun getEmail(journeyData: JourneyData) =
            getFieldStringValue(
                journeyData,
                RegisterLaUserStepId.Email.urlPathSegment,
                EmailFormModel::emailAddress.name,
            )
    }
}
