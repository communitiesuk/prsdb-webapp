package uk.gov.communities.prsdb.webapp.services

interface FileDequarantiner {
    fun dequarantine(objectKey: String): Boolean

    fun delete(objectKey: String): Boolean
}
