CREATE VIEW landlord_with_listed_property_count AS
SELECT l.id as landlord_id, COUNT (po.primary_landlord_id) AS listed_property_count
    FROM property_ownership po
    RIGHT JOIN landlord l ON po.primary_landlord_id = l.id AND po.is_active = true
    GROUP BY l.id
