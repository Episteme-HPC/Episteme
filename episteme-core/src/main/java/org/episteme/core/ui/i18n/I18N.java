/*
 * Episteme - Java(TM) Tools and Libraries for the Advancement of Sciences.
 * Copyright (C) 2025-2026 - Silvere Martin-Michiellot and Gemini AI (Google DeepMind)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package org.episteme.core.ui.i18n;

import java.util.*;
import java.util.function.Consumer;
import org.episteme.core.ui.ThemeManager;

/**
 * Internationalization helper for Episteme UI.
 * <p>
 * Manages multiple resource bundles and provides centralized access to
 * localized strings. Supports dynamic locale switching.
 * </p>
 *
 * @author Silvere Martin-Michiellot
 * @author Gemini AI (Google DeepMind)
 * @since 1.0
 */
public class I18N {
    private static final String CORE_BUNDLE_BASE = "org.episteme.core.ui.i18n.messages_core";
    private static I18N instance;

    private Locale currentLocale = Locale.getDefault();
    private final List<String> bundleBases = new ArrayList<>();
    private final Map<String, ResourceBundle> bundles = new HashMap<>();
    private final List<Consumer<Locale>> listeners = new ArrayList<>();

    private I18N() {
        // Auto-register all known bundles
        addBundle(CORE_BUNDLE_BASE);
        // Try to load additional bundles (they may not be on classpath depending on module)
        tryAddBundle("org.episteme.natural.ui.i18n.messages_natural");
        tryAddBundle("org.episteme.social.ui.i18n.messages_social");
        tryAddBundle("org.episteme.client.ui.i18n.messages_client");
        tryAddBundle("org.episteme.server.ui.i18n.messages_server");
        tryAddBundle("org.episteme.apps.ui.i18n.messages_apps");

    }

    private void tryAddBundle(String bundleBase) {
        try {
            ResourceBundle.getBundle(bundleBase, currentLocale, new Utf8Control());
            addBundle(bundleBase);
        } catch (MissingResourceException e) {
            // Bundle not found on classpath
        }
    }

    public static synchronized I18N getInstance() {
        if (instance == null) {
            instance = new I18N();
        }
        return instance;
    }

    /**
     * Registers an additional resource bundle base name.
     * 
     * @param bundleBase the bundle base name (e.g.,
     *                   "org.episteme.core.ui.i18n.messages_natural")
     */
    public void addBundle(String bundleBase) {
        if (!bundleBases.contains(bundleBase)) {
            bundleBases.add(bundleBase);
            loadBundle(bundleBase);
        }
    }

    /**
     * Sets the current locale and reloads all bundles.
     * 
     * @param locale the new locale
     */
    public void setLocale(Locale locale) {
        this.currentLocale = locale;
        bundles.clear();
        for (String base : bundleBases) {
            loadBundle(base);
        }
        ThemeManager.getInstance().notifyLocaleChange(locale);
        notifyListeners(locale);
    }

    public void addListener(Consumer<Locale> listener) {
        listeners.add(listener);
    }

    public void removeListener(Consumer<Locale> listener) {
        listeners.remove(listener);
    }

    private void notifyListeners(Locale locale) {
        for (Consumer<Locale> listener : listeners) {
            try {
                listener.accept(locale);
            } catch (Exception e) {
                System.err.println("Error in locale listener: " + e.getMessage());
            }
        }
    }

    /**
     * Gets the current locale.
     * 
     * @return current locale
     */
    public Locale[] getSupportedLocales() {
        return new Locale[] {
                Locale.ENGLISH,
                Locale.FRENCH,
                Locale.GERMAN,
                Locale.of("es"),
                Locale.CHINESE
        };
    }

    public Locale getLocale() {
        return currentLocale;
    }

    /**
     * Toggles between English and French.
     */
    public void toggleLanguage() {
        if (currentLocale.getLanguage().equals(Locale.ENGLISH.getLanguage())) {
            setLocale(Locale.FRENCH);
        } else {
            setLocale(Locale.ENGLISH);
        }
    }

    /**
     * Retrieves a localized string for the given key.
     * Searches all registered bundles in order of registration.
     * 
     * @param key the resource key
     * @return the localized string, or the key itself if not found
     */
    public String get(String key) {
        for (String base : bundleBases) {
            ResourceBundle bundle = bundles.get(base);
            if (bundle != null) {
                try {
                    return bundle.getString(key);
                } catch (MissingResourceException e) {
                    // Try next bundle
                }
            }
        }
        // Return key as fallback
        return markIfTest(key);
    }

    /**
     * Retrieves a localized string for the given key, returning the default value
     * if not found.
     * 
     * @param key          the resource key
     * @param defaultValue the value to return if key is missing
     * @return the localized string or defaultValue
     */
    public String get(String key, String defaultValue) {
        String value = get(key);
        // get(key) returns key if missing, so we check if it returned the key
        if (value.equals(key) && !hasKey(key)) {
            return defaultValue;
        }
        return value;
    }

    public boolean hasKey(String key) {
        for (String base : bundleBases) {
            ResourceBundle bundle = bundles.get(base);
            if (bundle != null && bundle.containsKey(key)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Retrieves a localized string with format arguments.
     * 
     * @param key  the resource key
     * @param args format arguments
     * @return the formatted localized string
     */
    public String get(String key, Object... args) {
        String val = get(key);
        if (args.length > 0 && val != null) {
            try {
                // Detect MessageFormat style {0}
                if (val.contains("{0}")) {
                    return java.text.MessageFormat.format(val, args);
                }
                // Default to String.format
                return String.format(val, args);
            } catch (Exception e) {
                // Fallback attempt with MessageFormat if String.format failed
                try {
                    return java.text.MessageFormat.format(val, args);
                } catch (Exception e2) {
                    return val + Arrays.toString(args);
                }
            }
        }
        return val;
    }

    private static boolean testMode = false;

    public static void setTestMode(boolean enabled) {
        testMode = enabled;
    }

    private void loadBundle(String bundleBase) {
        try {
            ResourceBundle bundle = ResourceBundle.getBundle(bundleBase, currentLocale, new Utf8Control());
            bundles.put(bundleBase, bundle);
        } catch (MissingResourceException e) {
            if ("zh".equals(currentLocale.getLanguage())) {
                try {
                    ResourceBundle bundle = ResourceBundle.getBundle(bundleBase, Locale.CHINA, new Utf8Control());
                    bundles.put(bundleBase, bundle);
                    return;
                } catch (MissingResourceException ignored) {}
            }
        }
    }

    // Add logic to marked missing keys
    private String markIfTest(String key) {
        if (testMode) {
            return "!!!" + key + "!!!";
        }
        return key;
    }

    /**
     * Custom ResourceBundle.Control to enforce UTF-8 encoding.
     */
    public static class Utf8Control extends ResourceBundle.Control {
        @Override
        public ResourceBundle newBundle(String baseName, Locale locale, String format, ClassLoader loader,
                boolean reload)
                throws IllegalAccessException, InstantiationException, java.io.IOException {
            String bundleName = toBundleName(baseName, locale);
            String resourceName = toResourceName(bundleName, "properties");
            ResourceBundle bundle = null;
            java.io.InputStream stream = null;
            if (reload) {
                java.net.URL url = loader.getResource(resourceName);
                if (url != null) {
                    java.net.URLConnection connection = url.openConnection();
                    if (connection != null) {
                        connection.setUseCaches(false);
                        stream = connection.getInputStream();
                    }
                }
            } else {
                stream = loader.getResourceAsStream(resourceName);
            }
            if (stream != null) {
                try (java.io.InputStreamReader reader = new java.io.InputStreamReader(stream,
                        java.nio.charset.StandardCharsets.UTF_8)) {
                    bundle = new PropertyResourceBundle(reader);
                }
            }
            return bundle;
        }
    }
}

