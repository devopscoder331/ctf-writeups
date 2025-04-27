import { setSession, getSession, setUser } from './redis.js';

async function createSession(sessionId, hostname) {
  const session = {
    userInfo: null,
    createdAt: Date.now(),
    hostname: hostname 
  };
  
  await setSession(sessionId, session, hostname);
  return { session };
}

export async function getOrCreateSession(sessionId, hostname) {
  let session = await getSession(sessionId, hostname);
  if (!session) {
    return createSession(sessionId, hostname);
  }

  if (!session.userInfo) {
    session.userInfo = null;
  }
  if (!session.createdAt) {
    session.createdAt = Date.now();
  }
  if (!session.hostname) {
    session.hostname = hostname;
  }

  return { session };
}

export async function updateUserInfo(sessionId, updates, hostname) {
  const { session } = await getOrCreateSession(sessionId, hostname);
  const userInfo = session.userInfo || {};
  
  const updatedUserInfo = {
    ...userInfo,
    ...updates
  };
  
  session.userInfo = updatedUserInfo;
  session.hostname = hostname;
  
  await setSession(sessionId, session, hostname);
  if (updates.email) {
    await setUser(updates.email, sessionId, hostname);
  }
  return { session };
}

export async function getUserInfo(sessionId, hostname) {
  const { session } = await getOrCreateSession(sessionId, hostname);
  return session.userInfo;
}

export async function isSessionReady(sessionId, hostname) {
  const userInfo = await getUserInfo(sessionId, hostname);
  return !!(userInfo?.email && userInfo?.name);
} 
