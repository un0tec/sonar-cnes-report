/*
 * This file is part of cnesreport.
 *
 * cnesreport is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * cnesreport is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with cnesreport.  If not, see <http://www.gnu.org/licenses/>.
 */

package fr.cnes.sonar.report.model;

import java.util.Map;
import java.util.HashMap;

/**
 * Map of all languages of sonarqube
 */
public class Languages {
    /** Languages map */
    private  Map<String, Language> languages;

    /**
     * Default constructor
     */
    public Languages() {
        this.languages = new HashMap<>();
    }

    /**
     * Languages map
     * @return a map of couples language's key/language
     */
    public Map<String, Language> getLanguages() {
        return languages;
    }

    /**
     * Set languages
     * @param languages
     */
    public void setLanguages(Map<String, Language> languages) {
        this.languages = languages;
    }

    /**
     * Get the language corresponding to the given key
     * @param languageKey the key of the language
     * @return Language's name
     */
    public String getLanguage(final String languageKey) {
        String languageName = "?";
        if(!this.languages.isEmpty() && this.languages.get(languageKey) != null) {
            languageName = this.languages.get(languageKey).getName();
        }
        return languageName;
    }
}