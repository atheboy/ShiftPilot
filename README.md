# ShiftPilot

ShiftPilot is a shift planning app I built in Java and SQL to show more than just basic forms and database tables.

The idea is simple: if a team needs people assigned to important shifts, the app should help choose the right person instead of making a manager do all the thinking manually.

It looks at things like:

- what skills a person has
- when they are available
- whether they are already booked somewhere else
- whether they have had enough rest between shifts
- whether they are already close to their weekly hour limit

Then it suggests the best people for the job and shows where the schedule still has risk.

## What the app does

- Shows open and filled shifts on a dashboard
- Suggests the best employees for each shift
- Prevents bad assignments like overlaps or not enough rest
- Tracks team workload so you can see who is carrying the most hours
- Highlights risky gaps where coverage is still weak
- Stores the data in SQL with a proper database structure

## Why I made it

I wanted a project that felt closer to a real business tool than a simple school project.

A lot of portfolio apps stop at “add, edit, delete.” This one tries to show decision-making, rules, tradeoffs, and data working together in a practical way.

## Built with

- Java 21
- Spring Boot
- SQL with H2
- Flyway for database setup
- Thymeleaf for the dashboard
- JUnit for testing

## How it works in plain English

When the app checks who should be assigned to a shift, it asks:

1. Does this person have the required skill?
2. Are they actually available at that time?
3. Are they already assigned to another shift that overlaps?
4. Have they had enough rest since their last shift?
5. Would this assignment push them over their weekly hours?

If they pass those checks, the app scores them and ranks the strongest choices first.

## What makes it stronger than a basic CRUD app

- It has real scheduling rules
- It uses SQL migrations and sample data
- It includes both a dashboard and API endpoints
- It is built around a recommendation engine, not just forms
- It includes tests for the core logic

## How to run the app

You only need one thing before starting:

- Java 21 installed on your computer

### Step 1: Open the project folder

Open a terminal inside the `ShiftPilot` folder.

### Step 2: Start the app

If you are using Windows PowerShell, run:

```powershell
.\mvnw.cmd spring-boot:run
```

If you are using macOS or Linux, run:

```bash
./mvnw spring-boot:run
```

### Step 3: Open it in your browser

Once the app finishes starting, open:

- [http://localhost:8080](http://localhost:8080)

You should see the ShiftPilot dashboard with sample shift data already loaded.

### Step 4: Stop the app

When you are done, go back to the terminal and press `Ctrl + C`.

### If it does not start

- Check that Java 21 is installed
- Make sure port `8080` is not already being used by another app
- Try running the test command below to confirm the project builds correctly

## Extra links

- H2 database console: [http://localhost:8080/h2-console](http://localhost:8080/h2-console)
- Dashboard API: [http://localhost:8080/api/dashboard](http://localhost:8080/api/dashboard)
- Sample recommendation API: [http://localhost:8080/api/shifts/1/recommendations](http://localhost:8080/api/shifts/1/recommendations)

If you open the H2 console, use:

- JDBC URL: `jdbc:h2:mem:shiftpilot`
- Username: `sa`
- Password: leave blank

## How to run the tests

If you want to check that everything is working, run the tests.

On Windows PowerShell:

```powershell
.\mvnw.cmd test
```

On macOS or Linux:

```bash
./mvnw test
```

## Sample data

The app comes with demo data already included, so when it starts you can immediately see:

- employees
- skills
- availability windows
- scheduled shifts
- existing assignments
- open staffing gaps

## Short portfolio note

This project was made to show that I can build something with:

- backend logic
- SQL data design
- business rules
- testing
- a usable interface

Not just something that looks nice, but something that has to think before it acts.

## Verification

The app has now been tested locally in this workspace:

- the Maven test suite passes
- the Spring Boot app starts successfully
- the dashboard responds at `http://localhost:8080`

I have only verified it directly on this Windows machine here. The project uses standard Java, Spring Boot, and Maven wrapper scripts for Windows, macOS, and Linux, so it should be portable, but cross-platform proof would still come from running it on those systems or adding CI for them.
