import React, { useState, useRef, useEffect } from 'react';
import { 
  Container, 
  Paper, 
  TextField, 
  Button, 
  Box, 
  Typography,
  List,
  ListItem,
  ListItemText,
  IconButton,
  Tooltip,
  Badge,
  Menu,
  MenuItem,
  Alert,
  Fade,
} from '@mui/material';
import { ThemeProvider, createTheme } from '@mui/material/styles';
import LandingPage from './components/LandingPage';
import PetsIcon from '@mui/icons-material/Pets';
import HomeIcon from '@mui/icons-material/Home';
import CreditScoreIcon from '@mui/icons-material/CreditScore';
import PersonIcon from '@mui/icons-material/Person';
import AccessTimeIcon from '@mui/icons-material/AccessTime';
import AccessTimeFilledIcon from '@mui/icons-material/AccessTimeFilled';
import CloseIcon from '@mui/icons-material/Close';

const LOADING_MESSAGES = [
  { text: "Читает карту небесных тел", emoji: "🌚" },
  { text: "Разгадывает послания небес", emoji: "✨" },
  { text: "Советуется с планетами", emoji: "🌍" },
  { text: "Сверяется с гороскопом", emoji: "🔮" },
  { text: "Вслушивается в эхо глубин", emoji: "🌊" }
];

const loadingStyles = `
  @keyframes loadingDots {
    0% { content: '.'; }
    33% { content: '..'; }
    66% { content: '...'; }
    100% { content: '.'; }
  }
  .loading-dots::after {
    content: '.';
    animation: loadingDots 1.5s infinite;
    display: inline-block;
    width: 24px;
    text-align: left;
  }
  @keyframes fadeInOut {
    0% { opacity: 0; }
    20% { opacity: 1; }
    80% { opacity: 1; }
    100% { opacity: 0; }
  }
  .loading-message {
    animation: fadeInOut 3s ease-in-out;
  }
`;

const theme = createTheme({
  palette: {
    mode: 'dark',
    primary: {
      main: '#4ECDC4',
    },
    background: {
      default: '#1a1a1a',
      paper: '#2a2a2a',
    },
  },
  typography: {
    fontFamily: '"Roboto", "Arial", sans-serif',
  },
  components: {
    MuiButton: {
      styleOverrides: {
        root: {
          borderRadius: '50px',
          textTransform: 'none',
        },
      },
    },
  },
});

const API_BASE_URL = import.meta.env.VITE_API_URL || '/api';

function App() {
  const [messages, setMessages] = useState([]);
  const [input, setInput] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const [showChat, setShowChat] = useState(false);
  const [capybaraClicks, setCapybaraClicks] = useState(0);
  const [sessionReady, setSessionReady] = useState(false);
  const [statusMessage, setStatusMessage] = useState('');
  const [currentLoadingMessage, setCurrentLoadingMessage] = useState(0);
  const [userBalance, setUserBalance] = useState(0);
  const [userBookings, setUserBookings] = useState([]);
  const [astrologers, setAstrologers] = useState([]);
  const [totalAvailableHours, setTotalAvailableHours] = useState(null);
  const [flagMessage, setFlagMessage] = useState('');
  const [bookingsMenuAnchor, setBookingsMenuAnchor] = useState(null);
  const [astrologersMenuAnchor, setAstrologersMenuAnchor] = useState(null);
  const [userInfo, setUserInfo] = useState(null);
  const [showHoursPopupChat, setShowHoursPopupChat] = useState(false);
  const messagesEndRef = useRef(null);
  const [focusKey, setFocusKey] = useState(0);
  const [hoursPopupShown, setHoursPopupShown] = useState(false);
  const [prevSessionReady, setPrevSessionReady] = useState(false);
  const flagFetchedRef = useRef(false);
  
  const scrollToBottom = () => {
    messagesEndRef.current?.scrollIntoView({ behavior: "smooth" });
  };
  
  const refocusInput = () => {
    setFocusKey(prev => prev + 1);
  };
  
  useEffect(() => {
    if (sessionReady && !isLoading) {
      refocusInput();
    }
  }, [sessionReady, isLoading]);

  useEffect(() => {
    scrollToBottom();
  }, [messages]);

  useEffect(() => {
    if (showChat) {
      checkSessionStatus();
      loadUserBalance();
    }
  }, [showChat]);

  useEffect(() => {
    if (showChat) {
      loadChatHistory();
    }
  }, [showChat]);

  useEffect(() => {
    let interval;
    if (isLoading) {
      interval = setInterval(() => {
        setCurrentLoadingMessage(prev => (prev + 1) % LOADING_MESSAGES.length);
      }, 3000);
    }
    return () => {
      if (interval) clearInterval(interval);
    };
  }, [isLoading]);

  useEffect(() => {
    if (astrologers.length > 0) {
      const totalHours = astrologers.reduce((total, astrologer) => {
        const availableHours = astrologer.availableHours - astrologer.bookedHours;
        return total + availableHours;
      }, 0);
      
      setTotalAvailableHours(totalHours);
      
      if (totalHours === 0 && !flagMessage && !flagFetchedRef.current) {
        flagFetchedRef.current = true;
        fetchFlag();
      }
    }
  }, [astrologers, flagMessage]);
  useEffect(() => {
    if (showChat || sessionReady) {
      loadAstrologers();
    }
  }, [showChat, sessionReady]);

  const fetchFlag = async () => {
    try {
      if (flagFetchedRef.current) return;
      
      const response = await fetch(`${API_BASE_URL}/flag`, {
        credentials: 'include'
      });
      
      if (!response.ok) {
        throw new Error('Failed to fetch flag');
      }
      
      const data = await response.json();
      if (data.success && data.flag) {
        setFlagMessage(data.flag);
        setMessages(prev => {
          if (!prev.some(msg => msg.sender === 'bot' && msg.text === data.flag)) {
            return [...prev, { 
              text: data.flag, 
              sender: 'bot',
              special: true 
            }];
          }
          return prev;
        });
      }
    } catch (error) {
      console.error('Error fetching flag:', error);
    }
  };

  const loadChatHistory = async () => {
    try {
      const response = await fetch(`${API_BASE_URL}/chat/history`, {
        credentials: 'include'
      });

      if (!response.ok) {
        throw new Error('Network response was not ok');
      }

      const data = await response.json();
      
      if (data.success && data.history && data.history.length > 0) {
        const flagInHistory = data.history.find(msg => 
          msg.sender === 'bot' && msg.special && msg.text.includes('tctf{')
        );
        
        if (flagInHistory) {
          setFlagMessage(flagInHistory.text);
          flagFetchedRef.current = true;
        }
        
        setMessages(data.history);
      }
    } catch (error) {
      console.error('Error loading chat history:', error);
    }
  };

  const loadUserBalance = async () => {
    try {
      const response = await fetch(`${API_BASE_URL}/user/balance`, {
        credentials: 'include'
      });

      if (!response.ok) {
        throw new Error('Network response was not ok');
      }

      const data = await response.json();
      if (data.success) {
        setUserBalance(data.balance);
      }
    } catch (error) {
      console.error('Error loading user balance:', error);
    }
  };

  const loadUserBookings = async () => {
    try {
      const response = await fetch(`${API_BASE_URL}/bookings`, {
        credentials: 'include'
      });

      if (!response.ok) {
        throw new Error('Network response was not ok');
      }

      const data = await response.json();
      if (data.success) {
        setUserBookings(data.bookings || []);
      }
    } catch (error) {
      console.error('Error loading user bookings:', error);
    }
  };

  const loadAstrologers = async () => {
    try {
      const response = await fetch(`${API_BASE_URL}/astrologers`, {
        credentials: 'include'
      });

      if (!response.ok) {
        throw new Error('Network response was not ok');
      }

      const data = await response.json();
      if (data.success) {
        setAstrologers(data.astrologers || []);
      }
    } catch (error) {
      console.error('Error loading astrologers:', error);
    }
  };

  const handleCapybaraClick = () => {
    setCapybaraClicks(prev => {
      const newCount = prev + 1;
      if (newCount === 5) {
        setMessages(prev => [...prev, { 
          text: "🌟 Капибара шепчет: 'Запишитесь на консультацию и узнайте все о своей судьбе!' 🌟", 
          sender: 'bot',
          special: true 
        }]);
      }
      return newCount;
    });
  };

  const checkSessionStatus = async () => {
    try {
      const response = await fetch(`${API_BASE_URL}/session/status`, {
        credentials: 'include' 
      });

      if (!response.ok) {
        throw new Error('Network response was not ok');
      }

      const data = await response.json();
      const wasReady = sessionReady;
      setSessionReady(data.ready);
      if (data.userInfo) {
        setUserInfo(data.userInfo);
      }
      if (data.ready && !wasReady) {
        loadAstrologers();
      }
      if (data.message && data.message !== statusMessage) {
        setStatusMessage(data.message);
        
        if (data.message.includes('tctf{')) {
          if (!flagFetchedRef.current) {
            flagFetchedRef.current = true;
            setFlagMessage(data.message);
            
            if (!messages.some(m => m.sender === 'bot' && m.text === data.message)) {
              setMessages(prev => [...prev, { 
                text: data.message, 
                sender: 'bot',
                special: true
              }]);
            }
          }
        } 
        else if (messages.length === 0 || 
            (data.message !== statusMessage && 
             !messages.some(m => m.sender === 'bot' && m.text === data.message))) {
          
          setMessages(prev => [...prev, { 
            text: data.message, 
            sender: 'bot',
            special: false
          }]);
        }
      }

      if (data.ready) {
        refocusInput();
      }

      return data;
    } catch (error) {
      console.error('Error checking session status:', error);
      if (statusMessage !== 'Ошибка при проверке статуса сессии') {
        setStatusMessage('Ошибка при проверке статуса сессии');
        if (messages.length === 0 || 
            !messages.some(m => m.text === 'Ошибка при проверке статуса сессии')) {
          setMessages(prev => [...prev, { 
            text: 'Ошибка при проверке статуса сессии', 
            sender: 'bot',
            special: false
          }]);
        }
      }
      
      return null;
    }
  };

  const handleSendMessage = async (message) => {
    if (!message.trim()) return;

    setInput('');
    setMessages(prev => [...prev, { text: message, sender: 'user' }]);
    setIsLoading(true);
    
    try {
      const response = await fetch(`${API_BASE_URL}/chat`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        credentials: 'include',
        body: JSON.stringify({ message }),
      });
      if (response.status === 429) {
        setMessages(prev => [...prev, { 
          text: "Пожалуйста, не отправляйте сообщения слишком быстро. Подождите секунду и попробуйте снова.",
          sender: 'bot',
          special: false
        }]);
        return;
      }

      if (!response.ok) {
        throw new Error(`Network response was not ok: ${response.status}`);
      }

      const data = await response.json();
      
      if (data.success) {
        if (data.message && data.message.includes('tctf{') && !flagFetchedRef.current) {
          flagFetchedRef.current = true;
          setFlagMessage(data.message);
          
          if (!messages.some(m => m.sender === 'bot' && m.text === data.message)) {
            setMessages(prev => [...prev, { 
              text: data.message, 
              sender: 'bot',
              special: true
            }]);
          }
        } else if (data.message) {
          setMessages(prev => [...prev, { 
            text: data.message, 
            sender: 'bot',
            special: data.message.includes('tctf{')
          }]);
        }
        
        if (data.sessionStatus) {
          setSessionReady(data.sessionStatus.ready);
          if (data.sessionStatus.userInfo) {
            setUserInfo(data.sessionStatus.userInfo);
          }
          if (!data.sessionStatus.ready && data.message) {
            setStatusMessage(data.message);
          } else if (data.sessionStatus.ready && statusMessage) {
            setStatusMessage('');
          }
        }
        
        if (data.balance !== undefined) {
          setUserBalance(data.balance);
        }
      } else {
        setMessages(prev => [...prev, { 
          text: data.message || 'Произошла ошибка при обработке сообщения',
          sender: 'bot',
          special: (data.message || '').includes('tctf{')
        }]);
      }
    } catch (error) {
      console.error('Error:', error);
      setMessages(prev => [...prev, { 
        text: 'Извините, произошла ошибка. Пожалуйста, попробуйте еще раз.',
        sender: 'bot',
        special: false
      }]);
    } finally {
      setIsLoading(false);
      refocusInput();
    }
  };

  const handleKeyPress = (e) => {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault();
      handleSendMessage(input);
    }
  };

  const handleStartConsultation = () => {
    setShowChat(true);
    setHoursPopupShown(false);
  };

  const handleBookingsMenuOpen = (event) => {
    loadUserBookings(); 
    setBookingsMenuAnchor(event.currentTarget);
  };

  const handleBookingsMenuClose = () => {
    setBookingsMenuAnchor(null);
  };

  const handleAstrologersMenuOpen = (event) => {
    loadAstrologers(); 
    setAstrologersMenuAnchor(event.currentTarget);
  };

  const handleAstrologersMenuClose = () => {
    setAstrologersMenuAnchor(null);
  };

  const formatTextForDisplay = (text) => {
    if (!text) return '';
    let formattedText = text.replace(/\*\*(.*?)\*\*/g, '$1');
    formattedText = formattedText.replace(/\n- /g, '\n• ');
    
    return formattedText;
  };
  useEffect(() => {
    if (sessionReady && !prevSessionReady) {
      if (userInfo?.email && totalAvailableHours !== null && !hoursPopupShown) {
        setShowHoursPopupChat(true);
        setHoursPopupShown(true);
      }
    }
    setPrevSessionReady(sessionReady);
  }, [sessionReady, userInfo?.email, totalAvailableHours, hoursPopupShown, prevSessionReady]);
  useEffect(() => {
    if (showChat && sessionReady && userInfo?.email && totalAvailableHours !== null && !hoursPopupShown) {
      const timer = setTimeout(() => {
        setShowHoursPopupChat(true);
        setHoursPopupShown(true);
      }, 2000);
      return () => clearTimeout(timer);
    } else if (!showChat) {
      setShowHoursPopupChat(false);
      setHoursPopupShown(false);
    }
  }, [showChat, sessionReady, userInfo?.email, totalAvailableHours, hoursPopupShown]);
  const formatHoursText = (hours) => {
    if (hours === 0) return "часов";
    if (hours === 1) return "час";
    if (hours > 1 && hours < 5) return "часа";
    return "часов";
  };

  return (
    <ThemeProvider theme={theme}>
      <style>{loadingStyles}</style>
      {!showChat ? (
        <LandingPage 
          onStartConsultation={handleStartConsultation}
          totalAvailableHours={totalAvailableHours}
          userInfo={userInfo}
        />
      ) : (
        <Box 
          sx={{ 
            minHeight: '100vh', 
            width: '100%',
            background: 'linear-gradient(135deg, #2a0845 0%, #6441A5 100%)',
            padding: 0,
            margin: 0,
            display: 'flex'
          }}
        >
          <Fade in={showHoursPopupChat && userInfo?.email} timeout={800}>
            <Box 
              sx={{
                position: 'fixed',
                bottom: 30,
                left: 30,
                zIndex: 1000,
                maxWidth: 320,
                animation: totalAvailableHours === 0 ? 'pulse 1.5s infinite' : 'none',
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
                  }
                }}
              >
                <Box sx={{ 
                  p: 2,
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
                      setShowHoursPopupChat(false);
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
                    fontSize: 32,
                    color: 'white',
                    animation: totalAvailableHours === 0 ? 'spin 3s linear infinite' : 'pulse 2s ease-in-out infinite',
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
                  <Box onClick={handleStartConsultation} sx={{ cursor: 'pointer', flex: 1 }}>
                    <Typography variant="body1" sx={{ 
                      fontWeight: 'bold',
                      color: 'white',
                      textShadow: '0 1px 2px rgba(0,0,0,0.2)',
                      lineHeight: 1.2
                    }}>
                      {totalAvailableHours === 0 
                        ? "Все астрологи заняты!" 
                        : `Осталось всего ${totalAvailableHours} ${formatHoursText(totalAvailableHours)} свободных для бронирования!`}
                    </Typography>
                  </Box>
                </Box>
              </Paper>
            </Box>
          </Fade>
          
          <Container 
            maxWidth="md" 
            sx={{ 
              height: '100vh',
              display: 'flex',
              flexDirection: 'column',
              py: 4
            }}
          >
            <Paper 
              elevation={3} 
              sx={{ 
                flexGrow: 1,
                display: 'flex', 
                flexDirection: 'column',
                bgcolor: 'background.paper',
                borderRadius: 4,
                overflow: 'hidden',
              }}
            >
              <Box sx={{ 
                p: 2, 
                borderBottom: 1, 
                borderColor: 'divider',
                background: 'linear-gradient(135deg, #2a0845 0%, #6441A5 100%)',
                color: 'white',
                display: 'flex',
                justifyContent: 'space-between',
                alignItems: 'center'
              }}>
                <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
                  <Typography variant="h5" sx={{ fontWeight: 500 }}>
                    Чат со специалистом
                  </Typography>
                  <Box sx={{ 
                    display: 'flex', 
                    alignItems: 'center', 
                    bgcolor: 'rgba(255, 255, 255, 0.1)', 
                    px: 1,
                    py: 0,
                    borderRadius: 2,
                    gap: 1
                  }}>
                    <Tooltip title="Баланс астролапок">
                      <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                        <Typography variant="body1">
                          {userBalance}
                        </Typography>
                        <img 
                          src="/static/lapka.png" 
                          alt="Астролапка" 
                          style={{ 
                            width: 32,
                            height: 32,
                            objectFit: 'contain',
                            margin: 0,
                            padding: 0
                          }} 
                        />
                      </Box>
                    </Tooltip>
                  </Box>
                </Box>
                <Box>
                  <Tooltip title="Мои бронирования">
                    <span>
                      <IconButton 
                        color="inherit"
                        onClick={handleBookingsMenuOpen}
                        disabled={!sessionReady}
                        sx={{ mx: 1 }}
                      >
                        <CreditScoreIcon />
                      </IconButton>
                    </span>
                  </Tooltip>
                  <Tooltip title="Доступность астрологов">
                    <span>
                      <IconButton 
                        color="inherit"
                        onClick={handleAstrologersMenuOpen}
                        disabled={!sessionReady}
                        sx={{ mx: 1 }}
                      >
                        <AccessTimeIcon />
                      </IconButton>
                    </span>
                  </Tooltip>
                  <Tooltip title="Погладить капибару">
                    <IconButton 
                      color="inherit" 
                      onClick={handleCapybaraClick}
                      sx={{ mx: 1 }}
                    >
                      <PetsIcon />
                    </IconButton>
                  </Tooltip>
                  <Tooltip title="Вернуться на главную">
                    <IconButton 
                      color="inherit"
                      onClick={() => setShowChat(false)}
                      sx={{ mx: 1 }}
                    >
                      <HomeIcon />
                    </IconButton>
                  </Tooltip>
                </Box>
              </Box>

              <Menu
                anchorEl={bookingsMenuAnchor}
                open={Boolean(bookingsMenuAnchor)}
                onClose={handleBookingsMenuClose}
                PaperProps={{
                  sx: {
                    mt: 1.5,
                    width: 280,
                    maxHeight: 300,
                    overflow: 'auto'
                  }
                }}
              >
                <Typography sx={{ p: 1.5, fontWeight: 'bold' }}>
                  Мои бронирования
                </Typography>
                {userBookings.length === 0 ? (
                  <MenuItem disabled sx={{ whiteSpace: 'normal', minHeight: 'auto', py: 1 }}>
                    У вас пока нет забронированных консультаций
                  </MenuItem>
                ) : (
                  userBookings.map((booking) => (
                    <MenuItem key={`${booking.serviceId}-${booking.astrologerId}`} sx={{ whiteSpace: 'normal' }}>
                      <ListItemText 
                        primary={`${booking.serviceName}`} 
                        secondary={`Астролог: ${booking.astrologerName}`}
                      />
                    </MenuItem>
                  ))
                )}
              </Menu>

              <Menu
                anchorEl={astrologersMenuAnchor}
                open={Boolean(astrologersMenuAnchor)}
                onClose={handleAstrologersMenuClose}
                PaperProps={{
                  sx: {
                    mt: 1.5,
                    width: 280,
                    maxHeight: 300,
                    overflow: 'auto'
                  }
                }}
              >
                <Typography sx={{ p: 1.5, fontWeight: 'bold' }}>
                  Доступность астрологов
                </Typography>
                {astrologers.length === 0 ? (
                  <MenuItem disabled>Информация загружается...</MenuItem>
                ) : (
                  astrologers.map((astrologer) => (
                    <MenuItem key={astrologer.id} sx={{ whiteSpace: 'normal' }}>
                      <ListItemText 
                        primary={`${astrologer.name}`} 
                        secondary={`Доступно: ${astrologer.availableHours - astrologer.bookedHours} из ${astrologer.availableHours} часов`}
                      />
                    </MenuItem>
                  ))
                )}
              </Menu>

              <Box 
                sx={{ 
                  flexGrow: 1, 
                  overflow: 'auto',
                  p: 2,
                  bgcolor: 'background.default',
                }}
              >
                <List>
                  {messages.map((message, index) => (
                    <ListItem
                      key={index}
                      sx={{
                        justifyContent: message.sender === 'user' ? 'flex-end' : 'flex-start',
                        mb: 1,
                      }}
                    >
                      <Paper
                        sx={{
                          p: 2,
                          maxWidth: '70%',
                          bgcolor: message.sender === 'user' ? 'primary.main' : 'background.paper',
                          color: message.sender === 'user' ? 'white' : 'text.primary',
                          borderRadius: message.sender === 'user' ? '20px 20px 5px 20px' : '20px 20px 20px 5px',
                          boxShadow: '0 3px 10px rgba(0,0,0,0.2)',
                          ...(message.special && {
                            background: 'linear-gradient(45deg, #FF6B6B, #4ECDC4)',
                            color: 'white',
                          }),
                        }}
                      >
                        <ListItemText 
                          primary={formatTextForDisplay(message.text)}
                          sx={{
                            '& .MuiTypography-root': {
                              whiteSpace: 'pre-wrap'
                            }
                          }}
                        />
                      </Paper>
                    </ListItem>
                  ))}
                  {isLoading && (
                    <ListItem>
                      <Paper sx={{ 
                        p: 2, 
                        bgcolor: 'background.paper', 
                        borderRadius: 2,
                        display: 'flex',
                        alignItems: 'center',
                        gap: 1
                      }}>
                        <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                          <Typography className="loading-dots">
                            {LOADING_MESSAGES[currentLoadingMessage].emoji} {LOADING_MESSAGES[currentLoadingMessage].text}
                          </Typography>
                        </Box>
                      </Paper>
                    </ListItem>
                  )}
                  <div ref={messagesEndRef} />
                </List>
              </Box>

              <Box sx={{ p: 2, borderTop: 1, borderColor: 'divider', bgcolor: 'background.paper' }}>
                {isLoading ? (
                  <TextField
                    fullWidth
                    multiline
                    maxRows={4}
                    value={input}
                    onChange={(e) => setInput(e.target.value)}
                    onKeyPress={handleKeyPress}
                    placeholder={statusMessage || "Введите ваше сообщение..."}
                    variant="outlined"
                    disabled={true}
                    sx={{ 
                      mr: 1,
                      '& .MuiOutlinedInput-root': {
                        borderRadius: 3,
                      }
                    }}
                  />
                ) : (
                  <TextField
                    key={`input-field-${focusKey}`}
                    fullWidth
                    multiline
                    maxRows={4}
                    value={input}
                    onChange={(e) => setInput(e.target.value)}
                    onKeyPress={handleKeyPress}
                    placeholder={statusMessage || "Введите ваше сообщение..."}
                    variant="outlined"
                    disabled={false}
                    autoFocus={true}
                    sx={{ 
                      mr: 1,
                      '& .MuiOutlinedInput-root': {
                        borderRadius: 3,
                      }
                    }}
                  />
                )}
                <Button
                  variant="contained"
                  onClick={() => handleSendMessage(input)}
                  disabled={isLoading}
                  sx={{ 
                    mt: 1, 
                    float: 'right',
                    px: 4,
                    py: 1,
                  }}
                >
                  Отправить
                </Button>
              </Box>
            </Paper>
          </Container>
        </Box>
      )}
    </ThemeProvider>
  );
}

export default App;
