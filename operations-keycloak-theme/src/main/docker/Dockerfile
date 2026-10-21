# Licensed to the Apache Software Foundation (ASF) under one or more
# contributor license agreements. See the NOTICE file distributed with
# this work for additional information regarding copyright ownership.
# The ASF licenses this file to You under the Apache License, Version 2.0
# (the "License"); you may not use this file except in compliance with
# the License. You may obtain a copy of the License at
#
#     https://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

# Built on the platform's deployed Keycloak image (stock Keycloak + the organization-role policy
# provider, helm-charts/charts/keycloak-helm-chart), so this image is that Keycloak plus the
# ForwardMeasure login theme - a drop-in replacement once REALM_LOGIN_THEME selects the theme.
ARG KEYCLOAK_VERSION
ARG KEYCLOAK_AUTHZ_IMAGE_DIGEST
FROM docker.io/forwardmeasure/keycloak:${KEYCLOAK_VERSION}-authz@${KEYCLOAK_AUTHZ_IMAGE_DIGEST}

# The theme jar built by this module; path passed by the pom, so no version is typed here.
ARG THEME_JAR

COPY --chown=1000:0 ${THEME_JAR} /opt/keycloak/providers/operations-keycloak-theme.jar

USER 1000
