package uk.gov.communities.prsdb.webapp.models.viewModels

data class TicketPanelLinkViewModel(
    val text: String,
    val url: String,
    val opensInNewTab: Boolean = false,
)
