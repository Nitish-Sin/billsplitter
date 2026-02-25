# 💸 BillSplitter — Group Expense Tracker

A full-stack Spring Boot web application for splitting group expenses with a peer-approval workflow.

---

## 🚀 Features

| Feature | Description |
|---|---|
| **Multi-device access** | Everyone accesses via their browser on the local network or internet |
| **Post expenses** | Record who paid, amount, and who shares the cost |
| **Peer approval** | Every expense needs **3 approvals** before counting |
| **Conclude the day** | See exactly who owes whom at end of day |
| **Settlement history** | View all past day summaries |

---

## 🛠️ Tech Stack

- **Backend**: Java 17, Spring Boot 3.2, Spring Security, Spring Data JPA
- **Database**: PostgreSQL 14+
- **Frontend**: Thymeleaf, HTML/CSS/JS (no extra frameworks)
- **Build**: Maven

---

## 📦 Project Structure

```
billsplitter/
├── src/main/java/com/billsplitter/
│   ├── BillSplitterApplication.java   ← Main entry point
│   ├── config/
│   │   └── SecurityConfig.java        ← Spring Security setup
│   ├── controller/
│   │   ├── AuthController.java        ← Login / Register
│   │   └── ExpenseController.java     ← All expense & settlement logic
│   ├── model/
│   │   ├── User.java
│   │   ├── Expense.java
│   │   ├── ExpenseApproval.java
│   │   ├── DaySettlement.java
│   │   └── SettlementTransaction.java
│   ├── repository/                    ← JPA repositories
│   ├── service/
│   │   ├── UserService.java
│   │   ├── ExpenseService.java        ← Core business logic + settlement math
│   │   ├── SettlementService.java
│   │   └── CustomUserDetailsService.java
│   └── dto/
├── src/main/resources/
│   ├── application.properties
│   ├── static/css/style.css
│   ├── static/js/app.js
│   └── templates/
│       ├── login.html
│       ├── register.html
│       ├── dashboard.html
│       ├── expense-detail.html
│       ├── conclude.html
│       ├── settlement.html
│       └── history.html
├── setup.sql
└── pom.xml
```

---

## ⚙️ Setup Instructions

### 1. Prerequisites

- Java 17 (JDK)
- Maven 3.8+
- PostgreSQL 14+
- Eclipse IDE with Spring Tools (or IntelliJ / VS Code)

### 2. Database Setup

```bash
# Log in to PostgreSQL
psql -U postgres

# Create the database
CREATE DATABASE billsplitter_db;
\q
```

Or run `setup.sql` in pgAdmin.

### 3. Configure Database Credentials

Edit `src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/billsplitter_db
spring.datasource.username=postgres
spring.datasource.password=YOUR_ACTUAL_PASSWORD
```

### 4. Import into Eclipse

1. Open Eclipse → **File → Import → Existing Maven Projects**
2. Browse to the `billsplitter` folder
3. Click **Finish**
4. Wait for Maven to download dependencies (first time may take a few minutes)

### 5. Run the Application

- Right-click `BillSplitterApplication.java` → **Run As → Spring Boot App**
- OR: In terminal: `mvn spring-boot:run`

### 6. Access the App

- Open browser: **http://localhost:8080**
- Register accounts for each person in your group
- Everyone connects on the same Wi-Fi using your machine's IP:
  `http://YOUR_IP:8080` (e.g., `http://192.168.1.5:8080`)

---

## 🔄 How the Workflow Works

```
1. Any user posts an expense (description + amount + who shares it)
   → Status: PENDING

2. Other group members see it in "Pending Approvals" and vote Approve/Reject
   - Poster CANNOT vote on their own expense
   - Each person can vote only once
   → After 3 approvals: Status: APPROVED
   → Any rejection: Status: REJECTED (not counted)

3. At end of day: Any user clicks "Conclude Day"
   → Settlement preview shows who owes whom
   → After confirmation, all APPROVED expenses are finalized
   → Optimized settlement transactions are saved

4. View breakdown in "History" → pick any past day
```

---

## 💡 Settlement Algorithm

Uses a **greedy net-balance algorithm**:

1. For each approved expense, calculate each person's share
2. Sum up net balance per person (positive = owed money, negative = owes money)  
3. Greedily match biggest creditor with biggest debtor to minimize number of transactions

**Example:**  
- Alice paid ₹900 for 3 people → Bob and Carol each owe ₹300  
- Bob paid ₹300 for 2 people → Carol owes ₹150  

**Result:** Carol pays Alice ₹450, Carol pays Bob ₹150

---

## 🔧 Configuration

Change number of required approvals in `application.properties`:

```properties
app.approvals.required=3
```

---

## 🌐 Deploying for Multiple Devices

To let all group members access on same Wi-Fi:

1. Find your machine's local IP: `ipconfig` (Windows) or `ifconfig` (Mac/Linux)
2. Share: `http://192.168.x.x:8080` with the group
3. Each person registers their own account and uses the app on their phone/laptop

For internet access, deploy to a cloud platform (Render, Railway, Heroku) with a PostgreSQL add-on.
