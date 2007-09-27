/*
 * Copyright 2005-2007 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package sdloader.desktop;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Label;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

import sdloader.LifecycleEvent;
import sdloader.LifecycleListener;
import sdloader.SDLoader;
import sdloader.util.MiscUtils;
import sdloader.util.ResourceUtil;
/**
 * Swingで起動するSDLoaderのUI
 * @author AKatayama
 *
 */
public class SwingUI extends JFrame{

	private SDLoader server;
	private Properties appProperties;
	
	public static void main(String[] args){
		SwingUI ui = new SwingUI();
		try{
			ui.start();
		}catch(Throwable t){
			JOptionPane.showMessageDialog(ui,t.getMessage());
			System.exit(-1);
		}
	}

	public void start(){
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(300,200);
		setLocationRelativeTo(null);//画面の真ん中
		
		InputStream app = ResourceUtil.getResourceAsStream("application.properties",SwingUI.class);
		if(app == null){
			throw new RuntimeException("application.propertiesがありません");
		}
		appProperties = new Properties();
		try{
			appProperties.load(app);
		}catch(IOException ioe){
			throw new RuntimeException("application.propertiesがありません");
		}		
		String title = appProperties.getProperty("title","SDLoaderDesktop");
		setTitle(title);
	
		getContentPane().add(new Label("起動中です・・・"),BorderLayout.CENTER);		
		setVisible(true);
		
		server = new SDLoader();
		server.setAutoPortDetect(true);
		server.addEventListener(LifecycleEvent.AFTER_START,new LifecycleListener(){
			public void handleLifecycle(LifecycleEvent arg0) {
				showUI();
			}
		});
		try{
			server.start();
		}catch(Exception e){
			e.printStackTrace();
			throw new RuntimeException("エラーが発生しました。\n"+e.getMessage());
		}
	}
	private void showUI(){
		this.getContentPane().removeAll();
		addCloseHandler();
		addAppButton();
		this.getContentPane().validate();
	}
	/**
	 * application.propertiesに基づき、アプリケーション起動ボタンを追加します。
	 */
	private void addAppButton(){
		int port = server.getPort();
		final String baseurl = "http://localhost:"+port;
		String prefix = "app";		
		getContentPane().setLayout(new GridBagLayout());
		GridBagConstraints constraint = new GridBagConstraints();
		constraint.gridx=0;
		constraint.gridy=0;
		constraint.fill=GridBagConstraints.HORIZONTAL;
		getContentPane().add(new Label("起動したいアプリケーションをクリックして下さい。"),constraint);
		
		for(int i = 1; ;i++){
			String name = appProperties.getProperty(prefix+i+".name");
			final String url = appProperties.getProperty(prefix+i+".url");	
			final String autorun = appProperties.getProperty(prefix+i+".autorun","false");
			if(name != null && url != null){
				JButton button =new JButton(name);
				constraint = new GridBagConstraints();
				constraint.gridx=0;
				constraint.gridy=i;
				constraint.fill=GridBagConstraints.BOTH;
				constraint.insets = new Insets(4,4,4,4);
				constraint.weighty=100;
				constraint.weightx=100;
				getContentPane().add(button,constraint);
				button.addActionListener(new ActionListener(){
					public void actionPerformed(ActionEvent e) {
						try{
							MiscUtils.openBrowser(baseurl+url);
						}catch(IOException ioe){
							JOptionPane.showMessageDialog(SwingUI.this,url+"の起動に失敗しました。");
						}
					}
				});
				if(autorun.equals("true")){
					try{
						MiscUtils.openBrowser(baseurl+url);
					}catch(IOException ioe){
						JOptionPane.showMessageDialog(SwingUI.this,url+"の起動に失敗しました。");
					}
				}
			}else{
				break;
			}
		}
	}
	private void addCloseHandler(){
		this.addWindowListener(new WindowAdapter(){
			@Override
			public void windowClosing(WindowEvent e) {
				if(server != null){
					server.stop();
				}
			}
		});
	}
}
