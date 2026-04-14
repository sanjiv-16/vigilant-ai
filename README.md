# Vigilant AI 🛡️

Vigilant AI is an advanced, fully containerized Automated Code Review system powered by Spring Boot and Spring AI. It acts as an expert backend engineer, evaluating codebases for security vulnerabilities, logical bugs, and architectural anti-patterns. 

Unlike traditional linters or standard LLM wrappers, Vigilant AI utilizes a highly optimized Dual-Engine Architecture, combining lightning-fast static analysis with the deep reasoning capabilities of cloud-based Large Language Models (LLMs), all while managing RAG (Retrieval-Augmented Generation) memory entirely on-premise.

## ✨ Uniqueness & Key Features

* **Dual-Engine Review System:** * **Static Bouncer:** A customizable regex-based static engine (`rules.json`) that catches fatal errors (e.g., hardcoded AWS keys, string pooling issues) in milliseconds, saving API costs and processing time.
  * **AI Brain:** Integrates with Groq's high-speed inference engine (Llama 3.1) to analyze cross-file boundaries, deep logical bugs, and architectural optimizations.
* **100% Free Local Embeddings:** Bypasses paid embedding APIs by running the `all-MiniLM-L6-v2` ML model directly inside the Java container using ONNX and Spring AI Transformers.
* **RAG-Ready Vector Database:** Powered by PostgreSQL and the `pgvector` extension to give the AI long-term memory of past code reviews and false positives.
* **Multi-Format Support:** Accepts simple JSON payloads for single-file checks or full `.zip` project uploads for holistic repository reviews.
* **Production-Ready Containerization:** Fully Dockerized using an Ubuntu (`jammy`) base image to seamlessly support native C++ machine learning libraries off-heap.

---

## ⚙️ What Needs to be Adjusted (Configuration)

Before building the project, you must configure your environment variables and review rules.

### 1. Environment Variables
Create a `.env` file in the root directory of the project (next to `docker-compose.yml`) and add the following keys:

    OPENAI_API_KEY=gsk_your_api_key_here
    AI_MODEL=llama-3.1-8b-instant
    AI_TEMPERATURE=0.2
    POSTGRES_DB=vigilant_db
    POSTGRES_USER=admin
    POSTGRES_PASSWORD=admin

### 2. Custom Static Rules (Optional)
You can define your own static analysis rules by editing `src/main/resources/rules.json`. Each rule requires an ID, regex pattern, severity, and a boolean `fatal` flag. If a codebase violates a `fatal=true` rule, the review is instantly rejected with an 'F' grade without calling the LLM.

---

## 🚀 Steps to Build and Run

### Prerequisites
* Docker and Docker Compose installed on your machine.

### Build Instructions
1. Clone the repository and navigate into the project folder.
2. Ensure your `.env` file is created and populated.
3. Run the following command to build the Java application and start the containers:

    docker-compose up --build -d

Note: On the very first startup, the application will take approximately 15-30 seconds to download the native ONNX `libtokenizers` and the `all-MiniLM-L6-v2` machine learning model into the container's memory. You can monitor the startup process by running: `docker logs -f vigilant-ai-api`. Wait until you see `Started VigilantAiApplication`.

---

## 💡 How to Use

The API runs on `http://localhost:8080`. You can interact with it using the following endpoints:

### 1. Review a ZIP Archive (Recommended)
Upload a `.zip` file containing your Java project. The system will extract the files, run static analysis, and feed the context to the AI.

    curl -X POST http://localhost:8080/api/v1/review/upload \
      -F "file=@your-project-source.zip"

### 2. Review a JSON Payload
Send a JSON object containing the code you want reviewed. Useful for CI/CD pipeline integrations. Create a `request.json` file:

    {
      "files": [
        {
          "fileName": "src/main/java/com/example/UserService.java",
          "content": "package com.example;\n\npublic class UserService {\n    public void doWork() {\n        System.out.println(\"Starting work...\");\n    }\n}"
        }
      ]
    }

Send the request:

    curl -X POST http://localhost:8080/api/v1/review \
      -H "Content-Type: application/json" \
      -d @request.json

### Example AI Output

    {
        "finalScore": 85,
        "grade": "B",
        "staticViolations": [
            {
                "fileName": "src/main/java/com/example/UserService.java",
                "ruleId": "SONAR-106",
                "message": "Standard outputs should not be used directly to log anything.",
                "severity": "LOW",
                "isFatal": false
            }
        ],
        "aiInsights": {
            "bugs": [],
            "securityIssues": [],
            "performanceTips": ["Avoid using System.out.println for logging. Use SLF4J."],
            "overallSummary": "The code is structurally sound but lacks enterprise-grade logging practices.",
            "qualityScore": 90
        }
    }

---

## 🛠️ Built With
* Java 21 & Spring Boot 3.3.x
* Spring AI (OpenAI & Transformers Integration)
* Groq API (Llama 3.1)
* PostgreSQL & pgvector
* Docker