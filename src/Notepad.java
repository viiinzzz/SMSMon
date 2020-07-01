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

import java.awt.*;
import java.awt.event.*;
import java.awt.datatransfer.*;
import java.io.*;

public class Notepad extends Frame {
     String filename;
     boolean readonly;
     TextArea text = new TextArea();
     Clipboard clip = getToolkit().getSystemClipboard();

     Notepad(boolean readonly, String path) {
         this(readonly);
         ReadFile(path);
     }
     
     Notepad(boolean readonly) {
         this.readonly = readonly;
         setLayout(new GridLayout(1,1));
         add(text);
         text.setEditable(!readonly);
         MenuBar mb = new MenuBar();
         setMenuBar(mb);
         Menu F = new Menu("File");
         mb.add(F);
         Menu E = new Menu("Edit");
         mb.add(E);


         MenuItem refresh = new MenuItem("Refresh", new MenuShortcut(KeyEvent.VK_F5, false));
         refresh.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 if (filename != null) ReadFile();
             }
         }); E.add(refresh);

         MenuItem copy = new MenuItem("Copy", new MenuShortcut(KeyEvent.VK_C, false));
         copy.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {copy();}
         }); E.add(copy);

         if (!readonly) {

             MenuItem cut = new MenuItem("Cut", new MenuShortcut(KeyEvent.VK_X, false));
             cut.addActionListener(new ActionListener() {
                 public void actionPerformed(ActionEvent e) {cut();}
             }); E.add(cut);

             MenuItem paste = new MenuItem("Paste", new MenuShortcut(KeyEvent.VK_V, false));
             paste.addActionListener(new ActionListener() {
                 public void actionPerformed(ActionEvent e) {paste();}
             }); E.add(paste);

             MenuItem newfile = new MenuItem("New", new MenuShortcut(KeyEvent.VK_N, false));
             newfile.addActionListener(new ActionListener() {
                 public void actionPerformed(ActionEvent e) {
                     text.setText(" ");
                     setTitle(filename);
                 }
             });
             F.add(newfile);

             MenuItem open = new MenuItem("Open", new MenuShortcut(KeyEvent.VK_O, false));
             open.addActionListener(new ActionListener() {
                 public void actionPerformed(ActionEvent e) {open();}
             }); F.add(open);

             MenuItem save = new MenuItem("Save", new MenuShortcut(KeyEvent.VK_S, false));
             save.addActionListener(new ActionListener() {
                 public void actionPerformed(ActionEvent e) {save();}
             }); F.add(save);

             MenuItem saveas = new MenuItem("Save As", new MenuShortcut(KeyEvent.VK_S, true));
             saveas.addActionListener(new ActionListener() {
                 public void actionPerformed(ActionEvent e) {saveas();}
             }); F.add(saveas);
         }

         MenuItem exit = new MenuItem("Exit", new MenuShortcut(KeyEvent.VK_Q, false));
         exit.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {close();}
         }); F.add(exit);

         addWindowListener(new WindowAdapter() {
             @Override
             public void windowClosing (WindowEvent e) {close();}
         });
         setSize(500,400);
         setVisible(true);
     }



     void copy() {
         String sel = text.getSelectedText();
         StringSelection clipString = new StringSelection(sel);
         clip.setContents(clipString,clipString);
     }

     void cut() {
         String sel = text.getSelectedText();
         StringSelection ss = new StringSelection(sel);
         clip.setContents(ss,ss);
         text.replaceRange(" ",text.getSelectionStart(),text.getSelectionEnd());
     }

     void paste() {
         try {
             Transferable cliptran = clip.getContents(Notepad.this);
             String sel = (String) cliptran.getTransferData(DataFlavor.stringFlavor);
             text.replaceRange(sel,text.getSelectionStart(),text.getSelectionEnd());
         } catch(Exception e) {
             System.out.println("Cannot Paste: " + e.getMessage());
         }
     }

     void open() {
         FileDialog fd = new FileDialog(Notepad.this, "Open File",FileDialog.LOAD);
         fd.show(); if (fd.getFile()!=null) {
             if (filename != null) openlist.remove(filename);
             setTitle(filename = fd.getDirectory() + fd.getFile());
             Notepad duplicate = openlist.get(filename);
             if (duplicate != null) duplicate.close();
             openlist.put(filename, this);
             ReadFile();
             text.requestFocus();
         } 
     }

     void saveas() {if (readonly) return;
         FileDialog fd = new FileDialog(Notepad.this,"Save File",FileDialog.SAVE);
         fd.show(); if (fd.getFile()!=null) {
             if (filename != null) openlist.remove(filename);
             setTitle(filename = fd.getDirectory() + fd.getFile());
             openlist.put(filename, this);
             save();
             text.requestFocus();
         }
     }

     void save() {if (readonly) return;
         if (filename != null) try {
             FileOutputStream fos = new FileOutputStream(filename);
             DataOutputStream d = new DataOutputStream(fos);
             d.writeBytes(text.getText().replaceAll("([^\\r])\\n", "$1\r\n"));
             d.close(); fos.close();
         } catch(Exception e) {
             System.out.println("Cannot Save " + filename + ": " + e.getMessage());
         }
     }

     void close() {
         if (filename != null) save(); else saveas();
         setVisible(false);
         openlist.remove(filename);
     }

     void ReadFile(String path) {
         setTitle(filename = path);
         ReadFile();
     }

     void ReadFile() {
         try {
             BufferedReader d;
             StringBuffer sb = new StringBuffer();
             d = new BufferedReader(new FileReader(filename));
             String line;
             while((line=d.readLine())!=null)
             sb.append(line + "\n");
             text.setText(sb.toString());
             d.close();
         } catch(Exception e) {
             System.out.println("Cannot Open " + filename + ": " + e.getMessage());
         }
     }


    @Override
     public void show() {
         super.show();
         text.requestFocus();
     }

     static java.util.Hashtable<String, Notepad> openlist = new java.util.Hashtable();

     static void edit(boolean readonly, String path) {
         Notepad n = openlist.get(path);
         if (n == null) openlist.put(path, n = new Notepad(readonly, path));
         n.show();
     }

}
