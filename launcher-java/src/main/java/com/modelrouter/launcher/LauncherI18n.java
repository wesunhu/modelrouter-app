/**
 * Loads ResourceBundle messages for zh/ja/en; input: locale tag; output: localized strings.
 *
 * @version 1.0.1
 * @since 2026-03-21
 * @author wesun hu
 */

package com.modelrouter.launcher;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

final class LauncherI18n {

    private final ResourceBundle bundle;
    private final String tag;

    private LauncherI18n(String tag, ResourceBundle bundle) {
        this.tag = tag;
        this.bundle = bundle;
    }

    static LauncherI18n fromTag(String tag) {
        String t = tag == null || tag.isBlank() ? detectDefaultTag() : tag.trim().toLowerCase(Locale.ROOT);
        if (!t.equals("zh") && !t.equals("ja") && !t.equals("en")) {
            t = detectDefaultTag();
        }
        Locale locale = localeForTag(t);
        ResourceBundle b = ResourceBundle.getBundle("i18n.messages", locale);
        return new LauncherI18n(t, b);
    }

    private static String detectDefaultTag() {
        String lang = Locale.getDefault().getLanguage();
        if ("zh".equals(lang)) {
            return "zh";
        }
        if ("ja".equals(lang)) {
            return "ja";
        }
        return "en";
    }

    private static Locale localeForTag(String t) {
        return switch (t) {
            case "zh" -> Locale.SIMPLIFIED_CHINESE;
            case "ja" -> Locale.JAPAN;
            default -> Locale.ENGLISH;
        };
    }

    String tag() {
        return tag;
    }

    String get(String key) {
        try {
            return bundle.getString(key);
        } catch (MissingResourceException e) {
            return key;
        }
    }

    String format(String key, Object... args) {
        return MessageFormat.format(get(key), args);
    }
}
