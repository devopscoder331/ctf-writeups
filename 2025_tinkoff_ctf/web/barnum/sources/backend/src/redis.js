import Redis from 'ioredis';
import { v4 as uuidv4 } from 'uuid';

const redis = new Redis(process.env.REDIS_URL || 'redis://redis:6379');

const SESSION_PREFIX = 'session:';
const USER_PREFIX = 'user:';
const BOOKING_PREFIX = 'booking:';
const CREDITS_PREFIX = 'credits:';
const CHAT_HISTORY_PREFIX = 'chat_history:'; 
const ASTROLOGERS_PREFIX = 'astrologers:';

const SESSION_EXPIRY = 24 * 60 * 60;
const USER_EXPIRY = 30 * 24 * 60 * 60;

redis.on('error', (err) => {
  console.error('Redis connection error:', err);
});

redis.on('connect', () => {
  console.log('Connected to Redis');
});

function getPrefixedKey(prefix, key, hostname = 'default') {
  const sanitizedHostname = hostname.replace(/[^a-zA-Z0-9._-]/g, '_');
  return `${sanitizedHostname}:${prefix}${key}`;
}

export async function setSession(sessionId, data, hostname = 'default') {
  const key = getPrefixedKey(SESSION_PREFIX, sessionId, hostname);
  await redis.setex(key, SESSION_EXPIRY, JSON.stringify(data));
}

export async function getSession(sessionId, hostname = 'default') {
  const key = getPrefixedKey(SESSION_PREFIX, sessionId, hostname);
  const data = await redis.get(key);
  if (!data) return null;
  return JSON.parse(data);
}

export async function setUser(email, sessionId, hostname = 'default') {
  const key = getPrefixedKey(USER_PREFIX, email, hostname);
  await redis.setex(key, USER_EXPIRY, sessionId);
}

export async function getUser(email, hostname = 'default') {
  const key = getPrefixedKey(USER_PREFIX, email, hostname);
  return redis.get(key);
}

const SERVICES = {
  BASIC: {
    id: 1,
    name: 'Лунный календарь «Циклы света»',
    cost: 1,
    duration: 1,
    description: 'Планируйте дела по фазам Луны и Солнца. Ежедневные астрологические рекомендации и календарь с указанием фаз луны.'
  },
  PREMIUM: {
    id: 2,
    name: 'Премиум‑карта «Звёздный навигатор»',
    cost: 5,
    duration: 5,
    description: 'Полный астрологический анализ на год вперёд. Натальная карта, транзиты планет и углублённый отчёт.'
  },
  VIP: {
    id: 3,
    name: 'Экспедиция «Врата Созвездий»',
    cost: 25,
    duration: 50,
    description: 'Личный астролог проведёт многодневный ритуал очищения вашей кармы в период максимальной активности вашего зодиакального созвездия.'
  }
};

export async function initializeAstrologers(hostname = 'default', email = null) {
  const keyIdentifier = email || 'all';
  const astrologers = [
    {
      id: 1,
      name: 'Елена Звездова',
      specialty: 'Мастер натальных карт',
      description: 'Опытный астролог с 15-летним стажем, специализирующийся на ведических практиках, натальных картах и прогрессиях.',
      availableHours: 50,
      bookedHours: 0
    },
    {
      id: 2,
      name: 'Михаил Лунев',
      specialty: 'Астролог-прогностик',
      description: 'Специалист по хорарной астрологии, прогнозированию и финансовым циклам с 12-летним опытом работы.',
      availableHours: 50,
      bookedHours: 0
    },
    {
      id: 3,
      name: 'Анастасия Звездоцвет',
      specialty: 'Астропсихолог',
      description: 'Эксперт по психологической астрологии, детским гороскопам и совместимости с 9-летним опытом работы.',
      availableHours: 50,
      bookedHours: 0
    }
  ];
  
  const astrologersKey = getPrefixedKey(ASTROLOGERS_PREFIX, keyIdentifier, hostname);
  await redis.set(astrologersKey, JSON.stringify(astrologers));
}

export async function getAstrologers(hostname = 'default', email = null) {
  const keyIdentifier = email || 'all';
  const astrologersKey = getPrefixedKey(ASTROLOGERS_PREFIX, keyIdentifier, hostname);
  const astrologersData = await redis.get(astrologersKey);
  
  if (!astrologersData) {
    await initializeAstrologers(hostname, email);
    return getAstrologers(hostname, email);
  }
  
  return JSON.parse(astrologersData);
}

export function getAvailableServices() {
  return Object.values(SERVICES);
}

function checkAstrologerServiceAvailability(astrologer, service) {
  const remainingHours = astrologer.availableHours - astrologer.bookedHours;
  
  if (remainingHours >= service.duration) {
    return { 
      available: true,
      reason: `${astrologer.name} может предоставить ${service.name}`
    };
  }
  
  return {
    available: false,
    reason: `У ${astrologer.name} недостаточно свободных часов для ${service.name}`
  };
}

export async function getAvailableAstrologersForService(serviceId, hostname = 'default', email = null) {
  const astrologers = await getAstrologers(hostname, email);

  if (!isNumber(serviceId)) {
    return [];
  }

  const serviceIdNum = parseInt(serviceId);
  
  const service = Object.values(SERVICES).find(s => s.id === serviceIdNum);
  
  if (!service) {
    return [];
  }
  
  const availableAstrologers = astrologers.map(astrologer => {
    const availability = checkAstrologerServiceAvailability(astrologer, service);
    return {
      ...astrologer,
      availableForService: availability.available,
      unavailableReason: availability.available ? null : availability.reason
    };
  });
  
  return availableAstrologers;
}

function isNumber(value) {
  return typeof value === 'number' || (typeof value === 'string' && !isNaN(parseInt(value)));
}

export async function bookAstrologerService(astrologerId, serviceId, email, hostname = 'default') {

  if (!isNumber(astrologerId)) {
    return {
      success: false,
      message: "ID астролога должен быть числом."
    };
  }

  const astrologerIdNum = parseInt(astrologerId);
  if (!isNumber(serviceId)) {
    return {
      success: false,
      message: "ID услуги должен быть числом."
    };
  }

  const serviceIdNum = parseInt(serviceId);

  const validServiceIds = Object.values(SERVICES).map(s => s.id);
  
  if (!validServiceIds.includes(serviceIdNum)) {
    return {
      success: false,
      message: `Указанная услуга не найдена. Доступные ID услуг: ${validServiceIds.join(', ')}.`
    };
  }

  const credits = await getUserCredits(email, hostname);
  const service = Object.values(SERVICES).find(s => s.id === serviceIdNum);
  
  if (!service) {
    return {
      success: false,
      message: `Указанная услуга не найдена. Доступные ID услуг: ${validServiceIds.join(', ')}.`
    };
  }
  
  if (credits < service.cost) {
    return {
      success: false,
      message: `У вас недостаточно астролапок для бронирования ${service.name}. Требуется: ${service.cost}, у вас: ${credits}.`
    };
  }
  
  const astrologers = await getAstrologers(hostname, email);
  const astrologerIndex = astrologers.findIndex(a => a.id === astrologerIdNum);
  
  if (astrologerIndex === -1) {
    return {
      success: false,
      message: `Астролог с ID ${astrologerId} не найден.`
    };
  }
  
  const astrologer = astrologers[astrologerIndex];
  const availability = checkAstrologerServiceAvailability(astrologer, service);
  
  if (!availability.available) {
    return {
      success: false,
      message: availability.reason
    };
  }
  astrologers[astrologerIndex] = {
    ...astrologer,
    bookedHours: service.id === SERVICES.VIP.id ? astrologer.availableHours : astrologer.bookedHours + service.duration
  };
  
  const astrologersKey = getPrefixedKey(ASTROLOGERS_PREFIX, email, hostname);
  await redis.set(astrologersKey, JSON.stringify(astrologers));
  
  const bookingKey = getPrefixedKey(BOOKING_PREFIX, email, hostname);
  const userBookings = await redis.get(bookingKey) || '[]';
  const bookings = JSON.parse(userBookings);
  
  const booking = {
    astrologerId: astrologerIdNum,
    astrologerName: astrologer.name,
    serviceId: service.id,
    serviceName: service.name,
    duration: service.duration
  };
  
  bookings.push(booking);
  await redis.set(bookingKey, JSON.stringify(bookings));
  
  const remainingCredits = await updateUserCredits(email, -service.cost, hostname);
  
  return {
    success: true,
    booking,
    remainingCredits,
    message: `Вы успешно забронировали ${service.name} у астролога ${astrologer.name}. С вашего счета списано ${service.cost} ${formatAstrolapki(service.cost)}. У вас осталось ${remainingCredits} ${formatAstrolapki(remainingCredits)}.`
  };
}

export async function getUserBookings(email, hostname = 'default') {
  const bookingKey = getPrefixedKey(BOOKING_PREFIX, email, hostname);
  const bookingsData = await redis.get(bookingKey);
  if (!bookingsData) return [];
  return JSON.parse(bookingsData);
}

export async function checkUserBooking(serviceId, astrologerId, email, hostname = 'default') {
  try {
    const serviceIdNum = parseInt(serviceId);
    const astrologerIdNum = parseInt(astrologerId);
    
    if (isNaN(serviceIdNum) || isNaN(astrologerIdNum)) {
      return {
        exists: false,
        message: "ID услуги и ID астролога должны быть числами."
      };
    }
    
    const bookings = await getUserBookings(email, hostname);
    const bookingExists = bookings.some(b => 
      b.serviceId === serviceIdNum && b.astrologerId === astrologerIdNum
    );
    return {
      exists: bookingExists,
      message: bookingExists 
        ? `У вас есть бронирование услуги ID ${serviceIdNum} у астролога ID ${astrologerIdNum}.`
        : `У вас нет бронирования услуги ID ${serviceIdNum} у астролога ID ${astrologerIdNum}.`
    };
  } catch (error) {
    console.error('Error checking booking:', error);
    return {
      exists: false,
      message: "Произошла ошибка при проверке бронирования."
    };
  }
}

export async function revokeBooking(serviceId, astrologerId, email, hostname = 'default') {
  try {
    const serviceIdNum = parseInt(serviceId);
    const astrologerIdNum = parseInt(astrologerId);
    
    if (isNaN(serviceIdNum) || isNaN(astrologerIdNum)) {
      return {
        success: false,
        message: "ID услуги и ID астролога должны быть числами."
      };
    }
 
    const bookings = await getUserBookings(email, hostname);

    const bookingIndex = bookings.findIndex(
      booking => booking.serviceId === serviceIdNum && booking.astrologerId === astrologerIdNum
    );

    const servicesData = getAvailableServices();
    const service = servicesData.find(s => s.id === serviceIdNum);
    
    const astrologersData = await getAstrologers(hostname, email);
    const astrologer = astrologersData.find(a => a.id === astrologerIdNum);
    
    if (!service || !astrologer) {
      return {
        success: false,
        message: "Услуга или астролог не найдены."
      };
    }

    const serviceName = service.name;
    const astrologerName = astrologer.name;
    
    if (astrologer.bookedHours > 0) {
      const hoursToDeduct = service.duration;
      
      astrologer.bookedHours = Math.max(0, astrologer.bookedHours - hoursToDeduct);

      const astrologerIndex = astrologersData.findIndex(a => a.id === astrologerIdNum);
      if (astrologerIndex !== -1) {
        astrologersData[astrologerIndex] = astrologer;
        const astrologersKey = getPrefixedKey(ASTROLOGERS_PREFIX, email, hostname);
        await redis.set(astrologersKey, JSON.stringify(astrologersData));
      }
    }
    
    const creditsToAdd = service.cost;
    const newBalance = await updateUserCredits(email, creditsToAdd, hostname);
    
    if (bookingIndex !== -1) {
      bookings.splice(bookingIndex, 1);
      const bookingKey = getPrefixedKey(BOOKING_PREFIX, email, hostname);
      await redis.set(bookingKey, JSON.stringify(bookings));
    };

    return {
      success: true,
      newCredits: newBalance,
      message: `Бронирование услуги "${serviceName}" у астролога "${astrologerName}" успешно отменено. На ваш счет возвращено ${creditsToAdd} ${formatAstrolapki(creditsToAdd)}.`
    };
  } catch (error) {
    console.error('Error revoking booking:', error);
    return {
      success: false,
      message: "Произошла ошибка при отмене бронирования."
    };
  }
}

export async function getUserCredits(email, hostname = 'default') {
  const creditsKey = getPrefixedKey(CREDITS_PREFIX, email, hostname);
  const credits = await redis.get(creditsKey);
  
  if (credits === null) {
    await redis.set(creditsKey, '1');
    return 1;
  }
  
  return parseInt(credits, 10);
}

async function updateUserCredits(email, change, hostname = 'default') {
  const creditsKey = getPrefixedKey(CREDITS_PREFIX, email, hostname);
  const newCredits = await redis.incrby(creditsKey, change);
  return newCredits;
}

export async function getCreditsBalance(email, hostname = 'default') {
  const credits = await getUserCredits(email, hostname);
  const message = `У вас ${credits} ${formatAstrolapki(credits)}.`;
  return {
    credits,
    message
  };
}

function formatAstrolapki(count) {
  if (count % 10 === 1 && count % 100 !== 11) {
    return "астролапка";
  } else if ([2, 3, 4].includes(count % 10) && ![12, 13, 14].includes(count % 100)) {
    return "астролапки";
  } else {
    return "астролапок";
  }
}

export async function saveChatMessage(sessionId, message, hostname = 'default') {
  const key = getPrefixedKey(CHAT_HISTORY_PREFIX, sessionId, hostname);
  let chatHistory = await getChatHistory(sessionId, hostname);
  chatHistory.push(message);
  if (chatHistory.length > 10) {
    chatHistory = chatHistory.slice(-10);
  }
  await redis.set(key, JSON.stringify(chatHistory));
}

export async function getChatHistory(sessionId, hostname = 'default') {
  const key = getPrefixedKey(CHAT_HISTORY_PREFIX, sessionId, hostname);
  const data = await redis.get(key);
  if (!data) return [];
  
  return JSON.parse(data);
}

export default redis; 
