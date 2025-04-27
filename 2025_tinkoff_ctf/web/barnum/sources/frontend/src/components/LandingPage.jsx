import React, { useState, useEffect } from 'react';
import {
  Box,
  Container,
  Typography,
  Grid,
  Card,
  CardContent,
  CardMedia,
  Button,
  List,
  ListItem,
  ListItemIcon,
  ListItemText,
  Tooltip,
  Zoom,
  Paper,
  Badge,
  Snackbar,
  Alert,
  Fade,
  Collapse,
  Avatar,
  Divider,
  Chip,
  IconButton
} from '@mui/material';
import StarIcon from '@mui/icons-material/Star';
import PersonIcon from '@mui/icons-material/Person';
import TimerIcon from '@mui/icons-material/Timer';
import AutoAwesomeIcon from '@mui/icons-material/AutoAwesome';
import PetsIcon from '@mui/icons-material/Pets';
import CreditScoreIcon from '@mui/icons-material/CreditScore';
import LocalOfferIcon from '@mui/icons-material/LocalOffer';
import AccessTimeFilledIcon from '@mui/icons-material/AccessTimeFilled';
import SchoolIcon from '@mui/icons-material/School';
import CakeIcon from '@mui/icons-material/Cake';
import EmojiEventsIcon from '@mui/icons-material/EmojiEvents';
import CloseIcon from '@mui/icons-material/Close';
import PaymentPage from './PaymentPage';

const services = [
  {
    title: "Лунный календарь «Циклы света»",
    description: "Планируйте дела по фазам Луны и Солнца в удобном PDF. Получите доступ к древней астрологической мудрости в современном формате для гармоничной жизни в потоке космических энергий.",
    image: "/static/service1.png",
    price: "1 астролапка",
    features: ["Ежедневные астрологические рекомендации", "Инфографика‑календарь с указанием фаз луны", "Советы по ритуалам в каждой фазе"]
  },
  {
    title: "Премиум‑карта «Звёздный навигатор»",
    description: "Полный астрологический анализ на год вперёд с дополнительными опциями для максимального погружения. Раскройте потенциал своей судьбы с помощью древних знаний.",
    image: "/static/service2.png",
    price: "5 астролапок",
    features: ["Натальная карта + транзиты планет", "Углублённый отчёт в подарочном буклете", "Видеоразбор от астролога-эксперта"]
  },
  {
    title: "Экспедиция «Врата Созвездий»",
    description: "Личный астролог отправится в древнее место силы в предгорьях Капибаревской Сопки, где проведёт многодневный ритуал очищения вашей кармы в период максимальной активности вашего зодиакального созвездия.",
    image: "/static/service3.png",
    price: "25 астролапок",
    features: [
      "4 дня ритуалов в местах пересечения лей-линий звёздных энергий Капибаревской Сопки",
      "Создание персонального оберега из горного хрусталя в час силы*"
    ],
    footnote: "*Доставка оберега оплачивается отдельно"
  }
];

const astrologers = [
  {
    name: "Елена Звездова",
    title: "Мастер натальных карт",
    image: "/static/astrologer1.png",
    sign: "Овен",
    experience: 15,
    specialization: "Ведические практики, натальные карты, прогрессии",
    description: "С детства одаренная необычным видением мира, Елена посвятила 15 лет изучению ведической астрологии в Непале и Тибете. Её прогнозы поражают точностью и глубиной, а методики работы с кармическими узлами признаны уникальными даже среди опытных астрологов.",
    achievements: ["Автор методики «Кармические Ключи»", "Создатель собственной школы астрологии", "Личный астролог известных деятелей искусства"]
  },
  {
    name: "Михаил Лунев",
    title: "Астролог-прогностик",
    image: "/static/astrologer2.png",
    sign: "Скорпион",
    experience: 12,
    specialization: "Хорарная астрология, прогнозирование, финансовые циклы",
    description: "Михаил соединяет классические техники астрологии с передовыми методами анализа данных. Специализируется на точных прогнозах и выборе благоприятного времени для важных решений.\nЕго консультации особенно ценят бизнесмены и те, кто стоит перед важным жизненным выбором.",
    achievements: ["Разработчик системы «Астро-финанс»", "Преподаватель в Академии Астрологических Наук", "Автор книги «Время Успеха»"]
  },
  {
    name: "Анастасия Звездоцвет",
    title: "Астропсихолог",
    image: "/static/astrologer3.png",
    sign: "Рыбы",
    experience: 9,
    specialization: "Психологическая астрология, детские гороскопы, совместимость",
    description: "Анастасия обладает уникальным даром соединять астрологические знания с глубоким пониманием психологии человека. Её подход помогает не только понять причины происходящего, но и найти путь к гармонии. Специализируется на семейных отношениях и детских гороскопах.",
    achievements: ["Создатель методики «Астро-Гармония»", "Ведущая астрологического подкаста", "Эксперт по детской астропсихологии"]
  }
];

const benefits = [
  "Первая услуга бесплатно!",
  "Личный астролог подберёт благоприятные даты",
  "Дипломированные астрологи с многолетним опытом",
  "Сакральные знания древней астрологии",
  "Все услуги в гармонии с движением планет"
];

const API_BASE_URL = '/api';

function LandingPage({ onStartConsultation, credits, hasUsedFreeCredit, onCreditsUpdate, totalAvailableHours, userInfo }) {
  const [hoveredCapybara, setHoveredCapybara] = useState(null);
  const [clickCount, setClickCount] = useState(0);
  const [showPayment, setShowPayment] = useState(false);
  const [selectedPackage, setSelectedPackage] = useState(null);
  const [showCapybaraMessage, setShowCapybaraMessage] = useState(false);
  const [showHoursPopup, setShowHoursPopup] = useState(false);
  const [localAvailableHours, setLocalAvailableHours] = useState(totalAvailableHours);

  useEffect(() => {
    if (userInfo?.email) {
      if (totalAvailableHours !== null) {
        setLocalAvailableHours(totalAvailableHours);
        const timer = setTimeout(() => {
          setShowHoursPopup(true);
        }, 2000);
        return () => clearTimeout(timer);
      } else {
        const fetchAstrologers = async () => {
          try {
            const response = await fetch(`${API_BASE_URL}/astrologers`, {
              credentials: 'include'
            });
            
            if (response.ok) {
              const data = await response.json();
              if (data.success && data.astrologers) {
                const hours = data.astrologers.reduce((total, astrologer) => {
                  const availableHours = astrologer.availableHours - astrologer.bookedHours;
                  return total + Math.max(0, availableHours);
                }, 0);
                setLocalAvailableHours(hours);
                
                setTimeout(() => {
                  setShowHoursPopup(true);
                }, 2000);
              }
            }
          } catch (error) {
            console.error('Error fetching astrologers:', error);
          }
        };
        
        fetchAstrologers();
      }
    } else {
      setShowHoursPopup(false);
    }
  }, [totalAvailableHours, userInfo]);

  const handleCapybaraClick = () => {
    const newCount = clickCount + 1;
    setClickCount(newCount);
    if (newCount === 7) {
      setShowCapybaraMessage(true);
    }
  };

  const handleCloseCapybaraMessage = (event, reason) => {
    if (reason === 'clickaway') {
      return;
    }
    setShowCapybaraMessage(false);
  };

  const handleBuyCredits = (creditPackage) => {
    setSelectedPackage(creditPackage);
    setShowPayment(true);
  };

  const handlePaymentSuccess = () => {
    if (selectedPackage && onCreditsUpdate) {
      onCreditsUpdate(credits + selectedPackage.amount);
    }
    setShowPayment(false);
    setSelectedPackage(null);
  };

  const handlePaymentCancel = () => {
    setShowPayment(false);
    setSelectedPackage(null);
  };

  const formatHoursText = (hours) => {
    if (hours === 0) return "часов";
    if (hours === 1) return "час";
    if (hours > 1 && hours < 5) return "часа";
    return "часов";
  };

  if (showPayment) {
    return (
      <PaymentPage
        selectedPackage={selectedPackage}
        onSuccess={handlePaymentSuccess}
        onCancel={handlePaymentCancel}
      />
    );
  }

  return (
    <Box sx={{ bgcolor: '#1a1a1a', minHeight: '100vh', color: 'white', position: 'relative' }}>
      <Snackbar
        open={showCapybaraMessage}
        autoHideDuration={6000}
        onClose={handleCloseCapybaraMessage}
        anchorOrigin={{ vertical: 'top', horizontal: 'center' }}
      >
        <Alert
          onClose={handleCloseCapybaraMessage}
          severity="success"
          variant="filled"
          sx={{
            width: '100%',
            bgcolor: '#4ECDC4',
            '& .MuiAlert-icon': {
              color: 'white'
            },
            '& .MuiAlert-message': {
              color: 'white',
              fontSize: '1.1rem'
            }
          }}
        >
          <Box sx={{ textAlign: 'center' }}>
            <Typography variant="h6" component="div" sx={{ mb: 1 }}>
              🌟 Поздравляем! Вы нашли секретную капибару! 🌟
            </Typography>
            <Typography>
              Вам будет доступен бесплатный астрологический прогноз при первом обращении к нашим мастерам!
            </Typography>
          </Box>
        </Alert>
      </Snackbar>

      <Fade in={showHoursPopup && userInfo?.email} timeout={800}>
        <Box 
          sx={{
            position: 'fixed',
            bottom: 30,
            left: 30,
            zIndex: 1000,
            maxWidth: 360,
            animation: localAvailableHours === 0 ? 'pulse 1.5s infinite' : 'none',
            '@keyframes pulse': {
              '0%': { transform: 'scale(1)' },
              '50%': { transform: 'scale(1.05)' },
              '100%': { transform: 'scale(1)' }
            }
          }}
        >
          <Paper
            elevation={8}
            sx={{
              borderRadius: 2,
              overflow: 'hidden',
              background: 'linear-gradient(45deg, #FF6B6B, #4ECDC4)',
              boxShadow: '0 4px 20px rgba(0,0,0,0.25)',
              transition: 'all 0.3s ease',
              '&:hover': {
                transform: 'translateY(-5px)',
                boxShadow: '0 8px 30px rgba(0,0,0,0.3)',
                background: 'linear-gradient(45deg, #FF6B6B, #4ECDC4)',
                backgroundSize: '200% 200%',
                animation: 'gradientShift 2s ease infinite',
              },
              '@keyframes gradientShift': {
                '0%': { backgroundPosition: '0% 50%' },
                '50%': { backgroundPosition: '100% 50%' },
                '100%': { backgroundPosition: '0% 50%' }
              }
            }}
          >
            <Box sx={{ 
              p: 2.5,
              display: 'flex',
              alignItems: 'center',
              gap: 2,
              position: 'relative'
            }}>
              <IconButton
                size="small"
                aria-label="close"
                onClick={(e) => {
                  e.stopPropagation();
                  setShowHoursPopup(false);
                }}
                sx={{
                  position: 'absolute',
                  right: 8,
                  top: 8,
                  color: 'white',
                }}
              >
                <CloseIcon fontSize="small" />
              </IconButton>
              <AccessTimeFilledIcon sx={{ 
                fontSize: 36,
                color: 'white',
                animation: localAvailableHours === 0 ? 'spin 3s linear infinite' : 'pulse 2s ease-in-out infinite',
                '@keyframes spin': {
                  '0%': { transform: 'rotate(0deg)' },
                  '100%': { transform: 'rotate(360deg)' }
                },
                '@keyframes pulse': {
                  '0%': { opacity: 0.7 },
                  '50%': { opacity: 1 },
                  '100%': { opacity: 0.7 }
                }
              }} />
              <Box onClick={onStartConsultation} sx={{ cursor: 'pointer', flex: 1 }}>
                <Typography variant="h6" sx={{ 
                  fontWeight: 'bold',
                  color: 'white',
                  textShadow: '0 1px 2px rgba(0,0,0,0.2)',
                  lineHeight: 1.2
                }}>
                  {localAvailableHours === 0 
                    ? "Все астрологи заняты!" 
                    : `Осталось всего ${localAvailableHours} ${formatHoursText(localAvailableHours)} доступных для бронирования!`}
                </Typography>
                <Typography variant="body2" sx={{ 
                  color: 'rgba(255,255,255,0.9)',
                  mt: 0.5
                }}>
                  {localAvailableHours === 0 
                    ? "Запишитесь в лист ожидания сейчас" 
                    : "Успейте забронировать встречу с астрологом"}
                </Typography>
              </Box>
            </Box>
          </Paper>
        </Box>
      </Fade>

      <Box
        sx={{
          background: 'linear-gradient(45deg, #FF6B6B, #4ECDC4)',
          py: 2,
          px: 4,
          display: 'flex',
          justifyContent: 'center',
          alignItems: 'center',
          gap: 2
        }}
      >
        <LocalOfferIcon />
        <Typography variant="h6" sx={{ fontWeight: 500 }}>
          {hasUsedFreeCredit 
            ? `У вас ${credits} астролапка(ок) для консультаций` 
            : "Первая консультация бесплатно! Начните свой духовный путь прямо сейчас"}
        </Typography>
      </Box>

      <Box
        sx={{
          background: 'linear-gradient(135deg, #2a0845 0%, #6441A5 100%)',
          color: 'white',
          height: 'calc(100vh - 56px)',
          display: 'flex',
          alignItems: 'center',
          position: 'relative',
          overflow: 'hidden',
          textAlign: 'center'
        }}
      >
        <Container maxWidth="lg">
          <Typography 
            variant="h1" 
            component="h1" 
            gutterBottom
            sx={{ 
              fontSize: { xs: '2.5rem', md: '4rem' },
              fontWeight: 'bold',
              textShadow: '2px 2px 4px rgba(0,0,0,0.3)',
              mb: 4
            }}
          >
            Астрология и древние практики
          </Typography>
          <Typography 
            variant="h5" 
            sx={{ 
              mb: 6,
              opacity: 0.9,
              maxWidth: '800px',
              margin: '0 auto',
              lineHeight: 1.5
            }}
          >
            Откройте врата в мир древних знаний с опытными астрологами. Доверьтесь мудрости веков для преображения вашей судьбы.
          </Typography>
          <Button
            variant="contained"
            size="large"
            onClick={onStartConsultation}
            sx={{
              bgcolor: 'rgba(255,255,255,0.9)',
              color: '#2a0845',
              fontSize: '1.2rem',
              py: 2,
              px: 6,
              mt: 4,
              borderRadius: '50px',
              boxShadow: '0 4px 20px rgba(0,0,0,0.15)',
              position: 'relative',
              zIndex: 2,
              '&:hover': {
                bgcolor: 'white',
                transform: 'scale(1.05) translateY(-2px)',
                transition: 'all 0.3s ease',
                boxShadow: '0 6px 25px rgba(0,0,0,0.2)'
              }
            }}
          >
            Начать свой путь
          </Button>
          <Tooltip title="Погладь капибару на удачу!" placement="top">
            <PetsIcon 
              sx={{ 
                position: 'absolute',
                bottom: 20,
                right: 20,
                fontSize: '2rem',
                cursor: 'pointer',
                opacity: 0.7,
                '&:hover': {
                  opacity: 1,
                  transform: 'rotate(360deg)',
                  transition: 'all 0.5s ease'
                }
              }}
              onClick={handleCapybaraClick}
            />
          </Tooltip>

          <Box 
            sx={{ 
              display: 'flex', 
              alignItems: 'center', 
              justifyContent: 'center',
              gap: 2,
              mt: 4,
              bgcolor: 'rgba(255,255,255,0.1)',
              borderRadius: 3,
              p: 2,
              maxWidth: 'fit-content',
              margin: '2rem auto'
            }}
          >
            <Badge badgeContent={credits} color="primary">
              <CreditScoreIcon sx={{ fontSize: '2rem' }} />
            </Badge>
            <Typography>
              {hasUsedFreeCredit 
                ? `У вас осталось ${credits} астролапка(ок)` 
                : "У вас есть бесплатная астролапка для первой консультации!"}
            </Typography>
          </Box>
        </Container>
      </Box>

      <Container sx={{ py: 12 }}>
        <Typography 
          variant="h2" 
          component="h2" 
          textAlign="center" 
          gutterBottom
          sx={{ 
            mb: 8,
            position: 'relative',
            '&::before': {
              content: '""',
              position: 'absolute',
              top: 0,
              left: 0,
              right: 0,
              bottom: 0,
              background: 'linear-gradient(45deg, #FF6B6B, #4ECDC4)',
              backgroundClip: 'text',
              WebkitBackgroundClip: 'text',
              color: 'transparent',
              display: 'block',
              fontWeight: 'bold',
              fontSize: { xs: '2rem', md: '3rem' },
              textAlign: 'center',
              letterSpacing: '0.02em',
              WebkitTextFillColor: 'transparent'
            }
          }}
        >
          Наши услуги
        </Typography>
        <Grid container spacing={6}>
          {services.map((service, index) => (
            <Grid 
              item 
              xs={12} 
              md={4} 
              key={index}
              sx={{ 
                display: 'flex',
                '& > *': {
                  flex: 1
                }
              }}
            >
              <Card 
                sx={{ 
                  height: '100%',
                  display: 'flex',
                  flexDirection: 'column',
                  bgcolor: 'rgba(255,255,255,0.05)',
                  backdropFilter: 'blur(10px)',
                  position: 'relative',
                  boxShadow: '0 4px 12px rgba(0,0,0,0.1)',
                  borderRadius: 2,
                  transition: 'all 0.3s ease',
                  '&:hover': {
                    transform: 'scale(1.05)',
                    bgcolor: 'rgba(255,255,255,0.05)'
                  }
                }}
              >
                <Box sx={{ position: 'relative' }}>
                  <CardMedia
                    component="img"
                    height="300"
                    image={service.image}
                    alt={service.title}
                    sx={{ 
                      filter: 'brightness(0.8)',
                      objectFit: 'cover',
                      width: '100%',
                      height: '300px',
                      borderRadius: '8px 8px 0 0'
                    }}
                  />
                  <Box
                    sx={{
                      position: 'absolute',
                      bottom: 0,
                      left: 0,
                      right: 0,
                      height: '100%',
                      background: 'linear-gradient(to top, rgba(0,0,0,0.7) 0%, rgba(0,0,0,0) 50%)',
                      borderRadius: '8px 8px 0 0'
                    }}
                  />
                </Box>
                <CardContent sx={{ 
                  color: 'white',
                  flex: 1,
                  display: 'flex',
                  flexDirection: 'column',
                  justifyContent: 'space-between'
                }}>
                  <Box>
                    <Typography variant="h5" component="h3" gutterBottom sx={{ color: '#4ECDC4', fontSize: '1.5rem' }}>
                      {service.title}
                    </Typography>
                    <Typography variant="body1" sx={{ 
                      mb: 2, 
                      color: 'rgba(255,255,255,0.8)',
                      fontSize: '1.1rem',
                      lineHeight: 1.5
                    }}>
                      {service.description}
                    </Typography>
                    <List sx={{ mb: 2, pl: 0 }}>
                      {service.features && service.features.map((feature, idx) => (
                        <ListItem key={idx} sx={{ py: 0.5, px: 0 }}>
                          <ListItemIcon sx={{ minWidth: '30px' }}>
                            <StarIcon fontSize="small" sx={{ color: '#FF6B6B' }} />
                          </ListItemIcon>
                          <ListItemText 
                            primary={feature} 
                            sx={{ 
                              '& .MuiTypography-root': { 
                                fontSize: '1rem', 
                                color: 'rgba(255,255,255,0.9)',
                                lineHeight: 1.4
                              } 
                            }}
                          />
                        </ListItem>
                      ))}
                    </List>
                    {service.footnote && (
                      <Typography 
                        variant="caption" 
                        sx={{ 
                          display: 'block', 
                          mt: 1, 
                          fontStyle: 'italic', 
                          color: 'rgba(255,255,255,0.6)',
                          fontSize: '0.75rem'
                        }}
                      >
                        {service.footnote}
                      </Typography>
                    )}
                  </Box>
                  <Box sx={{ display: 'flex', alignItems: 'center', gap: 2, mt: 'auto' }}>
                    <Typography 
                      variant="h6" 
                      sx={{ 
                        color: '#FF6B6B',
                        textDecoration: index === 0 ? 'line-through' : 'none',
                        opacity: index === 0 ? 0.7 : 1
                      }}
                    >
                      {service.price}
                    </Typography>
                    {index === 0 ? (
                      <Typography 
                        variant="h6" 
                        sx={{ 
                          color: '#4ECDC4',
                          fontWeight: 'bold'
                        }}
                      >
                        ПЕРВАЯ БЕСПЛАТНО!
                      </Typography>
                    ) : null}
                  </Box>
                </CardContent>
              </Card>
            </Grid>
          ))}
        </Grid>


      <Container sx={{ py: 12 }}>
        <Grid container spacing={6}>
          {[
            {
              icon: <PersonIcon sx={{ fontSize: 60, color: '#4ECDC4' }} />,
              title: "Опытные астрологи",
              description: "Наши мастера совмещают классическую астрологию с древними практиками."
            },
            {
              icon: <TimerIcon sx={{ fontSize: 60, color: '#4ECDC4' }} />,
              title: "Астрологическое время",
              description: "Все услуги проводятся в моменты максимальной силы планет."
            },
            {
              icon: <AutoAwesomeIcon sx={{ fontSize: 60, color: '#4ECDC4' }} />,
              title: "Индивидуальный подход",
              description: "Каждая услуга основана на вашей личной натальной карте."
            }
          ].map((feature, index) => (
            <Grid 
              item 
              xs={12} 
              md={4} 
              key={index}
              sx={{
                display: 'flex',
                '& > *': {
                  flex: 1
                }
              }}
            >
              <Box 
                sx={{ 
                  textAlign: 'center',
                  p: 3,
                  bgcolor: 'rgba(255,255,255,0.03)',
                  borderRadius: 4,
                  transition: 'all 0.3s ease',
                  display: 'flex',
                  flexDirection: 'column',
                  alignItems: 'center',
                  height: '100%',
                  '&:hover': {
                    transform: 'translateY(-10px)',
                    bgcolor: 'rgba(255,255,255,0.05)'
                  }
                }}
              >
                <Box sx={{ display: 'flex', justifyContent: 'center', width: '100%' }}>
                  {feature.icon}
                </Box>
                <Typography variant="h6" gutterBottom sx={{ color: '#4ECDC4', mt: 2 }}>
                  {feature.title}
                </Typography>
                <Typography sx={{ color: 'rgba(255,255,255,0.8)', flex: 1 }}>
                  {feature.description}
                </Typography>
              </Box>
            </Grid>
          ))}
        </Grid>
      </Container>

      <Box sx={{ bgcolor: 'rgba(255,255,255,0.01)', py: 12 }}>
        <Container>
          <Typography 
            variant="h2" 
            component="h2" 
            textAlign="center" 
            gutterBottom
            sx={{ 
              mb: 8,
              position: 'relative',
              '&::before': {
                content: '""',
                position: 'absolute',
                top: 0,
                left: 0,
                right: 0,
                bottom: 0,
                background: 'linear-gradient(45deg, #4ECDC4, #FF6B6B)',
                backgroundClip: 'text',
                WebkitBackgroundClip: 'text',
                color: 'transparent',
                display: 'block',
                fontWeight: 'bold',
                fontSize: { xs: '2rem', md: '3rem' },
                textAlign: 'center',
                letterSpacing: '0.02em',
                WebkitTextFillColor: 'transparent'
              }
            }}
          >
            Наши астрологи
          </Typography>
          
          <Typography 
            variant="h6" 
            textAlign="center" 
            sx={{ 
              mb: 6,
              color: 'rgba(255,255,255,0.8)',
              maxWidth: '800px',
              mx: 'auto'
            }}
          >
            Познакомьтесь с нашими опытными астрологами, которые помогут вам раскрыть тайны вашей судьбы и направят на путь гармонии и процветания
          </Typography>
          
          <Grid container spacing={6}>
            {astrologers.map((astrologer, index) => (
              <Grid 
                item 
                xs={12} 
                md={4} 
                key={index}
                sx={{
                  display: 'flex',
                  height: '100%'
                }}
              >
                <Card 
                  sx={{ 
                    display: 'flex',
                    flexDirection: 'column',
                    bgcolor: 'rgba(255,255,255,0.03)',
                    backdropFilter: 'blur(10px)',
                    borderRadius: 4,
                    overflow: 'hidden',
                    transition: 'all 0.3s ease',
                    height: '100%',
                    width: '100%',
                    '&:hover': {
                      transform: 'translateY(-10px)',
                      boxShadow: '0 12px 30px rgba(78, 205, 196, 0.2)',
                      '& .MuiCardMedia-root': {
                        transform: 'scale(1.05)'
                      }
                    }
                  }}
                >
                  <Box sx={{ position: 'relative', overflow: 'hidden' }}>
                    <CardMedia
                      component="img"
                      height="300"
                      image={astrologer.image}
                      alt={astrologer.name}
                      sx={{ 
                        transition: 'transform 0.5s ease',
                        objectFit: 'cover'
                      }}
                    />
                    <Box
                      sx={{
                        position: 'absolute',
                        bottom: 0,
                        left: 0,
                        right: 0,
                        height: '50%',
                        background: 'linear-gradient(to top, rgba(0,0,0,0.7) 0%, rgba(0,0,0,0) 100%)',
                      }}
                    />
                    <Box
                      sx={{
                        position: 'absolute',
                        bottom: 0,
                        left: 0,
                        width: '100%',
                        p: 2,
                        zIndex: 2
                      }}
                    >
                      <Typography variant="h5" component="h3" sx={{ color: 'white', fontWeight: 'bold', textShadow: '0 2px 4px rgba(0,0,0,0.5)' }}>
                        {astrologer.name}
                      </Typography>
                      <Typography variant="subtitle1" sx={{ color: '#4ECDC4', textShadow: '0 1px 2px rgba(0,0,0,0.5)' }}>
                        {astrologer.title}
                      </Typography>
                    </Box>
                  </Box>
                  
                  <CardContent sx={{ p: 3, flexGrow: 1, display: 'flex', flexDirection: 'column' }}>
                    <Box sx={{ mb: 2, display: 'flex', gap: 1, flexWrap: 'wrap' }}>
                      <Chip 
                        icon={<CakeIcon fontSize="small" />} 
                        label={`Знак: ${astrologer.sign}`}
                        size="small"
                        sx={{ bgcolor: 'rgba(255,255,255,0.05)', color: '#FF6B6B' }}
                      />
                      <Chip 
                        icon={<SchoolIcon fontSize="small" />} 
                        label={`Опыт: ${astrologer.experience} лет`}
                        size="small"
                        sx={{ bgcolor: 'rgba(255,255,255,0.05)', color: '#4ECDC4' }}
                      />
                    </Box>
                    
                    <Typography variant="subtitle2" sx={{ color: '#4ECDC4', mb: 1 }}>
                      Специализация:
                    </Typography>
                    <Typography variant="body2" sx={{ color: 'rgba(255,255,255,0.8)', mb: 2 }}>
                      {astrologer.specialization}
                    </Typography>
                    
                    <Typography variant="body2" sx={{ color: 'rgba(255,255,255,0.8)', mb: 2, flex: 1 }}>
                      {astrologer.description}
                    </Typography>
                    
                    <Divider sx={{ my: 2, bgcolor: 'rgba(255,255,255,0.1)' }} />
                    
                    <Box>
                      <Typography variant="subtitle2" sx={{ color: '#4ECDC4', mb: 1, display: 'flex', alignItems: 'center', gap: 1 }}>
                        <EmojiEventsIcon fontSize="small" /> Достижения:
                      </Typography>
                      <List dense disablePadding>
                        {astrologer.achievements.map((achievement, idx) => (
                          <ListItem key={idx} sx={{ py: 0.5, px: 0 }}>
                            <ListItemIcon sx={{ minWidth: '30px' }}>
                              <StarIcon fontSize="small" sx={{ color: '#FF6B6B' }} />
                            </ListItemIcon>
                            <ListItemText 
                              primary={achievement} 
                              sx={{ 
                                '& .MuiTypography-root': { 
                                  fontSize: '0.875rem', 
                                  color: 'rgba(255,255,255,0.8)'
                                } 
                              }}
                            />
                          </ListItem>
                        ))}
                      </List>
                    </Box>
                    
                    <Button 
                      variant="contained" 
                      fullWidth 
                      onClick={onStartConsultation}
                      sx={{ 
                        mt: 3,
                        background: 'linear-gradient(45deg, #4ECDC4, #FF6B6B)',
                        color: 'white',
                        '&:hover': {
                          background: 'linear-gradient(45deg, #3dbdb5, #ff5b5b)',
                          transform: 'translateY(-2px)'
                        }
                      }}
                    >
                      Запись на консультацию
                    </Button>
                  </CardContent>
                </Card>
              </Grid>
            ))}
          </Grid>
        </Container>
      </Box>

      <Paper 
          sx={{ 
            mt: 8,
            p: 4,
            bgcolor: 'rgba(255,255,255,0.05)',
            borderRadius: 4,
            backdropFilter: 'blur(10px)',
            textAlign: 'center'
          }}
        >
          <Typography variant="h4" gutterBottom sx={{ color: '#4ECDC4' }}>
            Приобретите астролапки для оплаты услуг
          </Typography>
          <Typography sx={{ color: 'rgba(255,255,255,0.8)', mb: 3 }}>
            Инвестируйте в свою судьбу с помощью опытных астрологов и древних практик
          </Typography>
          <Grid container spacing={3} justifyContent="center">
            {[
              { amount: 1, price: "5000₡", discount: "Пробный пакет" },
              { amount: 5, price: "20000₡", discount: "15% выгода" },
              { amount: 25, price: "75000₡", discount: "40% выгода" }
            ].map((pack, index) => (
              <Grid 
                item 
                xs={12} 
                sm={4} 
                key={index}
                sx={{
                  display: 'flex'
                }}
              >
                <Paper 
                  sx={{ 
                    p: 3,
                    bgcolor: 'rgba(255,255,255,0.03)',
                    transition: 'all 0.3s ease',
                    display: 'flex',
                    flexDirection: 'column',
                    width: '100%',
                    height: '100%',
                    '&:hover': {
                      transform: 'scale(1.05)',
                      bgcolor: 'rgba(255,255,255,0.05)'
                    }
                  }}
                >
                  <Box sx={{ flex: 1 }}>
                    <Typography variant="h5" sx={{ color: '#4ECDC4', mb: 1 }}>
                      {pack.amount} {pack.amount >= 5 ? 'Астролапок' : 'Астролапки'}
                    </Typography>
                    <Typography variant="h6" sx={{ color: '#FF6B6B' }}>
                      {pack.price}
                    </Typography>
                    <Typography variant="body2" sx={{ color: 'rgba(255,255,255,0.6)' }}>
                      Скидка {pack.discount}
                    </Typography>
                  </Box>
                  <Button
                    variant="contained"
                    fullWidth
                    onClick={() => handleBuyCredits(pack)}
                    sx={{ 
                      mt: 2,
                      bgcolor: '#4ECDC4',
                      '&:hover': {
                        bgcolor: '#3dbdb5'
                      }
                    }}
                  >
                    Купить
                  </Button>
                </Paper>
              </Grid>
            ))}
          </Grid>
        </Paper>
      </Container>

      <Box sx={{ bgcolor: 'rgba(255,255,255,0.02)', py: 12 }}>
        <Container>
          <Typography 
            variant="h3" 
            component="h2" 
            textAlign="center" 
            gutterBottom
            sx={{ 
              mb: 8,
              background: 'linear-gradient(45deg, #4ECDC4, #FF6B6B)',
              backgroundClip: 'text',
              WebkitBackgroundClip: 'text',
              color: 'transparent'
            }}
          >
            Заинтересованы?
          </Typography>
          <Grid container spacing={6}>
            <Grid item xs={12} md={6}>
              <List>
                {benefits.map((benefit, index) => (
                  <ListItem 
                    key={index}
                    sx={{
                      mb: 2,
                      bgcolor: 'rgba(255,255,255,0.03)',
                      borderRadius: 2,
                      transition: 'all 0.3s ease',
                      '&:hover': {
                        bgcolor: 'rgba(255,255,255,0.05)',
                        transform: 'translateX(10px)'
                      }
                    }}
                  >
                    <ListItemIcon>
                      <StarIcon sx={{ color: '#4ECDC4' }} />
                    </ListItemIcon>
                    <ListItemText 
                      primary={benefit}
                      sx={{ 
                        '& .MuiTypography-root': { 
                          color: 'rgba(255,255,255,0.9)'
                        }
                      }}
                    />
                  </ListItem>
                ))}
              </List>
            </Grid>
            <Grid item xs={12} md={6}>
              <Box 
                sx={{ 
                  p: 4, 
                  bgcolor: 'rgba(255,255,255,0.03)',
                  borderRadius: 4,
                  backdropFilter: 'blur(10px)'
                }}
              >
                <Typography variant="h5" gutterBottom sx={{ color: '#4ECDC4' }}>
                  Начните сегодня
                </Typography>
                <Typography paragraph sx={{ color: 'rgba(255,255,255,0.8)' }}>
                  Наши опытные астрологи готовы применить древние знания, чтобы открыть для вас новые пути и возможности в потоке космических энергий.
                </Typography>
                <Button
                  variant="contained"
                  size="large"
                  onClick={onStartConsultation}
                  sx={{
                    bgcolor: '#4ECDC4',
                    '&:hover': {
                      bgcolor: '#3dbdb5'
                    }
                  }}
                >
                  Записаться к астрологу
                </Button>
              </Box>
            </Grid>
          </Grid>
        </Container>
      </Box>

    </Box>

    
  );
}

export default LandingPage; 
