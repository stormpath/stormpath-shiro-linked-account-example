#Stormpath is Joining Okta
We are incredibly excited to announce that [Stormpath is joining forces with Okta](https://stormpath.com/blog/stormpaths-new-path?utm_source=github&utm_medium=readme&utm-campaign=okta-announcement). Please visit [the Migration FAQs](https://stormpath.com/oktaplusstormpath?utm_source=github&utm_medium=readme&utm-campaign=okta-announcement) for a detailed look at what this means for Stormpath users.

We're available to answer all questions at [support@stormpath.com](mailto:support@stormpath.com).


[![Build Status](https://travis-ci.org/stormpath/stormpath-shiro-web-sample.png?branch=master)](https://travis-ci.org/stormpath/stormpath-shiro-web-sample)

# Stormpath Shiro Account Linking Example

Stormpath is a User Management API that reduces development time with instant-on, scalable user infrastructure. Stormpath's intuitive API and expert support make it easy for developers to authenticate, manage, and secure users and roles in any application. The `stormpath-shiro` plugin allows a [Shiro](http://shiro.apache.org/)-enabled application to use [Stormpath](http://www.stormpath.com) for all authentication and access control needs.

This project is a fork of [stormpath/stormpath-shiro-web-sample](https://github.com/stormpath/stormpath-shiro-web-sample) which provides an example in which an source account from a read-only Stormpath directory (Active Directory mirror, a social directory, etc) is copied to a Stormpath directory, were groups can be applied. **NOTE: Passwords are NOT copied**, The original source directory is used for authentication.

If you have not already seen [stormpath/stormpath-shiro-web-sample](https://github.com/stormpath/stormpath-shiro-web-sample) please take a look at that before continuing.

This feature will be supported natively by Stormpath in the future.

## Documentation

Stormpath offers usage documentation and support for the Apache Shiro Plugin for Stormpath [in the wiki](https://github.com/stormpath/stormpath-shiro/wiki). Please email support@stormpath.com with any errors or issues with the documentation.

## Links

Below are some resources you might find useful!
- [The Apache Shiro Plugin for Stormpath](https://github.com/stormpath/stormpath-shiro)
- [User Permissions with Apache Shiro and Stormpath](https://stormpath.com/blog/user-permissions-apache-shiro-and-stormpath/)

**Stormpath Java Support**
- [Stormpath API Docs for Java](https://docs.stormpath.com/java/apidocs/)
- [Stormpath Java Product Guide](https://docs.stormpath.com/java/product-guide/)
- [Stormpath Java SDK](https://github.com/stormpath/stormpath-sdk-java)


## Stormpath Tenant Configuration

This example assumes you have the following setup:
1. Application
1. Source Directory (Social, Active Directory, etc)
1. Target Directory (Stormpath Cloud Directory), referred `cloud directory` in this example.
1. Group named `admin`, `good_guys`, and `bad_guys` defined in the `cloud directory`.
1. Accounts defined in the Source Directory, this doc will assume account Joe Coder (`jcoder`) will be created.


## Application Configuration

Edit the following properties in `src/main/webapp/WEB-INF/shiro.ini`:

| property | description | example value |
|----------|-------------|---------------|
| stormpathClient.apiKeyFileLocation | Location of api secrets file | /home/jcoder/.stormpath/apiKey.properties |
| stormpathRealm.applicationRestUrl | Applicaton HREF | https://enterprise.stormpath.io/v1/applications/<app-id> |
| stormpathRealm.cloudDirHref | HREF of a cloud directory | https://enterprise.stormpath.io/v1/directories/<dir-id> |

## Contributing

Contributions, bug reports and issues are very welcome. Stormpath regularly maintains this repository, and are quick to review pull requests and accept changes!

You can make your own contributions by forking the develop branch of this
repository, making your changes, and issuing pull request on the develop branch.

## Build Instructions ##

This project requires Maven 3 to build. Run the following from a command prompt:

`mvn clean compile`

Release changes are viewable in the [change log](changelog.md)

## Running the Sample Application ##

Run it:

`mvn jetty:run`

Open a browser to: http://localhost:8080/
Log in as `jcoder`, after a successful login, this user should have NO roles associated.

Back in the Stormpath Admin Console you will see a new Account created in the `cloud directory`.
Add this user to the `admin` group.

Browse back to: http://localhost:8080/
If you do NOT see the new `admin` role, log out and log back in.


**NOTE:** This example does NOT keep the account synchronized, meaning if Joe Coder changes his name to Joseph Coder.  The user's name will still appear as 'Joe Coder' to this application.


## Copyright ##

Copyright &copy; 2013-2016 Stormpath, Inc. and contributors.

This project is open-source via the [Apache 2.0 License](http://www.apache.org/licenses/LICENSE-2.0).
