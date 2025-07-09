package uk.gov.communities.prsdb.webapp.helpers

class JourneyContextHelper {
    companion object {
        fun isCheckingAnswers(checkingAnswersForStep: String?) = checkingAnswersForStep != null
    }
}
