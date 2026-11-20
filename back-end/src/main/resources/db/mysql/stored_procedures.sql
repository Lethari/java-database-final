-- Run against the MySQL schema after the application tables have been created.
-- Category IDs follow the front-end selector order: 1 Mobile, 2 TV and AV,
-- 3 Home Appliances, 4 Laptops and Monitors, 5 Accessories.

DELIMITER //

CREATE PROCEDURE GetMonthlySalesForEachStore(
    IN p_year INT,
    IN p_month INT
)
BEGIN
    SELECT
        s.id AS store_id,
        s.name AS store_name,
        COALESCE(SUM(o.total_price), 0) AS monthly_sales
    FROM store s
    LEFT JOIN order_details o
        ON o.store_id = s.id
        AND YEAR(o.`date`) = p_year
        AND MONTH(o.`date`) = p_month
    GROUP BY s.id, s.name
    ORDER BY s.id;
END //

CREATE PROCEDURE GetAggregateSalesForCompany(
    IN p_year INT,
    IN p_month INT
)
BEGIN
    SELECT
        p_year AS sales_year,
        p_month AS sales_month,
        COALESCE(SUM(total_price), 0) AS company_sales
    FROM order_details
    WHERE YEAR(`date`) = p_year
        AND MONTH(`date`) = p_month;
END //

CREATE PROCEDURE GetTopSellingProductsByCategory(
    IN p_category_id INT,
    IN p_year INT
)
BEGIN
    DECLARE v_category VARCHAR(100);

    SET v_category = CASE p_category_id
        WHEN 1 THEN 'Mobile'
        WHEN 2 THEN 'TV and AV'
        WHEN 3 THEN 'Home Appliances'
        WHEN 4 THEN 'Laptops and Monitors'
        WHEN 5 THEN 'Accessories'
        ELSE NULL
    END;

    IF v_category IS NULL THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Unknown category ID';
    END IF;

    SELECT
        p.id AS product_id,
        p.name AS product_name,
        p.category,
        SUM(oi.quantity) AS units_sold,
        SUM(oi.quantity * oi.price) AS sales_revenue
    FROM order_item oi
    JOIN order_details o ON o.id = oi.order_id
    JOIN product p ON p.id = oi.product_id
    WHERE p.category = v_category
        AND YEAR(o.`date`) = p_year
    GROUP BY p.id, p.name, p.category
    ORDER BY units_sold DESC, sales_revenue DESC, p.id;
END //

DELIMITER ;
