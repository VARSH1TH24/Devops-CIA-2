# End-to-End Cloud CI/CD Pipeline: Java + Maven + Docker + Kubernetes (AWS)

Push to `main` -> GitHub Actions builds and tests with Maven -> builds a Docker image ->
pushes it to Amazon ECR -> Ansible deploys it to Amazon EKS -> the pipeline verifies the rollout and the public endpoint.

```
Developer --git push--> GitHub --> GitHub Actions
                                     1 Checkout  2 mvn verify (compile+test+package)
                                     3 docker build/tag  4 push to ECR
                                     5 ansible-playbook -> EKS  6 rollout + curl verification
                                                              |
                         Amazon ECR <--image pull--  EKS (2 pods) <-- LoadBalancer <-- Browser
```

## Repository layout
| Path | Purpose |
|---|---|
| `src/`, `pom.xml` | Spring Boot 3 / Java 17 app and unit tests (JUnit 5, MockMvc) |
| `Dockerfile`, `.dockerignore` | Multi-stage build, non-root runtime image |
| `.github/workflows/ci-cd.yml` | CI/CD pipeline |
| `k8s/` | Namespace, Deployment (Jinja template rendered by Ansible), LoadBalancer Service |
| `ansible/` | `inventory.ini`, `deploy.yml` (deployment automation) |
| `scripts/setup-aws.sh` | One-time ECR + EKS creation |

## Run locally
```bash
mvn clean verify
java -jar target/app.jar          # http://localhost:8080
docker build -t cicd-java-app . && docker run -p 8080:8080 cicd-java-app
```

## One-time cloud setup
1. Install `aws` CLI, `eksctl`, `kubectl`; run `aws configure` with an IAM user that can create EKS/ECR resources.
2. `./scripts/setup-aws.sh` (about 15-20 minutes).
3. In GitHub: Settings > Secrets and variables > Actions, add
   `AWS_ACCESS_KEY_ID`, `AWS_SECRET_ACCESS_KEY`, `AWS_REGION`, `EKS_CLUSTER_NAME`.
   Use the same IAM user that created the cluster (it is cluster admin), or grant yours access via an EKS access entry.
4. Push to `main`. The first run creates the namespace, deployment and service.

## Change and redeployment demo
1. Edit `MESSAGE` in `HomeController.java` to `"Hello from the CI/CD pipeline - version 2"`.
2. Update the test needs no change (it reads the constant).
3. `git commit -am "Update message to v2" && git push`
4. Watch the Actions run; note the new image tag (commit SHA) in ECR.
5. `kubectl get pods -n cicd-demo -w` shows the rolling update; refresh the browser to see v2 and the new commit SHA.

## Useful verification commands
```bash
kubectl get nodes
kubectl get deploy,pods,svc -n cicd-demo
kubectl rollout history deployment/cicd-java-app -n cicd-demo
kubectl get svc cicd-java-app -n cicd-demo -o jsonpath='{.status.loadBalancer.ingress[0].hostname}'
aws ecr describe-images --repository-name cicd-java-app
```

## Cleanup (avoid charges)
```bash
kubectl delete ns cicd-demo
eksctl delete cluster --name cicd-demo-cluster --region ap-south-1
aws ecr delete-repository --repository-name cicd-java-app --force
```
