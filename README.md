# ***ATCP is now abandoned!***
1. ## Abstract

   *ATCP* is a powerful C/S long-TCP connection framework developed by ATATC™. It supports:

   - p2p communication (even 2 client of the same IP)
   - TCP transition
   - RSA encryption/signature
   - Message Flags
   - Mutil-threads processing
   - Load balance
   - Local data compression

2. ## Before Reading

   *** Before reading, notice the form of text, they have meanings.** 

   1. object or class or type

      <u>XXX</u>

   2. specific name

      *XXX*

   3. method and its notes

      ```pseudocode
      XXX
      ```

      > XXX

3. ## Download

   Download .jar, .java or .py files here. 

4. ## Main Logic

   Lifecycle:<img src="/Users/futianhao/Documents/Projects/Projects/ATCP/img/atcp_main_logic.png" alt="200%" style="zoom:200%;" />

   2. ### Message Blocking

      When *send()* is called, the message will be added to the message queue, it won't be sent until the last message gets its result or there's no message before. 
      In several cases, the next message will be sent at the same time as current message. Cases including using *Flags.RESULT_NO_NEED*, or current message is a result (this can't be caused manually). 

      <u>MID</u>s are to bind the result to its original message. 
      Each message that is manually sent has a <u>MID</u>, method *onRecved()* will be called back when the result is received. Use the <u>MID</u> to recognize the result's original message. 

5. ## Objects

   1. ### Log

      *** *indi.atatc.atcp_server.packages.log.Log*. This is an singleton object, which stores server logs and all the Input/Output Streams.** 

      1. #### Set Debug Level

         ```java
         public enum  DebugLevel {
             INFO, DEBUG, WARNING, ERROR
         }
         ```

         *INFO* < *DEBUG* < *WARNING* < *ERROR*
         <u>Log</u> will only print the message to the console if the message's debug level is bigger than or the same as current debug level. 

         ```java
         Log.getInstance().debugLevel = Log.DebugLevel.DEBUG;
         ```

         > Set the Log's debug level as *DEBUG*, then the Log will only print the message whose debug level is from *INFO* to *DEBUG* (including *INFO* and *DEBUG*). 

      2. #### Publish

         The most important method is *log.publish()*. 

         ```java
         Log.getInstance().publish();
         ```

         The parameter "message" can be either <u>Exception</u> or any type else. 
         If <u>Exception</u>, the debug level must be *ERROR*. Otherwise, you must fill the parameter "debugLevel". 
         For both cases, you can specify the end of the message, it's "\n" in default. 

         **For example:** 

         ```java
         Log log = Log.getInstance();
         log.debugLevel = Log.DebugLevel.DEBUG;
         log.publish("log message", Log.DebugLevel.INFO);
         log.publish(new RuntimeException("a runtime exception for test"));
         ```

         <center>GOES TO</center>

         ```console
         log message
         
         ```

         Because ERROR > BEBUG > INFO. 

         **Another sample:** 

         ```java
         Log log = Log.getInstance();
         log.debugLevel = Log.DebugLevel.DEBUG;
         log.publish("log message 1", ", ", Log.DebugLevel.INFO);
         log.publish("log message 2", Log.DebugLevel.INFO);
         ```

         <center>GOES TO</center>

         ```console
         log message 1, log message 2
         
         ```

         In this case, I specified the end of the first message. 

      3. #### Prison

         <u>Prison</u> is an object which stores many blacklist <u>Column</u>s. Each <u>Column</u> can store objects. There will always be a <u>Column</u> with the name "ip", the IPs failed in *judge()* won't be added to the <u>Column</u> automatically, you can add them manually in *judge()*. 

         Get the <u>Prison</u> object: 
         
         ```java
      Log.Prison prison = Log.getInstance().prison;
         ```

         Main methods:
         
         ```java
      String name = "column 1";
         Log.Prison.Column column = new Log.Prison.Column(name);
      prison.add(column);
         ```
         
         > Add a blacklist <u>Column</u> to the <u>Prison</u>. 
         >
         > Throw <u>StatusError</u> if there is already a <u>Column</u> which has the same name as the given <u>Column</u>. 
         
         ```java
         Column column1 = prison.getColumnByName("column 1");
         ```
         
         > Get the <u>Column</u> with the name. 
         >
         > Throw <u>StatusError</u> if there is no such <u>Column</u> in the <u>Prison</u>. 
         
         ```java
         column1.add("element 1 in column 1");
         prison.contains("element 1 in column 1"); // will return true;
         ```
         
         > Return whether any <u>Column</u> contains the element. 
         
         ```java
         prison.clear();
         ```
         
         > Empty the <u>Prison</u>. 

      4. #### Time

         <u>Log</u> also provides a static method to get <u>String</u> time. 

         ```java
         String time = Log.time();
         Log.getInstance().publish(time, Log.DebugLevel.INFO);
         ```

         <center>GOES TO</center>

         ```console
         2020-7-24 15:37:26
         ```

         > 「YEAR」-「MONTH (max-12)」-「DAY (max-366)」 「HOUR (max-24)」:「MINUTE (max-60)」:「SECOND (max-60)」
         >
         > yyyy-MM-dd HH:mm:ss

         You can also specify the format: 

         ```java
         String format = "HH:mm";
         String time = Log.time(format);
         Log.getInstance().publish(time, Log.DebugLevel.INFO);
         ```

         <center>GOES TO</center>

         ```console
         15:37
         ```

         > Use the same format as <u>SimpleDateFormat</u>. 

   2. ### Values

      *** indi.atatc.atcp_server.packages.data.Values. This is an singleton object which stores global attributes. The attribute names must be <u>String</u>. **

      1. #### Define New Attributes

         ```java
      String name = "attribute 1";
         Object value = "value of attribute 1";
         Values.getInstance().put(name, value);
         ```
      
         > Set a new attribute or rewrite the attribute named "attribute 1". 

         ```java
      String valueOfAttribute1 = (String) Values.getInstance().get("attribute 1");
         ```
      
         > Get the attribute named "attribute 1". 
      >
         > Method *get()* will throw AccidentEvents.StatusError when there is no such attribute. 

      2. #### Pre-defined Attributes

         There are 6 pre-defined attributes: 

         | Name                     | Default Value | Usage                               |
         | ------------------------ | ------------- | ----------------------------------- |
         | "log_path"               | "log.txt"     | The path of the log file.           |
         | "key_length"             | 2048          | The key length of the RSA key pair. |
         | "separator_first_grade"  | "\\\\\\"      |                                     |
         | "separator_second_grade" | "@"           |                                     |
         | "separator_third_grade"  | "#"           |                                     |
         | "separator_flag"         | ":"           |                                     |

   3. ### Flag & Flags

      1. #### Flag

         <u>Flag</u>s are used to identify the properties of a message. 
         There are 2 types of <u>Flag</u>s: system-owned <u>Flag</u>s and costume <u>Flag</u>s. There are totally five pre-defined (system-owned) <u>Flag</u>s: *RESULT_NO_NEED*, *KILL*, *RES*, *TO_PID*, *FROM_PID*. 

         | Name             | String                     | Usage                                                        | User Available | User Visible |
         | ---------------- | -------------------------- | ------------------------------------------------------------ | -------------- | ------------ |
         | *RESULT_NO_NEED* | \\\\res_no_need\\\         | The message doesn't need a result. The message with this <u>Flag</u> won't be blocked). | True           | False        |
         | *FROM_PID*       | \\\from_pid\\\: <u>PID</u> | The message is a p2p message from a <u>PID</u>.              | False          | True         |
         | *TO_PID*         | \\\to_pid\\\\: <u>PID</u>  | The message is a p2p message which expected to be sent to a <u>PID</u>. | False          | False        |
         | *KILL*           | \\\kill\\\                 | This is the last message.                                    | False          | False        |
         | *RES*            | \\\res\\\                  | This is a result.                                            | False          | False        |

         <u>Flag</u>s can have values, like *Flags.FROM_PID* and *Flags.TO_PID*. The values must be <u>String</u>. 

         1. ##### Create A Costume Flag

            ```java
            Basics.ContainerClass.Flag flag = new Basics.ContainerClass.Flag("costume flag 1");
            ```

            > Create a costume <u>Flag</u> named "costume flag 1". 

            ```java
            Basics.ContainerClass.Flag flag = new Basics.ContainerClass.Flag("costume flag 2", "value of costume flag 2");
            ```

            > Create a costume <u>Flag</u> named "costume flag 2" with value "value of costume flag 2". 

         2. ##### Get The Flag's Value

            ```java
            Basics.ContainerClass.Flag flag1 = new Basics.ContainerClass.Flag("flag 1", "I am flag 1");
            String valueOfFlag1 = flag1.getValue();
            Log.getInstance().publish(valueOfFlag1, Log.DebugLevel.INFO);
            ```

            <center>GOES TO</center>

            ```console
            I am flag 1
            
            ```

      2. #### Flags

         *** <u>Flags</u> is a serial of <u>Flag</u>s. All the pre-defined <u>Flag</u>s are also stored inside as static members. ** 

         1. ##### Create A New Flags Object

            ```java
            Flags flags = new Flags(Flags.RESULT_NO_NEED, new Basics.ContainerClass.Flag("costume flag 1", "value of costume flag 1"));
            ```

            > Create a new Flags object which contains *Flags.RESULT_NO_NEED* and a costume <u>Flag</u>. 

         2. ##### Add

            ```java
            flags.add(new Basics.ContainerClass.Flag("costume flag 2"));
            ```

            > Add a new costume <u>Flag</u> to the <u>Flags</u> object. 

         3. ##### Get Value

            ```java
            String valueOfFlag1 = flags.valueOf("costume flag 1");
            Log.getInstance().publish(valueOfFlag1, Log.DebugLevel.INFO);
            ```

            <center>GOES TO</center>

            ```console
            value of costume flag 1
            
            ```

            > Get the value of <u>Flag</u> "costume flag 1". 
            > Method *valueOf()* will return <u>null</u> if there is no such <u>Flag</u>. 

            Or, 

            ```java
            String valueOfFlag1 = flags.valueOf(new Basics.ContainerClass.Flag("costume flag 1"));
            Log.getInstance().publish(valueOfFlag1, Log.DebugLevel.INFO);
            ```

            <center>GOES TO</center>

            ```console
            value of costume flag 1
            
            ```

            > Get the value of <u>Flag</u> "costume flag 1". 
            > Method *valueOf()* will return <u>null</u> if there is no such <u>Flag</u>. 

         4. ##### Contains

            ```java
            boolean contains = flags.contains(new Basics.ContainerClass.Flag("costume flag 1"));
            Log.getInstance().publish(contains, Log.DebugLevel.INFO);
            ```

            <center>GOES TO</center>

            ```console
            true
            
            ```

            > Get whether the <u>Flags</u> object contains <u>Flag</u> *Flags.RESULT_NO_NEED*. 

         5. ##### Iteration

            Use *toArray()* to turn the <u>Flags</u> object into an array. Then use foreach for iteration: 

            ```java
            for (Basics.ContainerClass.Flag flag: flags.toArray()) {
            }
            ```

6. ## Usage

   *** All the samples are written with JAVA, if you are using Python version, the differences are mentioned in the source code.** 

   1. ### Server

      1. #### Setup Your Configuration

         <div name="setup your configuration">Create a new <u>Configutation</u> object: </div>

         ```java
         Server.Configuration configuration = new Server.Configuration();
         configuration.name = "ATCP Test"; // the server's name
         configuration.port = 1024; // server port, it's 4747 in default
         configuration.project = "ATCP"; // the project to which the server belongs
         ```

      2. #### Create A Simple <u>Server</u> Object

         ```java
         Server server = new Server(2000, configuration) {
             @Override
             public Process onConnected(Connection connection) {
                 return new Process(this, connection) {
                     @Override
                     protected void onSent(MID mid) {
                     }
         
                     @Override
                     protected void onRecved(MID mid, String result, Flags flags) {
                     }
         
                     @Override
                     protected String process(String msg, Flags flags) {
                     }
                 };
             }
         };
         ```

         Override 1 abstract method: 

         ```java
         abstract Process onConnected(Connection connection)
         ```

         > This method should return a <u>Process</u> object which will be used to handle the client's requests. 

         There are 5 callback methods in a <u>Server</u> object:

         ```java
         void onStart()
         ```

         > Called when

         ```java
         void onStarted()
         ```

         > Called when

         ```java
         abstract Process onConnected(Connection connection)
         ```

         > Called when

         ```java
         void onInterrupt()
         ```

         > Called when

         ```java
         void onInterrupted()
         ```

         > Called when

         *** Attention, make sure that you have complete all the settings here because you can never meet this <u>Server</u> object again anymore.** 

         Use

         ```java
         Process = new Process(this, connection) {
             @Override
             protected void onSent(MID mid) {}
         
             @Override
             protected void onRecved(MID mid, String result, Flags flags) {}
         
             @Override
             protected String process(String msg, Flags flags) {
                 return "recved" + msg;
             }
         };
         ```

         to create a new <u>Process</u> object. 
         There are 9 callback methods during a <u>Process</u> object's lifecycle: 

         ```java
         void onStart()
         ```
         
         > Called when <u>Thread</u> has been started. 

         ```java
      void onStarted()
         ```
         
         > Called when the connection parameters have been determined. 

         ```java
      void onSend(MID mid)
         ```
         
         > Called when a message is about to be sent. 

         ```java
      abstract void onSent(MID mid)
         ```
         
         > Called when a message has been sent. 

         ```java
      abstract void onRecved(MID mid, String result, Flags flags)
         ```
         
         > Called when a result is sent back to the server. 

         ```java
      abstract String process(String msg, Flags flags);
         ```
         
         > Called when the server receives a message. 
      > This method usually should return a String, if you don't want to return anything, just return "". 
      
      ```java
         void onClosed()
      ```
      
      > Called when the <u>Process</u> is about to end. 
      
         ```java
      void onDiscard()
         ```
      
         > Called when the <u>Process</u> has ended. 

         ```java
      void onDiscard()
         ```
      
         > Called when the <u>Process</u> object is about to be destroyed. 

         

         
      
      3. #### Setup <u>Process</u> Attributes

         You can disable any additional function like RSA encryption, message blocking, multi-threads processing. 
         If you change these attributes after the <u>Process</u> has been started (generally you can't), it won't work. 

         1. ##### RSA Encryption

            ```java
            server.setRSAOn(true/false);
            ```

            | Boolean        | Affect                                                       |
            | -------------- | ------------------------------------------------------------ |
            | true (default) | Use RSA encryption and signature to keep every message safe. |
            | false          | All the messages will be sent in plaintext.                  |

            Make sure you have a secure environment before you disable this function. 

         2. ##### Message Blocking

            ```java
            server.setMessageBlockingOn(true/false);
            ```

            | Boolean        | Affect                                                       |
            | -------------- | ------------------------------------------------------------ |
            | true (default) | There won't be 2 messages being handled by the remote at the same time. |
            | false          | The message will be sent and handled by the remote as long as the user calls *send()*. |

            Generally, this function is ineffective to the main function. Disable this function to improve the performance, but might cause some bugs. 

         3. ##### Multi-threads Processing

            ```java
            server.setMultiThreadsOn(true/false);
            ```

            | Boolean         | Affect                                                    |
            | --------------- | --------------------------------------------------------- |
            | true            | Every message will be handled in several threads if able. |
            | false (default) | Every message will be handled in the same thread.         |

            This function is disabled in default. Enable it if you have a lot of computations for each client. 

         4. ##### Error Handling

            ```java
            server.setErrorHandlingOn(true/false);
            ```

            | Boolean        | Affect                                                       |
            | -------------- | ------------------------------------------------------------ |
            | true (default) | Catch every known exception automatically.                   |
            | false          | Catch every known exception and throw an <u>AccidentEvent</u> with the origin message. |

            Never disable this function unless in tests. If disabled, all will be thrown directly. Disable function might cause more exceptions. 

      4. #### Setup <u>LoopListener</u>s

         Use

         ```java
         LoopListener loopListener = new LoopListener() {
           	// ToDo: override this method
             @Override
             public boolean when() {
                 if (...) return true; // return true to execute
               	else return false; // return false to pass
             }
         
           	// ToDo: Override this method
             @Override
             public void run() {
               	// run sth
             }
         };
         ```

         to create a new <u>LoopListener</u>. If *when()* returns true, *run()* will be called. 
         A <u>LoopListener</u> can be added to a <u>Server</u> or a <u>Process</u>. Generally, you should send or do anything but settings inside a <u>LoopListener</u>. 

      5. #### Set Connect Rule

         Override *judge()* in the <u>Server</u> object. 

         ```java
         Server server = new Server(configuration) {
           	//Todo: override this method
           	@Override
           	public boolean judge(Log log, IP ip) {
               	if (...) return true; // return true to allow the ip to connect
                 else return false; // return false to discard this connection
             }
             @Override
             public Process onConnected(Connection connection) {
                 return new Process(this, connection) {
                     @Override
                     protected void onSent(ID.MID mid) {}
         
                     @Override
                     protected void onRecved(ID.MID mid, String result, Flags flags) {}
         
                     @Override
                     protected String process(String msg, Flags flags) {
                         return "recved msg: " + msg;
                     }
                 };
             }
         };
         ```

      6. #### Start

         ```java
         server.start();
         ```

         The server will run in the main thread, if you need it non-blocking in a child thread, fill the parameter with *Server.Mode.MT*. 

         ```java
         server.start(Server.Mode.MT);
         ```

         There are 3 modes to start the server. 

         | Name   | Full Name          | Usage                                                        |
         | ------ | ------------------ | ------------------------------------------------------------ |
         | *MT*   | Multi-threads      | Run the server in a child thread.                            |
         | *T*    | Test               | Run the server as a test (only accept from IP that starts with 192.168 or localhost). |
         | *MT_T* | Multi-threads test | Run the server as a test (only accept from IP that starts with 192.168 or localhost) in a child thread. |

      7. #### Specify ActionCallback

         <u>ActionCallback</u> is an interface of periodic callback method. Specifying the periodic callback methods when handling a message instead of recognising by <u>MID</u> inside the default callback methods is much simpler and more practical (at least I think so). 

         Create a new <u>ActionCallback</u> interface: 

         ```java
         Process.ActionCallback actionCallback = new Process.ActionCallback() {
           @Override
           public void onSend() {}
         
           @Override
           public void onSent() {}
         
           @Override
           public void onRecved(String result, Flags flags) {}
         };
         ```

         Override 3 abstract methods: 

         ```java
         abstract void onSent(MID mid)
         abstract String process(String msg, Flags flags)
         abstract void onRecved(MID mid, String result, Flags flags)
         ```

         It's easy to find out that these methods in an <u>ActionCallback</u> interface are part of the callback methods of a <u>Process</u> object, and its function and lifecycle are consistent with those in a <u>Process</u> object. 

         ```java
         process.specifyActionCallback(mid, actionCallback);
         ```

      8. #### Interrupt

         ```java
         server.interrupt();
         ```

         This method will directly stop all the process and stop listening the port immediately without blocking. 

      9. #### Stop

         ```java
         server.stop();
         ```

         This method will stop listening the port. As long as all the process have ended, the <u>Server</u> will no longer be maintained. 

   2. ### Client

   3. ### Transition Server

