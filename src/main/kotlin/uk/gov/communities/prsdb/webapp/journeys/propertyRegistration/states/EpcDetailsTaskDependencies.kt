package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states

// The context an EpcDetailsTask needs from the journey it is mounted in - values it cannot compute itself because
// they differ per enclosing journey. Property registration reads isOccupied from the occupied step and the uprn from
// the selected address, and allows the "provide certificate later" route; the update journey seeds isOccupied and
// uprn from the database and disallows it.
interface EpcDetailsTaskDependencies {
    val isOccupied: Boolean?

    val uprn: Long?

    val allowProvideCertificateLaterRoute: Boolean
}
