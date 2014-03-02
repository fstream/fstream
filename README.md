fStream System
===

This is the fStream project.

Setup
---

Clone the repository:

	git clone git@bitbucket.org:fstream/fstream.git

Install [gradle](http://www.gradle.org/download). On Mac, you can use [brew](http://brew.sh/):

	brew install gradle
	
Install [Gradle IDE](https://github.com/spring-projects/eclipse-integration-gradle/)

Import Gradle project in Eclipse  

Patch Eclipse to use Lombok:

	wget http://projectlombok.googlecode.com/files/lombok.jar
	java -jar lombok.jar

Build
---

To build from the command line:

	cd fstream
	gradle build

