package models

import com.ning.http.client.Realm.AuthScheme
import play.api.libs.json.Json
import play.api.libs.ws
import play.api.libs.ws.WS
import play.api.libs.ws.{WS, Response}
import play.api.mvc.{Result, RequestHeader}
import scala.concurrent.duration._
import play.api.libs.concurrent.Execution.Implicits._

import scala.concurrent.{Await, Future}

case class User(name: String, password: String)

object Users {

  case class AuthInformation(statusCode: Int, authCookie: String)

  /**
   * Authenticates the given username and pwd against couchDB.
   * @param name the username to authenticate
   * @param password the pwd to authenticate
   * @return Object of Authinformation containing the authCookier and statusCode
   */
  def authenticate(name: String, password: String) = {
    val data = Json.obj(
      "name" -> name,
      "password" -> password
    )
    val url = "http://" + name + ":" + password + "@" + "127.0.0.1:5984/_session";
    //needs to be done manually because couchDB only returns cookie on post
    val authResponse: Future[ws.Response] = WS.url(url).withHeaders("Accept" -> "application/json").
      withAuth(name, password, AuthScheme.BASIC).post(data) //basic login needed when require_valid_user = true in couch config

    val waited = Await.result(authResponse, 5 seconds)
    val cookie = waited.getAHCResponse.getCookies()
    val body = waited.getAHCResponse.getStatusText()
    println(cookie)
    val cookieValue = cookie.get(0).getValue()
    val status = waited.status //couchDB returns 200 if user is authenticated and 401 if not

    AuthInformation(status, cookieValue)
  }

  /**
   * Checks for legal authCookie in couchDB session.
   * @param authCookie the authCookie given from the user
   * @return <code>true</code> if the authCookie is affirmated by couchDB
   *         <code>false</code> if couchDB tells the cookie is invalid
   */
  def checkCookie(authCookie: Option[String]): Boolean = {
    val authResponse: Future[ws.Response] = WS.url("http://127.0.0.1:5984/_session").
      withHeaders("Cookie" -> ("AuthSession="+authCookie.get)).get()
    val waited = Await.result(authResponse, 5 seconds)
    println(authCookie.get)
    println(waited.status)
    waited.status match {
      case 200 => return true
      case _ => return false
    }
  }



}
