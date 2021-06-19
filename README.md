# Kubernetes operator
[SDK Operator](https://github.com/operator-framework/operator-sdk)
> It could be interesting to have a REST API exposed, to control the new deployment triggered by the api client

## Helm Based
The existing helm chart are embedded int the operator `Operator` pattern . The operator implementation itself can be installed as a helm chart as well. 
Once it is running, it will `watch` the object of a certain `Kind`, most likeyly is an object of `Custom Resource`.  There is a concept `primary` and `secondary` resources. But for this brief introduction it may not be so interesting.

`Creation`

It is done through an sdk, `operator-sdk`,

```shell
operator-sdk init --plugins helm --domain samutup.com --helm-chart <existing_helm_chart>
```
eg.

```shell
perator-sdk init --plugins helm --domain samutup.com --helm-chart ../minimart/chart
```
This is the template for `operator` package generated

```folder
minimart-operator
├── config
│   ├── crd
│   │   └── bases
│   ├── default
│   ├── manager
│   ├── manifests
│   ├── prometheus
│   ├── rbac
│   ├── samples
│   └── scorecard
│       ├── bases
│       └── patches
└── helm-charts
    └── minimart
        ├── configs
        └── templates
watch.yaml
Makefile
```
Important notes.

`watch.yaml`
> This defined what object is watched for an event, like `delete/create/update`. Under the hood, there is a `controller` that has `reconsiler` object with `reconcile` method to map the event into reconcile request to the `Minimart` custom resource

```yaml
# Use the 'create api' subcommand to add watches to this file.
- group: charts.samutup.com
  version: v1alpha1
  kind: Minimart
  chart: helm-charts/minimart
#+kubebuilder:scaffold:watch
```

`config/crd/bases/*.yaml`

> This defines the custom resources

```yaml
apiVersion: apiextensions.k8s.io/v1
kind: CustomResourceDefinition
metadata:
  name: minimarts.charts.samutup.com
spec:
  group: charts.samutup.com
  names:
    kind: Minimart
    listKind: MinimartList
    plural: minimarts
    singular: minimart
	.....
```




`Update`

What need to update?
The `Makefile` to be updated with the correct docker hub registry.
By default it is,

```text
IMAGE_TAG_BASE ?= <Domain>/<HelmchartName>-operator
```
Change to the actual dockerhub registry,

```text
samutup/minimart-operator
```

The version `VERSION` of the operator image is `0.0.1` by default.

Modify the controller image

```text
# Image URL to use all building/pushing image targets
IMG ?= controller:latest
```
Change it to,

```text
IMG ?= $(IMAGE_TAG_BASE):$(VERSION)
```
`Build and Push docker image`

```shell
make docker-build docker-push
```

At this stage there are 2 docker images, `samutup/minimart-operator` and `samutup/minimart`

`run locally`

```shell
make install run
```
REST Api resources exposed by the operator are,

```
http://127.0.0.1:8080/metrics
http://127.0.0.1:8081/healthz
http://127.0.0.1:8081/readyz
```



`deploy to cluster`

```shell
make deploy
```

So a new custom kubernetes resources is created here, `Minimart`.

The following resources are created now in `cluster`


```shell
kubectl get all -n minimart-operator-system
NAME                                                        READY   STATUS    RESTARTS   AGE
pod/minimart-operator-controller-manager-845bfdc99d-95vcp   2/2     Running   0          10m

NAME                                                           TYPE        CLUSTER-IP      EXTERNAL-IP   PORT(S)    AGE
service/minimart-operator-controller-manager-metrics-service   ClusterIP   10.103.53.110   <none>        8443/TCP   10m

NAME                                                   READY   UP-TO-DATE   AVAILABLE   AGE
deployment.apps/minimart-operator-controller-manager   1/1     1            1           10m

NAME                                                              DESIRED   CURRENT   READY   AGE
replicaset.apps/minimart-operator-controller-manager-845bfdc99d   1         1         1       10m
```

`tail the operator logs`

```shell
kubectl logs -f deployment.apps/minimart-operator-controller-manager  -n minimart-operator-system -c manager
```

```json
{
"level":"info",
"ts":1624107321.9514985,
"logger":"controller-runtime.manager.controller.minimart-controller","msg":"Starting EventSource",
"source":"kind source: charts.samutup.com/v1alpha1, Kind=Minimart"
}
```
To see the events and status of Minimart `CRD`,

```shell
kubectl describe crd minimarts.charts.samutup.com
```

`trigger the kubernetes spawning`

```text
kubectl apply -f config/samples/charts_v1alpha1_minimart.yaml
```



```text
❯ kubectl get pods
NAME                               READY   STATUS    RESTARTS   AGE
minimart-sample-655b7d48fc-wdxc7   1/1     Running   0          55s
❯ kubectl get all
NAME                                   READY   STATUS    RESTARTS   AGE
pod/minimart-sample-655b7d48fc-wdxc7   1/1     Running   0          65s

NAME                      TYPE        CLUSTER-IP     EXTERNAL-IP   PORT(S)        AGE
service/kubernetes        ClusterIP   10.96.0.1      <none>        443/TCP        4h19m
service/minimart-sample   NodePort    10.106.27.26   <none>        80:30038/TCP   65s

NAME                              READY   UP-TO-DATE   AVAILABLE   AGE
deployment.apps/minimart-sample   1/1     1            1           65s

NAME                                         DESIRED   CURRENT   READY   AGE
replicaset.apps/minimart-sample-655b7d48fc   1         1         1       65s
```

### Cleanup
```shell
kubectl delete -f config/samples/charts_v1alpha1_minimart.yaml
make undeploy
```

```text
Name:         minimarts.charts.samutup.com
Namespace:    
Labels:       <none>
Annotations:  <none>
API Version:  apiextensions.k8s.io/v1
Kind:         CustomResourceDefinition
Metadata:
  Creation Timestamp:  2021-06-19T12:55:17Z
  Generation:          1
  Managed Fields:
    API Version:  apiextensions.k8s.io/v1
    Fields Type:  FieldsV1
    fieldsV1:
      f:status:
        f:acceptedNames:
          f:kind:
          f:listKind:
          f:plural:
          f:singular:
        f:conditions:
    Manager:      kube-apiserver
    Operation:    Update
...
Spec:
  Conversion:
    Strategy:  None
  Group:       charts.samutup.com
  Names:
    Kind:       Minimart
    List Kind:  MinimartList
    Plural:     minimarts
    Singular:   minimart
  Scope:        Namespaced
  Versions:
    Name:  v1alpha1
    ...
```

This is exposing an endpoint

```text
/apis/charts.samutup.com/v1alpha1
```

```json
{
    "kind": "APIResourceList",
    "apiVersion": "v1",
    "groupVersion": "charts.samutup.com/v1alpha1",
    "resources": [
        {
            "name": "minimarts",
            "singularName": "minimart",
            "namespaced": true,
            "kind": "Minimart",
            "verbs": [
                "delete",
                "deletecollection",
                "get",
                "list",
                "patch",
                "create",
                "update",
                "watch"
            ],
            "storageVersionHash": "wqUOO9GS5iM="
        },
        {
            "name": "minimarts/status",
            "singularName": "",
            "namespaced": true,
            "kind": "Minimart",
            "verbs": [
                "get",
                "patch",
                "update"
            ]
        }
    ]
}
```
# How to trigger the Custom Resource changes from Application.

While in the above example, the CR update is through `kubectl apply -f`, there is still not very clear how this can be triggered from the application.
For example, the click buttom on portal will trigger the update on the CR so that, the operator will do action on the Custom Object.

## Ansible Based

## Golang Based

[Kubernetes Operator](https://sdk.operatorframework.io/docs/building-operators/golang/tutorial/)

What resource the controller is wathed?

It is defined in the `XXXX_controller.go`

```go
func (r *MemcachedReconciler) SetupWithManager(mgr ctrl.Manager) error {
	return ctrl.NewControllerManagedBy(mgr).
		For(&cachev1alpha1.Memcached{}).
		Complete(r)
}

```
`Quote from the above documentation`

>The `NewControllerManagedBy()` provides a controller builder that allows various controller configurations. `For(&cachev1alpha1.Memcached{})` specifies the `Memcached` type as the primary resource to watch. For each `Memcached` type `Add/Update/Delete` event the reconcile loop will be sent a reconcile `Request`  (a namespace/name key) for that Memcached object.`Owns(&appsv1.Deployment{})` specifies the Deployments type as the secondary resource to watch. For each `Deployment` type `Add/Update/Delete` event, the event handler will map each event to a reconcile Request for the owner of the Deployment. Which in this case is the Memcached object for which the Deployment was created.

## Watching what?

`Primary Resources`

It is the `Memcached` type.
Any add,delete,update on the object with that type will be watched

`Secondary Resources`

It is a `Deployment` resource.
For each add,delete,update event on the `Deployment`, event handler will map it into a reconcile `Request` to the owner of the `Deployment` which is the `Memcached` object for which the deployment is created.

## Reconcile Loop
`Controller` has `Reconciler` object with `Reconcile` function that implements the `reconcile loop`
The `reconcile loop` is passed the request argument which is `Namespace/Name` key used to look up the primary resource object, `Memchaced` from the cache.



```go

```

