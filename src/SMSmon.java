 // SMSmon v1
 // A Java program for sending SMS alerts via a GSM modem,
 // upon events from APC Environmental Station or Smart UPS
 // Web Site: http://sourceforge.net/projects/smsmon
 //
 // Copyright (C) 2009-2010, Vincent Fontaine.
 // SMSmon is distributed under the terms of the Apache License version 2.0
 //
 // Licensed under the Apache License, Version 2.0 (the "License");
 // you may not use this file except in compliance with the License.
 // You may obtain a copy of the License at
 //
 // http://www.apache.org/licenses/LICENSE-2.0
 //
 // Unless required by applicable law or agreed to in writing, software
 // distributed under the License is distributed on an "AS IS" BASIS,
 // WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 // See the License for the specific language governing permissions and
 // limitations under the License.

import org.jfree.chart.*;
import org.jfree.chart.axis.*;
import org.jfree.chart.plot.*;
import org.jfree.chart.plot.dial.*;
import org.jfree.chart.renderer.xy.*;
import org.jfree.chart.title.*;
import org.jfree.data.general.*;
import org.jfree.data.time.*;
import org.jfree.data.xy.*;
import org.jfree.ui.*;
import org.smslib.AGateway;
import org.smslib.OutboundMessage;

//import javax.comm.CommPortIdentifier;
//import gnu.io.CommPortIdentifier;

/*
 Improvement proposition:
    Send maintenance contact as reminder when alerting
 Bugs:
     Support more phone modem easily
 */


class SMSmon {

    static final boolean RxTx = false;
    
    
    
    
    static final String
            applicationString = "SMS - A L E R T S  FOR  D A T A CENTER",
            versionString = "(c) Vincent FONTAINE - 1.00423",
            fileString = "File",
            meteoString = "Meteo Map",
            meteoLabelString = "A P C Environmental Station (inside) and Weather (outside) Meteo Map",
            confString = "SMSmon.conf",
            newEnvString = "New Env. Monitor",
            newUPSString = "New UPS Monitor",
            enableSMSString = "Enable SMS",
            disableSMSString = "Disable SMS",
            enableDAYREPString = "Enable Daily Report",
            disableDAYREPString = "Disable Daily Report",
            enableFORECASTString = "Enable Weather Forecast",
            disableFORECASTString = "Disable Weather Forecast",
            quitString = "Quit",
            envString = "Env. Monitor",
            upsString = "UPS Monitor",
            graphString = "Env. Graph",
            testString = "Test",
            logString = "Log",
            testSMSString = "Test SMS delivery",
            pingsimulString = "Simulate network outage",
            pingsimulStopString = "Stop simulating net. outage",
            hotsimulString = "Simulate overheat",
            hotsimulStopString = "Stop simulating overheat",
            upssimulString = "Simulate electricity outage",
            upssimulStopString = "Stop simulating elec. outage",
            helpString = "Help",
            aboutString = "About...";

    static boolean
            SMS_DISABLE = false, //for testing purpose it is possible to suspend SMS communication
            DAYREP_DISABLE = false, //for less sms it is possible to suspend daily environmental report
            FORECAST_DISABLE = false, //for shorther reports it is possible ro remove the weather forecast
            ASKPIN = false;
    
    static final int
    //sms
            smsTimeout = 45,
    //env. station
            envInterval = 15, //minutes
            reportHour = 8, //daily environmental report sending time hh:00
            maxT = 33, //maximum instantaneous temperature
            minH = 10, //minimum instantaneous relative humidity
    //avoid message flooding burden
            limiterNumber = 8, //a maximum of x SMS will be sent to a single mobile phone number
            limiterHours = 6,  //over a period of y hours
    //ping parameters
            timeOut = 30, //seconds before time out
            triggerDelay = 120, //seconds before to cast an alert
    //GUI dimensions
            windowWidth = 900,
            windowHeight = 750,
            frameBorder = 6,
            desktopWidth = 3000,
            logsWidth = 800,
            logsHeight = 75,
            statusHeight = 25,
            adjustHeight = 70,
            monitorWidth = 400,
            monitorHeight = 550,
            monitorOffsetX = 400,
            monitorOffsetY = 0,
            upsWidth = 500,
            upsHeight = 250,
            upsOffsetX = 20,
            upsOffsetY = 20,
            graphWidth = 800,
            graphHeight = 400,
            graphOffsetX = 20,
            graphOffsetY = 20,
            tableHeight = 65;

    static String envSyntax = "Location,MapX;MapY;TimeZone,\n"
            + "YearIndex;MonthIndex;DayIndex;\n"
            + "HourIndex;MinIndex;SecIndex;\n"
            + "SkipLines;TempIndex;HumidIndex,\n"
            + "APCenvIP,APCuser,APCpw,highT,lowT,highH,lowH,\n"
            + "PingLocation,PingDNS,PingAlert,CellPH1[;CellPH2]";

    static String envExample =
            "AAAAA,xxx;yyy;zzzzzzzzzz,i;i;i;i;i;i;s;i;i,"
            + "xxx.xxx.xxx.xxx,aaaaaa,aaaaaa,25,15,85,15,"
            + "BBBBB,xxx.xxx.xxx.xxx,false,xxxxxxxx";

    static String upsSyntax = "Location,Label,UPSmgmtIP,CommunityStr,\n"
            + "Alerting,CellPH1[;CellPH2]";

    static String upsExample = "AAAAA,BBBBB,xxx.xxx.xxx.xxx,aaaaaa,"
            + "false,xxxxxxxx";

    static java.util.regex.Pattern envP = java.util.regex.Pattern.compile(
              "\\w+\\s*,\\s*\\d{1,3}\\s*;\\s*\\d{1,3}\\s*;\\s*\\w+\\s*,"
            + "\\s*\\d{1,2}\\s*;\\s*\\d{1,2}\\s*;\\s*\\d{1,2}\\s*;"
            + "\\s*\\d{1,2}\\s*;\\s*\\d{1,2}\\s*;\\s*\\d{1,2}\\s*;"
            + "\\s*\\d{1,2}\\s*;\\s*\\d{1,2}\\s*;\\s*\\d{1,2}\\s*,"
            + "\\s*\\d{1,3}.\\d{1,3}.\\d{1,3}.\\d{1,3}\\s*,"
            + "\\s*.+,.+,\\s*\\d{1,2}\\s*,\\s*\\d{1,2}\\s*,\\s*\\d{1,2}\\s*,\\s*\\d{1,2}\\s*,"
            + "\\s*\\w+\\s*,\\s*[\\w-]+\\s*,\\s*\\w+\\s*,\\s*[0-9+; ]+");

    static java.util.regex.Pattern upsP = java.util.regex.Pattern.compile(
              "\\w+\\s*,\\s*.+\\s*,\\s*\\d{1,3}.\\d{1,3}.\\d{1,3}.\\d{1,3}\\s*,\\s*\\w+\\s*,"
            + "\\s*\\w+\\s*,\\s*[0-9+; ]+");

    static java.util.regex.Pattern comP = java.util.regex.Pattern.compile(
                "COM[?\\d]{1,2}(;.+;.*)?");
    static java.util.regex.Pattern mapP = java.util.regex.Pattern.compile(
                "MAP=(.*)");
    static java.util.regex.Pattern smsDisableP = java.util.regex.Pattern.compile(
                "SMS_DISABLE");
    static java.util.regex.Pattern dayrepDisableP = java.util.regex.Pattern.compile(
                "DAYREP_DISABLE");
    static java.util.regex.Pattern forecastDisableP = java.util.regex.Pattern.compile(
                "FORECAST_DISABLE");
    static java.util.regex.Pattern date_P = java.util.regex.Pattern.compile(
              "(\\p{Alnum}{2,4})[-/](\\p{Alnum}{2,4})[-/](\\p{Alnum}{2,4})\\s+"
            + "(\\d{2}):(\\d{2}):(\\d{2})\\t(.*)");
    static java.util.regex.Pattern wunderP = java.util.regex.Pattern.compile(
                "WUNDERK=(.*)");


    static final javax.swing.ImageIcon systemIcon = importIconNoException("system.GIF");
    static final javax.swing.ImageIcon errorIcon = importIconNoException("error.GIF");
    static final javax.swing.ImageIcon infoIcon = importIconNoException("info.GIF");
    static final javax.swing.ImageIcon smsIcon = importIconNoException("sms.GIF");

    static java.util.TimeZone LOCALTZ = java.util.TimeZone.getDefault();

    static Wunderground wunder = null;

    
    public static void main(String args[]) throws Exception {

        System.out.println("curdir=" + new java.io.File(".").getAbsolutePath());
        org.apache.log4j.PropertyConfigurator.configure("log4j.properties");
        org.smslib.helper.Logger.getInstance().logInfo("--------------------------------------------------------------------------------", null, "-"); 
        org.smslib.helper.Logger.getInstance().logInfo("------------------------------ SMSmon is starting ------------------------------", null, "-"); 
        org.smslib.helper.Logger.getInstance().logInfo("--------------------------------------------------------------------------------", null, "-"); 
        
        try {
            javax.swing.UIManager.setLookAndFeel(
            javax.swing.UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {e.printStackTrace();}

        Thread.sleep(3000);

        try {
            if (!isWindows()) throw new Exception();
        } catch(Exception e) {
            play("error.WAV");
            javax.swing.JOptionPane.showMessageDialog(null,
                      "    Welcome to the application:\n" + applicationString + "\n" + versionString + "\n \n"
                    + "ERROR: Only implemented on Windows\n \n"
                    + "The application will exit.",
                    applicationString, javax.swing.JOptionPane.ERROR_MESSAGE);
            System.exit(-1);
        }

        try {
            if (UPSmon.getAPC_SMARTUPS_VALUES() == null) throw new Exception();
        } catch(Exception e) {
            play("error.WAV");
            javax.swing.JOptionPane.showMessageDialog(null,
                      "    Welcome to the application:\n" + applicationString + "\n" + versionString + "\n \n"
                    + "ERROR: Cannot load UPS MIB.\n \n"
                    + "The application will exit.",
                    applicationString, javax.swing.JOptionPane.ERROR_MESSAGE);
            System.exit(-1);
        }

        if (!RxTx) try {
            if (!doExportLib("jar:lib/comm.jar!/win32com.dll", "bin")) throw new Exception();
            if (!doExportLib("jar:lib/comm.jar!/javax.comm.properties", "lib")) throw new Exception();
        } catch(Exception e) {
            play("error.WAV");
            javax.swing.JOptionPane.showMessageDialog(null,
                      "    Welcome to the application:\n" + applicationString + "\n" + versionString + "\n \n"
                    + "ERROR: Missing library win32com.dll\n \n"
                    + "The application will exit.",
                    applicationString, javax.swing.JOptionPane.ERROR_MESSAGE);
            System.exit(-1);
        }
        
        else try {
            if (!doExportLib("jar:lib/RXTXcomm.jar!/rxtxSerial.dll", "bin")) throw new Exception();
            //if (!doExportLib("jar:lib/RXTXcomm.jar!/javax.comm.properties", "lib")) throw new Exception();
        } catch(Exception e) {
            play("error.WAV");
            javax.swing.JOptionPane.showMessageDialog(null,
                      "    Welcome to the application:\n" + applicationString + "\n" + versionString + "\n \n"
                    + "ERROR: Missing library rxtxSerial.dll\n \n"
                    + "The application will exit.",
                    applicationString, javax.swing.JOptionPane.ERROR_MESSAGE);
            System.exit(-1);
        }
        
        try {
            args = loadConf(confString, args);
        } catch(Exception e) {
            play("error.WAV");
            javax.swing.JOptionPane.showMessageDialog(null,
                      "    Welcome to the application:\n" + applicationString + "\n" + versionString + "\n \n"
                    + e.getMessage() + "\n \n"
                    + "Syntax:\n \njava SMSMON [args]\n"
                    + "    * args : modem conf1 [conf2 ...]\n"
                    + "    * modem: COMx[,ModemMaker,ModemModel]\n"
                    + "    * conf : " + envSyntax
                    + "    * conf : " + upsSyntax
                    + "\n                                                                                \n"
                    + "-OR- write your configuration into file " + confString + "\n \n"
                    + "The application will exit.",
                    applicationString, javax.swing.JOptionPane.ERROR_MESSAGE);
            System.exit(-1);
        }


        Meteo meteo_ = new Meteo(null);
        for(int i = 0; i < args.length; i++) {
            String line = args[i]; try {
                if (mapP.matcher(line).matches()) {
                    String resource = line.substring(line.indexOf('=') + 1);
                    meteo_ = new Meteo(importIcon(resource));
                }
            } catch(Exception e) {
                System.out.println("Invalid meteo image: " + line);
            }
        }
        for(int i = 0; i < args.length; i++) {
            String line = args[i]; try {
                if (wunderP.matcher(line).matches()) {
                    String WUNDERK = line.substring(line.indexOf('=') + 1);
                    wunder = new Wunderground(WUNDERK);
                }
            } catch(Exception e) {
                System.out.println("Invalid meteo image: " + line);
            }
        }

        final Meteo meteo = meteo_;
        final javax.swing.JFrame frame = new javax.swing.JFrame(applicationString);
        final java.util.Vector monitors = new java.util.Vector();
        final javax.swing.JDesktopPane desktop = new javax.swing.JDesktopPane();

        desktop.putClientProperty("frame", frame);
        javax.swing.JScrollPane desktop_ = new javax.swing.JScrollPane(desktop);

        final javax.swing.JList logs = new javax.swing.JList();
        javax.swing.JScrollPane logs_ = new javax.swing.JScrollPane(logs);
        final Events events = new Events(logs, logs_);
        logs.setModel(events);

        logs.setCellRenderer(new javax.swing.DefaultListCellRenderer() {
            javax.swing.border.MatteBorder hr = new javax.swing.border.MatteBorder(
                    infoIcon.getIconHeight(), 0, 0, 0, logs.getBackground());
            @Override
            public java.awt.Component getListCellRendererComponent(
                javax.swing.JList list, Object value, int index,
                boolean selected, boolean focus) {
                super.getListCellRendererComponent(list, value, index, selected, focus);
                String s = value.toString().toUpperCase();
                setBorder(s.contains("\n") ? hr : null);
                setIcon(infoIcon);
                if (s.contains("SYSTEM")) setIcon(systemIcon);
                if (s.contains("ALERT")) setIcon(errorIcon);
                if (s.contains("SMS")) setIcon(smsIcon);
                if (s.contains("FAIL")) setIcon(errorIcon);
                return this;
            }
        });

        javax.swing.JLabel status = new javax.swing.JLabel();
        javax.swing.JSplitPane split = new javax.swing.JSplitPane(
                javax.swing.JSplitPane.VERTICAL_SPLIT, true, desktop_, logs_);
        frame.setDefaultCloseOperation(javax.swing.JFrame.DO_NOTHING_ON_CLOSE);
        final java.awt.event.WindowAdapter frameCloser;
        frame.addWindowListener(frameCloser = new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                System.out.println("Shut down requested...");
                if (getSize(monitors) == 0 || javax.swing.JOptionPane.OK_OPTION
                        == javax.swing.JOptionPane.showConfirmDialog(frame,
                        "Are you sure you want to quit:\n" + applicationString + "\n"
                        + "Press Cancel to return to the application.\n \n",
                        applicationString, javax.swing.JOptionPane.OK_CANCEL_OPTION,
                        javax.swing.JOptionPane.QUESTION_MESSAGE)) try {
                    final javax.swing.JMenu envMenu = (javax.swing.JMenu)desktop.getClientProperty(envString);
                    for(int i = envMenu.getItemCount() - 1; i >= 0; i--)
                        ((javax.swing.JInternalFrame)envMenu.getItem(i)
                                .getClientProperty("frame")).setClosed(true);
                    final javax.swing.JMenu upsMenu = (javax.swing.JMenu)desktop.getClientProperty(upsString);
                    for(int i = upsMenu.getItemCount() - 1; i >= 0; i--)
                        ((javax.swing.JInternalFrame)upsMenu.getItem(i)
                                .getClientProperty("frame")).setClosed(true);
                    new Thread() {
                        @Override
                        public void run() {
                            int t = 60;
                            try {while(envMenu.getItemCount() > 0 && t > 0) {
                                System.out.println("Wait " + envMenu.getItemCount() + " env. process to end...");
                                Thread.sleep(1000); t--;
                            }} catch(Exception ee) {}
                            try {while(upsMenu.getItemCount() > 0 && t > 0) {
                                System.out.println("Wait " + upsMenu.getItemCount() + " ups process to end...");
                                Thread.sleep(1000); t--;
                            }} catch(Exception ee) {}
                            if (envMenu.getItemCount() == 0
                                    && upsMenu.getItemCount() == 0) {
                                javax.swing.JOptionPane.showMessageDialog(frame,
                                    "Thank you very much to have used:\n" + applicationString + "\n"
                                    + "Bye.\n \n",
                                    applicationString, javax.swing.JOptionPane.INFORMATION_MESSAGE);
                                System.out.println("Shut down correctly.");
                                System.exit(0);
                            }
                        }
                    }.start();
                } catch(Exception ee) {
                    ee.printStackTrace();
                    System.out.println("Shut down incorrectly.");
                    System.exit(1);
                } else System.out.println("Shut down cancelled.");

            }
        });
        
        split.setOneTouchExpandable(true);
        split.setResizeWeight(1d);
        desktop.setBackground(java.awt.SystemColor.inactiveCaption);
        desktop.setDragMode(javax.swing.JDesktopPane.OUTLINE_DRAG_MODE);
        status.setBorder(new javax.swing.border.BevelBorder(javax.swing.border.BevelBorder.LOWERED));
        desktop_.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        desktop_.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
        logs_.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        logs_.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        frame.getContentPane().add(split, java.awt.BorderLayout.CENTER);
        frame.getContentPane().add(status, java.awt.BorderLayout.SOUTH);
        status.setPreferredSize(new java.awt.Dimension(statusHeight, statusHeight));
        split.setDividerLocation(windowHeight - adjustHeight - logsHeight - statusHeight);
        desktop.setPreferredSize(new java.awt.Dimension(desktopWidth,
                windowHeight - adjustHeight - logsHeight - statusHeight));
        desktop.setMaximumSize(desktop.getPreferredSize());
        logs.setMinimumSize(new java.awt.Dimension(logsWidth, logsHeight));
        logs.setPreferredSize(null);
        frame.setPreferredSize(new java.awt.Dimension(windowWidth, windowHeight));

        frame.addComponentListener(new java.awt.event.ComponentAdapter(){
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                desktop.setPreferredSize(new java.awt.Dimension(desktopWidth,
                        frame.getHeight() - adjustHeight - logsHeight - statusHeight));
                desktop.setMaximumSize(desktop.getPreferredSize());
            }
        });

        /* Menus Definition */
        javax.swing.JMenuBar bar = new javax.swing.JMenuBar();
        bar.setEnabled(false); frame.setJMenuBar(bar);
        javax.swing.JMenu fileMenu, helpMenu; {

            fileMenu = new javax.swing.JMenu(fileString);
            bar.add(fileMenu);
            desktop.putClientProperty(fileString, fileMenu);

            javax.swing.JMenu envMenu = new javax.swing.JMenu(envString);
            bar.add(envMenu);
            desktop.putClientProperty(envString, envMenu);

            javax.swing.JMenu graMenu = new javax.swing.JMenu(graphString);
            bar.add(graMenu);
            desktop.putClientProperty(graphString, graMenu);

            javax.swing.JMenu upsMenu = new javax.swing.JMenu(upsString);
            bar.add(upsMenu);
            desktop.putClientProperty(upsString, upsMenu);

            javax.swing.JMenu testMenu = new javax.swing.JMenu(testString);
            bar.add(testMenu);
            desktop.putClientProperty(testString, testMenu);

            javax.swing.JMenu logMenu = new javax.swing.JMenu(logString);
            bar.add(logMenu);
            desktop.putClientProperty(logString, logMenu);

            helpMenu = new javax.swing.JMenu(helpString);
            bar.add(helpMenu);

        }

        javax.swing.JMenuItem confItem = new javax.swing.JMenuItem(new javax.swing.AbstractAction(confString) {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                javax.swing.JMenuItem item = (javax.swing.JMenuItem)e.getSource();
                Notepad.edit(false, confString);
            }
        });
        fileMenu.add(confItem);

        javax.swing.JMenuItem newEnvItem = new javax.swing.JMenuItem(
                new javax.swing.AbstractAction(newEnvString) {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                javax.swing.JMenuItem item = (javax.swing.JMenuItem)e.getSource();
                java.util.Vector monitors = (java.util.Vector)item.getClientProperty("monitors");
                javax.swing.JDesktopPane desktop = (javax.swing.JDesktopPane)item.getClientProperty("desktop");
                String line = envExample;
                boolean already = true;
                while (line != null && (!envP.matcher(line).matches() || already)) {
                    line = (String)javax.swing.JOptionPane.showInputDialog(frame,
                          "Add a new Environmental Monitor:\n \n" + envSyntax
                          + "\n                                                                                ",
                          applicationString, javax.swing.JOptionPane.QUESTION_MESSAGE, null, null, line);
                    synchronized(monitors) {
                        already = false; for(int i = 0; i < monitors.size(); i++) try {
                            if (((Thread)monitors.get(i)).getName()
                                    .equalsIgnoreCase(getStringArray(line, ',')[0]))
                                already = true;
                        } catch(Exception ee) {}
                    }
                } if (line != null && !already) synchronized(monitors) {
                    monitors.add(showEnvMonitor(desktop, events, meteo, line));
                }
            }
        });
        newEnvItem.putClientProperty("monitors", monitors);
        newEnvItem.putClientProperty("desktop", desktop);
        fileMenu.add(newEnvItem);

        javax.swing.JMenuItem newUPSItem = new javax.swing.JMenuItem(
                new javax.swing.AbstractAction(newUPSString) {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                javax.swing.JMenuItem item = (javax.swing.JMenuItem)e.getSource();
                java.util.Vector monitors = (java.util.Vector)item.getClientProperty("monitors");
                javax.swing.JDesktopPane desktop = (javax.swing.JDesktopPane)item.getClientProperty("desktop");
                String line = upsExample;
                boolean already = true;
                while (line != null && (!upsP.matcher(line).matches() || already)) {
                    line = (String)javax.swing.JOptionPane.showInputDialog(frame,
                          "Add a new UPS Monitor:\n \n" + upsSyntax
                          + "\n                                                                                ",
                          applicationString, javax.swing.JOptionPane.QUESTION_MESSAGE, null, null, line);
                    synchronized(monitors) {
                        already = false; for(int i = 0; i < monitors.size(); i++) try {
                            if (((Thread)monitors.get(i)).getName()
                                    .equalsIgnoreCase(getStringArray(line, ',')[0]))
                                already = true;
                        } catch(Exception ee) {}
                    }
                } if (line != null && !already) synchronized(monitors) {
                    monitors.add(showUpsMonitor(desktop, events, line));
                }
            }
        });
        newUPSItem.putClientProperty("monitors", monitors);
        newUPSItem.putClientProperty("desktop", desktop);
        fileMenu.add(newUPSItem);

        javax.swing.JMenuItem smsItem;
        fileMenu.add(smsItem = new javax.swing.JMenuItem(new javax.swing.AbstractAction(
                SMS_DISABLE ? enableSMSString : disableSMSString) {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                javax.swing.JMenuItem item = (javax.swing.JMenuItem)e.getSource();
                SMS_DISABLE = !SMS_DISABLE;
                item.setText(SMS_DISABLE ? enableSMSString : disableSMSString);
            }
        }));

        javax.swing.JMenuItem dayrepItem;
        fileMenu.add(dayrepItem = new javax.swing.JMenuItem(new javax.swing.AbstractAction(
                DAYREP_DISABLE ? enableDAYREPString : disableDAYREPString) {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                javax.swing.JMenuItem item = (javax.swing.JMenuItem)e.getSource();
                DAYREP_DISABLE = !DAYREP_DISABLE;
                item.setText(DAYREP_DISABLE ? enableDAYREPString : disableDAYREPString);
            }
        }));

        javax.swing.JMenuItem forecastItem;
        fileMenu.add(forecastItem = new javax.swing.JMenuItem(new javax.swing.AbstractAction(
                FORECAST_DISABLE ? enableFORECASTString : disableFORECASTString) {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                javax.swing.JMenuItem item = (javax.swing.JMenuItem)e.getSource();
                FORECAST_DISABLE = !FORECAST_DISABLE;
                item.setText(FORECAST_DISABLE ? enableFORECASTString : disableFORECASTString);
            }
        }));

        final Frame mf = new Frame(meteoString); {
            mf.setClosable(false);
            mf.getContentPane().setLayout(new javax.swing.BoxLayout(
                    mf.getContentPane(), javax.swing.BoxLayout.Y_AXIS));
            add(mf.getContentPane(), meteoLabelString, null);
            javax.swing.JPanel p = new javax.swing.JPanel();
            p.setLayout(new java.awt.BorderLayout());
            p.add(meteo, java.awt.BorderLayout.CENTER);
            add(mf.getContentPane(), p, null);
            mf.setSize(meteo.getWidth(), meteo.getHeight());
            mf.setLocation(0, 0);
            mf.addToDesktop(desktop, fileString);
        } mf.setVisible(true);

        fileMenu.add(new javax.swing.JMenuItem(new javax.swing.AbstractAction(quitString) {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                frameCloser.windowClosing(null);
                //System.exit(0);
            }
        }));

        helpMenu.add(new javax.swing.JMenuItem(new javax.swing.AbstractAction(aboutString) {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                javax.swing.JOptionPane.showMessageDialog(frame,
                    "    Welcome to the application:\n" + applicationString + "\n" + versionString + "\n \n"
                    + "http://sourceforge.net/projects/smsmon/\n \n",
                    applicationString, javax.swing.JOptionPane.INFORMATION_MESSAGE);
            }
        }));

        frame.pack();
        frame.setVisible(true);

        java.util.Vector activeports = new java.util.Vector();
        if (RxTx == true) {
            java.util.Enumeration l = gnu.io.CommPortIdentifier.getPortIdentifiers();
            while (l.hasMoreElements()) {
                gnu.io.CommPortIdentifier cpi = (gnu.io.CommPortIdentifier) l.nextElement();
                if (cpi.getPortType() == gnu.io.CommPortIdentifier.PORT_SERIAL) activeports.add(cpi.getName());
            }
        } else {
            java.util.Enumeration l = javax.comm.CommPortIdentifier.getPortIdentifiers();
            while (l.hasMoreElements()) {
                javax.comm.CommPortIdentifier cpi = (javax.comm.CommPortIdentifier) l.nextElement();
                if (cpi.getPortType() == javax.comm.CommPortIdentifier.PORT_SERIAL) activeports.add(cpi.getName());
            }
        }
        
        String comport = null, commaker = "Nokia", commodel = "6310i";
        for(int i = 0; i < args.length; i++) {
            String line = args[i];
            if (comP.matcher(line).matches()) {comport = line; break;}
        }
        String cp[] = getStringArray(comport, ';');
        if (cp.length > 0) comport = cp[0];
        if (cp.length > 1) {commaker = cp[1]; commodel = "";}
        if (cp.length > 2) commodel = cp[2];
        if ("COM?".equalsIgnoreCase(comport)) comport = null;

        if (comport != null && !activeports.contains(comport)) {
            play("error.WAV");
            javax.swing.JOptionPane.showMessageDialog(frame,
                    comport + " is not available.",
                    applicationString, javax.swing.JOptionPane.ERROR_MESSAGE);
            comport = null;
        }

        if (comport == null) {
            status.setText("Serial Port Selection...");
            play("respond.WAV");
            comport = (String)javax.swing.JOptionPane.showInputDialog(frame,
                      "    Welcome to the application:\n" + applicationString + "\n" + versionString + "\n \n"
                    + "    Please select the Serial Port (COMx)\n"
                    + "to which is bundled the Phone Modem\n"
                    + "you will use to send SMS:\n \n"
                    + "    * Check Radio is activated (Special key that lights up the blue antenna led)\n"
                    + "    * Exit HP Connection Manager for the Wireless WAN Modem to be available\n"
                    + "    * Check in HP Wireless Assistant that Wireless WAN is turned on\n"
                    + "    * Go to the Device Manager under category Modem, open the Wireless WAN Modem\n"
                    + "    * Go to the Modem tab, there can be found the Port: COMx\n \n"
                    + " --- PRESS CANCEL TO ENTER NO-SMS MODE FOR TESTING PURPOSE ---",
                    applicationString, javax.swing.JOptionPane.QUESTION_MESSAGE, null,
                    (String[])activeports.toArray(new String[]{}), null);
            if (comport != null && comport.length() > 0) {
                play("respond.WAV");
                String cm0 = (String)javax.swing.JOptionPane.showInputDialog(frame,
                          "    Welcome to the application:\n" + applicationString + "\n" + versionString + "\n \n"
                        + "    Please select the Phone Modem\n"
                        + "you will use to send SMS:\n \n",
                        applicationString, javax.swing.JOptionPane.QUESTION_MESSAGE,
                        null, null, "Nokia-6310i");
                if (cm0 != null && cm0.length() > 0) {
                    String cm[] = getStringArray(cm0, '-');
                    if (cm.length > 0) {commaker = cm[0]; commodel = "";}
                    if (cm.length > 1) commodel = cm[1];
                }
            }
        }
        if (commaker != null) commaker = commaker.trim();
        if (commodel != null) commodel = commodel.trim();
        if (commaker.length() == 0) commaker = null;
        if (commodel.length() == 0) commodel = null;
        if (comport == null) {
            play("error.WAV");
            javax.swing.JOptionPane.showMessageDialog(frame,
                    "You have to select a COM Port for the application to run properly."
                    //+ "\n \nThe application will exit.",
                    + "\n \nThe application will run without SMS capabilities.",
                    applicationString, javax.swing.JOptionPane.ERROR_MESSAGE);
            SMS_DISABLE = true; //SENDER == null
            status.setText("SMS disabled (no device available)");
            fileMenu.remove(smsItem);
            //System.exit(-1);
        } else {
            status.setText("Connecting to " + comport + " - " + commaker + " - " + commodel + "...");
            play("launch.WAV");
            final String x = comport, y = commaker, z = commodel; {
                new Thread() {
                    @Override
                    public void run() {try{
                        SENDER = new SMS(x, y, z, "0000", events);
                    }catch(Exception e) {}}
                }.start();
                int n = 0; try {while (SENDER == null && n++ < smsTimeout * 2) {
                    Thread.sleep(500);
                    status.setText(status.getText() + ".");
                }} catch(Exception e) {
                    /*javax.swing.JOptionPane.showMessageDialog(frame,
                        comport + " - " + commaker + " - " + commodel
                        + ":\n \n" + e.getMessage().replace(':', '\n')
                        + "\n \nThe application will exit.",
                        APP_TITLE, javax.swing.JOptionPane.ERROR_MESSAGE);
                    System.exit(-1);*/
                }
            } if (ASKPIN && SENDER == null) {
                play("respond.WAV");
                final String PIN = (String)javax.swing.JOptionPane.showInputDialog(frame,
                          "Enter the PIN:", applicationString,
                        javax.swing.JOptionPane.QUESTION_MESSAGE,
                        null, null, "");
                if (PIN != null && PIN.length() > 0) {new Thread() {
                    @Override
                    public void run() {try{
                        SENDER = new SMS(x, y, z, PIN, events);
                    }catch(Exception e) {}}
                }.start();
                int n = 0; try {while (SENDER == null && n++ < smsTimeout * 2) {
                    Thread.sleep(500);
                    status.setText(status.getText() + ".");
                }} catch(Exception e) {
                    play("error.WAV");
                    javax.swing.JOptionPane.showMessageDialog(frame,
                        comport + " - " + commaker + " - " + commodel
                        + ":\n \n" + e.getMessage().replace(':', '\n')
                        + "\n \nThe application will exit.",
                        applicationString, javax.swing.JOptionPane.ERROR_MESSAGE);
                    System.exit(-1);
                }
            }} if (SENDER == null) {
                status.setText("Time out on " + comport + " - " + commaker + " - " + commodel + "!");
                play("error.WAV");
                javax.swing.JOptionPane.showMessageDialog(frame,
                    comport + " - " + commaker + " - " + commodel
                    + ":\n \n" + "Time out while trying to initialize:\n \n"
                    + "    * Try to select another Serial Port\n"
                    + "    * Or to select another Modem Model\n"
                    + "    * Or check your device is properly attached."
                    + "\n \nThe application will exit.",
                    applicationString, javax.swing.JOptionPane.ERROR_MESSAGE);
                System.exit(-1);
            } else {
                status.setText("Connected to " + comport + " - " + commaker + " - " + commodel + ".");
            }
        }

        for(int i = 0; i < args.length; i++) {
            String line = args[i];
            if (smsDisableP.matcher(line).matches()) {
                SMS_DISABLE = true;
                smsItem.setText(SMS_DISABLE ? enableSMSString : disableSMSString);
            } else if (dayrepDisableP.matcher(line).matches()) {
                DAYREP_DISABLE = true;
                dayrepItem.setText(DAYREP_DISABLE ? enableDAYREPString : disableDAYREPString);
            } else if (forecastDisableP.matcher(line).matches()) {
                FORECAST_DISABLE = true;
                forecastItem.setText(FORECAST_DISABLE ? enableFORECASTString : disableFORECASTString);
            } else if (envP.matcher(line).matches()) {
                monitors.add(showEnvMonitor(desktop, events, meteo, line));
                play("roger.WAV");
                try{Thread.sleep(1500);}catch(Exception e) {}
            } else if (upsP.matcher(line).matches()) {
                monitors.add(showUpsMonitor(desktop, events, line));
                play("roger.WAV");
                try{Thread.sleep(1500);}catch(Exception e) {}
            }
        }

        //wait til no monitoring is running then naturally end.
        bar.setEnabled(true);
        int count = 0, newcount;
        while ((newcount = getSize(monitors)) > 0) {
            if (newcount != count) System.out.println("Running threads: " + newcount);
            count = newcount; Thread.sleep(2500);
            synchronized(monitors) {
                java.util.Enumeration i = monitors.elements();
                while(i.hasMoreElements()) {
                    Thread t = (Thread)i.nextElement();
                    if (!t.isAlive()) monitors.remove(t);
                }
            }
        } System.out.println("done.");
        frameCloser.windowClosing(null);
        //System.exit(0);
    }//eom:main


    static int getSize(java.util.Vector v) {synchronized(v) {
        return v.size();
    }}

    /**************************************************************************
                                 GUI Elements
    ***************************************************************************/

    static class Events extends javax.swing.DefaultListModel {
        final javax.swing.JList list;
        final javax.swing.JScrollPane scroll;
        Events(javax.swing.JList list, javax.swing.JScrollPane scroll) {
            this.list = list;
            this.scroll = scroll;
        }
        void add(final String x) {
            javax.swing.SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    Events.super.addElement(x);
                    list.setSelectedIndex(size() - 1);
                    scroll.getViewport().setViewPosition(list.indexToLocation(size() - 1));
                }
            });

        }
    }


    static class Frame extends javax.swing.JInternalFrame {
        Frame(String title) {
            super(title,
                    false, /*resizable*/   true, /*closable*/
                    false, /*maximizable*/ true  /*iconifiable*/);
        }
        void changeIcon() {
            javax.swing.JDesktopPane desktop = getDesktopPane();
            javax.swing.DesktopManager manager = desktop.getDesktopManager();
            if (isIcon()) {
                manager.deiconifyFrame(this);
                desktop.remove(this);
                desktop.add(this, 0);
                desktop.setSelectedFrame(this);
                isIcon = false;
            } else {
                manager.iconifyFrame(this);
                isIcon = true;
            }
        }
        javax.swing.JMenu menu = null;
        javax.swing.JMenuItem item = null;
        void addToDesktop(javax.swing.JDesktopPane desktop, String menuName) {
            removeFromDesktop();
            menu = (javax.swing.JMenu)desktop.getClientProperty(menuName);
            menu.add(item = new javax.swing.JMenuItem(new javax.swing.AbstractAction(getTitle()) {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    changeIcon();
                }
            }));
            item.putClientProperty("frame", this);
            pack();
            desktop.add(this);
        }
        void removeFromDesktop() {
            if (item != null) {
                menu.remove(item);
                menu = null;
                item = null;
            }
            if (getDesktopPane() != null) getDesktopPane().getDesktopManager().closeFrame(this);
        }
    }

    static int
            monitorOffsetX_current = 0,
            monitorOffsetY_current = 0,
            upsOffsetX_current = 0,
            upsOffsetY_current = 0,
            graphOffsetX_current = 0,
            graphOffsetY_current = 0;

/*
 [0]=Location,[1]=MapX;MapY,
 [2]=YearIndex;MonthIndex;DayIndex;HourIndex;MinIndex;SecIndex;SkipLines;TempIndex;HumidIndex,
 [3]=APCenvIP,[4]=APCuser,[5]=APCpw,[6]=highT,[7]=lowT,[8]=highH,[9]=lowH,
 [10]=PingLocation,[11]=PingDNS,[12]=PingAlert,[13]=CellPH1[;CellPH2]
*/

    static Thread showEnvMonitor(
            final javax.swing.JDesktopPane desktop,
            final Events events,
            final Meteo meteo,
            final String conf) {
        try {
            String[] array = getStringArray(conf, ',');
            final String location = array[0];
            String[] xyz = getStringArray(array[1], ';');
            final int x = Integer.parseInt(xyz[0]);
            final int y = Integer.parseInt(xyz[1]);
            final java.util.TimeZone z = java.util.TimeZone.getTimeZone(xyz[2]);
            final Measurer env = new Measurer(
                /*location*/location,
                /*timezone*/z,
                /*indices*/array[2],
                /*server*/array[3],
                /*user*/array[4],
                /*pw*/array[5],
                /*highT*/Integer.parseInt(array[6]),
                /*lowT*/Integer.parseInt(array[7]),
                /*highH*/Integer.parseInt(array[8]),
                /*lowH*/Integer.parseInt(array[9]),
                /*phonenb*/array[13]) {
                @Override
                void alert(String phonenb, String x) {
                    play("trouble.WAV");
                    if (!x.startsWith("TEST: ")) x = "ALERT: " + x;
                    if (SENDER != null) SENDER.send(phonenb, x);
                    else javax.swing.JOptionPane.showMessageDialog(desktop,
                      "SMS alert to: " + phonenb + " (no device available):\n\n" + x,
                    applicationString, javax.swing.JOptionPane.WARNING_MESSAGE);
                    log(location, x);
                }
                @Override
                void info(String x) {
                    play("fine.WAV");
                    events.add(x);
                }
                @Override
                //com.google.weather.WeatherSet
                Wunderground.WeatherSet
                meteo(boolean req) {
                    //com.google.weather.WeatherSet
                    Wunderground.WeatherSet
                    ret = null;
                    updateThread t = new updateThread();
                    return req ? null : t.getValue();
                }
                class updateThread extends Thread {
                    //com.google.weather.WeatherSet
                    Wunderground.WeatherSet
                    ret = null;
                    updateThread() {
                        start();
                    }
                    @Override
                    public void run() {
                        ret = meteo.update(location);
                    }
                    //com.google.weather.WeatherSet
                    Wunderground.WeatherSet
                    getValue() {
                        try {join();} catch(Exception e) {e.printStackTrace();}
                        return ret;
                    }
                }
            };
            final Pinger ping = new Pinger(
                    /*location*/location,
                    /*to*/array[10],
                    /*host*/array[11],
                    /*alert*/array[12],
                    /*phonenb*/array[13]) {
                @Override
                void alert(String phonenb, String x) {
                    play(x.indexOf("resumed") == -1 ? "trouble.WAV" : "fine.WAV");
                    if (!alert) events.add(x);
                    else if (SENDER != null) SENDER.send(phonenb, "ALERT: " + x);
                    else javax.swing.JOptionPane.showMessageDialog(desktop,
                        "SMS alert to: " + phonenb + " (no device available):\n\n"
                        + "ALERT: " + x,
                        applicationString, javax.swing.JOptionPane.WARNING_MESSAGE);
                    log(location, "ALERT: " + x);

                }
                @Override
                void info(String x) {
                    play("fine.WAV");
                    events.add(x);
                }
            };
            meteo.add(location, env, x, y);

            final javax.swing.JFrame frame = (javax.swing.JFrame)desktop.getClientProperty("frame");
            final Frame f = new Frame(envString + " " + location); {
                f.getContentPane().setLayout(new javax.swing.BoxLayout(
                        f.getContentPane(), javax.swing.BoxLayout.Y_AXIS));
                add(f.getContentPane(), "N E T W O R K  Connectivity Monitoring", ping.alert ? smsIcon : null);
                add(f.getContentPane(), ping.getGUI(), null);
                add(f.getContentPane(), "A P C  Environmental Station  Monitoring", smsIcon);
                add(f.getContentPane(), env.getGUI(), null);
                f.setSize(monitorWidth, monitorHeight);
                f.setLocation(monitorOffsetX_current, monitorOffsetY_current);
                monitorOffsetX_current += monitorOffsetX + (monitorOffsetX > 0 ? frameBorder : 0);
                monitorOffsetY_current += monitorOffsetY + (monitorOffsetY > 0 ? frameBorder : 0);
                f.addToDesktop(desktop, envString);
            }
            final Frame g = new Frame(graphString + " " + location); {
                g.setClosable(false);
                g.getContentPane().setLayout(new java.awt.BorderLayout(0, 0));
                g.getContentPane().add(env.getGUI2(), java.awt.BorderLayout.CENTER);
                g.getContentPane().setPreferredSize(new java.awt.Dimension(graphWidth, graphHeight));
                g.setSize(graphWidth, graphHeight);
                g.setLocation(graphOffsetX_current, graphOffsetY_current);
                graphOffsetX_current += graphOffsetX + (graphOffsetX > 0 ? frameBorder : 0);
                graphOffsetY_current += graphOffsetY + (graphOffsetY > 0 ? frameBorder : 0);
                g.addToDesktop(desktop, graphString);
            }

            final javax.swing.JMenu logMenu = (javax.swing.JMenu)desktop.getClientProperty(logString);
            final javax.swing.JMenuItem logItem = new javax.swing.JMenuItem(new javax.swing.AbstractAction(
                    logString + " " + location) {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    Notepad.edit(true, getLog(location));
                }
            }); logMenu.add(logItem);

            final javax.swing.JMenu testMenu = (javax.swing.JMenu)desktop.getClientProperty(testString);
            final javax.swing.JMenuItem testItem = new javax.swing.JMenuItem(new javax.swing.AbstractAction(
                    testSMSString + " " + location) {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    new Thread() {
                        @Override
                        public void run() {
                            try {
                                play("respond.WAV");
                                int ret = javax.swing.JOptionPane.showConfirmDialog(frame,
                                "SMS Test for " + location + "\n \n"
                                + "Contact list: " + env.phonenb + "\n \n"
                                + "Please press YES to proceed and wait a moment...",
                                applicationString, javax.swing.JOptionPane.INFORMATION_MESSAGE);
                                if (ret != javax.swing.JOptionPane.OK_OPTION) throw new Exception("Cancelled");
                                env.checkEnvironment(true);
                                throw new Exception("Successful");
                            } catch(Exception ee) {
                                play("roger.WAV");
                                javax.swing.JOptionPane.showMessageDialog(frame,
                                "SMS Test for " + location + "\n \n"
                                + "Contact list: " + env.phonenb + "\n \n"
                                + "Result: " + ee.getMessage(),
                                applicationString, javax.swing.JOptionPane.INFORMATION_MESSAGE);
                                ee.printStackTrace();
                            }
                        }
                    }.start();
                }
            }); testMenu.add(testItem);

            final javax.swing.JMenuItem pingsimulItem = new javax.swing.JMenuItem(new javax.swing.AbstractAction(
                    pingsimulString + " " + location) {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    javax.swing.JMenuItem simulItem = (javax.swing.JMenuItem)e.getSource();
                    ping.simul = !ping.simul;
                    simulItem.setText((ping.simul ? pingsimulStopString : pingsimulString) + " " + location);
                }
            }); testMenu.add(pingsimulItem);

            final javax.swing.JMenuItem hotsimulItem = new javax.swing.JMenuItem(new javax.swing.AbstractAction(
                    hotsimulString + " " + location) {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    javax.swing.JMenuItem hotsimulItem = (javax.swing.JMenuItem)e.getSource();
                    env.simul = !env.simul;
                    hotsimulItem.setText((env.simul ? hotsimulStopString : hotsimulString) + " " + location);
                    try {
                        if (env.simul) env.checkEnvironment(false);
                    } catch(Exception ee) {
                        System.out.println("Failed opening " + env.getName()+ " (" + env.server + "): " + ee.getMessage());
                        env.showGUI(-5, true, 0, true);
                    }
                }
            }); testMenu.add(hotsimulItem);

            Thread T = new Thread(f.getTitle()) {
                @Override
                public void run() {
                    events.add("SYSTEM: Started: " + conf);
                    f.setVisible(true); f.changeIcon();
                    g.setVisible(true); g.changeIcon();
                    try {
                        while(!f.isClosed()) sleep(1000);
                    } catch (Exception e) {}
                    destroy();
                    f.removeFromDesktop();
                    logMenu.remove(logItem);
                    testMenu.remove(testItem);
                    testMenu.remove(pingsimulItem);
                    testMenu.remove(hotsimulItem);
                    g.removeFromDesktop();
                    meteo.remove(location);
                    events.add("\nSYSTEM: Stopped: " + conf);
                }
                @Override
                public void destroy() {
                    ping.destroy();
                    env.destroy();
                }
            }; T.start();
            return T;
        }catch(Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    /*
 [0]=Location,
 [1]=Label,
 [2]=APCmgmtIP,
 [3]=CommunityStr
 [4]=alerting
 [5]=CellPH1[;CellPH2]
*/

    static Thread showUpsMonitor(
            final javax.swing.JDesktopPane desktop,
            final Events events,
            final String conf) {
        try {
            String[] array = getStringArray(conf, ',');
            final String location = array[0];
            final String label = array[1];
            final String ip = array[2];
            final String comstr = array[3];
            final boolean alerting = Boolean.parseBoolean(array[4]);
            final String phonenb = array[5];

            final UPSmon ups = new UPSmon(label, ip, comstr) {
                @Override
                void alarm(String x, boolean set) {
                    if (set) {
                        play("trouble.WAV");
                    } else {
                        play("fine.WAV");
                    }
                    if (alerting) {
                        if (!x.startsWith("TEST: ")) x = "ALERT: " + x;
                        if (!set) x.replaceAll("ALERT", "Info");
                        if (SENDER != null) SENDER.send(phonenb, x);
                        else javax.swing.JOptionPane.showMessageDialog(desktop,
                          "SMS alert to: " + phonenb + " (no device available):\n\n" + x,
                        applicationString, javax.swing.JOptionPane.WARNING_MESSAGE);
                    } else events.add(x);
                    log(location, x);
                }
            };
            
            final javax.swing.JFrame frame = (javax.swing.JFrame)desktop.getClientProperty("frame");
            final Frame f = new Frame(upsString + " " /*+ location + ":"*/ + label); {
                f.getContentPane().setLayout(new javax.swing.BoxLayout(
                        f.getContentPane(), javax.swing.BoxLayout.Y_AXIS));
                add(f.getContentPane(), "A P C  Uninterrupted Power Supply  Monitoring", smsIcon);
                javax.swing.JPanel gui = ups.getGUI();
                gui.setPreferredSize(new java.awt.Dimension(upsWidth, upsHeight));
                add(f.getContentPane(), gui, null);
                f.setSize(upsWidth, upsHeight + 40);
                f.setLocation(upsOffsetX_current, upsOffsetY_current);
                upsOffsetX_current += upsOffsetX + (upsOffsetX > 0 ? frameBorder : 0);
                upsOffsetY_current += upsOffsetY + (upsOffsetY > 0 ? frameBorder : 0);
                f.addToDesktop(desktop, upsString);
            }

            final javax.swing.JMenu testMenu = (javax.swing.JMenu)desktop.getClientProperty(testString);
            final javax.swing.JMenuItem upssimulItem = new javax.swing.JMenuItem(new javax.swing.AbstractAction(
                    upssimulString + " " + label) {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    javax.swing.JMenuItem upssimulItem = (javax.swing.JMenuItem)e.getSource();
                    ups.setSimul(!ups.getSimul());
                    upssimulItem.setText((ups.getSimul() ? upssimulStopString : upssimulString) + " " + location);
                }
            }); testMenu.add(upssimulItem);
  
            Thread T = new Thread(f.getTitle()) {
                @Override
                public void run() {
                    events.add("SYSTEM: Started: " + conf);
                    f.setVisible(true); f.changeIcon();
                    try {
                        while(!f.isClosed()) sleep(1000);
                    } catch (Exception e) {}
                    destroy();
                    f.removeFromDesktop();
//                    logMenu.remove(logItem);
                    testMenu.remove(upssimulItem);
                    events.add("\nSYSTEM: Stopped: " + conf);
                }
                @Override
                public void destroy() {
                    ups.destroy();
                }
            }; T.start();
            return T;
        } catch(Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    static void add(java.awt.Container parent, Object o, javax.swing.ImageIcon icon) {
        if (!String.class.isInstance(o)) {
            parent.add((javax.swing.JComponent)o);
        } else {
            javax.swing.JPanel p = new javax.swing.JPanel();
            p.setLayout(new java.awt.BorderLayout(0, 0));
            javax.swing.JLabel label = new javax.swing.JLabel((String)o + "  ");
            label.setIcon(icon);
            label.setHorizontalTextPosition(javax.swing.SwingConstants.LEFT);
            label.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
            label.setFont(new java.awt.Font("Dialog", 1, 11));
            label.setOpaque(true);
            label.setBackground(java.awt.SystemColor.info);
            label.setForeground(java.awt.SystemColor.infoText);
            p.setPreferredSize(new java.awt.Dimension(parent.getPreferredSize().width, 20));
            p.add(new javax.swing.JSeparator(), java.awt.BorderLayout.NORTH);
            p.add(label, java.awt.BorderLayout.CENTER);
            p.add(new javax.swing.JSeparator(), java.awt.BorderLayout.SOUTH);
            parent.add(p);
        }
    }

    static class Meteo extends javax.swing.JLabel {
        javax.swing.ImageIcon map;
        java.awt.image.BufferedImage buf;
        java.awt.Graphics2D g;
        java.util.Hashtable data = new java.util.Hashtable();
        int dy = 9;

        Meteo(javax.swing.ImageIcon map) {
            setHorizontalTextPosition(javax.swing.JLabel.RIGHT);
            this.map = map;
            buf = new java.awt.image.BufferedImage(
                    map == null ? windowWidth : map.getIconWidth(),
                    map == null ? monitorHeight : map.getIconHeight(),
                    java.awt.image.BufferedImage.TYPE_INT_ARGB);
            g = (java.awt.Graphics2D)buf.getGraphics();
            update((String)null);
        }
        void add(String city, Measurer env, int x, int y) {synchronized(this) {
            data.put(city, new Object[]{city, env, x, y});
            update((String)null);
        }}
        void remove(String city) {synchronized(this) {
            data.remove(city);
            update((String)null);
        }}
        //com.google.weather.WeatherSet
        Wunderground.WeatherSet
        update(String location) {
            //com.google.weather.WeatherSet
            Wunderground.WeatherSet
            ret = null; synchronized(this) {
                g.setColor(java.awt.Color.WHITE);
                g.fillRect(0, 0, getWidth(), getHeight());
                if (map != null) g.drawImage(map.getImage(), 0, 0, null);
                java.util.Iterator i = data.values().iterator();
                while(i.hasNext()) {
                    Object[] entry = (Object[])i.next();
                    String city = (String)entry[0];
                    //com.google.weather.WeatherForecast weather = new com.google.weather.WeatherForecast();
                    //com.google.weather.WeatherSet weatherSet = com.google.weather.WeatherForecast.parse(city);
                    Wunderground.WeatherSet cc = wunder.get(city);
                    //if (weatherSet == null) {//retry
                    //    try{Thread.sleep(500);} catch(Exception e) {}
                    //    weather = new com.google.weather.WeatherForecast();
                    //    weatherSet = com.google.weather.WeatherForecast.parse(city);
                    //}
                    //if (cc == null) {//retry
                    //    try{Thread.sleep(500);} catch(Exception e) {}
                    //    cc = wunder.get(city);
                    //}
                    //if (weatherSet != null && location != null && city.equals(location)) ret = weatherSet;
                    //String outside = weatherSet == null ? "N/A"
                    //        : weatherSet.getCurrentCondition().getTempCelcius();
                    if (cc != null && location != null && city.equals(location)) ret = cc;
                    //String outside = weatherSet == null ? "N/A"
                    //        : weatherSet.getCurrentCondition().getTempCelcius();
                    String outside = cc == null ? "N/A"
                            : "" + cc.getCelsius();
                    int outT = 0; try {outT = Integer.parseInt(outside);} catch(Exception e) {}
                    javax.swing.ImageIcon icon = null; int w = 32, h = 32;
//                    try {
//                        icon = new javax.swing.ImageIcon(
//                                new java.net.URL("http://www.google.com"
//                                + weatherSet.getCurrentCondition().getIcon()));
//                        w = icon.getIconWidth(); h = icon.getIconHeight();
//                    } catch(Exception e) {/*e.printStackTrace();*/}
                    icon = cc == null ? null : cc.getIcon();
                    w = icon != null ? icon.getIconWidth() : 32;
                    h = icon != null ? icon.getIconHeight() : 32;
                    
                    Measurer env = (Measurer)entry[1];
                    int inT = env.lastT;
                    boolean alert = !env.COND_OK.isSet();
                    String inside = inT == 0 ? "N/A" : "" + inT;
                    String diff =
                            //weatherSet == null
                            cc == null
                            || inT == 0 ? "" : (outT - inT > 0 ? "+" : "") + (outT - inT);
                    int x = ((Integer)entry[2]).intValue();
                    int y = ((Integer)entry[3]).intValue();
                    if (icon != null) g.drawImage(icon.getImage(), x - w/2, y - h/2, null);
                    g.setColor(alert ? java.awt.Color.RED : java.awt.Color.BLACK);
                    g.setFont(g.getFont().deriveFont(java.awt.Font.BOLD));
                    g.drawString(city + "  " + diff, x + w/2, y - dy);
                    g.drawString(inside + "°C in", x + w/2, y + h/3 - dy);
                    g.drawString(outside + "°C out", x + w/2, y + 2*h/3 - dy);
                }
                g.setColor(java.awt.Color.DARK_GRAY);
                g.setFont(g.getFont().deriveFont(java.awt.Font.PLAIN));
                java.util.Calendar c = java.util.Calendar.getInstance();
                c.setTimeInMillis(System.currentTimeMillis());
                int h = c.get(java.util.Calendar.HOUR_OF_DAY), m = c.get(java.util.Calendar.MINUTE);
                String mm = (m < 10 ? "0" :"") + m, hh = (h < 10 ? "0" :"") + h;
                g.drawString(hh + ":" + mm + " " + LOCALTZ.getDisplayName(), dy, dy);
                setIcon(new javax.swing.ImageIcon(buf));
            } return ret;
        }
    }//eoc Meteo


    /**************************************************************************
                                 SMS Communication
    ***************************************************************************/


    static SMS SENDER = null;
    static class SMS extends org.smslib.Service {
        final Events events;
        org.smslib.modem.SerialModemGateway gateway;
        SMS(String comport, String commaker, String commodel, String PIN,
                Events events)
                throws org.smslib.GatewayException, org.smslib.SMSLibException,
                       java.io.IOException, InterruptedException {
            this.events = events;
            
            //org.smslib.queues.AbstractQueueManager queueManager = new org.smslib.queues.DefaultQueueManager(5000);
            //setQueueManager(queueManager);
            
            gateway = new org.smslib.modem.SerialModemGateway(
                "mymodem", comport, 57600, commaker, commodel);
            gateway.setInbound(true);
            gateway.setOutbound(true);
            gateway.setSimPin(PIN);
            setOutboundMessageNotification(new OutboundNotification());
            addGateway(gateway);
            
            
            startService();
            
            System.out.println("SMS GATEWAY: " + gateway.getStatus().toString() + " #" + gateway.getRestartCount()
                    + " (" + gateway.getGatewayId() + ")");
            /*
            while(gateway.getStatus() != org.smslib.AGateway.GatewayStatuses.STARTED) {
                Thread.sleep(500);
                gateway.startGateway();
                System.out.println("SMS GATEWAY: " + gateway.getStatus().toString() + " #" + gateway.getRestartCount()
                        + " (" + gateway.getGatewayId() + ")");
            }
             
             */
        }
        void send(String phonenb, String msg) {synchronized(this) {
            String[] nb = getStringArray(phonenb, ';');
            for(int i = 0; i < nb.length; i++) {
                Thread t = getWaiter(nb[i]); if (t != null) {
                    org.smslib.OutboundMessage m = new org.smslib.OutboundMessage(nb[i], msg);
                    m.setValidityPeriod(1);
                    m.setGatewayId(gateway.getGatewayId());
                    if (!SMS_DISABLE) try {sendMessage(m);} catch(Exception e) {e.printStackTrace();}
                    events.add("SMS: " + m.getMessageStatus() + ": " + msg);
                    if (!m.getMessageStatus().equals(org.smslib.OutboundMessage.MessageStatuses.SENT)) {
                        System.err.println("********** SMS COULD NOT BE SENT **********");
                        System.err.println("GATEY: " + gateway.getGatewayId());
                        System.err.println("STATUS: " + m.getMessageStatus().toString());
                        System.err.println("ERRMES: " + m.getErrorMessage());
                        System.err.println("FCAUSE: " + m.getFailureCause().toString());
                        t.destroy();
                    }
                } else {
                    events.add("SMS: DISCARDED: " + msg);
                }
            }
        }}
        @Override
        protected void finalize() {
            try {
                stopService();
            } catch(Exception e) {e.printStackTrace();}
            try {
                super.finalize();
            } catch (Throwable e) {e.printStackTrace();}
        }
        java.util.Hashtable SMSLIMIT = new java.util.Hashtable();
        ThreadGroup getSMSLimit(String nb) {
            ThreadGroup tg = (ThreadGroup)SMSLIMIT.get(nb);
            if (tg == null) {
                tg = new ThreadGroup(nb);
                SMSLIMIT.put(nb, tg);
            } return tg;
        }
        int SMSINC = 0;
        Thread getWaiter(String nb) {
            ThreadGroup tg = getSMSLimit(nb);
            String name = "MSG#" + SMSINC++;
            if (tg.activeCount() >= limiterNumber) {
                play("toomany.WAV");
                events.add("\nSMS: " + name + " To:" + nb + " ***DISCARDED TO AVOID TOO MANY SMS***");
                return null;
            } else {
                events.add("\nSMS: " + name + " To:" + nb + " Limit:" + (tg.activeCount()+1) + "/" + limiterNumber);
                Thread t = new Thread(tg, name) {
                    boolean STOP = false;
                    @Override
                    public void destroy() {
                        STOP = true;
                    }
                    @Override
                    public void run() {try{
                        for(int i = 0; !STOP && i < limiterHours * 60 * 60; i++) sleep(1000);
                    }catch(Exception e) {}}
                }; t.setDaemon(true); t.start();
                return t;
            }
        }
    }


    /**************************************************************************
                                    Environment
    ***************************************************************************/


    static class Measurer extends Thread {
        String location, server, user, pw, phonenb;
        java.util.TimeZone timezone;
        int dateindices[], sampleindices[], highT, lowT, highH, lowH, lastT = 0, lastH = 0;
        boolean STOP = false;
        Flag COND_OK = new Flag(true);
        boolean simul = false;//overheat simulation
        Measurer(String location, java.util.TimeZone timezone,
                String datesampleindices,
                String server, String user, String pw,
                int highT, int lowT, int highH, int lowH,
                String phonenb) throws Exception {
            this.location = location;
            this.timezone = timezone;
            this.server = server;
            this.user = user;
            this.pw = pw;
            try {
                int[] ds = getIntArray(datesampleindices, ';');
                dateindices = new int[]{ds[0], ds[1], ds[2], ds[3], ds[4], ds[5]};
                sampleindices = new int[]{ds[6], ds[7], ds[8]};
            } catch(Exception e) {
                throw new Exception("Date and Sample indices are incorrect.");
            }
            this.highT = highT;
            this.lowT = lowT;
            this.highH = highH;
            this.lowH = lowH;
            this.phonenb = phonenb;
            setName("ENV. " + location);
            gui = makeGUI(lowT, highT, lowH, highH);
            gui2 = makeGUI2();
            setDaemon(true);
            start();
        }
        @Override
        public void run() {
            try {
                boolean TODO = true; //daily report todo flag
                showGUI(-5, false, 0, false);
                showGUI2(new TimeSeries("Temperature"), new TimeSeries("Humidity"),
                    0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);

                while(!STOP) {
                    long t0 = System.currentTimeMillis(), t1, rt;
                    int h = new java.util.Date(t0).getHours();
                    try {checkEnvironment(!DAYREP_DISABLE && h == reportHour && TODO);}
                    catch(Exception e) {
                        System.out.println("Failed opening " + getName()+ " (" + server + "): " + e.getMessage());
                        e.printStackTrace();
                        showGUI(-5, true, 0, true);
                    }
                    TODO = h != reportHour;
                    t1 = System.currentTimeMillis(); rt = envInterval * 1000 * 60 - (t1 - t0);
                    if (rt > 0) sleep(rt);
                }
            } catch (Exception e){e.printStackTrace();}
        }
        @Override
        public void destroy() {
            STOP = true;
        }


        void checkEnvironment(boolean INFOREQ)
        throws Exception { synchronized(this) {
            System.out.println("Opening " + getName()+ " (" + server + ")...");
            long t0 = System.currentTimeMillis();
            org.finj.FTPClient client = new org.finj.FTPClient();
            client.isVerbose(false);
            client.open(server);
            client.login(user, pw.toCharArray());
            java.io.ByteArrayOutputStream buf = new java.io.ByteArrayOutputStream();
            client.getFile(buf, "data.txt", true);
            client.close();
            long dt = System.currentTimeMillis() - t0;
            System.out.println((buf.size() / 1024) + "KB received from " + getName() + " (" + server + ")"
                    + " - " + (dt / 1000) + "s");
            long now = System.currentTimeMillis();
            java.io.BufferedReader r = new java.io.BufferedReader(new java.io.StringReader(buf.toString()));

            TimeSeries valT = new TimeSeries("Temperature");
            TimeSeries valH = new TimeSeries("Humidity");
            double avgT_week = 0, avgT_day = 0, avgT_hour = 0,
                   avgH_week = 0, avgH_day = 0, avgH_hour = 0;
            java.util.Date lastDate = new java.util.Date(now); lastT = 0; lastH = 0;
            int    minT_week = 99, minT_day = 99, minT_hour = 99,
                   maxT_week = 0, maxT_day = 0, maxT_hour = 0,
                   minH_week = 99, minH_day = 99, minH_hour = 99,
                   maxH_week = 0, maxH_day = 0, maxH_hour = 0;
            int n_week = 0, n_day = 0, n_hour = 0, n = 0;
            
            boolean first = true; String line;
            java.util.regex.Matcher date_M; int sample[] = null;
            int skip = sampleindices[0];
            while ((line = r.readLine()) != null) if (skip-- <= 0) try {
                if ((date_M = date_P.matcher(line)).matches()) {
                    sample = getIntArray(date_M.group(7).replace('\t', ';'), ';');
                    int     T = sample[sampleindices[1] - 1],
                            H = sample[sampleindices[2] - 1],
                            Y = Integer.parseInt(date_M.group(dateindices[0])),
                            M = Integer.parseInt(getMonth(date_M.group(dateindices[1]))),
                            D = Integer.parseInt(date_M.group(dateindices[2])),
                            h = Integer.parseInt(date_M.group(dateindices[3])),
                            m = Integer.parseInt(date_M.group(dateindices[4])),
                            s = Integer.parseInt(date_M.group(dateindices[5]));
                    java.util.Calendar c = java.util.Calendar.getInstance();
                    c.setTimeZone(timezone);
                    c.set(Y < 100 ? 2000 + Y : Y, M - 1, D, h, m, s);
                    java.util.Date date = c.getTime();
                    n++;
                    valT.addOrUpdate(new Minute(date), T * 1d);
                    valH.addOrUpdate(new Minute(date), H * 1d);
                    if (first) {first = false; lastDate = date; lastT = T; lastH = H;}
                    long hours = (now - date.getTime()) / (1000 * 60 * 60);
                    if (hours < 7 * 24) {
                        avgT_week += T; avgH_week += H; n_week++;
                        if (T < minT_week) minT_week = T;
                        if (T > maxT_week) maxT_week = T;
                        if (H < minH_week) minH_week = H;
                        if (H > maxH_week) maxH_week = H;
                    } if (hours < 24) {
                        avgT_day += T; avgH_day += H; n_day++;
                        if (T < minT_day) minT_day = T;
                        if (T > maxT_day) maxT_day = T;
                        if (H < minH_day) minH_day = H;
                        if (H > maxH_day) maxH_day = H;
                    } if (hours < 1) {
                        avgT_hour += T; avgH_hour += H; n_hour++;
                        if (T < minT_hour) minT_hour = T;
                        if (T > maxT_hour) maxT_hour = T;
                        if (H < minH_hour) minH_hour = H;
                        if (H > maxH_hour) maxH_hour = H;
                    }
                } else throw new Exception("No match");
            } catch(Exception e) {
                e.printStackTrace();
                System.out.println(location + ": Sample error: " + e.getMessage() + ":\n" + line);
                if (sample != null) for(int i = 0; i < sample.length; i++)
                    System.out.println("\tsample["+i+"] = '" + sample[i]+"'");
            }
            System.out.println(location + ": Samples: Hour " + n_hour + ": Day " + n_day + ": Week " + n_week);
            if (n_hour == 0) info(location + ": No sample during the last hour: Check the device time is properly set.");

            if (n_week != 0) {avgT_week /= n_week; avgH_week /= n_week;}
            else {avgT_hour = lastT; avgH_hour = lastH;}
            if (n_day != 0) {avgT_day /= n_day; avgH_day /= n_day;}
            else {avgT_hour = lastT; avgH_hour = lastH;}
            if (n_hour != 0) {avgT_hour /= n_hour; avgH_hour /= n_hour;}
            else {avgT_hour = lastT; avgH_hour = lastH;}
            if (simul) lastT = maxT;

            String INFO = location + ": " + lastDate + ": "
                  + (n == 0 ? " ***INVALID DATA***"
                  : Math.round(lastT) + "deg.C: " + Math.round(lastH) + "%RH"
                  + (simul ? " ***OVERHEAT SIMULATION***" : ""));
            showGUI(lastT, lastT > highT || lastT < lowT,
                    lastH, lastH < lowH  || lastH > highH);
            showGUI2(valT, valH,
                    minT_hour, avgT_hour, maxT_hour,
                    minT_day, avgT_day, maxT_day,
                    minT_week, avgT_week, maxT_week,
                    minH_hour, avgH_hour, maxH_hour,
                    minH_day, avgH_day, maxH_day,
                    minH_week, avgH_week, maxH_week);

            if (INFOREQ) {
                info("\n" + INFO);
                if (n > 0) info(location + ": "
                        + "Immediate Temperature/Humidity:                        "
                        + "                           " + lastT + "°C      /    " + lastH + "%RH");
                if (n > 0) info(location + ": "
                        + "One-Hour Minimum-Average-Maximum Temperature/Humidity : "
                        + minT_hour + "-" + Math.round(avgT_hour) + "-" + maxT_hour + "°C/"
                        + minH_hour + "-" + Math.round(avgH_hour) + "-" + maxH_hour + "%RH");
                if (n_day > 0) info(location + ": "
                        + "One-Day Minimum-Average-Maximum Temperature/Humidity  : "
                        + minT_day + "-" + Math.round(avgT_day) + "-" + maxT_day + "°C/"
                        + minH_day + "-" + Math.round(avgH_day) + "-" + maxH_day + "%RH");
                if (n_week > 0) info(location + ": "
                        + "One-Week Minimum-Average-Maximum Temperature/Humidity:"
                        + minT_week + "-" + Math.round(avgT_week) + "-" + maxT_week + "°C/"
                        + minH_week + "-" + Math.round(avgH_week) + "-" + maxH_week + "%RH");
            }

            String problem = n == 0 ? ""
            : (lastT >= maxT ? ": CRITICALLY HOT: T>" + maxT + "deg.C" : "")
            + (lastT <  maxT && avgT_hour > highT ? ": TOO HOT: T>" + highT + "deg.C"  + ": Last Hour "  + avgT_hour : "")
            + (lastT <  maxT && avgT_hour < lowT  ? ": T<" + lowT  + "deg.C" + ": Last Hour "  + avgT_hour : "")
            + (lastH <= minH ? ": CRITICALLY DRY: H<" + minH + "%RH" : "")
            + (lastH >  minH && avgH_hour > highH ? ": H>" + highH + "%RH" + ": Last Hour "  + avgH_hour : "")
            + (lastH >  minH && avgH_hour < lowH  ? ": TOO DRY: H<" + lowH  + "%RH" + ": Last Hour "  + avgH_hour : "");

            boolean ALARM = problem.length() > 0;
            if (ALARM) {
                if (COND_OK.set(false)) {
                    boolean hot = avgT_hour > highT || avgH_hour < lowH;
                    if (hot) play("toohot.WAV");
                    problem = ": CHECK AIRCON" + problem;
                }
            } else {
                if (COND_OK.set(true)) {
                    problem = ": Environmental conditions back to normal.";
                    ALARM = true;
                }
            }

            System.out.println(INFO + "\n" + (COND_OK.isSet()
                    ? "Environmental conditions are normal."
                    : "Environmental conditions are not normal."));
            
            //com.google.weather.WeatherSet weatherSet = meteo(!FORECAST_DISABLE && INFOREQ);//meteo update
            Wunderground.WeatherSet cc = meteo(!FORECAST_DISABLE && INFOREQ);//meteo update

            String forecast = null;
            if (!FORECAST_DISABLE && INFOREQ) {
/*                com.google.weather.WeatherForecast weather = new com.google.weather.WeatherForecast();
                com.google.weather.WeatherSet weatherSet = com.google.weather.WeatherForecast.parse(location);
                if (weatherSet == null) {//retry
                    try{Thread.sleep(500);} catch(Exception e) {}
                    weather = new com.google.weather.WeatherForecast();
                    weatherSet = com.google.weather.WeatherForecast.parse(location);
                }*/
                //if (weatherSet != null)
                //    forecast = weatherSet.getForecastText();
                if (cc != null)
                    forecast = cc.getForecast();
            } boolean METEO = forecast != null;

            if (ALARM || INFOREQ) alert(phonenb,
                    (INFOREQ  || simul ? "TEST: " : "")
                  + INFO
                  + (ALARM   ? problem : "")
                  + (METEO   ? ": Forecast" + forecast : ""));
        }}//eom checkEnvironment

        //com.google.weather.WeatherSet
        Wunderground.WeatherSet
        meteo(boolean req) {
            //to be overridden
            return null;
        }
        void alert(String phonenb, String x) {
            System.out.println("ALERT: " + x);
            //to be overridden
        }
        void info(String x) {
            System.out.println("INFO: " + x);
            //to be overridden
        }
        javax.swing.JPanel gui;
        javax.swing.JPanel getGUI() {
            return gui;
        }
        javax.swing.JPanel makeGUI(double lowT, double highT, double lowH, double highH) {
            javax.swing.JPanel p = new javax.swing.JPanel();
            p.setLayout(new java.awt.BorderLayout());
            javax.swing.JButton time = new javax.swing.JButton(new javax.swing.AbstractAction(" ") {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    new Thread() {
                        @Override
                        public void run() {
                            try {checkEnvironment(false);} catch(Exception ee) {
                                System.out.println("Failed opening " + getName()
                                        + " (" + server + "): " + ee.getMessage());
                            }
                        }
                    }.start();
                    javax.swing.JOptionPane.showMessageDialog(null,
                          "You requested to update the data for:\n"
                          + "\n" + getName() + "\n\n"
                          + "This will be displayed in a short while...\n"
                          + "You can click on OK.",
                          applicationString, javax.swing.JOptionPane.INFORMATION_MESSAGE);
                }
            });
            time.setFont(new java.awt.Font("Dialog", 1, 12));
            time.setForeground(java.awt.Color.RED);
            time.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
            time.setBorder(null);
            time.setFocusPainted(false);
            time.setContentAreaFilled(false);
            DialPanel dial = new DialPanel(getName(), "  °C       %RH",
                    lowT, highT, lowH, highH);
            p.add(time, java.awt.BorderLayout.NORTH);
            p.add(dial, java.awt.BorderLayout.CENTER);
            p.putClientProperty("dial", dial);
            p.putClientProperty("time", time);
            return p;
        }
        java.awt.Color DARKGREEN = new java.awt.Color(0, 128, 0);
        void showGUI(int T, boolean alertT, int H, boolean alertH) {
            DialPanel dial = (DialPanel)gui.getClientProperty("dial");
            javax.swing.JButton time = (javax.swing.JButton)gui.getClientProperty("time");
            time.setForeground(java.awt.SystemColor.textText);
            dial.setValue(T, H);
            java.util.Calendar c = java.util.Calendar.getInstance();
            c.setTimeInMillis(System.currentTimeMillis());
            int h = c.get(java.util.Calendar.HOUR_OF_DAY), m = c.get(java.util.Calendar.MINUTE);
            String mm = (m < 10 ? "0" :"") + m, hh = (h < 10 ? "0" :"") + h;
            time.setText(hh + ":" + mm +"          ("
                    + (T == -5 && H == 0 ? "not available)" : "last update time)"));
        }


        javax.swing.JPanel gui2;
        javax.swing.JPanel getGUI2() {
            return gui2;
        }

        final String[] columns = new String[] {
                "Temp.", "Min.°C", "Avg.°C", "Max.°C",
                "Humid.", "Min.%RH", "Avg.%RH", "Max.%RH"};

        final String[][] emptytable = new String[][] {
                columns,
                {"-", "-", "-", "-", "-", "-", "-", "-"},
                {"-", "-", "-", "-", "-", "-", "-", "-"},
                {"-", "-", "-", "-", "-", "-", "-", "-"}};

        javax.swing.JPanel makeGUI2() {
            javax.swing.JPanel p = new javax.swing.JPanel();
            p.setLayout(new java.awt.BorderLayout());
            javax.swing.table.DefaultTableModel model =
                    new javax.swing.table.DefaultTableModel(emptytable, columns);
            javax.swing.JTable table = new javax.swing.JTable(model);
            table.setModel(model);
            table.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_OFF);
            table.setPreferredSize(new java.awt.Dimension(graphWidth, tableHeight));
            TimeChart chart = new TimeChart(
                    "APC Environmental Station Monitoring",
                    "Temperature", "°C",
                    "Humidity", "%RH",
                    10d, 30d, -100d, 0d,
                    lowT, highT, lowH, highH);
            p.add(chart, java.awt.BorderLayout.CENTER);
            p.add(table, java.awt.BorderLayout.SOUTH);
            p.putClientProperty("chart", chart);
            p.putClientProperty("table", table);
            p.putClientProperty("model", model);
            return p;
        }
        void showGUI2(TimeSeries valT, TimeSeries valH,
                final int minT_hour, final double avgT_hour, final int maxT_hour,
                final int minT_day, final double avgT_day, final int maxT_day,
                final int minT_week, final double avgT_week, final int maxT_week,
                final int minH_hour, final double avgH_hour, final int maxH_hour,
                final int minH_day, final double avgH_day, final int maxH_day,
                final int minH_week, final double avgH_week, final int maxH_week) {
            java.util.Calendar c = java.util.Calendar.getInstance();
            c.setTimeInMillis(System.currentTimeMillis());
            int h = c.get(java.util.Calendar.HOUR_OF_DAY), m = c.get(java.util.Calendar.MINUTE),
                M = c.get(java.util.Calendar.MONTH) + 1, D = c.get(java.util.Calendar.DAY_OF_MONTH);
            String mm = (m < 10 ? "0" :"") + m, hh = (h < 10 ? "0" :"") + h,
                    MM = (M < 10 ? "0" :"") + M, DD = (D < 10 ? "0" :"") + D;
            ((TimeChart)gui2.getClientProperty("chart"))
                    .setValue(getName() + " " + MM + "/" + DD+ " " + hh + ":" + mm, valT, valH);
            javax.swing.SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    javax.swing.table.DefaultTableModel model =
                            (javax.swing.table.DefaultTableModel)gui2.getClientProperty("model");
                    model.setDataVector(new String[][] {columns,
                        {"Hour", "" + minT_hour, "" + Math.round(avgT_hour * 10) / 10, "" + maxT_hour,
                                 "Hour", "" + minH_hour, "" + Math.round(avgH_hour * 10) / 10, "" + maxH_hour},
                        {"Day", "" + minT_day, "" + Math.round(avgT_day * 10) / 10, "" + maxT_day,
                                 "Day", "" + minH_day, "" + Math.round(avgH_day * 10) / 10, "" + maxH_day},
                        {"Week", "" + minT_week, "" + Math.round(avgT_week * 10) / 10, "" + maxT_week,
                                 "Week", "" + minH_week, "" + Math.round(avgH_week * 10) / 10, "" + maxH_week}
                    }, columns);
                }
            });
        }
    }//eoc Measurer


    /**************************************************************************
                                    Network
    ***************************************************************************/


    static class Pinger extends Thread {
        boolean STOP = false, ALERTED = false, alert = true;
        long outage0 = -1, outage1 = -1;
        String from, to, host, phonenb;
        boolean simul = false;//outage simulation
        Pinger(String from, String to, String host, String alert, String phonenb) {
            this.from = from; this.to = to; this.host = host;
            this.alert = "true".equalsIgnoreCase(alert); this.phonenb = phonenb;
            setName("CNX. " + to);
            gui = makeGUI();
            setDaemon(true); start();
        }
        java.net.InetAddress addr = null;
        long ping_(int n) {
            if (simul) return -1; else try {
                if (addr == null) addr = java.net.InetAddress.getByName(host);
                long t0 = System.currentTimeMillis();
                boolean reachable = addr.isReachable(timeOut * 1000);
                long t1 = System.currentTimeMillis();
                return reachable ? t1 - t0 : -1;
            } catch(Exception e) {
                String N = ""; for(int i = 0; i < n; i++) N += ".";
                System.out.println(N + "X");
                return -1;
            }
        }
        long ping() {
            long ret = ping_(1);
            if (ret == -1)  ret = ping_(2);
            if (ret == -1)  ret = ping_(3);
            return ret;
        }
        @Override
        public void run() { try {
            while(!STOP) {long t0 = System.currentTimeMillis(); try {
                //test connection
                //info("Ping " + (addr == null ? host + "/?" : addr.toString()) + "...");
                long pingDelay; if ((pingDelay = ping()) == -1) throw new Exception();
                //info("Pong " + (addr == null ? host + "/?" : addr.toString()) + ":" + pingDelay + "ms.");
                showGUI(-1);
                if (outage0 != -1) {
                    if (outage1 == -1) outage1 = t0;
                    long restoreDelay = t0 - outage1;
                    if (restoreDelay > triggerDelay * 1000) {
                        //outage ends
                        showGUI(-1);
                        long outage = outage1 - outage0, minutes = outage / 60000;
                        if (minutes <= 0) minutes = 1;
                        if (outage > triggerDelay * 1000) {
                            String msg = from + ": " + new java.util.Date(outage1)
                                              + ": Connection resumed: " + to
                                              + ": Outage duration: " + minutes + "min";
                            System.out.println(msg);
                            if (alert) alert(phonenb, msg); else info(msg);
                        }
                        outage0 = outage1 = -1; ALERTED = false;
                    }
                } else {
                    //stable connection
                    showGUI(-1);
                }
            } catch(Exception e) {
                if (outage0 != -1) {// outage extents longer
                    outage1 = t0;
                    long outage = outage1 - outage0;
                    info("Ping " + (addr == null ? host + "/?" : addr.toString())
                        + ": outage: " + (outage / 1000) + "s");
                    showGUI(outage);
                    if (!ALERTED && outage > triggerDelay * 1000) {
                        ALERTED = true;
                        String msg = from + ": " + new java.util.Date(outage0)
                                          + ": CONNECTION LOST: " + to;
                        System.out.println(msg);
                        if (alert) alert(phonenb, msg); else info(msg);
                    }
                } else {// outage starts
                    outage0 = t0;
                    info("Ping " + (addr == null ? host + "/?" : addr.toString())
                        + ": time out: " + new java.util.Date(outage0));
                    showGUI(0);
                }
            } finally {
                long rest = timeOut * 1000 - System.currentTimeMillis() + t0;
                if (rest > 0) sleep(rest);
            }}
        } catch(Exception e) {e.printStackTrace();}}//eom:run

        @Override
        public void destroy() {
            STOP = true;
        }
        void alert(String phonenb, String x) {
            System.out.println("ALERT: " + x);
            //to be overridden
        }
        void info(String x) {
            System.out.println("Info: " + x);
            //to be overridden
        }
        javax.swing.JPanel gui;
        javax.swing.JPanel makeGUI() {
            javax.swing.JPanel p = new javax.swing.JPanel();
            p.setPreferredSize(new java.awt.Dimension(400, 45));
            p.setLayout(new java.awt.BorderLayout());
            javax.swing.JLabel title = new javax.swing.JLabel(getName());
            title.setPreferredSize(new java.awt.Dimension(400, 25));
            title.setFont(new java.awt.Font("Dialog", 1, 18));
            javax.swing.JLabel ping = new javax.swing.JLabel();
            ping.setPreferredSize(new java.awt.Dimension(400, 20));
            ping.setFont(new java.awt.Font("Dialog", 1, 12));
            title.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
            ping.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
            p.add(title, java.awt.BorderLayout.CENTER);
            p.add(ping, java.awt.BorderLayout.NORTH);
            p.add(new javax.swing.JPanel(), java.awt.BorderLayout.SOUTH);
            p.putClientProperty("ping", ping);
            p.putClientProperty("flag", new Flag(false));
            return p;
        }
        javax.swing.JPanel getGUI() {
            return gui;
        }
        void showGUI(long outage) {
            javax.swing.JLabel ping = (javax.swing.JLabel)gui.getClientProperty("ping");
            Flag f = (Flag)gui.getClientProperty("flag");
            if (outage == -1) {
                java.util.Calendar c = java.util.Calendar.getInstance();
                c.setTimeInMillis(System.currentTimeMillis());
                int h = c.get(java.util.Calendar.HOUR_OF_DAY), m = c.get(java.util.Calendar.MINUTE);
                String mm = (m < 10 ? "0" :"") + m, hh = (h < 10 ? "0" :"") + h;
                ping.setForeground(java.awt.SystemColor.controlText);
                ping.setText(hh + (f.isSet() ? ":" : ".") + mm + "          (last ping time)");
            } else {
                long s, m, h; s = outage / 1000;
                m = s / 60; s = s % 60;
                h = m / 60; m = m % 60;
                String ss = (s < 10 ? "0" :"") + s,
                       mm = (m < 10 ? "0" :"") + m,
                       hh = (h < 10 ? "0" :"") + h;
                ping.setForeground(java.awt.Color.RED);
                ping.setText(hh + ":" + mm + (f.isSet() ? ":" : ".") + ss + "       (outage duration)");
            } f.set(!f.isSet());
        }
    }


    /*
        public class PseudoPing {
         public static void main(String args[]) {
             try {
             Socket t = new Socket(args[0], 7);
             DataInputStream dis = new DataInputStream(t.getInputStream());
             PrintStream ps = new PrintStream(t.getOutputStream());
             ps.println("Hello");
             String str = is.readLine();
             if (str.equals("Hello"))
             System.out.println("Alive!") ;
             else
             System.out.println("Dead or echo port not responding");
             t.close();
         }
             catch (IOException e) {
         e.printStackTrace();}
         }
        }
    */


    /**************************************************************************
                            Companion Classes/Methods
    ***************************************************************************/


    static String[] loadConf(String filename, String args[]) throws Exception {
        java.util.Vector v = new java.util.Vector(); String line;
        System.out.println("Loading configuration...");
        System.out.println("---from the command line arguments...");
        if (args != null) for(int i = 0; i < args.length; i++) {
            line = args[i];
            if (envP.matcher(line).matches()
                    || comP.matcher(line).matches()
                    || smsDisableP.matcher(line).matches()
                    || dayrepDisableP.matcher(line).matches()
                    || forecastDisableP.matcher(line).matches()
                    || mapP.matcher(line).matches()
                    || upsP.matcher(line).matches()
                    ) {
                v.add(line);
                System.out.println(line);
            } else throw new Exception("Invalid configuration: " + line);
        }
        System.out.println("---from the file " + filename + "...");
        java.io.FileReader fr = new java.io.FileReader(filename);
        java.io.BufferedReader br = new java.io.BufferedReader(fr);
        while ((line = br.readLine()) != null) {
            line = line.trim();
            if (line.length() == 0 || line.startsWith("#")) System.out.println(line);
            else if (envP.matcher(line).matches()
                    || comP.matcher(line).matches()
                    || smsDisableP.matcher(line).matches()
                    || dayrepDisableP.matcher(line).matches()
                    || forecastDisableP.matcher(line).matches()
                    || mapP.matcher(line).matches()
                    || upsP.matcher(line).matches()
                    || wunderP.matcher(line).matches()
                    ) {
                v.add(line);
                System.out.println(line);
            } else throw new Exception("Invalid configuration: " + line);
        }
        if (v.size() == 0) throw new Exception("No configuration found.");
        System.out.println("--------------------------------------------------------------------------------");
        return (String[])v.toArray(args);
    }

    static String getLog(String logname) {return logname + ".log";}
    static void log(String logname, String x) {try {
        java.io.File dir = new java.io.File(System.getProperty("user.home") + "/.SMSmon");
        java.io.File log = new java.io.File(dir, getLog(logname));
        if (!dir.exists()) if (!dir.mkdir()) throw new java.io.IOException("Cannot create directory: " + dir.getPath());
        java.io.FileWriter fw = new java.io.FileWriter(log, true);
        fw.write(x + "\r\n");
        fw.close();
    }catch(java.io.IOException e) {System.out.println("Cannot write to file: " + getLog(logname));}}

    static class OutboundNotification implements org.smslib.IOutboundMessageNotification {
        public void process(String gatewayId, org.smslib.OutboundMessage msg) {
            System.out.println("Outbound handler called from Gateway: " + gatewayId);
            System.out.println(msg);
        }

        public void process(AGateway gateway, OutboundMessage msg) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }

    static class Flag {
        boolean x = false;
        Flag(boolean x) {this.x = x;}
        boolean isSet() {return x;}
        boolean set(boolean x) {
            boolean change = this.x != x;
            this.x = x;
            return change;
        }
    }

    static int[] getIntArray(String x, char sep) {
        int i = 0, j = 0, n = 0; while(i < x.length()) {
            while(j < x.length() && x.charAt(j) != sep) j++;
            try {
                String s = x.substring(i, j).trim();
                if (s.length() == 0) s = "0";
                Integer.parseInt(s); n++;
            } catch(Exception e) {
                //e.printStackTrace();
            }
            i = j + 1; j = i;
        } int[] ret = new int[n]; i = j = n = 0; while(i < x.length()) {
            while(j < x.length() && x.charAt(j) != sep) j++;
            try {
                String s = x.substring(i, j).trim();
                if (s.length() == 0) s = "0";
                ret[n] = Integer.parseInt(s); n++;
            } catch(Exception e) {
                //e.printStackTrace();
            }
            i = j + 1; j = i;
        } return ret;
    }

    static String[] getStringArray(String x, char sep) {
        int i = 0, j = 0, n = 0; while(i < x.length()) {
            while(j < x.length() && x.charAt(j) != sep) j++;
            try { if (x.substring(i, j).length() > 0) n++;
            } catch(Exception e) {e.printStackTrace();}
            i = j + 1; j = i;
        } String[] ret = new String[n]; i = j = n = 0; while(i < x.length()) {
            while(j < x.length() && x.charAt(j) != sep) j++;
            try { if (x.substring(i, j).length() > 0)
                ret[n++] = x.substring(i, j);
            } catch(Exception e) {e.printStackTrace();}
            i = j + 1; j = i;
        } return ret;
    }

    static String getMonth(String x) {
        x = x.toLowerCase();
        if (x.startsWith("jan")) return "1";
        else if (x.startsWith("feb")) return "2";
        else if (x.startsWith("mar")) return "3";
        else if (x.startsWith("apr")) return "4";
        else if (x.startsWith("may")) return "5";
        else if (x.startsWith("jun")) return "6";
        else if (x.startsWith("jul")) return "7";
        else if (x.startsWith("aug")) return "8";
        else if (x.startsWith("sep")) return "9";
        else if (x.startsWith("oct")) return "10";
        else if (x.startsWith("nov")) return "11";
        else if (x.startsWith("dec")) return "12";
        else return x;
    }
    static boolean doExport(String resource, java.io.File out) { try {
        if (!out.exists()) {
            java.io.InputStream is = null; {
                try {is = ClassLoader.getSystemResourceAsStream(resource);} catch(Exception e) {}
            } if (is == null)
                try {is = new java.net.URL(resource).openStream();} catch(Exception e) {}
            if (is == null)
                throw new Exception("Cannot find resource: " + resource);
            int total = is.available();
            byte buf[] = new byte[total];
            int done = 0; while (done < total) {
                    int n = is.read(buf, done, total - done);
                    if (n == -1) break;
                    else done += n;
            }
            is.close();
            java.io.FileOutputStream fos = new java.io.FileOutputStream(out);
            fos.write(buf);
            fos.close();
        } return true;
    } catch(Exception e) {System.out.println(e.getMessage()); return false;}}

    static boolean doExportLib(String resource, String subfolder) {
        java.io.File home = new java.io.File(System.getProperty("java.home"));
        if (!home.exists() || home.isFile()) return false;
        resource = resource.replace('\\', '/');
        int i = resource.lastIndexOf('/');
        String name = i == -1 ? null : resource.substring(i + 1);
        if (name == null) return false;
        java.io.File out = new java.io.File(home, subfolder + "/" + name);
        if (out.exists()) return true;
        String cd = null; try {
            cd = "file:" + new java.io.File(".").getCanonicalPath().replace('\\', '/');
        } catch(Exception e) {}
        if (cd == null) return false;
//System.out.println("cd="+cd);
        resource = resource.replaceAll("jar:", "jar:" + cd + "/").replaceAll("/+", "/");
        System.out.println("Export Library:\n---Source: " + resource + "\n---Dest:   " + out);
        return doExport(resource, out);
    }

    static javax.swing.ImageIcon importIconNoException(String resource)  {try{
        return importIcon(resource);
    } catch(Exception e) {
        e.printStackTrace();
        return null;
    }}
    static javax.swing.ImageIcon importIcon(String resource) throws Exception {
        java.io.InputStream is = null;
        java.io.File f = new java.io.File(resource);
        if (f.exists() && f.isFile() && !f.isDirectory()) is = new java.io.FileInputStream(f);
        else is = ClassLoader.getSystemResourceAsStream(resource);
        int total = is.available();
        byte buf[] = new byte[total];
        int done = 0; while (done < total) {
            int n = is.read(buf, done, total - done);
            if (n == -1) break;
            else done += n;
        } is.close();
        return new javax.swing.ImageIcon(buf);
    }

    static boolean isWindows() {
        if (!System.getProperty("os.name").contains("Windows"))
            return false;
        else return true;
    }

    
    static void play(final String resource) {
        new Thread() {
            @Override
            public void run() {try{
                java.net.URL url = ClassLoader.getSystemResource(resource);
                if (url == null) throw new Exception("Resource not found.");
                javax.sound.sampled.AudioInputStream audioInputStream =
                        javax.sound.sampled.AudioSystem.getAudioInputStream(url);
                javax.sound.sampled.AudioFormat audioFormat = audioInputStream.getFormat();
                javax.sound.sampled.SourceDataLine line = null;
                javax.sound.sampled.DataLine.Info info =
                        new javax.sound.sampled.DataLine.Info(javax.sound.sampled.SourceDataLine.class, audioFormat);
                line = (javax.sound.sampled.SourceDataLine) javax.sound.sampled.AudioSystem.getLine(info);
                line.open(audioFormat); line.start();
                int	n = 0; byte[] buf = new byte[100000]; while (n != -1) {
                    n = audioInputStream.read(buf, 0, buf.length);
                    if (n >= 0) line.write(buf, 0, n);
                } line.drain(); line.close();
            } catch(Exception e) {
                System.out.println(
                        "Cannot play sound: " + resource + "\n"
                        + e.getMessage());
            }}
        }.start();
    }




   /**************************************************************************
                                    Dials
    ***************************************************************************/


     static class DialPanel extends javax.swing.JPanel {
         DefaultValueDataset dataset1;
         DefaultValueDataset dataset2;
         public void setValue(int x, int y) {
             dataset1.setValue(new Integer(x));
             dataset2.setValue(new Integer(y));
         }
         public DialPanel(String title, String label,
                 double lowT, double highT, double lowH, double highH) {
                 super(new java.awt.BorderLayout());
                 dataset1 = new DefaultValueDataset(-5D);
                 dataset2 = new DefaultValueDataset(0D);
                 java.awt.Color color = new java.awt.Color(192, 0, 0);
                 java.awt.Color color1 = new java.awt.Color(0, 0, 128);

                 DialPlot dialplot = new DialPlot();
                 dialplot.setView(0.0D, 0.0D, 1.0D, 1.0D);
                 dialplot.setDataset(0, dataset1);
                 dialplot.setDataset(1, dataset2);
                 StandardDialFrame standarddialframe = new StandardDialFrame();
                 standarddialframe.setBackgroundPaint(java.awt.Color.lightGray);
                 standarddialframe.setForegroundPaint(java.awt.Color.darkGray);
                 dialplot.setDialFrame(standarddialframe);
                 java.awt.GradientPaint gradientpaint = new java.awt.GradientPaint(
                         new java.awt.Point(), java.awt.Color.white,
                         new java.awt.Point(), new java.awt.Color(170, 170, 220));
                 DialBackground dialbackground = new DialBackground(gradientpaint);
                 dialbackground.setGradientPaintTransformer(
                        new StandardGradientPaintTransformer(GradientPaintTransformType.VERTICAL));
                 dialplot.setBackground(dialbackground);

                 DialTextAnnotation dialtextannotation = new DialTextAnnotation(label);
                 dialtextannotation.setFont(new java.awt.Font("Dialog", 1, 14));
                 dialtextannotation.setRadius(0.70D);
                 dialplot.addLayer(dialtextannotation);

                 DialValueIndicator dialvalueindicator = new DialValueIndicator(0);
                 dialvalueindicator.setFont(new java.awt.Font("Dialog", 0, 10));
                 dialvalueindicator.setOutlinePaint(java.awt.Color.red);
                 dialvalueindicator.setRadius(0.60D);
                 dialvalueindicator.setAngle(-103D);
                 dialplot.addLayer(dialvalueindicator);

                 DialValueIndicator dialvalueindicator1 = new DialValueIndicator(1);
                 dialvalueindicator1.setFont(new java.awt.Font("Dialog", 0, 10));
                 dialvalueindicator1.setOutlinePaint(java.awt.Color.blue);
                 dialvalueindicator1.setRadius(0.60D);
                 dialvalueindicator1.setAngle(-77D);
                 dialplot.addLayer(dialvalueindicator1);

                 StandardDialScale standarddialscale =
                         new StandardDialScale(-5D, 45D, -120D, -300D, 5D, 4);
                 standarddialscale.setTickRadius(0.88D);
                 standarddialscale.setTickLabelOffset(0.15D);
                 standarddialscale.setTickLabelFont(new java.awt.Font("Dialog", 0, 14));
                 standarddialscale.setMajorTickPaint(color);
                 standarddialscale.setMinorTickPaint(color);
                 standarddialscale.setTickLabelPaint(color);
                 dialplot.addScale(0, standarddialscale);

                 StandardDialScale standarddialscale1 =
                         new StandardDialScale(0.0D, 100D, -120D, -300D, 10D, 4);
                 standarddialscale1.setTickRadius(0.5D);
                 standarddialscale1.setTickLabelOffset(0.15D);
                 standarddialscale1.setTickLabelFont(new java.awt.Font("Dialog", 0, 10));
                 standarddialscale1.setMajorTickPaint(color1);
                 standarddialscale1.setMinorTickPaint(color1);
                 standarddialscale1.setTickLabelPaint(color1);
                 dialplot.addScale(1, standarddialscale1);
                 dialplot.mapDatasetToScale(1, 1);

                 StandardDialRange standarddialrange_lowT =
                         new StandardDialRange(-5D, lowT, color);
                 standarddialrange_lowT.setScaleIndex(0);
                 standarddialrange_lowT.setInnerRadius(0.88D);
                 standarddialrange_lowT.setOuterRadius(0.88D);
                 dialplot.addLayer(standarddialrange_lowT);

                 StandardDialRange standarddialrange_highT =
                         new StandardDialRange(highT, 45D, color);
                 standarddialrange_highT.setScaleIndex(0);
                 standarddialrange_highT.setInnerRadius(0.88D);
                 standarddialrange_highT.setOuterRadius(0.88D);
                 dialplot.addLayer(standarddialrange_highT);

                 StandardDialRange standarddialrange_lowH =
                         new StandardDialRange(0D, lowH, color1);
                 standarddialrange_lowH.setScaleIndex(1);
                 standarddialrange_lowH.setInnerRadius(0.5D);
                 standarddialrange_lowH.setOuterRadius(0.5D);
                 dialplot.addLayer(standarddialrange_lowH);

                 StandardDialRange standarddialrange_highH =
                         new StandardDialRange(highH, 100D, color1);
                 standarddialrange_highH.setScaleIndex(1);
                 standarddialrange_highH.setInnerRadius(0.5D);
                 standarddialrange_highH.setOuterRadius(0.5D);
                 dialplot.addLayer(standarddialrange_highH);

                 org.jfree.chart.plot.dial.DialPointer.Pin pin =
                         new org.jfree.chart.plot.dial.DialPointer.Pin(1);
                 pin.setRadius(0.55D);
                 pin.setPaint(color);
                 dialplot.addPointer(pin);

                 org.jfree.chart.plot.dial.DialPointer.Pointer pointer =
                         new org.jfree.chart.plot.dial.DialPointer.Pointer(0);
                 dialplot.addPointer(pointer);
                 DialCap dialcap = new DialCap();
                 dialcap.setRadius(0.10D);
                 pin.setPaint(color1);
                 dialplot.setCap(dialcap);

                 JFreeChart jfreechart = new JFreeChart(dialplot);
                 jfreechart.setTitle(title);
                 ChartPanel chartpanel = new ChartPanel(jfreechart);
                 chartpanel.setPreferredSize(new java.awt.Dimension(monitorWidth, monitorWidth));
                 add(chartpanel);
         }
     }


    /**************************************************************************
                                    Charts
    ***************************************************************************/


    static double chghum(double H) {    //0..100
        return (100 - H)                //100..0
                * 0.2                   //20..0
                + 10;                   //30..10
    }

    static class TimeChart extends javax.swing.JPanel {
        String subtitle, axis1, unit1, axis2, unit2;
        double floorT, ceilT, floorH, ceilH;
        int highT, lowT, highH, lowH;
        ChartPanel cp = null;
        TimeChart(String subtitle,
                String axis1, String unit1,
                String axis2, String unit2,
                double floorT, double ceilT, double floorH, double ceilH,
                int highT, int lowT, int highH, int lowH) {
            this.subtitle = subtitle;
            this.axis1 = axis1;
            this.unit1 = unit1;
            this.axis2 = axis2;
            this.unit2 = unit2;
            this.floorT = floorT;
            this.ceilT = ceilT;
            this.floorH = floorH;
            this.ceilH = ceilH;
            this.highT = highT;
            this.lowT = lowT;
            this.highH = highH;
            this.lowH = lowH;
        }
        void setValue(String title, TimeSeries valT, TimeSeries valH) {
                TimeSeriesCollection values = new TimeSeriesCollection();
                values.addSeries(valT);
                values.addSeries(valH);

                YIntervalSeriesCollection intervals = new YIntervalSeriesCollection(); {
                YIntervalSeries norH = new YIntervalSeries("norH");
                YIntervalSeries varH = new YIntervalSeries("varH");
                YIntervalSeries norT = new YIntervalSeries("norT");
                YIntervalSeries varT = new YIntervalSeries("varT");
                long HALFDAYMS = 1000 * 60 * 60 * 24;
                //double normalT = (lowT + highT) * 0.5d;
                for(int i = 0; i < valT.getItemCount(); i++) {
                    long t0 = valT.getTimePeriod(i).getMiddleMillisecond();
                    long t1 = t0 - HALFDAYMS;
                    long t2 = t0 + HALFDAYMS;
                    double x = valT.getValue(i).doubleValue();
                    int n = 0; double avg = x, min = x, max = x;
                    for(int j = 0; j < valT.getItemCount(); j++) {
                        long t = valT.getTimePeriod(j).getMiddleMillisecond();
                        if (t1 < t && t < t2) {
                            double y = valT.getValue(j).doubleValue();
                            avg += y; if (y < min) min = y; if (y > max) max = y; n++;
                        }
                    } if (n != 0) avg /= n;
                    varT.add(t0, x, min, max);
                    norT.add(t0, avg, lowT, highT);
                }
                double normalH = (lowH + highH) * 0.5d;
                for(int i = 0; i < valH.getItemCount(); i++) {
                    long t0 = valH.getTimePeriod(i).getMiddleMillisecond();
                    long t1 = t0 - HALFDAYMS;
                    long t2 = t0 + HALFDAYMS;
                    double x = chghum(valH.getValue(i).doubleValue());
                    int n = 0; double avg = x, min = x, max = x;
                    for(int j = 0; j < valH.getItemCount(); j++) {
                        long t = valH.getTimePeriod(j).getMiddleMillisecond();
                        if (t1 < t && t < t2) {
                            double y = chghum(valH.getValue(j).doubleValue());
                            avg += y; if (y < min) min = y; if (y > max) max = y; n++;
                        }
                    } if (n != 0) avg /= n;
                    varH.add(t0, x, min, max);
//                    norH.add(t0, avg, chghum(lowH), chghum(highH));
                    norH.add(t0, avg, avg, avg);
                }

                YIntervalSeries varT_ = new YIntervalSeries("varT");
                for(int i = 0; i < varT.getItemCount(); i++) {
                    long t0 = valT.getTimePeriod(i).getMiddleMillisecond();
                    long t1 = t0 - HALFDAYMS;
                    long t2 = t0 + HALFDAYMS;
                    double avg = varT.getYValue(i);
                    int n = 0; double min = 0, max = 0;
                    for(int j = 0; j < varT.getItemCount(); j++) {
                        long t = valT.getTimePeriod(j).getMiddleMillisecond();
                        if (t1 < t && t < t2) {
                            double low = varT.getYLowValue(j);
                            double high = varT.getYHighValue(j);
                            min += low; max += high; n++;
                        }
                    } if (n != 0) {min /= n; max /= n;}
                    varT_.add(t0, avg, min, max);
                } varT = varT_;
                YIntervalSeries varH_ = new YIntervalSeries("varH");
                for(int i = 0; i < varH.getItemCount(); i++) {
                    long t0 = valH.getTimePeriod(i).getMiddleMillisecond();
                    long t1 = t0 - HALFDAYMS;
                    long t2 = t0 + HALFDAYMS;
                    double avg = varH.getYValue(i);
                    int n = 0; double min = 0, max = 0;
                    for(int j = 0; j < varH.getItemCount(); j++) {
                        long t = valT.getTimePeriod(j).getMiddleMillisecond();
                        if (t1 < t && t < t2) {
                            double low = varH.getYLowValue(j);
                            double high = varH.getYHighValue(j);
                            min += low; max += high; n++;
                        }
                    } if (n != 0) {min /= n; max /= n;}
                    varH_.add(t0, avg, min, max);
                } varH = varH_;

                intervals.addSeries(varT);
                intervals.addSeries(norT);
                intervals.addSeries(varH);
                intervals.addSeries(norH);
            }


            JFreeChart chart = ChartFactory.createXYLineChart(
                title, "Time", axis1 + " " + unit1, intervals, PlotOrientation.VERTICAL,
                false /*legend*/, false /*tooltips*/, false /*urls*/);
            chart.addSubtitle(new TextTitle(subtitle));
            chart.setBackgroundPaint(java.awt.SystemColor.control);
            XYPlot plot = chart.getXYPlot();
            plot.setBackgroundPaint(new java.awt.GradientPaint(
                    new java.awt.Point(), new java.awt.Color(170, 170, 220),
                    new java.awt.Point(), java.awt.SystemColor.white));
            plot.setDomainGridlinePaint(java.awt.SystemColor.black);
            plot.setRangeGridlinePaint(java.awt.SystemColor.lightGray);
            plot.setAxisOffset(new RectangleInsets(5.0, 5.0, 5.0, 5.0));

            DeviationRenderer renderer = new DeviationRenderer(true, false);
            plot.setRenderer(renderer);

            renderer.setSeriesStroke(0, new java.awt.BasicStroke(1.0f));
            renderer.setSeriesStroke(1, new java.awt.BasicStroke(3.0f,
                    java.awt.BasicStroke.CAP_ROUND, java.awt.BasicStroke.JOIN_ROUND));
            renderer.setSeriesStroke(2, new java.awt.BasicStroke(1.0f));
            renderer.setSeriesStroke(3, new java.awt.BasicStroke(3.0f,
                    java.awt.BasicStroke.CAP_ROUND, java.awt.BasicStroke.JOIN_ROUND));

            renderer.setSeriesPaint(0, java.awt.SystemColor.red);
            renderer.setSeriesFillPaint(0, java.awt.SystemColor.red);
            renderer.setSeriesPaint(1, java.awt.SystemColor.red);
            renderer.setSeriesFillPaint(1, new java.awt.Color(170, 255, 170));
            renderer.setSeriesPaint(2, java.awt.SystemColor.blue);
            renderer.setSeriesFillPaint(2, java.awt.SystemColor.blue);
            renderer.setSeriesPaint(3, java.awt.SystemColor.blue);
            renderer.setSeriesFillPaint(3, java.awt.SystemColor.blue);

            ValueAxis a0 = ChartFactory.createTimeSeriesChart(
                title, "Time", axis1 + " " + unit1,
                values, false, false, false).getXYPlot().getDomainAxis();
            a0.setPositiveArrowVisible(true);
            plot.setDomainAxis(a0);

            ValueAxis a1 = plot.getRangeAxis(0);
            a1.setRange(floorT, ceilT);
            a1.setAutoTickUnitSelection(false);
            a1.setMinorTickCount((int)Math.round(ceilT - floorT));
            a1.setPositiveArrowVisible(true);
            a1.setLabelPaint(java.awt.SystemColor.red);
            a1.setTickLabelPaint(java.awt.SystemColor.red);

            NumberAxis a2 = new NumberAxis(axis2 + " " + unit2);
            a2.setRange(floorH, ceilH);
            a2.setAutoRangeIncludesZero(false);
            a2.setLabelPaint(java.awt.SystemColor.blue);
            a2.setTickLabelPaint(java.awt.SystemColor.blue);
            a2.setNegativeArrowVisible(true);
            plot.setRangeAxis(1, a2);
            plot.setRangeAxisLocation(1, AxisLocation.TOP_OR_RIGHT);

            if (cp != null) remove(cp);
            cp = new ChartPanel(chart);
            cp.setPreferredSize(new java.awt.Dimension(graphWidth, graphHeight - tableHeight));
            add(cp);
        }
    }


}//eoc