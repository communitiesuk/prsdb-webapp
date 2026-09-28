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
    ) : super("GOV.UK Pay request failed with HTTP status ${httpStatus.value()}: $errorCode - $errorDescription") {
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
}
