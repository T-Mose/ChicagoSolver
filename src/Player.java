import java.util.ArrayList;
import java.util.List;

public class Player {
    protected String name;
    protected List<Card> hand;
    protected int points;
    private List<Card> originalHand; // Store the hand before outplay


    public Player(String name) {
        this.name = name;
        this.points = 0;
        this.hand = new ArrayList<>();
        this.originalHand = new ArrayList<>();    }

    public String getName() {
        return name;
    }

    public List<Card> getHand() {
        return hand;
    }
    public void changePoints(int change) {
        points += change;
    }
    public int getPoints() {
        return points;
    }

    public void receiveCard(Card card) {
        hand.add(card);
    }

    public void redrawCard(int index, Card newCard) {
        hand.set(index, newCard);
    }

    public void showHand() {
        StringBuilder handRepresentation = new StringBuilder();
        for (Card card : hand) {
            handRepresentation.append(card.toString()).append(" ");
        }
        System.out.println(handRepresentation.toString().trim());
    }

    public int calculateScore() {
        return PokerHandEvaluator.calculateHandScore(this.hand);
    }

    public Card playCard(int index) {
        return hand.remove(index);
    }
    public void saveOriginalHand() {
        originalHand.clear();
        originalHand.addAll(hand); // Store the current hand
    }

    public List<Card> getOriginalHand() {
        return originalHand;
    }
}
