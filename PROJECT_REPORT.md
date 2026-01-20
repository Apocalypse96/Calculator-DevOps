# Production-Grade CI/CD Pipeline for Spring Boot Application

## Project Report

---

## Table of Contents
1. [Problem Background & Motivation](#1-problem-background--motivation)
2. [Application Overview](#2-application-overview)
3. [CI/CD Architecture Diagram](#3-cicd-architecture-diagram)
4. [CI/CD Pipeline Design & Stages](#4-cicd-pipeline-design--stages)
5. [Security & Quality Controls](#5-security--quality-controls)
6. [Results & Observations](#6-results--observations)
7. [Limitations & Improvements](#7-limitations--improvements)

---

## 1. Problem Background & Motivation

### 1.1 The Challenge
Modern software development demands rapid, reliable, and secure delivery of applications. Traditional manual deployment processes suffer from:
- **Human Error**: Manual steps lead to configuration drift and deployment failures
- **Security Gaps**: Late-stage security testing allows vulnerabilities to reach production
- **Slow Feedback**: Developers wait hours or days to learn about issues
- **Inconsistent Environments**: "Works on my machine" syndrome

### 1.2 Motivation
This project implements a **shift-left security** approach with a production-grade CI/CD pipeline that:
- Automates the entire build-test-deploy lifecycle
- Integrates security scanning at every stage (SAST, SCA, Container Scanning)
- Provides fast feedback loops (fail early, fail fast)
- Ensures consistent, reproducible deployments
- Follows DevSecOps best practices

### 1.3 Goals
| Goal | Description |
|------|-------------|
| Automation | Zero manual intervention from code commit to deployment |
| Security | Detect vulnerabilities before they reach production |
| Quality | Enforce code standards and comprehensive testing |
| Speed | Parallel execution where possible |
| Reliability | Consistent deployments across environments |

---

## 2. Application Overview

### 2.1 Technology Stack
| Component | Technology | Version |
|-----------|------------|---------|
| Language | Java | 17 (LTS) |
| Framework | Spring Boot | 3.2.1 |
| Build Tool | Maven | 3.9.x |
| Container | Docker | Multi-stage |
| Orchestration | Kubernetes | 1.29+ |
| CI/CD | GitHub Actions | Latest |

### 2.2 Application Architecture
The Calculator REST API is a Spring Boot microservice providing:

```
┌─────────────────────────────────────────────────────────────┐
│                    Calculator REST API                       │
├─────────────────────────────────────────────────────────────┤
│  Endpoints:                                                  │
│  ├── GET  /health          → Health status check            │
│  ├── POST /calculate       → Arithmetic operations          │
│  └── GET  /actuator/*      → Spring Boot Actuator metrics   │
├─────────────────────────────────────────────────────────────┤
│  Layers:                                                     │
│  ├── Controller Layer      → REST endpoint handling         │
│  ├── Service Layer         → Business logic (add, multiply) │
│  ├── DTO Layer            → Request/Response objects         │
│  └── Exception Handler    → Global error handling            │
└─────────────────────────────────────────────────────────────┘
```

### 2.3 API Endpoints

| Endpoint | Method | Description | Request Body | Response |
|----------|--------|-------------|--------------|----------|
| `/health` | GET | Health check | None | `{"status": "UP"}` |
| `/calculate` | POST | Perform calculations | `{"operand1": 10, "operand2": 5}` | `{"sum": 15, "product": 50}` |
| `/actuator/health` | GET | Detailed health | None | Liveness/Readiness status |

### 2.4 Project Structure
```
spring-calculator/
├── .github/workflows/
│   ├── ci.yml                 # CI Pipeline (12 stages)
│   └── cd.yml                 # CD Pipeline (manual trigger)
├── src/
│   ├── main/java/com/example/calculator/
│   │   ├── CalculatorApplication.java
│   │   ├── controller/CalculatorController.java
│   │   ├── service/CalculatorService.java
│   │   ├── service/CalculatorServiceImpl.java
│   │   ├── dto/CalculationRequest.java
│   │   ├── dto/CalculationResponse.java
│   │   └── exception/GlobalExceptionHandler.java
│   └── test/java/...          # Unit & Integration tests
├── k8s/
│   ├── namespace.yaml
│   ├── configmap.yaml
│   ├── deployment.yaml
│   └── service.yaml
├── config/
│   ├── checkstyle/checkstyle.xml
│   └── owasp/suppressions.xml
├── Dockerfile
└── pom.xml
```

---

## 3. CI/CD Architecture Diagram

### 3.1 High-Level Architecture

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                           DEVELOPER WORKFLOW                                 │
└─────────────────────────────────────────────────────────────────────────────┘
        │
        │ git push
        ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                              CI PIPELINE                                     │
│  ┌──────────┐    ┌──────────┐    ┌──────────┐                              │
│  │ Checkout │───▶│  Setup   │───▶│  Cache   │                              │
│  │   (1)    │    │ Java (2) │    │   (3)    │                              │
│  └──────────┘    └──────────┘    └──────────┘                              │
│        │                               │                                     │
│        └───────────────┬───────────────┘                                     │
│                        │                                                     │
│        ┌───────────────┼───────────────┐         PARALLEL SECURITY          │
│        ▼               ▼               ▼              SCANS                  │
│  ┌───────────┐  ┌───────────┐  ┌───────────┐                               │
│  │   Lint    │  │   SAST    │  │    SCA    │                               │
│  │Checkstyle │  │  CodeQL   │  │   OWASP   │                               │
│  │   (4)     │  │    (5)    │  │    (6)    │                               │
│  └───────────┘  └───────────┘  └───────────┘                               │
│        │               │               │                                     │
│        └───────────────┼───────────────┘                                     │
│                        ▼                                                     │
│                ┌───────────────┐                                            │
│                │  Unit Tests   │                                            │
│                │     (7)       │                                            │
│                └───────────────┘                                            │
│                        │                                                     │
│                        ▼                                                     │
│                ┌───────────────┐                                            │
│                │  Build JAR    │                                            │
│                │     (8)       │                                            │
│                └───────────────┘                                            │
│                        │                                                     │
│                        ▼                                                     │
│                ┌───────────────┐                                            │
│                │ Docker Build  │                                            │
│                │     (9)       │                                            │
│                └───────────────┘                                            │
│                        │                                                     │
│                        ▼                                                     │
│                ┌───────────────┐                                            │
│                │Container Scan │                                            │
│                │  Trivy (10)   │                                            │
│                └───────────────┘                                            │
│                        │                                                     │
│                        ▼                                                     │
│                ┌───────────────┐                                            │
│                │ Runtime Test  │                                            │
│                │    (11)       │                                            │
│                └───────────────┘                                            │
│                        │                                                     │
│                        ▼                                                     │
│                ┌───────────────┐                                            │
│                │ DockerHub     │                                            │
│                │ Push (12)     │                                            │
│                └───────────────┘                                            │
└─────────────────────────────────────────────────────────────────────────────┘
                         │
                         │ Image Published
                         ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                              CD PIPELINE                                     │
│                        (Manual Trigger Only)                                 │
│                                                                              │
│  ┌────────────────┐    ┌────────────────┐    ┌────────────────┐            │
│  │ Manual Trigger │───▶│   Validate     │───▶│     DAST       │            │
│  │ workflow_      │    │ Image + YAML   │    │  (Simulated)   │            │
│  │ dispatch       │    │                │    │                │            │
│  └────────────────┘    └────────────────┘    └────────────────┘            │
│                                                      │                       │
│                                                      ▼                       │
│                        ┌────────────────┐    ┌────────────────┐            │
│                        │   Summary &    │◀───│   Deploy to    │            │
│                        │   Reporting    │    │  Kubernetes    │            │
│                        └────────────────┘    └────────────────┘            │
└─────────────────────────────────────────────────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                          KUBERNETES CLUSTER                                  │
│  ┌─────────────────────────────────────────────────────────────────────┐   │
│  │                    Namespace: calculator                             │   │
│  │  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐                 │   │
│  │  │  ConfigMap  │  │ Deployment  │  │   Service   │                 │   │
│  │  │             │  │  (2 pods)   │  │  ClusterIP  │                 │   │
│  │  └─────────────┘  └─────────────┘  └─────────────┘                 │   │
│  └─────────────────────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────────────────────┘
```

### 3.2 Data Flow

```
Developer → GitHub → CI Pipeline → DockerHub → CD Pipeline → Kubernetes
    │          │          │            │            │            │
    │          │          │            │            │            │
 Commit    Trigger    Build &      Store      Deploy       Run
  Code     Actions    Validate     Image     Manifests   Application
```

---

## 4. CI/CD Pipeline Design & Stages

### 4.1 CI Pipeline - 12 Stages

| Stage | Name | Tool | Purpose | Failure Action |
|-------|------|------|---------|----------------|
| 1 | Checkout | actions/checkout@v4 | Retrieve source code with full git history | Pipeline stops |
| 2 | Setup Runtime | actions/setup-java@v4 | Configure Java 17 (Temurin) environment | Pipeline stops |
| 3 | Cache Dependencies | Maven cache | Cache ~/.m2 for faster builds | Continue (slower) |
| 4 | Lint | Checkstyle | Enforce Google Java Style code standards | Pipeline stops |
| 5 | SAST | CodeQL | Static Application Security Testing | Pipeline stops |
| 6 | SCA | OWASP Dependency-Check | Scan dependencies for CVEs (CVSS ≥ 7) | Warning |
| 7 | Unit Tests | JUnit 5 / Surefire | Validate business logic | Pipeline stops |
| 8 | Build JAR | Maven package | Create executable Spring Boot JAR | Pipeline stops |
| 9 | Docker Build | docker/build-push-action | Build multi-stage container image | Pipeline stops |
| 10 | Container Scan | Trivy | Scan image for OS/library vulnerabilities | Warning on CRITICAL |
| 11 | Runtime Test | curl / health check | Verify container starts and responds | Pipeline stops |
| 12 | Push to Registry | docker/login-action | Publish validated image to DockerHub | Pipeline stops |

### 4.2 Stage Details

#### Stage 4: Linting (Checkstyle)
```yaml
- name: Run Checkstyle
  run: ./mvnw checkstyle:check -Dcheckstyle.config.location=config/checkstyle/checkstyle.xml
```
**Rules Enforced:**
- Line length ≤ 120 characters
- Method length ≤ 50 lines
- No star imports
- Proper naming conventions
- Whitespace and formatting

#### Stage 5: SAST (CodeQL)
```yaml
- name: Initialize CodeQL
  uses: github/codeql-action/init@v3
  with:
    languages: java
    queries: +security-extended,security-and-quality
```
**Vulnerabilities Detected:**
- SQL Injection
- Cross-Site Scripting (XSS)
- Path Traversal
- Insecure Deserialization
- Hardcoded Credentials

#### Stage 6: SCA (OWASP Dependency-Check)
```yaml
- name: Run OWASP Dependency Check
  run: ./mvnw dependency-check:check -DfailBuildOnCVSS=7
```
**Threshold:** Fails build on CVSS score ≥ 7.0 (HIGH/CRITICAL)

#### Stage 10: Container Scanning (Trivy)
```yaml
- name: Run Trivy vulnerability scanner
  uses: aquasecurity/trivy-action@master
  with:
    image-ref: ${{ env.DOCKER_IMAGE }}:${{ github.sha }}
    severity: 'CRITICAL'
    format: 'sarif'
```
**Scans For:**
- OS package vulnerabilities
- Application library vulnerabilities
- Misconfigurations

### 4.3 CD Pipeline

| Stage | Purpose | Trigger |
|-------|---------|---------|
| Validate | Verify Docker image exists in registry | Manual |
| DAST | Dynamic security testing (placeholder) | Manual |
| Deploy | Apply Kubernetes manifests | Manual |
| Summary | Generate deployment report | Automatic |

**Manual Trigger Inputs:**
- `image_tag`: Docker image tag to deploy (SHA or "latest")
- `environment`: Target environment (staging/production)
- `run_dast`: Enable/disable DAST scan

---

## 5. Security & Quality Controls

### 5.1 Shift-Left Security Model

```
Traditional:    Code → Build → Test → Deploy → [Security Scan] → Production
                                                     ↑
                                              Late Detection!

Shift-Left:     Code → [SAST+SCA] → Build → [Container Scan] → [Runtime Test] → Deploy
                         ↑                        ↑                   ↑
                   Early Detection!         Pre-Push Scan      Health Verify
```

### 5.2 Security Gates

| Gate | Tool | Threshold | Stage |
|------|------|-----------|-------|
| Code Quality | Checkstyle | Zero violations | 4 |
| Static Security | CodeQL | No HIGH/CRITICAL | 5 |
| Dependency Vulnerabilities | OWASP | CVSS < 7.0 | 6 |
| Container Vulnerabilities | Trivy | No CRITICAL | 10 |
| Runtime Health | curl | HTTP 200 + UP status | 11 |

### 5.3 Container Security (Dockerfile)

```dockerfile
# Multi-stage build - removes build tools from production
FROM eclipse-temurin:17-jdk-jammy AS builder
# ... build stage ...

FROM eclipse-temurin:17-jre-jammy AS runtime

# Non-root user (UID 1001)
RUN groupadd -g 1001 appgroup && \
    useradd -u 1001 -g appgroup -s /bin/bash -m appuser
USER appuser

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=30s --retries=3 \
    CMD curl -sf http://localhost:8080/actuator/health || exit 1
```

### 5.4 Kubernetes Security Context

```yaml
securityContext:
  runAsNonRoot: true
  runAsUser: 1001
  allowPrivilegeEscalation: false
  readOnlyRootFilesystem: true
  capabilities:
    drop:
      - ALL
```

### 5.5 Quality Metrics

| Metric | Target | Tool |
|--------|--------|------|
| Code Coverage | Tracked | JaCoCo (future) |
| Test Pass Rate | 100% | JUnit 5 |
| Lint Violations | 0 | Checkstyle |
| Security Findings | 0 HIGH/CRITICAL | CodeQL + Trivy |
| Dependency CVEs | 0 (CVSS ≥ 7) | OWASP |

---

## 6. Results & Observations

### 6.1 Pipeline Execution Results

| Stage | Status | Duration (avg) |
|-------|--------|----------------|
| Setup & Cache | ✅ Pass | ~45s |
| Lint (Checkstyle) | ✅ Pass | ~15s |
| SAST (CodeQL) | ✅ Pass | ~2m |
| SCA (OWASP) | ✅ Pass | ~1m |
| Unit Tests | ✅ Pass (19 tests) | ~30s |
| Build JAR | ✅ Pass | ~20s |
| Docker Build | ✅ Pass | ~1m |
| Container Scan | ✅ Pass | ~30s |
| Runtime Test | ✅ Pass | ~45s |
| DockerHub Push | ✅ Pass | ~20s |
| **Total CI Time** | | **~7-8 minutes** |

### 6.2 Test Results

```
Tests run: 19, Failures: 0, Errors: 0, Skipped: 0

Test Classes:
- CalculatorServiceTest: 8 tests (add, multiply operations)
- CalculatorControllerTest: 11 tests (endpoint validation, error handling)
```

### 6.3 Security Scan Results

**CodeQL (SAST):**
- Queries executed: security-extended, security-and-quality
- Findings: 0 HIGH, 0 CRITICAL
- Status: PASSED

**OWASP Dependency-Check (SCA):**
- Dependencies scanned: 45+
- Vulnerable dependencies: 0 (CVSS ≥ 7)
- Status: PASSED

**Trivy (Container Scan):**
- Base image: eclipse-temurin:17-jre-jammy
- CRITICAL vulnerabilities: 0
- Status: PASSED

### 6.4 Docker Image Metrics

| Metric | Value |
|--------|-------|
| Base Image | eclipse-temurin:17-jre-jammy |
| Final Image Size | ~453 MB |
| Layers | Optimized via multi-stage |
| User | Non-root (UID 1001) |
| Health Check | Enabled |

### 6.5 Local Kubernetes Deployment (Minikube)

```
$ kubectl get all -n calculator

NAME                              READY   STATUS    RESTARTS   AGE
pod/calculator-865bdd6c58-kc99c   1/1     Running   0          103s

NAME                 TYPE        CLUSTER-IP      PORT(S)   AGE
service/calculator   ClusterIP   10.103.72.218   80/TCP    103s

NAME                         READY   UP-TO-DATE   AVAILABLE   AGE
deployment.apps/calculator   1/1     1            1           103s
```

**Endpoint Tests:**
```bash
$ curl http://localhost:8080/health
{"status":"UP"}

$ curl -X POST http://localhost:8080/calculate \
    -H "Content-Type: application/json" \
    -d '{"operand1": 10, "operand2": 5}'
{"operand1":10.0,"operand2":5.0,"sum":15.0,"product":50.0}
```

---

## 7. Limitations & Improvements

### 7.1 Current Limitations

| Limitation | Impact | Reason |
|------------|--------|--------|
| No real K8s cluster in CD | Dry-run only | GitHub Actions cannot reach local Minikube |
| DAST is simulated | No real security testing | Requires deployed staging environment |
| No code coverage enforcement | Quality gap | JaCoCo not configured |
| NVD API rate limiting | SCA may fail | Free tier limitations |
| Single-region deployment | No HA | Demo scope |

### 7.2 Future Improvements

#### Short-term Improvements
1. **Add JaCoCo Code Coverage**
   ```xml
   <plugin>
     <groupId>org.jacoco</groupId>
     <artifactId>jacoco-maven-plugin</artifactId>
     <configuration>
       <rules>
         <rule>
           <limits>
             <limit>
               <minimum>80%</minimum>
             </limit>
           </limits>
         </rule>
       </rules>
     </configuration>
   </plugin>
   ```

2. **Implement Real DAST**
   - Deploy to staging environment
   - Integrate OWASP ZAP or Nuclei
   - Fail pipeline on HIGH/CRITICAL findings

3. **Add Integration Tests**
   - Testcontainers for database testing
   - Contract testing with Pact

#### Medium-term Improvements
1. **Multi-Environment CD**
   - Configure EKS/GKE/AKS cluster
   - Implement GitOps with ArgoCD
   - Add environment promotion (dev → staging → prod)

2. **Enhanced Monitoring**
   - Prometheus metrics scraping
   - Grafana dashboards
   - Alertmanager for notifications

3. **Secret Management**
   - HashiCorp Vault integration
   - External Secrets Operator
   - Sealed Secrets for GitOps

#### Long-term Improvements
1. **Multi-Cluster Deployment**
   - Blue-green deployments
   - Canary releases
   - Geographic distribution

2. **Compliance as Code**
   - OPA/Gatekeeper policies
   - Kyverno admission controller
   - Compliance scanning (SOC2, HIPAA)

3. **Cost Optimization**
   - Spot instances for CI runners
   - Image layer caching
   - Artifact retention policies

### 7.3 Recommended Architecture Evolution

```
Current State:                    Future State:
┌─────────────┐                  ┌─────────────┐
│   GitHub    │                  │   GitHub    │
│   Actions   │                  │   Actions   │
└─────────────┘                  └─────────────┘
       │                                │
       ▼                                ▼
┌─────────────┐                  ┌─────────────┐
│  DockerHub  │                  │    ECR/     │
│             │                  │   GCR/ACR   │
└─────────────┘                  └─────────────┘
       │                                │
       ▼                                ▼
┌─────────────┐                  ┌─────────────┐
│  Minikube   │                  │   ArgoCD    │
│  (Local)    │                  │  (GitOps)   │
└─────────────┘                  └─────────────┘
                                        │
                                        ▼
                                 ┌─────────────┐
                                 │  EKS/GKE/   │
                                 │    AKS      │
                                 └─────────────┘
```

---

## Appendix A: Quick Reference

### Commands
```bash
# Build locally
./mvnw clean package

# Run tests
./mvnw test

# Build Docker image
docker build -t spring-calculator:local .

# Run container
docker run -p 8080:8080 spring-calculator:local

# Deploy to Minikube
kubectl apply -f k8s/namespace.yaml
kubectl apply -f k8s/configmap.yaml
kubectl apply -f k8s/deployment-local.yaml
kubectl apply -f k8s/service.yaml

# Test endpoints
curl http://localhost:8080/health
curl -X POST http://localhost:8080/calculate \
  -H "Content-Type: application/json" \
  -d '{"operand1": 10, "operand2": 5}'
```

### Required Secrets
| Secret | Purpose |
|--------|---------|
| DOCKERHUB_USERNAME | Docker registry authentication |
| DOCKERHUB_TOKEN | Docker registry access token |
| NVD_API_KEY | OWASP NVD API (optional) |

---

**Author:** DevOps Engineering Team
**Date:** January 2026
**Version:** 1.0
