⚠️ TIMEZONE NOTE (WINDOWS USERS)

On Windows, the system timezone "SE Asia Standard Time"
is mapped by Java to "Asia/Saigon" (legacy ID).

PostgreSQL does NOT accept "Asia/Saigon" and will fail
during JDBC connection initialization.

Therefore:
- The JVM timezone MUST be explicitly set to Asia/Ho_Chi_Minh
- Do NOT rely on system default timezone
- Do NOT use Asia/Saigon anywhere

This project enforces timezone via:
- JVM argument: -Duser.timezone=Asia/Ho_Chi_Minh
- Hibernate property: hibernate.jdbc.time_zone
