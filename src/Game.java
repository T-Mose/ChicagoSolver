import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

public class Game {
    public static void main(String[] args) {
        ChicagoGame game = new ChicagoGame();

        // Adding a regular human player
        game.addPlayer(new Player("Alice"));
        game.addPlayer(new Player("Theo"));

        // Adding an AI player
        // game.addPlayer(new AIPlayer("AI Bob", new MaximizeOutplayStrategy(), new
        // ConservativeOutplayStrategy()));

        game.startGame();
    }
}

class ChicagoGame {
    private Deck deck;
    private List<Player> players;
    private Scanner scanner;
    private Player firstPlayer; // Tracks the player who starts each round

    public ChicagoGame() {
        deck = new Deck();
        players = new ArrayList<>();
        scanner = new Scanner(System.in);
    }

    public void addPlayer(Player player) {
        players.add(player);
    }

    public void startGame() {
        int round = 0;
        while (!playerHasWon() || round > 30) {
            playRound(round);
            round++;
        }
        System.out.println("The game is over and the winner is...:");
        System.out.println(firstPlayer.getName());
    }

    private boolean playerHasWon() {
        for (Player player : players) {
            if (player.getPoints() >= 52) {
                firstPlayer = player;
                return true;
            }
        }
        return false;
    }

    public void playRound(int round) {
        deck.shuffle();
        dealInitialCards();
        firstPlayer = players.get(round % players.size()); // Set the initial starting player

        for (Player player : players) {
            System.out.println(player.getName() + ", your hand:");
            player.showHand();
            if (player instanceof AIPlayer) {
                ((AIPlayer) player).executeRedraw(deck);
            } else {
                handleHumanPlayerRedraw(player);
            }
        }

        determineBestHandWinner(false);

        // Second round of redraws and outplay phase
        for (Player player : players) {
            System.out.println(player.getName() + ", your hand:");
            player.showHand();
            if (player instanceof AIPlayer) {
                ((AIPlayer) player).executeRedraw(deck);
            } else {
                handleHumanPlayerRedraw(player);
            }
        }

        // Save each player's original hand before outplay begins
        for (Player player : players) {
            player.saveOriginalHand();
        }

        playOutplayRounds();

        determineBestHandWinner(true); // Final scoring after outplay phase
    }

    private void dealInitialCards() {
        for (Player player : players) {
            for (int i = 0; i < 5; i++) {
                player.receiveCard(deck.drawCard());
            }
        }
    }

    private void handleHumanPlayerRedraw(Player player) {
        System.out.println(player.getName()
                + ", discard the cards you want, as example input: AH JS (or type 'all' to discard all cards, or press Enter to keep all cards):");
        String input = scanner.nextLine().trim();

        if (input.equalsIgnoreCase("all")) {
            // Discard all cards and redraw
            for (int i = 0; i < player.getHand().size(); i++) {
                player.redrawCard(i, deck.drawCard());
            }
        } else if (!input.isEmpty()) {
            // Discard specific cards based on input
            String[] cardsToDiscard = input.split(" ");
            for (String cardNotation : cardsToDiscard) {
                discardAndRedraw(player, cardNotation);
            }
        }

        System.out.println("Your new hand:");
        player.showHand();
    }

    private void discardAndRedraw(Player player, String cardNotation) {
        List<Card> hand = player.getHand();
        Card cardToDiscard = null;

        for (Card card : hand) {
            if (card.toString().equalsIgnoreCase(cardNotation)) {
                cardToDiscard = card;
                break;
            }
        }

        if (cardToDiscard != null) {
            int index = hand.indexOf(cardToDiscard);
            Card newCard = deck.drawCard();
            player.redrawCard(index, newCard);
        } else {
            System.out.println("Card " + cardNotation + " not found in your hand.");
        }
    }

    private void determineBestHandWinner(boolean useOriginalHands) {
        Player bestPlayer = null;
        int bestScore = 0;
        List<Player> tiedPlayers = new ArrayList<>();

        for (Player player : players) {
            List<Card> handToEvaluate = useOriginalHands ? player.getOriginalHand() : player.getHand();
            int score = PokerHandEvaluator.calculateHandScore(handToEvaluate);
            System.out.println(player.getName() + " has a score of " + score);

            if (score > bestScore) {
                bestScore = score;
                bestPlayer = player;
                tiedPlayers.clear(); // Reset tied players list as we have a new best player
                tiedPlayers.add(player);
            } else if (score == bestScore) {
                int comparisonResult = compareHands(bestPlayer.getHand(), handToEvaluate);
                if (comparisonResult < 0) { // Current player's hand is stronger
                    bestPlayer = player;
                    tiedPlayers.clear(); // Reset tied players list as we have a new best player
                    tiedPlayers.add(player);
                } else if (comparisonResult == 0) { // Hands are identical in strength
                    tiedPlayers.add(player);
                }
            }
        }

        // Award points to the best player(s)
        if (tiedPlayers.size() == 1) {
            System.out.println(bestPlayer.getName() + " wins this round with the best hand!");
            // Add points to the best player
        } else {
            System.out.println("It's a tie between:");
            for (Player player : tiedPlayers) {
                System.out.println(player.getName());
                // Add points to each tied player
            }
        }
    }

    private int compareHands(List<Card> hand1, List<Card> hand2) {
        // Compare hands based on their card ranks and suits
        // This example assumes both hands have the same score (e.g., both are pairs,
        // both are flushes, etc.)

        List<Integer> hand1Values = hand1.stream()
                .map(PokerHandEvaluator::rankValue)
                .sorted((a, b) -> b - a) // Sort in descending order
                .collect(Collectors.toList());

        List<Integer> hand2Values = hand2.stream()
                .map(PokerHandEvaluator::rankValue)
                .sorted((a, b) -> b - a) // Sort in descending order
                .collect(Collectors.toList());

        for (int i = 0; i < hand1Values.size(); i++) {
            int comparison = hand1Values.get(i).compareTo(hand2Values.get(i));
            if (comparison != 0) {
                return comparison;
            }
        }

        // If hands are identical in terms of rank values, consider them equal
        return 0;
    }

    private void playOutplayRounds() {
        for (int i = 0; i < 5; i++) { // Assuming 5 rounds of outplay
            firstPlayer = playOutplayRound(firstPlayer);
        }
        System.out.println(firstPlayer.getName() + " Won the outplay and 5 points! The points are: ");
        firstPlayer.changePoints(5);
        displayPoints();
    }

    private void displayPoints() {
        for (Player player : players) {
            System.out.println(player.getPoints());
        }
    }

    private Player playOutplayRound(Player startingPlayer) {
        List<Card> cardsPlayed = new ArrayList<>();
        Card leadingCard = null;
        String leadingSuit = "";
        Player roundWinner = null;

        int currentPlayerIndex = players.indexOf(startingPlayer);
        int firstPlayerIndex = currentPlayerIndex; // Track the starting player for this round

        while (cardsPlayed.size() < players.size()) {
            Player currentPlayer = players.get(currentPlayerIndex);
            System.out.println(currentPlayer.getName() + ", choose a card to play:");
            currentPlayer.showHand();
            int index;
            if (currentPlayer instanceof AIPlayer) {
                index = ((AIPlayer) currentPlayer).chooseCardToPlay();
            } else {
                System.out.println("Enter the index of the card you want to play (1-5): ");
                index = scanner.nextInt() - 1;
                scanner.nextLine(); // consume the newline
            }

            Card playedCard = currentPlayer.playCard(index);
            System.out.println(currentPlayer.getName() + " played " + playedCard);

            if (leadingCard == null) {
                // This is the first card played in the round
                leadingCard = playedCard;
                leadingSuit = playedCard.getSuit();
                roundWinner = currentPlayer; // Initially assume the first player is the winner
            } else if (!playedCard.getSuit().equals(leadingSuit) && playerHasSuit(currentPlayer, leadingSuit)) {
                // Invalid move: Player did not follow the leading suit
                System.out.println("You must follow the suit of the leading card!");
                currentPlayer.getHand().add(playedCard); // Return the card to player's hand
                continue; // Skip the rest of the loop and let the same player play again
            } else if (playedCard.getSuit().equals(leadingSuit) &&
                    PokerHandEvaluator.rankValue(playedCard) > PokerHandEvaluator.rankValue(leadingCard)) {
                // If the played card is of the leading suit and higher, update the round winner
                leadingCard = playedCard;
                roundWinner = currentPlayer;
            }

            cardsPlayed.add(playedCard);
            currentPlayerIndex = (currentPlayerIndex + 1) % players.size(); // Move to the next player
        }

        if (roundWinner != null) {
            System.out.println(roundWinner.getName() + " wins the round with " + leadingCard + "!");
        }

        return roundWinner; // The winner of this round will start the next round
    }

    private boolean playerHasSuit(Player player, String suit) {
        for (Card card : player.getHand()) {
            if (card.getSuit().equals(suit)) {
                return true;
            }
        }
        return false;
    }
}
