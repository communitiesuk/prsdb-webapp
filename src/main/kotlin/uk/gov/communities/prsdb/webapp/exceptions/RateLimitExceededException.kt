package uk.gov.communities.prsdb.webapp.exceptions

import org.apache.http.HttpException

class RateLimitExceededException(
    message: String,
) : HttpException(message)
