// AI assistance API — suggestions + sentiment (Phase 5)
import axios from '@/plugins/axios'

export const aiApi = {
  // AI suggestions
  generateSuggestion: (conversationId, agentId = null) =>
    axios.post('/ai/suggestions/generate', { conversationId, agentId }),
  getSuggestionsForConversation: (conversationId) =>
    axios.get(`/ai/suggestions/conversation/${conversationId}`),
  getPendingSuggestions: () => axios.get('/ai/suggestions/pending'),
  acceptSuggestion: (id) => axios.post(`/ai/suggestions/${id}/accept`),
  rejectSuggestion: (id, note = null) =>
    axios.post(`/ai/suggestions/${id}/reject`, { note }),

  // Sentiment
  scoreConversation: (conversationId) =>
    axios.post(`/ai/sentiment/score/${conversationId}`),
  getLatestSentiment: (conversationId) =>
    axios.get(`/ai/sentiment/conversation/${conversationId}`),
}

export default aiApi
