package io.github.seggan.segganbot.constants;

import java.util.regex.Pattern;

public class Patterns {
    public static final Pattern INCORRECT_SLIMEFUN_PATTERN = Pattern.compile("[Ss]lime(?:F|( [Ff]))un");
    public static final Pattern ERROR_PATTERN = Pattern.compile("(\\..+(Exception|Error): ')[\\s\\S]+(at .+(\\(.+\\.java:[1-9]+\\)))");
    public static final Pattern SPACE_PATTERN = Pattern.compile(" ");
    public static final Pattern NEWLINE_PATTERN = Pattern.compile("\\n");
}
