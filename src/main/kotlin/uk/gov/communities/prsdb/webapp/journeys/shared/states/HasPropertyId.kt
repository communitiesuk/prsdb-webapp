package uk.gov.communities.prsdb.webapp.journeys.shared.states

/**
 * Implemented by journey states whose completion should trigger the shared
 * property-update success banner (see CompletePropertyUpdateStep).
 */
interface HasPropertyId {
    val propertyId: Long
    val successBannerMessageKey: String
}
