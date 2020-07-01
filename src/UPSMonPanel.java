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


public class UPSMonPanel extends javax.swing.JPanel {

    javax.swing.JTextField[] fields;
    public UPSMonPanel() {
        initComponents();
        fields = new javax.swing.JTextField[] {
            jTextField0, jTextField1, jTextField2, jTextField3, jTextField4,
            jTextField5, jTextField6, jTextField7, jTextField8, jTextField9,
            jTextField10, jTextField11, jTextField12, jTextField13, jTextField14,
            jTextField15};
    }


    public void setValues(Object[] values) {
        try{
            for(int i = 0; i < fields.length && (values != null ? i < values.length : true); i++)
                fields[i].setText(values == null || values[i] == null ? "-" : values[i].toString());
        }catch(Exception e) {e.printStackTrace();}
        jTextField0.setBackground(jTextField0.getText().equalsIgnoreCase("selfTest")
                ? java.awt.Color.GREEN : java.awt.Color.RED);
        jTextField5.setBackground(jTextField5.getText().equalsIgnoreCase("ok")
                ? java.awt.Color.GREEN : java.awt.Color.RED);
        jTextField7.setBackground(jTextField7.getText().equalsIgnoreCase("batteryNormal")
                ? java.awt.Color.GREEN : java.awt.Color.RED);
        jTextField8.setBackground(jTextField8.getText().equalsIgnoreCase("noBatteryNeedsReplacing")
                ? java.awt.Color.GREEN : java.awt.Color.RED);
        jTextField11.setBackground(jTextField11.getText().equalsIgnoreCase("onLine")
                ? java.awt.Color.GREEN : java.awt.Color.RED);
    }

    
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jLabel0 = new javax.swing.JLabel();
        jTextField0 = new javax.swing.JTextField();
        jTextField1 = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        jTextField2 = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        jTextField3 = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        jTextField4 = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        jPanel4 = new javax.swing.JPanel();
        jLabel5 = new javax.swing.JLabel();
        jTextField5 = new javax.swing.JTextField();
        jTextField6 = new javax.swing.JTextField();
        jLabel6 = new javax.swing.JLabel();
        jPanel5 = new javax.swing.JPanel();
        jLabel7 = new javax.swing.JLabel();
        jTextField7 = new javax.swing.JTextField();
        jLabel8 = new javax.swing.JLabel();
        jTextField8 = new javax.swing.JTextField();
        jLabel9 = new javax.swing.JLabel();
        jTextField9 = new javax.swing.JTextField();
        jTextField10 = new javax.swing.JTextField();
        jLabel10 = new javax.swing.JLabel();
        jPanel3 = new javax.swing.JPanel();
        jLabel11 = new javax.swing.JLabel();
        jTextField11 = new javax.swing.JTextField();
        jTextField12 = new javax.swing.JTextField();
        jLabel12 = new javax.swing.JLabel();
        jTextField13 = new javax.swing.JTextField();
        jLabel13 = new javax.swing.JLabel();
        jTextField14 = new javax.swing.JTextField();
        jLabel14 = new javax.swing.JLabel();
        jTextField15 = new javax.swing.JTextField();
        jLabel15 = new javax.swing.JLabel();

        setPreferredSize(new java.awt.Dimension(500, 250));
        setLayout(new javax.swing.BoxLayout(this, javax.swing.BoxLayout.X_AXIS));

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder("Input"));
        jPanel1.setPreferredSize(new java.awt.Dimension(200, 400));
        jPanel1.setLayout(new java.awt.GridLayout(5, 2, 10, 10));

        jLabel0.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel0.setText("St.");
        jPanel1.add(jLabel0);

        jTextField0.setEditable(false);
        jTextField0.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        jTextField0.setText("-");
        jTextField0.setBorder(null);
        jPanel1.add(jTextField0);

        jTextField1.setEditable(false);
        jTextField1.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        jTextField1.setText("-");
        jTextField1.setBorder(null);
        jTextField1.setOpaque(false);
        jPanel1.add(jTextField1);

        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel1.setText("°C");
        jPanel1.add(jLabel1);

        jTextField2.setEditable(false);
        jTextField2.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        jTextField2.setText("-");
        jTextField2.setBorder(null);
        jTextField2.setOpaque(false);
        jPanel1.add(jTextField2);

        jLabel2.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel2.setText("V");
        jPanel1.add(jLabel2);

        jTextField3.setEditable(false);
        jTextField3.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        jTextField3.setText("-");
        jTextField3.setBorder(null);
        jTextField3.setOpaque(false);
        jPanel1.add(jTextField3);

        jLabel3.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel3.setText("Hz");
        jPanel1.add(jLabel3);

        jTextField4.setEditable(false);
        jTextField4.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        jTextField4.setText("-");
        jTextField4.setBorder(null);
        jTextField4.setOpaque(false);
        jPanel1.add(jTextField4);

        jLabel4.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel4.setText("%");
        jPanel1.add(jLabel4);

        add(jPanel1);

        jPanel2.setPreferredSize(new java.awt.Dimension(200, 400));
        jPanel2.setLayout(new javax.swing.BoxLayout(jPanel2, javax.swing.BoxLayout.Y_AXIS));

        jPanel4.setBorder(javax.swing.BorderFactory.createTitledBorder("APC Smart-UPS"));
        jPanel4.setLayout(new java.awt.GridLayout(2, 2, 10, 10));

        jLabel5.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel5.setText("Diag");
        jPanel4.add(jLabel5);

        jTextField5.setEditable(false);
        jTextField5.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        jTextField5.setText("-");
        jTextField5.setBorder(null);
        jPanel4.add(jTextField5);

        jTextField6.setEditable(false);
        jTextField6.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        jTextField6.setText("-");
        jTextField6.setBorder(null);
        jTextField6.setOpaque(false);
        jPanel4.add(jTextField6);

        jLabel6.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel6.setText("°C");
        jPanel4.add(jLabel6);

        jPanel2.add(jPanel4);

        jPanel5.setBorder(javax.swing.BorderFactory.createTitledBorder("Battery"));
        jPanel5.setLayout(new java.awt.GridLayout(4, 2, 10, 10));

        jLabel7.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel7.setText("St.");
        jPanel5.add(jLabel7);

        jTextField7.setEditable(false);
        jTextField7.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        jTextField7.setText("-");
        jTextField7.setBorder(null);
        jPanel5.add(jTextField7);

        jLabel8.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel8.setText("Repl.");
        jPanel5.add(jLabel8);

        jTextField8.setEditable(false);
        jTextField8.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        jTextField8.setText("-");
        jTextField8.setBorder(null);
        jPanel5.add(jTextField8);

        jLabel9.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel9.setText("Bad");
        jPanel5.add(jLabel9);

        jTextField9.setEditable(false);
        jTextField9.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        jTextField9.setText("-");
        jTextField9.setBorder(null);
        jTextField9.setOpaque(false);
        jPanel5.add(jTextField9);

        jTextField10.setEditable(false);
        jTextField10.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        jTextField10.setText("-");
        jTextField10.setBorder(null);
        jTextField10.setOpaque(false);
        jPanel5.add(jTextField10);

        jLabel10.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel10.setText("%");
        jPanel5.add(jLabel10);

        jPanel2.add(jPanel5);

        add(jPanel2);

        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder("Output"));
        jPanel3.setPreferredSize(new java.awt.Dimension(200, 400));
        jPanel3.setLayout(new java.awt.GridLayout(5, 2, 10, 10));

        jLabel11.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel11.setText("St.");
        jPanel3.add(jLabel11);

        jTextField11.setEditable(false);
        jTextField11.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        jTextField11.setText("-");
        jTextField11.setBorder(null);
        jPanel3.add(jTextField11);

        jTextField12.setEditable(false);
        jTextField12.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        jTextField12.setText("-");
        jTextField12.setBorder(null);
        jTextField12.setOpaque(false);
        jPanel3.add(jTextField12);

        jLabel12.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel12.setText("A");
        jPanel3.add(jLabel12);

        jTextField13.setEditable(false);
        jTextField13.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        jTextField13.setText("-");
        jTextField13.setBorder(null);
        jTextField13.setOpaque(false);
        jPanel3.add(jTextField13);

        jLabel13.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel13.setText("V");
        jPanel3.add(jLabel13);

        jTextField14.setEditable(false);
        jTextField14.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        jTextField14.setText("-");
        jTextField14.setBorder(null);
        jTextField14.setOpaque(false);
        jPanel3.add(jTextField14);

        jLabel14.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel14.setText("Hz");
        jPanel3.add(jLabel14);

        jTextField15.setEditable(false);
        jTextField15.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        jTextField15.setText("-");
        jTextField15.setBorder(null);
        jTextField15.setOpaque(false);
        jPanel3.add(jTextField15);

        jLabel15.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel15.setText("min");
        jPanel3.add(jLabel15);

        add(jPanel3);
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel0;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    protected javax.swing.JTextField jTextField0;
    protected javax.swing.JTextField jTextField1;
    protected javax.swing.JTextField jTextField10;
    protected javax.swing.JTextField jTextField11;
    protected javax.swing.JTextField jTextField12;
    protected javax.swing.JTextField jTextField13;
    protected javax.swing.JTextField jTextField14;
    protected javax.swing.JTextField jTextField15;
    protected javax.swing.JTextField jTextField2;
    protected javax.swing.JTextField jTextField3;
    protected javax.swing.JTextField jTextField4;
    protected javax.swing.JTextField jTextField5;
    protected javax.swing.JTextField jTextField6;
    protected javax.swing.JTextField jTextField7;
    protected javax.swing.JTextField jTextField8;
    protected javax.swing.JTextField jTextField9;
    // End of variables declaration//GEN-END:variables

    

}
