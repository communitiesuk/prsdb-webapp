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

    fun deleteUnusedInactiveAddresses() {
        val query =
            """
                -- Function returning every address.id that is referenced by any foreign key
                CREATE OR REPLACE FUNCTION used_address_ids()
                RETURNS TABLE(id BIGINT)
                LANGUAGE plpgsql
                AS $$
                DECLARE
                    r record;
                    sql text;
                BEGIN
                    FOR r IN
                        SELECT c.conrelid::regclass AS tbl, a.attname AS col
                        FROM pg_constraint c
                        JOIN pg_attribute a
                        ON a.attrelid = c.conrelid
                        AND a.attnum = ANY(c.conkey)
                        WHERE c.contype = 'f'
                        AND c.confrelid = 'address'::regclass
                    LOOP
                        sql := format('SELECT DISTINCT %I FROM %s WHERE %I IS NOT NULL', r.col, r.tbl, r.col);
                        RETURN QUERY EXECUTE sql;
                    END LOOP;
                END;
                $$;
                
                DELETE FROM address
                WHERE is_active = false
                AND id NOT IN (SELECT * FROM used_address_ids());
            """
        session.createNativeMutationQuery(query).executeUpdate()
    }
}
