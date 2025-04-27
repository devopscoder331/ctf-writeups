package router

import (
	"net/http"
	"time"

	"syllabus/internal/api"
	"syllabus/internal/middleware"
	"syllabus/internal/storage"

	"github.com/gin-contrib/cors"
	"github.com/gin-gonic/gin"

	"github.com/gin-contrib/sessions"
	"github.com/gin-contrib/sessions/cookie"
)

type Router struct {
	router *gin.Engine
	store  storage.Store
}

func New(s storage.Store, secretKey string) *Router {
	r := &Router{
		router: gin.Default(),
		store:  s,
	}

	r.setCors()
	r.initSessions(secretKey)
	r.initRoutes()
	return r
}

func (r *Router) ServeHTTP(w http.ResponseWriter, req *http.Request) {
	r.router.ServeHTTP(w, req)
}

func (r *Router) setCors() {
	config := cors.DefaultConfig()
	config.AllowOriginFunc = func(origin string) bool { return true }
	config.AllowMethods = []string{"POST", "GET", "PUT", "DELETE", "OPTIONS"}
	config.ExposeHeaders = []string{"Content-Length"}
	config.AllowCredentials = true
	config.MaxAge = 12 * time.Hour

	r.router.Use(cors.New(config))
}

func (r *Router) initSessions(secretKey string) {
	sessionStore := cookie.NewStore([]byte(secretKey))
	r.router.Use(sessions.Sessions("session", sessionStore))
}

func (r *Router) initRoutes() {
	// API Router Group
	apiRouter := r.router.Group("/api")

	authedApiRouter := apiRouter.Group("")
	authedApiRouter.Use(middleware.AuthRequired)

	// Auth controllers
	{
		apiRouter.POST("/auth/register", api.Register(r.store))
		apiRouter.POST("/auth/login", api.Login(r.store))
		apiRouter.POST("/auth/logout", api.Logout(r.store))
		authedApiRouter.GET("/auth/me", api.GetCurrentUser(r.store))
	}

	// Lesson controllers
	{
		authedApiRouter.POST("/lesson", api.CreateLesson(r.store))
		authedApiRouter.GET("/lesson/:id", api.FindLessonByID(r.store))
		authedApiRouter.PUT("/lesson/:id", api.UpdateLesson(r.store))
		authedApiRouter.DELETE("/lesson/:id", api.DeleteLesson(r.store))
		authedApiRouter.GET("/lessons", api.GetAllLessons(r.store))
		authedApiRouter.POST("/lessons/bulk-delete", api.BulkDeleteLessons(r.store))

	}

	// Learning controllers
	{
		authedApiRouter.GET("/learning-requirements", api.GetLearningRequirements(r.store))
		authedApiRouter.POST("/start-learning", api.StartLearning(r.store))
	}
}
