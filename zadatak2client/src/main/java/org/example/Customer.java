package org.example;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

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
    private boolean VIP;
    private int noOfCards;

    public Customer(String username, String password, String name, String surname, long JMBG, String email) {
        this.username = username;
        this.password = password;
        this.name = name;
        this.surname = surname;
        this.JMBG = JMBG;
        this.email = email;
    }

    public Customer(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public Customer(String name, String surname, long JMBG, String email) {
        this.name = name;
        this.surname = surname;
        this.JMBG = JMBG;
        this.email = email;

    }
}
