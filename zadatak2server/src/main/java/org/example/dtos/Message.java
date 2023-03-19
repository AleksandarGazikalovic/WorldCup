package org.example.dtos;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
public class Message implements Serializable {
    private static final long serialVersionUID = 1234561L;
    private boolean status;
    private String message;

    public Message(boolean status, String message) {
        this.status = status;
        this.message = message;
    }

    public Message(boolean status) {
        this.status = status;
    }
}
