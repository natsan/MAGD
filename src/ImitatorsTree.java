import java.util.ArrayList;
import java.util.List;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

public class ImitatorsTree extends JTree {
	private int N = 16; // number of imitators
	int cnt = 0;// number of connected imitators
	private OnCommandListener onCommandListener_ = null;

	private DefaultMutableTreeNode root_ = null;
	private List<Imitator> imitList = new ArrayList<Imitator>(N);

	public ImitatorsTree(DefaultMutableTreeNode root) {
		super(root);
		for (int i = 0; i < N; i++) {
			Imitator im = new Imitator();
			imitList.add(i, im);
		}
		root_ = root;
		filingRoot();

	}

	public void filingRoot() {
		for (int i = 0; i < N; i++)
			root_.add(new DefaultMutableTreeNode("MAGD " + (i + 1) + " "
					+ imitList.get(i).getCon()));
	}

	public void addImitator(String ipAddress) {
		for (int i = 0; i < N; i++) {
			if ((imitList.get(i).getCon() == "Connected" && (!(imitList.get(i)
					.getIp() == ipAddress))) || (isAllDisconnected())) {
				imitList.get(cnt).putCon("Connected");
				imitList.get(cnt).putIp(ipAddress);
				imitList.get(cnt).setOnCommandListener(onCommandListener_);
				cnt++;
			}
		}
		if (cnt > N) {
			System.out.println("Number of IP > number of imitators");
			cnt = 0;
		}
		reDraw();
	}

	public void removeImitator(String ipAddress) {

		for (int i = 0; i < N; i++) {
			if (imitList.get(i).getIp() == ipAddress) {
				imitList.get(i).putCon("Disconnected");
				imitList.get(i).putIp(null);
				break;
			}
			for (int j = i; j < N; j++)
				imitList.add(j, imitList.get(j + 1));
			cnt--;
		}
	}

	private void reDraw() {
		root_.removeAllChildren();
		filingRoot();
		((DefaultTreeModel) this.getModel()).reload();

	}

	public boolean isAllDisconnected() {
		for (int i = 0; i < N; i++)
			if (imitList.get(i).getCon() == "Connected")
				return false;
		return true;
	}

	public List<Imitator> getImitList() {
		return imitList;
	}
}
