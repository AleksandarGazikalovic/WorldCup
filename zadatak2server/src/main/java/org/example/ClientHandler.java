package org.example;

import org.example.dtos.Message;

import java.io.*;
import java.net.Socket;

public class ClientHandler implements Runnable, Serializable {

    ObjectInputStream clientInputStream;
    ObjectOutputStream clientOutputStream;
    Socket communicationSocket;
    Cards cards;

    public ClientHandler(Socket communicationSocket, ObjectInputStream clientInputStream, ObjectOutputStream clientOutputStream) {
        this.communicationSocket = communicationSocket;
        this.clientInputStream = clientInputStream;
        this.clientOutputStream = clientOutputStream;
        this.cards = new Cards();
        this.cards.load();
    }

    @Override
    public void run() {
        Customer customer = new Customer();
        Message message = new Message();
        while (true) {
            int option;
            try {
                option = clientInputStream.readInt();
                switch (option) {
                    case 1:
                        try {
                            customer = ((Customer) clientInputStream.readObject());
                            message = Customer.register(customer);
                            clientOutputStream.writeObject(message);
                            clientOutputStream.flush();
                            break;
                        } catch (IOException e) {
                            break;
                        }
                    case 2:
                        customer = (Customer) clientInputStream.readObject();
                        message = Customer.login(customer.getUsername(), customer.getPassword());
                        if(message.isStatus()) {
                            if(Customer.getCustomerByUsername(customer.getUsername())!=null) {
                                customer = Customer.getCustomerByUsername(customer.getUsername());
                                customer.setLoggedIn(true);
                            }
                        }
                        clientOutputStream.writeObject(message);
                        clientOutputStream.flush();
                        break;
                    case 3:
                        int normalOrVIP = clientInputStream.readInt();
                        int noOfCards = clientInputStream.readInt();
                        switch (normalOrVIP) {
                            case 1:
                                if (cards.getNormalCards() < noOfCards) {
                                    clientOutputStream.writeBoolean(true);
                                    clientOutputStream.flush();
                                    message = new Message(false, "Na stanju nemamo dovoljno normalnih karata!");
                                    clientOutputStream.writeObject(message);
                                    clientOutputStream.flush();
                                    break;
                                }
                                clientOutputStream.writeBoolean(false);
                                if (customer != null && customer.isLoggedIn()) {
                                    customer.setNoOfCards(noOfCards);
                                    message = Customer.reservation(customer, normalOrVIP);
                                } else {
                                    message = new Message(false, "Rezervisete karte kao guest korisnik, molimo Vas unesite potrebne podatke:");
                                    clientOutputStream.writeObject(message);
                                    clientOutputStream.flush();
                                    customer = (Customer) clientInputStream.readObject();
                                    if (cards.getNormalCards() < noOfCards) {
                                        customer.setNoOfCards(cards.getNormalCards());
                                    } else {
                                        customer.setNoOfCards(noOfCards);
                                    }
                                    message = Customer.guestReservation(customer);
                                }
                                break;
                            case 2:
                                if (cards.getVipCards() < noOfCards) {
                                    clientOutputStream.writeBoolean(true);
                                    clientOutputStream.flush();
                                    message = new Message(false, "Na stanju nemamo dovoljno VIP karata!");
                                    clientOutputStream.writeObject(message);
                                    clientOutputStream.flush();
                                    break;
                                }
                                clientOutputStream.writeBoolean(false);
                                if (customer != null && customer.isLoggedIn()) {
                                    customer.setNoOfCards(noOfCards);
                                    message = Customer.reservation(customer, normalOrVIP);
                                } else {
                                    message = new Message(false, "Ne mozete rezervisati VIP karte kao guest korisnik!");
                                }
                                break;
                        }


                        clientOutputStream.writeObject(message);
                        clientOutputStream.flush();
                        if (message.isStatus()) {
                            if (normalOrVIP == 1) {
                                cards.lower(false, noOfCards);
                            } else {
                                cards.lower(true, noOfCards);
                            }
                            String filename = customer.getName() + "_" + String.valueOf(customer.getJMBG()) + ".txt";
                            clientOutputStream.writeUTF(filename);
                            clientOutputStream.flush();
                            File reservation = Customer.getReservation(customer);
                            int count;
                            byte[] buffer = new byte[1024];
                            FileInputStream fis = new FileInputStream(reservation);
                            BufferedInputStream in = new BufferedInputStream(fis);
                            OutputStream os = communicationSocket.getOutputStream();
                            while ((count = in.read(buffer)) >= 0) {
                                os.write(buffer, 0, count);
                                os.flush();
                            }
                            fis.close();
                        }
                        break;

                    case 4:
                        clientOutputStream.writeObject(cards.left());
                        clientOutputStream.flush();
                        break;
                    case 5:
                        boolean guest = clientInputStream.readBoolean();
                        if (guest) {
                            long guestJMBG = clientInputStream.readLong();
                            cards.cancelReservation(Customer.getNoOfCards(guestJMBG, guest), false);
                            message = Customer.deleteGuestReservation(guestJMBG);
                        } else {
                            cards.cancelReservation(Customer.getNoOfCards(customer.getJMBG(), guest), Customer.getVIP(customer.getJMBG()));
                            message = Customer.deleteReservation(customer);
                        }
                        clientOutputStream.writeObject(message);
                        clientOutputStream.flush();
                        break;
                    case 0:
                        if (customer.isLoggedIn()) {
                            customer = new Customer();
                            message = new Message(false);
                        }
                        break;
                    default:
                        clientOutputStream.writeUTF("Uneli ste opciju koja ne postoji");
                        clientOutputStream.flush();
                        break;
                }
            } catch (NumberFormatException | ClassNotFoundException e) {
                e.printStackTrace();
                break;
            } catch (IOException e) {
                System.out.println("Konekcija je prekinuta!");
                try {
                    communicationSocket.close();
                } catch (IOException ex) {
                }
                break;
            }

        }
    }
}