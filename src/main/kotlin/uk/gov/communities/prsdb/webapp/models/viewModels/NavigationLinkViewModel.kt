package uk.gov.communities.prsdb.webapp.models.viewModels

data class NavigationLinkViewModel(
    val href: String,
    val messageProperty: String,
    val isActive: Boolean,
)
