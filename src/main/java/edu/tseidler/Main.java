package edu.tseidler;

import edu.tseidler.model.*;
import edu.tseidler.service.FileLineConsumer;
import edu.tseidler.service.FileLineSupplier;
import edu.tseidler.states.OXGame;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class Main {

    private static final Supplier<String> DEFAULT_STRING_SUPPLIER = new Scanner(System.in)::nextLine;
    private static final Logger logger = Logger.getLogger(Main.class);
    private static final Consumer<String> DEFAULT_STRING_CONSUMER = System.out::println;
    private static final Consumer<String> ERROR_CONSUMER = System.err::println;

    public static void main(String[] args) {

        Supplier<String> inputSupplier = getStringSupplier();

        Consumer<String> output = getStringConsumer();

        Language lang = loadLanguage();

        Board board = getBoard();

        PlayerList playerList = getPlayerList();
        System.out.println(playerList);
        System.exit(1);

        new OXGame(inputSupplier, output, "en").start();
    }

    private static PlayerList getPlayerList() {
        PlayerList playerList = new PlayerList();
        String player1Name = (String) properties.getOrDefault("player1_name",
                defaultProperties.get("player1_name"));
        BoardField player1Mark = BoardField.valueOf((String) properties.getOrDefault("player1_mark",
                defaultProperties.get("player1_mark")));
        boolean player1First = Boolean.valueOf((String) properties.getOrDefault("player1_first",
                defaultProperties.get("player1_first")));
        String player2Name = (String) properties.getOrDefault("player2_name",
                defaultProperties.get("player2_name"));
        BoardField player2Mark = BoardField.valueOf((String) properties.getOrDefault("player2_mark",
                defaultProperties.get("player2_mark")));
        boolean player2First = Boolean.valueOf((String) properties.getOrDefault("player2_first",
                defaultProperties.get("player2_first")));
        if (player1Mark == player2Mark) {
            player2Mark = player1Mark.other();
            logger.log(Level.WARN, "player 2 mark adjusted to the opposite of player 1");
        }
        if (player1First == player2First) {
            player2First = !player1First;
            logger.log(Level.WARN, "player 2 starting adjusted to opposite of player 1");
        }
        playerList.add(new Player(player1Name, player1Mark, player1First));
        playerList.add(new Player(player2Name, player2Mark, player2First));
        return playerList;
    }

    private static Language loadLanguage() {
        String langShort = (String) properties.getOrDefault("language", "en");
        Set<String> available = new HashSet<String>() {{
            add("en");
            add("pl");
        }};
        if (!available.contains(langShort))
            langShort = (String) defaultProperties.get("language");
        return new Language(langShort);
    }

    private static Properties defaultProperties = new Properties() {{
        setProperty("input", "stdin");
        setProperty("output", "stdout");
        setProperty("language", "en");
        setProperty("player1_name", "Jacek");
        setProperty("player1_mark", "X");
        setProperty("player1_first", "true");
        setProperty("player2_name", "Placek");
        setProperty("player2_mark", "O");
        setProperty("player2_first", "false");
        setProperty("board_rows", "3");
        setProperty("board_cols", "3");
        setProperty("board_winn", "3");
    }};

    private static Board getBoard() {
        int board_rows = Integer.valueOf((String) defaultProperties.get("board_rows"));
        int board_cols = Integer.valueOf((String) defaultProperties.get("board_cols"));
        int board_winn = Integer.valueOf((String) defaultProperties.get("board_winn"));
        try {
            board_rows = Integer.valueOf((String) properties.get("board_rows"));
            board_cols = Integer.valueOf((String) properties.get("board_cols"));
            board_winn = Integer.valueOf((String) properties.get("board_winn"));
        } catch (Exception e) {
            logger.log(Level.ERROR, "error parsing board parameters from settings file");
        }
        BoardParameters parameters = new BoardParameters(board_rows, board_cols, board_winn);
        return new Board(parameters);
    }

    private static Consumer<String> getStringConsumer() {
        Map<String, Consumer<String>> consumerMap = new HashMap<String, Consumer<String>>() {{
            put("stdout", DEFAULT_STRING_CONSUMER);
            put("stderr", ERROR_CONSUMER);
            try {
                Path path = Paths.get((String) properties.getOrDefault("output_file", "output.log"));
                put("file", new FileLineConsumer(path));
            } catch (IOException e) {
                logger.log(Level.WARN, "file operation error - falling back to console output");
                put("file", DEFAULT_STRING_CONSUMER);
            }
        }};

        return consumerMap.get(properties.get("output"));
    }

    private static Supplier<String> getStringSupplier() {
        Map<String, Supplier<String>> supplierMap = new HashMap<String, Supplier<String>>() {{
            put("stdin", DEFAULT_STRING_SUPPLIER);
            try {
                put("file", new FileLineSupplier(Main.class.getResource((String) properties.getOrDefault("input_file", "scenarios/error")).getPath()));
            } catch (NullPointerException e) {
                logger.log(Level.WARN, "input file in settings.properties doesn't exist - falling back to console input");
                put("file", DEFAULT_STRING_SUPPLIER);
            }
        }};
        return supplierMap.getOrDefault(properties.get("input"), DEFAULT_STRING_SUPPLIER);
    }

    private static Properties properties = new Properties() {{
        try (InputStream inputStream = Main.class.getResourceAsStream("settings.properties")) {
            load(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }};
}
