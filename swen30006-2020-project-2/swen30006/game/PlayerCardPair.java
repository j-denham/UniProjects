package game;

import ch.aplu.jcardgame.Card;

public class PlayerCardPair {
    // Default access
    final Player player;

    // Default access
    Card card;

    PlayerCardPair(Player player) {
        this.player = player;
    }

    PlayerCardPair(Player player, Card card) {
        this.player = player;
        this.card = card;
    }

    public Player getPlayer() {
        return player;
    }

    public Card getCard() {
        return card;
    }
}
