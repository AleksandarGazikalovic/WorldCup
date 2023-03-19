package org.example;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.dtos.Message;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

@Getter
@Setter
@NoArgsConstructor
public class Cards {
    private int normalCards;
    private int vipCards;

    public Cards(int normalCards, int VIPCards) {
        this.normalCards = normalCards;
        this.vipCards = VIPCards;
    }

    public void load() {
        try {
            String data = Files.readString(Paths.get("./cards.txt"));
            ObjectMapper mapper = new ObjectMapper();
            if (data.length() == 0) {
                Cards cards = new Cards(20, 5);
                normalCards = cards.getNormalCards();
                vipCards = cards.getVipCards();
                String json = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(cards);
                FileWriter file = new FileWriter("./cards.txt");
                file.write(json);
                file.close();
            } else {
                Cards cards = mapper.readValue(data, Cards.class);
                normalCards = cards.getNormalCards();
                vipCards = cards.getVipCards();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    public void lower(boolean VIP, int number) {
        try {
            String data = Files.readString(Paths.get("./cards.txt"));
            ObjectMapper mapper = new ObjectMapper();
            Cards cards;
            if (data.length() == 0) {
                cards = new Cards(normalCards, vipCards);
                mapper.writerWithDefaultPrettyPrinter().writeValueAsString(cards);
            } else {
                cards = mapper.readValue(data, Cards.class);
                if (VIP) {
                    cards.setVipCards(cards.getVipCards() - number);
                } else {
                    cards.setNormalCards(cards.getNormalCards() - number);
                }
            }
            String json = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(cards);
            FileWriter file = new FileWriter("./cards.txt");
            file.write(json);
            file.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Message left() {
        try {
            String data = Files.readString(Paths.get("./cards.txt"));
            ObjectMapper mapper = new ObjectMapper();
            Cards cards = mapper.readValue(data, Cards.class);

            if (cards.getNormalCards() == 0 && cards.getVipCards() == 0) {
                return new Message(false, "Na stanju nema vise karata!");
            } else if (cards.getNormalCards() == 0) {
                return new Message(false, "Na stanju nema vise normalnih karata, ali je ostalo jos " + cards.getVipCards() + " VIP karte");
            } else if (cards.getVipCards() == 0) {
                return new Message(false, "Na stanju nema vise VIP karata, ali je ostalo jos " + cards.getNormalCards() + " normalnih karata");
            }

            if (cards.getNormalCards() > 4) {
                return new Message(false, "Preostalo je jos " + cards.getNormalCards() + " obicnih karata i "
                        + cards.getVipCards() + " VIP karte");
            } else if (cards.getNormalCards() > 1) {
                return new Message(false, "Preostalo je jos " + cards.getNormalCards() + " obicne karte i "
                        + cards.getVipCards() + " VIP karte");
            }
            return new Message(false, "Preostalo je jos " + cards.getNormalCards() + " obicna karta i "
                    + cards.getVipCards() + " VIP karte");

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    public void cancelReservation(int cancel, boolean VIP) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            String data = Files.readString(Paths.get("./cards.txt"));
            Cards cards = mapper.readValue(data, Cards.class);
            if(VIP) {
                cards.setVipCards(cards.getVipCards() + cancel);
                vipCards = cards.getVipCards();
            } else {
                cards.setNormalCards(cards.getNormalCards() + cancel);
                normalCards = cards.getNormalCards();
            }
            String json = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(cards);
            FileWriter file = new FileWriter("./cards.txt");
            file.write(json);
            file.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

