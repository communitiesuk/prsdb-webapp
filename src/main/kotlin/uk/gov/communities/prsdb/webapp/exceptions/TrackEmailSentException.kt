package uk.gov.communities.prsdb.webapp.exceptions

class TrackEmailSentException : RuntimeException {
    constructor(message: String?) : super(message)
    constructor(message: String?, cause: Throwable?) : super(message, cause)
}
