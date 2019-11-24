# rabbitmq-java-client

“Hello World”
(using the Java Client)
In this part of the tutorial we’ll write two programs in Java; a producer that sends a single message, and a consumer that receives messages and prints them out. We’ll gloss over some of the detail in the Java API, concentrating on this very simple thing just to get started. It’s a “Hello World” of messaging.

In the diagram below, “P” is our producer and “C” is our consumer. The box in the middle is a queue - a message buffer that RabbitMQ keeps on behalf of the consumer.

(P) -> [|||] -> (C)

The Java client library
RabbitMQ speaks AMQP, which is an open,
general-purpose protocol for messaging. You will need to install
the Java client package for Pivotal RabbitMQ to progress with
these tutorials.
Now we have the Java client and its dependencies, we can write some code.

Sending
(P) -> [|||]

We’ll call our message sender Send and our message receiver Recv. The sender will connect to RabbitMQ, send a single message, then exit.

In Send.java, we need some classes imported:

import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Channel;
Set up the class and name the queue:

public class Send {

  private final static String QUEUE_NAME = "hello";

  public static void main(String[] argv)
      throws java.io.IOException {
      ...
  }
}
then we can create a connection to the server:

    ConnectionFactory factory = new ConnectionFactory();
    factory.setHost("localhost");
    Connection connection = factory.newConnection();
    Channel channel = connection.createChannel();
The connection abstracts the socket connection, and takes care of protocol version negotiation and authentication and so on for us. Here we connect to a broker on the local machine - hence the localhost. If we wanted to connect to a broker on a different machine we’d simply specify its name or IP address here.

Next we create a channel, which is where most of the API for getting things done resides.

To send, we must declare a queue for us to send to; then we can publish a message to the queue:

    channel.queueDeclare(QUEUE_NAME, false, false, false, null);
    String message = "Hello World!";
    channel.basicPublish("", QUEUE_NAME, null, message.getBytes());
    System.out.println(" [x] Sent '" + message + "'");
Declaring a queue is idempotent - it will only be created if it doesn’t exist already. The message content is a byte array, so you can encode whatever you like there.

Lastly, we close the channel and the connection;

    channel.close();
    connection.close();
Here’s the whole Send.java class.

Sending doesn't work!
If this is your first time using RabbitMQ and you don't see the "Sent"
message then you may be left scratching your head wondering what could
be wrong. Maybe the broker was started without enough free disk space
(by default it needs at least 1Gb free) and is therefore refusing to
accept messages. Check the broker logfile to confirm and reduce the
limit if necessary. The configuration
file documentation will show you how to set disk_free_limit.
Receiving
That’s it for our sender. Our receiver is pushed messages from RabbitMQ, so unlike the sender which publishes a single message, we’ll keep it running to listen for messages and print them out.

[|||] -> (C)

The code (in Recv.java) has almost the same imports as Send:

import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.QueueingConsumer;
The extra QueueingConsumer is a class we’ll use to buffer the messages pushed to us by the server.

Setting up is the same as the sender; we open a connection and a channel, and declare the queue from which we’re going to consume. Note this matches up with the queue that send publishes to.

public class Recv {

  private final static String QUEUE_NAME = "hello";

  public static void main(String[] argv)
      throws java.io.IOException,
             java.lang.InterruptedException {

    ConnectionFactory factory = new ConnectionFactory();
    factory.setHost("localhost");
    Connection connection = factory.newConnection();
    Channel channel = connection.createChannel();

    channel.queueDeclare(QUEUE_NAME, false, false, false, null);
    System.out.println(" [*] Waiting for messages. To exit press CTRL+C");
    ...
    }
}
Note that we declare the queue here, as well. Because we might start the receiver before the sender, we want to make sure the queue exists before we try to consume messages from it.

We’re about to tell the server to deliver us the messages from the queue. Since it will push us messages asynchronously, we provide a callback in the form of an object that will buffer the messages until we’re ready to use them. That is what QueueingConsumer does.

    QueueingConsumer consumer = new QueueingConsumer(channel);
    channel.basicConsume(QUEUE_NAME, true, consumer);

    while (true) {
      QueueingConsumer.Delivery delivery = consumer.nextDelivery();
      String message = new String(delivery.getBody());
      System.out.println(" [x] Received '" + message + "'");
    }
QueueingConsumer.nextDelivery() blocks until another message has been delivered from the server.

Here’s the whole Recv.java class.

Putting it all together
You can compile both of these with just the RabbitMQ java client on the classpath:

$ javac -cp rabbitmq-client.jar Send.java Recv.java
To run them, you’ll need rabbitmq-client.jar and its dependencies on the classpath. In a terminal, run the sender:

$ java -cp .:commons-io-1.2.jar:commons-cli-1.1.jar:rabbitmq-client.jar Send
then, run the receiver:

$ java -cp .:commons-io-1.2.jar:commons-cli-1.1.jar:rabbitmq-client.jar Recv
On Windows, use a semicolon instead of a colon to separate items in the classpath.

The receiver will print the message it gets from the sender via RabbitMQ. The receiver will keep running, waiting for messages (Use Ctrl-C to stop it), so try running the sender from another terminal.

If you want to check on the queue, try using rabbitmqctl list_queues.

Hello World!

Time to move on to part 2 and build a simple work queue.

