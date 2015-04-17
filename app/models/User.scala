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

  def authenticate(name: String, password: String) = {
    val data = Json.obj(
      "name" -> name,
      "password" -> password
    )
    //needs to be done manually because couchDB only returns cookie on post
    val authResponse: Future[ws.Response] = WS.url("http://127.0.0.1:5984/_session").
      withHeaders("Content-Type" -> "application/json").
      withHeaders("Accept" -> "application/json").post(data)

    //    val status = authResponse.map { resp =>
    //      val json = resp.json
    //      val name = json.\("name")
    ////      val roles = json.\("roles")
    ////      val cookie = resp.getAHCResponse.getCookies()
    ////      val cookieValue = cookie.get(0).getValue()
    ////      println(cookie)
    //      val statusCode = resp.getAHCResponse.getStatusCode()
    ////      val login = new loggedinUser(name.toString(), statusCode)
    ////          WS.url(url).withAuth(user, password, AuthScheme.BASIC).get()
    //    }

    val waited = Await.result(authResponse, 5 seconds)
    //    val logInName = waited.json.\("name")
    val cookie = waited.getAHCResponse.getCookies()
    val cookieValue = cookie.get(0).getValue()
    val status = waited.status //couchDB returns 200 if user is authenticated and 401 if not

    AuthInformation(status, cookieValue)
  }

  def checkCookie(authCookie: Option[String]): Boolean = {
    //TODO[SB] implementation
    true
  }



}
