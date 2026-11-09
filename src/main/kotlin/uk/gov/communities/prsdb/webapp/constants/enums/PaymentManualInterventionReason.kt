package uk.gov.communities.prsdb.webapp.constants.enums

enum class PaymentManualInterventionReason(
    val description: String,
) {
    CAPTURED_BUT_NOT_REGISTERED("GOV.UK Pay reports the payment as taken, but the property was not registered"),
    CAPTURE_OUTCOME_UNKNOWN("We could not confirm whether GOV.UK Pay took the payment"),
    REGISTRATION_DID_NOT_COMMIT("The payment was taken, but the property registration did not save"),
    TAKEN_AFTER_CANCELLATION_REQUESTED("GOV.UK Pay took the payment after we asked to cancel it"),
}
