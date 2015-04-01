package controllers

import models.Users.loggedinUser
import play.api._
import play.api.libs.ws.Response
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import play.api.libs.concurrent.Execution.Implicits._

import views._
import models._

import scala.concurrent
import scala.concurrent.Future
import scala.parallel.Future
import scala.util.{Failure, Success}

object Application extends Controller {

  lazy val loginForm = Form(
    tuple(
      "name" -> nonEmptyText,
      "password" -> nonEmptyText))

  def login = Action { implicit request =>
    Ok(html.login(loginForm))
  }

  /**
   * Handle login form submission.
   */
  def authenticate = Action { implicit request =>
    loginForm.bindFromRequest.fold(
      formWithErrors => BadRequest(html.login(formWithErrors)),
      user => {
        val authResponse = Users.authenticate(user._1, user._2)
        authResponse.statusCode match {
            // if couchDB returns 200 the user exists and the pw is right
          case 200 => Redirect(routes.WelcomeController.index).withSession("username" -> user._1, "authCookie" -> authResponse.authCookie)
            // if the user not exists/pw is wrong couchDB returns 401
          case _ => {
            val loginError = loginForm.withGlobalError("Invalid user or password")
            BadRequest(html.login(loginError))
          }
        }

      })
  }

  /**
   * Logout and clean the session.
   */
  def logout = Action {
    Redirect(routes.Application.login).withNewSession.flashing(
      "success" -> "You've been logged out")
  }

}

/**
 * Provide security features
 */
trait Secured {
  self: Controller =>

  /**
   * Retrieve the connected user id.
   */
  def username(request: RequestHeader) = request.session.get("username")

  /**
   * Redirect to login if the user is not authorized
   */
  private def onUnauthorized(request: RequestHeader) = Results.Redirect(routes.Application.login)

  /**
  * Checks if the username is stored in the session. If yes the request is fulfilled; if not login is shown
  */
  def IsAuthenticated(f: => String => Request[AnyContent] => Result) =
    Security.Authenticated(username, onUnauthorized) { user =>
      Action(request => f(user)(request))
    }
}
