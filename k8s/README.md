# FHIR Trials Registry Deployment Config

## Deployment

1. Create a Basic Auth secret to protect the FHIR API

   see <https://kubernetes.github.io/ingress-nginx/examples/auth/basic/>, name the secret `fhir-server-basic-auth`.

1. Install a FHIR Server as a backend for the studies

   ```sh
   helm repo add hapifhir https://hapifhir.github.io/hapi-fhir-jpaserver-starter/
   helm upgrade --install \
      -f helm/fhir-server-staging-values.yaml \
      hapi-fhir-jpaserver hapifhir/hapi-fhir-jpaserver
   ```

1. Deploy the multi-site study merger and the web app

   ```sh
   kustomize build kustomize/overlays/prod/ | kubectl apply -n miracum-registry -f -
   ```
