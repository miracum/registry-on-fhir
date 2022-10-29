# Registry on FHIR

A clinical trial registry based on FHIR.

## Components

- [k8s](k8s/README.md) contains Kubernetes manifests and instructions for deploying the registry on Kubernetes.
- [web](web/README.md) the web app displaying the studies from the FHIR server
- [multi-site-merger](multi-site-merger/README.md) a service used to merge multiple FHIR ResearchStudy resources that represent the
  same study into a single "master" study.

## Citation

```console
Gulden C, Blasini R, Nassirian A, Stein A, Altun F, Kirchner M, Prokosch H, Boeker M
Prototypical Clinical Trial Registry Based on Fast Healthcare Interoperability Resources (FHIR): Design and Implementation Study
JMIR Med Inform 2021;9(1):e20470
URL: https://medinform.jmir.org/2021/1/e20470
DOI: 10.2196/20470
```
