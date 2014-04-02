Kirke
=====

Kirke, the Greek Goddess of Transformation, will help you import data into Talool.  

Pronounced "*kir KEE*".  

Read about her [backstory from the Odyssey.](http://messagenetcommresearch.com/myths/bios/circe.html)


Usage
-----------
This is an executable JAR package. Run it as "tomcat" from a directory owned by "tomcat", so any imported images are created with the correct permissions for the admin.  Here's an example (sample args - change them as needed):
````
java -jar kirke.jar /xml/EntertainmentTest.xml /xsl/EntertainmentTest.xsl 32
````

| Arg    | Description |
|:---------|:-------------|
| 0        | The path to the 3rd party XML file you want to transform.  Can be a resource file or a URL.
| 1        | The path to the XSL file that will transform the 3rd party XML.  This file should be stored in /xsl.
| 2        | The MerchantAccount ID for the publisher.  This merchant will get a dummy deal offer will all the imported deals and all imported merchants will be created by this account.
| 3        | (Optional) Namespace used in the XML/XSL.

Environment
-----------
The dataSource.xml is currently pointed at //dev-db1:5432/talool.  For testing, you should point this at your own test DB on dev-db1.

- Dump the "talool" db to a file
````
pg_dump -U postgres talool > talool-dev.sql
````
- Create "talooldoug"
````
createdb -U postgres talooldoug
````
- Import "talool" into "talooldoug"
````
psql -U postgres talooldoug < talool-dev.sql
````
