package uk.gov.communities.prsdb.webapp.helpers

class ViewModelOptionsHelper {
    companion object {
        fun toggleChangeLink(
            link: String?,
            useLink: Boolean,
        ) = if (useLink) {
            link
        } else {
            null
        }
    }
}
