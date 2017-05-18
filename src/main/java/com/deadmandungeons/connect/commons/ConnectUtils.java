package com.deadmandungeons.connect.commons;

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

}
