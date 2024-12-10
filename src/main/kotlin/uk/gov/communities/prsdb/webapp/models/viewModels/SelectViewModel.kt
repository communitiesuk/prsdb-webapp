package uk.gov.communities.prsdb.webapp.models.viewModels

data class SelectViewModel<T>(
    val value: T,
    val label: String? = null,
)
