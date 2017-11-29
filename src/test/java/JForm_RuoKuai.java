import java.awt.EventQueue;
import java.awt.Image;

import javax.imageio.ImageIO;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import java.awt.CardLayout;

import javax.swing.ImageIcon;
import javax.swing.JLayeredPane;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JTextArea;
import javax.swing.JLabel;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.JTextField;
import javax.swing.JSeparator;

import java.awt.Color;

import javax.swing.JButton;
import javax.swing.JTextPane;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;

import javax.swing.JScrollPane;

import java.awt.Toolkit;


public class JForm_RuoKuai {

	private JFrame frame;
	private static JTextField textField;
	private static JTextField txtCcfdcfbbfcec;
	private static JTextField textField_2;
	private static JTextField txtA;
	private static JTextField textField_4;
	private JTextField textField_5;
	private JTextField textField_6;
	private JTextField textField_7;
	private JTextField textField_8;
	private JTextField textField_9;
	private JTextField textField_10;
	private JTextField textField_11;
	private JTextArea textArea;
	DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
	private JTextField txtHttpcaptchaqqcomgetimage;
	private JTextField textField_3;
	private JTextField textField_1;
	private JLabel lblNewLabel_25;
	
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					JForm_RuoKuai window = new JForm_RuoKuai();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public JForm_RuoKuai() {
		initialize();
	}
	
	class XMLResult{
		/**
	     * 解析xml结果
	     * @param xml xml返回结果字符串
	     */
	    public void displayXmlResult(String xml) {
	    	if(xml.length() <= 0) {
	    		textArea.append("未知问题");
	    		return ;
	    	}
	    	Document dm;
			try {
				DocumentBuilder db = dbf.newDocumentBuilder();
				dm = db.parse(new ByteArrayInputStream(xml.getBytes("utf-8")));
				NodeList resultNl = dm.getElementsByTagName("Result");
				NodeList idNl = dm.getElementsByTagName("Id");
				NodeList errorNl = dm.getElementsByTagName("Error");
				
				if(resultNl.getLength() > 0 ) {
					textArea.append(String.format("结果：%s\r\n", resultNl.item(0).getFirstChild().getNodeValue()));
					if(idNl.getLength()>0) textArea.append(String.format("ID:%s\r\n", idNl.item(0).getFirstChild().getNodeValue()));
				} else if (errorNl.getLength() > 0) {
					textArea.append(String.format("错误：%s\r\n", errorNl.item(0).getFirstChild().getNodeValue()));
				} else {
					textArea.append("未知问题\r\n");
				}
		        
			} catch (Exception e) {
				textArea.append("XML解析错误\r\n");
				e.printStackTrace();
			}
	        
	    }
	    
	    /**
	     * URL 验证
	     * @param softId
	     * @param softKey
	     * @param typeid
	     * @param username
	     * @param password
	     * @param url
	     * @return
	     */
	    
	    public  String createByUrl(String softId, String softKey, String typeid, String username, String password, String url) {
			// TODO Auto-generated method stub
			String param = String.format("username=%s&password=%s&typeid=%s&timeout=%s&softid=%s&softkey=%s", username,password,typeid,"90",softId,softKey);
			ByteArrayOutputStream baos = null;
			String ret;
			
			try {
				URL u = new URL(url);
				BufferedImage image = ImageIO.read(u);		
				baos = new ByteArrayOutputStream();
				ImageIO.write(image, "jpg",baos);
				baos.flush();
				byte[] data = baos.toByteArray();
				baos.close();	
				ImageIcon ico = new ImageIcon(data);
		        lblNewLabel_25.setIcon(ico);
				ret=RuoKuai.httpPostImage("http://api.ruokuai.com/create.xml",param,data);
			} catch (Exception e) {
				// TODO: handle exception
				return "未知错误";
			}
			
			return ret;
		}
	   
		
	}
	

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		Image img = Toolkit.getDefaultToolkit ().getImage ("1.png");
		frame = new JFrame("若快答题使用事例程序");
		frame.setIconImage(img);
		frame.setBounds(100, 100, 532, 539);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(new CardLayout(0, 0));
		
		JLayeredPane layeredPane = new JLayeredPane();
		frame.getContentPane().add(layeredPane, "name_3661591840866");
		
		JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		tabbedPane.setBounds(0, 0, 516, 291);
		layeredPane.add(tabbedPane);
		
		JPanel panel_1 = new JPanel();
		tabbedPane.addTab("\u8BF4\u660E", null, panel_1, null);
		
		JTextPane txtpnbugqq = new JTextPane();
		txtpnbugqq.setEditable(false);
		txtpnbugqq.setText("   \u4F7F\u7528\u524D\u8BF7\u5148\u53BB\u8BBE\u7F6E\u9009\u9879\u5361\u586B\u5199\u7B54\u9898(\u6253\u7801)\u5E10\u53F7,\u9664\u4E86\u6CE8\u518C\u529F\u80FD\u4EE5\u5916\u8BBE\u7F6E\u7684\u4FE1\u606F\u5168\u5C40\u901A\u7528.\r\n   \u672C\u793A\u4F8B\u7A0B\u5E8F\u53EA\u5BF9\u82E5\u5FEB\u7B54\u9898(\u6253\u7801)\u63A5\u53E3\u7684\u4F7F\u7528\u505A\u6F14\u793A,\u672C\u7A0B\u5E8F\u53EF\u80FD\u5B58\u5728\u7F3A\u9677\u6216\u8005BUG,\u4E0D\u80FD\u4FDD\u8BC1\u6B64\u793A\u4F8B\u4EE3\u7801\u4E3A\u6700\u4F18\u72B6\u6001.\r\n\u5982\u679C\u5F00\u53D1\u4E2D\u9047\u5230\u4EC0\u4E48\u95EE\u9898\u53EF\u4EE5\u52A0\u6211QQ:606195");
		
		JLabel lblNewLabel_22 = new JLabel("\u5F00\u53D1\u8005\u6587\u6863");
		lblNewLabel_22.setText("<html><A   href='http://wiki.ruokuai.com/'>开发者文档</A></html>");
		
		JLabel lblNewLabel_23 = new JLabel("\u82E5\u5FEB\u7B54\u9898\u5B98\u7F51\u9996\u9875");
		lblNewLabel_23.setText("<html><A   href='http://www.ruokuai.com'>若快答题官网首页</A></html>");
		
		JLabel lblNewLabel_24 = new JLabel("\u5F00\u53D1\u8005\u5E10\u53F7\u548C\u8F6F\u4EF6KEY\u7533\u8BF7");
		lblNewLabel_24.setText("<html><A   href='https://www.ruokuai.com/home/register'>开发者帐号和软件KEY申请</A></html>");
		
		GroupLayout gl_panel_1 = new GroupLayout(panel_1);
		gl_panel_1.setHorizontalGroup(
			gl_panel_1.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_panel_1.createSequentialGroup()
					.addContainerGap(32, Short.MAX_VALUE)
					.addComponent(txtpnbugqq, GroupLayout.PREFERRED_SIZE, 453, GroupLayout.PREFERRED_SIZE)
					.addGap(26))
				.addGroup(gl_panel_1.createSequentialGroup()
					.addGap(202)
					.addComponent(lblNewLabel_22, GroupLayout.PREFERRED_SIZE, 92, GroupLayout.PREFERRED_SIZE)
					.addContainerGap(217, Short.MAX_VALUE))
				.addGroup(gl_panel_1.createSequentialGroup()
					.addGap(189)
					.addComponent(lblNewLabel_23, GroupLayout.PREFERRED_SIZE, 131, GroupLayout.PREFERRED_SIZE)
					.addContainerGap(191, Short.MAX_VALUE))
				.addGroup(gl_panel_1.createSequentialGroup()
					.addGap(173)
					.addComponent(lblNewLabel_24, GroupLayout.PREFERRED_SIZE, 201, GroupLayout.PREFERRED_SIZE)
					.addContainerGap(137, Short.MAX_VALUE))
		);
		gl_panel_1.setVerticalGroup(
			gl_panel_1.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_panel_1.createSequentialGroup()
					.addGap(36)
					.addComponent(txtpnbugqq, GroupLayout.PREFERRED_SIZE, 100, GroupLayout.PREFERRED_SIZE)
					.addGap(18)
					.addComponent(lblNewLabel_22)
					.addGap(18)
					.addComponent(lblNewLabel_23)
					.addGap(18)
					.addComponent(lblNewLabel_24)
					.addContainerGap(27, Short.MAX_VALUE))
		);
		panel_1.setLayout(gl_panel_1);
		
		JPanel panel_2 = new JPanel();
		tabbedPane.addTab("\u8BBE\u7F6E", null, panel_2, null);
		
		JLabel lblNewLabel = new JLabel("\u8F6F\u4EF6\u8BBE\u7F6E");
		
		JLabel lblNewLabel_1 = new JLabel("\u8F6F\u4EF6ID");
		
		JLabel lblNewLabel_2 = new JLabel("\u8F6F\u4EF6KEY");
		
		textField = new JTextField();
		textField.setText("1");
		textField.setColumns(10);
		
		txtCcfdcfbbfcec = new JTextField();
		txtCcfdcfbbfcec.setText("b40ffbee5c1cf4e38028c197eb2fc751");
		txtCcfdcfbbfcec.setColumns(10);
		
		JLabel lblNewLabel_3 = new JLabel("\u9898\u76EE\u7C7B\u578B");
		
		textField_2 = new JTextField();
		textField_2.setText("2040");
		textField_2.setColumns(10);
		
		JLabel lblNewLabel_4 = new JLabel("\u6CE8\u610F:\u8F6F\u4EF6ID\u4E0E\u8F6F\u4EF6KEY\u662F\u5F00\u53D1\u8005\u4ECE\u540E\u53F0\u7533\u8BF7.");
		
		JLabel lblNewLabel_5 = new JLabel("<html><a href=\"\">类型表</a></html");
		
		JSeparator separator = new JSeparator();
		separator.setBackground(Color.BLACK);
		
		JLabel lblNewLabel_6 = new JLabel("\u7528\u6237\u4FE1\u606F");
		
		JLabel lblNewLabel_7 = new JLabel("\u7528\u6237\u540D");
		
		JLabel lblNewLabel_8 = new JLabel("\u5BC6\u7801");
		
		txtA = new JTextField();
		txtA.setColumns(10);
		
		textField_4 = new JTextField();
		textField_4.setColumns(10);
		
		JLabel lblNewLabel_9 = new JLabel("\u6CE8\u610F:\u5F00\u53D1\u8005\u5E10\u53F7\u4E0D\u80FD\u7B54\u9898.\u5FC5\u987B\u662F\u666E\u901A\u5E10\u53F7.");
		GroupLayout gl_panel_2 = new GroupLayout(panel_2);
		gl_panel_2.setHorizontalGroup(
			gl_panel_2.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_panel_2.createSequentialGroup()
					.addGroup(gl_panel_2.createParallelGroup(Alignment.LEADING)
						.addGroup(gl_panel_2.createSequentialGroup()
							.addContainerGap()
							.addComponent(lblNewLabel))
						.addGroup(gl_panel_2.createSequentialGroup()
							.addGap(37)
							.addGroup(gl_panel_2.createParallelGroup(Alignment.LEADING)
								.addGroup(gl_panel_2.createSequentialGroup()
									.addGroup(gl_panel_2.createParallelGroup(Alignment.LEADING)
										.addComponent(lblNewLabel_1)
										.addComponent(lblNewLabel_2))
									.addGap(18)
									.addGroup(gl_panel_2.createParallelGroup(Alignment.LEADING)
										.addGroup(gl_panel_2.createSequentialGroup()
											.addComponent(txtCcfdcfbbfcec, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
											.addGap(18)
											.addComponent(lblNewLabel_4))
										.addComponent(textField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)))
								.addGroup(gl_panel_2.createSequentialGroup()
									.addComponent(lblNewLabel_3)
									.addPreferredGap(ComponentPlacement.UNRELATED)
									.addComponent(textField_2, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
									.addGap(18)
									.addComponent(lblNewLabel_5))))
						.addGroup(gl_panel_2.createSequentialGroup()
							.addGap(11)
							.addComponent(separator, GroupLayout.DEFAULT_SIZE, 490, Short.MAX_VALUE))
						.addGroup(gl_panel_2.createSequentialGroup()
							.addContainerGap()
							.addComponent(lblNewLabel_6))
						.addGroup(gl_panel_2.createSequentialGroup()
							.addGap(38)
							.addGroup(gl_panel_2.createParallelGroup(Alignment.TRAILING)
								.addComponent(lblNewLabel_8)
								.addComponent(lblNewLabel_7))
							.addGap(28)
							.addGroup(gl_panel_2.createParallelGroup(Alignment.LEADING)
								.addComponent(textField_4, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
								.addGroup(gl_panel_2.createSequentialGroup()
									.addComponent(txtA, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
									.addGap(18)
									.addComponent(lblNewLabel_9)))))
					.addContainerGap())
		);
		gl_panel_2.setVerticalGroup(
			gl_panel_2.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_panel_2.createSequentialGroup()
					.addContainerGap()
					.addComponent(lblNewLabel)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addGroup(gl_panel_2.createParallelGroup(Alignment.BASELINE)
						.addComponent(lblNewLabel_1)
						.addComponent(textField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
					.addPreferredGap(ComponentPlacement.RELATED)
					.addGroup(gl_panel_2.createParallelGroup(Alignment.BASELINE)
						.addComponent(lblNewLabel_2)
						.addComponent(txtCcfdcfbbfcec, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(lblNewLabel_4))
					.addGap(7)
					.addGroup(gl_panel_2.createParallelGroup(Alignment.BASELINE)
						.addComponent(lblNewLabel_3)
						.addComponent(textField_2, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(lblNewLabel_5))
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(separator, GroupLayout.PREFERRED_SIZE, 6, GroupLayout.PREFERRED_SIZE)
					.addGroup(gl_panel_2.createParallelGroup(Alignment.LEADING)
						.addGroup(gl_panel_2.createSequentialGroup()
							.addPreferredGap(ComponentPlacement.UNRELATED)
							.addComponent(lblNewLabel_6)
							.addGap(18)
							.addGroup(gl_panel_2.createParallelGroup(Alignment.BASELINE)
								.addComponent(lblNewLabel_7)
								.addComponent(txtA, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
							.addPreferredGap(ComponentPlacement.UNRELATED)
							.addGroup(gl_panel_2.createParallelGroup(Alignment.BASELINE)
								.addComponent(lblNewLabel_8)
								.addComponent(textField_4, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)))
						.addGroup(gl_panel_2.createSequentialGroup()
							.addGap(58)
							.addComponent(lblNewLabel_9)))
					.addContainerGap(48, Short.MAX_VALUE))
		);
		panel_2.setLayout(gl_panel_2);
		
		JPanel panel_3 = new JPanel();
		tabbedPane.addTab("\u7B54\u9898\u76F8\u5173", null, panel_3, null);
		
		JLabel lblNewLabel_10 = new JLabel("\u7B54\u98981(\u4E0A\u4F20)");
		
		JButton btnNewButton = new JButton("\u4E0A\u4F20");
		
		JButton btnNewButton_1 = new JButton("\u8FDC\u7A0B\u4E0B\u8F7D\u9A8C\u8BC1\u7801");
		
		JSeparator separator_1 = new JSeparator();
		separator_1.setBackground(Color.BLACK);
		
		JLabel lblNewLabel_11 = new JLabel("\u7B54\u98982(\u9898\u76EEurl)");
		
		JLabel lblNewLabel_12 = new JLabel("\u56FE\u7247\u5730\u5740");
		
		textField_5 = new JTextField();
		textField_5.setColumns(40);
		
		JButton btnNewButton_2 = new JButton("\u63D0\u4EA4");
		
		JSeparator separator_2 = new JSeparator();
		separator_2.setBackground(Color.BLACK);
		
		JLabel lblNewLabel_13 = new JLabel("\u62A5\u9519");
		
		JLabel lblNewLabel_14 = new JLabel("\u9898\u76EEID");
		
		textField_6 = new JTextField();
		textField_6.setColumns(10);
		
		JButton btnNewButton_3 = new JButton("\u63D0\u4EA4");
		
		lblNewLabel_25 = new JLabel("");
		GroupLayout gl_panel_3 = new GroupLayout(panel_3);
		gl_panel_3.setHorizontalGroup(
			gl_panel_3.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_panel_3.createSequentialGroup()
					.addContainerGap()
					.addGroup(gl_panel_3.createParallelGroup(Alignment.TRAILING)
						.addComponent(lblNewLabel_12)
						.addComponent(lblNewLabel_11))
					.addPreferredGap(ComponentPlacement.UNRELATED)
					.addComponent(textField_5, GroupLayout.PREFERRED_SIZE, 273, GroupLayout.PREFERRED_SIZE)
					.addGap(18)
					.addComponent(btnNewButton_2, GroupLayout.DEFAULT_SIZE, 84, Short.MAX_VALUE)
					.addGap(35))
				.addGroup(gl_panel_3.createSequentialGroup()
					.addContainerGap()
					.addComponent(separator_1, GroupLayout.DEFAULT_SIZE, 494, Short.MAX_VALUE)
					.addContainerGap())
				.addGroup(gl_panel_3.createSequentialGroup()
					.addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
					.addComponent(separator_2, GroupLayout.PREFERRED_SIZE, 494, GroupLayout.PREFERRED_SIZE)
					.addContainerGap())
				.addGroup(gl_panel_3.createSequentialGroup()
					.addContainerGap()
					.addComponent(lblNewLabel_13)
					.addContainerGap(480, Short.MAX_VALUE))
				.addGroup(gl_panel_3.createSequentialGroup()
					.addGap(42)
					.addComponent(lblNewLabel_14)
					.addGap(29)
					.addComponent(textField_6, GroupLayout.PREFERRED_SIZE, 267, GroupLayout.PREFERRED_SIZE)
					.addGap(18)
					.addComponent(btnNewButton_3, GroupLayout.PREFERRED_SIZE, 89, GroupLayout.PREFERRED_SIZE)
					.addContainerGap(33, Short.MAX_VALUE))
				.addGroup(gl_panel_3.createSequentialGroup()
					.addGroup(gl_panel_3.createParallelGroup(Alignment.LEADING)
						.addGroup(gl_panel_3.createSequentialGroup()
							.addGap(38)
							.addComponent(btnNewButton))
						.addGroup(gl_panel_3.createSequentialGroup()
							.addContainerGap()
							.addComponent(lblNewLabel_10)))
					.addGap(45)
					.addComponent(lblNewLabel_25, GroupLayout.PREFERRED_SIZE, 167, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.RELATED, 65, Short.MAX_VALUE)
					.addComponent(btnNewButton_1)
					.addGap(25))
		);
		gl_panel_3.setVerticalGroup(
			gl_panel_3.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_panel_3.createSequentialGroup()
					.addContainerGap()
					.addGroup(gl_panel_3.createParallelGroup(Alignment.TRAILING)
						.addGroup(gl_panel_3.createSequentialGroup()
							.addComponent(lblNewLabel_10)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addGroup(gl_panel_3.createParallelGroup(Alignment.BASELINE)
								.addComponent(btnNewButton)
								.addComponent(btnNewButton_1)))
						.addComponent(lblNewLabel_25, GroupLayout.DEFAULT_SIZE, 44, Short.MAX_VALUE))
					.addPreferredGap(ComponentPlacement.UNRELATED)
					.addComponent(separator_1, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
					.addGap(12)
					.addComponent(lblNewLabel_11)
					.addPreferredGap(ComponentPlacement.UNRELATED)
					.addGroup(gl_panel_3.createParallelGroup(Alignment.BASELINE)
						.addComponent(lblNewLabel_12)
						.addComponent(btnNewButton_2)
						.addComponent(textField_5, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
					.addGap(31)
					.addComponent(separator_2, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.UNRELATED)
					.addComponent(lblNewLabel_13)
					.addPreferredGap(ComponentPlacement.UNRELATED)
					.addGroup(gl_panel_3.createParallelGroup(Alignment.BASELINE)
						.addComponent(lblNewLabel_14)
						.addComponent(textField_6, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(btnNewButton_3))
					.addGap(45))
		);
		panel_3.setLayout(gl_panel_3);
		
		JPanel panel_4 = new JPanel();
		tabbedPane.addTab("\u5E10\u53F7\u76F8\u5173", null, panel_4, null);
		
		JLabel lblNewLabel_15 = new JLabel("\u6CE8\u518C");
		
		JLabel lblNewLabel_16 = new JLabel("\u5E10\u53F7");
		
		textField_7 = new JTextField();
		textField_7.setColumns(10);
		
		JLabel lblNewLabel_17 = new JLabel("\u5BC6\u7801");
		
		textField_8 = new JTextField();
		textField_8.setColumns(15);
		
		JLabel lblNewLabel_18 = new JLabel("\u90AE\u7BB1");
		
		textField_9 = new JTextField();
		textField_9.setColumns(15);
		
		JButton btnNewButton_4 = new JButton("\u6CE8\u518C");
		
		JSeparator separator_3 = new JSeparator();
		separator_3.setForeground(Color.BLACK);
		separator_3.setBackground(Color.BLACK);
		
		JLabel label = new JLabel("\u67E5\u8BE2\u5E10\u53F7\u4FE1\u606F");
		
		JButton btnNewButton_5 = new JButton("\u67E5\u8BE2");
		
		JSeparator separator_4 = new JSeparator();
		separator_4.setBackground(Color.BLACK);
		
		JLabel lblNewLabel_19 = new JLabel("\u5145\u503C");
		
		JLabel lblNewLabel_20 = new JLabel("\u5361\u53F7");
		
		textField_10 = new JTextField();
		textField_10.setColumns(20);
		
		JLabel lblNewLabel_21 = new JLabel("\u5361\u5BC6");
		
		textField_11 = new JTextField();
		textField_11.setColumns(20);
		
		JButton btnNewButton_6 = new JButton("\u5145\u503C");
		GroupLayout gl_panel_4 = new GroupLayout(panel_4);
		gl_panel_4.setHorizontalGroup(
			gl_panel_4.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_panel_4.createSequentialGroup()
					.addGroup(gl_panel_4.createParallelGroup(Alignment.LEADING)
						.addGroup(gl_panel_4.createSequentialGroup()
							.addContainerGap()
							.addComponent(lblNewLabel_15))
						.addGroup(gl_panel_4.createSequentialGroup()
							.addGap(26)
							.addComponent(lblNewLabel_16)
							.addPreferredGap(ComponentPlacement.UNRELATED)
							.addComponent(textField_7, GroupLayout.PREFERRED_SIZE, 103, GroupLayout.PREFERRED_SIZE)
							.addGap(9)
							.addComponent(lblNewLabel_17)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(textField_8, GroupLayout.PREFERRED_SIZE, 125, GroupLayout.PREFERRED_SIZE)
							.addGap(11)
							.addComponent(lblNewLabel_18)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(textField_9, GroupLayout.PREFERRED_SIZE, 102, GroupLayout.PREFERRED_SIZE))
						.addGroup(gl_panel_4.createSequentialGroup()
							.addContainerGap()
							.addComponent(label))
						.addGroup(gl_panel_4.createSequentialGroup()
							.addContainerGap()
							.addComponent(separator_4, GroupLayout.PREFERRED_SIZE, 473, GroupLayout.PREFERRED_SIZE))
						.addGroup(gl_panel_4.createSequentialGroup()
							.addContainerGap()
							.addComponent(separator_3, GroupLayout.PREFERRED_SIZE, 468, GroupLayout.PREFERRED_SIZE))
						.addGroup(gl_panel_4.createSequentialGroup()
							.addGap(177)
							.addComponent(btnNewButton_4, GroupLayout.PREFERRED_SIZE, 88, GroupLayout.PREFERRED_SIZE)))
					.addContainerGap(28, Short.MAX_VALUE))
				.addGroup(gl_panel_4.createSequentialGroup()
					.addGap(17)
					.addComponent(lblNewLabel_20)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(textField_10, GroupLayout.PREFERRED_SIZE, 158, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.RELATED, 10, Short.MAX_VALUE)
					.addComponent(lblNewLabel_21)
					.addGap(18)
					.addComponent(textField_11, GroupLayout.PREFERRED_SIZE, 136, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.UNRELATED)
					.addComponent(btnNewButton_6)
					.addGap(53))
				.addGroup(gl_panel_4.createSequentialGroup()
					.addContainerGap()
					.addComponent(lblNewLabel_19)
					.addContainerGap(477, Short.MAX_VALUE))
				.addGroup(gl_panel_4.createSequentialGroup()
					.addGap(175)
					.addComponent(btnNewButton_5, GroupLayout.PREFERRED_SIZE, 88, GroupLayout.PREFERRED_SIZE)
					.addContainerGap(248, Short.MAX_VALUE))
		);
		gl_panel_4.setVerticalGroup(
			gl_panel_4.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_panel_4.createSequentialGroup()
					.addGroup(gl_panel_4.createParallelGroup(Alignment.LEADING)
						.addGroup(gl_panel_4.createSequentialGroup()
							.addContainerGap(16, Short.MAX_VALUE)
							.addComponent(lblNewLabel_15)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addGroup(gl_panel_4.createParallelGroup(Alignment.BASELINE)
								.addComponent(lblNewLabel_16)
								.addComponent(textField_7, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
							.addGap(41))
						.addGroup(gl_panel_4.createSequentialGroup()
							.addGap(33)
							.addGroup(gl_panel_4.createParallelGroup(Alignment.BASELINE)
								.addComponent(textField_8, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
								.addComponent(lblNewLabel_17))
							.addGap(12)
							.addComponent(btnNewButton_4))
						.addGroup(gl_panel_4.createSequentialGroup()
							.addGap(32)
							.addGroup(gl_panel_4.createParallelGroup(Alignment.BASELINE)
								.addComponent(textField_9, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
								.addComponent(lblNewLabel_18))))
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(separator_3, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
					.addGap(9)
					.addComponent(label)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(btnNewButton_5)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(separator_4, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
					.addGroup(gl_panel_4.createParallelGroup(Alignment.LEADING)
						.addGroup(gl_panel_4.createSequentialGroup()
							.addPreferredGap(ComponentPlacement.UNRELATED)
							.addComponent(lblNewLabel_19)
							.addGap(11)
							.addGroup(gl_panel_4.createParallelGroup(Alignment.BASELINE)
								.addComponent(lblNewLabel_20)
								.addComponent(textField_10, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)))
						.addGroup(gl_panel_4.createSequentialGroup()
							.addGap(37)
							.addGroup(gl_panel_4.createParallelGroup(Alignment.BASELINE)
								.addComponent(lblNewLabel_21)
								.addComponent(textField_11, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
								.addComponent(btnNewButton_6))))
					.addGap(39))
		);
		panel_4.setLayout(gl_panel_4);
		btnNewButton_5.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				btnNewButton_5ActionPerforme(arg0);
			}
/**
 * 
 * @param arg0
 * 
 * 查询
 */
			private void btnNewButton_5ActionPerforme(ActionEvent arg0) {
				// TODO Auto-generated method stub
				String username = txtA.getText();
				String password = textField_4.getText();
				if(username.length()==0 || password.length()==0){
					JOptionPane.showMessageDialog(null, "用户名、密码不能为空", "提示", JOptionPane.INFORMATION_MESSAGE);					
				}
				String result="";
				result = RuoKuai.getInFo(username, password);
		        //textArea.setText(result); 
				try {
					DocumentBuilder db = dbf.newDocumentBuilder();
					try {
						Document dm = db.parse(new ByteArrayInputStream(result.getBytes("utf-8")));
						NodeList resultNl = dm.getElementsByTagName("Score");
						NodeList idNl = dm.getElementsByTagName("HistoryScore");
						NodeList errorNl = dm.getElementsByTagName("TotalTopic");
						NodeList errorNl1 = dm.getElementsByTagName("Error");
						if(resultNl.getLength()>0)
						textArea.append(String.format("快豆：%s\r\n", resultNl.item(0).getFirstChild().getNodeValue()));
						if(idNl.getLength()>0)
						textArea.append(String.format("历史快豆:%s\r\n", idNl.item(0).getFirstChild().getNodeValue()));
						if(errorNl.getLength()>0)
						textArea.append(String.format("答题总数:%s\r\n", errorNl.item(0).getFirstChild().getNodeValue()));
						if(errorNl1.getLength()>0)
						textArea.append(String.format("错误：%s\r\n", errorNl1.item(0).getFirstChild().getNodeValue()));
					} catch (UnsupportedEncodingException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (SAXException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				} catch (ParserConfigurationException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
		
		
		/**
		 * 
		 * 充值
		 * 
		 */
		btnNewButton_6.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				btnNewButton_6Action(e);
			}

			private void btnNewButton_6Action(ActionEvent e) {
				// TODO Auto-generated method stub
				String username = txtA.getText();
				String kid = textField_10.getText();
				String Key = textField_11.getText();
				String ret="";
				try {
					ret=RuoKuai.chongzhi(username,kid,Key);
				} catch (Exception e2) {
					// TODO: handle exception
					return;
				}
				new XMLResult().displayXmlResult(ret);
			}
		});
		/**
		 * 注册
		 */
		btnNewButton_4.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				btnNewButton_4Action(e);
			}

			private void btnNewButton_4Action(ActionEvent e) {
				// TODO Auto-generated method stub
				String username = textField_7.getText();
				String password = textField_8.getText();
				String email = textField_9.getText();
				String ret="";
				try {
					ret = RuoKuai.zhuce(username,password,email);
				} catch (Exception e2) {
					// TODO: handle exception
					return;
				}				
				new XMLResult().displayXmlResult(ret);;
			}
		});
		
		JPanel panel_5 = new JPanel();
		tabbedPane.addTab("多线程", null, panel_5, null);
		
		JLabel lblNewLabel_26 = new JLabel("验证码地址");
		
		txtHttpcaptchaqqcomgetimage = new JTextField();
		txtHttpcaptchaqqcomgetimage.setEditable(false);
		txtHttpcaptchaqqcomgetimage.setText("http://captcha.qq.com/getimage");
		txtHttpcaptchaqqcomgetimage.setColumns(10);
		
		textField_3 = new JTextField();
		textField_3.setColumns(10);
		
		JLabel lblNewLabel_27 = new JLabel("线程数");
		
		JButton btnNewButton_7 = new JButton("测试");
		
		textField_1 = new JTextField();
		textField_1.setText("0");
		textField_1.setEditable(false);
		textField_1.setColumns(10);
		
		JLabel lblNewLabel_28 = new JLabel("已完成");
		GroupLayout gl_panel_5 = new GroupLayout(panel_5);
		gl_panel_5.setHorizontalGroup(
			gl_panel_5.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_panel_5.createSequentialGroup()
					.addGap(24)
					.addComponent(lblNewLabel_26)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(txtHttpcaptchaqqcomgetimage, GroupLayout.PREFERRED_SIZE, 353, GroupLayout.PREFERRED_SIZE)
					.addContainerGap(70, Short.MAX_VALUE))
				.addGroup(gl_panel_5.createSequentialGroup()
					.addGap(32)
					.addComponent(lblNewLabel_27)
					.addGap(30)
					.addComponent(textField_3, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.RELATED, 51, Short.MAX_VALUE)
					.addComponent(lblNewLabel_28)
					.addPreferredGap(ComponentPlacement.UNRELATED)
					.addComponent(textField_1, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
					.addGap(27)
					.addComponent(btnNewButton_7)
					.addGap(82))
		);
		gl_panel_5.setVerticalGroup(
			gl_panel_5.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_panel_5.createSequentialGroup()
					.addGap(57)
					.addGroup(gl_panel_5.createParallelGroup(Alignment.BASELINE)
						.addComponent(lblNewLabel_26)
						.addComponent(txtHttpcaptchaqqcomgetimage, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
					.addGroup(gl_panel_5.createParallelGroup(Alignment.LEADING)
						.addGroup(gl_panel_5.createSequentialGroup()
							.addGap(49)
							.addGroup(gl_panel_5.createParallelGroup(Alignment.BASELINE)
								.addComponent(lblNewLabel_27)
								.addComponent(btnNewButton_7)
								.addComponent(textField_1, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
								.addComponent(lblNewLabel_28)))
						.addGroup(gl_panel_5.createSequentialGroup()
							.addGap(50)
							.addComponent(textField_3, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)))
					.addContainerGap(112, Short.MAX_VALUE))
		);
		panel_5.setLayout(gl_panel_5);
		
		JPanel panel = new JPanel();
		panel.setBounds(0, 293, 516, 208);
		layeredPane.add(panel);
		
		JScrollPane scrollPane = new JScrollPane();
		GroupLayout gl_panel = new GroupLayout(panel);
		gl_panel.setHorizontalGroup(
			gl_panel.createParallelGroup(Alignment.LEADING)
				.addComponent(scrollPane, GroupLayout.DEFAULT_SIZE, 516, Short.MAX_VALUE)
		);
		gl_panel.setVerticalGroup(
			gl_panel.createParallelGroup(Alignment.LEADING)
				.addComponent(scrollPane, GroupLayout.DEFAULT_SIZE, 208, Short.MAX_VALUE)
		);
		
		textArea = new JTextArea();
		scrollPane.setViewportView(textArea);
		panel.setLayout(gl_panel);
	

		
		/**
		 * URL验证
		 */
		
		btnNewButton_2.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				// TODO Auto-generated method stub
				btnNewButton_2ActionPerforme(arg0);
			}

			private void btnNewButton_2ActionPerforme(ActionEvent arg0) {
				// TODO Auto-generated method stub
				String softId = textField.getText();
		        String softKey = txtCcfdcfbbfcec.getText();
		        String typeid =  textField_2.getText();
		        String username = txtA.getText();
		        String password = textField_4.getText();
		        String url = textField_5.getText();
		        
		        String str ="";
		        str=new XMLResult().createByUrl(softId,softKey,typeid,username,password,url);	        
		        new XMLResult().displayXmlResult(str);
			}
		});
		
		
		/**
		 * 上传报错
		 */
		btnNewButton_3.addActionListener(new ActionListener() {			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				// TODO Auto-generated method stub
				btnNewButton_3ActionPerforme(arg0);
			}

			private void btnNewButton_3ActionPerforme(ActionEvent arg0) {
				// TODO Auto-generated method stub
				String softId = textField.getText();
		        String softKey = txtCcfdcfbbfcec.getText();
		        String error =  textField_6.getText();
		        String username = txtA.getText();
		        String password = textField_4.getText();
		        
		        String ret="";
		        ret=RuoKuai.error(username,password,softId,softKey,error);
		       new XMLResult().displayXmlResult(ret);
			}
		});
		
		/**
		 * 远程下载验证码
		 */
		btnNewButton_1.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				btnNewButton_1Acction(e);
			}

			private void btnNewButton_1Acction(ActionEvent e) {
				// TODO Auto-generated method stub
				String softId = textField.getText();
		        String softKey = txtCcfdcfbbfcec.getText();
		        String typeid =  textField_2.getText();
		        String username = txtA.getText();
		        String password = textField_4.getText();
		        String url = "http://captcha.qq.com/getimage";
		        
		        String str ="";
		        str=new XMLResult().createByUrl(softId,softKey,typeid,username,password,url);
		        new XMLResult().displayXmlResult(str);
			}			
		});
		
		/**
		 * 多线程测试
		 */
		btnNewButton_7.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				if(txtHttpcaptchaqqcomgetimage.getText().length()>0 && textField_3.getText().length()>0){
					
					btnNewButton_7Action(e);
				}else{
					textArea.append("请填写线程数\r\n");
				}

			}

			private void btnNewButton_7Action(ActionEvent e) {
				int n =0; 
				textField_1.setText("0");
				n=Integer.valueOf(textField_3.getText()).intValue();
				for (int i = 0; i < n; i++) {
					 Thread t = new MyThread();
					 t.start();

				 }
			}
		});
		
			/**
			 * 上传验证
			 */
		btnNewButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent arg0) {
				textArea.append("正在解析...\r\n");
				Thread t = new shangchuanThread();
				t.start();
			}

				class shangchuanThread extends Thread{
					public void run() {		
						String softId = textField.getText();
				        String softKey = txtCcfdcfbbfcec.getText();
				        String typeid =  textField_2.getText();
				        
				        String username = txtA.getText();
				        String password = textField_4.getText();
				        textArea.setText("");
				        JFileChooser chooser = new JFileChooser();
				        chooser.setAcceptAllFileFilterUsed(true);
				        int ret = chooser.showOpenDialog(null);
				        if(ret == JFileChooser.APPROVE_OPTION)
				        {
				            String imagePath = chooser.getSelectedFile().getAbsolutePath();
				            ImageIcon ico = new ImageIcon(imagePath);
				            lblNewLabel_25.setIcon(ico);
				            String result = "";
						try{
				            result = RuoKuai.createByPost(username, password, typeid, "90", softId, softKey, imagePath);
				            }catch(Exception e){
				            	textArea.append("参数错误\r\n");
				            }
				          new XMLResult().displayXmlResult(result);
				        }
					}
				}
		});
		
		/**
		 * 
		 * 面板链接
		 * 
		 */
		lblNewLabel_22.addMouseListener(new MouseListener() {
			
			@Override
			public void mouseReleased(MouseEvent e) {
			}
			
			@Override
			public void mousePressed(MouseEvent e) {
			}
			
			@Override
			public void mouseExited(MouseEvent e) {
			}
			
			@Override
			public void mouseEntered(MouseEvent e) {			
			}		
			@Override
			public void mouseClicked(MouseEvent e) {
				// TODO Auto-generated method stub
			    try{
                    java.awt.Desktop.getDesktop().browse(new java.net.URI("http://wiki.ruokuai.com/") );
               }catch(Exception ex){
                    ex.printStackTrace();
               }
			}
		});
		
		lblNewLabel_23.addMouseListener(new MouseListener() {
			
			@Override
			public void mouseReleased(MouseEvent e) {
			}
			
			@Override
			public void mousePressed(MouseEvent e) {
			}
			
			@Override
			public void mouseExited(MouseEvent e) {
			}
			
			@Override
			public void mouseEntered(MouseEvent e) {			
			}		
			@Override
			public void mouseClicked(MouseEvent e) {
				// TODO Auto-generated method stub
			    try{
                    java.awt.Desktop.getDesktop().browse(new java.net.URI("http://www.ruokuai.com/") );
               }catch(Exception ex){
                    ex.printStackTrace();
               }
			}
		});

	lblNewLabel_24.addMouseListener(new MouseListener() {
			
			@Override
			public void mouseReleased(MouseEvent e) {
			}
			
			@Override
			public void mousePressed(MouseEvent e) {
			}
			
			@Override
			public void mouseExited(MouseEvent e) {
			}
			
			@Override
			public void mouseEntered(MouseEvent e) {			
			}		
			@Override
			public void mouseClicked(MouseEvent e) {
				// TODO Auto-generated method stub
			    try{
                    java.awt.Desktop.getDesktop().browse(new java.net.URI("http://www.ruokuai.com/home/register") );
               }catch(Exception ex){
                    ex.printStackTrace();
               }
			}
		});

		
		lblNewLabel_5.addMouseListener(new MouseListener() {
				
				@Override
				public void mouseReleased(MouseEvent e) {
				}
				
				@Override
				public void mousePressed(MouseEvent e) {
				}
				
				@Override
				public void mouseExited(MouseEvent e) {
				}
				
				@Override
				public void mouseEntered(MouseEvent e) {			
				}		
				@Override
				public void mouseClicked(MouseEvent e) {
					// TODO Auto-generated method stub
				    try{
	                    java.awt.Desktop.getDesktop().browse(new java.net.URI("http://www.ruokuai.com/home/pricetype") );
	               }catch(Exception ex){
	                    ex.printStackTrace();
	               }
				}
			});
	}
	
	/**
	 * 
	 * 
	 * @author Administrator
	 *多线程测试
	 */
	class MyThread extends Thread{
		@Override
		public void run() {
			String softId = textField.getText();
	        String softKey = txtCcfdcfbbfcec.getText();
	        String typeid =  textField_2.getText();
	        String username = txtA.getText();
	        String password = textField_4.getText();
	        String url = "http://captcha.qq.com/getimage";
	
	        String str ="";
	        str=RuoKuai.createByUrl(softId,softKey,typeid,username,password,url);
	        new XMLResult().displayXmlResult(str);
	        
	        int i = Integer.valueOf(textField_1.getText()).intValue()+1;
	        textField_1.setText(String.valueOf(i));
	        
		}
		
	}
	protected void displayXmlResult(String str) {
		// TODO Auto-generated method stub
		
	}

	protected void btnNewButtonActionPerforme() {
		// TODO Auto-generated method stub	
	}
}
