package uk.gov.communities.prsdb.webapp.helpers

class EmailHelper {
    companion object {
        fun sendEmailAndLogFailures(
            emailDescription: String,
            sendEmail: () -> Unit,
        ) {
            try {
                sendEmail()
            } catch (exception: Exception) {
                println("Failed to send $emailDescription: ${exception.stackTraceToString()}")
            }
        }
    }
}
