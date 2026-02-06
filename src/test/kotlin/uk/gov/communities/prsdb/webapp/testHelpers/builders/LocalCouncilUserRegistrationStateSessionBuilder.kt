package uk.gov.communities.prsdb.webapp.testHelpers.builders

import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.CheckAnswersFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.EmailFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.LocalCouncilPrivacyNoticeFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NameFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NoInputFormModel

class LocalCouncilUserRegistrationStateSessionBuilder :
    JourneyStateSessionBuilder<LocalCouncilUserRegistrationStateSessionBuilder>() {
    fun withLandingPage(): LocalCouncilUserRegistrationStateSessionBuilder {
        withSubmittedValue("landing-page", NoInputFormModel())
        return this
    }

    fun withPrivacyNotice(agreed: Boolean = true): LocalCouncilUserRegistrationStateSessionBuilder {
        val formModel =
            LocalCouncilPrivacyNoticeFormModel().apply {
                agreesToPrivacyNotice = agreed
            }
        withSubmittedValue("privacy-notice", formModel)
        return this
    }

    fun withName(name: String = "Test User"): LocalCouncilUserRegistrationStateSessionBuilder {
        val formModel =
            NameFormModel().apply {
                this.name = name
            }
        withSubmittedValue("name", formModel)
        return this
    }

    fun withEmail(email: String = "test@example.com"): LocalCouncilUserRegistrationStateSessionBuilder {
        val formModel =
            EmailFormModel().apply {
                emailAddress = email
            }
        withSubmittedValue("email", formModel)
        return this
    }

    fun withCheckedAnswers(): LocalCouncilUserRegistrationStateSessionBuilder {
        val checkAnswersFormModel = CheckAnswersFormModel()
        withSubmittedValue("check-answers", checkAnswersFormModel)
        return this
    }

    companion object {
        fun beforePrivacyNotice() = LocalCouncilUserRegistrationStateSessionBuilder().withLandingPage()

        fun beforeName() = beforePrivacyNotice().withPrivacyNotice()

        fun beforeEmail() = beforeName().withName()

        fun beforeCheckAnswers() = beforeEmail().withEmail()
    }
}
