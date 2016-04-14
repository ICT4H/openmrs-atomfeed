package org.openmrs.module.atomfeed.utils;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.openmrs.api.context.Context;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.net.URI;
import java.net.URISyntaxException;

@Component
public class UrlUtil {
    private static Logger logger = Logger.getLogger(UrlUtil.class);

    public String getRequestURL(HttpServletRequest request) {
        String requestUrl = getServiceUriFromRequest(request);
        if (requestUrl == null) {
            requestUrl = getBaseUrlFromOpenMrsGlobalProperties(request);
        }
        return requestUrl != null ? requestUrl : formUrl(request.getScheme(), request.getServerName(), request.getServerPort(), request.getRequestURI(), request.getQueryString());
    }

    private String getBaseUrlFromOpenMrsGlobalProperties(HttpServletRequest request) {
        String restUri = Context.getAdministrationService().getGlobalProperty("webservices.rest.uriPrefix");
        if (StringUtils.isNotBlank(restUri)) {
            try {
                URI uri = new URI(restUri);
                return formUrl(uri.getScheme(), uri.getHost(), uri.getPort(), request.getRequestURI(), request.getQueryString());
            } catch (URISyntaxException e) {
                logger.warn("Invalid url is set in global property webservices.rest.uriPrefix");
            }
        }
        return null;
    }

    private String getServiceUriFromRequest(HttpServletRequest request) {
        String scheme = request.getHeader("X-Forwarded-Proto");
        if (scheme == null) {
            return null;
        }
        return formUrl(scheme, request.getServerName(), request.getServerPort(), request.getRequestURI(), request.getQueryString());
    }

    private String formUrl(String scheme, String hostname, int port, String path, String queryString) {
        String url = null;
        if (port != 80 && port != 443 && port != -1) {
            url = scheme + "://" + hostname + ":" + port + path;
        } else {
            url = scheme + "://" + hostname + path;
        }
        if (queryString != null) {
            return url + "?" + queryString;
        }
        return url;
    }
}
