### Universal command line tool

Currently there are two tool for working with nispero, metapasta etc:

* [giter8](https://github.com/n8han/giter8) - generic tool for downloading templates form github. 
It not so easy to make it work with windows, also it doesn't support tags

* **nisperoCLI** - command line interface for **nispero** together with AWS account configuration.

The main idea of universal command line tool is simplification of **nisperoCLI** in way that it can be used for
every project that with templates.

### Functionality

#### AWS acount configuration (configure command)

* creates `nispero` security group with SSH and HTTPS rights
* creates tag with default value for `bucketSuffix` 
* creates `nispero` IAM role and instance profile
* creates a key pair if needed

#### Applying template (create command)

* clones repository (tag can be selected)
* provides default values for placeholders `credentialsProvider` and `bucketSuffix`
* substitutes placeholders

#### Credentials retreiving

New version tries to retrieve AWS credentials from several places:

1 from file (by default from `~/nispero.credentials`, but can be changed using last argument)
* from environment variable
* from instance profile

