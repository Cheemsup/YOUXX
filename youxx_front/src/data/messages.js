const MESSAGES_KEY = 'systemMessages'

export const getMessages = () => {
  const messagesStr = localStorage.getItem(MESSAGES_KEY)
  if (messagesStr) {
    return JSON.parse(messagesStr)
  }
  return []
}

export const saveMessages = (messages) => {
  localStorage.setItem(MESSAGES_KEY, JSON.stringify(messages))
}

export const getUnreadCount = () => {
  const messages = getMessages()
  return messages.filter(m => !m.isRead).length
}

export const markMessageAsRead = (messageId) => {
  const messages = getMessages()
  const msg = messages.find(m => m.id === messageId)
  if (msg) {
    msg.isRead = true
    saveMessages(messages)
    return true
  }
  return false
}

export const markConversationAsRead = (conversationId) => {
  const messages = getMessages()
  let hasChanges = false
  messages.forEach(msg => {
    if (msg.conversationId === conversationId && !msg.isRead) {
      msg.isRead = true
      hasChanges = true
    }
  })
  if (hasChanges) {
    saveMessages(messages)
  }
}

export const sendMessage = (conversationId, from, fromName, content) => {
  const messages = getMessages()
  const newMessage = {
    id: `msg${Date.now()}`,
    from,
    fromName,
    avatar: '',
    content,
    time: new Date().toISOString(),
    isRead: false,
    conversationId
  }
  messages.push(newMessage)
  saveMessages(messages)
  return newMessage
}

export const sendUserMessage = (username, content) => {
  const conversationId = `conv_${username}`
  return sendMessage(conversationId, 'user', username, content)
}

export const getConversations = () => {
  const messages = getMessages()
  const conversations = {}
  
  messages.forEach(msg => {
    if (!conversations[msg.conversationId]) {
      conversations[msg.conversationId] = {
        id: msg.conversationId,
        from: msg.from,
        fromName: msg.fromName,
        avatar: msg.avatar,
        lastMessage: msg.content,
        lastTime: msg.time,
        unreadCount: 0
      }
    }
    if (!msg.isRead && msg.from !== 'admin') {
      conversations[msg.conversationId].unreadCount++
    }
    if (new Date(msg.time) > new Date(conversations[msg.conversationId].lastTime)) {
      conversations[msg.conversationId].lastMessage = msg.content
      conversations[msg.conversationId].lastTime = msg.time
    }
  })
  
  return Object.values(conversations).sort((a, b) => 
    new Date(b.lastTime) - new Date(a.lastTime)
  )
}

export const getConversationMessages = (conversationId) => {
  const messages = getMessages()
  return messages
    .filter(msg => msg.conversationId === conversationId)
    .sort((a, b) => new Date(a.time) - new Date(b.time))
}
