// Debug JWT token to see what's inside
export const debugJWT = (token) => {
  if (!token) {
    console.log('No token provided');
    return;
  }
  
  try {
    const parts = token.split('.');
    if (parts.length !== 3) {
      console.log('Invalid JWT format');
      return;
    }
    
    const payload = JSON.parse(atob(parts[1]));
    console.log('JWT Payload:', payload);
    console.log('Possible user IDs:', {
      sub: payload.sub,
      userId: payload.userId,
      id: payload.id,
      user_id: payload.user_id,
      username: payload.username,
      email: payload.email
    });
    
    return payload;
  } catch (error) {
    console.error('Error parsing JWT:', error);
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
    console.error('Error getting user ID from JWT:', error);
    return 1;
  }
};
