package game.players;

import ch.aplu.jcardgame.Card;
import game.Game;
import game.Player;
import game.Rank;

import java.util.*;
import java.util.stream.Collectors;

class SampleSmartPlayerStrategy implements SmartPlayerStrategy {
    private HashMap<Player, ArrayList<Card>> playedCards = new HashMap<>();

    // Used when leading
    private SmartPlayerStrategy leadingStrategy = new LeadingStrategy();
    // Used when have leading
    private SmartPlayerStrategy followStrategy = new FollowStrategy();
    // Used when no leading but have trump
    private SmartPlayerStrategy trumpStrategy = new TrumpStrategy();
    // Used when no leading and no trump
    private SmartPlayerStrategy burnStrategy = new BurnStrategy();

    @Override
    public Card getBestCard(Game game, Card[] hand) {
        var trick = game.getTrick();

        // Record played cards right before thinking
        Arrays.stream(trick).forEach(p -> {
            playedCards.putIfAbsent(p.getPlayer(), new ArrayList<>());
            if (p.getCard() != null) {
                playedCards.get(p.getPlayer()).add(p.getCard());
            }
        });

        Card result;

        var leading = trick[0].getCard();
        if (leading == null) {
            result = leadingStrategy.getBestCard(game, hand);
        } else if (Arrays.stream(hand).anyMatch(c -> c.getSuit().equals(leading.getSuit()))) {
            result = followStrategy.getBestCard(game, hand);
        } else if (Arrays.stream(hand).anyMatch(c -> c.getSuit().equals(game.getTrump()))) {
            result = trumpStrategy.getBestCard(game, hand);
        } else {
            result = burnStrategy.getBestCard(game, hand);
        }

        return result;
    }

    @Override
    public void postPlay(Game game) {
        // Record played cards
        Arrays.stream(game.getTrick()).forEach(p -> playedCards.get(p.getPlayer()).add(p.getCard()));
    }

    @Override
    public void reset() {
        playedCards.forEach((key, value) -> value.clear());
    }

    private class LeadingStrategy extends SmartPlayerStrategyBase {
        @Override
        public Card getBestCard(Game game, Card[] hand) {
            assert hand.length > 0;

            var trumps = Arrays.stream(hand).filter(c -> c.getSuit().equals(game.getTrump())).toArray(Card[]::new);
            if (trumps.length > 0)
                return trumpStrategy.getBestCard(game, hand);

            // Apply the burn strategy to save larger cards i.e. increase chances of winning later
            return burnStrategy.getBestCard(game, hand);
        }
    }

    private class FollowStrategy extends SmartPlayerStrategyBase {
        @Override
        public Card getBestCard(Game game, Card[] hand) {
            var lead = game.getTrick()[0].getCard();

            var byLead = Arrays.stream(hand).filter(c -> c.getSuit().equals(lead.getSuit()))
                    .sorted(Comparator.comparingInt(Card::getRankId)).collect(Collectors.toList());
            assert byLead.size() > 0;

            // If all the cards larger than our largest card in the leading suit have been played
            // then we can play the largest card with full confidence of winning the trick
            // unless someone else has a trump card
            // but we cannot beat that here anyway because we have cards of the leading suit
            var removedByLeadSuit = playedCards.entrySet().stream().flatMap(e -> e.getValue().stream())
                    .filter(c -> c.getSuit().equals(lead.getSuit())).collect(Collectors.toList());
            if (removedByLeadSuit.stream().filter(c -> c.getRankId() < byLead.get(0).getRankId()).count() ==
                    byLead.get(0).getRankId())
                return byLead.get(0);

            // Burn the smallest card
            return byLead.get(byLead.size() - 1);
        }
    }

    private class TrumpStrategy extends SmartPlayerStrategyBase {
        @Override
        public Card getBestCard(Game game, Card[] hand) {
            // Basically the same algorithm as above except follow the trump suit instead of the leading suit

            var trump = game.getTrump();

            var byTrump = Arrays.stream(hand).filter(c -> c.getSuit().equals(trump))
                    .sorted(Comparator.comparingInt(Card::getRankId)).collect(Collectors.toList());
            assert byTrump.size() > 0;

            var ace = byTrump.stream().filter(c -> c.getRank().equals(Rank.ACE)).findFirst();
            if (ace.isPresent())
                return ace.get();

            var removedByTrumpSuit = playedCards.entrySet().stream().flatMap(e -> e.getValue().stream())
                    .filter(c -> c.getSuit().equals(trump)).collect(Collectors.toList());
            if (removedByTrumpSuit.stream().filter(c -> c.getRankId() < byTrump.get(0).getRankId()).count() ==
                    byTrump.get(0).getRankId())
                return byTrump.get(0);

            return byTrump.get(byTrump.size() - 1);
        }
    }

    // Static because it does not depend on instance attributes in SampleSmartPlayerStrategy
    private static class BurnStrategy extends SmartPlayerStrategyBase {
        @Override
        public Card getBestCard(Game game, Card[] hand) {
            // Play the smallest card in the suit with least cards
            // NOTE: This algorithm does not constrain the suit of the card played
            //       so it is not suitable when there are cards of the leading suit in hand
            var bySuits = Arrays.stream(hand).collect(Collectors.groupingBy(Card::getSuit));
            var countBySuit = bySuits.entrySet().stream().collect(
                    Collectors.toMap(Map.Entry::getKey, e -> e.getValue().size()));
            assert countBySuit.size() > 0;
            var smallestSuit = bySuits.get(
                    countBySuit.entrySet().stream().min(Comparator.comparingInt(Map.Entry::getValue)).get().getKey());
            assert smallestSuit.size() > 0;
            return smallestSuit.stream().max(Comparator.comparingInt(Card::getRankId)).get();
        }
    }

    private static abstract class SmartPlayerStrategyBase implements SmartPlayerStrategy {
        @Override
        public void postPlay(Game game) {
        }

        @Override
        public void reset() {
        }
    }
}
