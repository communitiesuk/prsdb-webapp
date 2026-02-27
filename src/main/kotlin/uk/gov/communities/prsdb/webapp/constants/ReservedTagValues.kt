package uk.gov.communities.prsdb.webapp.constants

/**
 * Contains reserved tag values used throughout the journey framework. These will often be privately referenced by various
 * helper functions and classes to identify special configurations or behaviors. Generally if you need to use a tag, you
 * should use a helper function that applies the tag for you and add the value here to ensure we do not have any collisions.
 */
class ReservedTagValues {
    companion object {
        const val SAVABLE = "savable step"
    }
}
