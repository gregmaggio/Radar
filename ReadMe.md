Running locally on docker

mvn clean install
docker build -t radar .
docker run -p 80:8080 radar

Open: http://localhost


Deploying image to GCP

gcloud auth login
gcloud auth configure-docker
gcloud config set project api-project-378578942759

docker build . --tag us.gcr.io/api-project-378578942759/radar:latest
docker push us.gcr.io/api-project-378578942759/radar:latest

gcloud run deploy radar --image us.gcr.io/api-project-378578942759/radar:latest
