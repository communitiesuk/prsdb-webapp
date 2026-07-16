package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states

// The context a GasSafetyDetailsTask needs from the journey it is mounted in - values it cannot compute itself
// because they differ per enclosing journey. Property registration reads isOccupied from the occupied step and
// allows the "provide certificate later" route; the update journey seeds isOccupied from the database and disallows it.
interface GasSafetyDetailsTaskDependencies {
    val isOccupied: Boolean

    val allowProvideCertificateLaterRoute: Boolean
}
