package com.ju1cer.fax;

import java.awt.Button;
import java.awt.Frame;
import java.awt.Panel;
import java.awt.TextArea;
import java.awt.TextArea;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.io.OutputStream;

import javax.swing.JOptionPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

public class Main {
	private static String username;
	private static String password;
	private static int t = 5000;
	private static boolean working = false;
	public static void main(String args[]) throws IOException, InterruptedException {
		username = (String) JOptionPane.showInputDialog(null,"邮箱地址：\n","Fax",JOptionPane.PLAIN_MESSAGE, null, null, "example@example.com");
		if(username == null)
			System.exit(0);
		password = (String) JOptionPane.showInputDialog(null,"授权码：\n","Fax",JOptionPane.PLAIN_MESSAGE, null, null, "授权码不等同于邮箱密码！！！");
		if(password == null)
			System.exit(0);
		//zhaozijunvip@yeah.net xyz1234
		GUI gui = new GUI();
		gui.addWindowListener(new WindowAdapter(){
			public void windowClosing(WindowEvent e){
			       System.exit(0);
			   }
			});
		while(true) {
			if(working)
				start();
			Thread.sleep(t);
		}
	}
	
	public static void start() throws IOException {
		try {  
            new ReceiveMailHandler().receiveMail(username, password);  
        } catch (Exception e) {  
            // TODO Auto-generated catch block  
            Log.generateE(e);
        }
	}
	
	public static void change() throws IOException {
		if(!working)
			start();
		working = !working;
	}
	
	public static void set(int t_) {
		t = t_;
	}
}
class GUI extends Frame{
	private static boolean working = false;
	private Button button = new Button("开始接收");
	private Button set = new Button("应用");
	private TextArea ta = new TextArea("5000", 0, 0, TextArea.SCROLLBARS_NONE);
	private TextArea ta_notice = new TextArea("设置冷却时间（单位：ms）", 0, 0, TextArea.SCROLLBARS_NONE);
	private static TextArea ta_console = new TextArea("Fax v1.0\nAuthor:Ju1cer\ncopyright 2018\n", 0, 0, TextArea.SCROLLBARS_VERTICAL_ONLY);
	private static String console = "Fax v1.0\nAuthor:Ju1cer\ncopyright 2018\n";
	public GUI() {
		this.setSize(320, 260);
		this.setTitle("Fax");
		this.setVisible(true);
		this.setLayout(null);
		this.setResizable(false);
		button.setBounds(240, 130, 80, 130);
		set.setBounds(160, 130, 80, 130);
		ta.setBounds(160, 26, 160, 105);
		ta.setEditable(true);
		ta.setVisible(true);
		ta_notice.setEditable(false);
		ta_notice.setBounds(160, 130, 160, 30);
		ta_notice.setVisible(true);
		ta_console.setEditable(false);
		//ta_console.setText(console);
		ta_console.setBounds(2, 26, 160, 232);
		ta_console.setVisible(true);
		//ta_notice.
		//Button button = new Button("开始接收");
		this.add(ta_console);
		this.add(ta_notice);
		this.add(set);
		this.add(button);
		this.add(ta);
		
		button.addActionListener(new Monitor1());
		set.addActionListener(new Monitor2());
	}
	
	public class Monitor1 implements ActionListener{
		@Override
		public void actionPerformed(ActionEvent e) {
			try {
				working = !working;
				if(working)
					button.setLabel("停止接收");
				else
					button.setLabel("开始接收");
				Main.change();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
	}
	public class Monitor2 implements ActionListener{
		@Override
		public void actionPerformed(ActionEvent e) {
			Main.set(Integer.valueOf(ta.getText()));
		}
	}
	public static class setText{
		public static void print(String str) {
			console += str + "\n";
			ta_console.setText(console);
			ta_console.setCaretPosition(console.length());
		}
	}
}