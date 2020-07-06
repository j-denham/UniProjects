package game.players;

import ch.aplu.jcardgame.Card;
import game.Game;

import java.util.ArrayList;
import java.util.Arrays;

class LegalPlayer extends RandomPlayer {
    private final Game game;

    public LegalPlayer(Game game) {
        super(game);
        this.game = game;
    }

    @Override
    public Card play() {
        Card card;
        var trick = game.getTrick();
        // Player is leader
        if (Arrays.stream(trick).filter(p -> p.getCard() != null).count() < 1) {
            card = hand.get(random.nextInt(this.hand.getNumberOfCards()));
        } else {
            var lead = trick[0].getCard();
            ArrayList<Card> valid = hand.getCardsWithSuit(lead.getSuit());
            if (valid.size() == 0) {
                card = hand.get(random.nextInt(hand.getNumberOfCards()));
            } else {
                card = valid.get(random.nextInt(valid.size()));
            }
        }
        assert game.canFollow(hand, card);
        return card;
    }
}
