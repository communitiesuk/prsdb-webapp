package uk.gov.communities.prsdb.webapp.database.repository

import org.hibernate.StatelessSession

class NgdAddressLoaderRepository(
    private val session: StatelessSession,
) {
    fun saveCommentOnAddressTable(comment: String) {
        // Comment cannot be parameterized
        val query = "COMMENT ON TABLE address IS '$comment';"
        session.createNativeMutationQuery(query).executeUpdate()
    }

    fun findCommentOnAddressTable(): String? {
        val query = "SELECT obj_description('address'::regclass, 'pg_class') AS comment;"
        return session.createNativeQuery(query, String::class.java).singleResultOrNull
    }

    // Finds the table columns that have a foreign key reference to the address table's id column
    @Suppress("UNCHECKED_CAST")
    fun findAddressReferencingTableAndColumnNames(): List<Pair<String, String>> {
        val query =
            """
                SELECT conrelid::regclass::text AS referencing_table, a.attname AS referencing_column 
                FROM pg_constraint AS c 
                JOIN pg_attribute AS a ON a.attnum = ANY(c.conkey) AND a.attrelid = c.conrelid 
                JOIN pg_attribute AS fa ON fa.attnum = ANY(c.confkey) AND fa.attrelid = c.confrelid 
                WHERE c.contype = 'f' 
                AND confrelid = 'address'::regclass 
                AND fa.attname = 'id';
            """
        return session.createNativeQuery(query, Pair::class.java).resultList as List<Pair<String, String>>
    }

    fun countReferencesToAddressInTableColumn(
        uprn: Long,
        tableName: String,
        columnName: String,
    ): Long {
        // Table and column names cannot be parameterized
        val query =
            """
                SELECT COUNT(*) 
                FROM address
                JOIN $tableName
                ON address.id = $tableName.$columnName
                WHERE address.uprn = :uprn;
            """
        return session.createNativeQuery(query, Long::class.java).setParameter("uprn", uprn).singleResult
    }

    fun findAddressId(uprn: Long): Long? {
        val query =
            """
                SELECT id
                FROM address
                WHERE uprn = :uprn;
            """
        return session.createNativeQuery(query, Long::class.java).setParameter("uprn", uprn).singleResultOrNull
    }

    fun deactivateAddress(uprn: Long) {
        val query =
            """
                UPDATE address
                SET is_active = false
                WHERE uprn = :uprn;
            """
        session.createNativeMutationQuery(query).setParameter("uprn", uprn).executeUpdate()
    }

    fun deleteAddress(uprn: Long) {
        val query =
            """
                DELETE FROM address
                WHERE uprn = :uprn;
            """
        session.createNativeMutationQuery(query).setParameter("uprn", uprn).executeUpdate()
    }
}
