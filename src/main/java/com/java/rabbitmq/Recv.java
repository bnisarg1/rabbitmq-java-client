package com.java.rabbitmq;

import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.DeliverCallback;

import java.util.concurrent.TimeoutException;

import com.rabbitmq.client.Channel;

public class Recv {
	
	private final static String QUEUE_NAME = "hello";

	public static void main(String[] args) throws java.io.IOException,
    java.lang.InterruptedException, TimeoutException {
		
		ConnectionFactory factory=  new ConnectionFactory();
		factory.setHost("localhost");
		Connection connection= factory.newConnection();
		Channel channel= connection.createChannel();
		channel.queueDeclare(QUEUE_NAME, false, false, false, null);
	    System.out.println(" [*] Waiting for messages. To exit press CTRL+C");
	    
	    
	    
	    DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), "UTF-8");
            System.out.println(" [x] Received '" + message + "'");
        };
        channel.basicConsume(QUEUE_NAME, true, deliverCallback, consumerTag -> { });
    }
		

}
