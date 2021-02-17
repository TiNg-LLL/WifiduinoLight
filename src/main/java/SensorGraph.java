import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.time.Second;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import com.fazecast.jSerialComm.SerialPort;

public class SensorGraph {

    static SerialPort chosenPort;
    static int x = 0;
    static ServerSocket serverSocket;
    static Socket socket;
    static Scanner scanner;
    static SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss");
    static Second current = new Second();

    private static JFreeChart createChart(final XYDataset dataset) {
        return ChartFactory.createTimeSeriesChart(
                "Computing Test",
                "Seconds",
                "Value",
                dataset,
                false,
                false,
                false);
    }

    public static void main(String[] args) {

        // create and configure the window
        JFrame window = new JFrame();
        window.setTitle("Sensor Graph GUI");
        window.setSize(600, 400);
        window.setLayout(new BorderLayout());
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // create a drop-down box and connect button, then place them at the top of the window
        JComboBox<String> portList = new JComboBox<String>();
        JButton connectButton = new JButton("Connect");
        JPanel topPanel = new JPanel();
        topPanel.add(portList);
        topPanel.add(connectButton);
        window.add(topPanel, BorderLayout.NORTH);

        // populate the drop-down box
        SerialPort[] portNames = SerialPort.getCommPorts();
        for(int i = 0; i < portNames.length; i++){
            portList.addItem(portNames[i].getSystemPortName());
        }

        // create the line graph
        TimeSeries series = new TimeSeries("亮度 max1000");
        TimeSeriesCollection dataset = new TimeSeriesCollection(series);
        JFreeChart chart = ChartFactory.createXYLineChart("亮度折线图", "时间", "亮度值", dataset);
        JFreeChart chart1 = createChart(dataset);
        window.add(new ChartPanel(chart1), BorderLayout.CENTER);



        // configure the connect button and use another thread to listen for data
        connectButton.addActionListener(new ActionListener(){
            @Override public void actionPerformed(ActionEvent arg0) {
                if(connectButton.getText().equals("Connect")) {
                    // attempt to connect to the serial port
                    series.clear();
                    //chosenPort = SerialPort.getCommPort(portList.getSelectedItem().toString());
                    //chosenPort.setComPortTimeouts(SerialPort.TIMEOUT_SCANNER, 0, 0);

                    try {
                        serverSocket = new ServerSocket(8085, 3, InetAddress.getByName("192.168.2.100"));
                        InetAddress ia = InetAddress.getByName(null);
                        //System.out.println("Server@"+ia+" start!");
                        //System.out.println("Server started at:" + InetAddress.getLocalHost());
                        //System.out.println(serverSocket.getInetAddress());
                        socket = serverSocket.accept();
                        //System.out.println("wifiduino已连接");
//                        BufferedReader socketReader = null;
//                        BufferedWriter socketWriter = null;
//                        socketReader = new BufferedReader(new InputStreamReader(
//                                socket.getInputStream()));
//                        socketWriter = new BufferedWriter(new OutputStreamWriter(
//                                socket.getOutputStream()));

                        scanner = new Scanner(socket.getInputStream());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    if(socket.isConnected()) {
                        connectButton.setText("Disconnect");
                        portList.setEnabled(false);
                    }
                    // create a new thread that listens for incoming text and populates the graph
                    Thread thread = new Thread(){
                        @Override public void run() {
                            while(scanner.hasNextLine()) {
                                try {
                                    String line = scanner.nextLine();
                                    int number = Integer.parseInt(line);
                                    //series.add(Integer.parseInt(df.format(new Date()).replaceAll("[[\\s-:punct:]]","")), 1000-number);
                                    series.add(current,1000-number);
                                    current = ( Second ) current.next( );
                                    window.repaint();
                                } catch(Exception e) {}
                            }
                            scanner.close();
                        }
                    };
                    thread.start();
                } else {
                    // disconnect from the serial port
                    //chosenPort.closePort();
                    try {
                        serverSocket.close();
                        socket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    portList.setEnabled(true);
                    connectButton.setText("Connect");
                    x = 0;
                }
            }
        });

        // show the window
        window.setVisible(true);
    }

}