package org.example;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.dtos.Message;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

@Getter
@Setter
@NoArgsConstructor
public class Customer implements Serializable {
    private static final long serialVersionUID = 1234567L;
    private String username;
    private String password;
    private String name;
    private String surname;
    private long JMBG;
    private String email;
    @JsonIgnore
    private boolean loggedIn = false;
    private boolean VIP;
    private int noOfCards;

    public static Message register(Customer customer) {
        try {
            String data = Files.readString(Paths.get("./customers.txt"));
            ObjectMapper mapper = new ObjectMapper();
            ArrayList<Customer> customerList;
            if (data.length() == 0) {
                customerList = new ArrayList<>();
            } else {
                CollectionType listType = mapper.getTypeFactory().constructCollectionType(ArrayList.class, Customer.class);
                customerList = mapper.readValue(data, listType);
            }

            Customer checkExisting = customerList.stream().filter(customerCheck -> customerCheck.getUsername().equals(customer.getUsername())).findFirst().orElse(null);
            if (checkExisting != null) {
                return new Message(false, "Vec postoji korisnik sa istim username-om!");
            }

            checkExisting = customerList.stream().filter(customerCheck -> customerCheck.getJMBG() == customer.getJMBG()).findFirst().orElse(null);
            if (checkExisting != null) {
                return new Message(false, "Vec postoji korisnik sa istim JMBG-om");
            }

            customerList.add(customer);
            String jsonList = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(customerList);
            FileWriter file = new FileWriter("./customers.txt");
            file.write(jsonList);
            file.close();
            return new Message(true, "Uspesno ste se registrovali.");
        } catch (IOException e) {
            e.printStackTrace();
            return new Message(false, "Greska pri upisivanju u file.");
        }
    }

    public static Message login(String username, String password) {
        try {
            String data = Files.readString(Paths.get("./customers.txt"));
            ObjectMapper mapper = new ObjectMapper();
            ArrayList<Customer> customerList;
            if (data.length() == 0) {
                return new Message(false, "Ne postoji korisnik sa datim username-om!");
            } else {
                CollectionType listType = mapper.getTypeFactory().constructCollectionType(ArrayList.class, Customer.class);
                customerList = mapper.readValue(data, listType);
            }
            Customer checkExisting = customerList.stream().filter(customer -> username.equals(customer.getUsername())).findFirst().orElse(null);
            if (checkExisting == null) {
                return new Message(false, "Ne postoji korisnik sa datim username-om!");
            }
            if (!checkExisting.getPassword().equals(password)) {
                return new Message(false, "Uneli ste pogresnu sifru!");
            }
            checkExisting.setLoggedIn(true);
            return new Message(true, "Uspesno ste se ulogovali.");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static Message reservation(Customer customer, int normalOrVIP) {
        try {
            String data = Files.readString(Paths.get("./customers.txt"));
            ObjectMapper mapper = new ObjectMapper();
            ArrayList<Customer> customerList;
            if (data.length() == 0) {
                return new Message(false, "Ne postoji korisnik sa datim username-om!");
            } else {
                CollectionType listType = mapper.getTypeFactory().constructCollectionType(ArrayList.class, Customer.class);
                customerList = mapper.readValue(data, listType);
            }
            Customer checkExisting = customerList.stream().filter(customerCheck -> customerCheck.getUsername().equals(customer.getUsername())).findFirst().orElse(null);
            if (checkExisting == null) {
                return new Message(false, "Ne postoji korisnik sa datim username-om!");
            }

            if(checkExisting.getNoOfCards()!=0) {
                return new Message(false, "Vec postoji rezervacija na ovo ime, molimo vas izbrisite staru rezervaciju ako zelite da je promenite!");
            }

            customerList.stream().filter(customerCheck -> customerCheck.getUsername().equals(customer.getUsername())).
                    forEach(customerUpdate -> customerUpdate.setNoOfCards(customer.getNoOfCards()));

            if(normalOrVIP == 2) {
                customerList.stream().filter(customerCheck -> customerCheck.getUsername().equals(customer.getUsername())).
                        forEach(customerUpdate -> customerUpdate.setVIP(true));
            }

            String jsonList = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(customerList);
            FileWriter file = new FileWriter("./customers.txt");
            file.write(jsonList);
            file.close();
            if (normalOrVIP == 1) {
                if (customer.getNoOfCards() == 1) {
                    return new Message(true, "Uspesno ste rezervisali " + customer.getNoOfCards() + " obicnu kartu.");
                }
                return new Message(true, "Uspesno ste rezervisali " + customer.getNoOfCards() + " obicne karte.");
            } else {
                customer.setVIP(true);
                if (customer.getNoOfCards() == 1) {
                    return new Message(true, "Uspesno ste rezervisali " + customer.getNoOfCards() + " VIP kartu.");
                }
                return new Message(true, "Uspesno ste rezervisali " + customer.getNoOfCards() + " VIP karte.");
            }
        } catch (IOException e) {
            e.printStackTrace();
            return new Message(false, "Greska pri upisivanju u file.");
        }
    }

    public static Message guestReservation(Customer guest) {
        try {
            String data = Files.readString(Paths.get("./guestCustomers.txt"));
            ObjectMapper mapper = new ObjectMapper();
            ArrayList<Customer> guestCustomerList;
            if (data.length() == 0) {
                guestCustomerList = new ArrayList<>();
            } else {
                CollectionType listType = mapper.getTypeFactory().constructCollectionType(ArrayList.class, Customer.class);
                guestCustomerList = mapper.readValue(data, listType);
            }
            Customer guestCheck = guestCustomerList.stream().filter(customerCheck -> customerCheck.getJMBG() == guest.getJMBG())
                    .findFirst().orElse(null);

            String data2 = Files.readString(Paths.get("./customers.txt"));
            ArrayList<Customer> customerList;
            if (data2.length() == 0) {
                customerList = new ArrayList<>();
            } else {
                CollectionType listType = mapper.getTypeFactory().constructCollectionType(ArrayList.class, Customer.class);
                customerList = mapper.readValue(data2, listType);
            }

            Customer guestCheck2 = customerList.stream().filter(customerCheck -> customerCheck.getJMBG() == guest.getJMBG())
                    .findFirst().orElse(null);

            if (guestCheck != null) {
                return new Message(false, "Vec postoji korisnik sa istim JMBG-om");
            }

            if (guestCheck2 != null) {
                return new Message(false, "U nasem sistemu postoji korisnik sa tim JMBG-om, molimo vas ulogujte se da bi rezervisali karte!");
            }
            guestCustomerList.add(guest);
            String jsonList = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(guestCustomerList);
            FileWriter file = new FileWriter("./guestCustomers.txt");
            file.write(jsonList);
            file.close();
            if (guest.getNoOfCards() == 1) {
                return new Message(true, "Uspesno ste rezervisali " + guest.getNoOfCards() + " kartu.");
            }
            return new Message(true, "Uspesno ste rezervisali " + guest.getNoOfCards() + " karte.");
        } catch (IOException e) {
            return new Message(false, "Greska pri upisivanju u file.");
        }
    }

    public static File getReservation(Customer customer) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            String reservation = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(customer);
            String filename = customer.getName() + "_" + customer.getJMBG() + ".txt";
            File file = new File(filename);
            FileWriter fw = new FileWriter(file);
            fw.write(reservation);
            fw.close();
            return file;
        } catch (IOException e) {
            System.out.println("Greska pri parsiranju");
        }
        return new File(customer.getName() + "_" + customer.getJMBG() + ".txt");
    }

    public static Message deleteReservation(Customer customer) {
        try {
            String data = Files.readString(Paths.get("./customers.txt"));
            ObjectMapper mapper = new ObjectMapper();
            ArrayList<Customer> customerList;
            if (data.length() == 0) {
                return new Message(false, "Nema rezervacija!");
            } else {
                CollectionType listType = mapper.getTypeFactory().constructCollectionType(ArrayList.class, Customer.class);
                customerList = mapper.readValue(data, listType);
            }
            Customer checkExisting = customerList.stream().filter(customerCheck -> customerCheck.getUsername().equals(customer.getUsername())).findFirst().orElse(null);
            if (checkExisting == null) {
                return new Message(false, "Ne postoji korisnik sa datim username-om!");
            }

            if (checkExisting.getNoOfCards() == 0) {
                return new Message(false, "Ne postoji rezervacija na vase ime!");
            }

            Files.delete(Paths.get(checkExisting.getName() + "_" + String.valueOf(checkExisting.getJMBG()) + ".txt"));

            customerList.stream().filter(customerCheck -> customerCheck.getUsername().equals(customer.getUsername())).
                    forEach(customerUpdate -> customerUpdate.setNoOfCards(0));

            if (customer.isVIP()) {
                customerList.stream().filter(customerCheck -> customerCheck.getUsername().equals(customer.getUsername())).
                        forEach(customerUpdate -> customerUpdate.setVIP(false));
            }

            String jsonList = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(customerList);
            FileWriter file = new FileWriter("./customers.txt");
            file.write(jsonList);
            file.close();
            return new Message(true, "Uspesno ste izbrisali rezervaciju karata.");
        } catch (IOException e) {
            e.printStackTrace();
            return new Message(false, "Greska pri upisivanju u file.");
        }
    }

    public static Message deleteGuestReservation(long JMBG) {
        try {
            String data = Files.readString(Paths.get("./guestCustomers.txt"));
            ObjectMapper mapper = new ObjectMapper();
            ArrayList<Customer> guestCustomerList;
            if (data.length() == 0) {
                return new Message(false, "Nema rezervacija!");
            } else {
                CollectionType listType = mapper.getTypeFactory().constructCollectionType(ArrayList.class, Customer.class);
                guestCustomerList = mapper.readValue(data, listType);
            }
            String data2 = Files.readString(Paths.get("./customers.txt"));
            ArrayList<Customer> customerList;
            if (data2.length() == 0) {
                customerList = new ArrayList<>();
            } else {
                CollectionType listType = mapper.getTypeFactory().constructCollectionType(ArrayList.class, Customer.class);
                customerList = mapper.readValue(data2, listType);
            }

            Customer checkExisting2 = customerList.stream().filter(customerCheck -> customerCheck.getJMBG() == JMBG)
                    .findFirst().orElse(null);

            if(checkExisting2 != null && checkExisting2.getNoOfCards()!=0) {
                return new Message(false, "Postoji rezervacija na to ime, ali morate se ulogovati da bi je obrisali!");
            }

            Customer checkExisting = guestCustomerList.stream().filter(customerCheck -> customerCheck.getJMBG() == JMBG).findFirst().orElse(null);
            if (checkExisting == null) {
                return new Message(false, "Ne postoji rezervacija na dati JMBG!");
            }
            Files.delete(Paths.get(checkExisting.getName() + "_" + String.valueOf(checkExisting.getJMBG()) + ".txt"));
            guestCustomerList.remove(checkExisting);
            String jsonList = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(guestCustomerList);
            FileWriter file = new FileWriter("./guestCustomers.txt");
            file.write(jsonList);
            file.close();
            return new Message(true, "Uspesno ste izbrisali rezervaciju karata.");
        } catch (IOException e) {
            e.printStackTrace();
            return new Message(false, "Greska pri upisivanju u file.");
        }

    }

    public static int getNoOfCards(long JMBG, boolean guest) {
        try {
            String data;
            if (guest) {
                data = Files.readString(Paths.get("./guestCustomers.txt"));
            } else {
                data = Files.readString(Paths.get("./customers.txt"));
            }
            ObjectMapper mapper = new ObjectMapper();
            ArrayList<Customer> customerList;
            if (data.length() == 0) {
                return 0;
            } else {
                CollectionType listType = mapper.getTypeFactory().constructCollectionType(ArrayList.class, Customer.class);
                customerList = mapper.readValue(data, listType);
            }
            Customer checkExisting = customerList.stream().filter(customerCheck -> customerCheck.getJMBG() == JMBG).findFirst().orElse(null);
            if (checkExisting == null) {
                return 0;
            }
            return checkExisting.getNoOfCards();

        } catch (IOException e) {
            e.printStackTrace();
            return 0;
        }
    }

    public static boolean getVIP(long JMBG) {
        try {
            String data = Files.readString(Paths.get("./customers.txt"));
            ObjectMapper mapper = new ObjectMapper();
            ArrayList<Customer> customerList;
            if (data.length() == 0) {
                return false;
            } else {
                CollectionType listType = mapper.getTypeFactory().constructCollectionType(ArrayList.class, Customer.class);
                customerList = mapper.readValue(data, listType);
            }
            Customer checkExisting = customerList.stream().filter(customerCheck -> customerCheck.getJMBG() == JMBG).findFirst().orElse(null);
            if (checkExisting == null) {
                return false;
            }
            return checkExisting.isVIP();

        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static Customer getCustomerByUsername(String username) {
        try {
            String data = Files.readString(Paths.get("./customers.txt"));
            ObjectMapper mapper = new ObjectMapper();
            ArrayList<Customer> customerList;
            if (data.length() == 0) {
                return null;
            } else {
                CollectionType listType = mapper.getTypeFactory().constructCollectionType(ArrayList.class, Customer.class);
                customerList = mapper.readValue(data, listType);
            }
            Customer checkExisting = customerList.stream().filter(customer -> username.equals(customer.getUsername())).findFirst().orElse(null);
            if (checkExisting == null) {
                return null;
            } else {
                return checkExisting;
            }
        } catch (IOException e) {
            System.out.println("Nema tog korisnika");
        }
        return null;
    }

    /*public String register() {
        try {
            Class.forName("oracle.jdbc.driver.OracleDriver");
            Connection connection = DriverManager.getConnection("jdbc:oracle:thin:@localhost:8081");
            Statement statement = connection.createStatement();
            ResultSet rs = statement.executeQuery("INSERT INTO CUSTOMER VALUES {$this}");
            this.registered = true;
            connection.close();
            return "Uspesno ste se registrovali";
        } catch (ClassNotFoundException | SQLException e) {
            return "Puklo negde pri registraciji";
        }

    }

    public String login() {
        try {
            Class.forName("oracle.jdbc.driver.OracleDriver");
            Connection connection = DriverManager.getConnection("jdbc:oracle:thin:@localhost:8081");
            Statement statement = connection.createStatement();
            ResultSet rs = statement.executeQuery("SELECT * FROM CUSTOMER");
            String dbUsername = null;
            while(rs.next()) {
                dbUsername = rs.getString(1);
                if(dbUsername.equals(this.username)) {
                    this.name = rs.getString(3);
                    this.surname = rs.getString(4);
                    this.JMBG = Long.parseLong(rs.getString(5));
                    this.email = rs.getString(6);
                    this.loggedIn = true;
                    connection.close();
                    return "Uspesno ste se ulogovali";
                }
            }
            if(dbUsername==null) {
                connection.close();
                return "Ne postoji korisnik sa datim username-om";
            }else {
                connection.close();
                return "Doslo je do greške";
            }
        } catch (ClassNotFoundException | SQLException e) {
            return "Puklo negde";
        }
    }*/
}
