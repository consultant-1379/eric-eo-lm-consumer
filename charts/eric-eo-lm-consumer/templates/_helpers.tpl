{{/* vim: set filetype=mustache: */}}
{{/*
Expand the name of the chart.
*/}}
{{- define "eric-eo-lm-consumer.name" -}}
{{- default .Chart.Name .Values.nameOverride | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{/*
Create a default fully qualified app name.
We truncate at 63 chars because some Kubernetes name fields are limited to this (by the DNS naming spec).
*/}}
{{- define "eric-eo-lm-consumer.fullname" -}}
{{- if .Values.fullnameOverride -}}
{{- .Values.fullnameOverride | trunc 63 | trimSuffix "-" -}}
{{- else -}}
{{- template "eric-eo-lm-consumer.name" . -}}
{{- end -}}
{{- end -}}

{{/*
Create chart name and version as used by the chart label.
*/}}
{{- define "eric-eo-lm-consumer.chart" -}}
{{- printf "%s-%s" .Chart.Name .Chart.Version | replace "+" "_" | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{/*
Create chart version as used by the chart label.
*/}}
{{- define "eric-eo-lm-consumer.version" -}}
{{- printf "%s" .Chart.Version | replace "+" "_" | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{/*
Create main image registry url
*/}}
{{- define "eric-eo-lm-consumer.mainImagePath" -}}
  {{- include "eric-eo-evnfm-library-chart.mainImagePath" (dict "ctx" . "svcRegistryName" "licenseConsumer") -}}
{{- end -}}

{/*
The pgInitContainer image registry url
*/}}
{{- define "eric-eo-lm-consumer.pgInitContainerPath" -}}
  {{- include "eric-eo-evnfm-library-chart.mainImagePath" (dict "ctx" . "svcRegistryName" "pgInitContainer") -}}
{{- end -}}

{{/*
Create image pull secrets
*/}}
{{- define "eric-eo-lm-consumer.pullSecrets" -}}
  {{- include "eric-eo-evnfm-library-chart.pullSecrets" . -}}
{{- end -}}

{{/*
Create Ericsson Product Info
*/}}
{{- define "eric-eo-lm-consumer.helm-annotations" -}}
  {{- include "eric-eo-evnfm-library-chart.helm-annotations" . -}}
{{- end -}}

{{/*
Create Ericsson product app.kubernetes.io info
*/}}
{{- define "eric-eo-lm-consumer.kubernetes-io-info" -}}
  {{- include "eric-eo-evnfm-library-chart.kubernetes-io-info" . -}}
{{- end -}}

{{/*
Create pullPolicy for licenseConsumer service container
*/}}
{{- define "eric-eo-lm-consumer.imagePullPolicy" -}}
  {{- include "eric-eo-evnfm-library-chart.imagePullPolicy" (dict "ctx" . "svcRegistryName" "licenseConsumer") -}}
{{- end -}}

{{/*
Create pull policy for pgInitContainer
*/}}
{{- define "eric-eo-lm-consumer.pgInitContainer.imagePullPolicy" -}}
  {{- include "eric-eo-evnfm-library-chart.pgInitContainer.imagePullPolicy" . -}}
{{- end -}}

{{/*
Define nodeSelector property
*/}}
{{- define "eric-eo-lm-consumer.nodeSelector" -}}
  {{- include "eric-eo-evnfm-library-chart.nodeSelector" . -}}
{{- end -}}

{{/*
DR-D1123-134
Generation of role bindings for admission control in OpenShift environment
*/}}
{{- define "eric-eo-lm-consumer.serviceAccount.name" -}}
  {{- printf "%s-sa" (include "eric-eo-lm-consumer.name" .) | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{/*
DR-D1123-134
Rolekind parameter for generation of role bindings for admission control in OpenShift environment
*/}}
{{- define "eric-eo-lm-consumer.securityPolicy.rolekind" }}
  {{- include "eric-eo-evnfm-library-chart.securityPolicy.rolekind" . }}
{{- end }}

{{/*
DR-D1123-134
Rolename parameter for generation of role bindings for admission control in OpenShift environment
*/}}
{{- define "eric-eo-lm-consumer.securityPolicy.rolename" }}
  {{- include "eric-eo-evnfm-library-chart.securityPolicy.rolename" . }}
{{- end }}

{{/*
DR-D1123-134
RoleBinding name for generation of role bindings for admission control in OpenShift environment
*/}}
{{- define "eric-eo-lm-consumer.securityPolicy.rolebinding.name" }}
  {{- include "eric-eo-evnfm-library-chart.securityPolicy.rolebinding.name" . }}
{{- end }}

{{/*
Kubernetes labels
*/}}
{{- define "eric-eo-lm-consumer.kubernetes-labels" -}}
app.kubernetes.io/name: {{ include "eric-eo-lm-consumer.name" . }}
app.kubernetes.io/instance: {{ .Release.Name | quote }}
app.kubernetes.io/version: {{ include "eric-eo-lm-consumer.version" . }}
{{- end -}}

{{/*
Common labels
*/}}
{{- define "eric-eo-lm-consumer.labels" -}}
  {{- $kubernetesLabels := include "eric-eo-lm-consumer.kubernetes-labels" . | fromYaml -}}
  {{- $globalLabels := (.Values.global).labels -}}
  {{- $serviceLabels := .Values.labels -}}
  {{- include "eric-eo-evnfm-library-chart.mergeLabels" (dict "location" .Template.Name "sources" (list $kubernetesLabels $globalLabels $serviceLabels)) }}
{{- end -}}

{{/*
Merged labels for extended defaults
*/}}
{{- define "eric-eo-lm-consumer.labels.extended-defaults" -}}
  {{- $extendedLabels := dict -}}
  {{- $_ := set $extendedLabels "eric-lm-combined-server-access" "true" -}}
  {{- $_ := set $extendedLabels "eric-oss-common-postgres-access" "true" -}}
  {{- $_ := set $extendedLabels "logger-communication-type" "direct" -}}
  {{- $_ := set $extendedLabels "app" (include "eric-eo-lm-consumer.name" .) -}}
  {{- $_ := set $extendedLabels "chart" (include "eric-eo-lm-consumer.chart" .) -}}
  {{- $_ := set $extendedLabels "release" (.Release.Name) -}}
  {{- $_ := set $extendedLabels "heritage" (.Release.Service) -}}
  {{- $commonLabels := include "eric-eo-lm-consumer.labels" . | fromYaml -}}
  {{- $serviceMesh := include "eric-eo-lm-consumer.service-mesh-inject" . | fromYaml }}
  {{- include "eric-eo-evnfm-library-chart.mergeLabels" (dict "location" .Template.Name "sources" (list $commonLabels $extendedLabels $serviceMesh)) | trim }}
{{- end -}}

{{/*
Create Ericsson product specific annotations
*/}}
{{- define "eric-eo-lm-consumer.helm-annotations_product_name" -}}
  {{- include "eric-eo-evnfm-library-chart.helm-annotations_product_name" . -}}
{{- end -}}
{{- define "eric-eo-lm-consumer.helm-annotations_product_number" -}}
  {{- include "eric-eo-evnfm-library-chart.helm-annotations_product_number" . -}}
{{- end -}}
{{- define "eric-eo-lm-consumer.helm-annotations_product_revision" -}}
  {{- include "eric-eo-evnfm-library-chart.helm-annotations_product_revision" . -}}
{{- end -}}

{{/*
Create a dict of annotations for the product information (DR-D1121-064, DR-D1121-067).
*/}}
{{- define "eric-eo-lm-consumer.product-info" }}
ericsson.com/product-name: {{ template "eric-eo-lm-consumer.helm-annotations_product_name" . }}
ericsson.com/product-number: {{ template "eric-eo-lm-consumer.helm-annotations_product_number" . }}
ericsson.com/product-revision: {{ template "eric-eo-lm-consumer.helm-annotations_product_revision" . }}
{{- end }}

{{/*
Common annotations
*/}}
{{- define "eric-eo-lm-consumer.annotations" -}}
  {{- $productInfo := include "eric-eo-lm-consumer.helm-annotations" . | fromYaml -}}
  {{- $globalAnn := (.Values.global).annotations -}}
  {{- $serviceAnn := .Values.annotations -}}
  {{- include "eric-eo-evnfm-library-chart.mergeAnnotations" (dict "location" .Template.Name "sources" (list $productInfo $globalAnn $serviceAnn)) | trim }}
{{- end -}}

{{/*
Define tolerations property
*/}}
{{- define "eric-eo-lm-consumer.tolerations.licenseConsumer" -}}
  {{- include "eric-eo-evnfm-library-chart.merge-tolerations" (dict "root" . "podbasename" "licenseConsumer" ) -}}
{{- end -}}

{{/*
Check global.security.tls.enabled
*/}}
{{- define "eric-eo-lm-consumer.global-security-tls-enabled" -}}
  {{- include "eric-eo-evnfm-library-chart.global-security-tls-enabled" . -}}
{{- end -}}

{{/*
DR-D470217-007-AD
This helper defines whether this service enter the Service Mesh or not.
*/}}
{{- define "eric-eo-lm-consumer.service-mesh-enabled" }}
  {{- include "eric-eo-evnfm-library-chart.service-mesh-enabled" . -}}
{{- end -}}

{{/*
DR-D470217-011
This helper defines the annotation which bring the service into the mesh.
*/}}
{{- define "eric-eo-lm-consumer.service-mesh-inject" }}
  {{- include "eric-eo-evnfm-library-chart.service-mesh-inject" . -}}
{{- end -}}

{{/*
GL-D470217-080-AD
This helper captures the service mesh version from the integration chart to
annotate the workloads so they are redeployed in case of service mesh upgrade.
*/}}
{{- define "eric-eo-lm-consumer.service-mesh-version" -}}
  {{- include "eric-eo-evnfm-library-chart.service-mesh-version" . -}}
{{- end -}}

{{/*
This helper determines the logging level for service mesh istio-proxy container
*/}}
{{- define "eric-eo-lm-consumer.service-mesh-logs" }}
  {{- include "eric-eo-evnfm-library-chart.service-mesh-logs" . -}}
{{- end -}}

{{/*
Istio excludeOutboundPorts. Outbound ports to be excluded from redirection to Envoy.
*/}}
{{- define "eric-eo-lm-consumer.excludeOutboundPorts" -}}
  {{- include "eric-eo-evnfm-library-chart.excludeOutboundPorts" . -}}
{{- end -}}

{{/*
Define probes property
*/}}
{{- define "eric-eo-lm-consumer.probes" -}}
{{- $default := .Values.probes -}}
{{- if .Values.probing }}
  {{- if .Values.probing.liveness }}
    {{- if .Values.probing.liveness.licenseConsumer }}
      {{- $default := mergeOverwrite $default.licenseConsumer.livenessProbe .Values.probing.liveness.licenseConsumer  -}}
    {{- end }}
  {{- end }}
  {{- if .Values.probing.readiness }}
    {{- if .Values.probing.readiness.licenseConsumer }}
      {{- $default := mergeOverwrite $default.licenseConsumer.readinessProbe .Values.probing.readiness.licenseConsumer  -}}
    {{- end }}
  {{- end }}
{{- end }}
{{- $default | toJson -}}
{{- end -}}

{{/*
To support Dual stack.
*/}}
{{- define "eric-eo-lm-consumer.internalIPFamily" -}}
  {{- include "eric-eo-evnfm-library-chart.internalIPFamily" . -}}
{{- end -}}


{{- define "eric-eo-lm-consumer.podPriority" -}}
  {{- include "eric-eo-evnfm-library-chart.podPriority" ( dict "ctx" . "svcName" "licenseConsumer" ) -}}
{{- end -}}

{{/*
DR-D1123-124
Evaluating the Security Policy Cluster Role Name
*/}}
{{- define "eric-eo-lm-consumer.securityPolicy.reference" -}}
  {{- include "eric-eo-evnfm-library-chart.securityPolicy.reference" . -}}
{{- end -}}

{{- define "eric-eo-lm-consumer.nels.productTypes" -}}
  {{- if .Values.global }}
    {{- if .Values.global.ericsson }}
      {{- if .Values.global.ericsson.licensing }}
        {{- if .Values.global.ericsson.licensing.licenseDomains }}
          {{- range .Values.global.ericsson.licensing.licenseDomains }}{{(print .productType)}};{{- end }}
        {{- end }}
      {{- end }}
    {{- end }}
  {{- end }}
{{- end }}

{{/*
DR-1123-136
Define fsGroup property
*/}}
{{- define "eric-eo-lm-consumer.fsGroup" -}}
  {{- include "eric-eo-evnfm-library-chart.fsGroup" . -}}
{{- end -}}

{{/*
Create prometheus info
*/}}
{{- define "eric-eo-lm-consumer.prometheus" -}}
  {{- include "eric-eo-evnfm-library-chart.prometheus" . -}}
{{- end -}}

{{/*
DR-D470222-010
Configuration of Log Collection Streaming Method
*/}}
{{- define "eric-eo-lm-consumer.log.streamingMethod" -}}
{{- $defaultMethod := "dual" }}
{{- $streamingMethod := (.Values.log).streamingMethod }}
    {{- if not $streamingMethod }}
        {{- if (.Values.global.log).streamingMethod -}}
            {{- $streamingMethod = (.Values.global.log).streamingMethod }}
        {{- else -}}
            {{- $streamingMethod = $defaultMethod -}}
        {{- end }}
    {{- end }}

    {{- if or (eq $streamingMethod "direct") (eq $streamingMethod "indirect") }}
        {{- $streamingMethod -}}
    {{- else }}
        {{- $defaultMethod -}}
    {{- end }}
{{- end }}
