package game.players;

import ch.aplu.jcardgame.Card;
import game.Game;
import game.Player;

import java.util.Random;

class RandomPlayer extends Player {
    protected final Random random;

    public RandomPlayer(Game game) {
        random = game.getRandom();
    }

    @Override
    public Card play() {
        return hand.get(random.nextInt(hand.getNumberOfCards()));
    }
}
