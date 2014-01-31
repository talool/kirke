Kirke
=====

Kirke, the Greek Goddess of Transformation, will help you import data into Talool 


Usage
-----------
This is an executable JAR package. To run it, use the following command (sample args - change them as needed):

````
java -jar kirke.jar /xml/EntertainmentTest.xml /xsl/EntertainmentTest.xml 32
````

| Arg    | Description |
|:---------|:-------------|
| 0        | The path to the 3rd party XML file you want to transform.  Can be a resource file or a URL.
| 1        | The path to the XSL file that will transform the 3rd party XML.  This files should be stored in /xsl.
| 2        | The MerchantAccount ID for the publisher.  This merchant will get a dummy deal offer will all the imported deals and all imported merchants will be created by this account.

Environment
-----------
The dataSource.xml is currently pointed at //dev-db1:5432/talool.  For test, you should point this at your own test DB on dev-db1.

- dump the "talool" db to a file
  > pg_dump -U postgres talool > talool-dev.sql

- create "talooldoug"
 > createdb -U postgres talooldoug

- import "talool" into "talooldoug"
 > psql -U postgres talooldoug < talool-dev.sql

