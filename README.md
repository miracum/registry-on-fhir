# Registry on FHIR

A clinical trial registry based on FHIR.

## Components

- [deploy](/deploy/README.md) contains Kubernetes manifests and instructions for deploying the registry on Kubernetes.
- [web](/web/README.md) the web app displaying the studies from the FHIR server
- [multisitemerger](/multisitemerger/README.md) a service used to merge multiple FHIR ResearchStudy resources that represent the
  same study into a single "master" study.
