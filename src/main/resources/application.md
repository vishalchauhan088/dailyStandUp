spring.application.name=dailyStandUp

# JWT Configuration
# IMPORTANT: In production, use environment variables for secrets!
# Example: app.jwt.secret=${JWT_SECRET:your-secret-here}
app.jwt.secret=
app.jwt.expiration=49000000

# H2 file-based DB (persists data)
spring.datasource.url=jdbc:postgresql://localhost:5432/daily_stand_up
spring.datasource.driverClassName=org.postgresql.Driver
spring.datasource.username=
spring.datasource.password=

# Hibernate settings
spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.show-sql=false
spring.jpa.properties.hibernate.format_sql=true

#flyway
spring.flyway.enabled=true
spring.flyway.baseline-on-migrate=true
spring.flyway.baseline-version=1
spring.flyway.locations=classpath:db/migration

# Logging
logging.level.org.springframework.security=INFO
logging.level.org.springframework.web=INFO
logging.level.org.hibernate.SQL=INFO
logging.level.com.vishalchauhan0688.dailyStandUp=INFO
