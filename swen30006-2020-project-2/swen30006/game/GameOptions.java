package game;

import java.util.Arrays;

public final class GameOptions {
    public final Long seed;
    public final int handSize;
    public final int leading_player;
    public final int winScore;
    public final boolean clockwise;
    public final PlayerType[] playerTypes;
    public final boolean checkedRules;

    public GameOptions(Long seed, int handSize, int leadingPlayer, int winScore, boolean clockwise, PlayerType[] playerTypes, boolean checkedRules) {
        this.seed = seed;
        this.handSize = handSize;
        this.leading_player = leadingPlayer;
        this.winScore = winScore;
        this.clockwise = clockwise;
        this.playerTypes = Arrays.copyOf(playerTypes, playerTypes.length);
        this.checkedRules = checkedRules;
    }
}
