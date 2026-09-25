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

import javafx.scene.control.Menu;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.control.ToggleGroup;
import java.util.Locale;

/**
 * A Menu component that allows switching the application language.
 *
 * @author Silvere Martin-Michiellot
 * @author Gemini AI (Google DeepMind)
 * @since 1.0
 */
public class LanguageMenu extends Menu {

    private final Runnable onLocaleChange;

    public LanguageMenu(Runnable onLocaleChange) {
        this.onLocaleChange = onLocaleChange;

        // Initial label
        setText(org.episteme.core.ui.i18n.I18N.getInstance().get("app.menu.language"));

        ToggleGroup langGroup = new ToggleGroup();

        // Add supported languages
        addLanguageItem("English (US)", Locale.US, langGroup);
        addLanguageItem("Fran\u00e7ais (France)", Locale.FRANCE, langGroup);
        addLanguageItem("Deutsch (Deutschland)", Locale.GERMANY, langGroup);
        addLanguageItem("Espa\u00f1ol (Espa\u00f1a)", new Locale.Builder().setLanguage("es").setRegion("ES").build(), langGroup);
        addLanguageItem("\u4e2d\u6587 (China)", Locale.CHINA, langGroup);
    }

    private void addLanguageItem(String text, Locale locale, ToggleGroup group) {
        RadioMenuItem item = new RadioMenuItem(text);
        item.setToggleGroup(group);

        // Check if this is the current language
        if (org.episteme.core.ui.i18n.I18N.getInstance().getLocale().getLanguage().equals(locale.getLanguage())) {
            item.setSelected(true);
        }

        item.setOnAction(e -> {
            if (org.episteme.core.ui.i18n.I18N.getInstance().get("app.menu.language").equals(getText())) {
                // Optimization: if no change, maybe skip? But Locale object might differ.
            }
            Locale.setDefault(locale);
            org.episteme.core.ui.i18n.I18N.getInstance().setLocale(locale);
            if (onLocaleChange != null) {
                onLocaleChange.run();
            }
        });
        getItems().add(item);
    }
}



