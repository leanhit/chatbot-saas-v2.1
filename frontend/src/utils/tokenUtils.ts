// Token utility functions for Identity Hub JWT handling

export const extractTenantIdsFromToken = (token: string): number[] => {
    try {
        const payload = JSON.parse(atob(token.split('.')[1]));
        return payload.tenant_ids || [];
    } catch (error) {
        console.error('Failed to extract tenant_ids from token:', error);
        return [];
    }
};

export const extractUserIdFromToken = (token: string): number | null => {
    try {
        const payload = JSON.parse(atob(token.split('.')[1]));
        return parseInt(payload.sub) || null;
    } catch (error) {
        console.error('Failed to extract user_id from token:', error);
        return null;
    }
};

export const isTokenExpired = (token: string): boolean => {
    try {
        const payload = JSON.parse(atob(token.split('.')[1]));
        return Date.now() >= payload.exp * 1000;
    } catch (error) {
        console.error('Failed to check token expiration:', error);
        return true;
    }
};

export const getTokenExpirationTime = (token: string): number | null => {
    try {
        const payload = JSON.parse(atob(token.split('.')[1]));
        return payload.exp || null;
    } catch (error) {
        console.error('Failed to extract token expiration:', error);
        return null;
    }
};
