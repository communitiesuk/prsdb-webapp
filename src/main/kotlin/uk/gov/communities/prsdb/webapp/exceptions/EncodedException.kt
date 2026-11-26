package uk.gov.communities.prsdb.webapp.exceptions

import org.springframework.http.HttpStatusCode

class EncodedException : PrsdbWebException {
    val httpStatus: HttpStatusCode?

    constructor(httpStatus: HttpStatusCode, cause: Throwable) :
        super("Encoded request failed with HTTP status ${httpStatus.value()}", cause) {
        this.httpStatus = httpStatus
    }

    constructor(message: String) : super(message) {
        httpStatus = null
    }

    constructor(message: String, cause: Throwable) : super(message, cause) {
        httpStatus = null
    }
}
