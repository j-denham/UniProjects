package game;

import ch.aplu.jcardgame.Card;
import ch.aplu.jcardgame.Hand;

public abstract class Player {
    protected Hand hand;

    // Java default access modifier (i.e. not having one): Accessible in this package only
    // This requires Player to be in the game package rather than game.players.
    // Must be done this way so that Player subclasses in game.players cannot change their own score
    // while allowing the Game class to manage this attribute.
    int score = 0;

    public final void deal(Hand hand) {
        this.hand = hand;
        onDeal();
    }

    public final int getScore() {
        return score;
    }

    public abstract Card play();

    public void postPlay() {
    }

    protected void onDeal() {
    }
}
