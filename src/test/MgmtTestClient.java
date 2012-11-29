package test;

import java.io.FileNotFoundException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;

import server.analytics.AnalyticsServerRMI;
import server.analytics.Event;
import tools.PropertiesParser;
import client.mgmt.ManagementClientInterface;

public class MgmtTestClient extends UnicastRemoteObject implements ManagementClientInterface, Runnable {


	private static final long serialVersionUID = -5040308854282471229L;
	private AnalyticsServerRMI as = null;
	private PropertiesParser ps = null;
	private Registry reg = null;
	private long id = 0;
	private ArrayList<String> buffer = null;
	private boolean auto = true;
	private LoadTest lt = null;

	public MgmtTestClient(LoadTest lt) throws RemoteException {
		this.lt = lt;
		try {
			ps = new PropertiesParser("registry.properties");
			int portNr = Integer.parseInt(ps.getProperty("registry.port"));
			String host = ps.getProperty("registry.host");
			reg = LocateRegistry.getRegistry(host, portNr);
			as = (AnalyticsServerRMI) reg.lookup(lt.getAnalyticsBindingName());
		} catch (FileNotFoundException e) {
			System.err.println("properties file not found!");
		} catch (NumberFormatException e) {
			System.err.println("Port non-numeric!");
		} catch (RemoteException e) {
			System.err.println("Registry couldn't be found!");
		} catch (NotBoundException e) {
			System.err.println("Object couldn't be found");
			e.printStackTrace();
		}
		id = 1;
		buffer = new ArrayList<String>();
		auto = true;
		as.subscribe(this, "'(.*)'");
	}

	public void run() {
		while (!lt.isShutdown()) {
			;
		}
	}

	public void processEvent(Event e) throws RemoteException {
			System.out.println(e.toString());
	}

	public void printBuffer() {
		if(!buffer.isEmpty()) {
			for(String s:buffer) {
				System.out.println(s);
			}
			buffer.clear();
		}
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public boolean getAuto() {
		return auto;
	}

	public void setAuto(boolean a) {
		auto = a;
	}

	public void hide(){
		auto = false;
	}

	public void auto(){
		auto = true;
	}

	public ArrayList<String> getBuffer() {
		return buffer;
	}

	public void setBuffer(ArrayList<String> buffer) {
		this.buffer = buffer;
	}
}
