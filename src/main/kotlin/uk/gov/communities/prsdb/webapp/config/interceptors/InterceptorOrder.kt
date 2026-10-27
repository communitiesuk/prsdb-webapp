package uk.gov.communities.prsdb.webapp.config.interceptors

// Interceptors run preHandle in ascending order and stop at the first one that returns false (e.g. after a redirect).
// Register every interceptor with one of these so that headers and attributes are set before any redirect can happen.
enum class InterceptorOrder(
    val value: Int,
) {
    HEADERS_AND_ATTRIBUTES(0),
    REDIRECT(100),
}
