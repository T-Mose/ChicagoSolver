public class Card {
    private final String suit;
    private final String rank;

    public Card(String suit, String rank) {
        this.suit = suit;
        this.rank = rank;
    }

    public String getSuit() {
        return suit;
    }

    public String getRank() {
        return rank;
    }

    @Override
    public String toString() {
        // Convert suit and rank to standard notation
        String suitInitial = "";
        switch (suit) {
            case "Spades": suitInitial = "S"; break;
            case "Hearts": suitInitial = "H"; break;
            case "Diamonds": suitInitial = "D"; break;
            case "Clubs": suitInitial = "C"; break;
        }
        return rank + suitInitial;
    }
}
