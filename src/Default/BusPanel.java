//Map Application
//Author: Maksim Zakharau, 256629 
//Date: December 2020;

package Default;

import java.awt.Font;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class BusPanel extends JFrame {
  private static final long serialVersionUID = 1L;
  
  private static int TRAFFIC = 2000;
  
  private static int DIRECTION = 50;
  
  public static void main(String[] args) {
    BusPanel bridge = new BusPanel();
    while (true) {
      BusDirection dir;
      if (ThreadLocalRandom.current().nextInt(0, 101) < DIRECTION) {
        dir = BusDirection.EAST;
      } else {
        dir = BusDirection.WEST;
      } 
      Bus bus = new Bus(bridge, dir);
      (new Thread(bus)).start();
      try {
        Thread.sleep((5500 - TRAFFIC));
      } catch (InterruptedException interruptedException) {}
    } 
  }
  
  List<Bus> allBuses = new LinkedList<>();
  
  List<Bus> busesWaiting = new LinkedList<>();
  
  List<Bus> busesOnTheBridge = new LinkedList<>();
  
  LimitType limitType = LimitType.ONE_BUS;
  
  void printBridgeInfo(Bus bus, String message) {
    StringBuilder sb = new StringBuilder();
    sb.append("Bus[" + bus.id + "->" + bus.dir + "]  ");
    sb.append(String.valueOf(message) + "\n");
    this.textArea.insert(sb.toString(), 0);
    sb = new StringBuilder();
    for (Bus b : this.busesWaiting) {
      sb.append(b.id);
      sb.append(" ");
    } 
    this.queueField.setText(sb.toString());
    sb = new StringBuilder();
    for (Bus b : this.busesOnTheBridge) {
      sb.append(b.id);
      sb.append(" ");
    } 
    this.bridgeField.setText(sb.toString());
  }
  
  BusDirection curDir = BusDirection.WEST;
  
  int nobuses = 0;
  
  synchronized void getOnTheBridge(Bus bus) {
    synchronized (bus) {
      bus.time = System.currentTimeMillis();
      bus.state = BusState.GET_ON_BRIDGE;
    } 
    boolean print_Message = true;
    while (true) {
      switch (this.limitType) {
      case NO_LIMITS:
    	  break;
        case ONE_BUS:
          if (this.busesOnTheBridge.isEmpty())
            break; 
          break;
        case ONE_WAY:
          if (this.busesOnTheBridge.isEmpty() && this.busesWaiting.isEmpty()) {
            this.nobuses = 0;
            break;
          } 
          if (this.busesOnTheBridge.isEmpty()) {
            if (bus.dir != this.curDir)
              break; 
            if (bus.dir == this.curDir && 
              this.nobuses < 10)
              break; 
            break;
          } 
          if (bus.dir == this.curDir && this.nobuses < 10 && this.busesOnTheBridge.size() < 3)
            break; 
          break;
        case TWO_WAY:
          if (this.busesOnTheBridge.size() < 3)
            break; 
          break;
      } 
      this.busesWaiting.add(bus);
      if (print_Message) {
        printBridgeInfo(bus, "Waiting to get on");
        print_Message = false;
      } 
      try {
        wait();
      } catch (InterruptedException interruptedException) {
      this.busesWaiting.remove(bus);
    } 
    if (this.curDir == bus.dir) {
        this.nobuses++;
      } 
    else {
        this.curDir = bus.dir;
        this.nobuses = 1;
      } 
    this.busesOnTheBridge.add(bus);
    printBridgeInfo(bus, "Get on the bridge");
  }
  }
  
  synchronized void getOffTheBridge(Bus bus) {
    synchronized (bus) {
      bus.time = System.currentTimeMillis();
      bus.state = BusState.GET_OFF_BRIDGE;
    } 
    this.busesOnTheBridge.remove(bus);
    printBridgeInfo(bus, "Get off the bridge");
    notifyAll();
  }
  
  JComboBox<LimitType> limitBox = new JComboBox<>(LimitType.values());
  JTextField bridgeField = new JTextField(30);
  JTextField queueField = new JTextField(30);
  JTextArea textArea = new JTextArea(21, 50);


BusPanel() {
    super("Roading bridge simulation");
    setSize(550, 700);
    setResizable(false);
    setDefaultCloseOperation(3);
    JMenuItem menuAuthor = new JMenuItem("Author");
    menuAuthor.addActionListener(action -> JOptionPane.showMessageDialog(this, "Author: Maksim Zakharau\n Date: 20.12.2020"));
    JMenuItem menuExit = new JMenuItem("Exit");
    menuExit.addActionListener(action -> System.exit(0));
    JMenu menu = new JMenu("Tools");
    menu.add(menuAuthor);
    menu.add(menuExit);
    JMenuBar menuBar = new JMenuBar();
    menuBar.add(menu);
    setJMenuBar(menuBar);
    JPanel leftPanel = new JPanel();
    Font font = new Font("MonoSpaced", 1, 16);
    this.limitBox.setSelectedItem(this.limitType);
    this.limitBox.addActionListener(a -> this.limitType = (LimitType)this.limitBox.getSelectedItem());
    final JSlider slider = new JSlider(0, 500, 5000, TRAFFIC);
    slider.setFont(font);
    slider.setSize(300, 20);
    slider.setMajorTickSpacing(1000);
    slider.setMinorTickSpacing(500);
    slider.setPaintLabels(true);
    Hashtable<Integer, JLabel> labelTable = new Hashtable<>();
    labelTable.put(500, new JLabel("Low"));
    labelTable.put(5000, new JLabel("High"));
    slider.setLabelTable(labelTable);
    slider.addChangeListener(new ChangeListener() {
          public void stateChanged(ChangeEvent e) {
            BusPanel.TRAFFIC = slider.getValue();
          }
        });
  
    JLabel limitLabel = new JLabel("Restriction of movement");
    JLabel sliderLabel = new JLabel("Intensity:");
    JLabel bridgeLabel = new JLabel("            On bridge:");
    JLabel queueLabel = new JLabel("        Queue:");
    limitLabel.setFont(font);
    sliderLabel.setFont(font);
    bridgeLabel.setFont(font);
    queueLabel.setFont(font);
    this.textArea.setFont(font);
    this.bridgeField.setFont(font);
    this.queueField.setFont(font);
    leftPanel.add(limitLabel);
    leftPanel.add(this.limitBox);
    leftPanel.add(sliderLabel);
    leftPanel.add(slider);
    leftPanel.add(bridgeLabel);
    leftPanel.add(this.bridgeField);
    leftPanel.add(queueLabel);
    leftPanel.add(this.queueField);
    this.textArea.setLineWrap(true);
    this.textArea.setWrapStyleWord(true);
    JScrollPane scroll_bars = new JScrollPane(this.textArea, 22, 30);
    leftPanel.add(scroll_bars);
    setContentPane(leftPanel);
    setVisible(true);
  }
}

