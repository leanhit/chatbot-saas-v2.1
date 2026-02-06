interface CacheItem<T> {
    data: T;
    expiresAt: number;
}

export class CacheManager {
    private static readonly PREFIX = 'chat_cache_';
    private static readonly DEFAULT_TTL = 5 * 60 * 1000; // 5 minutes in milliseconds

    private static getKey(key: string): string {
        return `${this.PREFIX}${key}`;
    }

    static set<T>(key: string, data: T, ttl: number = this.DEFAULT_TTL): void {
        try {
            const item: CacheItem<T> = {
                data,
                expiresAt: Date.now() + ttl
            };
            localStorage.setItem(this.getKey(key), JSON.stringify(item));
        } catch (error) {
            console.error('Error saving to cache:', error);
        }
    }

    static get<T>(key: string): T | null {
        try {
            const cached = localStorage.getItem(this.getKey(key));
            if (!cached) return null;

            const item = JSON.parse(cached) as CacheItem<T>;
            
            // Check if item is expired
            if (Date.now() > item.expiresAt) {
                this.remove(key);
                return null;
            }

            return item.data;
        } catch (error) {
            console.error('Error reading from cache:', error);
            return null;
        }
    }

    static remove(key: string): void {
        try {
            localStorage.removeItem(this.getKey(key));
        } catch (error) {
            console.error('Error removing from cache:', error);
        }
    }

    static clear(): void {
        try {
            Object.keys(localStorage).forEach(key => {
                if (key.startsWith(this.PREFIX)) {
                    localStorage.removeItem(key);
                }
            });
        } catch (error) {
            console.error('Error clearing cache:', error);
        }
    }

    // Specific cache keys for our use case
    static getConversationsKey(params: Record<string, any> = {}): string {
        const sortedParams = Object.entries(params)
            .sort(([a], [b]) => a.localeCompare(b))
            .map(([k, v]) => `${k}=${v}`)
            .join('&');
        return `conversations_${sortedParams || 'all'}`;
    }

    static getMessagesKey(conversationId: string | number): string {
        return `messages_${conversationId}`;
    }
}
