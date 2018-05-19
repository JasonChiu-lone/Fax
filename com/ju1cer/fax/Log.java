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
		fw.write("<========================�յ�һ�����ʼ�========================>\r\n");
		GUI.setText.print("====�յ�һ�����ʼ�====");
		fw.write("�����ˣ�" + getFrom(message) + "\r\n");
		GUI.setText.print("�����ˣ�" + getFrom(message) + "");
		fw.write("�ʼ����⣺" + getSubject(message) + "\r\n");
		GUI.setText.print("�ʼ����⣺" + getSubject(message) + "");
		fw.write("���ݣ�" + getMailContent((Part) message) + "\r\n");
		GUI.setText.print("���ݣ�" + getMailContent((Part) message) + "");
		fw.write("����ʱ�䣺" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(((MimeMessage) message).getSentDate()) + "\r\n");
		
		if(haveContent) {
			fw.write("���ڸ�����");
			GUI.setText.print("���ڸ�����");
			Part part = (Part) message;
			if (part.isMimeType("multipart/*")) {      
				Multipart mp = (Multipart) part.getContent();
				System.out.println("����������" + (mp.getCount()-1));
				for (int i = 1; i < mp.getCount(); i++) {  
            	BodyPart mpart = mp.getBodyPart(i);
            	fw.write(MimeUtility.decodeText(mpart.getFileName()) + " ");
            	GUI.setText.print(MimeUtility.decodeText(mpart.getFileName()));
				}
			}
		}
		else {
				fw.write("�޸���");
				GUI.setText.print("�޸���");
			}
		fw.write("\r\n");
		fw.write("��ǰϵͳʱ�䣺" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date().getTime()) + "\r\n");
		fw.close();
	}
	
	public static void generateE(Exception e) throws IOException {
		File log = new File("Fax.log");
		if(!log.exists())
			log.createNewFile();
		FileWriter fw = new FileWriter("Fax.log", true);
		fw.write("<========================��������========================>\r\n");
		GUI.setText.print("====��������====");
		fw.write(e + "\r\n");
		GUI.setText.print(e.toString());
		fw.write("��ǰϵͳʱ�䣺" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date().getTime()) + "\r\n");
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
            subject = MimeUtility.decodeText(((MimeMessage) message).getSubject());// ���ʼ��������    
        }  
        return subject;      
    }
	
	private static String getMailContent(Part part) throws Exception {      
        StringBuffer bodytext = new StringBuffer();//����ʼ�����  
        //�ж��ʼ�����,��ͬ���Ͳ�����ͬ  
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
