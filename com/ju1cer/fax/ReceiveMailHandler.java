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
	 * 获取session会话
	 * @return
	 * @throws GeneralSecurityException 
	 */
	private Session getSessionMail() throws GeneralSecurityException {
		Properties properties = System.getProperties(); 
		System.out.println("已获取系统属性");
		GUI.setText.print("已获取系统属性");
        properties.put("mail.stmp.host", Config.MAIL_HOST);  
        System.out.println("已添加host地址");
        GUI.setText.print("已添加host地址");
        properties.put("mail.stmp.auth", Config.MAIL_AUTH);  
        System.out.println("已添加auth认证");
        GUI.setText.print("已添加auth认证");
        MailSSLSocketFactory sf = new MailSSLSocketFactory();
	    sf.setTrustAllHosts(true);
	    properties.put("mail.smtp.ssl.enable", "true");
	    properties.put("mail.smtp.ssl.socketFactory", sf);
	    System.out.println("已添加ssl认证");
	    GUI.setText.print("已添加ssl认证");
        Session sessionMail = Session.getDefaultInstance(properties, null);  
        System.out.println("建立session");
        GUI.setText.print("建立session");
        return sessionMail;
	}
	
	/**
	 * 接收文件
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
			//连接邮箱服务器
			urln = new URLName(Config.MAIL_TYPE, Config.MAIL_HOST, 
					Config.MAIL_PORT, null, username, password);
			store = getSessionMail().getStore(urln);
			System.out.println("配置服务器完成");
			GUI.setText.print("配置服务器完成");
			System.out.println("尝试连接");
			GUI.setText.print("尝试连接");
			store.connect();
			System.out.println("连接成功");
			GUI.setText.print("连接成功");
			//获得Folder
			folder = store.getFolder("INBOX");
			System.out.println("已获取folder");
			GUI.setText.print("已获取folder");
			//以只读打开
			folder.open(Folder.READ_WRITE);
			System.out.println("已打开folder");
			GUI.setText.print("已打开folder");
			//获取邮件个数
			messageCount = folder.getMessageCount();
			System.out.println("新增" + messageCount + "封未读邮件");
			GUI.setText.print("新增" + messageCount + "封未读邮件");
			//Message[] messages = folder.getMessages(messageCount - unreadMessageCount, messageCount);
			Message[] messages = folder.getMessages(1, messageCount);
			//System.out.println(messages[0]);
			for(int i = 0;i < messages.length;i++) {
				System.out.println("正在处理第" + (i+1) + "封邮件");
				GUI.setText.print("正在处理第" + (i+1) + "封邮件");
				if(isContainAttach((Part) messages[i])) {
					System.out.println("有附件");
					GUI.setText.print("");
					//存在附件
					//保存附件
					saveAttach((Part) messages[i], Config.MAIL_ATTACH_PATH);
					Log.generateLog(messages[i], true);
				}
				else {
					System.out.println("无附件");
					GUI.setText.print("无附件");
					//不存在附件
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
		System.out.println("收件箱已清空");
		GUI.setText.print("收件箱已清空");
	}

	/**
	 * 判断是否包含附件
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
        //保存附件到服务器本地  
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
        System.out.println("附件已保存");
        GUI.setText.print("附件已保存");
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
