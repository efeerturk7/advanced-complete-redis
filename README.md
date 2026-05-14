![Java](https://img.shields.io/badge/Java-25-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.x-6DB33F?style=for-the-badge&logo=spring-boot&logoColor=white)
![Redis](https://img.shields.io/badge/Redis-High_Availability-DC382D?style=for-the-badge&logo=redis&logoColor=white)
![Redisson](https://img.shields.io/badge/Redisson-Distributed_Lock-CC0000?style=for-the-badge&logo=java&logoColor=white)
![H2 Database](https://img.shields.io/badge/H2-In_Memory_DB-003B57?style=for-the-badge&logo=database&logoColor=white)

**Flash Sale Engine** is an advanced, production-ready backend API designed to manage high-concurrency e-commerce operations, specifically tailored for "Flash Sale" scenarios where thousands of users compete for limited stock.

This project demonstrates a modern **High-Availability Data Architecture**. It goes far beyond simple caching, implementing **Distributed Locking (Redisson)**, **API Rate Limiting**, **Probabilistic Analytics (HyperLogLog)**, and a **Master-Replica Sentinel Topology** to ensure zero downtime and strict data consistency under heavy load.

---

## 🔗 API TESTING & USAGE

The API runs locally using your machine's hardware to process high-speed transactions safely without race conditions.

👉 **Ready to test via Postman or any REST Client at:** `http://localhost:8080/api/v1/flash-sale`

---

## 🏗 System Architecture & Workflow

### 🔄 Request Lifecycle (High-Concurrency Purchase Flow)
How a purchase request travels securely from the user, passes security gates, and updates stock without race conditions:

> **🌍 Client Request** 👉  **🛡️ Rate Limiter (INCR/EXPIRE)** 👉  **🧠 Cache Lookup** 👉 **🔒 Redisson Distributed Lock** 👉 **⏸️ Watchdog Extends Lease** 👉 **💾 Database Update & Cache Evict** 👉 **🔓 Release Lock** 👉 **⚡ JSON Success Output**

### ⚙️ Runtime Architecture
How the application handles complex traffic spikes in production:

1.  **🌍 Client Request:** User attempts to view or buy a limited-stock item.
2.  **🛡️ Traffic Security (`RateLimiterService`):** Intercepts the request. Checks if the user is spamming the API (Sliding/Fixed window using Redis). Blocks DDOS attempts before hitting the database.
3.  **📊 Analytics (`HyperLogLog`):** Instantly logs the unique user IP in memory using minimal RAM footprint.
4.  **💾 Memory Retrieval (`@Cacheable`):** Checks Redis for product details. If present, skips the DB entirely.
5.  **🔒 Critical Section (`RedissonClient`):** Creates a distributed lock across all application instances to ensure only one thread mutates the specific product's stock at a time.

---

## 🚀 Key Technical Features (Advanced Redis Implementation)

This project heavily focuses on system stability, concurrency management, and response performance.

### 1. 🛡️ Custom Rate Limiting Architecture (Security First)
Implemented a custom `RateLimiterService` utilizing atomic Redis commands (`INCR` and `EXPIRE`):
* **Spam Protection:** Automatically detects and blocks users sending more than 3 purchase requests within a 30-second fixed window.
* **Network Optimization:** Returns HTTP 429 (Too Many Requests) instantly from RAM, saving JVM and DB threads.

### 2. 🔐 Distributed Locking & Mutex (Redisson)
* **Zero Race Conditions:** Uses Redisson's `RLock` to prevent multiple threads (or scaled instances) from overselling a product.
* **Watchdog Mechanism:** Prevents deadlocks. If a server crashes while holding a lock, the Watchdog dies, and the lock is automatically released after the TTL expires.

### 3. 🧠 Enterprise Caching Strategy
* **Read-Through Architecture:** Utilizes Spring's `@Cacheable` to intercept database calls, reducing latency to milliseconds.
* **Smart Eviction:** Uses `@CacheEvict` specifically during stock updates to prevent "Cache Pollution" and ensure users never see stale data.
* **TTL Management:** Globally configured `RedisCacheConfiguration` strictly enforces a 15-minute Time-To-Live for all cached entities to protect RAM.

### 4. 🧩 Probabilistic Analytics (HyperLogLog)
* **Memory-Optimized Counting:** Replaced traditional `Set` data structures with HyperLogLog to count unique product viewers.
* **O(1) Efficiency:** capable of counting millions of unique IP addresses while strictly utilizing a maximum of **12 KB** of memory, with a marginal ~0.81% error rate.

### 5. 🗂️ High Availability (Redis Sentinel)
* **SPOF Prevention:** Transitioned from a standalone Redis instance to a **Sentinel Topology** in `RedisConfig`.
* **Automatic Failover:** If the Master node fails, Sentinels elect a new Replica as Master and dynamically reroute Spring Boot traffic without any human intervention or application restart.

---

## 🛠️ Tech Stack

| Category            | Technology                                 |
|:--------------------|:-------------------------------------------|
| **Language**        | Java 25                                    |
| **Framework**       | Spring Boot 3.x                            |
| **Cache Provider**  | Spring Data Redis                          |
| **Lock Mechanism**  | Redisson (3.27.0)                          |
| **Database (Test)** | H2 In-Memory DB                            |
| **ORM**             | Spring Data JPA / Hibernate                |
| **Boilerplate**     | Lombok                                     |
| **High Availability**| Redis Sentinel / Lettuce Client            |
| **Traffic Control** | Redis Atomic Commands (INCR/EXPIRE)        |
| **Analytics**       | Redis HyperLogLog (PFADD/PFCOUNT)          |

---

## 🧠 Advanced Concurrency Workflow (How it works)
1.  **Concurrent Assault:** 1,000 users click "Buy" on a product with exactly 5 items left in stock.
2.  **Gatekeeping:** The `RateLimiter` drops 60% of the requests that are identified as duplicate spam clicks from the same users.
3.  **Lock Acquisition:** The remaining valid requests attempt to acquire a distributed lock (`lock:product:ID`) via Redisson.
4.  **Sequential Processing:** Only 1 thread gets the lock. It checks the stock, updates the DB, clears the Cache, and releases the lock.
5.  **Rejection Handling:** Threads that fail to acquire the lock within the 3-second timeout window are gracefully rejected with a "System busy" message, protecting the database from thread exhaustion.

---

## ⚙️ How to Run Locally

To test all advanced features, you need a local Redis instance.

1.  **Install and Start Redis (via Docker for ease):**
    Run a standard Redis container in your terminal:
    ```bash
    docker run -d --name redis-stack -p 6379:6379 redis:latest
    ```
    *(Note: To test High Availability/Sentinel, you will need a `docker-compose.yml` with Master/Replica/Sentinel nodes. For basic local testing, comment out the Sentinel Config in `RedisConfig.java` and let Spring Boot default to localhost:6379).*

2.  **Clone the repository:**
    ```bash
    git clone https://github.com/efeerturk7/redis-flash-sale.git
    cd redis-flash-sale
    ```

3.  **Build and Run the Application:**
    Ensure you are using Java 25 and have Maven installed.
    ```bash
    mvn clean install
    mvn spring-boot:run
    ```

4.  **Test the Endpoints:**
    * **View Product (HLL & Cache Test):**
      `GET http://localhost:8080/api/v1/flash-sale/product/1?userIp=192.168.1.5`
      *(Run this multiple times with the same IP, then different IPs to test the HyperLogLog).*
    * **Analytics Report:**
      `GET http://localhost:8080/api/v1/flash-sale/product/1/stats`
      *(Returns the unique visitor count from HLL).*
    * **Purchase (Distributed Lock & Rate Limit Test):**
      `POST http://localhost:8080/api/v1/flash-sale/purchase?productId=1&userId=user_123&quantity=# 🛒 E-Commerce Flash Sale Engine - Advanced Redis Architecture