
```markdown
# Real-Time High-Concurrency Leaderboard Engine

A high-performance, low-latency real-time leaderboard and analytics engine built to run efficiently on resource-constrained infrastructure (optimized for a 1GB RAM AWS EC2 instance). 

This project demonstrates production-grade system design patterns, combining relational data durability with in-memory sorted caching, race-condition-free atomic updates, and asynchronous event decoupling.

---

## 🏗️ Architecture & System Design

The system implements a **Dual-Storage Pattern**:
1. **PostgreSQL (Persistence Tier):** Acts as the primary source of truth for player records, ensuring data durability across container restarts and failures.
2. **Redis (Ranking & Cache Tier):** Utilizes Redis Sorted Sets (`ZSET`) to deliver $O(\log N)$ real-time leaderboard rankings and range queries without taxing the relational database.
3. **Asynchronous Decoupling:** Non-critical background workflows (such as audit logs, metric tracking, and background processing) are offloaded to a separate thread pool using Spring's `@Async` and event publishing model.


```

```
              ┌───────────────────────┐
              │   Client / Load Test  │
              └───────────┬───────────┘
                          │ HTTP
                          ▼
              ┌───────────────────────┐
              │   Spring Boot (Tomcat)│
              └─────┬───────────┬─────┘
                    │           │
 Async Event        │           │ Atomic Write / Read
  Processing        ▼           ▼
              ┌──────────┐   ┌──────────┐
              │ Worker   │   │  Redis   │ (ZSET / O(log N))
              │ Thread   │   └──────────┘
              └──────────┘   ┌──────────┐
                             │ Postgres │ (Durable Persistence)
                             └──────────┘

```

```

---

## 🚀 Key Engineering Highlights

* **Concurrency Safety Without Lock Contention:** Avoids slow read-modify-write optimistic locking failures (`ObjectOptimisticLockingFailureException`) under high write loads by executing direct database atomic increments (`UPDATE ... SET score = score + :n`) combined with Redis `ZINCRBY`.
* **Resource Governance (1GB RAM Optimization):** Strictly budgeted memory caps across the entire stack (`-Xmx384m` JVM heap allocation, strict Docker Compose memory limits) to guarantee zero Out-Of-Memory (OOM) crashes on low-tier cloud instances (`t2/t3.micro`).
* **Container Orchestration:** Fully containerized multi-container architecture managed via Docker Compose with dedicated bridge networking and persistent volumes for database storage.

---

## 🧰 Tech Stack

* **Language & Framework:** Java 21, Spring Boot 4.x, Spring Data JPA, Spring Data Redis
* **Databases:** PostgreSQL 16 (Alpine), Redis 7 (Alpine)
* **Build Tool:** Maven (with Maven Wrapper)
* **Infrastructure:** Docker, Docker Compose, AWS EC2, VS Code Remote-SSH

---

## 📁 Project Structure

```text
analytics-engine/
├── .mvn/wrapper/               # Maven wrapper configuration
├── src/
│   ├── main/
│   │   ├── java/com/example/analytics/
│   │   │   ├── AnalyticsEngineApplication.java
│   │   │   ├── LeaderboardController.java
│   │   │   ├── LeaderboardService.java
│   │   │   ├── PlayerScore.java
│   │   │   ├── PlayerScoreRepository.java
│   │   │   ├── ScoreSubmittedEvent.java
│   │   │   └── ScoreEventListener.java
│   │   └── resources/
│   │       └── application.properties
│   └── test/                   # Unit & integration tests
├── Dockerfile                  # Multi-stage container packaging
├── docker-compose.yml          # Multi-container stack orchestration
├── mvnw / mvnw.cmd             # Maven executable wrappers
└── pom.xml                     # Project dependencies

```

---

## ⚙️ Getting Started & Local Deployment

### Prerequisites

* Java 21 SDK installed (if running natively)
* Docker and Docker Compose (recommended for containerized execution)

### 1. Clone the Repository

```bash
git clone [https://github.com/your-username/analytics-engine.git](https://github.com/your-username/analytics-engine.git)
cd analytics-engine

```

### 2. Build the Application Jar

Compile the project and bundle dependencies using the Maven wrapper:

```bash
./mvnw clean package -DskipTests

```

### 3. Spin Up the Stack with Docker Compose

Orchestrate PostgreSQL, Redis, and the Spring Boot application containers with resource limits enforced:

```bash
docker compose up --build -d

```

Verify that all containers are running healthy:

```bash
docker compose ps

```

---

## 🔌 API Endpoints

### 1. Submit or Increment Player Score

* **URL:** `POST /api/leaderboard/submit`
* **Query Parameters:**
* `username` (String)
* `score` (Long points to add)


* **Example Request:**
```bash
curl -X POST "[http://127.0.0.1:8080/api/leaderboard/submit?username=player1&score=1500](http://127.0.0.1:8080/api/leaderboard/submit?username=player1&score=1500)"

```



### 2. Retrieve Top Players (Leaderboard)

* **URL:** `GET /api/leaderboard/top`
* **Query Parameters:**
* `limit` (Integer, default 10)


* **Example Request:**
```bash
curl "[http://127.0.0.1:8080/api/leaderboard/top?limit=10](http://127.0.0.1:8080/api/leaderboard/top?limit=10)"

```



---

## 📊 Load Testing

You can benchmark the system's throughput and verify concurrency handling using **ApacheBench (`ab`)**:

* **High-Concurrency Read Test (Redis Cache):**
```bash
ab -n 10000 -c 100 "[http://127.0.0.1:8080/api/leaderboard/top?limit=10](http://127.0.0.1:8080/api/leaderboard/top?limit=10)"

```


* **High-Contention Write Test (Atomic DB + Redis):**
```bash
ab -n 1000 -c 10 -m POST "[http://127.0.0.1:8080/api/leaderboard/submit?username=player1&score=10](http://127.0.0.1:8080/api/leaderboard/submit?username=player1&score=10)"

```



```

```