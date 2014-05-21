import javax.swing.tree.DefaultMutableTreeNode;

public class Imitator extends DefaultMutableTreeNode {
	private String ip_ = null;
	private String connect_ = "Disconnected";
	private OnCommandListener onCommandListener_ = null;

	public Imitator(String ip) {
		super(ip);
		ip_ = ip;
		connect_ = "Connected";
	}

	public Imitator() {
		ip_ = null;
		connect_ = "Disconnected";
	}

	public boolean isConnect() {
		if (!(ip_ == null))
			return true;
		else
			return false;
	}

	public void setOnCommandListener(OnCommandListener onCommandListener) {
		onCommandListener_ = onCommandListener;
	}

	public String getIp() {
		return ip_;
	}
	public void putIp(String ipAddress) {
		ip_ = ipAddress;
	}
	public String getCon() {
		return connect_;
	}
	
	public void putCon(String str) {
		connect_ = str;
	}

}
