/*
 A Simple Example--Authentication Component.
 To Create a Component which works with the InterfaceServer,
 the interface ComponentBase is required to be implemented.

 interface ComponentBase is described in InterfaceServer.java.

 */

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.StringTokenizer;
import java.util.Vector;

public class componentMy {

	private final int init = 0;
	private final static int success = 1;
	private final static int failure = 2;


	/*
	 * interface ComponentBase: The interface you have to implement in your
	 * component
	 */
	interface ComponentBase {
		public KeyValueList processMsg(KeyValueList kvList);
	}

	/*
	 * Class InterfaceServer Set up a socket server waiting for the remote to
	 * connect.
	 */

	public static final int port = 7999;
	public static int state = failure;
	public static void main(String[] args) throws Exception {
		ServerSocket server = new ServerSocket(port);

		/*
		 * You need to create your component here
		 */

		ComponentBase compMy;
		Socket client = server.accept();
		try {
			MsgDecoder mDecoder = new MsgDecoder(client.getInputStream());
			MsgEncoder mEncoder = new MsgEncoder();
			KeyValueList kvInput, kvOutput;
			do {
				kvInput = mDecoder.getMsg();
				if (kvInput != null) {
					System.out.println("Incomming Message:\n");
					System.out.println(kvInput);
					KeyValueList kvResult = processMsg(kvInput);

					System.out.println("Outgoing Message:\n");
					System.out.println(kvResult);
					mEncoder.sendMsg(kvResult, client.getOutputStream());
				}
			} while (kvInput != null);
		} catch (SocketException e) {
			System.out.println("Connection was Closed by Client");
		}
	}

	private static void doAuthentication(String pw) {

		if (pw.equals("123"))
			state = success;
		else
			state = failure;
	}

	/* function in interface ComponentBase */

	public static KeyValueList processMsg(KeyValueList kvList) {
		int MsgID = Integer.parseInt(kvList.getValue("MsgID"));
		KeyValueList kvResult = new KeyValueList();
		if (MsgID == 1) {
			doAuthentication(kvList.getValue("pw"));
		}

		if (state != success) {
			kvResult.addPair("MsgID", "1");
			kvResult.addPair("Description", "Authentication Failed");
		} else {

			switch (MsgID) {
			case 1:
				kvResult.addPair("MsgID", "1");
				kvResult.addPair("Description", "Authentication Success");
				break;
			case 701:
				kvResult.addPair("MsgID", "711");
				kvResult.addPair("Name", "VotingSoftware");
				kvResult.addPair("Acknowledge",
						"CastVote with attributes VoterPhoneNo, CandidateID");
				break;
			case 702:
				kvResult.addPair("MsgID", "712");
				kvResult.addPair("Name", "VotingSoftware");
				kvResult.addPair("Acknowledge",
						"AcknowledgeRequestReport with attribute RankedReport");
				break;
			case 703:
				kvResult.addPair("MsgID", "713");
				kvResult.addPair("Name", "VotingSoftware");
				kvResult.addPair("Acknowledge",
						"Acknowledgement with attributes AckMsgID,YesNo,Name");
				break;
			default:
				kvResult.addPair("Acknowledge", "Wrong MsgID");
				break;
			}

		}
		return kvResult;
	}
}

class KeyValueList {
	private Vector Keys;
	private Vector Values;

	/* Constructor */
	public KeyValueList() {
		Keys = new Vector();
		Values = new Vector();
	}

	/* Look up the value given key, used in getValue() */

	private int lookupKey(String strKey) {
		for (int i = 0; i < Keys.size(); i++) {
			String k = (String) Keys.elementAt(i);
			if (strKey.equals(k))
				return i;
		}
		return -1;
	}

	/* add new (key,value) pair to list */

	public boolean addPair(String strKey, String strValue) {
		return (Keys.add(strKey) && Values.add(strValue));
	}

	/* get the value given key */

	public String getValue(String strKey) {
		int index = lookupKey(strKey);
		if (index == -1)
			return null;
		return (String) Values.elementAt(index);
	}

	/* Show whole list */
	public String toString() {
		String result = new String();
		for (int i = 0; i < Keys.size(); i++) {
			result += (String) Keys.elementAt(i) + ":"
					+ (String) Values.elementAt(i) + "\n";
		}
		return result;
	}

	public int size() {
		return Keys.size();
	}

	/* get Key or Value by index */
	public String keyAt(int index) {
		return (String) Keys.elementAt(index);
	}

	public String valueAt(int index) {
		return (String) Values.elementAt(index);
	}
}

/*
 * Class MsgEncoder: Serialize the KeyValue List and Send it out to a
 * Stream.
 */
class MsgEncoder {
	private PrintStream printOut;
	/* Default of delimiter in system is $$$ */
	private final String delimiter = "$$$";

	public MsgEncoder() {
	}

	/* Encode the Key Value List into a string and Send it out */

	public void sendMsg(KeyValueList kvList, OutputStream out)
			throws IOException {
		PrintStream printOut = new PrintStream(out);
		if (kvList == null)
			return;
		String outMsg = new String();
		for (int i = 0; i < kvList.size(); i++) {
			if (outMsg.equals(""))
				outMsg = kvList.keyAt(i) + delimiter + kvList.valueAt(i);
			else
				outMsg += delimiter + kvList.keyAt(i) + delimiter
						+ kvList.valueAt(i);
		}
		// System.out.println(outMsg);
		printOut.println(outMsg);
	}
}

/*
 * Class MsgDecoder: Get String from input Stream and reconstruct it to a
 * Key Value List.
 */

class MsgDecoder {

	private BufferedReader bufferIn;
	private final String delimiter = "$$$";

	public MsgDecoder(InputStream in) {
		bufferIn = new BufferedReader(new InputStreamReader(in));
	}

	/*
	 * get String and output KeyValueList
	 */

	public KeyValueList getMsg() throws IOException {
		String strMsg = bufferIn.readLine();

		if (strMsg == null)
			return null;

		KeyValueList kvList = new KeyValueList();
		StringTokenizer st = new StringTokenizer(strMsg, delimiter);
		while (st.hasMoreTokens()) {
			kvList.addPair(st.nextToken(), st.nextToken());
		}
		return kvList;
	}

}
