import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class PokerHandEvaluator {

    public static Map<String, Integer> countRanks(List<Card> hand) {
        Map<String, Integer> rankCount = new HashMap<>();
        for (Card card : hand) {
            rankCount.put(card.getRank(), rankCount.getOrDefault(card.getRank(), 0) + 1);
        }
        return rankCount;
    }

    public static Map<String, Integer> countSuits(List<Card> hand) {
        Map<String, Integer> suitCount = new HashMap<>();
        for (Card card : hand) {
            suitCount.put(card.getSuit(), suitCount.getOrDefault(card.getSuit(), 0) + 1);
        }
        return suitCount;
    }

    public static int calculateHandScore(List<Card> hand) {
        if (hasStraightFlush(hand)) return 15;
        if (hasFourOfAKind(hand)) return 10;
        if (hasFullHouse(hand)) return 8;
        if (hasFlush(hand)) return 5;
        if (hasBroadwayStraight(hand)) return 5000; // Special case: Ace-to-Ten straight
        if (hasRegularStraight(hand)) return 4; // Other straights
        if (hasThreeOfAKind(hand)) return 3;
        if (hasTwoPair(hand)) return 2;
        if (hasPair(hand)) return 1;
        return 0; // High card, or no valid hand
    }
    

    public static int rankValue(Card card) {
        switch (card.getRank()) {
            case "A":
                return 14;
            case "K":
                return 13;
            case "Q":
                return 12;
            case "J":
                return 11;
            default:
                return Integer.parseInt(card.getRank());
        }
    }

    public static boolean hasPair(List<Card> hand) {
        Map<String, Integer> rankCount = countRanks(hand);
        for (int count : rankCount.values()) {
            if (count == 2) {
                return true;
            }
        }
        return false;
    }

    public static boolean hasTwoPair(List<Card> hand) {
        Map<String, Integer> rankCount = countRanks(hand);
        int pairs = 0;
        for (int count : rankCount.values()) {
            if (count == 2) {
                pairs++;
            }
        }
        return pairs == 2;
    }

    public static boolean hasThreeOfAKind(List<Card> hand) {
        Map<String, Integer> rankCount = countRanks(hand);
        for (int count : rankCount.values()) {
            if (count == 3) {
                return true;
            }
        }
        return false;
    }

    public static boolean hasRegularStraight(List<Card> hand) {
        List<Integer> sortedValues = hand.stream()
            .map(PokerHandEvaluator::rankValue)
            .distinct()
            .sorted()
            .collect(Collectors.toList());
    
        for (int i = 0; i <= sortedValues.size() - 5; i++) {
            if (sortedValues.get(i) + 4 == sortedValues.get(i + 4) &&
                sortedValues.get(i + 1) == sortedValues.get(i) + 1 &&
                sortedValues.get(i + 2) == sortedValues.get(i) + 2 &&
                sortedValues.get(i + 3) == sortedValues.get(i) + 3) {
                return true;
            }
        }
    
        return false;
    }

    public static boolean hasBroadwayStraight(List<Card> hand) {
        List<Integer> sortedValues = hand.stream()
            .map(PokerHandEvaluator::rankValue)
            .sorted()
            .collect(Collectors.toList());
    
        // Check specifically for the Ace-to-Ten straight (10, J, Q, K, A)
        return sortedValues.contains(14) && sortedValues.contains(13) &&
               sortedValues.contains(12) && sortedValues.contains(11) &&
               sortedValues.contains(10);
    }

    public static boolean hasFlush(List<Card> hand) {
        Map<String, Integer> suitCount = countSuits(hand);
        for (int count : suitCount.values()) {
            if (count == 5) {
                return true;
            }
        }
        return false;
    }

    public static boolean hasFullHouse(List<Card> hand) {
        boolean hasThree = false;
        boolean hasPair = false;
        Map<String, Integer> rankCount = countRanks(hand);

        for (int count : rankCount.values()) {
            if (count == 3) {
                hasThree = true;
            } else if (count == 2) {
                hasPair = true;
            }
        }
        return hasThree && hasPair;
    }

    public static boolean hasFourOfAKind(List<Card> hand) {
        Map<String, Integer> rankCount = countRanks(hand);
        for (int count : rankCount.values()) {
            if (count == 4) {
                return true;
            }
        }
        return false;
    }

    public static boolean hasStraightFlush(List<Card> hand) {
        return (hasRegularStraight(hand) || hasBroadwayStraight(hand)) && hasFlush(hand);
    }

}
