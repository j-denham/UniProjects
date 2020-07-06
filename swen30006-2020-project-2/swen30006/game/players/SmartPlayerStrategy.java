package game.players;

import ch.aplu.jcardgame.Card;
import game.Game;

interface SmartPlayerStrategy {
    Card getBestCard(Game game, Card[] hand);
    void postPlay(Game game);
    void reset();
}
