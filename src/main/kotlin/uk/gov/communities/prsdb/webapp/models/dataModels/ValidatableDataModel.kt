package uk.gov.communities.prsdb.webapp.models.dataModels

interface ValidatableDataModel {
    val errorFieldMap: Map<String, String>
        get() = mapOf()

    val errorPrecedenceList: List<String>
        get() = listOf()
}
