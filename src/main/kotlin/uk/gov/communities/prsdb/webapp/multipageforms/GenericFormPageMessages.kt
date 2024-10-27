package uk.gov.communities.prsdb.webapp.multipageforms

data class GenericFormPageMessages(
    val title: String,
    val contentHeader: String,
    val infoText: String? = null,
    val formInstruction: String,
    val formInstructionExtra: String? = null,
)
