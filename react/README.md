# Final Capstone Project – Tech Elevator
## 🚗 "VALETE" - Parking Lot Valet 🏎️
### Team Members: Calvin Almanza, Jamia Conwell, Zane Roelen, Sunkyo Oh, and Evan Wheeler

## 🗺️ Overview
This project represents the Final Capstone deliverable for the Java Blue cohort, serving as a culmination of full-stack software engineering competencies. It embodies the design and implementation of a distributed, service-oriented application leveraging a Java 17 / Spring Boot backend, a React-based SPA frontend, and a PostgreSQL relational database for persistence.

The system architecture adheres to layered design principles, separating concerns across controllers, services, and data access objects (DAOs). It exposes a suite of RESTful API endpoints secured via Spring Security with role-based access control (RBAC). These endpoints facilitate stateful interactions between client and server, enabling seamless integration of user authentication, domain logic, and transactional database operations.

On the client side, the React application is scaffolded with Vite for optimized bundling and hot-module reloading, and it employs Axios for asynchronous HTTP communication with the backend API. State management is orchestrated using the Context API, ensuring that authentication tokens, user roles, and application-level state are consistently propagated throughout the component hierarchy.

The application domain centers on vehicle lifecycle management within a valet parking context. Users can:

- Register and associate vehicles with their accounts from the public [NHTSA API](https://vpic.nhtsa.dot.gov/api/)
- Initiate parking check-in flows
- Monitor the real-time status of parked vehicles
- Complete checkout workflows with dynamic billing computations based on time parked

The database layer is backed by PostgreSQL, with schema definitions and migrations scripted in SQL to ensure deterministic, reproducible environments across development and deployment stages. Referential integrity, indexing strategies, and normalized relational structures support transactional consistency and performant queries under concurrent load.

This implementation not only demonstrates proficiency in modern web application development but also integrates cross-cutting concerns such as exception handling, logging, and testing (unit, integration, and end-to-end) to ensure reliability, maintainability, and scalability of the system in production-like environments.

## ✔️ Features
- 🔐 **Authentication & Authorization** with role-based access (`ROLE_PATRON`, `ROLE_VALET`, `ROLE_ADMIN`).
- 🚗 **Vehicle Management**: register and view makes, models, and user-owned vehicles.
- 🅿️ **Parking/Valet System**: check-in, track, and pick up cars with ticketing and billing.
- 💳 **Payment Simulation**: calculates charges based on time parked.
- 📊 **Responsive Frontend** built with React (Vite, Axios, Context API).
- 🛠️ **Backend API** built with Spring Boot and REST controllers.
- 💾 **Database Integration** using PostgreSQL with schema migrations.
- ✅ **Unit and Integration Tests** included.

## ⚙️ Technologies
- **Backend:** Java 17, Spring Boot, Spring Security, JDBC, Maven
- **Frontend:** React, Vite, Axios
- **Database:** PostgreSQL
- **Tools:** IntelliJ, Visual Studio Code, pgAdmin, Postman, Git

## 🧩 Project Structure
```
/java-blue-finalcapstone-team2-main
 ├── java/                          # Spring Boot backend
 │   ├── src/main/java/...          # Controllers, Services, DAOs, Models
 │   ├── src/test/java/...          # Unit and integration tests
 │   ├── database/schema.sql        # Database schema
 |   └── database/PopulateLot.sql   # Populate database (lot) via pgAdmin
 ├── react/                         # React frontend
 │   ├── src/                       # Components, views, context, services, images 
 │   └── public/                    # CSS Reset and Global
```

## 🛠️ Setup Instructions

Install the following development tools before running the project:
- [Java 17 (Adoptium)](https://adoptium.net/) – Required for running the Spring Boot backend.  
- [PostgreSQL](https://www.postgresql.org/) – Relational database management system used for persistence.  
- [IntelliJ IDEA](https://www.jetbrains.com/idea/) – Recommended IDE for backend Java/Spring Boot development.  
- [Visual Studio Code](https://code.visualstudio.com/) – Lightweight editor for frontend React development.  
- [pgAdmin](https://www.pgadmin.org/) – GUI tool for managing and inspecting the PostgreSQL database.  
- [Postman](https://www.postman.com/) – API client for testing RESTful endpoints.  
- [Git](https://git-scm.com/) – Version control system for managing source code and collaborating with team members.

### Backend (Spring Boot)
1. Create a PostgreSQL database:
   ```sql
   CREATE DATABASE final_capstone;
   ```
2. Update `application.properties` (or `application.yml`) with your DB credentials.
3. From the backend directory (java-blue-finalcapstone-team2\java\database), run:
   ```bash
   via Git terminal: ./create.sh
   via pgAdmin: PopulateLot.sql (if you want default vehicles for presentation, testing, etc.)
   ```
4. Open '\java' folder or 'pom.xml' in Maven project via IntelliJ.
5. Run 'Application', under '\src\main\java\com\techelevator'.
6. Your backend is now running, including endpoints, accessible via Postman, etc.

Next take a moment to review the `.env` file that's located in the root of the project. You can store environment variables that you want to use throughout your application in this file. When you open it, it'll look like this:
```
VITE_REMOTE_API=http://localhost:9000
```
​
*Note:* the Java Spring Boot application is configured to run on port `9000` instead of `8080`.

### Frontend (React)
1. Navigate to the frontend folder:
   ```bash
   cd react
   ```
2. Install dependencies:
   ```bash
   npm install
   ```
3. Start the dev server:
   ```bash
   npm run dev
   ```
   The app should now be available at [http://localhost:5173](http://localhost:5173).

### API Documentation
- Base URL: `http://localhost:9000`
- Endpoints:
  - `POST /login` – authenticate user
  - `GET /vehicles` – list makes/models
  - `POST /user/vehicles` – add a user’s vehicle
  - `GET /valet/parked` – list parked cars
  - `POST /valet/checkin` – check in a car
  - `POST /valet/checkout/{ticketId}` – complete pickup and calculate charges

(See controllers for a full list.)

## 🎨 Application styling

The application includes two global CSS files—`public/css/global.css` and `public/css/reset.css`—that provide some basic styling to give you a starting point. You're free to change and modify these files to style the application how you want.
​
## 🔓 Authentication
​
When you first run the project and visit the base URL, you're taken to the home page at the route `/`. There's a side nav with a link to the login page. From there you can login (see the server instructions for default credentials) or register a new user.

Once you log in, the nav changes to have links to "Profile" (a protected route) and logout. The route for "Profile" uses the `<ProtectedRoute>` component to verify there's a logged-in user before rendering the content.

The authentication features work as you've seen in the curriculum already:

* `src/context/UserContext.jsx` and `src/context/UserProvider.jsx` is the user data context and provider for tracking and supplying user data to other components
* The `<UserProvider>` component surrounds the application code in `main.jsx` to provide the user context to any component that requests it
* `src/views/LoginView/LoginView.jsx` sets the user in `UserContext`, stores the user and token in `localStorage`, and adds the token to default Axios requests
* `src/views/LogoutView.jsx` removes the user from `UserContext`, removes the user and token from `localStorage`, and removes the token from Axios

## 🔬 Testing
- Run backend tests with:
  ```bash
  mvn test
  ```
- Frontend testing uses React Testing Library (if configured):
  ```bash
  npm test
  ```

## ⚖️ License / Legal
This project was created for educational purposes only as part of the Tech Elevator Capstone Program. It is a non-commercial, academic exercise and is not intended for production use.

All materials in this repository are provided “as-is” without warranty of any kind. The authors and contributors make no guarantees of accuracy, reliability, or fitness for any purpose. Use of this project outside its educational context is at the user’s own risk.

References to third-party services, data sources, or trademarks are included for demonstration only and do not imply endorsement or permission for commercial use. The contributors are not liable for any damages, losses, or legal issues that may arise from use, modification, or distribution of this code.
##

# 🛤️ Roadmap
## 🎯 Vision Statement
The "VALETE" Valet Parking Management System is designed to modernize and streamline the valet experience for both patrons and attendants at a fine dining establishment. The application provides a centralized, web-based solution for managing a single parking lot, with configurable capacity and hourly pricing of $5.

`Anonymous` users can register or log in as patrons or valets, and view lot availability with prominent warnings when the lot is full. `Patrons` can add their vehicles, check their current parking total in real time, use their valet slip to request vehicle pickup, and receive responsive service from attendants. `Valets` can perform check-ins with detailed vehicle and owner information, issue and/or print valet slips, track lot availability, process pickup requests, mark cars as picked up, and calculate totals. They can also manage the lot size as the business grows.

The system further includes an `Admin` view for managing valet registration invites, ensuring secure access control. It supports operational branding and delivers a clear, responsive interface for efficient workflows. By integrating authentication, ticketing, real-time monitoring, and automated billing, the application offers a professional, scalable solution that enhances both the customer experience and valet operations.

## 🚧 V2 and To Do / Improvements 
- Need to allow the Valet user to populate the database from existing Patron users and their associated cars.
- Need to add VIN 17 character limitation to Valet Check-In.
- Could integrate the NHTSA API Vehicle Browser to Valet View as well.
- SMS Messaging Slice via Twilio is included, but not integrated, in the following files. This would be used largely for Pickup purposes, both from the Patron to the Valet, and from Valet to Patron. This would be mission critical in a real-world application.
    - SMSMessageDao
    - JdbcMessageDao
    - SMSPickupRequestDao
    - JdbcSMSPickupRequestDao
    - Model/Parking/SmsMessage
    - Model/Parking/SmsPickupRequest
    - SmsService
    - SmsController
- Increase 'Added Lot' functionality.
- Need to enhance the ParkingBrowser to include better styling (responsive container, buttons, etc.) and some details about the car occupying this space, e.g. the License Plate.