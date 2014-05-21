import java.awt.*;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.*;
import javax.swing.tree.*;
import javax.swing.event.*;

public class MAGD_window extends JFrame {
	private ImitatorsTree imitatorsTree_ = new ImitatorsTree(
			new DefaultMutableTreeNode("Control Panel of MAGD"));
	
	JLabel jlab_name;
	JLabel jlab_ipAdr;
	JLabel jlab_ipMask;
	JLabel jlab_port;
	JLabel jlab_init;

	JTextField jtext_ipAdr;
	JTextField jtext_init;
	JTextField jtext_ipMask;
	JTextField jtext_port;

	private Server server_ = null;

	public MAGD_window() {
		super();
	}

	public void draw() {
		setDefaultLookAndFeelDecorated(true);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		JPanel jpMainPanel = new JPanel();

		GridBagLayout gbag = new GridBagLayout();
		GridBagConstraints gbc = new GridBagConstraints();
		getContentPane().add(jpMainPanel);

		jpMainPanel.setLayout(gbag);

		jlab_name = new JLabel("Control Panel of MAGD");
		jlab_ipAdr = new JLabel("IP Adress: ", SwingConstants.LEFT);
		jlab_ipMask = new JLabel("IP Mask: ", SwingConstants.LEFT);
		jlab_port = new JLabel("Port: ", SwingConstants.LEFT);
		jlab_init = new JLabel("Initial state: ", SwingConstants.LEFT);

		jtext_ipAdr = new JTextField(15);
		// jtext_ipAdr.addActionListener((ActionListener) this);
		jtext_init = new JTextField("0", 15);
		// jtext_init.addActionListener((ActionListener) this);
		jtext_ipMask = new JTextField(15);
		jtext_port = new JTextField(15);
		JButton jbtn_OnOff = new JButton("On/Off");
		// jbtn_OnOff.addActionListener((ActionListener) this);

		for (int i = 0; i < imitatorsTree_.getImitList().size(); i++)
			imitatorsTree_.expandRow(i);

		TreeSelectionModel tsm = imitatorsTree_.getSelectionModel();
		tsm.setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);

		imitatorsTree_.addTreeExpansionListener(new TreeExpansionListener() {
			public void treeExpanded(TreeExpansionEvent tse) {
				TreePath tp = tse.getPath();
				jlab_name.setText("Expansion: " + tp.getLastPathComponent());
			}

			public void treeCollapsed(TreeExpansionEvent tse) {
				TreePath tp = tse.getPath();
				jlab_name.setText("Collapse: " + tp.getLastPathComponent());
			}
		});
		imitatorsTree_.addTreeSelectionListener(new TreeSelectionListener() {
			public void valueChanged(TreeSelectionEvent tse) {
				TreePath tp = tse.getPath();
				jlab_name.setText("" + tp.getLastPathComponent());
				jlab_ipAdr.setText("IP Adress: ");
				jlab_ipMask.setText("IP Mask: ");
				jlab_port.setText("Port: ");
				jlab_init.setText("Initial state:");
				jtext_ipAdr.setText(showIp("" + tp.getLastPathComponent()));
			}
		});

		gbc.weightx = 0.5;
		gbc.weighty = 0.5;

		gbc.ipadx = 5;
		gbc.ipady = 5;

		gbc.gridheight = 6;
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.anchor = GridBagConstraints.PAGE_START;
		gbag.setConstraints(imitatorsTree_, gbc);

		gbc.gridx = 1;
		gbc.gridy = 0;
		gbc.gridheight = 1;
		gbc.gridwidth = 2;
		gbag.setConstraints(jlab_name, gbc);

		gbc.gridx = 1;
		gbc.gridy = 1;
		gbc.gridheight = 1;
		gbc.gridwidth = 1;
		gbag.setConstraints(jlab_ipAdr, gbc);

		gbc.gridx = 1;
		gbc.gridy = 2;
		gbc.gridheight = 1;
		gbc.gridwidth = 1;
		gbag.setConstraints(jlab_ipMask, gbc);

		gbc.gridx = 1;
		gbc.gridy = 3;
		gbc.gridheight = 1;
		gbc.gridwidth = 1;
		gbag.setConstraints(jlab_port, gbc);

		gbc.gridx = 1;
		gbc.gridy = 4;
		gbc.gridheight = 1;
		gbc.gridwidth = 1;
		gbag.setConstraints(jlab_init, gbc);

		gbc.gridx = 2;
		gbc.gridy = 5;
		gbc.gridheight = 1;
		gbag.setConstraints(jbtn_OnOff, gbc);

		gbc.gridx = 2;
		gbc.gridy = 1;
		gbc.gridheight = 1;
		gbc.gridwidth = 1;
		gbag.setConstraints(jtext_ipAdr, gbc);
		
		gbc.gridx = 2;
		gbc.gridy = 2;
		gbc.gridheight = 1;
		gbc.gridwidth = 1;
		gbag.setConstraints(jtext_ipMask, gbc);
		
		gbc.gridx = 2;
		gbc.gridy = 3;
		gbc.gridheight = 1;
		gbc.gridwidth = 1;
		gbag.setConstraints(jtext_port, gbc);

		gbc.gridx = 2;
		gbc.gridy = 4;
		gbc.gridheight = 1;
		gbc.gridwidth = 1;
		gbag.setConstraints(jtext_init, gbc);

		jpMainPanel.add(imitatorsTree_);
		jpMainPanel.add(jlab_name);
		jpMainPanel.add(jlab_ipAdr);
		jpMainPanel.add(jlab_ipMask);
		jpMainPanel.add(jlab_port);
		jpMainPanel.add(jlab_init);
		jpMainPanel.add(jbtn_OnOff);
		jpMainPanel.add(jtext_ipAdr);
		jpMainPanel.add(jtext_ipMask);
		jpMainPanel.add(jtext_port);
		jpMainPanel.add(jtext_init);

		setPreferredSize(new Dimension(450, 400));
		pack();

		setVisible(true);
	}

	public void ImageRotatorMain() {
		// make the frame half the height and width
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		int height = screenSize.height;
		int width = screenSize.width;
		this.setSize(width / 2, height / 2);

		// here's the part where i center the jframe on screen
		this.setLocationRelativeTo(null);

		this.setVisible(true);
	}

	public void start() {
		// ---------------------Server Initialization--------------------------
		try {
			server_ = new Server(2011);
		} catch (IOException ex) {
			Logger.getLogger(MAGD_window.class.getName()).log(Level.SEVERE,
					null, ex);
		}
		server_.setOnServerEventListener(new OnServerEventListener() {
			@Override
			public void onClientConnected(String ipAddress) {
				System.out.println("Server: " + ipAddress + " connected.");
				imitatorsTree_.addImitator(ipAddress);
			}

			@Override
			public void onClientDisconnected(String ipAddress) {
				System.out.println("Server: client " + ipAddress
						+ " disconnected");
				imitatorsTree_.removeImitator(ipAddress);
			}

			@Override
			public void onRequestRecieved(String ip, byte[] data) {
				throw new UnsupportedOperationException("Not supported yet.");
			}
		});
		server_.start();
	}

	public String showIp(String str) {
		Pattern p = Pattern.compile("/*[0-9]{1,2}");
		Matcher m = p.matcher(str);
		String j = null;
		int n = 0;
		while (m.find()) {
			j = m.group();
		}

		try {
			if (j == null)
				n = 0;
			else
				n = Integer.parseInt(j);
		} catch (NumberFormatException e) {
			e.printStackTrace();
		}
		if (n == 0)
			return null;
		if (imitatorsTree_.getImitList().get(n - 1).getCon() == "Disconnected")
			return null;
		else {
			return imitatorsTree_.getImitList().get(n - 1).getIp();
		}
	}

	public static void main(String[] args) {
		MAGD_window mainWindow = new MAGD_window();
		mainWindow.ImageRotatorMain();
		mainWindow.draw();
		mainWindow.start();
	}
}