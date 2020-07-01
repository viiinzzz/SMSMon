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

import org.snmp4j.*;
import org.snmp4j.event.*;
import org.snmp4j.mp.*;
import org.snmp4j.smi.*;
import org.snmp4j.transport.*;
import net.percederberg.mibble.*;
import net.percederberg.mibble.value.*;


public class UPSmon extends Thread {

    public static final String
        APC_SMARTUPS = ".1.3.6.1.4.1.318.1.1",

        INPUT_FAILURE       = APC_SMARTUPS + ".1.3.2.5.0",
        OUTPUT_STATUS       = APC_SMARTUPS + ".1.4.1.1.0",
        RUNTIME_MINUTE      = APC_SMARTUPS + ".1.2.2.3.0",
        LOAD_PERCENT        = APC_SMARTUPS + ".1.4.2.3.0",

        OUTPUT_AMPERE       = APC_SMARTUPS + ".1.4.2.4.0",
        OUTPUT_VOLT         = APC_SMARTUPS + ".1.4.2.1.0",
        OUTPUT_HERTZ        = APC_SMARTUPS + ".1.4.2.2.0",
        INPUT_VOLT          = APC_SMARTUPS + ".1.3.2.1.0",
        INPUT_HERTZ         = APC_SMARTUPS + ".1.3.2.4.0",

        INTERNAL_DEGREE     = APC_SMARTUPS + ".1.2.2.2.0",
        ENVIRONMENT_DEGREE  = APC_SMARTUPS + ".10.2.3.2.1.4.1",
        PROBE_DEGREE        = APC_SMARTUPS + ".10.1.3.3.1.4.0",
        PROBE_HUMID         = APC_SMARTUPS + ".10.1.3.3.1.6.0",

        BATTERY_STATUS      = APC_SMARTUPS + ".1.2.1.1.0",
        BATTERY_PERCENT     = APC_SMARTUPS + ".1.2.2.1.0",
        BATTERY_REPLACE     = APC_SMARTUPS + ".1.2.2.4.0",
        BATTERY_BADNUM      = APC_SMARTUPS + ".1.2.2.6.0",

        DIAG_RESULT         = APC_SMARTUPS + ".1.7.2.3.0",
        DIAG_DATE           = APC_SMARTUPS + ".1.7.2.4.0",

/*
.2.1.1.0           APC UPS Environment Ambient Temperature
.2.1.2.0           APC UPS Environment Relative Humidity
.2.1.3.0           APC UPS Environment Ambient Temperature 2
.2.1.4.0           APC UPS Environment Relative Humidity 2
.2.2.2.1.5.1      APC UPS Contact Entry 1
.2.2.2.1.5.2      APC UPS Contact Entry 2
.2.2.2.1.5.3      APC UPS Contact Entry 3
.2.2.2.1.5.4      APC UPS Contact Entry 4
  */


        //PROBE_DEGREE, PROBE_HUMID,
        //"Probe Temperature", "Probe Humidity",
        //"°C", "%RH",

        APC_SMARTUPS_OIDS[] = new String[] {
            INPUT_FAILURE, ENVIRONMENT_DEGREE, INPUT_VOLT, INPUT_HERTZ, LOAD_PERCENT,
            DIAG_RESULT, INTERNAL_DEGREE, BATTERY_STATUS, BATTERY_REPLACE, BATTERY_BADNUM, BATTERY_PERCENT,
            OUTPUT_STATUS, OUTPUT_AMPERE, OUTPUT_VOLT, OUTPUT_HERTZ, RUNTIME_MINUTE
        }, APC_SMARTUPS_LABELS[] = new String[]{
            "Input Failure", "Environment Temperature", "Input Voltage", "Input Frequency", "Output Load",
            "Last Diagnostic Result", "Internal Temperature", "Battery Status", "Battery Replace", "Battery Bad Number", "Battery Capacity",
            "Output Status", "Output Current", "Output Voltage", "Output Frequency", "Runtime Remaining"
        }, APC_SMARTUPS_UNITS[] = new String[] {
            "", "°C", "V", "Hz", "%",
            "", "°C", "", "", "", "%",
            "", "A", "V", "Hz", "min."};

    public static java.util.Hashtable APC_SMARTUPS_VALUES = getAPC_SMARTUPS_VALUES();
    static java.util.Hashtable getAPC_SMARTUPS_VALUES() { try {
        if (APC_SMARTUPS_VALUES != null) return APC_SMARTUPS_VALUES;
        System.out.println("Loading APC MIB...");
        java.io.InputStream is = ClassLoader.getSystemResourceAsStream("POWERNET398");
        if (is == null) throw new Exception();
        java.io.BufferedInputStream bis = new java.io.BufferedInputStream(is);
        java.io.InputStreamReader isr = new java.io.InputStreamReader(bis);
        System.out.println("\n");
        return APC_SMARTUPS_VALUES = extractOids(new MibLoader().load(isr), UPSmon.APC_SMARTUPS_OIDS);
    } catch(Exception e) {System.out.println("Cannot load MIB."); /*System.exit(-1);*/ return null;}}


    public static void main(String args[]) {
        try {
            javax.swing.UIManager.setLookAndFeel(javax.swing.UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {}

        new UPSmon("PARIS APC3.5KW01", "xxx.xxx.xxx.xxx", "monitor").showFrame();
        new UPSmon("PARIS APC3.5KW02", "xxx.xxx.xxx.xxx", "monitor").showFrame();
        Thread T = new Thread(UPSMons, "UPSMons") {
            @Override
            public void run() { try {
                Thread[] Ts; int n; do{
                    int j = UPSMons.enumerate(Ts = new Thread[UPSMons.activeCount()]);
                    n = 0; for(int i = 0; i < j; i++) if (Ts[i].getName().startsWith("UPSMon_")) n++;
                    //System.out.println(n + " active UPS monitoring threads...");
                    sleep(1000);
                } while(n > 0);
            } catch(Exception e) {}}
        }; T.start(); try{T.join();} catch(Exception e) {}
        //try {
        //    new java.io.BufferedReader(new java.io.InputStreamReader(System.in)).readLine();
        //} catch(Exception e) {}
        System.out.println("done.");
        System.exit(0);
    }

    void showFrame() {
        javax.swing.JFrame.setDefaultLookAndFeelDecorated(true);
        javax.swing.JFrame f = new javax.swing.JFrame(getName());
        f.setDefaultCloseOperation(javax.swing.JFrame.DISPOSE_ON_CLOSE);
        f.getContentPane().add(p, java.awt.BorderLayout.CENTER);
        f.pack();
        f.setResizable(false);
        f.setSize(500, 250);
        f.addWindowListener(new java.awt.event.WindowListener() {
            public void windowOpened(java.awt.event.WindowEvent e) {}
            public void windowClosing(java.awt.event.WindowEvent e) {}
            public void windowIconified(java.awt.event.WindowEvent e) {}
            public void windowDeiconified(java.awt.event.WindowEvent e) {}
            public void windowActivated(java.awt.event.WindowEvent e) {}
            public void windowDeactivated(java.awt.event.WindowEvent e) {}
            public void windowClosed(java.awt.event.WindowEvent e) {destroy();}
        });
        f.setVisible(true);
    }

    javax.swing.JPanel getGUI() {
        return p;
    }

    void alarm(String x, boolean set) {
        System.out.println((set ? "ALARM: " : "Info: ") + ": " + x);
    }

    boolean getSimul() {
        return q.simul;
    }

    void setSimul(boolean simul) {
        q.simul = simul;
        REFRESHNOW = true;
    }

    final int REFRESH = 60;//seconds
    boolean STOP= false;
    boolean REFRESHNOW = false;
    Question q;
    static ThreadGroup UPSMons = new ThreadGroup("UPSMons");
    UPSMonPanel p = new UPSMonPanel();
    UPSmon(String label, String IP_Address, String Community_String) {
        super(UPSMons, "UPSMon_" + label.replace(' ', '_').replace('.', '_'));
        q = new Question(getName(), IP_Address, Community_String, APC_SMARTUPS_OIDS) {
            @Override
            void alarm(String x, boolean set) {
                UPSmon.this.alarm(getName() + ": " + x, set);
            }
        };

        setDaemon(true);
        start();
    }
    @Override
    public void run() {
        System.out.println("\nAPC SmartUPS monitoring thread: " + getName());
        try { while(!STOP) {
            Object[] APC_SMARTUPS_VALUE = q.ask();
            if (APC_SMARTUPS_VALUE != null) {
                java.util.Date date = new java.util.Date(System.currentTimeMillis());
                System.out.println(getName() + " " + date);
                /*for(int i = 0; i < APC_SMARTUPS_VALUES.length; i ++)
                    System.out.println("\t" + APC_SMARTUPS_LABELS[i] + " = "
                            + (String.class.isInstance(APC_SMARTUPS_VALUES[i]) ? "'" : "")
                            + APC_SMARTUPS_VALUES[i]
                            + (String.class.isInstance(APC_SMARTUPS_VALUES[i]) ? "'" : "")
                            + APC_SMARTUPS_UNITS[i]);*/
                p.setValues(APC_SMARTUPS_VALUE);
            } for(int i = 0; i < REFRESH && !STOP && !REFRESHNOW; i++) sleep(1000);
            REFRESHNOW = false;
        }} catch(Exception e) {e.printStackTrace();}
        System.out.println(getName() + " ended.");
    }
    @Override
    public void destroy() {
        STOP = true;
    }


    static class Question {
        boolean simul = false;

        CommunityTarget target = new CommunityTarget();
        PDU pdu = new PDU();
        Snmp snmp = null;
        String name;
        Question(String name, String IP_Address, String Community_String, String[] OIDS) {
            this.name = name;
            target.setCommunity(new OctetString(Community_String));
            Address address = GenericAddress.parse("udp:" + IP_Address + "/161");
            target.setAddress(address);
            target.setRetries(1);//2
            target.setTimeout(1500);
            target.setVersion(SnmpConstants.version1);
            for(int i = 0; i < OIDS.length; i++)
                pdu.add(new VariableBinding(new OID(OIDS[i])));
            pdu.setType (PDU.GETNEXT);
            try {
                snmp = new Snmp(new DefaultUdpTransportMapping());
                snmp.listen();
            } catch(java.io.IOException e) {
                e.printStackTrace();
            }
        }

        static java.util.regex.Pattern duration_P = java.util.regex.Pattern.compile(
                "(\\d{1,2}):(\\d{2}):(\\d{2}).(\\d{2})");
        static java.util.regex.Pattern date_P = java.util.regex.Pattern.compile(
                "(\\d{2})/(\\d{2})/(\\d{2,4})");

        Object[] ask() {
             try {
                if (snmp == null) throw new Exception("not connected");
                ResponseEvent response = snmp.get(pdu, target);
                if (response.getResponse() == null) throw new Exception("time out");
                Object ret[] = new Object[response.getResponse().size()];
                for(int i = 0; i < ret.length; i++) {
                     String value = response.getResponse().get(i).toValueString();
                     String oid = response.getResponse().get(i).getOid().toString();
                     try {
                        String label = (String)((java.util.Hashtable)APC_SMARTUPS_VALUES
                                .get("." + oid)).get(value);
                        if (label != null) value = label;
                     } catch(Exception e) {}
                     java.util.regex.Matcher M;
                     if ((M = duration_P.matcher(value)).matches()) {
                         ret[i] = new Integer(Math.round((
                                 getInt(M, 1) * 3600
                                 + getInt(M, 2) * 60
                                 + getInt(M, 3)
                                 + getInt(M, 4) * 0.01f) /60));
                     } else if ((M = date_P.matcher(value)).matches()) {
                         java.util.Calendar c = java.util.Calendar.getInstance();
                         int m = getInt(M, 1), d = getInt(M, 2), y = getInt(M, 3);
                         c.set(y < 100 ? 2000 + y : y, m - 1, d, 0, 0, 1);
                         java.util.Date dt = c.getTime();
                         ret[i] = dt;
                     } else {
                         try {
                             ret[i] = new Integer(Integer.parseInt(value));
                         } catch(Exception e) {
                             ret[i] = value;
                         }
                     }
                }
                if (simul) ret[11] = "simul";

                if (InSt.set(!ret[0].toString().equalsIgnoreCase("selfTest")))
                    alarm("Input: " + ret[0], InSt.isSet());
                if (Diag.set(!ret[5].toString().equalsIgnoreCase("ok")))
                        alarm("Diagnostic: " + ret[5], Diag.isSet());
                if (BattSt.set(!ret[7].toString().equalsIgnoreCase("batteryNormal")))
                        alarm("Battery: " + ret[7], BattSt.isSet());
                if (ReplSt.set(!ret[8].toString().equalsIgnoreCase("noBatteryNeedsReplacing")))
                        alarm("Battery Replacement: " + ret[8], ReplSt.isSet());
                if (OutSt.set(!ret[11].toString().equalsIgnoreCase("onLine")))
                        alarm("Output: " + ret[11], OutSt.isSet());
                return ret;
            } catch(Exception e) {
                System.out.println(e.getMessage());
                return null;
            }
        }

        void alarm(String x, boolean set) {
            System.out.println((set ? "ALARM: " : "Info: ") + name + ": " + x);
        }

        SMSmon.Flag
                InSt = new SMSmon.Flag(false),
                Diag = new SMSmon.Flag(false),
                BattSt = new SMSmon.Flag(false),
                ReplSt = new SMSmon.Flag(false),
                OutSt = new SMSmon.Flag(false);
    }//eosc:Question

    static int getInt(java.util.regex.Matcher M, int group) {
        try {
            String x = M.group(group);
            return Integer.parseInt(x);
        } catch(Exception e) {
            return 0;
        }
    }

    static int getInt(String x) {
        try {
            return Integer.parseInt(x);
        } catch(Exception e) {
            return 0;
        }
    }



    public static java.util.Hashtable extractOids(Mib mib, String[] oidfilter) {
        java.util.Hashtable t = new java.util.Hashtable();
        java.util.Iterator i = mib.getAllSymbols().iterator();
        while (i.hasNext()) {
            MibSymbol symbol = (MibSymbol)i.next();
            MibValue value = extractOid(symbol, t, oidfilter);
        } return t;
    }

    public static ObjectIdentifierValue extractOid(MibSymbol symbol, java.util.Hashtable t, String[] oidfilter) {
        if (symbol instanceof MibValueSymbol) {
            MibValue value = ((MibValueSymbol)symbol).getValue();
            if (value instanceof ObjectIdentifierValue) {
                ObjectIdentifierValue oiv = (ObjectIdentifierValue)value;
                String oid = "." + oiv.toString() + ".0";
                java.util.Hashtable enumValues = null; try {
                    enumValues = getTable(symbol.toString().substring(symbol.toString().indexOf('{') + 2,
                        symbol.toString().indexOf('}', symbol.toString().indexOf('{') + 1) - 1));
                } catch(Exception e) {}
                for(int i = 0; i < oidfilter.length; i++)
                    if (oidfilter[i].toString().equals(oid)) {
                        t.put(oid, enumValues == null ? "" : enumValues);
                        //System.out.println(oid + " <==> " + enumValues);
                    }
                return oiv;
            } else return null;
        } else return null;
    }

    static java.util.Hashtable getTable(String x) {
        java.util.Hashtable t = new java.util.Hashtable();
        String[] xs = getStringArray(x, ',');
        if (xs != null) for(int i = 0; i < xs.length; i++) try {
            String s = xs[i].replace('(', '=').replace(')', ' ').trim();
            String label = s.substring(0, s.indexOf('='));
            String value = s.substring(label.length() + 1);
            t.put(value, label);
        } catch(Exception e) {}
        return t;
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


}//eoc
