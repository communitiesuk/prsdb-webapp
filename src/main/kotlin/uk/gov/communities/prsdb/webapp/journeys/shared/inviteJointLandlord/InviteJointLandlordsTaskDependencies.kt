package uk.gov.communities.prsdb.webapp.journeys.shared.inviteJointLandlord

// The context an InviteJointLandlordsTask needs from the journey it is mounted in - values it cannot compute itself
// because they differ per enclosing journey. Property registration supplies loggedInLandlordEmail; the update
// journey supplies the existing invited/landlord emails. Defaults mean an enclosing state only overrides what it
// actually provides, matching the behaviour that previously required separate task subclasses.
interface InviteJointLandlordsTaskDependencies {
    val existingInvitedEmails: List<String>
        get() = emptyList()

    val existingLandlordEmails: List<String>
        get() = emptyList()

    val loggedInLandlordEmail: String?
        get() = null
}
