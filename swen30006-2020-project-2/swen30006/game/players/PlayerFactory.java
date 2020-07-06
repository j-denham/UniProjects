package game.players;

import game.Game;
import game.Player;
import game.PlayerType;

import java.util.Arrays;

public class PlayerFactory {
    public static Player[] makePlayers(Game game, PlayerType[] types) {
        return Arrays.stream(types).map(type -> {
            switch (type) {
                case random:
                    return new RandomPlayer(game);
                case legal:
                    return new LegalPlayer(game);
                case human:
                    return new HumanPlayer();
                case smart:
                    return new SmartPlayer(game);
                default:
                    throw new IllegalArgumentException();
            }
        }).toArray(Player[]::new);
    }
}
