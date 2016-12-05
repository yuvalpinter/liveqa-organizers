# liveqa-organizers-clean
Code for organizing and evaluating a TREC LiveQA Challenge, under the BSD open source license.

The main purpose of this repository is as the code house of the system which sends requests to participants' servers with live questions, and collects and stores their answers.

Additional organizers' code resides in this repository as well.

Usage
-----

First, prepare a JavaDB database:
1. Make sure that $JAVA_HOME/db/bin is in your PATH.
2. Copy the file "create.sql" from /sql/ into the directory where you want to create the data base.
3. In command-line type ij
4. type connect 'jdbc:derby:challenge;create=true';
5. type run 'create.sql';
6. type exit;

Run com.yahoo.yrlhaifa.liveqa.challenge.ChallengeSystemMain from command line, with configuration file as a parameter.
An example configuration file is given in /configuration/ directory (named configuration.properties).
Make sure to change the database-connection-string parameter according to your environment.
The challenge-duration parameter should be changed to the duration decided for the challenge (e.g., 24 hours). 

Also, don't forget to set JVM parameters for large heap size and JVM server mode, i.e.,

java -server -Xmx10240m com.yahoo.yrlhaifa.liveqa.challenge.ChallengeSystemMain configuration.properties

