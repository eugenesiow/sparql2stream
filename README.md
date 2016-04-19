# sparql2stream
sparql2sql implementation for streams with Complex Event Processing (CEP) engines. Currently supports translation from SPARQL to [Event Processing Language (EPL)](http://www.espertech.com/esper/release-5.2.0/esper-reference/html/) for the Esper CEP engine via S2SML mappings.

### Transforming Linked Sensor Data from RDF to CSV 
To transform the LSD from RDF to CSV, see the tool provided at https://github.com/eugenesiow/lsd-ETL.

### SRBench
The translated queries using the engine can be found at https://github.com/eugenesiow/sparql2stream/wiki.

### Smarthome Analytics Benchmark
The benchmark queries and translations can be found at https://github.com/eugenesiow/ldanalytics-PiSmartHome/wiki/Q1.

### Running Benchmarks
1. `git clone https://github.com/eugenesiow/sparql2stream.git`
2. You need to have [maven](https://maven.apache.org/guides/getting-started/maven-in-five-minutes.html) installed.
3. `cd sparql2stream`
4. `mvn dependency:copy-dependencies package`
5. `cd target`
6. SRBench benchmark can be run with `./testq.sh 1 1`. First argument is the query number from 1 to 10 and the second argument is the run number of the output file. The output is in the current working directory (from which the script is run) in this format `q$1.result.out.$2`, where $1 is the query number and $2 is the run number. Another output of the insertion times is in the `Queries/` folder. You can then run the `./gettime.sh` which will take the output of the times received for the results and subtract the times of the insertion to get the latency of each insertion. Files are produced in the `Queries/` folder with the extension `.diff.out` for the latency times.
7. The smart home benchmark can be run with `./test_smarthome.sh 1 1`. First argument is the query number from 1 to 3 and the second argument is the run number of the output file. The output is in a `smarthome` folder in the current working directory (from which the script is run) in this format `q$1.result.out.$2`, where $1 is the query number and $2 is the run number, another output of the insertion times is in the `Queries/smarthome/` folder. You can then run the `./gettime_smarthome.sh` which will take the output of the times received for the results and subtract the times of the insertion to get the latency of each insertion. Files are produced in the `Queries/smarthome/` folder with the extension `.diff.out` for the latency times.
8. `./test_smarthome_4.sh` runs Q4 in the smart home benchmark and output is in the `smarthome` folder in the current working directory (from which the script is run). 

### sparql2sql
SPARQL to SQL with less joins at https://github.com/eugenesiow/sparql2sql. sparql2stream uses the sparql2sql engine at its core.

### sparql2sql Server

A Jetty-based server to provide a SPARQL endpoint with an RDBMS backend and using the sparql2sql translation engine can be found at  https://github.com/eugenesiow/sparql2sql-server.

### Other Projects
* [LSD-ETL](https://github.com/eugenesiow/lsd-ETL)
* [sparql2sql-server](https://github.com/eugenesiow/sparql2sql-server)
* [Linked Data Analytics](http://eugenesiow.github.io/iot/)
