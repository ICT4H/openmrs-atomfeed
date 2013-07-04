package org.openmrs.module.atomfeed.web.controller;

import org.apache.log4j.Logger;
import org.ict4h.atomfeed.server.domain.chunking.number.NumberChunkingHistory;
import org.ict4h.atomfeed.server.repository.jdbc.AllEventRecordsJdbcImpl;
import org.ict4h.atomfeed.server.repository.jdbc.ChunkingEntriesJdbcImpl;
import org.ict4h.atomfeed.server.service.EventFeedService;
import org.ict4h.atomfeed.server.service.EventFeedServiceImpl;
import org.ict4h.atomfeed.server.service.feedgenerator.FeedGeneratorFactory;
import org.ict4h.atomfeed.server.service.feedgenerator.NumberFeedGenerator;
import org.ict4h.atomfeed.server.service.helper.EventFeedServiceHelper;
import org.ict4h.atomfeed.server.service.helper.ResourceHelper;
import org.openmrs.module.atomfeed.repository.hibernate.OpenMRSConnectionProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;

@Controller
@RequestMapping(value = "/atomfeed")
public class AtomFeedController {
    private static Logger logger = Logger.getLogger(AtomFeedController.class);
    private EventFeedService eventFeedService;

    public AtomFeedController() {
        //close your eyes
        this(new EventFeedServiceImpl(new FeedGeneratorFactory().getFeedGenerator(new AllEventRecordsJdbcImpl(new OpenMRSConnectionProvider()), new ChunkingEntriesJdbcImpl(new OpenMRSConnectionProvider()), new ResourceHelper())));
    }

    public AtomFeedController(EventFeedService eventFeedService) {
        this.eventFeedService = eventFeedService;
    }

    @RequestMapping(method = RequestMethod.GET, value = "/{category}/recent")
    @ResponseBody
    public String getRecentEventFeedForCategory(HttpServletRequest httpServletRequest, @PathVariable String category) {
        return EventFeedServiceHelper.getRecentFeed(eventFeedService, httpServletRequest.getRequestURL().toString(),
                category, logger);
    }

    @RequestMapping(method = RequestMethod.GET, value = "/{category}/{n}")
    @ResponseBody
    public String getEventFeedWithCategory(HttpServletRequest httpServletRequest,
                                           @PathVariable String category, @PathVariable int n) {
        return EventFeedServiceHelper.getEventFeed(eventFeedService, httpServletRequest.getRequestURL().toString(),
                category, n, logger);
    }
}