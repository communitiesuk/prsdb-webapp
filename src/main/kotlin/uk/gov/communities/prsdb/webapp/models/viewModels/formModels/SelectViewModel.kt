package uk.gov.communities.prsdb.webapp.models.viewModels.formModels

data class SelectViewModel<T>(
    val value: T,
    val label: String? = null,
)
