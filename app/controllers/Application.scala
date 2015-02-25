package controllers

import play.api._
import play.api.libs.ws.Response
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._

import views._
import models._

import scala.parallel.Future

object Application extends Controller {

  lazy val loginForm = Form(
    tuple(
      "name" -> text,
      "password" -> text) verifying ("Invalid user or password", result => result match {
        case (name, password) => {
          println("user=" + name + "password=" + password);
          val userList = Users.authenticate(name, password)
          userList == 1

        }
        case _ => false
      }))

//  val loginForm = Form(
//    tuple(
//      "email" -> text,
//      "password" -> text
//    ) verifying ("Invalid email or password", result => result match {
//      case (email, password) => Users.authenticate(email, password).isDefined
//    })

    def login = Action { implicit request =>
      Ok(html.login(loginForm))
    }

  /**
   * Handle login form submission.
   */
  def authenticate = Action { implicit request =>
    loginForm.bindFromRequest.fold(
      formWithErrors => BadRequest(html.login(formWithErrors)),
      user => Redirect(routes.WelcomeController.index).withSession("email" -> user._1))
  }

  /**
   * Logout and clean the session.
   */
  def logout = Action {
    Redirect(routes.Application.login).withNewSession.flashing(
      "success" -> "You've been logged out")
  }

  //def index = Action {
  //  Ok(views.html.index("Your new application is ready."))
  //}

}

/**
 * Provide security features
 */
trait Secured {
  self: Controller =>

  /**
   * Retrieve the connected user id.
   */
  def username(request: RequestHeader) = request.session.get("email")

  /**
   * Redirect to login if the user is not authorized
   */
  private def onUnauthorized(request: RequestHeader) = Results.Redirect(routes.Application.login)

  def IsAuthenticated(f: => String => Request[AnyContent] => Result) =
    Security.Authenticated(username, onUnauthorized) { user =>
      Action(request => f(user)(request))
    }
}
