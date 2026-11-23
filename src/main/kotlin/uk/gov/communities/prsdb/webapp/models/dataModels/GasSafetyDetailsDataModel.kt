package uk.gov.communities.prsdb.webapp.models.dataModels

import java.time.LocalDate

data class GasSafetyDetailsDataModel(
    val hasGasSupply: Boolean,
    val issueDate: LocalDate?,
    val uploadIds: List<Long>,
)
