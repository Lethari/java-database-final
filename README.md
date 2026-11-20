# Warehouse distribution backend

The Spring Boot backend uses MySQL for store, product, inventory, customer, and
order data, and MongoDB for product reviews.

## Database configuration

Set these environment variables before starting the backend:

- `MYSQL_URL` (defaults to `jdbc:mysql://localhost:3306/inventory`, matching
  `insert_data.sql`)
- `MYSQL_USERNAME` (defaults to `root`)
- `MYSQL_PASSWORD` (defaults to empty)
- `MONGODB_URI` (defaults to `mongodb://localhost:27017/warehouse`)
- `JPA_DDL_AUTO` (defaults to `update`; use `validate` where schema migrations
  are managed separately)

For example, from `back-end`, run `mvn spring-boot:run` after the databases are
available. Admin authentication is expected to be enforced by the deployment
gateway; this application does not implement a separate login flow.

## Sales procedures

After the MySQL tables exist, execute
`back-end/src/main/resources/db/mysql/stored_procedures.sql` using a MySQL client.
The top-selling procedure uses the front-end category selector's 1-based order:
1 Mobile, 2 TV and AV, 3 Home Appliances, 4 Laptops and Monitors, 5 Accessories.

The review API supports both `GET /URL/reviews/{storeId}/{productId}` (used by
the existing front end) and `GET /reviews/{storeId}/{productId}`. Store,
product, and inventory operations use the corresponding `/store`, `/product`,
and `/inventory` routes.