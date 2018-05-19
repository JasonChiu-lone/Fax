/**
 * 
 */
package com.ju1cer.fax;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.BodyPart;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.URLName;
import javax.mail.internet.MimeUtility;

import com.sun.mail.util.MailSSLSocketFactory;

/**
 * @author 15910
 *
 */
public class ReceiveMailHandler {
	/**
	 * ��ȡsession�Ự
	 * @return
	 * @throws GeneralSecurityException 
	 */
	private Session getSessionMail() throws GeneralSecurityException {
		Properties properties = System.getProperties(); 
		System.out.println("�ѻ�ȡϵͳ����");
		GUI.setText.print("�ѻ�ȡϵͳ����");
        properties.put("mail.stmp.host", Config.MAIL_HOST);  
        System.out.println("�����host��ַ");
        GUI.setText.print("�����host��ַ");
        properties.put("mail.stmp.auth", Config.MAIL_AUTH);  
        System.out.println("�����auth��֤");
        GUI.setText.print("�����auth��֤");
        MailSSLSocketFactory sf = new MailSSLSocketFactory();
	    sf.setTrustAllHosts(true);
	    properties.put("mail.smtp.ssl.enable", "true");
	    properties.put("mail.smtp.ssl.socketFactory", sf);
	    System.out.println("�����ssl��֤");
	    GUI.setText.print("�����ssl��֤");
        Session sessionMail = Session.getDefaultInstance(properties, null);  
        System.out.println("����session");
        GUI.setText.print("����session");
        return sessionMail;
	}
	
	/**
	 * �����ļ�
	 * @param username
	 * @param password
	 * @throws IOException 
	 */
	public void receiveMail(String username, String password) throws IOException {
		Store store = null;
		Folder folder = null;
		int messageCount = 0;
		URLName urln = null;
		try {
			//�������������
			urln = new URLName(Config.MAIL_TYPE, Config.MAIL_HOST, 
					Config.MAIL_PORT, null, username, password);
			store = getSessionMail().getStore(urln);
			System.out.println("���÷��������");
			GUI.setText.print("���÷��������");
			System.out.println("��������");
			GUI.setText.print("��������");
			store.connect();
			System.out.println("���ӳɹ�");
			GUI.setText.print("���ӳɹ�");
			//���Folder
			folder = store.getFolder("INBOX");
			System.out.println("�ѻ�ȡfolder");
			GUI.setText.print("�ѻ�ȡfolder");
			//��ֻ����
			folder.open(Folder.READ_WRITE);
			System.out.println("�Ѵ�folder");
			GUI.setText.print("�Ѵ�folder");
			//��ȡ�ʼ�����
			messageCount = folder.getMessageCount();
			System.out.println("����" + messageCount + "��δ���ʼ�");
			GUI.setText.print("����" + messageCount + "��δ���ʼ�");
			//Message[] messages = folder.getMessages(messageCount - unreadMessageCount, messageCount);
			Message[] messages = folder.getMessages(1, messageCount);
			//System.out.println(messages[0]);
			for(int i = 0;i < messages.length;i++) {
				System.out.println("���ڴ����" + (i+1) + "���ʼ�");
				GUI.setText.print("���ڴ����" + (i+1) + "���ʼ�");
				if(isContainAttach((Part) messages[i])) {
					System.out.println("�и���");
					GUI.setText.print("");
					//���ڸ���
					//���渽��
					saveAttach((Part) messages[i], Config.MAIL_ATTACH_PATH);
					Log.generateLog(messages[i], true);
				}
				else {
					System.out.println("�޸���");
					GUI.setText.print("�޸���");
					//�����ڸ���
					Log.generateLog(messages[i], false);
				}
			}
			deleteMessages(messages);
		}
		catch(Exception e) {
			Log.generateE(e);
			//e.printStackTrace();
		}
		finally {
			try {
				folder.close(true);
			} catch (MessagingException e) {
				// TODO Auto-generated catch block
				Log.generateE(e);
			}
			try {
				store.close();
			} catch (MessagingException e) {
				// TODO Auto-generated catch block
				Log.generateE(e);
			}
		}
	}
	
	private void deleteMessages(Message[] messages) throws IOException {
		// TODO Auto-generated method stub
		for (int i = 0, count = messages.length; i < count; i++) {  
			try {
            Message message = messages[i];
            // set the DELETE flag to true
            message.setFlag(Flags.Flag.DELETED, true);
			}
			catch(MessagingException e) {
				Log.generateE(e);
			}
		}
		System.out.println("�ռ��������");
		GUI.setText.print("�ռ��������");
	}

	/**
	 * �ж��Ƿ��������
	 * @param part
	 * @return
	 * @throws Exception
	 */
	public boolean isContainAttach(Part part) throws Exception {   
        boolean attachflag = false;      
        if (part.isMimeType("multipart/*")) {      
            Multipart mp = (Multipart) part.getContent();      
            for (int i = 0; i < mp.getCount(); i++) {      
                BodyPart mpart = mp.getBodyPart(i);      
                String disposition = mpart.getDisposition();      
                if ((disposition != null) && ((disposition.equals(Part.ATTACHMENT)) || (disposition.equals(Part.INLINE))))      
                    attachflag = true;      
                else if (mpart.isMimeType("multipart/*")) {      
                    attachflag = isContainAttach((Part) mpart);      
                } else {      
                    String contype = mpart.getContentType();      
                    if (contype.toLowerCase().indexOf("application") != -1)      
                        attachflag = true;      
                    if (contype.toLowerCase().indexOf("name") != -1)      
                        attachflag = true;      
                }      
            }      
        } else if (part.isMimeType("message/rfc822")) {      
            attachflag = isContainAttach((Part) part.getContent());      
        }      
        return attachflag;      
    }
	
	private void saveAttach(Part part,String filePath) throws Exception {      
        String fileName = "";  
        //���渽��������������  
        if (part.isMimeType("multipart/*")) {      
            Multipart mp = (Multipart) part.getContent();  
            for (int i = 0; i < mp.getCount(); i++) {  
                BodyPart mpart = mp.getBodyPart(i);      
                String disposition = mpart.getDisposition();     
                if ((disposition != null) && ((disposition.equals(Part.ATTACHMENT)) || (disposition.equals(Part.INLINE)))) {     
                    fileName = mpart.getFileName();      
                    if (fileName != null) {  
                        fileName = MimeUtility.decodeText(fileName);  
                        saveFile(fileName, mpart.getInputStream(),filePath);    
                    }    
                } //else if (mpart.isMimeType("multipart/*")) {      
                    //saveAttach(mpart,filePath);  
                //} 
            else {  
                    fileName = mpart.getFileName();      
                    if (fileName != null) {  
                        fileName = MimeUtility.decodeText(fileName);  
                        saveFile(fileName, mpart.getInputStream(),filePath);  
                    }      
                }      
            }      
        } else if (part.isMimeType("message/rfc822")) {      
            saveAttach((Part) part.getContent(),filePath);      
        }  
        System.out.println("�����ѱ���");
        GUI.setText.print("�����ѱ���");
    }
	
	private void saveFile(String fileName, InputStream in,String filePath) throws Exception {      
        File storefile = new File(filePath);     
        if(!storefile.exists()){  
            storefile.mkdirs();  
        }  
        BufferedOutputStream bos = null;     
        BufferedInputStream bis = null;     
        try {     
            bos = new BufferedOutputStream(new FileOutputStream(filePath + fileName));     
            bis = new BufferedInputStream(in);     
            int c;     
            while ((c = bis.read()) != -1) {     
                bos.write(c);     
                bos.flush();     
            }     
        } catch (Exception e) {     
            throw e;     
        } finally {  
            if(bos != null){  
                bos.close();  
            }  
            if(bis != null){  
                bis.close();  
            }  
        }     
    }
}
