package com.deadmandungeons.connect.commons;

import com.deadmandungeons.connect.commons.messenger.exceptions.IdentifierSyntaxException;
import com.deadmandungeons.connect.commons.messenger.exceptions.IdentifierSyntaxException.SyntaxError;
import com.google.common.io.BaseEncoding;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.UUID;
import java.util.regex.Pattern;

public class ConnectUtils {

    protected static final BaseEncoding BASE_64_URL_ENCODING = BaseEncoding.base64Url().omitPadding();
    protected static final Pattern UUID_NO_HYPHEN_PATTERN = Pattern.compile("(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})");

    protected ConnectUtils() {
    }

    /**
     * This encodes a UUID in base64 with padding omitted and URL safe characters ('+' to '-' and '/ 'to '_').
     * @param uuid the UUID to encode
     * @return the base64 encoded UUID
     * @see #decodeUuidBase64(String)
     */
    public static String encodeUuidBase64(UUID uuid) {
        ByteBuffer bb = ByteBuffer.wrap(new byte[16]);
        bb.putLong(uuid.getMostSignificantBits());
        bb.putLong(uuid.getLeastSignificantBits());
        return BASE_64_URL_ENCODING.encode(bb.array());
    }

    /**
     * This decodes a UUID from a base64 encoding with padding omitted and URL safe characters ('+' to '-' and '/ 'to '_').
     * @param encodedId the encoded UUID to decode
     * @return the decoded UUID or null if encodedId was null or invalid
     * @see #encodeUuidBase64(UUID)
     */
    public static UUID decodeUuidBase64(String encodedId) {
        if (encodedId != null) {
            try {
                byte[] bytes = BASE_64_URL_ENCODING.decode(encodedId);
                ByteBuffer bb = ByteBuffer.wrap(bytes);
                return new UUID(bb.getLong(), bb.getLong());
            } catch (Exception e) {
                return null;
            }
        }
        return null;
    }

    public static UUID parseUuid(String uuid) {
        if (uuid != null) {
            try {
                return UUID.fromString(uuid);
            } catch (IllegalArgumentException e) {
                return null;
            }
        }
        return null;
    }

    private static UUID parseShortenedUuid(String shortenedId) {
        return parseUuid(UUID_NO_HYPHEN_PATTERN.matcher(shortenedId).replaceAll("$1-$2-$3-$4-$5"));
    }

    public static UUID parseId(String idStr) {
        if (idStr != null) {
            // Account for encoded id's (ex: reBaGYgHQ8OoTqfamvttvA)
            if (idStr.length() == 22) {
                return decodeUuidBase64(idStr);
            }
            if (idStr.length() == 32) {
                // Account for shortened uuid's (ex: c35a67c9b797469fa893cf81b4104898)
                return parseShortenedUuid(idStr);
            }
            if (idStr.length() == 36) {
                // Account for normal uuid's (ex: c35a67c9-b797-469f-a893-cf81b4104898)
                return parseUuid(idStr);
            }
        }
        return null;
    }

    public static URL parseUrl(String url) {
        if (url != null) {
            try {
                return new URL(url);
            } catch (MalformedURLException e) {
                return null;
            }
        }
        return null;
    }

    /**
     * A useful function to check the existence of a class at runtime for compatibility checks.
     * <p>
     * This simply calls {@link Class#forName(String)} and chatches exceptions.
     * @param className the fully qualified class name to check
     * @return <code>true</code> if a class with the given className does exist in the current ClassLoader context
     */
    public static boolean checkClass(String className) {
        try {
            Class.forName(className);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    /**
     * Validates that the given identifier is not empty, between 3 to 50 characters,
     * and contains only ASCII alpha-numeric or dash characters
     * @param identifier the identifier to validate
     * @throws IdentifierSyntaxException if the given identifier has invalid syntax
     */
    public static void validateIdentifier(String identifier) throws IdentifierSyntaxException {
        if (identifier == null || identifier.isEmpty()) {
            throw new IdentifierSyntaxException(SyntaxError.EMPTY);
        }
        if (identifier.length() < 3) {
            throw new IdentifierSyntaxException(SyntaxError.MIN_LENGTH, 3);
        }
        if (identifier.length() > 50) {
            throw new IdentifierSyntaxException(SyntaxError.MAX_LENGTH, 50);
        }
        for (int i = 0; i < identifier.length(); i++) {
            char character = identifier.charAt(i);
            if (!isAsciiAlphaNumeric(character) && character != '-' && character != '_') {
                throw new IdentifierSyntaxException(SyntaxError.INVALID_CHAR, character);
            }
        }
    }

    private static boolean isAsciiAlphaNumeric(char c) {
        return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || (c >= '0' && c <= '9');
    }

}
