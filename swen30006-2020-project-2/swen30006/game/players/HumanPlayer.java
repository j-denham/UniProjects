package game.players;

import ch.aplu.jcardgame.Card;
import ch.aplu.jcardgame.CardAdapter;
import ch.aplu.jcardgame.Hand;
import game.Player;

class HumanPlayer extends Player {
    private Card selectedCard;

    @Override
    public Card play() {
        selectedCard = null;
        hand.setTouchEnabled(true);
        while (selectedCard == null) {
            try {
                Thread.sleep(0);
            } catch (InterruptedException ignored) {
            }
        }
        return selectedCard;
    }

    @Override
    protected void onDeal() {
        hand.addCardListener(new CardAdapter() {
            @Override
            public void leftDoubleClicked(Card card) {
                selectedCard = card;
                hand.setTouchEnabled(false);
            }
        });
    }
}
