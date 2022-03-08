package chatserver;


import java.awt.*;
import java.awt.event.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.swing.*;


/**
 * IT NO : IT!9968216
 * NAME : D.P.K.D Balasooriya
 *Changed the whole architecture
 */


public class ChatClient {

    BufferedReader in;
    PrintWriter out;
    JFrame frame = new JFrame("Chatter");
    JTextField textField = new JTextField(40);
    JLabel lblName = new JLabel();
    JTextArea messageArea = new JTextArea(8, 40);
    JCheckBox isBroadcast = new JCheckBox("broadcast");
    JList jlist = new JList();
    String name;

    /**
     * Constructs the client by laying out the GUI and registering a
     * listener with the textfield so that pressing Return in the
     * listener sends the textfield contents to the server.  Note
     * however that the textfield is initially NOT editable, and
     * only becomes editable AFTER the client receives the NAMEACCEPTED
     * message from the server.
     */
    public ChatClient() {

        // Layout GUI
        textField.setEditable(false);
        messageArea.setEditable(false);
        isBroadcast.setSelected(true);
        frame.setLayout(new FlowLayout());

        frame.getContentPane().add(lblName );
        frame.getContentPane().add(textField );
        frame.getContentPane().add(isBroadcast);
        frame.getContentPane().add(jlist);
        frame.getContentPane().add(new JScrollPane(messageArea) );
        frame.pack();


        textField.addActionListener(new ActionListener() {
            /**
             * Responds to pressing the enter key in the textfield by sending
             * the contents of the text field to the server.    Then clear
             * the text area in preparation for the next message.
             */
            public void actionPerformed(ActionEvent e) {
                if (isBroadcast.isSelected()) {
                    out.println("BROADCAST " + textField.getText());
                    textField.setText("");
                }else if (jlist.getSelectedIndices().length >= 1){
                    List<String> selectedUsers = jlist.getSelectedValuesList();
                    selectedUsers.add(lblName.getText());
                    String userString = "DIRECTMESSAGE "+ selectedUsers.stream().collect(Collectors.joining(" "));
                    out.println(userString);
                    out.println("MESSAGE "+ textField.getText());
                    textField.setText("");
                }else{
                    showError("Select at least one recipient" , "select user");
                }

            }
        });


        /**
         * this action listener responsible for listen changers in isBroadcast check box
         */
        isBroadcast.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(isBroadcast.isSelected()) {
                    jlist.setSelectionInterval(0, jlist.getModel().getSize() - 1);
                }else{
                    jlist.removeSelectionInterval(0 , jlist.getModel().getSize() - 1);
                }
            }
        });

        /**
         * mouse click lister responsible for uncheck broadcast checkbox
         */
        jlist.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (isBroadcast.isSelected()){
                    isBroadcast.setSelected(false);
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {

            }

            @Override
            public void mouseReleased(MouseEvent e) {

            }

            @Override
            public void mouseEntered(MouseEvent e) {

            }

            @Override
            public void mouseExited(MouseEvent e) {

            }
        });

    }

    /**
     * Prompt for and return the address of the server.
     */
    private String getServerAddress() {
        return JOptionPane.showInputDialog(
            frame,
            "Enter IP Address of the Server:",
            "Welcome to the Chatter",
            JOptionPane.QUESTION_MESSAGE);
    }

    /**
     * Prompt for and return the desired screen name.
     */
    private String getName() {
        return JOptionPane.showInputDialog(
            frame,
            "Choose a screen name:",
            "Screen name selection",
            JOptionPane.PLAIN_MESSAGE);
    }

    /**
     *
     * @param message error message
     * @param title will be display in error message title
     * this method show error messages when need
     */
    private void showError(String message , String title) {
        JOptionPane.showMessageDialog(
                frame,
                message,
                title,
                JOptionPane.ERROR_MESSAGE);
    }

    /**
     * Connects to the server then enters the processing loop.
     */
    private void run() throws IOException {

        // Make connection and initialize streams
        String serverAddress = getServerAddress();
        while(serverAddress.equals("")){
            serverAddress = getServerAddress();
        }

        // create connection with server
        Socket socket = new Socket(serverAddress, 9001);
        in = new BufferedReader(new InputStreamReader(
            socket.getInputStream()));
        out = new PrintWriter(socket.getOutputStream(), true);

        // this loop responsible for handle incoming streams from the server
        while(true){
            String line = in.readLine();
            if(line.startsWith("SUBMITNAME ")){
                name = getName();
                out.println("ENTERDNAME " + name);
            }else if(line.startsWith("NAMEACCEPT ")){
                textField.setEditable(true);
                lblName.setText(name);
            }else if(line.startsWith("BROADCAST ")){
                messageArea.append(line + "\n");
            }else if(line.startsWith("NAMELIST ")){
                updateNameList(line);
            }else if(line.startsWith("MESSAGE ")){
                messageArea.append((line + "\n"));
            }
        }
    }

    /**
     * Runs the client as an application with a closeable frame.
     */
    public static void main(String[] args) throws Exception {
        ChatClient client = new ChatClient();
        client.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        client.frame.setVisible(true);
        client.run();
    }


    /**
     *
     * @param line contains names separated by space
     * this method responsible for update client list
     */
    public void updateNameList(String line) {
        ArrayList names = new ArrayList(Arrays.asList(line.split(" ")));
        names.remove(0);
        jlist.setListData(names.toArray());

        if (isBroadcast.isSelected()){
            jlist.setSelectionInterval(0, jlist.getModel().getSize() - 1);
        }
    }
}