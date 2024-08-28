## Overview
To prevent usage of EVNFM features without a valid license with hard and soft enforcement it's proposed to introduce </br> 
the licenses handling feature. In order to this ADP License Manager service must be integrated in EO, and it will </br> 
oversee the interactions with NeLS, the application that is supposed already installed in the customer network.

The solution is based on the NeLS and ADP LM capabilities.

## General info

License Consumer service follow the BDGS Licensing Behavioral Principles except monitoring (would be handled by NeLS).

This service responsible for:

- Registration on the NeLS server via ADP LM
- Translate licenses to feature list
- Select and report to LM about using licenses
- Handling of license use-cases

## License Management Use-Cases

#### Prevent usage of a feature when the corresponding license is not available
The absence of a License Key in a License Key File implies that there’s no right to use the related feature. </br> 
The Application is never authorized to activate and execute a feature that does not have the corresponding license </br> 
available in the LKF.
</br>
</br> 
### Use-cases:
**Initial deployment and Upgrade**

*Behavior:* 
- LCM operations disabled (Both RO and RW operations)
- Onboarding operations disabled
- Admin operations will be available even if no license
- Application remains running unless explicit update in requirement

**Allow feature for licenses valid in future or expired (in runtime)**

The presence of a License Key in a License Key File enables the Application to use the related feature, regardless of </br>
the validity time limits of the License Key. The LM will inform the Applications about license keys with validity </br>
interval in the future or expired.

Alarms are managed centrally by NeLS for these situations, so Application do not need to trigger any.

*Behavior:*
- No enforcement in EVNFM for all types of licenses except Trial license
- User should be allowed to continue to work even if license validity expires
- Notify user about expired license – warning message

**Allow feature for license is going to expire (Out of Scope)**

Alarms are managed centrally by NeLS for these situations, so Application do not need to trigger any.

**Removed License**

If a License Key was available and the feature was actively used, when the License Key is removed from the LKF </br> 
(maybe it was a trial for the feature and the customer did not buy it), then the Application shall gracefully stop </br>
(means ongoing operations continue but new operations would not be allowed) the feature after LM reports that the </br>
License is not available anymore.

*Behavior:*
- Refer “Prevent usage of a feature when the corresponding license is not available"

**Upgrade License: features**

Upgrade that will require new license keys in a new release

Example: Starter pack -> Base Pack

*Behavior:* </br>
No behavior changes in application but during EVNFM installation, if Starter Package license is replaced by the Base Pack </br>
then the license consumer has to look for a Base license if it’s starter pack license is no longer available

**Add new type of license | Add functionality to license**

Example: new MR </br>
It's supposed that a new functionality could be added only by developers.
## Registered licenses

|Features                     |No license |FAT 102 4423/3 </br>CVNFM_Limited_Key | FAT 102 4423 </br> CVNFM_Full_Key | FAT 102 4423/5 </br>CVNFM_GeoRed_Key |
|-----------------------------|-----------|--------------------------------------|-----------------------------------|--------------------------------------|
|cluster_management           |FALSE      | TRUE                                 | TRUE                              | TRUE                                 |
|enm_integration              |FALSE      | FALSE                                | TRUE                              | TRUE                                 |
|lcm_operations               |FALSE      | TRUE                                 | TRUE                              | TRUE                                 |
|onboarding                   |FALSE      | TRUE                                 | TRUE                              | TRUE                                 |

- *onboarding* - package onboarding
- *enm_integration* - add node to ENM, delete node from ENM
- *lcm_operations* - instantiate, terminate, heal CNF, sync, etc.
- *cluster_management* - register new cluster, modify cluster config, delete cluster


## Run service in IntelliJ IDEA


1. Deploy EVNFM with License Manager, Attract SysInfo Handler must be enabled
2. Port forward for `eric-lm-combined-server-license-server-client`, 9090 -> 9090
3. Port forward for `eric-oss-common-postgres`, 5432 -> 5432
4. Select `application-dev.yml` as the configuration to run
5. Run `EricLicenseConsumerApplication`


## License Consumer API
It's supposed that License Consumer would expose an API to check if functionality is allowed/disallowed based on </br>
Licenses present in License Manager and NeLS.
<br />
<br />


**Description:** Return a list of functionalities allowed/disallowed for the specific application. There is </br>
no possibility to return permissions for all an applications.

**Access control:** Authorization is not needed and no access control.

>GET  /lc/v1/{application}/permissions
<br />

| Parameter        | Parameter Type | Description                                                                                                               |
|------------------|----------------|---------------------------------------------------------------------------------------------------------------------------|
| {application}    | @Path          | define for which application permissions should be returned. <br />Constant value: [cvnfm, vmvnfm, so, pf].               |

## Response examples:

**No License**

```
curl -X GET http://{license_consumer}/cvnfm/permissions
404 NOT_FOUND
```
<br />

**CVNFM Limited License**

```
curl -X GET http://{license_consumer}/cvnfm/permissions
200 OK
{
"permissions": [ "lcm_operations", "onboarding", "cluster_management" ]
}
```
<br />

**CVNFM Full license**
```
curl -X GET http://{license_consumer}/cvnfm/permissions
200 OK
{
"permissions": [ "lcm_operations", "enm_integration", "onboarding", "cluster_management" ]
}
```
<br />

**CVNFM GeoRed license**
```
curl -X GET http://{license_consumer}/cvnfm/permissions
200 OK
{
  "permissions": [ "lcm_operations", "enm_integration", "onboarding", "cluster_management" ]
}
```

## Building LM Consumer with 'stub' active profile
Active profile 'stub' disables cron job and enables stub permission controller to return all hardcoded licenses from 
`com.ericsson.licenseconsumer.mapping.repository.PermissionRepositoryImpl` class.
<br/>
To build such image, execute the following command:
```build
docker build --no-cache --tag <image-name>:<tag> \
--build-arg BASE_IMAGE_VERSION=<version> \
--build-arg ACTIVE_PROFILE='-Dspring.profiles.active=stub' .
```
In order to use this image in EVNFM, push it to the registry and edit eric-eo-lm-consumer deployment to use this 
image.
