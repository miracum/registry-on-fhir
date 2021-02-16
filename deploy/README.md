# FHIR Trials Registry Deployment Config

## Deployment

1. Create a Basic Auth secret to protect the FHIR API

   see <https://kubernetes.github.io/ingress-nginx/examples/auth/basic/>, name the secret `fhir-server-basic-auth`.

1. Install a FHIR Server as a backend for the studies

   ```sh
   helm repo add miracum https://harbor.miracum.org/chartrepo/charts
   helm repo update
   helm install -n miracum-registry -f helm/fhir-server-values.yaml hapi-fhir-server miracum/hapi-fhir-server
   ```

1. Deploy the multi-site study merger and the web app

   ```sh
   kustomize build kustomize/overlays/prod/ | kubectl apply -n miracum-registry -f -
   ```
