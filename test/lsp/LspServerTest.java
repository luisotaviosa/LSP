package lsp;

import static org.junit.Assert.*;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.nio.ShortBuffer;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class LspServerTest {
	private static final short CONNECT = 0;
	private static final short DATA = 1;
	private static final short ACK = 2;

	private static LspServer server;
	private static int port;
	private static DatagramSocket sock;
	private static short connId;
	private static short seqNum = 0;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		server = new LspServer(0, new LspParams(500, 4));
		port = server.getPort();

		sock = new DatagramSocket();
		connId = connectServer();
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		server.closeAll();
	}

	@Test
	public void testAcceptingConnection() throws Exception {
		DatagramSocket sock = new DatagramSocket();

		ShortBuffer buf = connectServer(sock);
		assertEquals(ACK, buf.get());
		assertNotEquals(0, buf.get());
		assertEquals(0, buf.get());
	}

	@Test
	public void testRead() throws Exception {
		byte[] payload = "Hello, server!".getBytes();

		DatagramPacket pack = createPacket(DATA, payload);
		sock.send(pack);

		Pack recv = server.read();
		assertArrayEquals(payload, recv.getPayload());
	}

	public void testWrite() {
		fail("Not yet implemented");
	}

	public void testCloseConn() {
		fail("Not yet implemented");
	}

	public void testCloseAll() {
		fail("Not yet implemented");
	}

	private static ShortBuffer connectServer(DatagramSocket sock) throws Exception {
		DatagramPacket pack = createPacket(CONNECT, (short) 0, (short) 0, "".getBytes());
		ShortBuffer buf = ByteBuffer.wrap(pack.getData()).asShortBuffer();
		buf.put(new short[] {CONNECT, 0, 0});
		sock.send(pack);
		sock.receive(pack);

		buf.rewind();
		buf.limit(pack.getLength()/2);
		return buf;
	}

	private static short connectServer() throws Exception {
		return connectServer(sock).get(1);
	}

	private static DatagramPacket createPacket(short msgType, short connId, short seqNum, byte[] payload) throws Exception {
		ByteBuffer buf = ByteBuffer.wrap(new byte[1024]);
		buf.asShortBuffer().put(new short[] {msgType, connId, seqNum});
		buf.position(6);
		buf.put(payload);

		return new DatagramPacket(buf.array(), buf.position(), InetAddress.getLocalHost(), port);
	}

	private static DatagramPacket createPacket(short msgType, byte[] payload) throws Exception {
		return createPacket(msgType, connId, ++seqNum, payload);
	}
}
