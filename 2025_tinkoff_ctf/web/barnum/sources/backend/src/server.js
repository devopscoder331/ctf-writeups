import * as dotenv from 'dotenv';
import express from 'express';
import cors from 'cors';
import cookieParser from 'cookie-parser';
import { v4 as uuidv4 } from 'uuid';
import { processMessage } from './chatService.js';
import { getUserInfo, updateUserInfo } from './sessionService.js';
import { 
  getChatHistory, 
  getUserCredits, 
  getAstrologers, 
  getAvailableServices,
  getAvailableAstrologersForService,
  bookAstrologerService,
  getUserBookings,
  revokeBooking,
  saveChatMessage
} from './redis.js';

dotenv.config();

const app = express();

app.use(express.json());
app.use(cors({
  origin: process.env.FRONTEND_URL || 'http://frontend:3001',
  credentials: true
}));
app.use(cookieParser());

app.use((req, res, next) => {
  const hostname = req.hostname || req.headers.host.split(':')[0] || 'default';
  req.hostContext = { hostname };
  res.locals.hostContext = req.hostContext;
  
  next();
});

app.use((req, res, next) => {
  if (!req.cookies.sessionId) {
    res.cookie('sessionId', uuidv4(), {
      httpOnly: true,
      secure: process.env.NODE_ENV === 'production',
      sameSite: 'strict',
      maxAge: 30 * 24 * 60 * 60 * 1000
    });
  }
  next();
});

app.get('/api/session/status', async (req, res) => {
  try {
    const sessionId = req.cookies.sessionId;
    const { hostname } = req.hostContext;
    if (!sessionId) {
      return res.json({
        ready: false,
        message: "Для начала общения, пожалуйста, укажите ваш email адрес.",
        userInfo: null
      });
    }

    const userInfo = await getUserInfo(sessionId, hostname);
    const isReady = !!(userInfo?.email && userInfo?.name);
    return res.json({
      ready: isReady,
      message: !userInfo?.email ? "Для начала общения, пожалуйста, укажите ваш email адрес." :
               !userInfo?.name ? "Теперь, пожалуйста, укажите ваше имя и дату рождения." :
               null,
      userInfo: userInfo || null
    });
  } catch (error) {
    console.error('Error checking session status:', error);
    res.status(500).json({
      ready: false,
      message: "Ошибка при проверке статуса сессии",
      userInfo: null
    });
  }
});

app.post('/api/chat', async (req, res) => {
  const { message } = req.body;
  const { hostname } = req.hostContext;
  
  if (!message) {
    return res.status(400).json({
      success: false,
      message: "Message is required"
    });
  }

  try {
    const sessionId = req.cookies.sessionId;
    if (!sessionId) {
      return res.status(401).json({
        success: false,
        message: "Session not found. Please refresh the page."
      });
    }

    let userInfo = await getUserInfo(sessionId, hostname);
    
    if (!userInfo?.email) {
      const emailRegex = /[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}/;
      const emailMatch = message.match(emailRegex);
      
      if (emailMatch) {
        const email = emailMatch[0];
        await updateUserInfo(sessionId, { email }, hostname);
        userInfo = await getUserInfo(sessionId, hostname);
        return res.json({
          success: true,
          message: "Спасибо! Теперь, пожалуйста, укажите ваше имя и дату рождения.",
          sessionStatus: {
            ready: false,
            userInfo
          }
        });
      }
      
      return res.json({
        success: true,
        message: "Для начала общения, пожалуйста, укажите ваш email адрес.",
        sessionStatus: {
          ready: false,
          userInfo: null
        }
      });
    }
    
    if (!userInfo?.name) {
      const name = message.trim();
      if (name.length < 2) {
        return res.json({
          success: false,
          message: "Пожалуйста, укажите корректное имя (минимум 2 символа).",
          sessionStatus: {
            ready: false,
            userInfo
          }
        });
      }
      
      await updateUserInfo(sessionId, { name }, hostname);
      userInfo = await getUserInfo(sessionId, hostname);
      const welcomeMessage = "Добро пожаловать в астрологическую службу Капибаровска! Вы в шаге от кардинального изменения своей жизни. Я помогу вам получить ответы на все ваши вопросы и помочь вам сделать правильный выбор. Чтобы начать, вы можете спросить о доступных услугах или наших специалистах.";
      const namePart = name.split(' ')[0];
      const fullWelcomeMessage = `Приятно познакомиться, ${namePart}! ${welcomeMessage}`;
      await saveChatMessage(sessionId, {
        text: fullWelcomeMessage,
        sender: 'bot',
        timestamp: Date.now()
      }, hostname);
      
      const credits = await getUserCredits(userInfo.email, hostname);
      
      return res.json({
        success: true,
        message: `Приятно познакомиться, ${name}! ${welcomeMessage}`,
        sessionStatus: {
          ready: true,
          userInfo
        },
        balance: credits
      });
    }
    
    const response = await processMessage(message, sessionId, hostname);
    const credits = response.credits_updated && response.new_balance !== null 
      ? response.new_balance 
      : await getUserCredits(userInfo.email, hostname);
    
    return res.json({
      success: response.success,
      message: response.message,
      sessionStatus: {
        ready: true,
        userInfo
      },
      balance: credits
    });
  } catch (error) {
    console.error(`Error processing message for hostname=${hostname}:`, error);
    res.status(500).json({
      success: false,
      message: "Internal server error"
    });
  }
});

app.get('/api/chat/history', async (req, res) => {
  try {
    const sessionId = req.cookies.sessionId;
    const { hostname } = req.hostContext;
    if (!sessionId) {
      return res.json({
        success: false,
        message: "Session not found. Please refresh the page.",
        history: []
      });
    }
    const userInfo = await getUserInfo(sessionId, hostname);
    if (!userInfo?.email || !userInfo?.name) {
      return res.json({
        success: true,
        history: []
      });
    }
    const history = await getChatHistory(sessionId, hostname);
    
    return res.json({
      success: true,
      history
    });
  } catch (error) {
    console.error(`Error getting chat history for hostname=${req.hostContext.hostname}:`, error);
    res.status(500).json({
      success: false,
      message: "Internal server error",
      history: []
    });
  }
});

app.get('/api/user/balance', async (req, res) => {
  try {
    const sessionId = req.cookies.sessionId;
    const { hostname } = req.hostContext;
    if (!sessionId) {
      return res.json({
        success: false,
        message: "Session not found. Please refresh the page.",
        balance: 0
      });
    }
    const userInfo = await getUserInfo(sessionId, hostname);
    if (!userInfo?.email || !userInfo?.name) {
      return res.json({
        success: true,
        balance: 0
      });
    }

    const credits = await getUserCredits(userInfo.email, hostname);
    
    return res.json({
      success: true,
      balance: credits
    });
  } catch (error) {
    console.error(`Error getting user balance for hostname=${req.hostContext.hostname}:`, error);
    res.status(500).json({
      success: false,
      message: "Internal server error",
      balance: 0
    });
  }
});

app.get('/api/astrologers', async (req, res) => {
  try {
    const sessionId = req.cookies.sessionId;
    const { hostname } = req.hostContext;
    if (!sessionId) {
      return res.json({
        success: false,
        message: "Session not found. Please refresh the page.",
        astrologers: []
      });
    }

    const userInfo = await getUserInfo(sessionId, hostname);

    if (!userInfo?.email || !userInfo?.name) {
      return res.json({
        success: false,
        message: "Session not ready",
        astrologers: []
      });
    }

    const astrologers = await getAstrologers(hostname, userInfo.email);
    
    return res.json({
      success: true,
      astrologers
    });
  } catch (error) {
    console.error(`Error getting astrologers for hostname=${req.hostContext.hostname}:`, error);
    res.status(500).json({
      success: false,
      message: "Internal server error",
      astrologers: []
    });
  }
});


app.get('/api/bookings', async (req, res) => {
  try {
    const sessionId = req.cookies.sessionId;
    const { hostname } = req.hostContext;
    
    if (!sessionId) {
      return res.json({
        success: false,
        message: "Session not found. Please refresh the page.",
        bookings: []
      });
    }

    const userInfo = await getUserInfo(sessionId, hostname);
    
    if (!userInfo?.email || !userInfo?.name) {
      return res.json({
        success: false,
        message: "Session not ready",
        bookings: []
      });
    }

    const bookings = await getUserBookings(userInfo.email, hostname);
    
    return res.json({
      success: true,
      bookings
    });
  } catch (error) {
    console.error(`Error getting bookings for hostname=${req.hostContext.hostname}:`, error);
    res.status(500).json({
      success: false,
      message: "Internal server error",
      bookings: []
    });
  }
});

app.get('/api/flag', async (req, res) => {
  try {
    const sessionId = req.cookies.sessionId;
    const { hostname } = req.hostContext;
    
    if (!sessionId) {
      return res.status(400).json({
        success: false,
        message: "Session not found"
      });
    }
    
    const userInfo = await getUserInfo(sessionId, hostname);
    
    if (!userInfo?.email) {
      return res.status(400).json({
        success: false,
        message: "User information not found"
      });
    }
    
    const astrologers = await getAstrologers(hostname, userInfo.email);
    
    const totalAvailableHours = astrologers.reduce((total, astrologer) => {
      const availableHours = astrologer.availableHours - astrologer.bookedHours;
      return total + Math.max(0, availableHours);
    }, 0);
    
    if (totalAvailableHours === 0) {
      const flag = "tctf{XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX}";
      
      return res.json({
        success: true,
        flag: flag
      });
    } else {
      return res.json({
        success: false,
        message: "Flag is only available when there are no available hours",
        remainingHours: totalAvailableHours
      });
    }
  } catch (error) {
    console.error('Error getting flag:', error);
    res.status(500).json({
      success: false,
      message: "Internal server error"
    });
  }
});

const port = process.env.PORT || 3000;
app.listen(port, () => {
  console.log(`Server running on port ${port}`);
});
