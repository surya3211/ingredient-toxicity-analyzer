import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class IngredientAnalyzer {

    private static final String IMAGE_PATH = "D:\\Java Projects\\app2\\ingredient_label.png";
    private static final String TESS_INSTALL_PATH = "C:\\Program Files\\Tesseract-OCR";
    // ----------------------------------------------------------------

    // ANSI colors
    private static final String RESET = "\u001B[0m";
    private static final String RED = "\u001B[31m";
    private static final String YELLOW = "\u001B[33m";
    private static final String GREEN = "\u001B[32m";
    private static final String CYAN = "\u001B[36m";

    // Run Tesseract CLI and return stdout
    public static String extractWithCli(File imageFile) {
        try {
            String exe = TESS_INSTALL_PATH + File.separator + "tesseract.exe";
            ProcessBuilder pb = new ProcessBuilder(exe, imageFile.getAbsolutePath(), "stdout", "-l", "eng");
            pb.redirectErrorStream(true);
            Process p = pb.start();
            InputStream is = p.getInputStream();
            Scanner s = new Scanner(is).useDelimiter("\\A");
            String out = s.hasNext() ? s.next() : "";
            p.waitFor();
            s.close();
            return out;
        } catch (Exception e) {
            return null;
        }
    }

    // Normalize OCR text
    public static String normalizeOcr(String text) {
        if (text == null) return "";
        text = text.replaceAll("-\\s*\\r?\\n\\s*", ""); 
        text = text.replaceAll("\\r?\\n", " ");        
        text = text.replaceAll("\\s{2,}", " ").trim();
        return text.toLowerCase();
    }

    // Patterns and canonical labels
    public static LinkedHashMap<Pattern, String> buildPatterns() {
        LinkedHashMap<Pattern, String> patterns = new LinkedHashMap<>();
        patterns.put(Pattern.compile("\\bsodium laureth sulfate\\b", Pattern.CASE_INSENSITIVE), "Sodium laureth sulfate");
        patterns.put(Pattern.compile("\\bsodium lauryl sulfate\\b", Pattern.CASE_INSENSITIVE), "Sodium lauryl sulfate");
        patterns.put(Pattern.compile("\\b(sles|sls)\\b", Pattern.CASE_INSENSITIVE), "SLS / SLES");
        patterns.put(Pattern.compile("\\bmethylisothiazolinone\\b", Pattern.CASE_INSENSITIVE), "Methylisothiazolinone (MI)");
        patterns.put(Pattern.compile("\\bmethylchloroisothiazolinone\\b", Pattern.CASE_INSENSITIVE), "Methylchloroisothiazolinone (MCI)");
        patterns.put(Pattern.compile("\\bparabens?\\b", Pattern.CASE_INSENSITIVE), "Parabens");
        patterns.put(Pattern.compile("\\bphthalates?\\b", Pattern.CASE_INSENSITIVE), "Phthalates");
        patterns.put(Pattern.compile("\\bformaldehyde\\b", Pattern.CASE_INSENSITIVE), "Formaldehyde");
        patterns.put(Pattern.compile("\\bbisphenol\\s*a\\b", Pattern.CASE_INSENSITIVE), "Bisphenol A (BPA)");
        patterns.put(Pattern.compile("\\btriclosan\\b", Pattern.CASE_INSENSITIVE), "Triclosan");
        patterns.put(Pattern.compile("\\bbenzalkonium chloride\\b", Pattern.CASE_INSENSITIVE), "Benzalkonium chloride");
        patterns.put(Pattern.compile("\\bphenoxyethanol\\b", Pattern.CASE_INSENSITIVE), "Phenoxyethanol");
        patterns.put(Pattern.compile("\\baluminum\\b", Pattern.CASE_INSENSITIVE), "Aluminum compounds");
        patterns.put(Pattern.compile("\\bmercury\\b", Pattern.CASE_INSENSITIVE), "Mercury");
        patterns.put(Pattern.compile("\\barsenic\\b", Pattern.CASE_INSENSITIVE), "Arsenic");
        patterns.put(Pattern.compile("\\bcadmium\\b", Pattern.CASE_INSENSITIVE), "Cadmium");
        patterns.put(Pattern.compile("\\bhydroquinone\\b", Pattern.CASE_INSENSITIVE), "Hydroquinone");
        patterns.put(Pattern.compile("\\boxybenzone\\b", Pattern.CASE_INSENSITIVE), "Oxybenzone");
        patterns.put(Pattern.compile("\\bmineral oil\\b", Pattern.CASE_INSENSITIVE), "Mineral oil");
        return patterns;
    }

    // Map canonical label -> severity (0..10)
    public static Map<String, Integer> severityMap() {
        Map<String, Integer> m = new LinkedHashMap<>();
        m.put("Sodium laureth sulfate", 6);
        m.put("Sodium lauryl sulfate", 7);
        m.put("SLS / SLES", 7);
        m.put("Methylisothiazolinone (MI)", 9);
        m.put("Methylchloroisothiazolinone (MCI)", 9);
        m.put("Parabens", 6);
        m.put("Phthalates", 8);
        m.put("Formaldehyde", 9);
        m.put("Bisphenol A (BPA)", 9);
        m.put("Triclosan", 7);
        m.put("Benzalkonium chloride", 7);
        m.put("Phenoxyethanol", 6);
        m.put("Aluminum compounds", 6);
        m.put("Mercury", 10);
        m.put("Arsenic", 10);
        m.put("Cadmium", 10);
        m.put("Hydroquinone", 8);
        m.put("Oxybenzone", 7);
        m.put("Mineral oil", 5);
        return m;
    }

    // Detect matches -> label -> severity
    public static LinkedHashMap<String, Integer> detectWithSeverity(String normalizedText) {
        LinkedHashMap<String, Integer> found = new LinkedHashMap<>();
        LinkedHashMap<Pattern, String> patterns = buildPatterns();
        Map<String, Integer> sev = severityMap();

        for (Map.Entry<Pattern, String> e : patterns.entrySet()) {
            Matcher m = e.getKey().matcher(normalizedText);
            if (m.find()) {
                String label = e.getValue();
                int score = sev.getOrDefault(label, 5);
                found.put(label, score);
            }
        }
        return found;
    }

    // Print a horizontal ASCII meter for a 0..10 score
    public static void printMeter(int score, int width) {
        int filled = Math.round((score / 10f) * width);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < filled; i++) sb.append('█');
        for (int i = filled; i < width; i++) sb.append('·');
        System.out.print(sb.toString());
    }

    // Determine verdict text and color based on average score
    public static String[] verdictForAverage(double avg) {
        if (avg <= 3.0) {
            return new String[]{ "SAFE", GREEN, "Low risk — generally safe for use." };
        } else if (avg <= 6.0) {
            return new String[]{ "CAUTION", YELLOW, "Moderate risk — avoid if you have sensitive skin; better as rinse-off." };
        } else {
            return new String[]{ "AVOID", RED, "High risk — avoid use, especially as leave-on product or on sensitive skin." };
        }
    }

    // Print results: per-ingredient colored meters, overall meter, verdict
    public static void printResults(LinkedHashMap<String, Integer> found) {
        if (found.isEmpty()) {
            System.out.println("No toxic ingredients found");
            return;
        }

        System.out.println("Toxic ingredients (severity meter 0-10):");
        int width = 30;
        double sum = 0;
        for (Map.Entry<String, Integer> e : found.entrySet()) {
            String label = e.getKey();
            int score = e.getValue();
            sum += score;

            // Print label in cyan and score
            System.out.printf(CYAN + "%-35s " + RESET + "%2d/10  ", label, score);

            // Choose color for bar
            String color = score >= 8 ? RED : score >= 5 ? YELLOW : GREEN;
            System.out.print(color);
            printMeter(score, width);
            System.out.print(RESET);
            System.out.println();
        }

        double avg = sum / found.size();
        System.out.println();
        System.out.printf("Overall toxicity score: %.2f / 10\n", avg);

        // Overall meter colored by avg thresholds
        System.out.print("Overall meter: ");
        String overallColor = avg > 6.0 ? RED : avg > 3.0 ? YELLOW : GREEN;
        System.out.print(overallColor);
        printMeter((int)Math.round(avg), 50);
        System.out.print(RESET);
        System.out.println();

        // Verdict
        String[] verdict = verdictForAverage(avg); // [label, color, message]
        System.out.println();
        System.out.print("Verdict: ");
        System.out.print(verdict[1]); // color
        System.out.print(verdict[0]);
        System.out.print(RESET);
        System.out.println(" - " + verdict[2]);
    }

    public static void main(String[] args) {
    
        PrintStream originalErr = System.err;
        System.setErr(new PrintStream(OutputStream.nullOutputStream()));

        try {
            File imageFile = new File(IMAGE_PATH);
            if (!imageFile.exists() || !imageFile.canRead()) {
                System.out.println("No toxic ingredients found");
                return;
            }

            String raw = extractWithCli(imageFile);
            if (raw == null || raw.trim().isEmpty()) {
                System.out.println("No toxic ingredients found");
                return;
            }

            String normalized = normalizeOcr(raw);
            LinkedHashMap<String, Integer> found = detectWithSeverity(normalized);

            printResults(found);

        } finally {
            // Restore stderr
            System.setErr(originalErr);
        }
    }
}
