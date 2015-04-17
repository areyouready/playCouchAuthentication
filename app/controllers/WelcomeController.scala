package controllers

import play.api._
import play.api.mvc._

object WelcomeController extends Controller with Secured {

  /**
   * Needs to be overridden by the controller
   */
  def unauthorized(request: RequestHeader) = Results.Redirect(routes.Application.login())

  /**
   * Secured access to index action
   */
  def index = withAuthCookie { username =>
    implicit request =>
    Ok(views.html.index("Your new application is ready."))
  }

}

