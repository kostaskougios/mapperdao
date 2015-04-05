### Download ###

MapperDao can currently only be downloaded via maven central repository or from

http://oss.sonatype.org/content/repositories/releases/com/googlecode/mapperdao/mapperdao/

Please also check how to [configure maven/sbt](MavenConfiguration.md).

### building the source ###

Please clone the project:

```
git clone https://code.google.com/p/mapperdao/ 
```

There are some extra dependencies that needs to manually be installed (oracle and sqlserver driver), README contains information
on how to do that. Alternatively those dependencies can be temporarily removed from the pom.xml.

Then install the jars to your local m2:

```
mvn -DskipTests clean install
```