package uk.gov.communities.prsdb.webapp.exceptions

import org.springframework.http.HttpStatusCode

class GovUkPayException : PrsdbWebException {
    val httpStatus: HttpStatusCode?
    val errorCode: String?
    val errorDescription: String?

    constructor(
        httpStatus: HttpStatusCode,
        errorCode: String?,
        errorDescription: String?,
        cause: Throwable,
    ) : super(httpErrorMessage(httpStatus, errorCode, errorDescription), cause) {
        this.httpStatus = httpStatus
        this.errorCode = errorCode
        this.errorDescription = errorDescription
    }

    constructor(message: String) : super(message) {
        httpStatus = null
        errorCode = null
        errorDescription = null
    }

    constructor(message: String, cause: Throwable) : super(message, cause) {
        httpStatus = null
        errorCode = null
        errorDescription = null
    }

    companion object {
        private fun httpErrorMessage(
            httpStatus: HttpStatusCode,
            errorCode: String?,
            errorDescription: String?,
        ): String {
            val message = "GOV.UK Pay request failed with HTTP status ${httpStatus.value()}"
            val errorDetails = listOfNotNull(errorCode, errorDescription).joinToString(" - ")
            return if (errorDetails.isEmpty()) message else "$message: $errorDetails"
        }
    }
}
