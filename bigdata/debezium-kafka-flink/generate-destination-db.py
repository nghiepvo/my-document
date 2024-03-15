
import psycopg2
from psycopg2.extensions import ISOLATION_LEVEL_AUTOCOMMIT

pg_host = "localhost"
pg_port = 5432
pg_user = "postgres"
pg_password = "postgres"


db_source = "financial_db"
db_destination = "financial_db_destination"

def create_db_destination(conn):
    conn.set_isolation_level(ISOLATION_LEVEL_AUTOCOMMIT);
    cursor = conn.cursor()

    cursor.execute("CREATE DATABASE " + db_destination + ";")

    cursor.close()
    conn.commit()

def create_table(conn):
    cursor = conn.cursor()

    cursor.execute(
        """
        CREATE TABLE IF NOT EXISTS transactions (
            transaction_id VARCHAR(255) PRIMARY KEY,
            user_id VARCHAR(255),
            timestamp TIMESTAMP,
            amount DOUBLE PRECISION,
            currency VARCHAR(255),
            city VARCHAR(255),
            country VARCHAR(255),
            merchant_name VARCHAR(255),
            payment_method VARCHAR(255),
            ip_address VARCHAR(255),
            voucher_code VARCHAR(255),
            affiliate_id VARCHAR(255)
        )
        """)

    cursor.close()
    conn.commit()

if __name__ == "__main__":
    conn = psycopg2.connect(
        host=pg_host,
        database=db_source,
        user=pg_user,
        password=pg_password,
        port=pg_port
    )

    create_db_destination(conn)

    conn = psycopg2.connect(
        host=pg_host,
        database=db_destination,
        user=pg_user,
        password=pg_password,
        port=pg_port
    )

    create_table(conn)