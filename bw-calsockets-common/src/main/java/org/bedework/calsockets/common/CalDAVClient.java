/* ********************************************************************
    Appropriate copyright notice
*/
package org.bedework.calsockets.common;

import org.bedework.calsockets.common.artifacts.ResourceCollection;
import org.bedework.calsockets.common.exc.CalSocketsException;
import org.bedework.calsockets.common.requests.SyncCollectionRequest;
import org.bedework.calsockets.common.responses.Response;
import org.bedework.calsockets.common.responses.SyncCollectionResponse;
import org.bedework.util.dav.DavUtil;
import org.bedework.util.dav.DavUtil.DavProp;
import org.bedework.util.http.BasicHttpClient;
import org.bedework.util.logging.Logged;
import org.bedework.util.misc.Util;
import org.bedework.util.xml.XmlUtil;
import org.bedework.util.xml.tagdefs.AppleServerTags;
import org.bedework.util.xml.tagdefs.CaldavTags;
import org.bedework.util.xml.tagdefs.WebdavTags;

import org.apache.http.Header;
import org.apache.http.HttpException;
import org.apache.http.message.BasicHeader;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;
import javax.xml.namespace.QName;

/**
 * User: mike Date: 3/1/18 Time: 12:15
 */
public class CalDAVClient implements Logged {
  private static final Map<String, String> icalToCalSockTypes = new HashMap<>();
  private static final Map<String, String> calSockToIcalTypes = new HashMap<>();

  static {
    compType("vevent", "event");
    compType("vtodo", "task");
    compType("vfreebusy", "freebusy");
    compType("vpoll", "poll");
    compType("vavailability", "availability");
    compType("availability", "availabile");
  }

  public static class PrincipalInfo {
    public String cn;
    public String homeUrl;
    public String inboxUrl;
    public String outboxUrl;
    public List<String> calendarUserAddresses;
  }

  private final String calDavUrl;

  private String urlPrefix;

  private String context;

  private final Principal pr;

  //private static final String caldavWellKnown = "/.well-known/caldav";
  private static final String caldavWellKnown = "/ucal/caldav";
  private BasicHttpClient cl;

  public CalDAVClient(final String calDavUrl,
                      final Principal pr) {
    this.calDavUrl = calDavUrl;
    this.pr = pr;
    processUrl();
  }

  private BasicHttpClient getCl() throws CalSocketsException {
    try {
      if (cl == null) {
        cl = new BasicHttpClient(30000);
      }

      return cl;
    } catch (final Throwable t) {
      throw new CalSocketsException(t);
    }
  }

  private final static List<QName> currentUserPrincipalQPath =
          Arrays.asList(WebdavTags.propstat,
                        WebdavTags.prop,
                        WebdavTags.currentUserPrincipal,
                        WebdavTags.href);

  private final static List<QName> collectionQPath =
          Arrays.asList(WebdavTags.propstat,
                        WebdavTags.prop,
                        WebdavTags.resourcetype,
                        WebdavTags.collection);

  private final static List<QName> calendarQPath =
          Arrays.asList(WebdavTags.propstat,
                        WebdavTags.prop,
                        WebdavTags.resourcetype,
                        CaldavTags.calendar);

  private final static List<QName> supportedCalendarComponentSetQPath =
          Arrays.asList(WebdavTags.propstat,
                        WebdavTags.prop,
                        CaldavTags.supportedCalendarComponentSet);

  private final static List<QName> displaynameQPath =
          Arrays.asList(WebdavTags.propstat,
                        WebdavTags.prop,
                        WebdavTags.displayname);

  private final static List<QName> propQPath =
          Arrays.asList(WebdavTags.propstat,
                        WebdavTags.prop);

  private final static List<QName> syncTokenQPath =
          Arrays.asList(WebdavTags.propstat,
                        WebdavTags.prop,
                        WebdavTags.syncToken);

  // Discover current principal from caldav, then load the principal data
  public String currentUserPropfind() throws CalSocketsException {
    try {
      final DavUtil du = getDav();

      final List<Element> els =
              du.propfind(getCl(), calDavUrl,
                          Collections.singletonList(
                                  WebdavTags.currentUserPrincipal),
                          DavUtil.depth0);

      if (Util.isEmpty(els) || (els.size() != 1)) {
        // Not single response element
        return null;
      }

      return getElementVal(els.get(0), currentUserPrincipalQPath);
    } catch (final CalSocketsException cfe) {
      throw cfe;
    } catch (final Throwable t) {
      throw new CalSocketsException(t);
    }
  }

  /** Load principal details for this user
   */
  public PrincipalInfo getPrincipalDetails() throws CalSocketsException {
    try {
      final String principalUrl = currentUserPropfind();

      if (principalUrl == null) {
        return null;
      }

      final DavUtil du = getDav();

      final List<Element> els =
              du.propfind(getCl(), prefix(principalUrl),
                          Arrays.asList(WebdavTags.displayname,
                                        CaldavTags.calendarHomeSet,
                                        CaldavTags.calendarUserAddressSet,
                                        CaldavTags.scheduleInboxURL,
                                        CaldavTags.scheduleOutboxURL),
                          DavUtil.depth0);

      if (Util.isEmpty(els) || (els.size() != 1)) {
        // Not single response element
        return null;
      }

      final Element prop = getElement(els.get(0), propQPath);
      if (prop == null) {
        return null;
      }

      final PrincipalInfo pi = new PrincipalInfo();

      for (final Element el: XmlUtil.getElements(prop)) {
        if (XmlUtil.nodeMatches(el, WebdavTags.displayname)) {
          pi.cn = XmlUtil.getElementContent(el);
          continue;
        }

        if (XmlUtil.nodeMatches(el, CaldavTags.calendarHomeSet)) {
          pi.homeUrl = fromHref(el);
          continue;
        }

        if (XmlUtil.nodeMatches(el, CaldavTags.scheduleInboxURL)) {
          pi.inboxUrl = fromHref(el);
          continue;
        }

        if (XmlUtil.nodeMatches(el, CaldavTags.scheduleOutboxURL)) {
          pi.outboxUrl = fromHref(el);
          continue;
        }

        if (!XmlUtil
                .nodeMatches(el, CaldavTags.calendarUserAddressSet)) {
          continue;
        }

        pi.calendarUserAddresses = fromHrefs(el);
      }

      return pi;
    } catch (final CalSocketsException cfe) {
      throw cfe;
    } catch (final Throwable t) {
      throw new CalSocketsException(t);
    }
  }

  public List<ResourceCollection> getCalendars(final Response resp,
                                               final String homeUrl) {
    try {
      final DavUtil du = getDav();

      final List<Element> els =
              du.propfind(getCl(), prefix(homeUrl),
                          Arrays.asList(WebdavTags.resourcetype,
                                        WebdavTags.displayname,
                                        WebdavTags.addMember,
                                        CaldavTags.supportedCalendarComponentSet),
                          DavUtil.depth1);

      if (Util.isEmpty(els)) {
        Response.error(resp, "No calendars available");
        return null;
      }

      final List<ResourceCollection> collections = new ArrayList<>();

      for (final Element el: els) {
        final String href = getChildVal(el, WebdavTags.href);

        if ((href == null) || (href.equals(homeUrl))) {
          continue;
        }

        final String name = getElementVal(el, displaynameQPath);

        if ((name != null) && name.startsWith(".")) {
          continue;
        }

        final boolean isCalendar;

        if (getElement(el, collectionQPath) != null) {
          isCalendar = false;
        } else if (getElement(el, calendarQPath) != null) {
          isCalendar = true;
        } else {
          continue;
        }

        final ResourceCollection rc = new ResourceCollection();

        collections.add(rc);

        rc.setName(name);
        rc.setRef(href);

        if (isCalendar) {
          final List<String> compTypes = new ArrayList<>();
          rc.setComponentTypes(compTypes);

          final Element sccs = getElement(el,
                                          supportedCalendarComponentSetQPath);
          if (sccs != null) {
            for (final Element ch : XmlUtil.getElements(sccs)) {
              if (XmlUtil.nodeMatches(ch, CaldavTags.comp)) {
                final String cType = calSockType(
                        ch.getAttribute("name"));

                if (cType != null) {
                  compTypes.add(cType);
                }
              }
            }
          }
        }
      }

      Response.ok(resp, null);
      return collections;
    } catch (final Throwable t) {
      Response.error(resp, t);
      return null;
    }
  }

  /** Called to ensure that it is ok to start a synch collection
   * against the supplied target.
   *
   * @param resp for status
   */
  public void syncOk(final SyncCollectionRequest req,
                        final SyncCollectionResponse resp) {
    try {
      final DavUtil du = getDav();

      final List<Element> els =
              du.propfind(getCl(), req.getCollectionRef(),
                          Collections.singleton(WebdavTags.syncToken),
                          DavUtil.depth0);

      if (Util.isEmpty(els) || (els.size() != 1)) {
        // Not single response element
        Response.error(resp, "Unexpected response for " + req.getCollectionRef());
        return;
      }

      if (getElementVal(els.get(0), syncTokenQPath) == null) {
        Response.error(resp, "Cannot sync with " + req.getCollectionRef());
        return;
      }

      Response.ok(resp, null);
    } catch (final Throwable t) {
      Response.error(resp, t);
    }
  }

  /** Get a list of hres to sync
   *
   * @param req for sync parameters
   * @param resp for status and new change token
   * @return list of refs or nullfor no refs
   */
  public List<String> syncCollection(final SyncCollectionRequest req,
                                     final SyncCollectionResponse resp) {
    try {
      final Collection<DavUtil.DavChild> chs =
              getDav().syncReport(getCl(), req.getCollectionRef(),
                                  req.getChangeToken(), null);

      resp.setChangeToken(req.getChangeToken());

      if (Util.isEmpty(chs)) {
        Response.ok(resp, null);
        return null;
      }

      final List<String> hrefs = new ArrayList<>();

      for (final DavUtil.DavChild ch: chs) {
        if (ch.status != HttpServletResponse.SC_OK) {
          // A deleted item. Don't think we care.
          continue;
        }

        if ((ch.propVals.size() == 1) &&
                ch.propVals.get(0).name.equals(WebdavTags.syncToken)) {
          resp.setChangeToken(
                  XmlUtil.getElementContent(ch.propVals.get(0).element));
          continue;
        }

        final DavProp dp = ch.findProp(AppleServerTags.notificationtype);

        if (dp == null) {
          continue;
        }

        hrefs.add(ch.uri);
      }

      Response.ok(resp, null);
      return hrefs;
    } catch (final Throwable t) {
      Response.error(resp, t);
      return null;
    } finally {
      if (cl != null){
        try {
          cl.release();
        } catch (final HttpException e) {
          warn(e.getLocalizedMessage());
        }
        cl.close();
      }
    }
  }

  /**
   *
   * @param resp only for status
   * @param href the resource ref
   * @return String representation
   */
  public String getString(final Response resp,
                          final String href) {
    try {
      final InputStream is = getCl().get(prefix(href),
                                         "application/calendar+json",
                                         getAuthHeaders());
      final ByteArrayOutputStream baos = new ByteArrayOutputStream();

      final int bufSize = 2048;
      final byte[] buf = new byte[bufSize];
      while (true) {
        final int len = is.read(buf, 0, bufSize);
        if (len == -1) {
          break;
        }

        baos.write(buf, 0, len);
      }

      return baos.toString("UTF-8");
    } catch (final Throwable t) {
      Response.error(resp, t);
      return null;
    }
  }

  /* ====================================================================
   *                         Private methods
   * ==================================================================== */

  private String getElementVal(final Element el,
                               final List<QName> elPath) throws Throwable {
    final Element cur = getElement(el, elPath);
    if (cur == null) {
      return null;
    }

    return XmlUtil.getElementContent(cur);
  }

  private Element getElement(final Element el,
                             final List<QName> elPath) throws Throwable {
    Element cur = el;
    for (final QName qn: elPath) {
      cur = getChild(cur, qn);
      if (cur == null) {
        return null;
      }
    }

    return cur;
  }

  private String getChildVal(final Element el,
                             final QName qn) throws Throwable {
    final Element cur = getChild(el, qn);
    if (cur == null) {
      return null;
    }

    return XmlUtil.getElementContent(cur);
  }

  private Element getChild(final Element el,
                           final QName qn) throws Throwable {
    for (final Element ch: XmlUtil.getElements(el)) {
      if (XmlUtil.nodeMatches(ch, qn)) {
        return ch;
      }
    }

    return null;
  }

  private String fromHref(final Element el) throws Throwable {
    final List<Element> els = XmlUtil.getElements(el);

    if (Util.isEmpty(els)) {
      //throw new Exception(
      //        "Bad response. Expected href found " + nd);
      return null;
    }

    final Node nd = els.get(0);

    if (!XmlUtil.nodeMatches(nd, WebdavTags.href)) {
      //throw new Exception(
      //        "Bad response. Expected href found " + nd);
      return null;
    }

    return XmlUtil.getElementContent((Element)nd);
  }

  private List<String> fromHrefs(final Element el) throws Throwable {
    final List<String> res = new ArrayList<>();

    for (final Node nd: XmlUtil.getElements(el)) {
      if (XmlUtil.nodeMatches(nd, WebdavTags.href)) {
        res.add(XmlUtil.getElementContent((Element)nd));
        return null;
      }
    }
    return res;
  }

  private List<Header> getAuthHeaders() {
    final String token = ContextListener.getSysInfo().getToken();

    if (token == null) {
      return null;
    }

    final List<Header> authheaders = new ArrayList<>(1);
    authheaders.add(new BasicHeader("X-BEDEWORK-SOCKETTKN", token));
    authheaders.add(new BasicHeader("X-BEDEWORK-SOCKETPR", pr.getName()));
    authheaders.add(new BasicHeader("X-BEDEWORK-EXTENSIONS", "true"));

    return authheaders;
  }

  private DavUtil dav;

  private DavUtil getDav() throws CalSocketsException {
    if (dav != null) {
      return dav;
    }

    try {
      dav = new DavUtil(getAuthHeaders());
      dav.addNs(CaldavTags.caldavNamespace);
      return dav;
    } catch (final Throwable t) {
      throw new CalSocketsException(t);
    }
  }

  private void processUrl() {
    try {
      final URL url = new URL(calDavUrl);

      urlPrefix = new URL(url.getProtocol(), url.getHost(),
                          url.getPort(), "/").toString();

      context = Util.buildPath(true, "/", url.getPath());
    } catch (final Throwable t) {
      throw new RuntimeException(t);
    }
  }

  private String prefix(final String val) {
    if (!val.startsWith(context)) {
      return val;
    }

    return urlPrefix + val;
  }

  private static void compType(final String icalName,
                               final String calSockName) {
    icalToCalSockTypes.put(icalName, calSockName);
    calSockToIcalTypes.put(calSockName, icalName);
  }

  private static String calSockType(final String icalType) {
    if (icalType == null) {
      return null;
    }
    return icalToCalSockTypes.get(icalType.toLowerCase());
  }

  private static String icalType(final String calSockType) {
    if (calSockType == null) {
      return null;
    }
    return calSockToIcalTypes.get(calSockType.toLowerCase());
  }
}
