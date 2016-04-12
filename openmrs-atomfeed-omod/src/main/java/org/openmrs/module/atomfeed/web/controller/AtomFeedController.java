package org.openmrs.module.atomfeed.web.controller;

import org.apache.log4j.Logger;
import org.ict4h.atomfeed.server.repository.jdbc.AllEventRecordsJdbcImpl;
import org.ict4h.atomfeed.server.repository.jdbc.AllEventRecordsOffsetMarkersJdbcImpl;
import org.ict4h.atomfeed.server.repository.jdbc.ChunkingEntriesJdbcImpl;
import org.ict4h.atomfeed.server.service.EventFeedService;
import org.ict4h.atomfeed.server.service.EventFeedServiceImpl;
import org.ict4h.atomfeed.server.service.feedgenerator.FeedGeneratorFactory;
import org.ict4h.atomfeed.server.service.helper.EventFeedServiceHelper;
import org.ict4h.atomfeed.server.service.helper.ResourceHelper;
import org.openmrs.api.context.Context;
import org.openmrs.module.atomfeed.transaction.support.AtomFeedSpringTransactionManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;

@Controller
@RequestMapping(value = "/atomfeed")
public class AtomFeedController {
    private static Logger logger = Logger.getLogger(AtomFeedController.class);
    private AtomFeedSpringTransactionManager atomTxManager;
    private EventFeedService eventFeedService;

    @Autowired
    public AtomFeedController(PlatformTransactionManager transactionManager) {
        atomTxManager = new AtomFeedSpringTransactionManager(transactionManager);
        this.eventFeedService = new EventFeedServiceImpl(new FeedGeneratorFactory().getFeedGenerator(
                new AllEventRecordsJdbcImpl(atomTxManager),
                new AllEventRecordsOffsetMarkersJdbcImpl(atomTxManager),
                new ChunkingEntriesJdbcImpl(atomTxManager),
                new ResourceHelper()));
    }

    public AtomFeedController(EventFeedService eventFeedService) {
        this.eventFeedService = eventFeedService;
    }

    @RequestMapping(method = RequestMethod.GET, value = "/{category}/recent")
    @ResponseBody
    public String getRecentEventFeedForCategory(HttpServletRequest httpServletRequest, @PathVariable String category) {
        return EventFeedServiceHelper.getRecentFeed(eventFeedService, getRequestURL(httpServletRequest),
                category, logger, atomTxManager);
    }

    @RequestMapping(method = RequestMethod.GET, value = "/{category}/{n}")
    @ResponseBody
    public String getEventFeedWithCategory(HttpServletRequest httpServletRequest,
                                           @PathVariable String category, @PathVariable int n) {
        return EventFeedServiceHelper.getEventFeed(eventFeedService, getRequestURL(httpServletRequest),
                category, n, logger, atomTxManager);
    }

    private String getRequestURL(HttpServletRequest request) {
        String requestUrl = getServiceUriFromRequest(request);
        if (requestUrl == null) {
            requestUrl = getBaseUrlFromOpenMrsGlobalProperties(request);
        }
        return requestUrl != null ? requestUrl : request.getRequestURL().toString();
    }

    private String getBaseUrlFromOpenMrsGlobalProperties(HttpServletRequest request) {
        String restUri = Context.getAdministrationService().getGlobalProperty("webservices.rest.uriPrefix");
        if (restUri != null)
            return addPathToUrl(restUri, request.getRequestURI(), request.getQueryString());
        return null;
    }

    private String getServiceUriFromRequest(HttpServletRequest request) {
        String scheme = request.getHeader("X-Forwarded-Proto");
        if (scheme == null) {
            return null;
        }
        String hostname = request.getServerName();
        int port = request.getServerPort();
        String baseUrl = null;
        if (port != 80 && port != 443 && port != -1) {
            baseUrl = scheme + "://" + hostname + ":" + port;
        } else {
            baseUrl = scheme + "://" + hostname;
        }
        return addPathToUrl(baseUrl, request.getRequestURI(), request.getQueryString());
    }

    private String addPathToUrl(String baseUrl, String path, String queryString) {
        String url = baseUrl + path;
        if (queryString != null) {
            return url + "?" + queryString;
        }
        return url;
    }
}