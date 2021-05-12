//Map Application
//Author: Maksim Zakharau, 256629 
//Date: December 2020;


package Default;


import java.util.concurrent.ThreadLocalRandom;

class Bus implements Runnable {
  public static final int MIN_BOARDING_TIME = 1000;
  
  public static final int MAX_BOARDING_TIME = 10000;
  
  public static final int GETTING_TO_BRIDGE_TIME = 500;
  
  public static final int CROSSING_BRIDGE_TIME = 3000;
  
  public static final int GETTING_PARKING_TIME = 500;
  
  public static final int UNLOADING_TIME = 500;
  
  private static int numberOfBuses = 0;
  
  BusPanel bridge;
  
  int id;
  
  BusDirection dir;
  
  BusState state;
  
  public static void sleep(int millis) {
    try {
      Thread.sleep(millis);
    } catch (InterruptedException interruptedException) {}
  }
  
  public static void sleep(int min_millis, int max_milis) {
    sleep(ThreadLocalRandom.current().nextInt(min_millis, max_milis));
  }
  
  long time = System.currentTimeMillis();
  
  Bus(BusPanel bridge, BusDirection dir) {
    this.bridge = bridge;
    this.dir = dir;
    this.id = ++numberOfBuses;
  }
  
  int computeLocation(long currTime, int BUS, int ROAD) {
    int pos_start;
    int pos_stop;
    switch (this.state) {
      case BOARDING:
        return BUS / 2;
      case GOING_TO_BRIDGE:
        pos_start = BUS / 2;
        pos_stop = BUS / 2 + BUS + ROAD;
        return (int)(pos_start + (pos_stop - pos_start) * (currTime - this.time) / 500L);
      case GET_ON_BRIDGE:
        return BUS / 2 + BUS + ROAD;
      case RIDE_BRIDGE:
        pos_start = BUS / 2 + BUS + ROAD;
        pos_stop = BUS / 2 + 2 * (BUS + ROAD);
        return (int)(pos_start + (pos_stop - pos_start) * (currTime - this.time) / 3000L);
      case GET_OFF_BRIDGE:
        return BUS / 2 + 2 * (BUS + ROAD);
      case GOING_TO_PARKING:
        pos_start = BUS / 2 + 2 * (BUS + ROAD);
        pos_stop = BUS / 2 + 3 * (BUS + ROAD);
        return (int)(pos_start + (pos_stop - pos_start) * (currTime - this.time) / 500L);
      case UNLOADING:
        return BUS / 2 + 3 * (BUS + ROAD);
    } 
    return 0;
  }
  
  void printBusInfo(String message) {
    String t = "Bus (nr." + this.id + " driving to " + this.dir + ") State: " + message + "\n";
    this.bridge.textArea.insert(t, 0);
  }
  
  void boarding() {
    synchronized (this) {
      this.time = System.currentTimeMillis();
      this.state = BusState.BOARDING;
    } 
    printBusInfo("Waiting for new passangers");
    sleep(1000, 10000);
  }
  
  void goToTheBridge() {
    synchronized (this) {
      this.time = System.currentTimeMillis();
      this.state = BusState.GOING_TO_BRIDGE;
    } 
    printBusInfo("Driving to the bridge");
    sleep(500);
  }
  
  void rideTheBridge() {
    synchronized (this) {
      this.time = System.currentTimeMillis();
      this.state = BusState.RIDE_BRIDGE;
    } 
    printBusInfo("Drive over the bridge");
    sleep(3000);
  }
  
  void goToTheParking() {
    synchronized (this) {
      this.time = System.currentTimeMillis();
      this.state = BusState.GOING_TO_PARKING;
    } 
    printBusInfo("Driving to the parking");
    sleep(500);
  }
  
  void unloading() {
    synchronized (this) {
      this.time = System.currentTimeMillis();
      this.state = BusState.UNLOADING;
    } 
    printBusInfo("Unloading passengers");
    sleep(500);
  }
  
  public void run() {
    synchronized (this.bridge.allBuses) {
      this.bridge.allBuses.add(this);
    } 
    boarding();
    goToTheBridge();
    this.bridge.getOnTheBridge(this);
    rideTheBridge();
    this.bridge.getOffTheBridge(this);
    goToTheParking();
    unloading();
    synchronized (this.bridge.allBuses) {
      this.bridge.allBuses.remove(this);
    } 
  }
}
  
 


enum BusDirection {
	  EAST, WEST;
	  
	  public String toString() {
	    switch (this) {
	      case EAST:
	        return "East";
	      case WEST:
	        return "West";
	    } 
	    return "";
	  }
	}



enum BusState {
	  BOARDING, GOING_TO_BRIDGE, GET_ON_BRIDGE, RIDE_BRIDGE, GET_OFF_BRIDGE, GOING_TO_PARKING, UNLOADING;
	}

enum LimitType {
	  NO_LIMITS("No limits"),
	  TWO_WAY("Two way movement(max 3 busy)"),
	  ONE_WAY("One way movement (max 3 busy)"),
	  ONE_BUS("Limited movement (max 1 bus)");
	  
	  String limitName;
	  
	  LimitType(String limit_name) {
	    this.limitName = limit_name;
	  }
	  
	  public String toString() {
	    return this.limitName;
	  }
	}
