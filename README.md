# Spring Boot Notification Microservice

> A robust, scalable, and extensible microservice built with Spring Boot for dispatching notifications through various channels like Telegram, Email, and SMS.

This project listens to a RabbitMQ queue for notification requests and dynamically routes them to the appropriate service based on the request payload. It is designed with modern software engineering principles (SOLID, DRY, KISS) and includes production-ready features like retries, dead-lettering, structured logging, and health checks.

## ✨ Features

- **Multi-Channel Notifications**: Out-of-the-box support for Telegram, Email, and SMS (via Twilio).
- **Message Queue Driven**: Asynchronously consumes notification requests from RabbitMQ for high throughput and resilience.
- **Extensible Strategy Pattern**: Easily add new notification channels (e.g., Slack, Push Notifications) without modifying existing code.
- **Resilient by Design**:
    - **In-Memory Retries**: Automatically retries sending notifications with exponential backoff for transient failures.
    - **Circuit Breaker**: Prevents cascading failures by "failing fast" when an external service (like an email provider) is down.
    - **Dead Letter Queue (DLQ)**: Failed messages are routed to a DLQ for manual inspection and reprocessing, ensuring no message is lost.
- **Dynamic Configuration**:
    - **Feature Flags**: Enable or disable notification channels (like SMS) via a simple configuration property without redeploying.
- **Production-Ready Observability**:
    - **Structured JSON Logging**: Logs are produced in a machine-readable JSON format.
    - **Correlation IDs**: Every request is tagged with a unique `correlationId` for easy tracing across all logs.
    - **AOP-based Method Logging**: Automatically logs method entry, exit, and execution time for key components.
    - **ELK Stack Integration**: Pre-configured to ship logs directly to a local ELK (Elasticsearch, Logstash, Kibana) stack.
    - **Health Checks**: Exposes a `/actuator/health` endpoint via Spring Boot Actuator to monitor the status of the application and its connections (e.g., RabbitMQ).

## 🏗️ Architecture Overview

The service follows a simple, decoupled, event-driven architecture.

1.  **Producer**: An external service (or a manual user via the RabbitMQ UI) publishes a JSON message to the `notifications.exchange`.
2.  **RabbitMQ**: The exchange routes the message to the `notifications.queue`.
3.  **Consumer (`NotificationConsumer`)**: The Spring Boot application listens to the queue. Upon receiving a message, it adds a `correlationId` to the logging context (MDC).
4.  **Dispatcher (`NotificationDispatcher`)**: The consumer passes the request to the dispatcher. The dispatcher inspects the `targets` field and, for each target, finds the appropriate `NotificationService` implementation.
5.  **Service (`NotificationService` implementations)**: The specific service (e.g., `TelegramNotificationService`) handles the logic of sending the notification.
    - If sending fails, Spring Retry attempts to resend it.
    - If failures are persistent, a **Circuit Breaker** will open, causing subsequent requests to fail fast without retrying, preventing system overload.
6.  **Failure Handling**:
    - The `NotificationConsumer` catches the exception and tells RabbitMQ to reject the message without requeueing it.
    - RabbitMQ then routes the "poison" message to the Dead Letter Exchange (`notifications.dlx`) and into the Dead Letter Queue (`notifications.dlq`).

## 🛠️ Tech Stack

- **Backend**: Java 21, Spring Boot 3
- **Messaging**: RabbitMQ
- **Notifications**:
    - Telegram: `telegrambots-spring-boot-starter`
    - Email: `spring-boot-starter-mail`
    - SMS: `twilio`
- **Resilience**: `spring-retry`, Spring Cloud Circuit Breaker with `Resilience4j`
- **Build**: Maven
- **Containerization**: Docker, Docker Compose
- **Logging & Monitoring**:
    - Spring Boot Actuator
    - SLF4J with Logback
    - ELK Stack (Elasticsearch, Logstash, Kibana)

## 📂 Project Structure

This section provides an overview of the key files and directories in the project, with a special focus on the non-Java files.

-   **`pom.xml`**: The Maven Project Object Model file. It defines the project's dependencies, plugins, build process, and metadata.
-   **`Dockerfile`**: A multi-stage Dockerfile used to build a lightweight, production-ready container image for the Spring Boot application.
-   **`docker-compose.yml`**: Defines the local development environment. It starts RabbitMQ and the ELK stack with settings suitable for development (e.g., exposed ports, disabled security).
-   **`docker-compose.prod.yml`**: Defines the production environment. It builds the application from the `Dockerfile` and runs all services with security best practices (e.g., no exposed ports, secrets from `.env` file, health checks).
-   **`.env.example`**: A template file for environment variables required by the production Docker Compose setup. You should copy this to `.env` and fill it with your production secrets.
-   **`.gitignore`**: Specifies intentionally untracked files to be ignored by Git (e.g., IDE files, build artifacts, local configuration).
-   **`LICENSE`**: Contains the full text of the Creative Commons Attribution-NonCommercial 4.0 International license under which this project is shared.
-   **`README.md`**: This file, providing documentation for the project.
-   **`mvnw` & `mvnw.cmd`**: The Maven Wrapper scripts. They allow you to build the project using a specific version of Maven without having to install it system-wide.
-   **`.mvn/wrapper/maven-wrapper.properties`**: Configuration for the Maven wrapper, specifying the Maven version to use.
-   **`/src/main/resources`**:
    -   **`application.yml`**: The main configuration file for Spring Boot. It contains default settings for the application, RabbitMQ, and external services in YAML format.
    -   **`logback-spring.xml`**: The configuration file for Logback, which controls the logging behavior, including structured JSON logging and appenders for both console and Logstash.
-   **`/logstash`**:
    -   **`pipeline/logstash.conf`**: The Logstash pipeline configuration for the development environment. It receives logs from the app and forwards them to Elasticsearch and the console.
    -   **`pipeline-prod/logstash.conf`**: The Logstash pipeline for the production environment, configured to use credentials for a secure Elasticsearch instance.

## 🚀 Getting Started

### Prerequisites

- Java 21 (or later)
- Apache Maven 3.8+
- Docker and Docker Compose

### Configuration

1.  Clone the repository:
    ```bash
    git clone <your-repository-url>
    cd telegram-notifer
    ```
2.  Copy `src/main/resources/application.yml` to a new file named `src/main/resources/application-local.yml` to avoid committing secrets. This new file is already in `.gitignore`.
3.  Update `application-local.yml` with your credentials:
    - `telegram.bot.token`: Your token from Telegram's BotFather.
    - `spring.mail.*`: Your SMTP server details.
    - `twilio.account-sid`, `twilio.auth-token`, `twilio.from-number`: Your Twilio credentials.

### Running the Project

1.  **Start the infrastructure**:
    This command will start RabbitMQ, Elasticsearch, Logstash, and Kibana.
    ```bash
    docker-compose up -d
    ```

2.  **Run the Spring Boot application**:
    You can run the application from your IDE or using the Maven wrapper:
    To use your local configuration, you must activate the `local` profile.

    **On Windows (in CMD or PowerShell):**
    ```shell
    mvnw spring-boot:run -Dspring-boot.run.profiles=local
    ```

    **On Linux or macOS:**
    ```bash
    ./mvnw spring-boot:run -Dspring-boot.run.profiles=local
    ```

    > **Note:** If your shell has trouble with the command above, you can try this more robust alternative which works on all platforms:
    > ```shell
    > mvnw spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=local"
    > ```

The application will now be running and connected to the services in Docker.

## 💡 Usage

### Sending a Notification

You can send a notification request by publishing a message to the `notifications.queue` in RabbitMQ.

1.  Navigate to the RabbitMQ Management UI: `http://localhost:15672`
2.  Go to the **Queues** tab and click on `notifications.queue`.
3.  Open the **Publish message** section.
4.  Set the **Routing key** to `notifications.routing-key`.
5.  Use a JSON payload like the one below. This example will send a notification to both an email address and a Telegram chat.

```json
{
  "body": "Hello from the *Notifier Service*! This is a multi-channel alert.",
  "recipients": {
    "EMAIL": "your-email@example.com",
    "TELEGRAM": "YOUR_TELEGRAM_CHAT_ID"
  },
  "originator": "BillingSystem",
  "timestamp": "2024-08-01T12:00:00Z",
  "targets": ["EMAIL", "TELEGRAM"],
  "metadata": {
    "correlationId": "billing-run-abc-123",
    "invoiceId": "INV-98765"
  }
}
```

6.  Click **Publish message**.

## 🩺 API Endpoints (Actuator)

The application exposes several useful Actuator endpoints:

- **Health Check**: `http://localhost:8080/actuator/health`
  - Provides the health status of the application and its connections to RabbitMQ and the disk.
- **Application Info**: `http://localhost:8080/actuator/info`
  - Shows custom application information, including build details and a list of currently active notification services.

## 🧰 DLQ Management API

The application provides a REST API to manage messages in the Dead Letter Queue (DLQ). This is useful for manually inspecting and reprocessing failed notifications.

-   **`GET /api/dlq/count`**
    -   Returns the current number of messages in the DLQ.

-   **`POST /api/dlq/reprocess-one`**
    -   Takes a single message from the DLQ and sends it back to the main queue for another processing attempt.

-   **`POST /api/dlq/reprocess-all`**
    -   Takes all messages currently in the DLQ and sends them back to the main queue for processing.

-   **`DELETE /api/dlq/purge`**
    -   Permanently deletes all messages from the DLQ. Use with caution.

## 📝 Logging with ELK Stack

This project is configured for centralized logging.

1.  **Enable the Logstash Appender**: In `src/main/resources/logback-spring.xml`, uncomment the line `<appender-ref ref="LOGSTASH"/>`.
2.  **Restart** the Spring Boot application.
3.  **View Logs in Kibana**:
    - Navigate to Kibana: `http://localhost:5601`
    - Create a **Data View** with the index pattern `telegram-notifier-*`.
    - Go to the **Discover** tab to see your structured logs. You can filter by `correlationId` to trace a single request.

## 🤝 Contributing

Contributions are welcome! Please follow these steps to contribute:

1.  **Fork** the repository.
2.  **Create a new branch** for your feature or bug fix:
    ```bash
    git checkout -b feature/my-awesome-feature
    ```
3.  **Make your changes**. Ensure your code follows the existing style and that all tests pass.
4.  **Commit your changes** with a clear and descriptive commit message:
    ```bash
    git commit -m "feat: Add support for MyAwesomeFeature"
    ```
5.  **Push** your changes to your forked repository:
    ```bash
    git push origin feature/my-awesome-feature
    ```
6.  **Open a Pull Request** to the `main` branch of the original repository.

## 📄 License

This project is licensed under the Creative Commons Attribution-NonCommercial 4.0 International License. See the `LICENSE` file for details.