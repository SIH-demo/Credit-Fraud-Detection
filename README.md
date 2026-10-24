# Credit Card Fraud Detection System

An intelligent, web-based application for detecting potentially fraudulent credit card transactions using machine learning, data analytics, and an interactive dashboard.

## Project Overview

The Credit Card Fraud Detection System aims to analyze transaction data, identify potentially suspicious transactions, assign risk scores, and present useful insights through a web dashboard.

The system is designed for learning and experimentation with machine learning models, backend APIs, frontend development, database management, and DevOps practices.

**Important:** The system provides fraud-risk predictions, not definitive confirmation that a transaction is fraudulent.

## Technology Stack

- **Frontend:** HTML, CSS, JavaScript
- **Backend:** Java, Spring Boot
- **Machine Learning Service:** Python, FastAPI, Scikit-learn
- **Data Analytics:** Python, Pandas, NumPy, Matplotlib, Seaborn
- **Database:** PostgreSQL
- **Containerization:** Docker, Docker Compose
- **CI/CD:** GitHub Actions
- **Version Control & Project Management:** Git, GitHub, Jira

## Project Structure

```text
credit-card-fraud-detection/
├── frontend/          # User interface and dashboard
├── backend/           # Spring Boot APIs and business logic
├── ml-service/        # ML models and FastAPI endpoints
├── analytics/         # Data analysis and reporting
├── .gitignore
├── .env.example
└── README.md
```

## Core Features

- Single-transaction fraud-risk prediction
- CSV-based batch transaction analysis
- Machine learning model comparison
- Risk scores and risk-level classification
- Interactive dashboard and analytical visualizations
- Prediction history and filtering
- Transaction investigation interface
- Model evaluation using precision, recall, F1-score, and PR-AUC
- Containerized application deployment

*Features will be implemented progressively during development.*

## System Architecture

1. The **frontend** collects transaction details and displays results.
2. The **Spring Boot backend** handles application APIs, business logic, and database operations.
3. The **FastAPI ML service** preprocesses supported inputs and generates fraud-risk predictions.
4. **MySQL** stores application records and prediction history.
5. The **analytics module** evaluates datasets and generates insights.
6. **Docker and GitHub Actions** support consistent environments and automated checks.


## Getting Started

### Prerequisites

Install the following tools before running the project:

- Git
- Java JDK compatible with the project's Spring Boot version
- Maven
- Python and pip
- Docker and Docker Compose

### 1. Clone the Repository

```bash
git clone <YOUR_GITHUB_REPOSITORY_URL>
cd credit-card-fraud-detection
```

### 2. Configure Environment Variables

Create your local environment configuration using `.env.example` as a reference.

```bash
cp .env.example .env
```

On Windows, you can copy `.env.example` to `.env` manually.

Update the local configuration with the required database connection details and service settings. Never commit `.env` or real credentials.

### 3. Run the Application

Once the Docker Compose configuration is implemented, start the services using:

```bash
docker compose up --build
```

To stop the services:

```bash
docker compose down
```

For service-specific development instructions, refer to the documentation in each module's directory.

## Development Workflow

1. Create a separate feature branch for each task.
2. Make and test your changes locally.
3. Commit changes with clear messages.
4. Push your feature branch to GitHub.
5. Open a pull request targeting `main`.
6. Request a review and resolve feedback before merging.

Example:

```bash
git checkout -b feature/transaction-prediction
git add .
git commit -m "feat: add transaction prediction"
git push -u origin feature/transaction-prediction
```


## Testing and Evaluation

The project will use appropriate unit, integration, and API tests. Machine learning models will be evaluated using fraud-detection metrics such as precision, recall, F1-score, and PR-AUC.

Because fraud datasets are often highly imbalanced, accuracy alone will not be treated as a sufficient measure of model performance.


## Project Status

**Status:** In Development

This project is being developed collaboratively as a learning-focused software engineering and machine learning project.


