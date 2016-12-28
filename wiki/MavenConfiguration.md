Please note: if the artifact can't be found, please add the "sonatype.releases" as described at the bottom of this page.


### Sbt ###

```
"com.googlecode.mapperdao" %% "mapperdao" % "1.0.2"
```

### Maven ###

Dependency:

```
<dependency>
	<groupId>com.googlecode.mapperdao</groupId>
	<artifactId>mapperdao_2.11</artifactId>
	<version>1.0.2</version>
</dependency>
```

In case the maven artifact (or one of the dependencies of mapperdao) is not found in central repository, you might have to add the following:

```
<repositories>
	<repository>
		<id>sonatype.releases</id>
		<url>http://oss.sonatype.org/content/repositories/releases/</url>
	</repository>
</repositories>
```

### Snapshots ###

Occasionally MapperDao snapshots of upcoming versions are available. Snapshots can be used if this repository is configured in your pom.xml:

```
<repositories>
	<repository>
		<id>sonatype.snapshots</id>
		<url>http://oss.sonatype.org/content/repositories/snapshots/</url>
	</repository>
</repositories>
```