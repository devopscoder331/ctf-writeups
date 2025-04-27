package config

import (
	"crypto/rand"
	"fmt"

	"syllabus/pkg/logger"

	"github.com/spf13/viper"
)

func init() {
	viper.SetEnvPrefix("syllabus") // It will check for environment variables with prefix "SYLLABUS_"
	viper.AutomaticEnv()
}

type Config struct {
	Debug bool
	Web   *WebConfig
	DB    *DBConfig
}

type WebConfig struct {
	Port      int    `default:"8080"`
	SecretKey string `default:"secret"`
}

type DBConfig struct {
	Host     string `default:"db"`
	Port     int    `default:"5432"`
	User     string `default:"syllabus"`
	Password string `default:"syllabus"`
	DBName   string `default:"syllabus"`
	DSN      string
}

func getWebConfig() *WebConfig {
	secretKey := viper.GetString("web_secret_key")
	if secretKey == "" {
		randomBytes := make([]byte, 32)
		if _, err := rand.Read(randomBytes); err != nil {
			logger.Fatalf("failed to generate random secret key: %v", err)
		}
		secretKey = fmt.Sprintf("%x", randomBytes)
		logger.Warnf("web_secret_key not set, using generated random key")
	}

	return &WebConfig{
		Port:      viper.GetInt("web_port"),
		SecretKey: secretKey,
	}
}

func getDBConfig() *DBConfig {
	dbCfg := &DBConfig{
		Host:     viper.GetString("db_host"),
		Port:     viper.GetInt("db_port"),
		User:     viper.GetString("db_user"),
		Password: viper.GetString("db_password"),
		DBName:   viper.GetString("db_name"),
	}
	dbCfg.DSN = fmt.Sprintf("host=%s port=%d user=%s password=%s dbname=%s sslmode=disable",
		dbCfg.Host, dbCfg.Port, dbCfg.User, dbCfg.Password, dbCfg.DBName)

	return dbCfg
}

func GetConfig() *Config {
	debug := viper.GetBool("debug")

	return &Config{
		Debug: debug,
		Web:   getWebConfig(),
		DB:    getDBConfig(),
	}
}
