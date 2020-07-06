package game;

import ch.aplu.jcardgame.Card;
import ch.aplu.jcardgame.Deck;
import ch.aplu.jcardgame.Hand;

public interface IGameUI {
    void reinitialize();

    void playerSetHand(int index, Hand hand);

    void notifyTrumpSuit(Suit suit);

    void newTrick(Deck deck);

    void transferToTrick(Card card);

    void notifyPlayerTurn(int index);

    void notifyPlayerWinRound(int index, int score);
}
