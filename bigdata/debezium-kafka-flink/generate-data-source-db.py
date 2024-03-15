import schedule
import time
import faker
import psycopg2
from datetime import datetime
import random

pg_host = "localhost"
pg_port = 5432
pg_user = "postgres"
pg_password = "postgres"


db_source = "financial_db"

fake = faker.Faker()

def generate_transaction():
    user = fake.simple_profile()

    return {
        "transactionId": fake.uuid4(),
        "userId": user['username'],
        "timestamp": datetime.utcnow().timestamp(),
        "amount": round(random.uniform(10, 1000), 2),
        "currency": random.choice(['USD', 'GBP']),
        'city': fake.city(),
        "country": fake.country(),
        "merchantName": fake.company(),
        "paymentMethod": random.choice(['credit_card', 'debit_card', 'online_transfer']),
        "ipAddress": fake.ipv4(),
        "voucherCode": random.choice(['', 'DISCOUNT10', '']),
        'affiliate_id': fake.uuid4()
    }

def generate_data():

    conn = psycopg2.connect(
        host=pg_host,
        database=db_source,
        user=pg_user,
        password=pg_password,
        port=pg_port
    )

    transaction = generate_transaction()
    cur = conn.cursor()
    print(transaction)

    cur.execute(
        """
        INSERT INTO transactions(transaction_id, user_id, timestamp, amount, currency, city, country, merchant_name, payment_method, 
        ip_address, affiliate_id, voucher_code)
        VALUES (%s, %s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s)
        """, (transaction["transactionId"], transaction["userId"], datetime.fromtimestamp(transaction["timestamp"]).strftime('%Y-%m-%d %H:%M:%S'),
              transaction["amount"], transaction["currency"], transaction["city"], transaction["country"],
              transaction["merchantName"], transaction["paymentMethod"], transaction["ipAddress"],
              transaction["affiliate_id"], transaction["voucherCode"])
    )

    cur.close()
    conn.commit()

if __name__ == "__main__":
    generate_data()
    schedule.every(10).seconds.do(generate_data)

    while True:
        schedule.run_pending()
        time.sleep(0.5)