// Debug JWT token to see what's inside
export const debugJWT = (token) => {
  if (!token) {
    return;
  }
  try {
    const parts = token.split('.');
    if (parts.length !== 3) {
      return;
    }
    const payload = JSON.parse(atob(parts[1]));
    return {
      sub: payload.sub,
      userId: payload.userId,
      id: payload.id,
      user_id: payload.user_id,
      username: payload.username,
      email: payload.email
    };
  } catch (error) {
    console.error('JWT parsing error:', error);
    return null;
  }
};
// Get user ID from JWT token with multiple field attempts
export const getUserIdFromJWT = (token) => {
  if (!token) return 1;
  try {
    const payload = JSON.parse(atob(token.split('.')[1]));
    // Try multiple possible fields for user ID
    return parseInt(payload.sub) || 
           parseInt(payload.userId) || 
           parseInt(payload.id) || 
           parseInt(payload.user_id) || 
           parseInt(payload.subject) || 
           1;
  } catch (error) {
    return 1;
  }
};
