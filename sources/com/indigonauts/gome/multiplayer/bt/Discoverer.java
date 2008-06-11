package com.indigonauts.gome.multiplayer.bt;

import java.io.IOException;
import java.util.Vector;

import javax.bluetooth.BluetoothStateException;
import javax.bluetooth.DeviceClass;
import javax.bluetooth.DiscoveryAgent;
import javax.bluetooth.DiscoveryListener;
import javax.bluetooth.LocalDevice;
import javax.bluetooth.RemoteDevice;
import javax.bluetooth.ServiceRecord;
import javax.bluetooth.UUID;

public class Discoverer implements DiscoveryListener {

  private static final int L2PCAP_UUID = 0x0100;

  /**
   * The DiscoveryAgent for the local Bluetooth device.
   */
  private DiscoveryAgent agent;

  /**
   * Keeps track of the devices found during an inquiry.
   */
  private Vector deviceList;

  private Vector serviceList;

  /**
   * Creates a PrintClient object and prepares the object for device discovery
   * and service searching.
   * 
   * @exception BluetoothStateException
   *                if the Bluetooth system could not be initialized
   */
  public Discoverer() throws BluetoothStateException {

    /*
     * Retrieve the local Bluetooth device object.
     */
    LocalDevice local = LocalDevice.getLocalDevice();
    progress(1);

    /*
     * Retrieve the DiscoveryAgent object that allows us to perform device
     * and service discovery.
     */
    agent = local.getDiscoveryAgent();

    deviceList = new Vector();
    serviceList = new Vector();

  }

  public void progress(int ps) {

  }

  private void searchServices(RemoteDevice[] devList) {

    int[] attrs = { 0x100 };

    UUID[] searchList = new UUID[2];

    searchList[0] = new UUID(L2PCAP_UUID);
    searchList[1] = new UUID(BluetoothServiceConnector.GOME_UUID, false);

    /*
     * Start a search on as many devices as the system can support.
     */
    System.out.println("Searching " + devList.length + " devices");
    for (int i = 0; i < devList.length; i++) {

      try {
        System.out.println("Service Search on " + devList[i].getBluetoothAddress());
        progress(5);
        agent.searchServices(attrs, searchList, devList[i], this);
      } catch (BluetoothStateException e) {
        System.out.println("BluetoothStateException: " + e.getMessage());
      }

      /*
       * Determine if another search can be started. If not, wait for a
       * service search to end.
       */
      synchronized (this) {
        System.out.println("[Waiting|");
        try {
          this.wait();
        } catch (Exception e) {
          System.out.println("Error waiting:" + e.getMessage());
        }
        System.out.println("| Done Waiting ]");
      }
    }

  }

  public Vector findOtherGome() {

    RemoteDevice[] devList = null;
    try {

      agent.startInquiry(DiscoveryAgent.GIAC, this);
      progress(2);
      /*
       * Wait until all the devices are found before trying to start the
       * service search.
       */
      synchronized (this) {
        try {
          this.wait();
        } catch (Exception e) {
          // nothing to do
        }
      }

    } catch (BluetoothStateException e) {

      System.out.println("Unable to find devices to search");
    }

    if (deviceList.size() > 0) {
      devList = new RemoteDevice[deviceList.size()];
      deviceList.copyInto(devList);
      System.out.println("calling ss");
      searchServices(devList);
      return serviceList;
    }
    System.out.println("No Devices found!");
    System.out.println("No services found:(");
    return serviceList;
  }

  /**
   * Called when a device was found during an inquiry. An inquiry searches for
   * devices that are discoverable. The same device may be returned multiple
   * times.
   * 
   * @see DiscoveryAgent#startInquiry
   * 
   * @param btDevice
   *            the device that was found during the inquiry
   * 
   * @param cod
   *            the service classes, major device class, and minor device
   *            class of the remote device being returned
   * 
   */
  public void deviceDiscovered(RemoteDevice btDevice, DeviceClass cod) {

    progress(3);
    System.out.println("FoundDEV= " + btDevice.getBluetoothAddress());
    int minorDeviceClass;
    int majorDeviceClass;

    /**
     * optimization:work only with possible DiABlu Servers Major Device
     * Class 00001 (256) Computers Minor Device Class 0000010 (4) Desktop
     * Workstation " " " " 0000100 (8) Server Class Server " " " " 0000110
     * (12) Laptop see: Bluetooth Assigned Numbers Document
     */

    majorDeviceClass = cod.getMajorDeviceClass();
    minorDeviceClass = cod.getMinorDeviceClass();
    // TODO filter phones/PDA
    progress(4);

    System.out.println("Adding dev(" + majorDeviceClass + "/" + minorDeviceClass + ")");
    deviceList.addElement(btDevice);

    progress(5);
  }

  /**
   * The following method is called when a service search is completed or was
   * terminated because of an error. Legal status values include:
   * <code>SERVICE_SEARCH_COMPLETED</code>,
   * <code>SERVICE_SEARCH_TERMINATED</code>,
   * <code>SERVICE_SEARCH_ERROR</code>,
   * <code>SERVICE_SEARCH_DEVICE_NOT_REACHABLE</code>, and
   * <code>SERVICE_SEARCH_NO_RECORDS</code>.
   * 
   * @param transID
   *            the transaction ID identifying the request which initiated the
   *            service search
   * 
   * @param respCode
   *            the response code which indicates the status of the
   *            transaction; guaranteed to be one of the aforementioned only
   * 
   */
  public void serviceSearchCompleted(int transID, int respCode) {

    String logMsg = "SS100%(" + transID + ",";

    switch (respCode) {
    case SERVICE_SEARCH_COMPLETED: {
      logMsg += "COMPLETED";
      break;
    }
    case SERVICE_SEARCH_TERMINATED: {
      logMsg += "TERMINATED";
      break;
    }
    case SERVICE_SEARCH_ERROR: {
      logMsg += "ERROR";
      break;
    }
    case SERVICE_SEARCH_DEVICE_NOT_REACHABLE: {
      logMsg += "NOT REACHABLE";
      break;
    }
    case SERVICE_SEARCH_NO_RECORDS: {
      logMsg += "NO RECORDS";
      break;
    }
    default: {
      logMsg += "UNKNOW ERROR!!!";
    }
    }

    logMsg += ")";
    System.out.println(logMsg);

    // inform the threads
    synchronized (this) {
      this.notifyAll();
    }

  }

  /**
   * Called when service(s) are found during a service search. This method
   * provides the array of services that have been found.
   * 
   * @param transID
   *            the transaction ID of the service search that is posting the
   *            result
   * 
   * @param service
   *            a list of services found during the search request
   * 
   * @see DiscoveryAgent#searchServices
   */
  public void servicesDiscovered(int transID, ServiceRecord[] servRecord) {

    progress(6);
    System.out.println("Service:" + transID);
    System.out.println("SLength= " + servRecord.length);
    if (servRecord[0] == null) {

      System.out.println("The service record is null");

    } else {

      System.out.println("Adding SRecord");
      serviceList.addElement(servRecord[0]);

    }
    progress(7);
  }

  /**
   * Called when a device discovery transaction is completed. The
   * <code>discType</code> will be <code>INQUIRY_COMPLETED</code> if the
   * device discovery transactions ended normally, <code>INQUIRY_ERROR</code>
   * if the device discovery transaction failed to complete normally,
   * <code>INQUIRY_TERMINATED</code> if the device discovery transaction was
   * canceled by calling <code>DiscoveryAgent.cancelInquiry()</code>.
   * 
   * @param discType
   *            the type of request that was completed; one of
   *            <code>INQUIRY_COMPLETED</code>, <code>INQUIRY_ERROR</code>
   *            or <code>INQUIRY_TERMINATED</code>
   */
  public void inquiryCompleted(int discType) {

    // Log the Action
    String outLog = "[Inquiry100%,";
    switch (discType) {
    case INQUIRY_COMPLETED: {
      outLog += "COMPLETED";
      break;
    }
    case INQUIRY_ERROR: {
      outLog += "ERROR";
      break;
    }
    case INQUIRY_TERMINATED: {
      outLog += "TERMINATED";
      break;
    }
    default: {
      outLog += "UNKNOWN!!";
    }
    }
    outLog += "]";
    System.out.println(outLog);
    progress(4);
    // free the waits
    synchronized (this) {
      try {
        this.notifyAll();
      } catch (Exception e) {
      }
    }
  }

  public String getServerConnectionString(int index) {

    ServiceRecord dbS = (ServiceRecord) serviceList.elementAt(index);
    String tempURL = dbS.getConnectionURL(ServiceRecord.NOAUTHENTICATE_NOENCRYPT, false);
    System.out.println("ServerConn:" + tempURL);
    // return the connection String
    return tempURL;

  }

  public String getServerFriendlyName(int index) throws IOException {
    ServiceRecord dbS = (ServiceRecord) serviceList.elementAt(index);
    return dbS.getHostDevice().getFriendlyName(true);
  }
}
