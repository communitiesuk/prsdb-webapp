package uk.gov.communities.prsdb.webapp.services

interface FileDequarantiner {
    fun dequarantine(objectKey: String): Boolean

    fun isFileDequarantined(objectKey: String): Boolean
}
