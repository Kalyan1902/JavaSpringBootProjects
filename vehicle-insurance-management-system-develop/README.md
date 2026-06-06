# 🚗 Vehicle Insurance Policy & Claim Management System

A comprehensive Spring Boot MVC application for managing vehicle insurance policies and claims. The system provides specialized portals for both **Customers** and **Administrators** with secure, role-based authentication.

---

## 📌 Project Overview
This application follows a layered architecture to provide a seamless insurance lifecycle. 

* **Customers** can register vehicles, purchase policies, and track claims.
* **Admins** monitor system-wide data and process insurance claims.


##  User Roles

### Customer
* **Registration & Security:** Securely register and manage login credentials.
* **Profile Management:** View and update personal profile information.
* **Vehicle Assets:** Add, edit, and track registered vehicles.
* **Policy Lifecycle:** Purchase new insurance and renew existing policies.
* **Claim Management:** File insurance claims and monitor approval status.

###  Admin
* **Centralized Portal:** Dedicated login for administrative oversight.
* **Dashboard Analytics:** View system-wide statistics (Total Users, Policies, etc.).
* **Data Monitoring:** Access lists of all customers, vehicles, and active policies.
* **Claim Processing:** Review pending claims and perform **Approve/Reject** actions.
 
 ### Features
 **Authentication & Security**
 
-Role-based authentication (ADMIN / CUSTOMER)
-Session-based login using Spring Security
-BCrypt password encryption
-Separate login portals for Admin and Customer
 
**Vehicle Management**
 
-Register vehicles (Car, Bike, Truck) 
-Prevent duplicate registration numbers
-Lock vehicle editing if an approved claim exists
 
**Policy Management**
 
-Create insurance policies for vehicles 
-Automatic premium calculation
-Policy lifecycle tracking (ACTIVE / EXPIRED)
 
 
 **Claim Management**
 
-File insurance claims
-Claim amount validation against coverage
-Claim status tracking (SUBMITTED, APPROVED, REJECTED)
-Claim approval/rejection by Admin
 
 **Admin Dashboard**
 
-Total customers
-Total vehicles
-Total policies
 -Pending & approved claims
 
##  Technology Stack

| Layer | Technologies |
| :--- | :--- |
| **Backend** | Java 21, Spring Boot 3.2.x, Spring Security, JPA/Hibernate |
| **Database** | MySQL |
| **Frontend** | Thymeleaf, HTML5, CSS3, Bootstrap |
| **Testing** | JUnit 5, Mockito, Jacoco |
| **Utilities** | Lombok, Jakarta Validation |
📁**Project Structure**
src/main/java/com/cts/vis
│
├── controller/        # MVC Controllers
├── service/           # Business logic
├── repository/        # JPA repositories
├── model/             # Entity classes
├── dto/               # Data Transfer Objects
├── security/          # Spring Security configuration
├── exception/         # Custom & global exception handling
│
src/main/resources
│
├── templates/
│   ├── customer/      # Customer UI pages
│   ├── admin/         # Admin UI pages
│   └── fragments/     # Common UI fragments
│
├── static/
│   ├── css/
│   ├── js/
│   └── images/
│
└── application.properties
 

 **Database Entities**
The system relies on a relational schema with the following core entities:
User: email, passwordHash, role (ADMIN / CUSTOMER), isActive.
Customer: name, phone, address, user (One-to-One).
Vehicle: registrationNumber, make, model, yearOfManufacture, vehicleType, customer (Many-to-One).
Policy: policyNumber, coverageAmount, premiumAmount, startDate, endDate, policyStatus, vehicle (Many-to-One).
Claim: claimAmount, claimReason, claimDate, claimStatus, policy (Many-to-One).

**Security Design**
Built using Spring Security with a focus on role-based access control (RBAC):
Multi-Chain Security: Implementation of multiple SecurityFilterChains.
Custom Authentication: Uses a custom UserDetailsService with BCrypt password encoding.
Session-Based: Standard session management (No JWT).
Dedicated Entry Points:
Customer Login: /customer/login
Admin Login: /admin/login

 **Premium Calculation** LogicPremiums are dynamically calculated based on specific risk factors:
 Vehicle Type: (e.g., Car, Bike, Truck).
 Vehicle Age: Calculated automatically from the year of manufacture.
 Coverage Amount: The user-selected protection limit.
 Example Logic:$$Premium = Base Premium + (Coverage Amount \times Percentage)$$
 ### Setup & Installation
 Prerequisites
 Java 21
 Maven 3.x
 MySQL 8.x1. 

 ### Database Setup
SQLCREATE DATABASE insurance_db;
2.Configure application.propertiesUpdate your MySQL credentials in src/main/resources/application.properties:Propertiesspring.datasource.url=jdbc:mysql://localhost:3306/insurance_db
spring.datasource.username=your_username
spring.datasource.password=your_password
spring.jpa.hibernate.ddl-auto=update
3. Run the ApplicationBashmvn spring-boot:run
 Application URLsPurposeURL
Customer: Login/customer/login
Customer Dashboard/customer/dashboard
Admin Login/admin/login
Admin Dashboard/admin/dashboard
### Testing & Quality
Run the automated test suite to ensure system stability:Bashmvn test
Code Coverage: After running tests, 
MVC Architecture: Strict separation of concerns between View, Controller, and Model.Layered Design: Distinct functional layers for Controller, Service, and Repository.DTO Pattern: Ensures secure and optimized data transfer between layers.
Global Exception Handling: Centralized error management via @ControllerAdvice.
### Future Enhancements[ ]
PDF Export: Generate policy and claim documents.
Notifications: Email alerts for policy expiry or claim status updates.
Document Upload: Allow customers to upload images for claims.
UI/UX: Advanced reporting with pagination and search filters.
