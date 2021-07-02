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

package fr.cnes.sonar.report.providers.securityhotspots;

import com.google.gson.JsonObject;

import fr.cnes.sonar.report.model.Comment;
import fr.cnes.sonar.report.model.SecurityHotspot;
import org.sonarqube.ws.client.WsClient;
import org.sonarqube.ws.client.hotspots.SearchRequest;
import org.sonarqube.ws.client.hotspots.ShowRequest;
import org.sonarqube.ws.Hotspots.SearchWsResponse;
import org.sonarqube.ws.Hotspots.ShowWsResponse;
import org.sonarqube.ws.Rules.ShowResponse;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Provides security hotspots items in plugin mode
 */
public class SecurityHotspotsProviderPlugin extends AbstractSecurityHotspotsProvider implements SecurityHotspotsProvider {

    /**
     * Complete constructor.
     * @param wsClient The web client.
     * @param project The id of the project to report.
     * @param branch The branch of the project to report.
     */
    public SecurityHotspotsProviderPlugin(final WsClient wsClient, final String project, final String branch) {
        super(wsClient, project, branch);
    }

    @Override
    public List<SecurityHotspot> getToReviewSecurityHotspots() {
        return getSecurityHotspotsByStatus(TO_REVIEW);
    }

    @Override
    public List<SecurityHotspot> getReviewedSecurityHotspots() {
        return getSecurityHotspotsByStatus(REVIEWED);
    }

    /**
     * Get security hotspots depending on their status
     * @param status The status of security hotspots
     * @return List containing all the security hotspots
     * @throws BadSonarQubeRequestException A request is not recognized by the server
     * @throws SonarQubeException When SonarQube server is not callable.
     */
    private List<SecurityHotspot> getSecurityHotspotsByStatus(String status) {
        // results variable
        final List<SecurityHotspot> res = new ArrayList<>();
        // stop condition
        boolean goOn = true;
        // current page
        int page = 1;
        // get maximum number of results per page
        final int maxPerPage = Integer.parseInt(getRequest(MAX_PER_PAGE_SONARQUBE));

        // search all security hotspots of the project
        while(goOn) {
            // prepare the request to get all the security hotspots
            final String p = String.valueOf(page);
            final String ps = String.valueOf(maxPerPage);
            final SearchRequest searchRequest = new SearchRequest()
                                                    .setBranch(getBranch())
                                                    .setP(p)
                                                    .setProjectKey(getProjectKey())
                                                    .setPs(ps)
                                                    .setStatus(status);
            // perform the request to the server
            final SearchWsResponse searchWsResponse = getWsClient().hotspots().search(searchRequest);
            // transform response to JsonObject
            final JsonObject searchHotspotsResult = responseToJsonObject(searchWsResponse);
            // transform json to SecurityHotspot[]
            SecurityHotspot[] securityHotspotTemp = getGson().fromJson(searchHotspotsResult.get(HOTSPOTS),
                    SecurityHotspot[].class);
            // perform requests to get more information about each security hotspot
            for (SecurityHotspot securityHotspot : securityHotspotTemp) {
                final ShowRequest showHotspotRequest = new ShowRequest().setHotspot(securityHotspot.getKey());
                final ShowWsResponse showHotspotResponse = getWsClient().hotspots().show(showHotspotRequest);
                final JsonObject showHotspotsResult = responseToJsonObject(showHotspotResponse);
                JsonObject rule = showHotspotsResult.get(RULE).getAsJsonObject();
                String key = rule.get(KEY).getAsString();
                Comment[] comments = getGson().fromJson(showHotspotsResult.get(COMMENTS), Comment[].class);
                securityHotspot.setRule(key);
                securityHotspot.setComments(comments);
                if(status.equals(REVIEWED)) {
                    String resolution = showHotspotsResult.get(RESOLUTION).getAsString();
                    securityHotspot.setResolution(resolution);
                }

                final org.sonarqube.ws.client.rules.ShowRequest showRuleRequest =
                    new org.sonarqube.ws.client.rules.ShowRequest().setKey(securityHotspot.getRule());
                final ShowResponse showRuleResponse = getWsClient().rules().show(showRuleRequest);
                final JsonObject showRuleResult = responseToJsonObject(showRuleResponse);
                JsonObject ruleContent = showRuleResult.get(RULE).getAsJsonObject();
                String severity = ruleContent.get(SEVERITY).getAsString();
                String language = ruleContent.get(LANGUAGE).getAsString();
                securityHotspot.setSeverity(severity);
                securityHotspot.setLanguage(language);
            }
            // add security hotspots to the final result
            res.addAll(Arrays.asList(securityHotspotTemp));
            // get total number of items
            JsonObject paging = searchHotspotsResult.get(PAGING).getAsJsonObject();
            int number = paging.get(TOTAL).getAsInt();
            // update stop condition and increment current page
            goOn = page*maxPerPage < number;
            page++;
        }

        // return the security hotspots
        return res;
    }
}