# Overview

An experiment to the spring websockets infrastucture in order to answer some key questions and help tweak the various parameters found in the spring web socket infrastructure e.g. client-inbound-channel executor pools. 

The project is made up of two parts: 

* spring-websocket-performance-client
* spring-websocket-performance-endpoint  

spring-websocket-performance-client consist of two seperate node application which connect to the server componet using different transports; xhr_streaming client (main_xhr.js), web socket client (main_ws.js).  

spring-websocket-performance-endpoint is the server side component which provides an authentication flow, a sockjs endpoint, and a periodic message flow for our analysis.  

The two client variants run on `node` while the server side component runs on `Tomcat 8.0.3+`, Jetty 9.0.7+` or `Glassfish 4.0`.  

# Tomcat 8
The application has been test `Tomcat 8.0.3`.  For Tomcat 8, set `TOMCAT8_HOME` as an environment variable and use [deployTomcat8.sh](https://github.com/cloudmark/spring-websockets-performance-testing/blob/master/spring-websocket-performance-endpoint/deployTomcat8.sh) and [shutdownTomcat8.sh](https://github.com/cloudmark/spring-websockets-performance-testing/blob/master/spring-websocket-performance-endpoint/shutdownTomcat8.sh) in the `spring-websocket-performance-client` directory.  

Open a browser an go to <http://localhost:8080/spring-websocket-performance-endpoint/comet/info> in order to make sure that the application has been deployed successfully. 

# Jetty 9
The easiest way to run on Jetty 9 is mvn jetty:run.  

Open a browser an go to <http://localhost:8080/spring-websocket-performance-endpoint/comet/info> in order to make sure that the application has been deployed successfully. 

# Node
The client side components have been tested on `Node v0.10.22`.  In order to run the client first make sure that the server endpoint is up and running and then execute

`node main_xhr.js <identifier>`

or

`node main_ws.js <identifier>`

depending on the transport you are analysing.  

**Note** The client component is intended to be executed on a number of processes, each process will output some data (explained later on) and we will use the `<identifer>` to identify a particular process.  The name you give as an identifier is not important, my personal perference is  p_0, p_1 etc.  

To run multiple process on a single machine use the following script: 

```
for p in `seq 0 1 399`
do
	PROCESS="p_"$p
	echo "Process [$PROCESS]"; 
	node main_ws.js "$PROCESS"  2>&1 >  ./logs_ws/$PROCESS &; sleep 0.25;
done
```

# A Little More Detail
As outlined earlier the server provides three main services: 

* Authentication Flow 
* Stomp Endpoint (SockJs Stomp Endpoint)
* Message Flow 

## Authentication Flow. 
Once the endpoint is running, you can use 

<http://localhost:8080/spring-websocket-performance-endpoint/login>

to authentication together with the `username` and `password` header. The implemented authentication realm is pretty stupid - [SimpleAuthorizingRealm](https://github.com/cloudmark/spring-websockets-performance-testing/blob/master/spring-websocket-performance-endpoint/src/main/java/org/cloudmark/samples/security/realm/SimpleAuthorizingRealm.java) - and only requires that the `username` and `password` are the same.  

*Note* Authentication enables the web-socket session to identify the principle.  This correlation may be used to gather some statistics from the server side.  

## Message Flow
There are three topics which provide a stream of data to the client.  

* /topic/ping.1 
* /topic/ping.2
* /topic/ping.3

Each of these topics write the following json 
```
{
	time: <current server time in millis>, 
	data: <a random sequence of characters>
}
```
on the response stream periodically every 1, 2, and 3 seconds respectively.  **Note** the length of the random sequence of characters is controlled from the `dummyBytes` property in the [ping.properties](https://github.com/cloudmark/spring-websockets-performance-testing/blob/master/spring-websocket-performance-endpoint/src/main/resources/ping.properties) property file.  


# WS Client
The web socket client uses a modified version of the `sockjs-client-ws` client library.  Executing

`node main_ws.js p_0`

would perform the following sequence of actions: 

1. Authenticate with the backend code using the username p_0 and password p_0
2. Register to /topic/ping.1 (sub-0)
3. Register to /topic/ping.2 (sub-1)
4. Register to /topic/ping.3 (sub-2)
5. Output the username, subscription id, server time, time difference from the last packet received from this subscription.  


An example output for sub-0 is the following
p_0 SUB-0 1395622591670 0
p_0 SUB-0 1395622592670 1001
p_0 SUB-0 1395622593671 1002
p_0 SUB-0 1395622594673 1005
p_0 SUB-0 1395622595674 1000
...


# XHR Client 

The xhr streaming client uses a modified version of the `sockjs-client` client library.  Executing

`node main_xhr.js p_0`

results in the same sequence of actions outlined in the websocket variant.  *Note* that this would use the XHR Streaming transport instead.  

# Statistics

Although the data gather by each client (remember there will be several of these runing at one point) is simple, using the different client log files we can work out the average message arrival of the clients and the standard deviation from that mean.  This will be our guide when optimising the various web-socket message-broker parameters.  

Using the above script each process will output the data to a file whose name matches it's process name. Since we have registered with three subscriptions we will need to seperate the collected statistics in their own individual subscription file. To do this we will use the following script.  
```
for file in `ls -1 p*`
do
	cat $file | grep SUB-0 > ./sub-0/$file
	cat $file | grep SUB-1 > ./sub-1/$file
	cat $file | grep SUB-2 > ./sub-2/$file
done

```

Now that we have the process data per subscription we will group all this data based on the server timestamp, something like an inverted index.  So we will need to transform the process files e.g.

```
p_0 SUB-0 1395622591670 0
p_0 SUB-0 1395622592670 1001
p_0 SUB-0 1395622593671 1002
p_0 SUB-0 1395622594673 1005
p_0 SUB-0 1395622595674 1000
```

```
p_1 SUB-0 1395622591670 0
p_1 SUB-0 1395622592670 1001
p_1 SUB-0 1395622593671 1002
p_1 SUB-0 1395622594673 1005
p_1 SUB-0 1395622595674 1000
```

to a single file containing the following data.  

```
<timestamp> <process count> <min> <max> <avg> <std_dev>
```

an example of this is given below. We will than plot this file to make our observations. 

```
1395622593.671 5 1000 1002 1000.8 0.83666
1395622594.673 9 1003 1005 1004.56 0.726498
1395622595.674 12 999 1002 1000.42 0.792969
1395622596.676 16 1006 1010 1007.81 0.981074
1395622597.677 20 992 998 994.9 1.37267
1395622598.677 24 996 1002 998.667 1.57885
```

To perform this transformation you can use the process.sh file in the scripts folder.  This sruot will also plot a graph of the results using gnuplot and the graph file `inverted.gnuplot` in the scripts folder.    

