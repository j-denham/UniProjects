package game;

import ch.aplu.jcardgame.Card;
import ch.aplu.jcardgame.Deck;
import ch.aplu.jcardgame.Hand;
import game.players.PlayerFactory;

import java.util.*;
import java.util.function.Predicate;
import java.util.function.ToIntFunction;
import java.util.stream.IntStream;

public final class Game {
    private final int cPlayers = 4;
    private final int cCardsPerPlayer;
    private final int cWinScore;

    private final GameOptions options;
    private final Random random;
    private final IGameUI ui;

    private final Deck deck = new Deck(Suit.values(), Rank.values(), "cover");
    private final List<PlayerCardPair> trick;
    private final List<Player> players;

    private Suit trump;

    public Game(IGameUI ui, GameOptions options) {
        this.options = options;
        random = this.options.seed != null ? new Random(this.options.seed) : new Random();
        this.ui = ui;

        this.cCardsPerPlayer = this.options.handSize;
        this.cWinScore = this.options.winScore;

        trick = new LinkedList<>();
        players = new ArrayList<>(cPlayers);
        players.addAll(Arrays.asList(PlayerFactory.makePlayers(this, this.options.playerTypes)));
    }

    public boolean canFollow(Hand hand, Card card) {
        final var lead = trick.get(0);

        // Leading play, always works
        if (lead.card == null)
            return true;

        // Same as lead, always works
        if (lead.card.getSuit().equals(card.getSuit()))
            return true;

        // Only if no matching suit in hand
        return hand.getCardList().stream().filter(c -> lead.card.getSuit().equals(c.getSuit())).count() < 1;
    }

    public Random getRandom() {
        return random;
    }

    public Suit getTrump() {
        return trump;
    }

    public PlayerCardPair[] getTrick() {
        final var array = new PlayerCardPair[trick.size()];
        trick.toArray(array);
        return array;
    }

    public int play() throws BrokeRuleException {
        ui.reinitialize();

        Predicate<Player> winCondition = p -> p.score >= cWinScore;
        int round = 0;
        var leader = options.leading_player;
        if (leader < 0)
            leader = random.nextInt(cPlayers);

        while (players.stream().filter(winCondition).count() < 1) {
            // Deal cards when players run out
            if (round % cCardsPerPlayer == 0) {
                var hands = dealingOut(cPlayers, cCardsPerPlayer, true);
                Arrays.stream(hands).forEach(hand -> hand.sort(Hand.SortType.SUITPRIORITY, true));
                IntStream.range(0, cPlayers).forEach(i -> players.get(i).deal(hands[i]));
                IntStream.range(0, cPlayers).forEach(i -> ui.playerSetHand(i, hands[i]));
                final var suitValues = Suit.values();
                trump = suitValues[random.nextInt(suitValues.length)];
                ui.notifyTrumpSuit(trump);
            }
            leader = playOne(leader);
            ui.notifyPlayerWinRound(leader, players.get(leader).score);
            round += 1;
            IntStream.range(0, cPlayers).forEach(i -> players.get(i).postPlay());
        }

        final var winner = players.stream().filter(winCondition).max(Comparator.comparingInt(info -> info.score));
        return winner.map(players::indexOf).get();
    }

    private int playOne(int leader) throws BrokeRuleException {
        trick.clear();
        IntStream.range(0, cPlayers).forEachOrdered(i -> trick.add(new PlayerCardPair(players.get((i + leader) % cPlayers))));

        ui.newTrick(deck);

        ui.notifyPlayerTurn(leader);
        final var lead = playerPlayCard(leader);
        final var leadSuit = lead.card.getSuit();
        lead.card.setVerso(false);
        addToTrick(lead);
        for (int i = 1; i < cPlayers; ++i) {
            var j = (i + leader) % cPlayers;
            if (!options.clockwise) {
                j = 4 - j;
            }
            ui.notifyPlayerTurn(j);
            var card = playerPlayCard(j);
            addToTrick(card);
        }

        // Calculate the winning player
        final var rankComparator = Comparator.comparingInt((ToIntFunction<PlayerCardPair>) value -> value.getCard().getRankId()).reversed();
        var winning = trick.stream().filter(p -> trump.equals(p.card.getSuit())).max(rankComparator);
        if (winning.isEmpty())
            winning = trick.stream().filter(p -> leadSuit.equals(p.card.getSuit())).max(rankComparator);

        assert winning.isPresent();
        final var winner = (trick.indexOf(winning.get()) + leader) % cPlayers;
        ++players.get(winner).score;
        return winner;
    }

    private void addToTrick(PlayerCardPair pair) {
        final var pair_ = trick.stream().filter(p -> p.player == pair.player).findFirst();
        assert pair_.isPresent();
        pair_.get().card = pair.card;
        ui.transferToTrick(pair.card);
    }

    private PlayerCardPair playerPlayCard(int player) throws BrokeRuleException {
        final var player_ = players.get(player);
        final var card = player_.play();
        if (options.checkedRules) {
            if (!canFollow(player_.hand, card)) {
                final var violation = "Follow rule broken by player " + player + " attempting to play " + card;
                throw new BrokeRuleException(violation);
            }
        }
        return new PlayerCardPair(player_, card);
    }

    public Hand[] dealingOut(int nbPlayers, int nbCardsPerPlayer, boolean shuffle) {
        // Adapted from decompiled bytecode from JCardGame, added dependency
        // on an external random number generator

        var suits = Suit.values();
        var ranks = Rank.values();

        assert nbPlayers * nbCardsPerPlayer <= suits.length * ranks.length;

        var cards = new ArrayList<Card>(suits.length * ranks.length);
        for (var suit : Suit.values()) {
            for (var rank : Rank.values()) {
                cards.add(new Card(deck, suit, rank));
            }
        }

        if (shuffle) {
            Collections.shuffle(cards, random);
        }

        int i = 0;
        var hands = new Hand[nbPlayers + 1];
        for (int j = 0; j < nbPlayers; ++j) {
            hands[j] = new Hand(deck);
            for (int k = 0; k < nbCardsPerPlayer; ++k) {
                hands[j].insert(cards.get(i), false);
                ++i;
            }
        }

        hands[nbPlayers] = new Hand(deck);
        for (; i < cards.size(); ++i) {
            hands[nbPlayers].insert(cards.get(i), false);
        }

        return hands;
    }
}
