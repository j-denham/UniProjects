// Whist.java

import ch.aplu.jcardgame.*;
import ch.aplu.jgamegrid.Actor;
import ch.aplu.jgamegrid.Location;
import ch.aplu.jgamegrid.TextActor;
import game.*;

import java.awt.*;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.stream.IntStream;

public final class Whist extends CardGame implements IGameUI {
    public static void main(String[] args) throws BrokeRuleException, IOException {
        var path = "whist.properties";
        if (args.length > 0) {
            path = args[0];
        }
        GameOptions opt;
        try (var stream = new FileInputStream(path)) {
            opt = parseGameOptions(stream);
        }
        var ui = new Whist();
        var game = new Game(ui, opt);
        var winner = game.play();
        ui.addActor(new Actor("sprites/gameover.gif"), textLocation);
        ui.setStatusText("Game over. Winner is player: " + winner);
        ui.refresh();
    }

    private static GameOptions parseGameOptions(InputStream stream) throws IOException {
        var properties = new Properties();
        properties.load(stream);

        Long seed = null;
        var seedStr = properties.getProperty("seed");
        if (seedStr != null) {
            seed = Long.parseLong(seedStr);
        }
        var handSize = Integer.parseInt(properties.getProperty("handSize"));
        var leadingPlayer = Integer.parseInt(properties.getProperty("leadingPlayer"));
        var winScore = Integer.parseInt(properties.getProperty("winScore"));
        var clockwise = Boolean.parseBoolean(properties.getProperty("clockwise"));
        var playerTypes = new PlayerType[4];
        IntStream.range(0, 4).forEachOrdered(
                i -> playerTypes[i] = PlayerType.valueOf(properties.getProperty("playerType" + i)));
        var checkedRules = Boolean.parseBoolean(properties.getProperty("checkedRules"));

        return new GameOptions(seed, handSize, leadingPlayer, winScore, clockwise, playerTypes, checkedRules);
    }

    private Whist() {
        super(700, 700, 30);
        setTitle("Whist (V" + version + ") Constructed for UofM SWEN30006 with JGameGrid (www.aplu.ch)");
    }

    @Override
    public void reinitialize() {
        removeAllActors();
        IntStream.range(0, scoreActors.length).forEach(
                i -> scoreActors[i] = new TextActor("0", Color.WHITE, bgColor, bigFont));
        IntStream.range(0, scoreActors.length).forEach(
                i -> addActor(scoreActors[i], scoreLocations[i]));
    }

    @Override
    public void playerSetHand(int index, Hand hand) {
        var layout = new RowLayout(handLocations[index], handWidth);
        layout.setRotationAngle(90 * index);
        hand.setView(this, layout);
        hand.setTargetArea(new TargetArea(trickLocation));
        hand.draw();
    }

    @Override
    public void notifyTrumpSuit(Suit suit) {
        if (trumpActor != null)
            removeActor(trumpActor);
        trumpActor = new Actor("sprites/" + trumpImage[suit.ordinal()]);
        addActor(trumpActor, trumpsActorLocation);
    }

    @Override
    public void newTrick(Deck deck) {
        if (trick != null)
            trick.removeAll(true);
        trick = new Hand(deck);
    }

    @Override
    public void transferToTrick(Card card) {
        trick.setView(
                this,
                new RowLayout(trickLocation, (trick.getNumberOfCards() + 2) * trickWidth));
        trick.draw();
        card.transfer(trick, true);
    }

    @Override
    public void notifyPlayerTurn(int index) {
        setStatusText("Player " + index + "'s turn...");
    }

    @Override
    public void notifyPlayerWinRound(int index, int score) {
        setStatusText("Player " + index + " wins trick.");
        removeActor(scoreActors[index]);
        scoreActors[index] = new TextActor(String.valueOf(score), Color.WHITE, bgColor, bigFont);
        addActor(scoreActors[index], scoreLocations[index]);
    }

    private Actor[] scoreActors = new Actor[4];
    private Actor trumpActor;
    private Hand trick;

    private static final String version = "1.0";
    private static final Font bigFont = new Font("Serif", Font.BOLD, 36);
    private static final Location[] scoreLocations = {
            new Location(575, 675),
            new Location(25, 575),
            new Location(575, 25),
            new Location(650, 575)
    };
    private static final Location[] handLocations = {
            new Location(350, 625),
            new Location(75, 350),
            new Location(350, 75),
            new Location(625, 350)
    };
    private static final int handWidth = 400;
    private static final Location trickLocation = new Location(350, 350);
    private static final String trumpImage[] = {"bigspade.gif", "bigheart.gif", "bigdiamond.gif", "bigclub.gif"};
    private static final Location trumpsActorLocation = new Location(50, 50);
    private static final int trickWidth = 40;

    private static final Location textLocation = new Location(350, 450);
}
