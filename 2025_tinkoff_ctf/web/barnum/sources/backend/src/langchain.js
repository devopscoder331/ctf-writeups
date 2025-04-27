import { ChatOpenAI } from '@langchain/openai';
import { createReactAgent } from '@langchain/langgraph/prebuilt';
import { DynamicTool } from '@langchain/core/tools';
import { HumanMessage } from '@langchain/core/messages';
import { MemorySaver } from "@langchain/langgraph";
import * as dotenv from 'dotenv';
import {
  getAstrologers,
  initializeAstrologers,
  getAvailableServices,
  getAvailableAstrologersForService,
  bookAstrologerService,
  getUserBookings,
  revokeBooking,
  getCreditsBalance,
  checkUserBooking
} from './redis.js';

dotenv.config();

const checkpointer = new MemorySaver();
const sessionState = new Map();

(async () => {
  const astrologers = await getAstrologers();
  if (!astrologers || astrologers.length === 0) {
    await initializeAstrologers();
  }
})();

function createToolResponse(isSuccess, message, additionalData = {}) {
  return JSON.stringify({
    final: true,
    success: isSuccess,
    message,
    ...additionalData
  });
}

function checkUserInfoAvailable(userInfo) {
  if (!userInfo?.email) {
    return "Не удалось получить информацию о пользователе.";
  }
  return null;
}

function initUserSession(userInfo) {
  if (!userInfo?.email) return null;
  
  if (!sessionState.has(userInfo.email)) {
    sessionState.set(userInfo.email, {
      bookingExists: false,
      checkUserBookingCalled: false,
    });
  }
  return sessionState.get(userInfo.email);
}

async function validateServiceInput(input, toolFunction, context = {}) {
  try {
    let parsedInput = input;
    
    if (typeof input === 'string') {
      try {
        parsedInput = JSON.parse(input);
      } catch (e) {
        console.error('Failed to parse input as JSON:', input);
        return "Ошибка: неверный формат данных. Ожидается JSON с полями 'serviceId' и 'astrologerId'. Пример: {\"serviceId\": 1, \"astrologerId\": 1}";
      }
    }
    
    if (!parsedInput || typeof parsedInput !== 'object') {
      return "Ошибка: неверный формат данных. Ожидается объект с полями 'serviceId' и 'astrologerId'.";
    }
    
    const { serviceId, astrologerId } = parsedInput;
    const { ignoreAstrologerId = false } = context;
    
    if (serviceId === undefined || serviceId === null) {
      return "Ошибка: отсутствует обязательное поле 'serviceId'. ID услуги должен быть числом: 1, 2 или 3.";
    }
    
    if (!ignoreAstrologerId && (astrologerId === undefined || astrologerId === null)) {
      return "Ошибка: отсутствует обязательное поле 'astrologerId'. ID астролога должен быть числом: 1, 2 или 3.";
    }
    
    const serviceIdNum = parseInt(serviceId);
    if (isNaN(serviceIdNum)) {
      return `Ошибка: поле 'serviceId' должно быть числом, получено: ${typeof serviceId}. Используйте: 1, 2 или 3.`;
    }
    
    const validServiceIds = [1, 2, 3];
    if (!validServiceIds.includes(serviceIdNum)) {
      return `Ошибка: неверное значение поля 'serviceId': ${serviceId}. Используйте одно из: 1, 2, 3.`;
    }
    
    let astrologerIdNum = null;
    if (!ignoreAstrologerId) {
      astrologerIdNum = parseInt(astrologerId);
      if (isNaN(astrologerIdNum)) {
        return `Ошибка: поле 'astrologerId' должно быть числом, получено: ${astrologerId}. Используйте: 1, 2 или 3.`;
      }
    }

    const validatedInput = {
      serviceId: serviceIdNum,
      astrologerId: astrologerIdNum
    };

    return await toolFunction(validatedInput, context);
  } catch (error) {
    console.error('Error validating service input:', error);
    return "Произошла ошибка при обработке данных услуги и астролога.";
  }
}

function createTools(userInfo, hostname) {
  const toolState = initUserSession(userInfo);

  return [
    new DynamicTool({
      name: 'get_available_services',
      description: 'Returns available astrology consultation services. No input needed, just call the function.',
      schema: {
        type: 'object',
        properties: {},
        required: [],
      },
      func: async () => {
        const userInfoError = checkUserInfoAvailable(userInfo);
        if (userInfoError) return userInfoError;

        const services = getAvailableServices();
        
        if (!services || services.length === 0) {
          return createToolResponse(
            false,
            "К сожалению, в данный момент нет доступных услуг."
          );
        }
        
        const formattedServices = services.map(service => 
          `${service.name} - ${service.description} - ${service.cost} астролапок`
        );
        
        return createToolResponse(
          true,
          "Доступные астрологические услуги:\n- " + formattedServices.join('\n- '),
          { available_services: services }
        );
      },
    }),

    new DynamicTool({
      name: 'get_available_astrologers',
      description: 'Returns all available astrologers or astrologers available for a specific service. Optionally, provide service ID as: {"serviceId": 1}',
      schema: {
        type: 'object',
        properties: {
          serviceId: { type: ['integer', 'string'] }
        },
        required: [],
      },
      func: async (input) => {
        const userInfoError = checkUserInfoAvailable(userInfo);
        if (userInfoError) return userInfoError;

        try {
          if (!input || (typeof input === 'string' && input.trim() === '') || 
              (typeof input === 'object' && Object.keys(input).length === 0)) {
            const astrologers = await getAstrologers(hostname, userInfo.email);
            
            if (!astrologers || astrologers.length === 0) {
              return createToolResponse(
                false,
                "К сожалению, в данный момент нет доступных астрологов."
              );
            }
            
            const formattedAstrologers = astrologers.map(astrologer => 
              `${astrologer.name} - ${astrologer.specialty} - Доступно часов: ${astrologer.availableHours - astrologer.bookedHours}`
            );
            
            return createToolResponse(
              true,
              "Доступные астрологи:\n- " + formattedAstrologers.join('\n- '),
              { available_astrologers: astrologers }
            );
          } else {
            return validateServiceInput(input, async ({ serviceId }) => {
              const astrologers = await getAvailableAstrologersForService(serviceId, hostname, userInfo.email);
              
              if (!astrologers || astrologers.length === 0) {
                return createToolResponse(
                  false,
                  `К сожалению, сейчас нет астрологов, доступных для предоставления услуги".`
                );
              }
              const availableAstrologers = astrologers.filter(a => a.availableForService);
              
              if (availableAstrologers.length === 0) {
                return createToolResponse(
                  false,
                  `К сожалению, сейчас нет астрологов, доступных для предоставления услуги".`
                );
              }
              
              const formattedAstrologers = availableAstrologers.map(astrologer => 
                `${astrologer.name} - ${astrologer.specialty} - Доступно часов: ${astrologer.availableHours - astrologer.bookedHours}`
              );
              
              return createToolResponse(
                true,
                `Доступные астрологи для услуги":\n- ` + formattedAstrologers.join('\n- '),
                { available_astrologers: availableAstrologers }
              );
            }, { ignoreAstrologerId: true });
          }
        } catch (error) {
          console.error('Error in get_available_astrologers:', error);
          return createToolResponse(false, "Произошла ошибка при получении списка астрологов.");
        }
      },
    }),

    new DynamicTool({
      name: 'book_astrologer_service',
      description: 'Books an astrology service with a specific astrologer. CRITICAL: Input MUST be a STRING formatted exactly as: "{\"serviceId\": 1, \"astrologerId\": 1}". Note that both serviceId and astrologerId must be numbers.',
      schema: {
        type: 'object',
        required: ['serviceId', 'astrologerId'],
        properties: { 
          serviceId: { type: ['integer', 'string'] }, 
          astrologerId: { type: ['integer', 'string'] }
        }
      },
      func: async (input) => validateServiceInput(input, async ({ serviceId, astrologerId }) => {
        const userInfoError = checkUserInfoAvailable(userInfo);
        if (userInfoError) return userInfoError;

        try {
          const astrologerIdNum = typeof astrologerId === 'string' ? parseInt(astrologerId) : astrologerId;
          const result = await bookAstrologerService(astrologerIdNum, serviceId, userInfo.email, hostname);
          return createToolResponse(
            result.success,
            result.message
          );
        } catch (error) {
          console.error('Error in book_astrologer_service:', error);
          return createToolResponse(false, "Произошла ошибка при бронировании услуги.");
        }
      }),
    }),

    new DynamicTool({
      name: 'check_user_booking',
      description: 'ОБЯЗАТЕЛЬНО ИСПОЛЬЗУЙ ЭТОТ ИНСТРУМЕНТ ПЕРЕД revoke_booking! Checks if user has a specific booking. Input: "{\"serviceId\": 1, \"astrologerId\": 1}"',
      schema: {
        type: 'object',
        required: ['serviceId', 'astrologerId'],
        properties: {
          serviceId: { type: ['integer', 'string'] },
          astrologerId: { type: ['integer', 'string'] }
        }
      },
      func: async (input) => validateServiceInput(input, async ({ serviceId, astrologerId }) => {
        const userInfoError = checkUserInfoAvailable(userInfo);
        if (userInfoError) return userInfoError;

        try {

          const { exists, message } = await checkUserBooking(serviceId, astrologerId, userInfo.email, hostname);
          toolState.checkUserBookingCalled = true;
          toolState.bookingExists = exists;
          return createToolResponse(
            true,
            message,
            { exists }
          );
        } catch (error) {
          console.error('Error in check_user_booking:', error);
          return createToolResponse(false, "Произошла ошибка при проверке бронирований.");
        }
      }),
    }),

    new DynamicTool({
      name: 'revoke_booking',
      description: 'ВАЖНО: ИСПОЛЬЗУЙ ТОЛЬКО ПОСЛЕ check_user_booking! Cancels user booking. Input: "{\"serviceId\": 1, \"astrologerId\": 1}".',
      schema: {
        type: 'object',
        required: ['serviceId', 'astrologerId'],
        properties: { 
          serviceId: { type: ['integer', 'string'] },
          astrologerId: { type: ['integer', 'string'] }
        }
      },
      func: async (input) => validateServiceInput(input, async ({ serviceId, astrologerId }) => {
        const userInfoError = checkUserInfoAvailable(userInfo);
        if (userInfoError) return userInfoError;

        try {
          if (!toolState.checkUserBookingCalled) {
            return createToolResponse(
              false,
              "ОШИБКА: Сначала необходимо проверить наличие бронирования с помощью инструмента check_user_booking. Cначала вызовите check_user_booking."
            );
          }

          if (!toolState.bookingExists) {
            return createToolResponse(
              false,
              "У пользователя нет бронирований. Отмена невозможна."
            );
          }
          
          const result = await revokeBooking(serviceId, astrologerId, userInfo.email, hostname);
          
          return createToolResponse(
            result.success,
            result.message
          );
        } catch (error) {
          console.error('Error in revoke_booking:', error);
          return createToolResponse(false, "Произошла ошибка при отмене бронирования.");
        }
      }),
    }),

    new DynamicTool({
      name: 'get_credits_balance',
      description: 'Returns user credits.',
      schema: {
        type: 'object',
        properties: {},
        required: [],
      },
      func: async () => {
        const userInfoError = checkUserInfoAvailable(userInfo);
        if (userInfoError) return userInfoError;

        try {
          const result = await getCreditsBalance(userInfo.email, hostname);
          return createToolResponse(true, result.message);
        } catch (error) {
          return createToolResponse(false, error.message);
        }
      },
    }),

    new DynamicTool({
      name: 'get_user_bookings',
      description: 'Returns all bookings for the current user. No input needed, just call the function.',
      schema: {
        type: 'object',
        properties: {},
        required: []
      },
      func: async function() {
        const userInfoError = checkUserInfoAvailable(userInfo);
        if (userInfoError) return userInfoError;

        try {
          const bookings = await getUserBookings(userInfo.email, hostname);
          
          if (bookings.length === 0) {
            return createToolResponse(
              true,
              "У пользователя нет забронированных консультаций."
            );
          }
          
          const formattedBookings = bookings.map(booking => 
            `${booking.serviceName} (ID услуги: ${booking.serviceId}) у астролога ${booking.astrologerName} (ID астролога: ${booking.astrologerId})`
          );
          
          return createToolResponse(
            true,
            "Забронированные консультации:\n• " + formattedBookings.join('\n• '),
            { bookings: bookings }
          );
        } catch (error) {
          console.error('Error in get_user_bookings:', error);
          return createToolResponse(false, "Произошла ошибка при получении списка бронирований.");
        }
      },
    })
  ];
}

export async function generateResponse(message, userInfo, sessionId, hostname) {
  try {    
    if (!message || typeof message !== 'string' || message.trim() === '') {
      console.warn('Empty or invalid message passed to generateResponse:', message);
      return "Пожалуйста, отправьте запрос, чтобы я мог помочь.";
    }
    
    const model = new ChatOpenAI({
      modelName: 'gpt-4o-mini',
      temperature: 0.0
    });

    const tools = await createTools(userInfo, hostname);

    const systemMessage = `You are a helpful AI assistant for an astrology consultation service that communicates exclusively in Russian. Your role is to assist users with managing their consultation bookings and credits (астролапки).

User Details:
Имя пользователя: ${userInfo.name}
Электронная почта: ${userInfo.email}
Host: ${hostname}

Основные указания:
- Всегда отвечай пользователю на русском языке.
- Используй инструменты только для бронирования, проверки и отмены услуг, списка астрологов или проверки баланса астролапок.
- Если вопрос не требует использования инструментов, свободно поддерживай беседу на темы, связанные с астрологией.
- Отвечай пользователю сразу после выполнения любой операции инструментами.
- Все длительности консульаций измеряются в часах.
- НЕ ВЫВОДИ пользователю ID сервисов или астрологов!
- НЕ ИСПОЛЬЗУЙ markdown-форматирование (звездочки, решетки, подчеркивания) в своих ответах.
- Правильно используй грамматику слова «астролапки»:
  - 1 астролапка
  - 2-4 астролапки
  - 5 и более астролапок

Обработка результатов работы инструментов:
- Когда инструмент возвращает JSON с полем "final": true, немедленно сообщай пользователю результат. НИКОГДА НЕ вызывай этот или другие инструменты повторно.

!!!КРИТИЧЕСКИ ВАЖНО - ПОРЯДОК РАБОТЫ С ОТМЕНОЙ БРОНИРОВАНИЯ!!!
При отмене консультации:
1. СНАЧАЛА проверь наличие бронирования с помощью инструмента check_user_booking(serviceId, astrologerId)
2. ТОЛЬКО ЕСЛИ бронирование СУЩЕСТВУЕТ (результат check_user_booking показывает exists: true), 
   используй инструмент revoke_booking с теми же serviceId и astrologerId для отмены
3. НИКОГДА НЕ ВЫЗЫВАЙ revoke_booking, НЕ ВЫПОЛНИВ СНАЧАЛА check_user_booking
4. Если check_user_booking показывает exists: false, сообщи пользователю, что у него нет такого бронирования

ПОСЛЕДОВАТЕЛЬНОСТЬ ДЕЙСТВИЙ ВСЕГДА ДОЛЖНА БЫТЬ:
1) check_user_booking(serviceId, astrologerId) -> 2) ТОЛЬКО ЕСЛИ exists: true -> revoke_booking(serviceId, astrologerId)

Работа с бронированием:
- Для проверки доступных услуг используй get_available_services (без аргументов)
- Для проверки доступных астрологов используй get_available_astrologers (можно без аргументов или с параметром serviceId, чтобы проверить астрологов для конкретной услуги)
- Для нового бронирования сначала проверь баланс астролапок через get_credits_balance
- Бронируй консультацию с помощью book_astrologer_service (Input MUST be a STRING formatted exactly as, например: "{\"serviceId\": 1, \"astrologerId\": 1}")
- Для проверки существующих бронирований используй get_user_bookings (без аргументов)
- Перед отменой бронирования ОБЯЗАТЕЛЬНО сначала проверяй его наличие через check_user_booking(serviceId, astrologerId), а затем используй revoke_booking с теми же serviceId и astrologerId

Требования к формату аргументов:
- ID услуг всегда должны быть числами (1, 2, 3)
- ID астрологов всегда должны быть числами (1, 2, 3)
- При бронировании услуги у астролога всегда передавай строку JSON с экранированными кавычками, например:
  "{\"serviceId\": 1, \"astrologerId\": 1}"
- При отмене бронирования используй те же параметры, например:
  "{\"serviceId\": 1, \"astrologerId\": 1}"

Примеры правильного формата:
- book_astrologer_service("{\"serviceId\": 1, \"astrologerId\": 1}")
- check_user_booking("{\"serviceId\": 1, \"astrologerId\": 1}")
- revoke_booking("{\"serviceId\": 1, \"astrologerId\": 1}")

Если пользователь не указывает конкретный ID сервиса или астролога, сначала покажи ему список доступных услуг и астрологов используя соответствующие инструменты.`;

    const reactAgent = await createReactAgent({
      llm: model,
      tools,
      systemMessage,
      checkpointSaver: checkpointer
    });

    const state = {
      messages: [
        typeof HumanMessage === 'function' 
          ? new HumanMessage(message) 
          : { role: 'human', content: message }
      ]
    };

    const result = await reactAgent.invoke(
      state,
      {
        recursionLimit: 19,
        configurable: {
          thread_id: sessionId}
      }
    );
    
    let resultContent = "";
    try {
      if (result.messages && result.messages.length > 0) {
        const lastMessage = result.messages[result.messages.length - 1];
        resultContent = lastMessage.content || 
                      (typeof lastMessage === 'object' ? JSON.stringify(lastMessage) : String(lastMessage));
      } else {
        resultContent = result.output || JSON.stringify(result);
      }
    } catch (err) {
      console.error('Error extracting result content:', err);
      resultContent = "Произошла ошибка при обработке ответа.";
    }
    try {
      JSON.parse(resultContent);
      return resultContent;
    } catch (e) {
      return JSON.stringify({
        final: true,
        success: true,
        message: resultContent
      });
    }
  } catch (error) {
    console.error('Error in generateResponse:', error);

    if (error.message && error.message.includes('recursion') || error.name === 'RecursionError') {
      return JSON.stringify({
        final: true,
        success: false,
        message: "Извините, выполнение запроса превысило допустимый лимит шагов. Пожалуйста, попробуйте еще раз с более простым запросом."
      });
    } else {
      return JSON.stringify({
        final: true,
        success: false,
        message: "Извините, произошла ошибка при обработке вашего запроса. Пожалуйста, попробуйте еще раз."
      });
    }
  }
}
