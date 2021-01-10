1. ## 摘要

   *ATCP* 是一个TCP长连接架构，它支持：

   - 端对端通信 (哪怕是2个来自同一IP的客户端)
   - TCP 转发
   - RSA 加密/加签
   - 消息标识
   - 多线程处理
   - 负载均衡
   - 本地数据压缩

2. ## 引用

   下载 jar, java class, py 文件这里. 

3. ## 主要架构

   <img src="/Users/futianhao/Documents/Projects/Projects/ATCP/img/atcp_main_logic.png" alt="200%" style="zoom:200%;" />

   1. ### 消息标识

      <u>Flag</u> 被用来识别一条消息的属性。
      ATCP 提供了2种 <u>Flag</u>: 系统保留的 <u>Flag</u> 和 自定义 <u>Flag</u>. ATCP 总共提供了5个预定义 (系统保留) 的 <u>Flag</u>: *RESULT_NO_NEED*, *KILL*, *RES*, *TO_PID*, *FROM_PID*. 

      | 名字             | 字符串                     | Usage                                  | 用户可调用 | 用户可见 |
      | ---------------- | -------------------------- | -------------------------------------- | ---------- | -------- |
      | *RESULT_NO_NEED* | \\\\res_no_need\\\         | 这条消息不需要回复                     | 真         | 假       |
      | *FROM_PID*       | \\\from_pid\\\: <u>PID</u> | 这是一条来自某 <u>PID</u> 的端对端消息 | 假         | 真       |
      | *TO_PID*         | \\\to_pid\\\\: <u>PID</u>  | 这是一条给某 <u>PID</u> 的消息         | 假         | 假       |
      | *KILL*           | \\\kill\\\                 | 这是最后一条消息                       | 假         | 假       |
      | *RES*            | \\\res\\\                  | 这是回复                               | 假         | 假       |

      <u>Flag</u> 可以拥有值，比如 *Flags.FROM_PID* 和 *Flags.TO_PID*就有。

   2. ### 消息阻塞

      当 *send()* 被调用时，发送的消息会被追加到消息队列，直到上一条消息收到了回复或是消息队列中没有上一条消息之前，它都不会被送出。
      在某些情况下，消息会随着前一条消息同时被发出。这样的情况包括 使用 *Flags.RESULT_NO_NEED* 或是前一条消息是回复 (这种情况不能人为诱发)。

      <u>MID</u> 被用来将回复绑定到所属的原始消息。
      每一条人为发送的消息都拥有一个 <u>MID</u>，当这条消息收到回复的时候方法 *onRecved()* 将被回调。使用 <u>MID</u> 来辨认回复所属的原始消息。

   3. ### 动作回调

      <u>ActionCallback</u> 是一个集合了周期性回调方法的接口。指定处理消息时的周期性回调方法比使用 <u>MID</u> 在默认的回调方法内辨认更加简单和实用 (至少我这样认为)。

4. ## 对象

   1. ### 日志

      *** *indi.atatc.atcp_server.packages.log.Log*。这是一个储存了服务器日志与输入输出流的单例对象。** 

      1. #### 设置调试等级

         ```java
         public enum  DebugLevel {
             INFO, DEBUG, WARNING, ERROR
         }
         ```

         *INFO* < *DEBUG* < *WARNING* < *ERROR*
         只有调试等级小于当前调试等级的消息才会被打印到控制台。

         ```java
         Log.getInstance().debugLevel = Log.DebugLevel.DEBUG;
         ```

         > 将调试等级设为*DEBUG*，于是 <u>Log</u> 只会将调试等级在 *INFO* 至 *DEBUG* (包含 *INFO* 和 *DEBUG*) 的消息打印到控制台。

      2. #### 公布

         最重要的方法就是 *log.publish()*。

         ```java
         Log.getInstance().publish();
         ```

         参数 "message" 可以是 <u>String</u> 类型或是 <u>Exception</u> 类型。
         当是 <u>Exception</u> 类型时，调试等级肯定是*ERROR*。
         当是 <u>String</u> 类型时，必须填入参数 "debugLevel"。
         两种情况都可以指定结尾，默认是 "\n"。

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

   2. ### Values

      *** indi.atatc.atcp_server.packages.data.Values. This is an singleton object which stores global attributes. The attribute names must be <u>String</u>. **

      1. #### Define New Attributes

         To set a new attribute or rewrite the attribute: 

         ```java
         String name = "attribute 1";
         Object value = "value of attribute 1";
         Values.getInstance().put(name, value);
         ```

         Then get the attribute: 

         ```java
         String valueOfAttribute1 = (String) Values.getInstance().get("attribute 1");
         ```

         Method *get()* will throw AccidentEvents.StatusError when there is no such attribute named "attribute 1" (for example). 

      2. #### Pre-defined Attributes

         There are 6 pre-defined attributes: 

         | Name                     | Default Value | Usage                               |
         | ------------------------ | ------------- | ----------------------------------- |
         | "log_path"               | "log.txt"     | The path of the log file.           |
         | "key_length"             | 2048          | The key length of the RSA key pair. |
         | "separator_first_grade"  | "\\\\\\"      |                                     |
         | "separator_second_grade" | '@'           |                                     |
         | "separator_third_grade"  | '#'           |                                     |
         | "separator_flag"         | ':'           |                                     |

5. ## Usage

   ***All the samples are written with JAVA, if you are using Python version, the differences are mentioned in the source code.** 

   1. ### Server

      1. #### Setup Your Configuration

         <div name="setup your configuration">Create a new <u>Configutation</u> object: </div>

         ```java
         Server.Configuration configuration = new Server.Configuration();
         configuration.name = "ATCP Test"; // the server's name
         configuration.port = 2077; // server port, it's 4747 in default
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
         Override 3 abstract methods: 

         ```java
         abstract void onSent(MID mid)
         abstract String process(String msg, Flags flags)
         abstract void onRecved(MID mid, String result, Flags flags)
         ```

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

         You can use

         ```java
         Process.ActionCallback actionCallback = new Process.ActionCallback() {
           @Override
           public void onSend() {}
         
           @Override
           public void onSent() {}
         
           @Override
           public void onRecved(String result, Flags flags) {}
         };
         server.specifyActionCallback(mid, actionCallback);
         ```

         to specify an <u>ActionCallback</u> interface to a message. 
         The ActionCallback interface includes 3 callback methods: 

         ```java
         void onSend()
         void onSend()
         void onRecved(String result, Flags flags)
         ```

         It's easy to find out that these methods in an <u>ActionCallback</u> interface are part of the callback methods of a <u>Process</u> object, and its function and lifecycle are consistent with those in a <u>Process</u> object. 

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

      6. #### Start Server

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

      7. #### Interrupt

         ```java
         server.interrupt();
         ```

         This method will directly stop all the process and stop listening the port immediately without blocking. 

      8. #### Stop

         ```java
         server.stop();
         ```

         This method will stop listening the port. As long as all the process have ended, the <u>Server</u> will no longer be maintained. 

