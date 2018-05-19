package com.ju1cer.fax;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeUtility;

public class Log {
	public static void generateLog(Message message, boolean haveContent) throws Exception {
		String[] fileName = null;
		
		File log = new File("Fax.log");
		if(!log.exists())
			log.createNewFile();
		FileWriter fw = new FileWriter("Fax.log", true);
		fw.write("<========================收到一封新邮件========================>\r\n");
		GUI.setText.print("====收到一封新邮件====");
		fw.write("发件人：" + getFrom(message) + "\r\n");
		GUI.setText.print("发件人：" + getFrom(message) + "");
		fw.write("邮件主题：" + getSubject(message) + "\r\n");
		GUI.setText.print("邮件主题：" + getSubject(message) + "");
		fw.write("内容：" + getMailContent((Part) message) + "\r\n");
		GUI.setText.print("内容：" + getMailContent((Part) message) + "");
		fw.write("发送时间：" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(((MimeMessage) message).getSentDate()) + "\r\n");
		
		if(haveContent) {
			fw.write("存在附件：");
			GUI.setText.print("存在附件：");
			Part part = (Part) message;
			if (part.isMimeType("multipart/*")) {      
				Multipart mp = (Multipart) part.getContent();
				System.out.println("附件数量：" + (mp.getCount()-1));
				for (int i = 1; i < mp.getCount(); i++) {  
            	BodyPart mpart = mp.getBodyPart(i);
            	fw.write(MimeUtility.decodeText(mpart.getFileName()) + " ");
            	GUI.setText.print(MimeUtility.decodeText(mpart.getFileName()));
				}
			}
		}
		else {
				fw.write("无附件");
				GUI.setText.print("无附件");
			}
		fw.write("\r\n");
		fw.write("当前系统时间：" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date().getTime()) + "\r\n");
		fw.close();
	}
	
	public static void generateE(Exception e) throws IOException {
		File log = new File("Fax.log");
		if(!log.exists())
			log.createNewFile();
		FileWriter fw = new FileWriter("Fax.log", true);
		fw.write("<========================发生错误========================>\r\n");
		GUI.setText.print("====发生错误====");
		fw.write(e + "\r\n");
		GUI.setText.print(e.toString());
		fw.write("当前系统时间：" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date().getTime()) + "\r\n");
		fw.close();
	}
	
	private static String getFrom(Message message) throws Exception {    
        InternetAddress[] address = (InternetAddress[]) ((MimeMessage) message).getFrom();      
        String from = address[0].getAddress();      
        if (from == null){  
            from = "";  
        }  
        return from;      
    }
	
	private static String getSubject(Message message) throws Exception {  
        String subject = "";  
        if(((MimeMessage) message).getSubject() != null){  
            subject = MimeUtility.decodeText(((MimeMessage) message).getSubject());// 将邮件主题解码    
        }  
        return subject;      
    }
	
	private static String getMailContent(Part part) throws Exception {      
        StringBuffer bodytext = new StringBuffer();//存放邮件内容  
        //判断邮件类型,不同类型操作不同  
        if (part.isMimeType("text/plain")) {      
            bodytext.append((String) part.getContent());      
        } else if (part.isMimeType("text/html")) {      
            bodytext.append((String) part.getContent());      
        } else if (part.isMimeType("multipart/*")) {      
            Multipart multipart = (Multipart) part.getContent();      
            int counts = multipart.getCount();      
            for (int i = 0; i < counts; i++) {      
                getMailContent(multipart.getBodyPart(i));      
            }      
        } else if (part.isMimeType("message/rfc822")) {      
            getMailContent((Part) part.getContent());      
        } else {}      
        return bodytext.toString();  
    }
}
