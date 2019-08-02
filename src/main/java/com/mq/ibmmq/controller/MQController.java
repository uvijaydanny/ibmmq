package com.mq.ibmmq.controller;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

import javax.jms.Message;
import javax.jms.TextMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.JmsException;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.support.JmsHeaders;
import org.springframework.jms.support.JmsMessageHeaderAccessor;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ibm.mq.MQMessage;

@RestController
public class MQController {
	
	private static Logger log = LoggerFactory.getLogger(MQController.class);
	
	@Autowired
	JmsTemplate jms;
	
	@GetMapping("/send")
	public String sendMsg(@RequestParam String msg, String id) {
		try {
			jms.convertAndSend("DEV.QUEUE.1", msg, m ->{
//				String uid = UUID.randomUUID().toString();
//				System.out.println("Correlation id =" + uid);
				m.setJMSCorrelationID(id);
				return m;
			});
//			jms.send("DEV.QUEUE.1", (MessageCreator) mqmsg);
//		jms.convertAndSend("DEV.QUEUE.1",mqmsg);
		}
		catch(JmsException ex){
        ex.printStackTrace();
        return "FAIL";
    }
		return "SUCCESS";
	}

	@GetMapping("/recv")
	public Object receiveMsg(@RequestParam String id) {
		System.out.println("Id value received " + id);
		try {
		return jms.receiveSelectedAndConvert("DEV.QUEUE.1", "JMSCorrelationID='" + id + "'");
		}
		catch(JmsException ex) {
			ex.printStackTrace();
			return "FAIL";
		}
	}
	
//	@GetMapping("/recv")
//	@JmsListener(destination = "DEV.QUEUE.1")
    public void receiveMessage(@Payload String rcmsg,
                               @Header(JmsHeaders.CORRELATION_ID) String correlationId,
                               @Header(name = "jms-header-not-exists", defaultValue = "default") String nonExistingHeader,
                               @Headers Map<String, Object> headers,
                               MessageHeaders messageHeaders,
                               JmsMessageHeaderAccessor jmsMessageHeaderAccessor) {

        log.info("received <" + rcmsg + ">");

        log.info("\n# Spring JMS accessing single header property");
        log.info("- jms_correlationId=" + correlationId);
        log.info("- jms-header-not-exists=" + nonExistingHeader);

        log.info("\n# Spring JMS retrieving all header properties using Map<String, Object>");
        log.info("- jms-custom-header=" + String.valueOf(headers.get("jms-custom-property")));

        log.info("\n# Spring JMS retrieving all header properties MessageHeaders");
        log.info("- jms-custom-property-price=" + messageHeaders.get("jms-custom-property-price", Double.class));

        log.info("\n# Spring JMS retrieving all header properties JmsMessageHeaderAccessor");
        log.info("- jms_destination=" + jmsMessageHeaderAccessor.getDestination());
        log.info("- jms_priority=" + jmsMessageHeaderAccessor.getPriority());
        log.info("- jms_timestamp=" + jmsMessageHeaderAccessor.getTimestamp());
        log.info("- jms_type=" + jmsMessageHeaderAccessor.getType());
        log.info("- jms_redelivered=" + jmsMessageHeaderAccessor.getRedelivered());
        log.info("- jms_replyTo=" + jmsMessageHeaderAccessor.getReplyTo());
        log.info("- jms_correlationId=" + jmsMessageHeaderAccessor.getCorrelationId());
        log.info("- jms_contentType=" + jmsMessageHeaderAccessor.getContentType());
        log.info("- jms_expiration=" + jmsMessageHeaderAccessor.getExpiration());
        log.info("- jms_messageId=" + jmsMessageHeaderAccessor.getMessageId());
        log.info("- jms_deliveryMode=" + jmsMessageHeaderAccessor.getDeliveryMode() + "\n");

    }
	
	private MQMessage createMQMessage(String corrID){

        MQMessage message = new MQMessage();
//        message.messageFlags = MQConstants.MQMD_STRUC_ID;

        if (corrID != null) {

//            message.messageType = MQConstants.MQMD_;
//            message.replyToQueueManagerName = ackQueueManagerName;
//            message.replyToQueueName = ackQueueName;
//            message.report = MQConstants.MQRO_COA | MQConstants.MQRO_COD;
            message.correlationId = corrID.getBytes();
            try {
				message.writeString("something");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}



        }
        return message;
    }
	
	/*private MessageCreator createMessageCreator(String request, String correlationId, Destination replyToQueue) {
        return new MessageCreator() {
            @Override
            public TextMessage createMessage(Session session) throws JMSException {                
                TextMessage message = session.createTextMessage();
                message.setStringProperty("JMS_IBM_Format", MQConstants. MQFMT_STRING);
                message.setStringProperty(JmsHeaders.MESSAGE_ID, correlationId);
                message.setStringProperty(JmsHeaders.CORRELATION_ID, correlationId);
                message.setJMSReplyTo(replyToQueue);
                message.setText(request);          
                return message;
            }
        };
    }*/
}
