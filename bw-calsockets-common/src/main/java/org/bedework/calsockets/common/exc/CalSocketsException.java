/* ********************************************************************
    Licensed to Jasig under one or more contributor license
    agreements. See the NOTICE file distributed with this work
    for additional information regarding copyright ownership.
    Jasig licenses this file to you under the Apache License,
    Version 2.0 (the "License"); you may not use this file
    except in compliance with the License. You may obtain a
    copy of the License at:

    http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing,
    software distributed under the License is distributed on
    an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
    KIND, either express or implied. See the License for the
    specific language governing permissions and limitations
    under the License.
*/
package org.bedework.calsockets.common.exc;

/** Exception somewhere in the calsockets proxy
 *
 * @author Mike Douglass douglm   rpi.edu
 */
public class CalSocketsException extends Exception {
  /* Property names used as message value. These should be used to
   * retrieve a localized message and can also be used to identify the
   * cause of the exception.
   *
   * Every exception should have one of these as the getMessage()
   * value.
   */

  /* ****************** principals and ids ****************************** */

  /** principal does not exist */
  public static final String principalNotFound =
      "org.bedework.exception.principalnotfound";

  /** unknown principal type */
  public static final String unknownPrincipalType =
      "org.bedework.exception.unknownprincipaltype";

  /** bad calendar user address */
  public static final String badCalendarUserAddr =
    "org.bedework.caladdr.bad";

  /** null calendar user address */
  public static final String nullCalendarUserAddr =
      "org.bedework.caladdr.null";

  /** Used to indicate something you're not allowed to do -
   * not an access exception
   */
  public static final String forbidden = "org.bedework.exception.forbidden";

  private String extra;

  /** Constructor
   *
   */
  public CalSocketsException() {
    super();
  }

  /**
   * @param t
   */
  public CalSocketsException(final Throwable t) {
    super(t);
  }

  /**
   * @param s
   */
  public CalSocketsException(final String s) {
    super(s);
  }

  /**
   * @param s  - retrieve with getMessage(), property ame
   * @param extra String extra text
   */
  public CalSocketsException(final String s, final String extra) {
    super(s);
    this.extra = extra;
  }

  /**
   * @return String extra text
   */
  public String getExtra() {
    return extra;
  }

  /**
   * @return String message and 'extra'
   */
  @Override
  public String getMessage() {
    if (getExtra() != null) {
      return super.getMessage() + "\t" + getExtra();
    }

    return super.getMessage();
  }

  /**
   * @return String message without 'extra'
   */
  public String getDetailMessage() {
    return super.getMessage();
  }
}
