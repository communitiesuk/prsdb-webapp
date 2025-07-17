package uk.gov.communities.prsdb.webapp.services

interface FileDequarantiner {
    fun dequarantineFile(objectKey: String): Boolean

    fun deleteFile(objectKey: String): Boolean
}
