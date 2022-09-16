package org.variantsync.diffdetective.diff.difftree;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The type of nodes in a {@link DiffTree}.
 * Corresponds to the tau function from our paper.
 */
public enum CodeType {
    // Mapping types
    IF("if"),
    ENDIF("endif"),
    ELSE("else"),
    ELIF("elif"),

    // Code types
    CODE("code"),

    // Extra type for the root
    ROOT("ROOT");

    public final String name;
    CodeType(String name) {
        this.name = name;
    }

    /**
     * Returns true iff this code type represents a conditional feature annotation (i.e., if or elif).
     */
    public boolean isConditionalMacro() {
        return this == IF || this == ELIF;
    }

    /**
     * Returns true iff this code type represents a feature mapping.
     */
    public boolean isMacro() {
        return this != ROOT && this != CODE;
    }

    final static Pattern annotationRegex = Pattern.compile("^[+-]?\\s*#\\s*(if|endif|else|elif)");

    /**
     * Parses the code type from a line taken from a text-based diff.
     * @param line A line in a patch.
     * @return The type of edit of <code>line</code>.
     */
    public static CodeType ofDiffLine(String line) {
        Matcher matcher = annotationRegex.matcher(line);
        if (matcher.find()) {
            String id = matcher.group(1);
            if (id.equals(IF.name)) {
                return IF;
            } else if (id.equals(ENDIF.name)) {
                return ENDIF;
            } else if (id.equals(ELSE.name)) {
                return ELSE;
            } else if (id.equals(ELIF.name)) {
                return ELIF;
            }
        }

        return CODE;
    }

    /**
     * Creates a CodeType from its value names.
     * @see Enum#name()
     * @param name a string that equals the name of one value of this enum (ignoring case)
     * @return The CodeType that has the given name
     */
    public static CodeType fromName(final String name) {
        for (CodeType candidate : values()) {
            if (candidate.toString().equalsIgnoreCase(name)) {
                return candidate;
            }
        }

        throw new IllegalArgumentException("Given string \"" + name + "\" is not the name of a CodeType.");
    }

    /**
     * Prints this value as a macro annotation (i.e., starting with #).
     */
    public String asMacroText() {
        return "#" + this.name;
    }
}
