package org.example;

import org.example.dtos.Message;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;

public class Client {
    Message message = new Message();

    public void communication() {
        try {
            Socket communicationSocket = new Socket("localhost", 8081);
            BufferedReader console = new BufferedReader(new InputStreamReader(System.in));
            ObjectInputStream serverInput = new ObjectInputStream(communicationSocket.getInputStream());
            System.out.println(serverInput.readUTF());
            ObjectOutputStream serverOutput = new ObjectOutputStream(communicationSocket.getOutputStream());
            while (true) {
                System.out.println("Unesite neku od opcija 0-5");
                System.out.println("Opcija 1: Registracija");
                System.out.println("Opcija 2: Login");
                System.out.println("Opcija 3: Guest Rezervacija");
                System.out.println("Opcija 4: Broj preostalih karata");
                System.out.println("Opcija 5: Brisan3je rezervacije");
                System.out.println("Opcija 0: Gasenje");

                String input = console.readLine();
                if (checkOption(input, false) == -1) {
                    System.out.println("Niste uneli validnu opciju");
                    continue;
                }
                int option = checkOption(input, false);
                serverOutput.writeInt(option);
                serverOutput.flush();
                String username;
                String password;
                String name;
                String surname;
                String JMBGinput;
                long JMBG;
                String email;
                switch (option) {

                    case 1:
                        while (true) {
                            System.out.println("Unesite username");
                            username = console.readLine();
                            System.out.println("Unesite sifru");
                            password = console.readLine();
                            System.out.println("Unesite ime");
                            name = console.readLine();
                            System.out.println("Unesite prezime");
                            surname = console.readLine();
                            System.out.println("Unesite JMBG");
                            JMBGinput = console.readLine();
                            if (tryParseLong(JMBGinput) == -1) {
                                System.out.println("Niste uneli JMBG u pravom formatu!");
                                continue;
                            }
                            JMBG = tryParseLong(JMBGinput);
                            System.out.println("Unesite email");
                            email = console.readLine();
                            Customer customer = new Customer(username, password, name, surname, JMBG, email);
                            serverOutput.writeObject(customer);
                            serverOutput.flush();
                            message = (Message) serverInput.readObject();
                            message.setStatus(false);
                            System.out.println(message.getMessage());
                            break;
                        }
                        break;
                    case 2:
                        System.out.println("Unesite username");
                        username = console.readLine();
                        System.out.println("Unesite sifru");
                        password = console.readLine();
                        Customer customerLogin = new Customer(username, password);
                        serverOutput.writeObject(customerLogin);
                        serverOutput.flush();
                        message = (Message) serverInput.readObject();
                        System.out.println(message.getMessage());
                        if (message.isStatus()) {
                            boolean flag = true;
                            while (flag) {
                                System.out.println("Unesite neku od opcija:");
                                System.out.println("Opcija 3: Rezervacija");
                                System.out.println("Opcija 4: Broj preostalih karata");
                                System.out.println("Opcija 5: Brisanje rezervacije");
                                System.out.println("Opcija 0: Logout");
                                String input2 = console.readLine();
                                if (checkOption(input2, true) == -1) {
                                    System.out.println("Niste uneli validnu opciju");
                                    continue;
                                }
                                int option2 = checkOption(input2, true);
                                serverOutput.writeInt(option2);
                                serverOutput.flush();
                                switch (option2) {
                                    case 3:
                                        reservation(communicationSocket, console, serverInput, serverOutput, true);
                                        break;
                                    case 4:
                                        message = (Message) serverInput.readObject();
                                        System.out.println(message.getMessage());
                                        break;
                                    case 5:
                                        serverOutput.writeBoolean(false);
                                        serverOutput.flush();
                                        message = (Message) serverInput.readObject();
                                        System.out.println(message.getMessage());
                                        break;
                                    default:
                                        break;
                                    case 0:
                                        message.setStatus(false);
                                        flag = false;
                                        break;
                                }
                            }
                        }

                        break;
                    case 3:
                        reservation(communicationSocket, console, serverInput, serverOutput, false);
                        break;
                    case 4:
                        message = (Message) serverInput.readObject();
                        System.out.println(message.getMessage());
                        break;
                    case 5:

                        String guestJMBG;
                        while (true) {
                            System.out.println("Unesite JMBG preko kog cemo pronaci vasu prijavu");
                            guestJMBG = console.readLine();
                            if (tryParseLong(guestJMBG) == -1) {
                                System.out.println("Niste uneli JMBG u pravom formatu!");
                                continue;
                            }
                            break;
                        }
                        serverOutput.writeBoolean(true);
                        serverOutput.flush();
                        serverOutput.writeLong(Long.parseLong(guestJMBG));
                        serverOutput.flush();
                        message = (Message) serverInput.readObject();
                        System.out.println(message.getMessage());
                        message.setStatus(false);
                        break;
                    case 0:
                        System.exit(0);
                }
            }

        } catch (SocketException e) {
            System.out.println("Server je oboren!");
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

    }

    private void reservation(Socket communicationSocket, BufferedReader console, ObjectInputStream serverInput, ObjectOutputStream serverOutput, boolean registered) throws IOException, ClassNotFoundException {
        String surname;
        String name;
        long JMBG;
        String email;
        String JMBGinput;
        while (true) {
            if (registered) {
                while (true) {
                    System.out.println("Za rezervaciju obicnih karata izaberite opciju 1: ");
                    System.out.println("Za rezervaciju VIP karata izaberite opciju 2: ");
                    String normalOrVIP = console.readLine();
                    if (checkVIP(normalOrVIP) == -1) {
                        System.out.println("Niste uneli valinu opciju!");
                    } else {
                        serverOutput.writeInt(checkVIP(normalOrVIP));
                        break;
                    }
                }
            } else {
                serverOutput.writeInt(1);
            }
            int noOfCards;
            while (true) {
                System.out.println("Koliko karata zelite da rezervisete?");
                String cardsString = console.readLine();
                if (checkCards(cardsString) == -1) {
                    System.out.println("Ne mozete kupiti 0 ili vise od 4 karte");
                    continue;
                }
                noOfCards = checkCards(cardsString);
                break;
            }
            serverOutput.writeInt(noOfCards);
            serverOutput.flush();
            if (serverInput.readBoolean()) {
                message = (Message) serverInput.readObject();
                System.out.println(message.getMessage());
                break;
            }

            message = (Message) serverInput.readObject();
            System.out.println(message.getMessage());
            break;
        }
        if (!registered) {
            while (true) {
                System.out.println("Unesite ime");
                name = console.readLine();
                System.out.println("Unesite prezime");
                surname = console.readLine();
                System.out.println("Unesite JMBG");
                JMBGinput = console.readLine();
                if (tryParseLong(JMBGinput) == -1) {
                    System.out.println("Niste uneli JMBG u pravom formatu!");
                    continue;
                }
                JMBG = tryParseLong(JMBGinput);
                System.out.println("Unesite email");
                email = console.readLine();
                Customer customerReservation = new Customer(name, surname, JMBG, email);
                serverOutput.writeObject(customerReservation);
                serverOutput.flush();
                message = (Message) serverInput.readObject();
                System.out.println(message.getMessage());
                break;
            }

        }
        if (message.isStatus()) {
            String filename = serverInput.readUTF();
            FileOutputStream fos = new FileOutputStream(new File(filename));
            BufferedOutputStream out = new BufferedOutputStream(fos);
            byte[] buffer = new byte[1024];
            int count;
            InputStream in = communicationSocket.getInputStream();
            while ((count = in.read(buffer)) > 0) {
                fos.write(buffer, 0, count);
                break;
            }
            out.close();
            message.setStatus(false);
            System.out.println("Uspesno ste dobili rezervaciju.");
        }
    }

    private int checkOption(String input, boolean login) {
        try {
            int option = Integer.parseInt(input);
            if (!login) {
                if (option < 0 || option > 5) {
                    return -1;
                }
            } else {
                if (option != 0 && (option < 3 || option > 5)) {
                    return -1;
                }
            }
            return option;
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private int checkVIP(String input) {
        try {
            int option = Integer.parseInt(input);
                if (option != 1 && option != 2) {
                    return -1;
            }
            return option;
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private long tryParseLong(String input) {
        try {
            long option = Long.parseLong(input);
            if (String.valueOf(option).length() != 13) {
                return -1;
            }
            return option;
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private int checkCards(String input) {
        try {
            int cards = Integer.parseInt(input);
            if (cards < 1 || cards > 4) {
                return -1;
            }
            return cards;
        } catch (NumberFormatException e) {
            return -1;
        }
    }
}
