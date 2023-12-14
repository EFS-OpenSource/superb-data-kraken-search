# Search Service


This service provides endpoints to enable searching in elasticsearch.

- ```/search/v1.0/index``` Gets available indexes (filtered by given pattern).
- ```/search/v1.0/criteria``` Gets possible filter-criteria within the given index (properties enabled in elasticsearch-mapping), including datatype and
  possible operators.
- ```/search/v1.0/resultproperties``` Gets possible result-properties.
- ```/search/v1.0``` Execute search.

---
**NOTE on Operators**

The following operators are supported:

- ```EQ``` look up for '&lt;value&gt;'
- ```LIKE``` look up for '\*&lt;value&gt;\*'
- ```NOT``` look up for values not matching '&lt;value&gt;'
- ```GT``` look up for values greater than '&lt;value&gt;'
- ```GTE``` look up for values greater than or equal to '&lt;value&gt;'
- ```LT``` look up for values less than '&lt;value&gt;'
- ```LTE``` look up for values less than or equal to '&lt;value&gt;'
- ```BETWEEN``` look up for values between '&lt;lowerBound&gt;' and '&lt;upperBound&gt;' (where the bounds are included)

---
If multiple Filters are provided, same-property-filters are combined by 'OR', varying properties are combined by 'AND'. Example:

```
  "filter": [
    {
      "operator": "EQ",
      "property": "metadata.project.projectId",
      "value": "sdk"
    },
    {
      "operator": "EQ",
      "property": "metadata.project.projectId",
      "value": "sdk2"
    },
    {
      "operator": "EQ",
      "property": "metadata.customer.customerId",
      "value": "efs"
    }
  ],
```

will be interpreted as

```
"metadata.customer.customerId:efs AND metadata.project.projectId:( sdk OR sdk2 )"
```

## Deployment


For deployment push the service to Azure DevOps, the pipeline will start automatically for development and master branches. For feature branches, please start
it manually.
The deployment manifest is [azure-pipeline.yml](azure-pipeline.yml).


### Build/Deployment steps:

- Build and Push: an image is built using the [Dockerfile](Dockerfile) and pushed to the corresponding ACR (SDK or AICloud).
- Deployment: kubernetes manifests are deployed to the corresponding AKS (SDK or AICloud):
    - [config-map.yml](kubernetes/config-map.yml) writes the spring boot configuration application.yml as a config map
    - [rbac.yml](kubernetes/rbac.yml) gives permission for backend namespace
    - [deployment.yml](kubernetes/deployment.yml)  yields the k8 deployment "search", i.e. describes the desired state for Pods and ReplicaSets
    - [service.yml](kubernetes/service.yml) yields the corresponding k8 service "search-service", i.e. an abstract way to expose an application running on a set
      of Pods as a network service.
    - [ingress.yml](kubernetes/ingress.yml) yields the ingress "search" to the service, i.e. manages external http access to the service in the cluster via the
      public IP https://efs-aicloud.westeurope.cloudapp.azure.com/sdk-frontend/

### Service connections


For setting up the pipelines, the following service connections are needed in Azure Devops -> Project Settings:


#### Docker Registry service connection

- for SDK tenant: sc-efs-sdk-acrsdk (type: Azure Container Registry)

- for AICloud tenant: sc-efs-sdk-acraicloud (type: others)
    - docker registry: https://acraicloud.azurecr.io/
    - docker id: acraicloud
    - docker password: obtained from portal -> ACR -> access keys -> enable admin user -> copy password

#### Kubernetes service connection

- for SDK tenant: sc-efs-sdk-aks-sdk_devops
- for AICloud tenant: sc-efs-sdk-aks-aicloud_devops

Both are of type Service Account and have the following parameters

- server url: obtained (as described in Azure DevOps) from
  ```bash
  kubectl config view --minify -o jsonpath={.clusters[0].cluster.server}
  ```
- secret: obtained from
    ```bash
  kubectl get serviceAccounts <service-account-name> -n <namespace> -o=jsonpath={.secrets[*].name}
  ```
  where namespace is default and the service account is e.g. appreg-aicloud-aks-main.

---


### Pipeline Variables


the following pipeline variables are required:

| name                            | example                                    |
|---------------------------------|--------------------------------------------| 
| dockerRegistryServiceConnection | sc-efs-sdk-acraicloud                      |
| kubernetesServiceConnection     | sc-efs-sdk-aks-aicloud_devops              |
| environment                     | aicloud                                    |
| elasticsearch-service           | elasticsearch-opendistro-es-client-service |

The container registry service connection is established during pipeline creation.
---


## TODO


Currently, the documentation is located in usual files like `README.md`, `CHANGELOG.md`, `CONTRIBUTING.md` and `LICENSE.md` inside the root folder of the
repository. That folder is not processed by MkDocs. To build the technical documentation for MkDocs we could follow these steps:

- Move the documentation to Markdown files inside the `docs` folder.
- Build a proper folder/file structure in `docs` and update the navigation in `mkdocs.yaml`.
- Keep the usual files like `README.md`, `CHANGELOG.md`, `CONTRIBUTING.md` and `LICENSE.md` inside the root folder of the repository (developers expect them to
  be there, especially in open source projects), but keep them short/generic and just refer to the documentation in the `docs` folder.