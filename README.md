# Postgress Database Viewer

This is REST api service allowing to expose information about Postgress database and even preview its data.
Following functionality is supported:
- CRUD database configurations (connection details as user, password etc.)
- Following operations are supported upon selected database configuration:
    - Listing database schemas
    - Listening table names
    - Listing table columns (names, datatypes, unique, primary key etc.)
    - Data preview of the table


### How to run it
Use environment property -Dspring.profiles.active=prod to run production version against Postgress DB instance.
Add connection details to application-prod.properties.

### Connection Password encryption  
All passwords stored to database as part of database configuration are encrypted. Service is using Jasypt library for encoding.
This encryption need secret password and should be set within: encryptor.dbEncryptorPassword property. 
The value of this property should not contain directly the password but its encoded version. 

Service is using `jasypt-spring-boot-starter` project and therefore you can have ENC(#encoded string#) for properties that should be encoded and password for it passed through environment property `jasypt.encryptor.password`.

This property should be encoded using following process. 
1. User jasypt maven plugin: `mvn jasypt:encrypt-value -Djasypt.encryptor.password="the password" -Djasypt.plugin.value="theValueYouWantToEncrypt"`
2. Insert into encryptor.dbEncryptorPassword application-prod.properites in form: `encryptor.dbEncryptorPassword=ENC(#value from 1 step#)`
3. Add environment property: `-Djasypt.encryptor.password="password used in step 1"`

Please note that dev environment already has this setup done. You should follow this mainly for production setup.
  
## TODO
Following items are in progress: 
 - Add HATEOS for all resources
 - Add Swagger UI to document API 