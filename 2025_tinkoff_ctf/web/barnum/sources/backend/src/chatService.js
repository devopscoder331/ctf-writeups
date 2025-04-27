import { getUserInfo } from './sessionService.js';
import { generateResponse } from './langchain.js';
import { saveChatMessage } from './redis.js';

export async function processMessage(message, sessionId, hostname) {
  try {    
    const userInfo = await getUserInfo(sessionId, hostname);
    
    if (!userInfo) {
      return {
        success: false,
        message: "Session information not found. Please refresh the page."
      };
    }
    await saveChatMessage(sessionId, {
      text: message,
      sender: 'user',
      timestamp: Date.now()
    }, hostname);

    try {
      const response = await generateResponse(message, userInfo, sessionId, hostname);
      let credits_updated = false;
      let new_balance = null;
      const responseData = JSON.parse(response);
      const responseText = responseData.message;
      if (responseData.credits_update && responseData.new_balance !== undefined) {
        credits_updated = true;
        new_balance = responseData.new_balance;
      }

      await saveChatMessage(sessionId, {
        text: responseText,
        sender: 'bot',
        special: responseText.includes('tctf{'),
        timestamp: Date.now()
      }, hostname);
      
      return {
        success: responseData.success !== false,
        message: responseText,
        credits_updated,
        new_balance
      };
    } catch (error) {
      console.error('Error generating response:', {
        error: error.message,
        stack: error.stack,
        sessionId,
        hostname,
        userInfo
      });
      
      const errorMessage = "Произошла ошибка при обработке вашего запроса. Пожалуйста, попробуйте еще раз.";

      await saveChatMessage(sessionId, {
        text: errorMessage,
        sender: 'bot',
        timestamp: Date.now()
      }, hostname);
      
      return {
        success: false,
        message: errorMessage
      };
    }
  } catch (error) {
    console.error('Error in processMessage:', {
      error: error.message,
      stack: error.stack,
      message,
      sessionId,
      hostname
    });
    throw error;
  }
} 
