DELETE FROM ownership_link
WHERE id NOT IN (SELECT DISTINCT ON (landlord_id, landlordship_id) id FROM ownership_link);

ALTER TABLE ownership_link
    ADD CONSTRAINT uc_ownerhship_link_uniqueness UNIQUE (landlord_id, landlordship_id);
