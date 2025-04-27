package main

import (
	"fmt"
	"net/http"
	"os"
	"path/filepath"

	"syllabus/internal/config"
	"syllabus/internal/router"
	"syllabus/internal/storage"
	"syllabus/pkg/logger"

	"github.com/sirupsen/logrus"
)

func main() {
	if err := os.Chdir(filepath.Dir(appFilePath())); err != nil {
		logger.Fatalf("os.Chdir failed error: %v", err)
	}

	cfg := config.GetConfig()

	if cfg.Debug {
		logger.SetLogLevel(logrus.DebugLevel)
	}

	db, err := storage.DBConn(cfg.DB.DSN)
	if err != nil {
		logger.Fatalf("failed to init db: %s", err)
	}

	s := storage.New(db)

	if err := storage.MigrateTables(s); err != nil {
		logger.Fatalf("failed to migrate tables: %v", err)
	}

	srv := &http.Server{
		Addr:    fmt.Sprintf(":%d", cfg.Web.Port),
		Handler: router.New(s, cfg.Web.SecretKey),
	}

	msg := fmt.Sprintf("Syllabus is up and running on ':%d'", cfg.Web.Port)
	logger.Infof(msg)

	if err := srv.ListenAndServe(); err != nil {
		logger.Fatalf("failed to start server: %v", err)
	}
}

func appFilePath() string {
	path, err := os.Executable()
	if err != nil {
		return os.Args[0]
	}
	return path
}
