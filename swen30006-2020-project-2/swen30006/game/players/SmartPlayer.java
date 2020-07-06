package game.players;

import ch.aplu.jcardgame.Card;
import game.Game;
import game.Player;

class SmartPlayer extends Player {
    private final Game game;

    private SmartPlayerStrategy strategy;

    public SmartPlayer(Game game) {
        this.game = game;
        strategy = new SampleSmartPlayerStrategy();
    }

    @Override
    public Card play() {
        return strategy.getBestCard(game, hand.getCardList().toArray(Card[]::new));
    }

    @Override
    public void postPlay() {
        strategy.postPlay(game);
    }

    @Override
    protected void onDeal() {
        strategy.reset();
    }
}
