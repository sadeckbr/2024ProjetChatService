package fr.uga.miashs.dciss.chatservice.client;


import fr.uga.miashs.dciss.chatservice.server.UserMsg;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

import static java.lang.System.out;

public class InstantMessengerGUI extends JFrame {
    private JTextArea chatArea;
    private JTextField messageField;
    private JButton sendButton;
    private JTextField usernameField;
    private JButton connectButton;
    private ClientMsg client;
    private JButton createGroupButton;
    private JList<String> contactList;

    public InstantMessengerGUI() {
        setTitle("Messagerie Instantanée");
        setSize(400, 300);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        initComponents();
        //connectToServer();
    }
    private DataOutputStream out;



    //Pour déplacer le bouton "Créer un groupe" dans l'onglet de chat, vous pouvez simplement déplacer le code qui ajoute le bouton à l'onglet de connexion vers l'onglet de chat. Voici comment vous pouvez le faire :

    private void initComponents() {
        JTabbedPane tabbedPane = new JTabbedPane();

        JPanel connectionPanel = new JPanel();
        connectionPanel.setLayout(new BorderLayout());

        JPanel connectInputPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        connectInputPanel.add(new JLabel("Pseudo:"));
        usernameField = new JTextField(15);
        connectInputPanel.add(usernameField);
        connectButton = new JButton("Se connecter");

        connectButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                connect();
                JTabbedPane tabbedPane = (JTabbedPane) getContentPane().getComponent(0);
            }
        });
        connectInputPanel.add(connectButton);
        connectionPanel.add(connectInputPanel, BorderLayout.CENTER);

        tabbedPane.addTab("Connexion", connectionPanel);

        JPanel chatPanel = new JPanel();
        chatPanel.setLayout(new BorderLayout());

        chatArea = new JTextArea();
        chatArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(chatArea);
        chatPanel.add(scrollPane, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new BorderLayout());

        messageField = new JTextField();
        bottomPanel.add(messageField, BorderLayout.CENTER);

        sendButton = new JButton("Envoyer");
        sendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendMessage();
            }
        });
        bottomPanel.add(sendButton, BorderLayout.EAST);

        // Ajoutez le bouton "Créer un groupe" ici
        createGroupButton= new JButton("Créer un groupe");
        createGroupButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    createGroup();
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            }
        });
        bottomPanel.add(createGroupButton, BorderLayout.WEST);

        chatPanel.add(bottomPanel, BorderLayout.SOUTH);

        tabbedPane.addTab("Chat", chatPanel);

        add(tabbedPane);

        contactList=new JList<>();
        tabbedPane.addTab("Répertoire", new JScrollPane(contactList));

    }

    private void connect() {
        String username = usernameField.getText().trim();
        if (!username.isEmpty()) {
            try {
                // Créer une instance de ClientMsg
                this.client = new ClientMsg("localhost", 1666);
                // Appeler la méthode start (ou une autre méthode qui démarre le client)
                client.startSession();
                chatArea.append("Connecté en tant que: " + username + "\n");
                // Vous pouvez également changer d'onglet après la connexion réussie
                JTabbedPane tabbedPane = (JTabbedPane) getContentPane().getComponent(0);
                tabbedPane.setSelectedIndex(1); // Onglet de chat
            } catch (IOException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Erreur de connexion");
            }
        } else {
            JOptionPane.showMessageDialog(this, "Erreur de connexion");
        }
    }

    private void sendMessage() {
        String message = messageField.getText().trim();
        if (!message.isEmpty()) {
            try {
                out.writeUTF(message); // Envoie le message au serveur
                out.flush();
            } catch (IOException e) {
                e.printStackTrace();}
            chatArea.append("Moi: " + message + "\n");
            messageField.setText("");
        }
    }


    private void createGroup() throws IOException {

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(bos);
        // byte 1 : create group on server
        dos.writeByte(1);
        String input = JOptionPane.showInputDialog(this, "Combien de personnes voulez-vous ajouter au groupe ?");
        int nb = Integer.parseInt(input);
        if (input != null) {
            // nb members
            dos.writeInt(nb);
        }
        for (int i = 0; i < nb; i++) {
            // Pour chaque personne, demandez à l'utilisateur l'identifiant de la personne
            input = JOptionPane.showInputDialog(this, "Entrez l'ID de la personne " + (i) + " :");
            if (input != null) {
                int id = Integer.parseInt(input);
                // Ajoutez l'identifiant de la personne à la liste des membres
                dos.writeInt(id);
            }


        }
        dos.flush();
        client.sendPacket(0, bos.toByteArray());
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                // Nombre d'interfaces à lancer
                int numberOfInterfaces = 4;

                for (int i = 0; i < numberOfInterfaces; i++) {
                    InstantMessengerGUI gui = new InstantMessengerGUI();
                    gui.setVisible(true);
                }
            }
        });
    }
}
