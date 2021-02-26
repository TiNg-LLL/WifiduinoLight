import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.Scanner;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.StandardChartTheme;
import org.jfree.chart.plot.DefaultDrawingSupplier;
import org.jfree.chart.plot.PieLabelLinkStyle;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.category.StandardBarPainter;
import org.jfree.chart.renderer.xy.StandardXYBarPainter;
import org.jfree.data.time.Second;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.XYDataset;
import com.fazecast.jSerialComm.SerialPort;
import org.jfree.ui.RectangleInsets;

public class SensorGraph {
    private static Font FONT = new Font("黑体", Font.BOLD, 15);
    public static Color[] CHART_COLORS = { new Color(31, 129, 188), new Color(241, 92, 128), new Color(124, 181, 236), new Color(102, 172, 204),
            new Color(102, 102, 0), new Color(204, 153, 102), new Color(0, 153, 255), new Color(204, 255, 255), new Color(51, 153, 153),
            new Color(255, 204, 102), new Color(102, 102, 0), new Color(204, 204, 204), new Color(204, 255, 255), new Color(255, 204, 204),
            new Color(255, 255, 204), new Color(255, 153, 204), new Color(51, 0, 0), new Color(0, 51, 102), new Color(0, 153, 102), new Color(153, 102, 153),
            new Color(102, 153, 204), new Color(153, 204, 153), new Color(204, 204, 153), new Color(255, 255, 153), new Color(255, 204, 153),
            new Color(255, 153, 204), new Color(204, 153, 153), new Color(204, 204, 255), new Color(204, 255, 204), new Color(153, 204, 153),
            new Color(255, 204, 102) };//颜色


    static SerialPort chosenPort;
    static int x = 0;
    static ServerSocket serverSocketLD;
    static ServerSocket serverSocketWD;
    static ServerSocket serverSocketSD;
    static Socket socketLD;
    static Socket socketWD;
    static Socket socketSD;
    static Scanner scannerLD;
    static Scanner scannerWD;
    static Scanner scannerSD;
    static SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss");
    static Second current = new Second();

    private static JFreeChart createChart(final XYDataset dataset , String Name,String Unit) {
        return ChartFactory.createTimeSeriesChart(
                Name,
                "时间",
                Unit,
                dataset,
                false,
                false,
                false);
    }

    public static void main(String[] args) {

        //**************************************************************************************************************
        // 设置中文主题样式 解决乱码
        StandardChartTheme chartTheme = new StandardChartTheme("CN");
        // 设置标题字体
        chartTheme.setExtraLargeFont(FONT);
        // 设置图例的字体
        chartTheme.setRegularFont(FONT);
        // 设置轴向的字体
        chartTheme.setLargeFont(FONT);
        chartTheme.setSmallFont(FONT);

//        chartTheme.setTitlePaint(new Color(51, 51, 51));
//        chartTheme.setSubtitlePaint(new Color(85, 85, 85));
//
//        chartTheme.setLegendBackgroundPaint(Color.WHITE);// 设置标注
//        chartTheme.setLegendItemPaint(Color.BLACK);//
//        chartTheme.setChartBackgroundPaint(Color.WHITE);
//        // 绘制颜色绘制颜色.轮廓供应商
//        // paintSequence,outlinePaintSequence,strokeSequence,outlineStrokeSequence,shapeSequence
//
//        Paint[] OUTLINE_PAINT_SEQUENCE = new Paint[] { Color.WHITE };
//        //绘制器颜色源
//        DefaultDrawingSupplier drawingSupplier = new DefaultDrawingSupplier(CHART_COLORS, CHART_COLORS, OUTLINE_PAINT_SEQUENCE,
//                DefaultDrawingSupplier.DEFAULT_STROKE_SEQUENCE, DefaultDrawingSupplier.DEFAULT_OUTLINE_STROKE_SEQUENCE,
//                DefaultDrawingSupplier.DEFAULT_SHAPE_SEQUENCE);
//        chartTheme.setDrawingSupplier(drawingSupplier);
//
//        chartTheme.setPlotBackgroundPaint(Color.WHITE);// 绘制区域
//        chartTheme.setPlotOutlinePaint(Color.WHITE);// 绘制区域外边框
//        chartTheme.setLabelLinkPaint(new Color(8, 55, 114));// 链接标签颜色
//        chartTheme.setLabelLinkStyle(PieLabelLinkStyle.CUBIC_CURVE);
//
//        chartTheme.setAxisOffset(new RectangleInsets(5, 12, 5, 12));
//        chartTheme.setDomainGridlinePaint(new Color(192, 208, 224));// X坐标轴垂直网格颜色
//        chartTheme.setRangeGridlinePaint(new Color(192, 192, 192));// Y坐标轴水平网格颜色
//
//        chartTheme.setBaselinePaint(Color.WHITE);
//        chartTheme.setCrosshairPaint(Color.BLUE);// 不确定含义
//        chartTheme.setAxisLabelPaint(new Color(51, 51, 51));// 坐标轴标题文字颜色
//        chartTheme.setTickLabelPaint(new Color(67, 67, 72));// 刻度数字
//        chartTheme.setBarPainter(new StandardBarPainter());// 设置柱状图渲染
//        chartTheme.setXYBarPainter(new StandardXYBarPainter());// XYBar 渲染
//
//        chartTheme.setItemLabelPaint(Color.black);
//        chartTheme.setThermometerPaint(Color.white);// 温度计

        ChartFactory.setChartTheme(chartTheme);
        //以上解决图标乱码
        //**************************************************************************************************************


        // 窗口创建
        JFrame window = new JFrame();
        window.setTitle("亮度/温度/湿度");
        window.setExtendedState(JFrame.MAXIMIZED_BOTH);
        //window.setSize(600, 400);
        window.setLayout(new BorderLayout());
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // create a drop-down box and connect button, then place them at the top of the window
        JComboBox<String> portList = new JComboBox<String>();
        JButton connectButton = new JButton("Connect");
        JPanel topPanel = new JPanel();
        //topPanel.add(portList);
        topPanel.add(connectButton);
        window.add(topPanel, BorderLayout.NORTH);

        // populate the drop-down box
        SerialPort[] portNames = SerialPort.getCommPorts();
        for(int i = 0; i < portNames.length; i++){
            portList.addItem(portNames[i].getSystemPortName());
        }

        // create the line graph
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new GridLayout(1,3,5,5));
        TimeSeries seriesLD = new TimeSeries("亮度");
        TimeSeriesCollection datasetLD = new TimeSeriesCollection(seriesLD);
        TimeSeries seriesWD = new TimeSeries("温度");
        TimeSeriesCollection datasetWD = new TimeSeriesCollection(seriesWD);
        TimeSeries seriesSD = new TimeSeries("湿度");
        TimeSeriesCollection datasetSD = new TimeSeriesCollection(seriesSD);
        //JFreeChart chart = ChartFactory.createXYLineChart("亮度折线图", "时间", "亮度值", datasetLD);
        JFreeChart chartLD = createChart(datasetLD,"亮度","0%~100%");
        JFreeChart chartWD = createChart(datasetWD,"温度","摄氏度℃");
        JFreeChart chartSD = createChart(datasetSD,"湿度","0%~100%");
        chartLD.setTextAntiAlias(false);
        chartWD.setTextAntiAlias(false);
        chartSD.setTextAntiAlias(false);
        ((XYPlot)chartLD.getPlot()).getRangeAxis().setRange(0,1000);// 设置Y坐标轴最大最小值
        ((XYPlot)chartWD.getPlot()).getRangeAxis().setRange(0,100);// 设置Y坐标轴最大最小值
        //((XYPlot)chartWD.getPlot()).getRangeAxis().setFixedAutoRange(100);
        ((XYPlot)chartSD.getPlot()).getRangeAxis().setRange(0,100);// 设置Y坐标轴最大最小值
        centerPanel.add(new ChartPanel(chartLD));
        centerPanel.add(new ChartPanel(chartWD));
        centerPanel.add(new ChartPanel(chartSD));
        window.add(centerPanel, BorderLayout.CENTER);
//        window.add(new ChartPanel(chartLD), BorderLayout.WEST);
//        window.add(new ChartPanel(chartWD), BorderLayout.CENTER);
//        window.add(new ChartPanel(chartSD), BorderLayout.EAST);




        // configure the connect button and use another thread to listen for data
        connectButton.addActionListener(new ActionListener(){
            @Override public void actionPerformed(ActionEvent arg0) {
                if(connectButton.getText().equals("Connect")) {
                    // attempt to connect to the serial port
                    seriesLD.clear();
                    seriesWD.clear();
                    seriesSD.clear();

                    //chosenPort = SerialPort.getCommPort(portList.getSelectedItem().toString());
                    //chosenPort.setComPortTimeouts(SerialPort.TIMEOUT_SCANNER, 0, 0);

                    try {
                        serverSocketLD = new ServerSocket(8085, 3, InetAddress.getByName("192.168.2.100"));
                        serverSocketWD = new ServerSocket(8086, 3, InetAddress.getByName("192.168.2.100"));
                        serverSocketSD = new ServerSocket(8087, 3, InetAddress.getByName("192.168.2.100"));
                        //InetAddress ia = InetAddress.getByName(null);
                        //System.out.println("Server@"+ia+" start!");
                        //System.out.println("Server started at:" + InetAddress.getLocalHost());
                        //System.out.println(serverSocket.getInetAddress());
                        socketLD = serverSocketLD.accept();
                        socketWD = serverSocketWD.accept();
                        socketSD = serverSocketSD.accept();
                        //System.out.println("wifiduino已连接");
//                        BufferedReader socketReader = null;
//                        BufferedWriter socketWriter = null;
//                        socketReader = new BufferedReader(new InputStreamReader(
//                                socket.getInputStream()));
//                        socketWriter = new BufferedWriter(new OutputStreamWriter(
//                                socket.getOutputStream()));

                        scannerLD = new Scanner(socketLD.getInputStream());
                        scannerWD = new Scanner(socketWD.getInputStream());
                        scannerSD = new Scanner(socketSD.getInputStream());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    if(socketLD.isConnected() && socketWD.isConnected() && socketSD.isConnected()) {
                        connectButton.setText("Disconnect");
                        portList.setEnabled(false);
                    }
                    // create a new thread that listens for incoming text and populates the graph
                    Thread thread = new Thread(){
                        @Override public void run() {
                            while(scannerLD.hasNextLine() ||scannerWD.hasNextLine()||scannerSD.hasNextLine()) {
                                try {
                                    String line = scannerLD.nextLine();
                                    int number = Integer.parseInt(line);
                                    //seriesLD.add(Integer.parseInt(df.format(new Date()).replaceAll("[[\\s-:punct:]]","")), 1000-number);
                                    seriesLD.add(current,1000-number);
                                    window.repaint();
                                } catch(Exception e) {}
                                try {
                                    String line1 = scannerWD.nextLine();
                                    int number1 = Integer.parseInt(line1);
                                    seriesWD.add(current,number1);
                                    window.repaint();
                                } catch(Exception e) {}
                                try {
                                    String line2 = scannerSD.nextLine();
                                    int number2 = Integer.parseInt(line2);
                                    seriesSD.add(current,number2);
                                    window.repaint();
                                } catch(Exception e) {}
                                current = ( Second ) current.next( );
                            }
                            scannerLD.close();
                            scannerWD.close();
                            scannerSD.close();
                        }
                    };
                    thread.start();
                } else {
                    // disconnect from the serial port
                    //chosenPort.closePort();
                    try {
                        scannerLD.close();
                        serverSocketLD.close();
                        socketLD.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    try {
                        scannerWD.close();
                        serverSocketWD.close();
                        socketWD.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    try {
                        scannerSD.close();
                        serverSocketSD.close();
                        socketSD.close();
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