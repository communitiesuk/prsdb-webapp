CREATE VIEW landlord_with_listed_property_count AS
SELECT po.primary_landlord_id as landlord_id, COUNT(po.primary_landlord_id) as count
    FROM property_ownership po
    GROUP BY po.primary_landlord_id
