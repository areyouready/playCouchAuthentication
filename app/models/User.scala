package models

import com.ning.http.client.Realm.AuthScheme
import play.api.libs.json.Json
import play.api.libs.ws
import play.api.libs.ws.WS
import play.mvc.Http.Response
import play.api.libs.concurrent.Execution.Implicits._

import scala.concurrent.Future

case class User(name: String, password: String)

object Users {
  def authenticate(name: String, password: String): Int = {
    val data = Json.obj(
      "name" -> name,
      "password" -> password
    )
    //needs to be done manually because couchDB only returns cookie on post
    val authResponse: Future[ws.Response] = WS.url("http://127.0.0.1:5984/_session").
      withHeaders("Content-Type" -> "application/json").
      withHeaders("Accept" -> "application/json").post(data)
    authResponse.map { resp =>
      val json = resp.json
      val name = json.\("name")
      val roles = json.\("roles")
      val cookie = resp.getAHCResponse.getCookies()
      val cookieValue = cookie.get(0).getValue()
      println(cookie)


      //    WS.url(url).withAuth(user, password, AuthScheme.BASIC).get()
    }
    return 1
  }
}
