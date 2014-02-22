package scrabble;

import java.awt.Point;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

/**
 *
 * Main class
 * 
 * @author MehSki11zOwn
 */
public class Scrabble {

    /**
     * String containing letters that can be played.
     */
    public static String letters;

    /**
     * Map mapping each letter in the English alphabet to a specific point value.
     */
    public static final Map<String, Integer> pointKey = new HashMap<>();

    /**
     * UI object.
     */
    public static UI ui;

    /**
     * Main method.
     * 
     * @param args &lt;name of game/storage file&gt; &lt;letters currently in hand&gt;
     */
    public static void main(final String[] args) {
        UI.storage = new File(System.getProperty("user.home") + File.separator + "ScrabbleBot" + File.separator + args[0] + ".txt");
        letters = args[1];
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (final ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ignored) {}
        pointKey.put("a", 1);
        pointKey.put("b", 4);
        pointKey.put("c", 4);
        pointKey.put("d", 2);
        pointKey.put("e", 1);
        pointKey.put("f", 4);
        pointKey.put("g", 3);
        pointKey.put("h", 3);
        pointKey.put("i", 1);
        pointKey.put("j", 10);
        pointKey.put("k", 5);
        pointKey.put("l", 2);
        pointKey.put("m", 4);
        pointKey.put("n", 2);
        pointKey.put("o", 1);
        pointKey.put("p", 4);
        pointKey.put("q", 10);
        pointKey.put("r", 1);
        pointKey.put("s", 1);
        pointKey.put("t", 1);
        pointKey.put("u", 2);
        pointKey.put("v", 5);
        pointKey.put("w", 4);
        pointKey.put("x", 8);
        pointKey.put("y", 3);
        pointKey.put("z", 10);
        ui = new UI();
        getBestMove();
    }

    /**
     * Scans each possible move and loops through all the possible words that can be formed and prints highest scoring move.
     */
    public static void getBestMove() {
        Regex reg = new Regex(null, null, null, false);
        String newWord = "";
        int highest = 0, words = 0;
        for (final Regex regex : new MoveFinder().getValidTiles()) {
            for (final String word : getWords(letters + regex.playOff)) {
                words ++;
                final int points = getPoints(regex, word);
                if (word.matches(regex.regex) && !word.equals(regex.playOff) && points > highest) {
                    reg = regex;
                    newWord = word;
                    highest = points;
                }
            }
        }
        System.out.println("Scanned " + words + " words");
        System.out.println(reg.start + "\t" + newWord + "\t" + highest + "\t" + reg.regex);
    }

    /**
     * Returns a list of possible words that can be formed using the characters inputted.
     * 
     * @param chars a String containing each character to use
     * @return      a list of possible words
     * @see         java.util.ArrayList
     */
    public static ArrayList<String> getWords(final String chars) {
        final ArrayList<String> words = new ArrayList<>();
        String total = "";
        try {
            final URLConnection spoof = new URL("http://wordfinder.yourdictionary.com/unscramble/" + chars + "?remember_tiles=false").openConnection();
            spoof.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 5.5; Windows NT 5.0;    H010818)");
            try (final BufferedReader in = new BufferedReader(new InputStreamReader(spoof.getInputStream()))) {
                String str;
                while ((str = in.readLine()) != null) {
                    total += str;
                }
                in.close();
            }
        } catch (final IOException ex) {}
        total = total.substring(total.indexOf("<hr>") + 4, total.indexOf("</section>", total.indexOf("<hr>")));
        for (final String table : total.split("<a name=\"[0-9]\">")) {
            if (!table.startsWith("</a>")) {
                continue;
            }
            for (final String line : table.split("<a href=")) {
                if (!line.startsWith("'http://www.yourdictionary.com/")) {
                    continue;
                }
                for (final String row : line.split("<tr>")) {
                    if (row.equals("<td>")) {
                        continue;
                    }
                    words.add(row.substring(row.indexOf(">") + 1, row.startsWith("<td>") ? row.indexOf("</td>") : row.indexOf("</a>")));
                }
            }
        }
        return words;
    }

    /**
     * Calculates the amount of points a specific word would yield when played in a specific position on the board.
     * 
     * @param r    the Regex to scan
     * @param word the word to calculate points from
     * @return     the amount of points the word yields
     * @see        Regex
     */
    public static int getPoints(final Regex r, final String word) {
        final ArrayList<Point> used = new ArrayList<>();
        final char[] usedStr = r.playOff.toCharArray();
        for (int i = 0; i < usedStr.length; i ++) {
            used.add(new Point(r.start.x + (r.vert ? 0 : i), r.start.y + (r.vert ? i : 0)));
        }
        int x, y, multiplier = 1, total = 0;
        x = r.start.x - (r.vert ? 0 : word.indexOf(r.playOff));
        y = r.start.y - (r.vert ? word.indexOf(r.playOff) : 0);
        final char[] array = word.toCharArray();
        for (int i = 0; i < array.length; i ++) {
            final Point test = new Point(x + (r.vert ? 0 : i), y + (r.vert ? i : 0));
            int changed = 0;
            for (final Point p : used) {
                if (p.x == test.x && p.y == test.y) {
                    changed = pointKey.get(word.toCharArray()[i] + "");
                    break;
                }
            }
            if (changed != 0) {
                total += changed;
                continue;
            }
            switch (ui.getType(test.x, test.y)) {
                case 0:
                    total += pointKey.get(word.toCharArray()[i] + "");
                    break;
                case 1:
                    total += pointKey.get(word.toCharArray()[i] + "") * 2;
                    break;
                case 2:
                    total += pointKey.get(word.toCharArray()[i] + "");
                    multiplier *= 2;
                    break;
                case 3:
                    total += pointKey.get(word.toCharArray()[i] + "") * 3;
                    break;
                case 4:
                    total += pointKey.get(word.toCharArray()[i] + "");
                    multiplier *= 3;
                    break;
            }
        }
        return total * multiplier;
    }

    /**
     * Class containing all regex data from each move possibility.
     */
    public static class Regex {

        /**
         * Starting point on grid for regex.
         */
        public Point start;

        /**
         * Regex equation.
         */
        public String regex;

        /**
         * Letter(s) to play off of.
         */
        public String playOff;

        /**
         * Vertical move.
         */
        public boolean vert;

        /**
         * Constructor
         * 
         * @param start   starting point on grid for regex
         * @param regex   regex equation
         * @param playOff letter(s) to play off of
         * @param vert    vertical move
         */
        public Regex(final Point start, final String regex, final String playOff, final boolean vert) {
            this.start = start;
            this.regex = regex;
            this.playOff = playOff;
            this.vert = vert;
        }
    }
}
